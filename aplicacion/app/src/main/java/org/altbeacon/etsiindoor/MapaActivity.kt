package org.altbeacon.etsiindoor

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import org.altbeacon.utils.BaseActivity

class MapaActivity : BaseActivity() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mapa_activity)
        setupToolbar(R.id.toolbar)

        val webView: WebView = findViewById(R.id.webView)


        // Configuración del WebView
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true

        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                view?.loadUrl(request?.url.toString())
                return true
            }
        }

        webView.loadUrl("https://ap.cs.ucy.ac.cy/viewer/?buid=building_3f91a8fd-ca7b-4d7e-bc04-626e2623d229_1704557723480&floor=0")


    }
}
