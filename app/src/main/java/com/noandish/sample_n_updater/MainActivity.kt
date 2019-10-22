package com.noandish.sample_n_updater

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.noandish.nupdate.Updater

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Updater.install(this)
    }
}
