package com.vibecoder.ui.github

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.vibecoder.R

class GitHubAuthActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val uri = intent.data
        if (uri != null && uri.scheme == "vibecoder" && uri.host == "callback") {
            // Handle OAuth callback if needed
            // For device flow, we don't actually use a callback URL
        }
        
        finish()
    }
}