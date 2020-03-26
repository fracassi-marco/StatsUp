package com.statsup

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v4.view.GravityCompat.START
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
import com.statsup.strava.StravaLoginActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStreamReader

private const val STRAVA_REQUEST_CODE = 1001
private const val WEIGHT_IMPORT_REQUEST_CODE = 1003

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
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
                openFragment(menuItem.title, ActivityStatsFragment())
            }
            R.id.nav_import_from_strava -> {
                val intent = StravaLogin(applicationContext, Confs(applicationContext).stravaClientId())
                    .makeIntent()
                startActivityForResult(intent, STRAVA_REQUEST_CODE)
            }
            R.id.nav_weight_stats -> {
                openFragment(menuItem.title, WeightStatsFragment())
            }
            R.id.nav_weight_history->{
                openFragment(menuItem.title, WeightHistoryFragment())
            }
            R.id.nav_weight_import -> {
                val openWeightCsvIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                openWeightCsvIntent.addCategory(Intent.CATEGORY_OPENABLE)
                openWeightCsvIntent.type = "*/*"
                startActivityForResult(openWeightCsvIntent, WEIGHT_IMPORT_REQUEST_CODE)
            }
            R.id.nav_configurations -> {
                openFragment(menuItem.title, ConfigurationsFragment())
            }
        }

        findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawer(START)
        return true
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
            StravaActivities(applicationContext, code, Confs(applicationContext)) {
                hideProgressBar()
                openDefaultFragment()
            }.execute()
        }

        else if (requestCode == WEIGHT_IMPORT_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            showProgressBar()
            try {
                val inputStream = contentResolver.openInputStream(data.data!!)
                val reader = BufferedReader(InputStreamReader(inputStream!!))
                CsvWeights(applicationContext, reader){
                    hideProgressBar()
                }.execute()
            } catch (e: FileNotFoundException) {
                println(e.message)
            }
        }
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

}
