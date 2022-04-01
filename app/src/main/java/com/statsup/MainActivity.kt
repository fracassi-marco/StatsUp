package com.statsup

import android.content.Intent
import android.content.Intent.ACTION_OPEN_DOCUMENT
import android.content.Intent.CATEGORY_OPENABLE
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat.START
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptionsExtension
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.FitnessOptions.ACCESS_WRITE
import com.google.android.gms.fitness.data.DataType.TYPE_WEIGHT
import com.google.android.material.navigation.NavigationView
import com.statsup.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.FileNotFoundException


private const val STRAVA_LOGIN_CODE = 1001
private const val WEIGHT_IMPORT_CODE = 1003
private const val GOOGLE_FIT_PERMISSIONS = 1005

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    private lateinit var binding: ActivityMainBinding
    private lateinit var weightImport: ActivityResultLauncher<Intent>
    private lateinit var stravaLogin: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)

        ActivityRepository.load(applicationContext)
        UserRepository.load(applicationContext)
        WeightRepository.load(applicationContext)
        openDefaultFragment()

        weightImport = registerForActivityResult(StartActivityForResult()) { onResult(WEIGHT_IMPORT_CODE, it) }
        stravaLogin = registerForActivityResult(StartActivityForResult()) { onResult(STRAVA_LOGIN_CODE, it) }
    }

    private fun openDefaultFragment() {
        openFragment(getString(R.string.menu_stats), ActivityStatsFragment())
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(START)) {
            binding.drawerLayout.closeDrawer(START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.nav_stats_dashboard -> {
                openFragment(menuItem.title, ActivityDashboardFragment())
            }
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
                openFragment(menuItem.title, AllTimesMapFragment())
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
                weightImport.launch(Intent(ACTION_OPEN_DOCUMENT).apply {
                    addCategory(CATEGORY_OPENABLE)
                    type = "*/*"
                })
            }
            R.id.nav_weight_fit_export -> {
                val options: GoogleSignInOptionsExtension = FitnessOptions.builder()
                    .addDataType(TYPE_WEIGHT, ACCESS_WRITE)
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
        val intent = StravaLogin(applicationContext, Confs(applicationContext).stravaClientId).makeIntent()
        stravaLogin.launch(intent)
    }

    private fun openFragment(title: CharSequence?, fragment: Fragment) {
        supportActionBar!!.title = title
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .commit()
    }

    private fun onResult(requestCode: Int, result: ActivityResult) {
        if (requestCode == STRAVA_LOGIN_CODE && result.resultCode == RESULT_OK && result.data != null) {
            val code = result.data!!.getStringExtra(StravaLoginActivity.RESULT_CODE)

            showProgressBar()
            scope.launch {
                StravaActivities(applicationContext, code!!, this) {
                    hideProgressBar()
                    openDefaultFragment()
                }.download()
            }
        } else if (requestCode == WEIGHT_IMPORT_CODE && result.resultCode == RESULT_OK && result.data != null) {
            showProgressBar()
            try {
                val data: Intent = result.data!!
                val inputStream = contentResolver.openInputStream(data.data!!)
                CsvWeights(inputStream!!).read(scope, applicationContext) {
                    hideProgressBar()
                    openFragment(getString(R.string.menu_weight_history), WeightHistoryFragment())
                }
            } catch (e: FileNotFoundException) {
                println(e.message)
            }
        } else if (requestCode == GOOGLE_FIT_PERMISSIONS && result.resultCode == RESULT_OK) {
            accessGoogleFif()
        }
    }

    private fun accessGoogleFif() {
        GoogleFitAdapter(this).exportWeight()
    }

    private fun hideProgressBar() {
        binding.progressbar.visibility = View.GONE
        window.clearFlags(FLAG_NOT_TOUCHABLE)
    }

    private fun showProgressBar() {
        binding.progressbar.visibility = View.VISIBLE
        binding.progressbar.bringToFront()
        window.setFlags(FLAG_NOT_TOUCHABLE, FLAG_NOT_TOUCHABLE)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
