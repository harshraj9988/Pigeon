package com.hr9988apps.pigeon

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.adapters.NumberPickerBindingAdapter.setValue
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

/****************** Download your google-services.json from the firebase **************************/

class MainActivity : AppCompatActivity() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var authUid: String? = null

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        if (auth.currentUser != null) {
            authUid = auth.uid

            if (!authUid.isNullOrEmpty()) {
                database.reference.child("presence").child(authUid!!).child("status").setValue("Online")
            }
        }
    }

    override fun onPause() {
        if (auth.currentUser != null) {
            authUid = auth.uid

            if (!authUid.isNullOrEmpty()) {
                database.reference.child("presence").child(authUid!!).child("status").setValue("Offline").addOnCompleteListener {

                }
            }
        }
        super.onPause()
    }

    override fun onDestroy() {

        if (!authUid.isNullOrEmpty()) {
            database.reference.child("presence").child(authUid!!).child("status").setValue("Offline").addOnCompleteListener {

            }
        }
        super.onDestroy()
    }
}
