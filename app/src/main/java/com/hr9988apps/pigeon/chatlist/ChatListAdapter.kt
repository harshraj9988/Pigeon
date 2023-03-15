package com.hr9988apps.pigeon.chatlist

import android.graphics.drawable.Drawable
import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.database.FirebaseDatabase
import com.hr9988apps.pigeon.R
import com.hr9988apps.pigeon.databinding.ChatListItemBinding
import com.hr9988apps.pigeon.user.User
import com.hr9988apps.pigeon.util_functions.ChatListHelperFunctions

private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

class ChatListAdapter(
    private val authUid: String?,
    private val clickListener: ChatListListener,
    private val chatListHelperFunctions: ChatListHelperFunctions,
    private val profileWindow: RelativeLayout,
    private val profileImage: ImageView
) :
    ListAdapter<User, ChatListViewHolder>(UserDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatListViewHolder {
        return ChatListViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ChatListViewHolder, position: Int) {
        val user = getItem(position)
        holder.bind(clickListener, user, authUid, chatListHelperFunctions, profileWindow, profileImage)
    }

}

class ChatListViewHolder private constructor(val binding: ChatListItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(
        clickListener: ChatListListener,
        item: User,
        authUid: String?,
        chatListHelperFunctions: ChatListHelperFunctions,
        profileWindow: RelativeLayout,
        profileImage: ImageView
    ) {



        binding.user = item
        binding.clickListener = clickListener
        binding.name.text = item.name

        var drawable : Drawable? = null

        if (!item.profileImage.isNullOrEmpty()) {
            Glide.with(profileImage).asDrawable().placeholder(R.drawable.progress_bg).load(item.profileImage).into(object : CustomTarget<Drawable>() {
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    drawable = resource
                    binding.profilePic.setImageDrawable(drawable)
                }
                override fun onLoadCleared(placeholder: Drawable?) {
                    drawable = placeholder
                }
            })

        }

        binding.profilePic.setOnClickListener {
            profileImage.setImageDrawable(drawable)
            profileWindow.visibility = View.VISIBLE
        }



        if (!authUid.isNullOrEmpty() && !item.uid.isNullOrEmpty()) {
            //getting the last message
            chatListHelperFunctions.getLastMessage(
                database,
                authUid,
                item,
                binding.lastMessage,
                binding.unreadLastMessage
            )


            //checking if there's an unseen message
            chatListHelperFunctions.checkUnseenCount(
                database,
                authUid,
                item,
                binding.lastMessage,
                binding.unreadLastMessage,
                binding.unseenMessageCount
            )
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
    val longClickListener: (uid: String) -> Boolean,
) {
    fun onClick(user: User) = clickListener(user.name, user.profileImage, user.uid, user.token)
    fun onLongClick(user: User): Boolean = longClickListener(user.uid)
}
