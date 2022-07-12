package com.hr9988apps.pigeon.mediaPlayer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.hr9988apps.pigeon.databinding.MediaItemBinding

private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

class MediaAdapter( private val senderRoom: String, private val receiverRoom: String):
    ListAdapter<Media, MediaViewHolder>(MediaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        return MediaViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val media = getItem(position)
        holder.bind( media,  senderRoom, receiverRoom)
    }
}

class MediaViewHolder private constructor(val binding: MediaItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(
        item: Media,
        senderRoom: String,
        receiverRoom: String
    ) {
        binding.title.text = item.fileName

        binding.title.setOnClickListener {
            database.reference.child("chats")
                .child(senderRoom).child("mediaUrl").setValue(item.fileUrl)
                .addOnSuccessListener {
                    database.reference.child("chats")
                        .child(receiverRoom).child("mediaUrl").setValue(item.fileUrl)
                }
        }

        binding.delete.setOnClickListener {
            database.reference.child("chats").child(senderRoom)
                .child("medias").child(item.fileId)
                .setValue(null).addOnSuccessListener {
                    database.reference.child("chats").child(receiverRoom)
                        .child("medias").child(item.fileId)
                        .setValue(null)
                }
        }
    }

    companion object {
        fun from(parent: ViewGroup): MediaViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = MediaItemBinding.inflate(layoutInflater, parent, false)
            return MediaViewHolder(binding)
        }
    }
}

class MediaDiffCallback : DiffUtil.ItemCallback<Media>() {
    override fun areItemsTheSame(oldItem: Media, newItem: Media): Boolean {
        return oldItem.fileId.equals(newItem.fileId)
    }

    override fun areContentsTheSame(oldItem: Media, newItem: Media): Boolean {
        return oldItem.equals(newItem)
    }

}
