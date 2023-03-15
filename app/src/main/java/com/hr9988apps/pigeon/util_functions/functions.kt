package com.hr9988apps.pigeon.util_functions

import android.content.Context
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hr9988apps.pigeon.chatlist.ChatListFragmentDirections
import com.hr9988apps.pigeon.user.User

class ChatListHelperFunctions {

    fun navigateBackToSingInPage(view: View): Boolean {
        Navigation.findNavController(view)
            .navigate(ChatListFragmentDirections.actionChatListFragment2ToPhoneRegFragment())
        return false
    }

    fun navigateToChatScreen(
        view: View,
        name: String,
        profileImage: String,
        uid: String,
        token: String?
    ): Boolean {
        Navigation.findNavController(view).navigate(
            ChatListFragmentDirections.actionChatListFragment2ToChatScreenFragment2(
                name,
                profileImage,
                uid,
                token
            )
        )
        return false
    }

    fun navigateToProfileScreen(view: View): Boolean {
        Navigation.findNavController(view)
            .navigate(
                ChatListFragmentDirections.actionChatListFragment2ToSetupUserProfileFragment(
                    false
                )
            )
        return false
    }

    fun navigateToSearchScreen(view: View): Boolean {
        Navigation.findNavController(view)
            .navigate(ChatListFragmentDirections.actionChatListFragment2ToSearchFragment())
        return false
    }

    fun hideLoading(loading: ProgressBar, recyclerView: RecyclerView) {
        loading.visibility = View.INVISIBLE
        recyclerView.visibility = View.VISIBLE
    }

    fun showLoading(loading: ProgressBar, recyclerView: RecyclerView) {
        loading.visibility = View.VISIBLE
        recyclerView.visibility = View.INVISIBLE
    }

    fun confirmRemovalOfData(context: Context, action: () -> Unit): Boolean {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Are you sure?")
        builder.setMessage("Your messages will be restored with your contacts once you add them back")
        builder.setPositiveButton(
            "confirm"
        ) { _, _ -> action() }
        builder.setNegativeButton("cancel") { _, _ ->
            return@setNegativeButton
        }
        builder.show()
        return false
    }

    fun confirmDeletingContact(uid: String, context: Context, action: (String) -> Unit): Boolean {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Delete contact ?")
            .setMessage("This contact will be deleted and the chat will be cleared")
            .setPositiveButton("YES") { _, _ ->
                action(uid)
            }
            .setNegativeButton("No") { _, _ ->
                return@setNegativeButton
            }.show()
        return false
    }

    fun showToast(text: String, context: Context): Boolean {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
        return false
    }

    fun getLastMessage(
        database: FirebaseDatabase,
        authUid: String,
        item: User,
        lastMessage: TextView,
        unreadLastMessage: TextView
    ) {
        database.reference.child("lastMessages").child(authUid)
            .child(item.uid).child("lastMsg").addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val msg = snapshot.getValue(String::class.java)
                            if (!msg.isNullOrEmpty()) {
                                lastMessage.text = msg
                                unreadLastMessage.text = msg
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        lastMessage.text = "last message can't be retrieved"
                        unreadLastMessage.text = "last message can't be retrieved"
                    }

                })
    }

    fun checkUnseenCount(
        database: FirebaseDatabase,
        authUid: String,
        item: User,
        lastMessage: TextView,
        unreadLastMessage: TextView,
        unseenCount: TextView
    ) {
        database.reference.child("unseenCount").child(authUid)
            .child(item.uid).child("count")
            .addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val unseen =
                            snapshot.getValue(String::class.java)
                        if (!unseen.isNullOrEmpty()) {
                            if (unseen == "0") {
                                lastMessage.visibility = View.VISIBLE
                                unreadLastMessage.visibility = View.GONE
                                unseenCount.visibility = View.INVISIBLE
                            } else {
                                lastMessage.visibility = View.GONE
                                unreadLastMessage.visibility = View.VISIBLE
                                unseenCount.visibility = View.VISIBLE
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }
}