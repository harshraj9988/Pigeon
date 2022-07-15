package com.hr9988apps.pigeon.chatscreen

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.hr9988apps.pigeon.R
import com.hr9988apps.pigeon.databinding.ItemReceiveBinding
import com.hr9988apps.pigeon.databinding.ItemSentBinding
import com.squareup.picasso.Picasso
import kotlinx.coroutines.NonDisposableHandle.parent

const val ITEM_SENT = 1
const val ITEM_RECEIVE = 2

class MessagesAdapter() :
    ListAdapter<Message, RecyclerView.ViewHolder>(MessageDiffCallback()) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_SENT) {
            SentViewHolder.from(parent)
        } else {
            ReceiveViewHolder.from(parent)

        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message: Message = getItem(position)
        if (holder is SentViewHolder) {
            holder.bind(message)
        } else if (holder is ReceiveViewHolder) {
            holder.bind(message)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message: Message = getItem(position)
        return if (auth.uid?.equals(message.senderId) == true) {
            ITEM_SENT
        } else {
            ITEM_RECEIVE
        }
    }
}

class SentViewHolder private constructor(val binding: ItemSentBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(
        item: Message
    ) {
        binding.sent.text = item.message
        if (item.message == "photo") {
            binding.sentImage.visibility = View.VISIBLE
            Picasso.get().load(item.imageUrl).placeholder(R.drawable.progress_bg_3)
                .fit()
                .centerInside()
                .into(binding.sentImage)
        } else {
            binding.sentImage.visibility = View.GONE
        }
    }

    companion object {
        fun from(parent: ViewGroup): RecyclerView.ViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ItemSentBinding.inflate(layoutInflater, parent, false)
            return SentViewHolder(binding)
        }
    }
}

class ReceiveViewHolder private constructor(val binding: ItemReceiveBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(
        item: Message
    ) {
        binding.receive.text = item.message
        if (item.message == "photo") {
            Picasso.get().load(item.imageUrl).placeholder(R.drawable.progress_bg_3)
                .fit()
                .centerInside()
                .into(binding.receivedImage)
            binding.receivedImage.visibility = View.VISIBLE
        } else {
            binding.receivedImage.visibility = View.GONE
        }
    }

    companion object {
        fun from(parent: ViewGroup): RecyclerView.ViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ItemReceiveBinding.inflate(layoutInflater, parent, false)
            return ReceiveViewHolder(binding)
        }
    }
}

class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
    override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem.message.equals(newItem.message)
    }

    override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem.equals(newItem)
    }
}
