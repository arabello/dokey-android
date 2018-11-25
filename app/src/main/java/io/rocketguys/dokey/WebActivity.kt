package io.rocketguys.dokey

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_web.*

class WebActivity : AppCompatActivity() {
    companion object {
        val INTENT_URL_KEY = "url"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {
                if (progress < 100 && connectProgressBar.visibility == View.GONE) {
                    connectProgressBar.visibility = View.VISIBLE
                }

                connectProgressBar.progress = progress
                if (progress == 100) {
                    connectProgressBar.visibility = View.GONE
                }
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
                Toast.makeText(this@WebActivity, io.rocketguys.dokey.R.string.toast_internet_not_available, Toast.LENGTH_SHORT).show()
            }
        }


        val url: String = if (intent.getStringExtra(INTENT_URL_KEY) == null) getString(R.string.url_dokey_io) else intent.getStringExtra(INTENT_URL_KEY)

        webView.loadUrl(url)
    }
}
