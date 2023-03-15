package com.hr9988apps.pigeon.user

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.hr9988apps.pigeon.R
import com.hr9988apps.pigeon.databinding.FragmentSetupUserProfileBinding
import com.hr9988apps.pigeon.utils.ConnectionLiveData
import com.squareup.picasso.Picasso

class SetupUserProfileFragment : Fragment() {
    /********************************** Global Variables **********************************************/
    private lateinit var binding: FragmentSetupUserProfileBinding
    private lateinit var connectionLiveData: ConnectionLiveData

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private var selectedImage: Uri? = null

    private lateinit var viewModel: SetupUserProfileViewModel

    private lateinit var authUid: String

    private var userProfileImage: String = ""

    private  var isComingFromOtpScreen: Boolean = false

    /***************************************************************************************************/



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_setup_user_profile,
            container,
            false
        )
        isComingFromOtpScreen = false
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[SetupUserProfileViewModel::class.java]

        val args = SetupUserProfileFragmentArgs.fromBundle(requireArguments())
        isComingFromOtpScreen = args.isComingFromOtpScreen

        if(isComingFromOtpScreen){
            binding.backBtn.visibility = View.GONE
        }else{
            binding.backBtn.visibility = View.VISIBLE
        }


        // checking if the device has a network connection or not
        connectionLiveData = ConnectionLiveData(requireContext())
        connectionLiveData.observe(viewLifecycleOwner) { isNetworkAvailable ->
            if (isNetworkAvailable) {
                binding.progressBar.visibility = View.INVISIBLE
                binding.noInternet.visibility = View.INVISIBLE
                binding.mainLayout.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.VISIBLE
                binding.noInternet.visibility = View.VISIBLE
                binding.mainLayout.visibility = View.INVISIBLE
            }
        }

        binding.cardView.cardElevation = 30f

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        authUid = auth.uid!!

        loadPreviouslySavedImage()


        //selecting image from device storage
        val getContent =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                if (uri != null) {
                    binding.userProfilePic.setImageURI(uri)
                    binding.invalidateAll()
                    selectedImage = uri
                }
            }
        binding.userProfilePic.setOnClickListener {
            getContent.launch("image/*")
        }

        binding.doneBtn.setOnClickListener { doneBtnView ->

            binding.progressBar.visibility = View.VISIBLE
            binding.mainLayout.visibility = View.INVISIBLE

            val name: String = binding.nameBox.text.toString()

            if (name.isEmpty()) {
                binding.nameBox.error = "Please type a name"
                return@setOnClickListener
            }


            //uploading the selected image to the firebase
            if(auth.currentUser!=null){
                if (selectedImage != null) {
                    val storageReference: StorageReference =
                        storage.reference.child("profiles").child(auth.uid!!)
                    storageReference.putFile(selectedImage!!).addOnCompleteListener {
                        storageReference.downloadUrl.addOnSuccessListener { imageUri ->
                            val uid: String = auth.uid.toString()
                            val phone: String = auth.currentUser?.phoneNumber.toString()
                            val imageUrl: String = imageUri.toString()
                            val username: String = binding.nameBox.text.toString()

                            val user = User(uid, username, phone, imageUrl)

                            database.reference
                                .child("users")
                                .child(uid)
                                .setValue(user)
                                .addOnSuccessListener {
                                    Navigation.findNavController(doneBtnView)
                                        .navigate(SetupUserProfileFragmentDirections.actionSetupUserProfileFragmentToChatListFragment2())
                                }
                        }
                    }
                } else {
                    val uid: String = auth.uid.toString()
                    val phone: String = auth.currentUser?.phoneNumber.toString()
                    val imageUrl = userProfileImage
                    val user = User(uid, name, phone, imageUrl)

                    database.reference
                        .child("users")
                        .child(uid)
                        .setValue(user)
                        .addOnSuccessListener {
                            Navigation.findNavController(doneBtnView)
                                .navigate(SetupUserProfileFragmentDirections.actionSetupUserProfileFragmentToChatListFragment2())
                        }
                }
            }
        }

        binding.backBtn.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(SetupUserProfileFragmentDirections.actionSetupUserProfileFragmentToChatListFragment2())
        }
    }


    private fun loadPreviouslySavedImage() {

        database.reference.child("users")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChild(authUid)) {
                        val user = snapshot.child(authUid).getValue(User::class.java)
                        if(user!=null){

                            if(isComingFromOtpScreen){
                               if(view!=null) {
                                    Navigation.findNavController(view!!)
                                        .navigate(SetupUserProfileFragmentDirections.actionSetupUserProfileFragmentToChatListFragment2())
                                }}

                            userProfileImage = user.profileImage
                            binding.nameBox.setText(user.name)
                            if(!user.profileImage.isNullOrEmpty()){
                                Picasso.get().load(user.profileImage)
                                    .placeholder(R.drawable.user_icon)
                                    .into(binding.userProfilePic)
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })

    }


}
