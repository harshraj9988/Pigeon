package com.hr9988apps.pigeon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.hr9988apps.pigeon.screens.MainChatListComposable
import com.hr9988apps.pigeon.ui.theme.Pigeon
import com.hr9988apps.pigeon.ui.theme.appBackground

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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            Pigeon {
                Column(
                    modifier = Modifier.fillMaxSize()
                        .background(appBackground).systemBarsPadding()
                ){
                    MainChatListComposable()
                }
            }
        }


    }
}
