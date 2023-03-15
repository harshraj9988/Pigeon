package com.hr9988apps.pigeon.chatlist

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.hr9988apps.pigeon.user.User
import com.hr9988apps.pigeon.utils.AuthState
import kotlinx.coroutines.launch

class ChatListViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    var authUid: String? = null

    private val _authState: MutableLiveData<AuthState> by lazy {
        MutableLiveData<AuthState>(AuthState.SignedIn)
    }
    val authState: LiveData<AuthState> = _authState

    private val _user: MutableLiveData<ArrayList<User>> by lazy {
        MutableLiveData<ArrayList<User>>()
    }
    val user: LiveData<ArrayList<User>> = _user

    private val _loading: MutableLiveData<Boolean> by lazy {
        MutableLiveData(true)
    }
    val loading: LiveData<Boolean> = _loading

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        if (auth.currentUser == null) {
            _authState.value = AuthState.SignedOut
        } else {
            _authState.value = AuthState.SignedIn
            authUid = auth.uid
            updateToken()
            getUsersFromDatabase()
        }
    }

    private fun updateToken() {
        viewModelScope.launch {
            FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                val map: HashMap<String, Any> = HashMap()
                map["token"] = token
                authUid?.let {
                    database.reference.child("users").child(it).updateChildren(map)
                }
            }
        }
    }

    fun setUnseenCountZero(uid: String) {
        viewModelScope.launch {
            authUid?.let {
                database.reference
                    .child("unseenCount")
                    .child(it)
                    .child(uid)
                    .child("count")
                    .setValue("0")
            }
        }
    }

    private fun getUsersFromDatabase() {

        viewModelScope.launch {
            val contacts = HashSet<String>()
            if (authUid != null) {
                _loading.value = true
                database.reference.child("contacts").child(authUid!!).addValueEventListener(object :
                    ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {


                        snapshot.children.forEach {
                            val user = it.key
                            if (user != null) {
                                contacts.add(user)
                            }
                        }
                        getFromContact(contacts)
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
            }
        }
    }


    private fun getFromContact(contacts: HashSet<String>) {
        database.reference.child("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _user.postValue(snapshot.children.map { it.getValue(User::class.java) }
                    .filter { it != null && contacts.contains(it.uid) }
                    .filterNotNull() as ArrayList<User>)
                _loading.postValue(false)
            }

            override fun onCancelled(error: DatabaseError) {
                _loading.postValue(false)
            }
        })
    }

    fun deleteContact(uid: String) {
        viewModelScope.launch {
            if (authUid != null) {
                database.reference.child("contacts").child(authUid!!).child(uid).setValue(null)
                    .addOnCompleteListener {
                        database.reference.child("chats").child("${authUid!!}$uid").setValue(null)
                            .addOnCompleteListener {
                                database.reference.child("lastMessages").child(authUid!!).child(uid)
                                    .setValue(null).addOnCompleteListener {
                                        database.reference.child("unseenCount").child(authUid!!)
                                            .child(uid)
                                            .setValue(null)
                                    }
                            }
                    }
            }
        }
    }

    fun removeMeFromTheServer(): Boolean {
        viewModelScope.launch {
            database.reference.child("contacts").child(auth.uid!!).removeValue { _, _ ->
                database.reference.child("users").child(auth.uid!!).removeValue { _, _ ->
                    signOut()
                }
            }
        }
        return false
    }

    fun signOut(): Boolean {
        auth.signOut()
        checkAuthState()
        return false
    }


}
