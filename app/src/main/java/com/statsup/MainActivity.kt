package com.statsup

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.sweetzpot.stravazpot.authenticaton.api.AccessScope
import com.sweetzpot.stravazpot.authenticaton.api.ApprovalPrompt
import com.sweetzpot.stravazpot.authenticaton.api.StravaLogin
import com.sweetzpot.stravazpot.authenticaton.ui.StravaLoginActivity
import kotlinx.android.synthetic.main.activity_main.*

private const val STRAVA_REQUEST_CODE = 1001

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
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        val fragment = when (menuItem.itemId) {
            R.id.nav_home -> {
                FrequencyFragment()
            }
            R.id.nav_gallery -> {
                val intent = StravaLogin.withContext(applicationContext)
                    .withClientID(Confs(applicationContext).stravaClientId())
                    .withRedirectURI("oauth://com-sportshub")
                    .withApprovalPrompt(ApprovalPrompt.AUTO)
                    .withAccessScope(AccessScope.VIEW_PRIVATE_WRITE)
                    .makeIntent()
                startActivityForResult(intent, STRAVA_REQUEST_CODE)
                FrequencyFragment()
            }
            R.id.nav_slideshow -> {
                FrequencyFragment()
            }
            R.id.nav_tools -> {
                FrequencyFragment()
            }
            R.id.nav_share -> {
                FrequencyFragment()
            }
            R.id.nav_send -> {
                FrequencyFragment()
            }
            else -> {
                FrequencyFragment()
            }
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .commit()

        supportActionBar!!.title = menuItem.title

        findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawer(GravityCompat.START)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == STRAVA_REQUEST_CODE && resultCode == RESULT_OK && data != null) run {
            val code = data.getStringExtra(
                StravaLoginActivity.RESULT_CODE
            )

            StravaActivities(code, Confs(applicationContext)).execute()
        }
    }
}
