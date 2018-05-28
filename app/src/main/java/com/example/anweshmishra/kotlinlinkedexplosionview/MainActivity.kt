package com.example.anweshmishra.kotlinlinkedexplosionview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.example.linkedexplosionview.LinkedExplosionView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LinkedExplosionView.create(this)
    }
}
