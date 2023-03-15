package com.hr9988apps.pigeon.chatlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.hr9988apps.pigeon.R
import com.hr9988apps.pigeon.databinding.FragmentChatListBinding
import com.hr9988apps.pigeon.util_functions.*
import com.hr9988apps.pigeon.utils.AuthState

class ChatListFragment : Fragment() {

    private lateinit var binding: FragmentChatListBinding
    private lateinit var chatListAdapter: ChatListAdapter
    private lateinit var viewModel: ChatListViewModel

    private val chatListHelperFunctions: ChatListHelperFunctions by lazy {
        ChatListHelperFunctions()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat_list, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[ChatListViewModel::class.java]

        viewModel.authState.observe(viewLifecycleOwner) {
            if (it is AuthState.SignedOut) {
                chatListHelperFunctions.navigateBackToSingInPage(requireView())
            }
        }

        chatListAdapter = ChatListAdapter(viewModel.authUid, ChatListListener(
            clickListener = { name, profileImage, uid, token ->

                viewModel.setUnseenCountZero(uid)
                chatListHelperFunctions.navigateToChatScreen(requireView(), name, profileImage, uid, token)

            }, longClickListener = { uid ->
                chatListHelperFunctions.confirmDeletingContact(uid, requireContext(), viewModel::deleteContact)
            }
        ), chatListHelperFunctions, binding.profileWindow, binding.profileImage)

        binding.profileBtn.setOnClickListener {
            binding.profileWindow.visibility = View.GONE
        }

        binding.recyclerView.adapter = chatListAdapter

        viewModel.loading.observe(viewLifecycleOwner) {
            if (it) {
                chatListHelperFunctions.showLoading(binding.loading, binding.recyclerView)
            } else {
                chatListHelperFunctions.hideLoading(binding.loading, binding.recyclerView)
            }
        }

        viewModel.user.observe(viewLifecycleOwner) {
            chatListAdapter.submitList(it)
            chatListAdapter.notifyDataSetChanged()
        }

        binding.chatListToolbar.setOnMenuItemClickListener { menuItem ->
            clickedMenuItem(menuItem)
        }

    }

    private fun clickedMenuItem(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.search_btn -> chatListHelperFunctions.navigateToSearchScreen(requireView())
            R.id.my_profile_btn -> chatListHelperFunctions.navigateToProfileScreen(requireView())
            R.id.groups_btn -> chatListHelperFunctions.showToast("Groups", requireContext())
            R.id.invite_btn -> chatListHelperFunctions.showToast("Invite", requireContext())
            R.id.setting_btn -> chatListHelperFunctions.showToast("Settings", requireContext())
            R.id.sign_out_btn -> viewModel.signOut()
            R.id.remove_me_from_server_btn -> chatListHelperFunctions.confirmRemovalOfData(
                requireContext(),
                viewModel::removeMeFromTheServer
            )
            else -> false
        }
    }
}