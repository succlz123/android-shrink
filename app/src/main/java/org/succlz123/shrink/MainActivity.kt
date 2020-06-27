package org.succlz123.shrink

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import org.succlz123.shrink.app.R

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        (findViewById<View>(R.id.content) as TextView).text = "Go to Second Activity"
        content.setOnClickListener { v: View? ->
            val intent = Intent(this@MainActivity, FirstActivity::class.java)
            this@MainActivity.startActivity(intent)
        }
    }
}