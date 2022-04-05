package com.statsup

import android.content.Intent
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
import com.google.android.material.navigation.NavigationView
import com.statsup.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    private lateinit var binding: ActivityMainBinding
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
        openDefaultFragment()

        stravaLogin = registerForActivityResult(StartActivityForResult()) { onResult(it) }
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

    private fun onResult(result: ActivityResult) {
        if (result.resultCode == RESULT_OK && result.data != null) {
            val code = result.data!!.getStringExtra(StravaLoginActivity.RESULT_CODE)

            showProgressBar()
            scope.launch {
                StravaActivities(applicationContext, code!!, this) {
                    hideProgressBar()
                    openDefaultFragment()
                }.download()
            }
        }
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
