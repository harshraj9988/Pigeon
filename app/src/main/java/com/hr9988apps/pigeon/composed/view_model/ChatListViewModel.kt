package com.hr9988apps.pigeon.composed.view_model

import android.util.Log
import android.util.TimeUtils
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.snapshots
import com.hr9988apps.pigeon.composed.data_classes.Contact
import com.hr9988apps.pigeon.composed.data_classes.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.launch
import java.util.Calendar

const val TAG = "ChatListViewModel"

class ChatListViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference
    private var authUid: String? = null
    private val _users = MutableStateFlow(ArrayList<User>())
    val user: StateFlow<ArrayList<User>> = _users
    private val contacts: HashSet<String> = HashSet()

    private val contactParents = MutableStateFlow(ArrayList<ArrayList<String>>())
    private val map = HashMap<String, ArrayList<Contact>>()

    init {
        viewModelScope.apply {
            launch(Dispatchers.IO) {
                getContactParentsFromDatabase()
            }
            launch(Dispatchers.IO) {
                getUsersFromDatabase()
            }
        }
    }

    private suspend fun getUsersFromDatabase() {
        database.child("users").snapshots.collect { snapshot ->
            _users.value =
                (snapshot.children.map { child -> child.getValue(User::class.java) as User } as ArrayList<User>)
        }
    }

    private suspend fun getContactParentsFromDatabase() {
        database.child("contacts").snapshots.collect { snapshot ->
           contactParents.value = snapshot.children.filter { it.key != null }.map {
               val temp = ArrayList<String>()
               temp.add(it.key!!)
               it.children.filter { child-> child.key!=null }.forEach{x -> temp.add(x.key!!) }
               temp
            } as ArrayList<ArrayList<String>>

            contactParents.value.forEach {
                Log.d(TAG, it.toString())
            }
        }
    }

    //TODO: add last message time to the userContacts field of Realtime DB
    fun resetContact() {

        val usrs = HashMap<String, User>()
        _users.value.forEach {
            usrs[it.uid] = it
        }
        val time = Calendar.getInstance().timeInMillis
        contactParents.value.forEach { list ->
            val temp = ArrayList<Contact>()
            for(i in 1 until list.size) {
                val id = list[i]
                val contact = Contact(
                    uid = id,
                    name = usrs[id]?.name?:"",
                    profileImage = usrs[id]?.profileImage?:"",
                    lastMessage = "Start conversation",
                    unseenCount = 0,
                    lastMessageTime = time
                )
                temp.add(contact)
            }
            map[list[0]] = temp
        }

        map.forEach {
            val id = it.key
            val cntcts = it.value
            val db = database.child("userContacts").child(id)
            cntcts.forEach { ct ->
                db.child(ct.uid).setValue(ct)
            }
        }
    }
}


