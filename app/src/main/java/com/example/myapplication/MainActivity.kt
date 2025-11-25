package com.example.myapplication

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        mediaPlayer = MediaPlayer.create(this, R.raw.funny_toy)
        mediaPlayer.start()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.homeScreenGrid)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val btnStart = findViewById<Button>(R.id.btnStart)
        val btnExit = findViewById<Button>(R.id.btnExit)
        btnStart.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        btnExit.setOnClickListener {
            finishAffinity()
        }

        val btnStats = findViewById<Button>(R.id.btnstats)
        btnStats.setOnClickListener {
            startActivity(Intent(this, PythonActivity::class.java))
        }
    }
    override fun onPause() {
        super.onPause()
        if (this::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        }
    }
}