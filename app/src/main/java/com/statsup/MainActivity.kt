package com.statsup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.squareup.picasso.Picasso
import com.sweetzpot.stravazpot.authenticaton.api.AccessScope
import com.sweetzpot.stravazpot.authenticaton.api.ApprovalPrompt
import com.sweetzpot.stravazpot.authenticaton.api.StravaLogin
import com.sweetzpot.stravazpot.authenticaton.ui.StravaLoginActivity
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStreamReader

private const val STRAVA_REQUEST_CODE = 1001
private const val SIGNIN_REQUEST_CODE = 1002
private const val WEIGHT_IMPORT_REQUEST_CODE = 1003

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var activityStatsFragment: Fragment
    private lateinit var weightStatsFragment: Fragment
    private lateinit var activityHistoryFragment: Fragment
    private lateinit var weightHistoryFragment: Fragment
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

        startLogin()
    }

    private fun openDefaultFragment() {
        openFragment("Elenco", activityHistoryFragment)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.nav_history -> {
                openFragment(menuItem.title, activityHistoryFragment)
            }
            R.id.nav_stats -> {
                openFragment(menuItem.title, activityStatsFragment)
            }
            R.id.nav_import_from_strava -> {
                val intent = StravaLogin.withContext(applicationContext)
                    .withClientID(Confs(applicationContext).stravaClientId())
                    .withRedirectURI("oauth://com-sportshub")
                    .withApprovalPrompt(ApprovalPrompt.AUTO)
                    .withAccessScope(AccessScope.VIEW_PRIVATE_WRITE)
                    .makeIntent()
                startActivityForResult(intent, STRAVA_REQUEST_CODE)
            }
            R.id.nav_weight_stats -> {
                openFragment(menuItem.title, weightStatsFragment)
            }
            R.id.nav_weight_history->{
                openFragment(menuItem.title, weightHistoryFragment)
            }
            R.id.nav_weight_import -> {
                val openWeightCsvIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                openWeightCsvIntent.addCategory(Intent.CATEGORY_OPENABLE)
                openWeightCsvIntent.type = "*/*"
                startActivityForResult(openWeightCsvIntent, WEIGHT_IMPORT_REQUEST_CODE)
            }
            R.id.nav_logout -> {
                UserRepository.cleanListeners()
                ActivityRepository.cleanListeners()
                AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener { startLogin() }
            }
        }

        findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawer(GravityCompat.START)
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
            val code = data.getStringExtra(
                StravaLoginActivity.RESULT_CODE
            )

            showProgressBar()
            StravaActivities(code, Confs(applicationContext)) {
                hideProgressBar()
            }.execute()
        }
        else if (requestCode == SIGNIN_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                initUserListener()
            }
        }
        else if (requestCode == WEIGHT_IMPORT_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            showProgressBar()
            try {
                val inputStream = contentResolver.openInputStream(data.data!!)
                val reader = BufferedReader(InputStreamReader(inputStream!!))
                CsvWeights(reader){
                    hideProgressBar()
                }.execute()
            } catch (e: FileNotFoundException) {
                println("e = ${e}")
            }
        }
    }

    private fun hideProgressBar() {
        progressbar.visibility = View.GONE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun showProgressBar() {
        progressbar.visibility = View.VISIBLE
        progressbar.bringToFront()
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun initUserListener() {
        UserRepository.listen(object : Listener<User> {
            override fun update(subject: User) {
                user = subject
                updateGui()
            }
        })
    }

    private fun updateGui() {
        activityStatsFragment = ActivityStatsFragment()
        weightStatsFragment = WeightStatsFragment()
        activityHistoryFragment = ActivityHistoryFragment()
        weightHistoryFragment = WeightHistoryFragment()
        openDefaultFragment()

        nav_view.getHeaderView(0).findViewById<TextView>(R.id.sidebar_username).text = currentUser()!!.displayName
        nav_view.getHeaderView(0).findViewById<TextView>(R.id.sidebar_email).text = currentUser()!!.email

        for (info in currentUser()!!.providerData) {
            if (info.getProviderId() == "google.com") {
                val image = nav_view.getHeaderView(0).findViewById<CircleImageView>(R.id.sidebar_image)
                Picasso.with(applicationContext)
                    .load(info.photoUrl)
                    .placeholder(android.R.color.darker_gray)
                    .into(image)
                break
            }
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
                SIGNIN_REQUEST_CODE
            )
        } else {
            updateGui()
        }
    }

    private fun currentUser(): FirebaseUser? {
        return FirebaseAuth.getInstance().currentUser
    }
}
