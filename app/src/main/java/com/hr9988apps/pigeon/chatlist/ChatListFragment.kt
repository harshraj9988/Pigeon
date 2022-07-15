package com.hr9988apps.pigeon.chatlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.hr9988apps.pigeon.R
import com.hr9988apps.pigeon.databinding.FragmentChatListBinding
import com.hr9988apps.pigeon.user.User
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet

class ChatListFragment : Fragment() {

    /******************************* Global Variables ************************************************/

    private lateinit var binding: FragmentChatListBinding
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private var users: ArrayList<User> = ArrayList()
    private lateinit var chatListAdapter: ChatListAdapter
    private val contacts: HashSet<String> = HashSet()
    private var authUid: String? = null


    /*************************************************************************************************/

    private lateinit var viewModel: ChatListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat_list, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (auth.currentUser == null) {
            Navigation.findNavController(requireView())
                .navigate(ChatListFragmentDirections.actionChatListFragment2ToPhoneRegFragment())
        }

        authUid = auth.uid

        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            val map: HashMap<String, Any> = HashMap()
            map["token"] = token
            authUid?.let {
                database.reference.child("users").child(it).updateChildren(map)
            }
        }

        viewModel = ViewModelProvider(this)[ChatListViewModel::class.java]

        chatListAdapter = ChatListAdapter(authUid , ChatListListener(
            clickListener = { name, profileImage, uid , token->

                authUid?.let {
                    database.reference
                        .child("unseenCount")
                        .child(it)
                        .child(uid)
                        .child("count")
                        .setValue("0")
                }


                    Navigation.findNavController(requireView()).navigate(
                    ChatListFragmentDirections.actionChatListFragment2ToChatScreenFragment2(
                        name,
                        profileImage,
                        uid,
                        token
                    )
                )
            }, longClickListener = { uid ->
                confirmDeletingContact(uid)
            }
        ))

        chatListAdapter.setHasStableIds(true)

        binding.recyclerView.adapter = chatListAdapter


        getUsersFromDatabase()

        binding.chatListToolbar.setOnMenuItemClickListener { menuItem ->
            clickedMenuItem(menuItem)
        }

    }



    /****************************** Helper Methods ****************************************************/

    private fun getUsersFromDatabase() {

        if (authUid != null) {
            database.reference.child("contacts").child(authUid!!).addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    contacts.clear()

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


    private fun getFromContact(contacts: HashSet<String>) {
        database.reference.child("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                users.clear()
                snapshot.children.forEach {
                    val user = it.getValue(User::class.java)
                    if (user != null && contacts.contains(user.uid)) {
                        users.add(user)
                    }
                }
                chatListAdapter.submitList(users)
                chatListAdapter.notifyDataSetChanged()

                binding.loading.visibility = View.INVISIBLE
                binding.recyclerView.visibility = View.VISIBLE
            }

            override fun onCancelled(error: DatabaseError) {
                binding.loading.visibility = View.INVISIBLE
                binding.recyclerView.visibility = View.VISIBLE
                if (context != null) Toast.makeText(context, "Failed to load", Toast.LENGTH_SHORT)
                    .show()
            }
        })

    }


    private fun clickedMenuItem(menuItem: MenuItem): Boolean {
       return when (menuItem.itemId) {
            R.id.search_btn -> search()
            R.id.my_profile_btn -> myProfile()
            R.id.groups_btn -> groups()
            R.id.invite_btn -> invite()
            R.id.setting_btn -> settings()
            R.id.sign_out_btn -> signOut()
            R.id.remove_me_from_server_btn -> confirmRemovalOfData()
            else -> false
        }
    }

    private fun signOut(): Boolean {
        auth.signOut()
        Navigation.findNavController(requireView())
            .navigate(ChatListFragmentDirections.actionChatListFragment2ToPhoneRegFragment())

        return false
    }

    private fun search(): Boolean {
        Navigation.findNavController(requireView())
            .navigate(ChatListFragmentDirections.actionChatListFragment2ToSearchFragment())
        return false
    }

    private fun myProfile(): Boolean {
        Navigation.findNavController(requireView())
            .navigate(ChatListFragmentDirections.actionChatListFragment2ToSetupUserProfileFragment(false))
        return false
    }

    private fun groups(): Boolean {
        Toast.makeText(context, "Groups", Toast.LENGTH_SHORT).show()
        return false
    }

    private fun invite(): Boolean {
        Toast.makeText(context, "Invite", Toast.LENGTH_SHORT).show()
        return false
    }

    private fun settings(): Boolean {
        return false
    }

    private fun removeMeFromTheServer() {

        database.reference.child("contacts").child(auth.uid!!).removeValue { _, _ ->
            database.reference.child("users").child(auth.uid!!).removeValue { _, _ ->
                signOut()
            }
        }
    }

    private fun confirmRemovalOfData(): Boolean {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Are you sure?")
        builder.setMessage("Your messages will be restored with your contacts once you add them back")
        builder.setPositiveButton(
            "confirm"
        ) { _, _ -> removeMeFromTheServer() }
        builder.setNegativeButton("cancel") { _, _ ->
            return@setNegativeButton
        }
        builder.show()

        return false
    }

    private fun confirmDeletingContact(uid: String): Boolean{
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Delete contact ?")
            .setMessage("This contact will be deleted and the chat will be cleared")
            .setPositiveButton("YES") {_,_ ->
                deleteContact(uid)
            }
            .setNegativeButton("No") {_,_ ->
                return@setNegativeButton
            }.show()

        return false
    }

    private fun deleteContact(uid: String) {
        if(authUid!=null){
            database.reference.child("contacts").child(authUid!!).child(uid).setValue(null).addOnCompleteListener {
                database.reference.child("chats").child("${authUid!!}$uid").setValue(null).addOnCompleteListener {
                    database.reference.child("lastMessages").child(authUid!!).child(uid).setValue(null).addOnCompleteListener {
                        database.reference.child("unseenCount").child(authUid!!).child(uid).setValue(null)
                    }
                }
            }
        }
    }
}
