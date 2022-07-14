package com.hr9988apps.pigeon.chatscreen

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.android.volley.AuthFailureError
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.hr9988apps.pigeon.R
import com.hr9988apps.pigeon.databinding.FragmentChatScreenBinding
import com.hr9988apps.pigeon.user.User
import com.hr9988apps.pigeon.ytfiles.YtActivity
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.*


class ChatScreenFragment : Fragment() {

    /********************** Global Variables **********************************************************/
    private lateinit var binding: FragmentChatScreenBinding

    private lateinit var senderUid: String
    private lateinit var senderName: String
    private lateinit var senderProfileImage: String

    private lateinit var receiverUserName: String
    private lateinit var receiverUserProfileImage: String
    private lateinit var receiverUid: String
    private var receiverToken: String? = null

    private val messages: ArrayList<Message> = ArrayList()
    private lateinit var messagesAdapter: MessagesAdapter

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    private lateinit var senderRoom: String
    private lateinit var receiverRoom: String

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private lateinit var args: ChatScreenFragmentArgs

    private var isReceiverActive: Boolean = false
    private var playReceiveTone: Boolean = false

    private var vibrator: Vibrator? = null

    /**************************************************************************************************/

    private lateinit var viewModel: ChatScreenViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat_screen, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[ChatScreenViewModel::class.java]

        messagesAdapter = MessagesAdapter()
        binding.messageRecyclerView.adapter = messagesAdapter

        args = ChatScreenFragmentArgs.fromBundle(requireArguments())

        receiverUserName = args.name
        receiverUserProfileImage = args.profileImage
        receiverUid = args.uid
        receiverToken = args.token
        senderUid = auth.uid.toString()

        if (!auth.uid.isNullOrEmpty()) {
            database.reference.child("users").child(auth.uid!!).addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        senderName = user.name
                        senderProfileImage = user.profileImage
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
        }

        binding.receiverName.text = receiverUserName
        showOnlineStatus()
        checkReceiverActiveSession()

        if (receiverUserProfileImage.isNotEmpty()) {
            Picasso.get().load(receiverUserProfileImage).placeholder(R.drawable.user_icon)
                .into(binding.receiverProfilePic)
        }

        binding.backBtn.setOnClickListener { view_ ->
            Navigation.findNavController(view_)
                .navigate(ChatScreenFragmentDirections.actionChatScreenFragment2ToChatListFragment2())

            removeUnseenMessage()
        }



        if (context != null) {
            vibrator = context!!.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
        }

        scope.launch {
            getMessagesFromDatabase()
        }

        senderRoom = senderUid + receiverUid
        receiverRoom = receiverUid + senderUid

