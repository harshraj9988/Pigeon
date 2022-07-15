package com.hr9988apps.pigeon.mediaPlayer

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.Navigation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.hr9988apps.pigeon.R
import com.hr9988apps.pigeon.databinding.FragmentMediaBinding
import java.util.*
import kotlin.collections.ArrayList

class MediaFragment : Fragment() {
    /********************************** Global Variables *********************************************/

    private lateinit var senderUid: String
    private lateinit var senderName: String
    private lateinit var senderProfileImage: String
    private lateinit var senderRoom: String
    private lateinit var receiverUid: String
    private lateinit var receiverName: String
    private lateinit var receiverProfileImage: String
    private lateinit var receiverRoom: String
    private var receiverToken: String? = null

    private lateinit var binding: FragmentMediaBinding
    private lateinit var viewModel: MediaViewModel

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()


    private var player: ExoPlayer? = null

    private var playWhenReady = true
    private var currentItem = 0
    private var playbackPosition = 0L

    private val playbackStateListener: Player.Listener = playbackStateListener()

    private val mediaList: ArrayList<Media> = ArrayList()
    private lateinit var mediaAdapter: MediaAdapter

    /**************************************************************************************************/
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_media, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[MediaViewModel::class.java]

        val args = MediaFragmentArgs.fromBundle(requireArguments())
        senderUid = args.senderUid
        senderName = args.senderName
        senderProfileImage = args.senderProfileImage
        senderRoom = args.senderRoom
        receiverUid = args.receiverUid
        receiverName = args.receiverName
        receiverProfileImage = args.receiverProfileImage
        receiverRoom = args.receiverRoom
        receiverToken = args.receiverToken

        binding.receiverName.text = receiverName

        showOnlineStatus()

        mediaAdapter = MediaAdapter(senderRoom, receiverRoom)
        binding.mediaRecyclerView.adapter = mediaAdapter

        binding.backBtn.setOnClickListener {
            Navigation.findNavController(it).navigate(
                MediaFragmentDirections.actionMediaFragmentToChatScreenFragment2(
                    receiverName,
                    receiverProfileImage,
                    receiverUid,
                    receiverToken
                )
            )
        }

        binding.share.setOnClickListener {
            val url: String = binding.mediaUrl.text.toString()

            if (url.isEmpty()) {
                binding.mediaUrl.error = "Paste an url"
                return@setOnClickListener
            }
            addUrlToDatabase(url)
        }

