package com.ahmedbadereldin.videotrimmerapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.ahmedbadereldin.videotrimmerapplication.javaCode.NewPostActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnClick.setOnClickListener {
            startActivity(Intent(this, NewPostActivity::class.java))
        }
    }
}