package com.statsup

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.statsup.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    private lateinit var binding: ActivityMainBinding
    private lateinit var stravaLogin: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ActivityRepository.load(applicationContext)

        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_history -> openFragment(ActivityHistoryFragment())
                R.id.nav_stats -> openFragment(ActivityStatsFragment())
                R.id.nav_records -> openFragment(ActivityRecordsFragment())
                R.id.nav_map -> openFragment(AllTimesMapFragment())
                else -> openFragment(ActivityDashboardFragment())
            }

            true
        }

        binding.updateActivities.setOnClickListener {
            startActivitiesImport()
        }

        openDefaultFragment()

        stravaLogin = registerForActivityResult(StartActivityForResult()) { onResult(it) }
    }

    private fun openDefaultFragment() {
        binding.bottomNavigation.menu.findItem(R.id.nav_stats_dashboard).isChecked = true
        openFragment(ActivityDashboardFragment())
    }

    fun startActivitiesImport() {
        val intent = StravaLogin(applicationContext, Confs(applicationContext).stravaClientId).makeIntent()
        stravaLogin.launch(intent)
    }

    private fun openFragment(fragment: Fragment) {
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