        binding.chatScreenToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.clear_chat -> confirmChatDeletion()
                R.id.media -> goToMediaPlayer()
                R.id.yt -> goToYouTubePlayer()
                else -> false
            }
        }

        binding.sendBtn.setOnClickListener {
            val messageTxt: String = binding.messageBox.text.toString()
            binding.messageBox.setText("")

            val date = Date()
            val message = Message(messageTxt, senderUid, date.time)

            if (messageTxt.isNotEmpty()) {
                //sending message
                database.reference.child("chats")
                    .child(senderRoom).child("messages").push()
                    .setValue(message).addOnSuccessListener {
                        database.reference.child("chats")
                            .child(receiverRoom).child("messages").push()
                            .setValue(message).addOnSuccessListener {
                                //adding last message
                                addingLastMessage(messageTxt)
                                addToReceiverContact()
                                //sending notification
                                if (senderName.isNotEmpty()) {
                                    if (!receiverToken.isNullOrEmpty()) {
                                        sendNotification(senderName, messageTxt, receiverToken!!)
                                    }
                                }
                            }
                    }
            }
        }
        //uploading image here
        val getContent =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                if (uri != null) {
                    val calendar = Calendar.getInstance()
                    val ref: StorageReference =
                        storage.reference.child("chats").child("${senderUid}_to_${receiverUid}")
                            .child(calendar.timeInMillis.toString())

                    binding.uploading.visibility = View.VISIBLE
                    binding.uploadingtxt.visibility = View.VISIBLE

                    ref.putFile(uri).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            ref.downloadUrl.addOnSuccessListener { fileUri ->
                                if (fileUri != null) {
                                    val filePath: String = fileUri.toString()

                                    val date = Date()
                                    val message = Message("photo", senderUid, date.time)
                                    message.imageUrl = filePath
                                    database.reference.child("chats")
                                        .child(senderRoom).child("messages").push()
                                        .setValue(message).addOnSuccessListener {
                                            database.reference.child("chats")
                                                .child(receiverRoom).child("messages").push()
                                                .setValue(message).addOnSuccessListener {
                                                    //adding last message
                                                    addingLastMessage("photo")
                                                    addToReceiverContact()
                                                    //sending notification
                                                    if (senderName.isNotEmpty()) {
                                                        if (!receiverToken.isNullOrEmpty()) {
                                                            sendNotification(
                                                                senderName,
                                                                "photo",
                                                                receiverToken!!
                                                            )
                                                        }
                                                    }
                                                }
                                        }
                                    binding.uploading.visibility = View.GONE
                                    binding.uploadingtxt.visibility = View.GONE

                                }
                            }
                        }
                    }
                }
            }

        binding.insertImageBtn.setOnClickListener {
            getContent.launch("image/*")
        }


        binding.messageRecyclerView.addOnLayoutChangeListener { _, _, top, _, bottom, _, oldTop, _, oldBottom ->
            if (bottom != oldBottom || top != oldTop) {
                binding.messageRecyclerView.postDelayed({
                    if (messages.size > 1) {
                        binding.messageRecyclerView.smoothScrollToPosition(messages.size - 1)
                    }
                }, 100)
            }
        }

    }


    override fun onResume() {
        playReceiveTone = false
        super.onResume()
        activateSession()
    }

    override fun onPause() {
        deactivateSession()
        removeUnseenMessage()
        vibrator?.cancel()
        job.cancel()
        super.onPause()
    }


    /*********************************** Helper Methods **********************************************/

    private fun addingLastMessage(messageTxt: String) {
        database.reference.child("lastMessages").child(receiverUid)
            .child(senderUid).child("lastMsg")
            .setValue(messageTxt).addOnCompleteListener {

                database.reference.child("lastMessages").child(senderUid)
                    .child(receiverUid).child("lastMsg")
                    .setValue(messageTxt).addOnCompleteListener {

                        //indicating theres an unseen message
                        settingUnseenCountForReceiver()
                    }
            }
    }

    private fun settingUnseenCountForReceiver() {
        database.reference.child("unseenCount")
            .child(receiverUid)
            .child(senderUid).child("count")
            .setValue("1")
    }

    private fun removeUnseenMessage() {
        database.reference.child("unseenCount")
            .child(senderUid)
            .child(receiverUid).child("count")
            .setValue("0")
    }

    private fun getMessagesFromDatabase() {
        database.reference.child("chats")
            .child(senderRoom)
            .child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messages.clear()
                    snapshot.children.forEach {
                        val message: Message? = it.getValue(Message::class.java)
                        if (message != null) {
                            messages.add(message)
                        }
                    }
                    messagesAdapter.submitList(messages)
                    if (messages.size > 0) {
                        messagesAdapter.notifyItemInserted(messages.size - 1)
                        binding.messageRecyclerView.scrollToPosition(messages.size - 1)
                    } else {
                        messagesAdapter.notifyDataSetChanged()
                    }


                    if (playReceiveTone) {
                        if (messages.size > 0) {
                            if (messages[messages.size - 1].senderId == receiverUid) {
                                vibrateOnMessageReceive()

                            }
                        }
                    }

                    playReceiveTone = true
                    binding.messageLoading.visibility = View.INVISIBLE
                    binding.messageRecyclerView.visibility = View.VISIBLE
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

    private fun confirmChatDeletion(): Boolean {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Are you sure?")
        builder.setMessage("This will delete all messages on your end")
        builder.setPositiveButton(
            "clear"
        ) { _, _ -> clearChat() }
        builder.setNegativeButton("cancel") { _, _ ->
            return@setNegativeButton
        }
        builder.show()

        return false
    }

    private fun clearChat() {
        messages.clear()
        database.reference.child("chats")
            .child(senderRoom).child("messages")
            .setValue("").addOnSuccessListener {
                database.reference.child("lastMessages").child(senderUid)
                    .child(receiverUid).child("lastMsg")
                    .setValue("").addOnCompleteListener {

                        removeUnseenMessage()
                    }
            }

    }


    private fun showOnlineStatus() {
        database.reference.child("presence").child(receiverUid).child("status")
            .addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val status = snapshot.getValue(String::class.java)
                            if (!status.isNullOrEmpty()) {
                                if (status == "Online") {
                                    binding.receiverOnlineStatus.visibility = View.VISIBLE
                                } else {
                                    binding.receiverOnlineStatus.visibility = View.GONE
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                })
    }


    private fun sendNotification(name: String, message: String, token: String) {
        try {
            if (context != null && !isReceiverActive) {
                val requestQueue: RequestQueue = Volley.newRequestQueue(context)
                val url = "https://fcm.googleapis.com/fcm/send"
                val data = JSONObject()
                data.put("title", name)
                data.put("body", message)

                val notificationData = JSONObject()
                notificationData.put("notification", data)
                notificationData.put("to", token)

                val request: JsonObjectRequest =
                    object :
                        JsonObjectRequest(Method.POST, url, notificationData, Response.Listener { },
                            Response.ErrorListener { }) {
                        @Throws(AuthFailureError::class)
                        override fun getHeaders(): Map<String, String> {
                            val map: HashMap<String, String> = HashMap()


                        /********** get the FCM api key from the firebase *********************/
                            val ai: ApplicationInfo = context!!.packageManager.getApplicationInfo(
                                context!!.packageName,
                                PackageManager.GET_META_DATA
                            )
                            val key = ai.metaData["fcm_api"]


                            map["Content-Type"] = "application/json"
                            map["Authorization"] = key.toString()
                            return map
                        }
                    }
                requestQueue.add(request)
            }
        } catch (e: Exception) {
        }
    }


    private fun activateSession() {
        database.reference.child("activeSessions").child(senderUid).child(receiverUid)
            .child("activeSession").setValue("1")
    }

    private fun deactivateSession() {
        database.reference.child("activeSessions").child(senderUid).child(receiverUid)
            .child("activeSession").setValue("0")
    }

    private fun checkReceiverActiveSession() {
        database.reference.child("activeSessions").child(receiverUid).child(senderUid)
            .child("activeSession").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val status: String? = snapshot.getValue(String::class.java)
                        if (!status.isNullOrEmpty()) {
                            isReceiverActive = (status == "1")
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })

    }

    private fun vibrateOnMessageReceive() {

        if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val vibrationEffect =
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)

                vibrator!!.cancel()
                vibrator!!.vibrate(vibrationEffect)
            }
        }
    }

    private fun goToMediaPlayer(): Boolean {
        if (view != null) {
            Navigation.findNavController(view!!).navigate(
                ChatScreenFragmentDirections.actionChatScreenFragment2ToMediaFragment(
                    senderUid,
                    senderName,
                    senderProfileImage,
                    senderRoom,
                    receiverUid,
                    receiverUserName,
                    receiverUserProfileImage,
                    receiverRoom,
                    receiverToken
                )
            )
        }
        return false
    }

    private fun goToYouTubePlayer(): Boolean {
        if (context != null) {
            val intent = Intent(context, YtActivity::class.java)
            intent.putExtra("senderUid", senderUid)
            intent.putExtra("senderName", senderName)
            intent.putExtra("senderProfileImage", senderProfileImage)
            intent.putExtra("senderRoom", senderRoom)
            intent.putExtra("receiverUid", receiverUid)
            intent.putExtra("receiverName", receiverUserName)
            intent.putExtra("receiverProfileImage", receiverUserProfileImage)
            intent.putExtra("receiverRoom", receiverRoom)
            intent.putExtra("receiverToken", receiverToken)

            startActivity(intent)
        }
        return false
    }

    private fun addToReceiverContact() {
        database.reference
            .child("contacts")
            .child(receiverUid)
            .child(senderUid)
            .child("count")
            .setValue("")
    }
}