        //uploading media here
        val getContent =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                if (uri != null) {
                    if (activity != null) {

                        var title = ""

                        activity!!.contentResolver.apply {
                            query(uri, null, null, null, null)?.use { cursor ->
                                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                                cursor.moveToFirst()
                                cursor.getString(nameIndex)
                            }?.let { fileName ->
                                title = fileName
                            }
                        }

                        val mimeType = activity!!.contentResolver.getType(uri)

                        if(!(MimeTypes.isAudio(mimeType) || MimeTypes.isVideo(mimeType))){
                            if(context!=null){
                                Toast.makeText(context, "Please select audio or video", Toast.LENGTH_SHORT).show()
                            }
                            return@registerForActivityResult
                        }

                        if (title.isNotEmpty()) {

                            binding.uploading.visibility = View.VISIBLE
                            binding.mediaRecyclerView.visibility = View.INVISIBLE

                            val ref: StorageReference =
                                storage.reference.child("media").child(senderUid)
                                    .child(title)
                            ref.putFile(uri).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    ref.downloadUrl.addOnSuccessListener { fileUrl ->
                                        if (fileUrl != null) {
                                            val calendar = Calendar.getInstance()
                                            val media = Media(
                                                title,
                                                fileUrl.toString(),
                                                calendar.timeInMillis.toString()
                                            )
                                            if(!media.fileId.isNullOrEmpty()){
                                                database.reference.child("chats").child(senderRoom)
                                                    .child("medias").child(media.fileId)
                                                    .setValue(media).addOnCompleteListener {it1 ->
                                                        if(it1.isSuccessful){
                                                            database.reference.child("chats")
                                                                .child(receiverRoom).child("medias")
                                                                .child(media.fileId).setValue(media)
                                                                .addOnCompleteListener {
                                                                    binding.uploading.visibility =
                                                                        View.GONE
                                                                    binding.mediaRecyclerView.visibility =
                                                                        View.VISIBLE
                                                                }
                                                        }else{
                                                            binding.uploading.visibility = View.GONE
                                                            binding.mediaRecyclerView.visibility = View.VISIBLE
                                                            if(context!=null){
                                                                Toast.makeText(context, "Failed to upload", Toast.LENGTH_SHORT).show()
                                                            }
                                                        }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    binding.uploading.visibility = View.GONE
                                    binding.mediaRecyclerView.visibility = View.VISIBLE
                                    if(context!=null){
                                        Toast.makeText(context, "Failed to upload", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }
                }
            }

        binding.fab.setOnClickListener {
            getContent.launch("*/*")
        }

        fetchMediasFromDatabase()
        fetchTheMediaUrl()
        fetchThePlaybackState()
        syncSeekbar()
        playWhenSynced()
    }

    private fun showOnlineStatus() {
        database.reference.child("chats")
            .child(receiverRoom).child("mediaStatus").addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val status = snapshot.getValue(String::class.java)
                        if (!status.isNullOrEmpty()) {
                            if (status == "0") {
                                binding.receiverOnlineStatus.visibility = View.GONE
                            } else if (status == "1") {
                                binding.receiverOnlineStatus.visibility = View.VISIBLE
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun activeStatus(status: String) {
        database.reference.child("chats")
            .child(senderRoom).child("mediaStatus").setValue(status)
    }

    override fun onPause() {
        activeStatus("0")
        super.onPause()
        if (Util.SDK_INT <= 23) {
            releasePlayer()
        }
    }

    override fun onDetach() {
        activeStatus("0")
        super.onDetach()
        releasePlayer()
        addUrlToDatabase("")
    }

    override fun onResume() {
        activeStatus("1")
        super.onResume()
    }

    private fun addUrlToDatabase(url: String) {
        database.reference.child("chats")
            .child(senderRoom).child("mediaUrl").setValue(url)
            .addOnSuccessListener {
                database.reference.child("chats")
                    .child(receiverRoom).child("mediaUrl").setValue(url)
            }
    }

    private fun fetchTheMediaUrl() {
        database.reference.child("chats")
            .child(senderRoom).child("mediaUrl").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val url = snapshot.getValue(String::class.java)
                        if (!url.isNullOrEmpty()) {
                            releasePlayer()
                            initPlayer(url)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun updatePlaybackState(isPlaying: String) {
        database.reference.child("chats").child(receiverRoom).child("playback").setValue(isPlaying)
    }

    private fun fetchThePlaybackState() {
        database.reference.child("chats").child(senderRoom).child("playback").addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val isPlaying = snapshot.getValue(String::class.java)
                        if (!isPlaying.isNullOrEmpty()) {
                            player?.let { player ->
                                if (isPlaying == "true") {
                                    player.play()
                                } else {
                                    player.pause()
                                }
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun initPlayer(mediaUrl: String) {
        if (context != null) {
            player = ExoPlayer.Builder(context!!).build()
                .also {
                    binding.playerView.player = it

                    val mediaItem: MediaItem = MediaItem.fromUri(mediaUrl)
                    it.setMediaItem(mediaItem)

                    it.playWhenReady = false
                    it.seekTo(currentItem, playbackPosition)
                    it.addListener(playbackStateListener)
                    it.prepare()
                }

        }
    }

    private fun releasePlayer() {
        player?.let {

            playWhenReady = it.playWhenReady
            currentItem = it.currentMediaItemIndex
            playbackPosition = it.currentPosition
            it.removeListener(playbackStateListener)
            it.release()
        }
        player = null
    }


    private fun playbackStateListener() = object : Player.Listener {

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            super.onPositionDiscontinuity(oldPosition, newPosition, reason)
            player?.pause()
            playbackSync(newPosition.positionMs.toString())
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            if (isPlaying) {
                updatePlaybackState("true")
            } else {
                updatePlaybackState("false")
            }
        }
    }


    private fun playbackSync(value: String) {
        database.reference.child("chats")
            .child(receiverRoom).child("sync").setValue(value)
    }

    private fun syncSeekbar() {
        database.reference.child("chats")
            .child(senderRoom).child("sync").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val timeString = snapshot.getValue(String::class.java)
                        if (!timeString.isNullOrEmpty()) {
                            val seek = timeString.toLong()
                            player?.seekTo(seek)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun playWhenSynced() {
        database.reference.child("chats")
            .child(senderRoom).child("sync").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val time1 = snapshot.getValue(String::class.java)
                        if (!time1.isNullOrEmpty()) {
                            database.reference.child("chats")
                                .child(receiverRoom).child("sync").addValueEventListener(object :
                                    ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (snapshot.exists()) {
                                            val time2 = snapshot.getValue(String::class.java)
                                            if (!time2.isNullOrEmpty()) {
                                                if (time1 == time2) {
                                                    player?.play()
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

    private fun fetchMediasFromDatabase(){
        database.reference.child("chats").child(senderRoom)
            .child("medias").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    mediaList.clear()
                    if(snapshot.exists()){
                        snapshot.children.forEach {
                            val media = it.getValue(Media::class.java)
                            if(media!=null){
                                mediaList.add(media)
                            }
                        }
                    }
                    mediaAdapter.submitList(mediaList)
                    mediaAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}
