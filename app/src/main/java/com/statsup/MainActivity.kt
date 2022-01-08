package com.statsup

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat.START
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptionsExtension
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStreamReader


const val STRAVA_REQUEST_CODE = 1001
private const val WEIGHT_IMPORT_REQUEST_CODE = 1003
private const val GOOGLE_FIT_PERMISSIONS = 1005

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this,
            drawer_layout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        ActivityRepository.load(applicationContext)
        UserRepository.load(applicationContext)
        WeightRepository.load(applicationContext)
        openDefaultFragment()
    }

    private fun openDefaultFragment() {
        openFragment(getString(R.string.menu_stats), ActivityStatsFragment())
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(START)) {
            drawer_layout.closeDrawer(START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.nav_history -> {
                openFragment(menuItem.title, ActivityHistoryFragment())
            }
            R.id.nav_stats -> {
                openDefaultFragment()
            }
            R.id.nav_records -> {
                openFragment(menuItem.title, ActivityRecordsFragment())
            }
            R.id.nav_map -> {
                startActivity(Intent(this, AllTimesMapActivity::class.java))
            }
            R.id.nav_import_from_strava -> {
                startActivitiesImport()
            }
            R.id.nav_weight_stats -> {
                openFragment(menuItem.title, WeightStatsFragment())
            }
            R.id.nav_weight_history -> {
                openFragment(menuItem.title, WeightHistoryFragment())
            }
            R.id.nav_weight_import -> {
                val openWeightCsvIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                openWeightCsvIntent.addCategory(Intent.CATEGORY_OPENABLE)
                openWeightCsvIntent.type = "*/*"
                startActivityForResult(openWeightCsvIntent, WEIGHT_IMPORT_REQUEST_CODE)
            }
            R.id.nav_weight_fit_export -> {
                val options: GoogleSignInOptionsExtension = FitnessOptions.builder()
                    .addDataType(DataType.TYPE_WEIGHT, FitnessOptions.ACCESS_WRITE)
                    .build()
                val account = GoogleSignIn.getAccountForExtension(applicationContext, options)
                if (!GoogleSignIn.hasPermissions(account, options)) {
                    GoogleSignIn.requestPermissions(this, GOOGLE_FIT_PERMISSIONS, account, options)
                } else {
                    accessGoogleFif()
                }
            }
            R.id.nav_configurations -> {
                openFragment(menuItem.title, ConfigurationsFragment())
            }
        }

        findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawer(START)
        return true
    }

    fun startActivitiesImport() {
        val intent = StravaLogin(applicationContext, Confs(applicationContext).stravaClientId)
            .makeIntent()
        startActivityForResult(intent, STRAVA_REQUEST_CODE)
    }

    private fun openFragment(title: CharSequence?, fragment: Fragment) {
        supportActionBar!!.title = title
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == STRAVA_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val code = data.getStringExtra(StravaLoginActivity.RESULT_CODE)

            showProgressBar()
            scope.launch {
                StravaActivities(applicationContext, code!!, this) {
                    hideProgressBar()
                    openDefaultFragment()
                }.download()
            }
        }

        else if (requestCode == WEIGHT_IMPORT_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            showProgressBar()
            try {
                val inputStream = contentResolver.openInputStream(data.data!!)
                val reader = BufferedReader(InputStreamReader(inputStream!!))
                CsvWeights(applicationContext, reader){
                    hideProgressBar()
                    openFragment(getString(R.string.menu_weight_history), WeightHistoryFragment())
                }.execute()
            } catch (e: FileNotFoundException) {
                println(e.message)
            }
        }

        else if(requestCode == GOOGLE_FIT_PERMISSIONS  && resultCode == RESULT_OK && data != null) {
            accessGoogleFif()
        }
    }

    private fun accessGoogleFif() {
        GoogleFitAdapter(this).exportWeight()
    }

    private fun hideProgressBar() {
        progressbar.visibility = View.GONE
        window.clearFlags(FLAG_NOT_TOUCHABLE)
    }

    private fun showProgressBar() {
        progressbar.visibility = View.VISIBLE
        progressbar.bringToFront()
        window.setFlags(FLAG_NOT_TOUCHABLE, FLAG_NOT_TOUCHABLE)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
