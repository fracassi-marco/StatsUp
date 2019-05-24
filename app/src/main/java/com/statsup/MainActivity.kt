package com.statsup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.sweetzpot.stravazpot.authenticaton.api.AccessScope
import com.sweetzpot.stravazpot.authenticaton.api.ApprovalPrompt
import com.sweetzpot.stravazpot.authenticaton.api.StravaLogin
import com.sweetzpot.stravazpot.authenticaton.ui.StravaLoginActivity
import kotlinx.android.synthetic.main.activity_main.*

private const val STRAVA_REQUEST_CODE = 1001
private const val SIGNIN = 1002

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val frequencyFragment = FrequencyFragment()
    private lateinit var user: User

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

        openFragment("Home", frequencyFragment)
        startLogin()
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
            R.id.nav_import_from_strava -> {
                val intent = StravaLogin.withContext(applicationContext)
                    .withClientID(Confs(applicationContext).stravaClientId())
                    .withRedirectURI("oauth://com-sportshub")
                    .withApprovalPrompt(ApprovalPrompt.AUTO)
                    .withAccessScope(AccessScope.VIEW_PRIVATE_WRITE)
                    .makeIntent()
                startActivityForResult(intent, STRAVA_REQUEST_CODE)
                frequencyFragment
            }

            R.id.nav_logout -> {
                UserRepository.cleanListeners()
                ActivityRepository.cleanListeners()
                AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener { startLogin() }
                frequencyFragment
            }

            else -> {
                frequencyFragment
            }
        }

        openFragment(menuItem.title, fragment)

        findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawer(GravityCompat.START)
        return true
    }

    private fun openFragment(title: CharSequence?, fragment: FrequencyFragment) {
        supportActionBar!!.title = title
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == STRAVA_REQUEST_CODE && resultCode == RESULT_OK && data != null) run {
            val code = data.getStringExtra(
                StravaLoginActivity.RESULT_CODE
            )

            progressbar.visibility = View.VISIBLE
            StravaActivities(code, Confs(applicationContext)){progressbar.visibility = View.INVISIBLE}.execute()
        }
        else if (requestCode == SIGNIN) {
            if (resultCode == Activity.RESULT_OK) {
                initUserListener()
                openFragment("Home", frequencyFragment)
            } else {
                startLogin()
            }
        }
    }

    private fun initUserListener() {
        if (!::user.isInitialized) {
            UserRepository.listen(object : Listener<User> {
                override fun update(subject: User) {
                    user = subject
                    updateGui()
                }
            })
        }
    }

    private fun updateGui() {
        if (::user.isInitialized) {
            nav_view.getHeaderView(0).findViewById<TextView>(R.id.sidebar_username).text = user.name
        }
    }

    private fun startLogin() {
        if (currentUser() == null) {
            startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(
                        arrayListOf(
                            AuthUI.IdpConfig.GoogleBuilder().build()
                        )
                    )
                    .build(),
                SIGNIN
            )
        }
    }

    private fun currentUser(): FirebaseUser? {
        return FirebaseAuth.getInstance().currentUser
    }
}
