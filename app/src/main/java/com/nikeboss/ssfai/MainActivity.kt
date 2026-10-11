package com.nikeboss.ssfai

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private var selectedApkPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnSelect = findViewById<Button>(R.id.btnSelectApk)
        val btnAnalyze = findViewById<Button>(R.id.btnAnalyze)
        val tvPath = findViewById<TextView>(R.id.tvPath)
        val tvResult = findViewById<TextView>(R.id.tvResult)

        btnSelect.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "application/vnd.android.package-archive"
            startActivityForResult(intent, 100)
        }

        btnAnalyze.setOnClickListener {
            val path = selectedApkPath
            if (path == null) {
                tvResult.text = "Сначала выбери APK"
                return@setOnClickListener
            }
            tvResult.text = "Анализирую..."
            sendToAI(path, tvResult)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            selectedApkPath = uri?.path
            findViewById<TextView>(R.id.tvPath).text = "Выбран: $selectedApkPath"
        }
    }

    private fun sendToAI(path: String, tvResult: TextView) {
        val json = """{"code": "APK: $path"}"""
        val body = json.toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("http://141.11.209.158:8000/analyze")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { tvResult.text = "Ошибка: ${e.message}" }
            }

            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string() ?: "Пусто"
                runOnUiThread { tvResult.text = result }
            }
        })
    }
}
