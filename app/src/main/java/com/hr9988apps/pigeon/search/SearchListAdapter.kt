package com.hr9988apps.pigeon.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hr9988apps.pigeon.R
import com.hr9988apps.pigeon.databinding.SearchListItemBinding
import com.hr9988apps.pigeon.user.User

class SearchListAdapter(private val clickListener: SearchListListener) :
    ListAdapter<User, SearchListViewHolder>(SearchDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchListViewHolder {
        return SearchListViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: SearchListViewHolder, position: Int) {
        val user = getItem(position)
        holder.bind(clickListener, user)
    }

}

class SearchListViewHolder private constructor(private val binding: SearchListItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(
        clickListener: SearchListListener,
        item: User
    ) {
        binding.user = item
        binding.clickListener = clickListener
        binding.name.text = item.name

        if (!item.profileImage.isNullOrEmpty()) {
            Glide.with(binding.profilePic).asDrawable().load(item.profileImage)
                .placeholder(R.drawable.user_icon).centerInside().into(binding.profilePic)
        }
    }

    companion object {
        fun from(parent: ViewGroup): SearchListViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = SearchListItemBinding.inflate(layoutInflater, parent, false)
            return SearchListViewHolder(binding)
        }
    }
}

class SearchDiffCallback : DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.uid.equals(newItem.uid)
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.equals(newItem)
    }

}


class SearchListListener(val clickListener: (name: String, profileImage: String, uid: String, token: String?) -> Unit) {
    fun onClick(user: User) = clickListener(user.name, user.profileImage, user.uid, user.token)
}
