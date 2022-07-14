package com.hr9988apps.pigeon.ytfiles

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.media3.common.util.Util
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hr9988apps.pigeon.R
import com.hr9988apps.pigeon.databinding.ActivityYtBinding

class YtActivity : YouTubeBaseActivity(), YouTubePlayer.OnInitializedListener,
    YouTubePlayer.OnFullscreenListener, YouTubePlayer.PlaybackEventListener,
    YouTubePlayer.PlayerStateChangeListener {

/******************************* Global Variables *************************************************/
    private lateinit var binding: ActivityYtBinding

    private lateinit var youtubePlayer: YouTubePlayerView
    private var player: YouTubePlayer? = null

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    private lateinit var senderUid: String
    private lateinit var senderName: String
    private lateinit var senderProfileImage: String
    private lateinit var senderRoom: String
    private lateinit var receiverUid: String
    private lateinit var receiverName: String
    private lateinit var receiverProfileImage: String
    private lateinit var receiverRoom: String
    private var receiverToken: String? = null
    private var resume: Int? = null

    private var fullscreen = false

    private var videoId: String? = null
/**************************************************************************************************/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_yt)



        val intent = intent
        getDataFromIntent(intent)

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.receiverName.text = receiverName

        showOnlineStatus()

        youtubePlayer = binding.ytPlayerView

        binding.share.setOnClickListener {
            if (binding.mediaUrl.text.isEmpty()) {
                binding.mediaUrl.error = "No Link"
                return@setOnClickListener
            }
            val videoUrl = binding.mediaUrl.text.toString()
            val test = videoUrl.split('/')
            val id = test[test.size - 1]
            uploadVideoIdToDatabase(id)

        }

        binding.removeVidBtn.setOnClickListener {
            uploadVideoIdToDatabase("")
        }

        fetchVideoId(this)
        syncPlaybackPosition()
        syncPlayPause()


    }



    override fun onResume() {
        activeStatus("1")
        super.onResume()
    }

    override fun onPause() {
        activeStatus("0")
        resume = player?.currentTimeMillis
        super.onPause()
        if (Util.SDK_INT <= 23) {
            releasePlayer()
        }
    }

    override fun onStop() {
        activeStatus("0")
        super.onStop()
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        resume?.let { outState.putInt("resume", it) }
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        resume = savedInstanceState.getInt("resume")
        super.onRestoreInstanceState(savedInstanceState)
    }

    /**************************** helper methods **************************************************/

    private fun getDataFromIntent(intent: Intent) {
        senderUid = intent.getStringExtra("senderUid").toString()
        senderName = intent.getStringExtra("senderName").toString()
        senderProfileImage = intent.getStringExtra("senderProfileImage").toString()
        senderRoom = intent.getStringExtra("senderRoom").toString()
        receiverUid = intent.getStringExtra("receiverUid").toString()
        receiverName = intent.getStringExtra("receiverName").toString()
        receiverProfileImage = intent.getStringExtra("receiverProfileImage").toString()
        receiverRoom = intent.getStringExtra("receiverRoom").toString()
        receiverToken = intent.getStringExtra("receiverToken").toString()
    }

    override fun onInitializationSuccess(
        provider: YouTubePlayer.Provider?,
        player: YouTubePlayer?,
        wasRestored: Boolean
    ) {
        if (!videoId.isNullOrEmpty()) {
            if (!wasRestored) {
                player?.cueVideo(videoId)
            }
            player?.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT)
            player?.setPlaybackEventListener(this)
            player?.setOnFullscreenListener(this)
            player?.setPlaybackEventListener(this)
            player?.setPlayerStateChangeListener(this)

            this.player = player
        }

    }

    override fun onInitializationFailure(
        provider: YouTubePlayer.Provider?,
        player: YouTubeInitializationResult?
    ) {

    }

    override fun onFullscreen(isFullScreen: Boolean) {

        fullscreen = isFullScreen
        doLayout()
    }

    private fun doLayout() {
        val playerParams: LinearLayout.LayoutParams =
            youtubePlayer.layoutParams as LinearLayout.LayoutParams
        if (fullscreen) {
            playerParams.width = LinearLayout.LayoutParams.MATCH_PARENT
            playerParams.height = LinearLayout.LayoutParams.MATCH_PARENT

            binding.ytToolbar.visibility = View.GONE
            binding.layout.visibility = View.GONE
        } else {
            binding.ytToolbar.visibility = View.VISIBLE
            binding.layout.visibility = View.VISIBLE

            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
            playerParams.width = LinearLayout.LayoutParams.MATCH_PARENT
            playerParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
        }

    }

    override fun onPlaying() {
        updatePlaybackState("true")
    }

    override fun onPaused() {
        updatePlaybackState("false")
    }

    override fun onStopped() {

    }

    override fun onBuffering(p0: Boolean) {

    }

    override fun onSeekTo(p0: Int) {
        playbackSync(p0.toString())
    }

    override fun onLoading() {
    }

    override fun onLoaded(p0: String?) {
        resume?.let {
            player!!.seekToMillis(it)
            player!!.play()
        }
    }

    override fun onAdStarted() {

    }

    override fun onVideoStarted() {

    }

    override fun onVideoEnded() {
    }

    override fun onError(p0: YouTubePlayer.ErrorReason?) {
    }


    private fun uploadVideoIdToDatabase(id: String) {
        database.reference.child("chats")
            .child(senderRoom).child("ytVideoId").setValue(id)
            .addOnSuccessListener {
                database.reference.child("chats")
                    .child(receiverRoom).child("ytVideoId").setValue(id)
            }
    }

    private fun fetchVideoId(initializedListener: YouTubePlayer.OnInitializedListener) {
        database.reference.child("chats")
            .child(senderRoom).child("ytVideoId")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        releasePlayer()
                        val id = snapshot.getValue(String::class.java)
                        if (!id.isNullOrEmpty()) {
                            videoId = id

                            /*************** get the android api key from the GCP credentials ********/
                            if(applicationContext!=null){
                                val ai: ApplicationInfo = applicationContext!!.packageManager.getApplicationInfo(applicationContext!!.packageName, PackageManager.GET_META_DATA)
                                val key = ai.metaData["gcp_api"]
                                youtubePlayer.initialize(key.toString(), initializedListener)
                            }


                        } else {
                            releasePlayer()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }





    private fun updatePlaybackState(isPlaying: String) {

        database.reference.child("chats").child(senderRoom).child("ytPlayback")
            .setValue(isPlaying)

    }

    private fun syncPlayPause() {
        database.reference.child("chats").child(receiverRoom).child("ytPlayback")
            .addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val isPlaying = snapshot.getValue(String::class.java)
                            if (!isPlaying.isNullOrEmpty()) {
                                if (isPlaying == "true") {
                                    player?.play()
                                } else {
                                    player?.pause()
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
    }

    private fun playbackSync(value: String) {
        database.reference.child("chats")
            .child(receiverRoom).child("ytSync").setValue(value)
    }

    private fun syncPlaybackPosition() {
        database.reference.child("chats")
            .child(senderRoom).child("ytSync").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val timeString = snapshot.getValue(String::class.java)
                        if (!timeString.isNullOrEmpty()) {
                            val seek = timeString.toInt()
                            player?.seekToMillis(seek)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun releasePlayer() {
        if (player != null) {
            player!!.release()
            player = null
        }
    }


    private fun showOnlineStatus() {
        database.reference.child("chats")
            .child(receiverRoom).child("ytStatus").addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        val status = snapshot.getValue(String::class.java)
                        if(!status.isNullOrEmpty()){
                            if(status == "0"){
                                binding.receiverOnlineStatus.visibility = View.GONE
                            }else if(status == "1"){
                                binding.receiverOnlineStatus.visibility = View.VISIBLE
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun activeStatus(status: String){
        database.reference.child("chats")
            .child(senderRoom).child("ytStatus").setValue(status)
    }
}
