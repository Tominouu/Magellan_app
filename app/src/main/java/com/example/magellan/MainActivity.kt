package com.example.magellan

import android.os.Bundle
import android.security.KeyChain
import android.view.View
import android.webkit.*
import androidx.activity.ComponentActivity
import java.security.cert.X509Certificate
import java.security.PrivateKey

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val webView = WebView(this)
        setContentView(webView)

        // 🔥 FULLSCREEN (cache barre haut + bas)
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true

        // Mode desktop
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true

        settings.userAgentString =
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120 Safari/537.36"

        webView.webViewClient = object : WebViewClient() {

            override fun onPageFinished(view: WebView, url: String) {
                view.post {
                    view.evaluateJavascript(
                        """
                        (function() {
                            var body = document.body;
                            if (body) {
                                body.style.zoom = "0.5";
                                body.style.transformOrigin = "top left";
                            }
                        })();
                        """.trimIndent(),
                        null
                    )
                }
            }

            override fun onReceivedSslError(
                view: WebView,
                handler: SslErrorHandler,
                error: android.net.http.SslError
            ) {
                handler.proceed()
            }

            override fun onReceivedClientCertRequest(
                view: WebView,
                request: ClientCertRequest
            ) {
                KeyChain.choosePrivateKeyAlias(
                    this@MainActivity,
                    { alias ->
                        if (alias != null) {
                            val privateKey: PrivateKey? =
                                KeyChain.getPrivateKey(applicationContext, alias)

                            val certChain: Array<X509Certificate>? =
                                KeyChain.getCertificateChain(applicationContext, alias)

                            request.proceed(privateKey, certChain)
                        } else {
                            request.cancel()
                        }
                    },
                    request.keyTypes,
                    request.principals,
                    request.host,
                    request.port,
                    null
                )
            }
        }

        webView.loadUrl("https://192.168.255.100/app")
    }

    // 🔒 bloque bouton retour (mode app drone)
    override fun onBackPressed() {
        // rien → empêche de quitter
    }
}