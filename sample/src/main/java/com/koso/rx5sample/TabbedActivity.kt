package com.koso.rx5sample

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.koso.rx5sample.ui.main.SectionsPagerAdapter
import com.koso.rx5sample.ui.settings.SettingsActivity
import kotlinx.android.synthetic.main.activity_tabbed.*

class TabbedActivity : AppCompatActivity() {

    private val REQUEST_PERMISSION_COARSE_LOCATION = 20

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tabbed)

        initViews()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_tabbed_activity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.settings){
            SettingsActivity.launch(this)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initViews() {
        setSupportActionBar(vToolbar)

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)

        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.offscreenPageLimit = 3
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)

        checkLocationPermission()
    }


    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION),
                REQUEST_PERMISSION_COARSE_LOCATION
            )
        }
    }
}