package com.hr9988apps.pigeon.composed.view_model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.snapshots
import com.hr9988apps.pigeon.composed.data_classes.Contact
import com.hr9988apps.pigeon.composed.data_classes.User
import com.hr9988apps.pigeon.composed.utils.AuthState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

const val TAG = "ChatListViewModel"

class ChatListViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    private var authUid: String? = null

    private val _author = MutableStateFlow<User?>(null)
    val author: StateFlow<User?> = _author

    private val _users = MutableStateFlow(ArrayList<User>())
    val user: StateFlow<ArrayList<User>> = _users

    private val _contacts = MutableStateFlow(ArrayList<Contact>())
    val contact: StateFlow<ArrayList<Contact>> = _contacts


    private val _authState = mutableStateOf<AuthState?>(null)
    val authState: State<AuthState?> = _authState

    init {
        auth.addAuthStateListener {
            _authState.value = if (it.currentUser != null) AuthState.LoggedIn
            else AuthState.LoggedOut
        }
    }

    fun fetchData() {
        authUid = auth.currentUser?.uid
        viewModelScope.apply {
            launch(Dispatchers.IO) {
                getContactsFromDatabase()
            }
            launch(Dispatchers.IO) {
                getUsersFromDatabase()
            }
            launch(Dispatchers.IO) {
                getAuthor()
            }
        }
    }

    private suspend fun getAuthor() {
        authUid?.let { id ->
            database.child("users").child(id).snapshots.collect { snapshot ->
                _author.value = snapshot.getValue(User::class.java) as User
            }
        }
    }

    private suspend fun getUsersFromDatabase() {
        database.child("users").snapshots.collect { snapshot ->
            _users.value =
                (snapshot.children.map { child -> child.getValue(User::class.java) as User } as ArrayList<User>)
        }
    }

    private suspend fun getContactsFromDatabase() {
        authUid?.let {
            database.child("userContacts").child(it).snapshots.collect { snapshot ->
                _contacts.value =
                    (snapshot.children.map { child -> child.getValue(Contact::class.java) as Contact } as ArrayList<Contact>)
            }
        }
    }

    fun resetContact() {
        auth.signOut()
    }

    fun setOnlineStatusActive() {
        authUid?.let {
            database.child("activeStatus").child(it).setValue(true)
        }
    }

    fun setOnlineStatusInactive() {
        authUid?.let {
            database.child("activeStatus").child(it).setValue(false)
        }
    }
}


