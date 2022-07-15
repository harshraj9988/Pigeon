package com.hr9988apps.pigeon.search

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {

    /******************************* Global Variable *********************************************/

    private var showDialog = true

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            scope.launch {
                retrieveUserContactsFromDevice()
            }
        } else {
            if (showDialog) {
                if (activity != null) {
                    val alertView =
                        activity!!.layoutInflater.inflate(R.layout.alert_dialog_neg_layout, null)
                    val checkBox = alertView.findViewById<CheckBox>(R.id.don_t_show_again)
                    checkBox.setOnCheckedChangeListener { compoundButton, _ ->

                        val preferences = activity!!.getPreferences(Context.MODE_PRIVATE)
                            ?: return@setOnCheckedChangeListener
                        with(preferences.edit()) {
                            putBoolean("showDialog", !compoundButton.isChecked)
                            apply()

                        }
                    }
                    AlertDialog.Builder(requireContext())
                        .setView(alertView).setNeutralButton("OK") { _, _ ->
                            binding.switchBtn.visibility = View.INVISIBLE
                            return@setNeutralButton
                        }.show()
                }
            }
        }
    }

    private lateinit var binding: FragmentSearchBinding
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private lateinit var searchListAdapter: SearchListAdapter

    private val searchResults: ArrayList<User> = ArrayList()

    private val usersPhoneDataBase: HashMap<String, User> = HashMap()
    private val usersNameDataBase: HashMap<String, User> = HashMap()

    private var searchSwitch: Boolean = true

    private val contacts: HashSet<String> = HashSet()

    private val usersInContact: ArrayList<User> = ArrayList()

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)


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
            addToContact(name, profileImage, uid, token)
        })


        getFromContact(contacts)



        binding.searchRecyclerView.adapter = searchListAdapter



        try{
            if (activity != null) {
                val sharedPref = activity!!.getPreferences(Context.MODE_PRIVATE)
                showDialog =
                    sharedPref.getBoolean("showDialog", true)
            }
        }catch (e : Exception){
            showDialog = true
        }

        requestPermission()


        binding.backBtn.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(SearchFragmentDirections.actionSearchFragmentToChatListFragment2())
        }

        binding.search.setOnClickListener {
            val phoneNo: String = binding.phoneNumber.text.toString()
            if (searchSwitch) {
                if (phoneNo.length < 10) {
                    binding.phoneNumber.error = "Invalid phone number"
                    searchListAdapter.submitList(usersInContact)
                    searchListAdapter.notifyDataSetChanged()
                    return@setOnClickListener
                }
                val phoneNumber = "${binding.countryCode.text}${phoneNo}"
                searchUser(phoneNumber, usersPhoneDataBase)
            } else {
                searchUser(phoneNo.lowercase(), usersNameDataBase)
            }
        }

        binding.switchBtn.setOnClickListener {

            searchSwitch = !searchSwitch
            if (!searchSwitch) {
                binding.phoneNumber.setText("")
                binding.switchBtn.setImageResource(R.drawable.phone_icon)
                binding.phoneNumber.hint = "Search name"
                binding.phoneNumber.inputType = InputType.TYPE_CLASS_TEXT
                binding.countryCode.visibility = View.INVISIBLE
            } else {
                binding.phoneNumber.setText("")
                binding.switchBtn.setImageResource(R.drawable.name_icon)
                binding.phoneNumber.hint = "Search phone number"
                binding.phoneNumber.inputType = InputType.TYPE_CLASS_PHONE
                binding.countryCode.visibility = View.VISIBLE
            }


        }


    }

    override fun onDetach() {
        job.cancel()
        super.onDetach()
    }

    private fun searchUser(searchedTerm: String, dataset: HashMap<String, User>) {

        if (binding.phoneNumber.text.isEmpty()) {
            binding.phoneNumber.error = "Nothing to search!"
            searchListAdapter.submitList(usersInContact)
            searchListAdapter.notifyDataSetChanged()
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
        if (searchResults.size < 1) {
            searchListAdapter.submitList(usersInContact)
            searchListAdapter.notifyDataSetChanged()
            if (context != null) {
                Toast.makeText(context, "Not Found", Toast.LENGTH_SHORT).show()
            }
        } else {
            searchListAdapter.submitList(searchResults)
            searchListAdapter.notifyDataSetChanged()
        }
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

    private fun retrieveUserContactsFromDevice() {
        var cursor: Cursor? = null
        if (context != null) {

            val contentResolver = context!!.contentResolver
            if (contentResolver != null) {
                try {
                    cursor = contentResolver.query(
                        ContactsContract.Contacts.CONTENT_URI,
                        null,
                        null,
                        null,
                        null
                    )
                } catch (e: Exception) {
                    e.localizedMessage?.let { Log.e("Error on contacts: ", it) }
                }
                if (cursor != null) {
                    if (cursor.count > 0) {
                        while (cursor.moveToNext()) {
                            val hasPhoneNumber: Int =
                                cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                                    .toInt()
                            if (hasPhoneNumber > 0) {
                                val phoneCursor: Cursor? = contentResolver.query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    null,
                                    null,
                                    null,
                                    null
                                )

                                if (phoneCursor != null) {
                                    while (phoneCursor.moveToNext()) {
                                        val rawPhone = phoneCursor.getString(
                                            phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                        )
                                        val phoneParts = rawPhone.split(' ')
                                        val sb: StringBuilder = StringBuilder()
                                        var i = if (phoneParts.size > 2) 1 else 0
                                        sb.append("+91")
                                        while (i < phoneParts.size) {
                                            sb.append(phoneParts[i])
                                            i += 1
                                        }
                                        contacts.add(sb.toString())
                                    }
                                }
                                phoneCursor?.close()
                            }
                        }
                    }
                }
                cursor?.close()
            }
        }

        getFromContact(contacts)
    }

    private fun getFromContact(contacts: HashSet<String>) {
        database.reference.child("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                usersInContact.clear()
                usersNameDataBase.clear()
                usersPhoneDataBase.clear()
                snapshot.children.forEach {
                    val user = it.getValue(User::class.java)
                    if (user != null) {
                        usersPhoneDataBase[user.phoneNumber] = user
                        if (contacts.contains(user.phoneNumber)) {
                            usersInContact.add(user)
                            usersNameDataBase[user.name.lowercase()] = user
                        }
                    }
                }
                searchListAdapter.submitList(usersInContact)
                searchListAdapter.notifyDataSetChanged()

            }

            override fun onCancelled(error: DatabaseError) {
                if (context != null) Toast.makeText(context, "Failed to load", Toast.LENGTH_SHORT)
                    .show()
            }
        })

    }

    private fun requestPermission() {
        if (context != null && activity != null) {
            when {
                ContextCompat.checkSelfPermission(
                    context!!,
                    Manifest.permission.READ_CONTACTS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    scope.launch {
                        retrieveUserContactsFromDevice()
                    }
                }
                ActivityCompat.shouldShowRequestPermissionRationale(
                    activity!!,
                    Manifest.permission.READ_CONTACTS
                ) -> {

                    AlertDialog.Builder(requireContext())
                        .setView(R.layout.alert_dialog_perm_layout)
                        .setNeutralButton("OK") { _, _ ->
                            requestPermissionLauncher.launch(
                                Manifest.permission.READ_CONTACTS
                            )
                        }
                        .show()
                }
                else -> {
                    requestPermissionLauncher.launch(
                        Manifest.permission.READ_CONTACTS
                    )
                }
            }
        }
    }
}
