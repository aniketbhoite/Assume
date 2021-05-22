package com.aniketbhoite.assume

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.aniketbhoite.assume.di.DI
import com.aniketbhoite.assume.viewmodel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private val job = CoroutineScope(Dispatchers.IO)
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = MainViewModel(DI.provideApiService())
    }

    override fun onResume() {
        super.onResume()
        job.launch {
            try {
                val result = viewModel.getPost()
                withContext(Dispatchers.Main) {
                    findViewById<TextView>(R.id.textview).text = result[0].title
                }
            } catch (e: Exception) {
            }
        }

        job.launch {
            try {
                val result = viewModel.getPostById(1)
                withContext(Dispatchers.Main) {
                    findViewById<TextView>(R.id.textview2).text = result.body
                }
            } catch (e: Exception) {
            }
        }

        job.launch {
            try {
                val result = viewModel.getCommentsForPostId(1)
                withContext(Dispatchers.Main) {
                    findViewById<TextView>(R.id.textview3).text = result[0].body
                }
            } catch (e: Exception) {
            }
        }

        job.launch {
            try {
                val result = viewModel.queryCommentsForPostId(2)
                withContext(Dispatchers.Main) {
                    findViewById<TextView>(R.id.textview4).text = result[0].body
                }
            } catch (e: Exception) {
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
