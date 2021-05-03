package com.aniketbhoite.assume

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    override fun onResume() {
        super.onResume()
        (CoroutineScope(Dispatchers.Main)).launch {
            val result =
                withContext(Dispatchers.IO) {
                    ApiService.invoke().getHeadline()
                }
            withContext(Dispatchers.Main) {

                findViewById<TextView>(R.id.textview).text = result.toString()
            }
        }

        CoroutineScope(Dispatchers.Main).launch {
            val result =
                withContext(Dispatchers.IO) {
                    ApiService.invoke().getSport()
                }


            findViewById<TextView>(R.id.textview2).text = result.toString()

        }
    }


}