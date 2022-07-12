package com.hr9988apps.pigeon.search

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hr9988apps.pigeon.R
import com.hr9988apps.pigeon.databinding.FragmentSearchBinding
import com.hr9988apps.pigeon.user.User

class SearchFragment : Fragment() {

    /******************************* Global Variable *********************************************/

    private lateinit var binding: FragmentSearchBinding
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private lateinit var searchListAdapter: SearchListAdapter

    private val searchResults: ArrayList<User> = ArrayList()

    private val usersPhoneDataBase: HashMap<String, User> = HashMap()
    private val usersNameDataBase: HashMap<String, User> = HashMap()

    private var searchSwitch: Boolean = true

    /*********************************************************************************************/

    private lateinit var viewModel: SearchViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[SearchViewModel::class.java]



        searchListAdapter = SearchListAdapter(SearchListListener { name, profileImage, uid, token ->
            binding.loading.visibility = View.VISIBLE
            binding.searchRecyclerView.visibility = View.INVISIBLE
            addToContact(name, profileImage, uid, token)
        })

        binding.searchRecyclerView.adapter = searchListAdapter

        getUsersDatabase()

        binding.backBtn.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(SearchFragmentDirections.actionSearchFragmentToChatListFragment2())
        }

        binding.search.setOnClickListener {

            if (searchSwitch) {
                val phoneNumber = "${binding.countryCode.text}${binding.phoneNumber.text}"
                searchUser(phoneNumber, usersPhoneDataBase)
            } else {
                val name = "${binding.phoneNumber.text}"
                searchUser(name.lowercase(), usersNameDataBase)
            }
        }

        binding.switchBtn.setOnClickListener {

            searchSwitch = !searchSwitch
            if (!searchSwitch) {
                binding.switchBtn.setImageResource(R.drawable.phone_icon)
                binding.phoneNumber.hint = "Search name"
                binding.phoneNumber.inputType = InputType.TYPE_CLASS_TEXT
                binding.countryCode.visibility = View.INVISIBLE
            } else {
                binding.switchBtn.setImageResource(R.drawable.name_icon)
                binding.phoneNumber.hint = "Search phone number"
                binding.phoneNumber.inputType = InputType.TYPE_CLASS_PHONE
                binding.countryCode.visibility = View.VISIBLE
            }


        }
    }

    private fun searchUser(searchedTerm: String, dataset: HashMap<String, User>) {

        if(binding.phoneNumber.text.isEmpty()){
            binding.phoneNumber.error = "Nothing to search!"
            return
        }

        searchResults.clear()
        dataset.keys.forEach {
            if (it.startsWith(searchedTerm)) {
                dataset[it]?.let { user ->
                    searchResults.add(user)

                }
            }
        }
        searchListAdapter.submitList(searchResults)
        searchListAdapter.notifyDataSetChanged()
    }

    private fun addToContact(name: String, profileImage: String, uid: String, token: String?) {

        auth.uid?.let {
            database.reference
                .child("contacts")
                .child(it)
                .child(uid)
                .child("count")
                .setValue("")
                .addOnCompleteListener {

                    Navigation.findNavController(requireView()).navigate(
                        SearchFragmentDirections.actionSearchFragmentToChatScreenFragment2(
                            name,
                            profileImage,
                            uid,
                            token
                        )
                    )
                }
        }
    }

    private fun getUsersDatabase() {
        database.reference.child("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                usersNameDataBase.clear()
                usersPhoneDataBase.clear()
                snapshot.children.forEach {
                    val user = it.getValue(User::class.java)
                    if (user != null) {
                        if (user.uid != auth.uid) {
                            usersPhoneDataBase[user.phoneNumber] = user
                            usersNameDataBase[user.name.lowercase()] = user
                        }
                    }
                }
                binding.loading.visibility = View.INVISIBLE
                binding.searchRecyclerView.visibility = View.VISIBLE
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

}
