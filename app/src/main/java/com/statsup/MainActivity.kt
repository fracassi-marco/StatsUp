package com.statsup

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*

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
}
