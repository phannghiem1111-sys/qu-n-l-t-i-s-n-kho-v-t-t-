package com.xepca.app

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebViewAssetLoader
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private lateinit var web: WebView
    private lateinit var signInClient: GoogleSignInClient
    private val sheetScope = "https://www.googleapis.com/auth/spreadsheets"

    private val signInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            try {
                val account = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    .getResult(ApiException::class.java)
                val email = account?.email ?: ""
                web.evaluateJavascript("onSignedIn(${JSONObject.quote(email)})", null)
            } catch (e: Exception) {
                val msg = e.message ?: "Đăng nhập thất bại"
                web.evaluateJavascript("onSignInFailed(${JSONObject.quote(msg)})", null)
            }
        }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(sheetScope))
            .build()
        signInClient = GoogleSignIn.getClient(this, options)

        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .build()

        web = WebView(this)
        setContentView(web)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView,
                request: WebResourceRequest
            ): WebResourceResponse? = assetLoader.shouldInterceptRequest(request.url)
        }
        web.addJavascriptInterface(Bridge(), "Android")
        web.loadUrl("https://appassets.androidplatform.net/assets/index.html")
    }

    inner class Bridge {
        @JavascriptInterface
        fun signIn() {
            runOnUiThread { signInLauncher.launch(signInClient.signInIntent) }
        }

        @JavascriptInterface
        fun signOut() {
            runOnUiThread { signInClient.signOut() }
        }

        // Gọi từ luồng nền của WebView (không phải luồng chính), nên được phép chặn
        @JavascriptInterface
        fun getToken(): String {
            val account = GoogleSignIn.getLastSignedInAccount(this@MainActivity)?.account
                ?: return ""
            return try {
                GoogleAuthUtil.getToken(this@MainActivity, account, "oauth2:$sheetScope")
            } catch (e: Exception) {
                ""
            }
        }

        @JavascriptInterface
        fun email(): String =
            GoogleSignIn.getLastSignedInAccount(this@MainActivity)?.email ?: ""
    }
}
