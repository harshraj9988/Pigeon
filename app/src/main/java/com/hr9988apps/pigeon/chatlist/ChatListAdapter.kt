package com.hr9988apps.pigeon.chatlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hr9988apps.pigeon.R
import com.hr9988apps.pigeon.databinding.ChatListItemBinding
import com.hr9988apps.pigeon.user.User
import com.squareup.picasso.Picasso

private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

class ChatListAdapter(
    private val authUid: String?,
    private val clickListener: ChatListListener
) :
    ListAdapter<User, ChatListViewHolder>(UserDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatListViewHolder {
        return ChatListViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ChatListViewHolder, position: Int) {
        val user = getItem(position)
        holder.bind(clickListener, user, authUid)
    }

}

class ChatListViewHolder private constructor(val binding: ChatListItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(
        clickListener: ChatListListener,
        item: User,
        authUid: String?
    ) {
        binding.user = item
        binding.clickListener = clickListener
        binding.name.text = item.name

        if (!item.profileImage.isNullOrEmpty()) {
            Picasso.get().load(item.profileImage).placeholder(R.drawable.user_icon)
                .into(binding.profilePic)
        }

        if (!authUid.isNullOrEmpty() && !item.uid.isNullOrEmpty()) {
            //getting the last message
            database.reference.child("lastMessages").child(authUid)
                .child(item.uid).child("lastMsg").addValueEventListener(
                    object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                val lastMessage = snapshot.getValue(String::class.java)
                                if (!lastMessage.isNullOrEmpty()) {
                                    binding.lastMessage.text = lastMessage
                                    binding.unreadLastMessage.text = lastMessage

                                    //checking if there's an unseen message
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
                                                            binding.lastMessage.visibility =
                                                                View.VISIBLE
                                                            binding.unreadLastMessage.visibility =
                                                                View.GONE
                                                            binding.unseenMessageCount.visibility =
                                                                View.INVISIBLE
                                                        } else {
                                                            binding.unreadLastMessage.visibility =
                                                                View.VISIBLE
                                                            binding.lastMessage.visibility =
                                                                View.GONE
                                                            binding.unseenMessageCount.visibility =
                                                                View.VISIBLE
                                                        }
                                                    }
                                                }
                                            }

                                            override fun onCancelled(error: DatabaseError) {
                                            }
                                        })

                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }

                    })
        }
    }

    companion object {
        fun from(parent: ViewGroup): ChatListViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ChatListItemBinding.inflate(layoutInflater, parent, false)
            return ChatListViewHolder(binding)
        }
    }
}

class UserDiffCallback : DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.uid.equals(newItem.uid)
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.equals(newItem)
    }

}


class ChatListListener(
    val clickListener: (name: String, profileImage: String, uid: String, token: String?) -> Unit,
    val longClickListener: (uid: String) -> Boolean
) {
    fun onClick(user: User) = clickListener(user.name, user.profileImage, user.uid, user.token)
    fun onLongClick(user: User): Boolean = longClickListener(user.uid)
}
