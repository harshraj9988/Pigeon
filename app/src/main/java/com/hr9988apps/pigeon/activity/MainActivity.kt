@file:Suppress("FunctionName")
package com.hr9988apps.pigeon.activity

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import com.hr9988apps.pigeon.composed.screens.MainChatListComposable
import com.hr9988apps.pigeon.composed.utils.AuthState
import com.hr9988apps.pigeon.composed.view_model.ChatListViewModel
import com.hr9988apps.pigeon.ui.theme.Pigeon
import com.hr9988apps.pigeon.ui.theme.appBackground

const val TAG = "MainActivity"

/****************** Download your google-services.json from the firebase **************************/

//class MainActivity : AppCompatActivity() {
//
//    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
//    private var authUid: String? = null
//
//    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//    }
//
//    override fun onResume() {
//        super.onResume()
//        if (auth.currentUser != null) {
//            authUid = auth.uid
//
//            if (!authUid.isNullOrEmpty()) {
//                database.reference.child("presence").child(authUid!!).child("status").setValue("Online")
//            }
//        }
//    }
//
//    override fun onPause() {
//        if (auth.currentUser != null) {
//            authUid = auth.uid
//
//            if (!authUid.isNullOrEmpty()) {
//                database.reference.child("presence").child(authUid!!).child("status").setValue("Offline").addOnCompleteListener {
//
//                }
//            }
//        }
//        super.onPause()
//    }
//
//    override fun onDestroy() {
//
//        if (!authUid.isNullOrEmpty()) {
//            database.reference.child("presence").child(authUid!!).child("status").setValue("Offline").addOnCompleteListener {
//
//            }
//        }
//        super.onDestroy()
//    }
//}

class MainActivity : ComponentActivity() {

    private lateinit var chatListViewModel: ChatListViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chatListViewModel = ViewModelProvider(this)[ChatListViewModel::class.java]
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            Pigeon {
                val authState = remember { chatListViewModel.authState }
                Column(
                    modifier = Modifier.fillMaxSize()
                        .background(appBackground).systemBarsPadding()
                ) {
                    // check if user is logged in or not
                    authState.value.apply {
                        when (this) {
                            is AuthState.LoggedIn -> MainChatListComposable(chatListViewModel)
                            is AuthState.LoggedOut -> GoToSignInScreen()
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        chatListViewModel.setOnlineStatusActive()
    }

    override fun onPause() {
        chatListViewModel.setOnlineStatusInactive()
        super.onPause()
    }

    override fun onDestroy() {
        chatListViewModel.setOnlineStatusInactive()
        super.onDestroy()
    }

    @Composable
    private fun GoToSignInScreen() {
        val intent =
            Intent(LocalContext.current, SignUpActivity::class.java)
        startActivity(intent)
        finish()
    }
}
