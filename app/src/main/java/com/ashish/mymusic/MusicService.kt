package com.ashish.mymusic

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.*
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat


//this will help us to play music until we close the notification which is generated during playing the
// music
class MusicService: Service(), AudioManager.OnAudioFocusChangeListener{
    private var myBinder=MyBinder()
    var mediaPlayer:MediaPlayer?=null
    private lateinit var mediaSession:MediaSessionCompat

//    Runnable helps in running the same code baar baar run krne mai
    private lateinit var runnable: Runnable

    lateinit var audioManager: AudioManager


    override fun onBind(p0: Intent?): IBinder {
        mediaSession= MediaSessionCompat(baseContext,"My Music")
        return myBinder
    }


    inner class MyBinder:Binder(){
        fun currentService(): MusicService {
            return this@MusicService
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    fun showNotification(playPauesBtn:Int){

        val intent= Intent(baseContext,MainActivity::class.java)
        val contentIntent=PendingIntent.getActivity(this,0,intent,0)


        val prevIntent=Intent(baseContext,NotificationReceiver::class.java).setAction(ApplicationClass.PREVIOUS)
        val prevPendingIntent=PendingIntent.getBroadcast(baseContext,0,prevIntent,PendingIntent.FLAG_UPDATE_CURRENT)

        val playIntent=Intent(baseContext,NotificationReceiver::class.java).setAction(ApplicationClass.PLAY)
        val playPendingIntent=PendingIntent.getBroadcast(baseContext,0,playIntent,PendingIntent.FLAG_UPDATE_CURRENT)

        val nextIntent=Intent(baseContext,NotificationReceiver::class.java).setAction(ApplicationClass.NEXT)
        val nextPendingIntent=PendingIntent.getBroadcast(baseContext,0,nextIntent,PendingIntent.FLAG_UPDATE_CURRENT)

        val exitIntent=Intent(baseContext,NotificationReceiver::class.java).setAction(ApplicationClass.EXIT)
        val exitPendingIntent=PendingIntent.getBroadcast(baseContext,0,exitIntent,PendingIntent.FLAG_UPDATE_CURRENT)


        val imgArt= getImgArt(PlayerActivity.musicListPA[PlayerActivity.songPosition].path)
       val image= if (imgArt!=null)
        {
            BitmapFactory.decodeByteArray(imgArt,0,imgArt.size)
        }
        else{
            BitmapFactory.decodeResource(resources,R.drawable.music_note)
        }
        val notification = androidx.core.app.NotificationCompat.Builder(baseContext, ApplicationClass.CHANNEL_ID)
            .setContentIntent(contentIntent)
            .setContentTitle(PlayerActivity.musicListPA[PlayerActivity.songPosition].title)
            .setContentText(PlayerActivity.musicListPA[PlayerActivity.songPosition].artist)
            .setSmallIcon(R.drawable.music_icon)
            .setLargeIcon(image)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.sessionToken))
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setVisibility(androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .addAction(R.drawable.previous_icon,"Previous",prevPendingIntent)
            .addAction(playPauesBtn,"Play",playPendingIntent)
            .addAction(R.drawable.next_icon,"Next",nextPendingIntent)
            .addAction(R.drawable.exit_icon,"Exit",exitPendingIntent)
            .build()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            val playbackSpeed = if(PlayerActivity.isPlaying) 1F else 0F
            mediaSession.setMetadata(
                MediaMetadataCompat.Builder()
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mediaPlayer!!.duration.toLong())
                .build())
            val playBackState = PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PLAYING, mediaPlayer!!.currentPosition.toLong(), playbackSpeed)
                .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                .build()
            mediaSession.setPlaybackState(playBackState)
            mediaSession.setCallback(object: MediaSessionCompat.Callback(){
                override fun onSeekTo(pos: Long) {
                    super.onSeekTo(pos)
                    mediaPlayer!!.seekTo(pos.toInt())
                    val playBackStateNew = PlaybackStateCompat.Builder()
                        .setState(PlaybackStateCompat.STATE_PLAYING, mediaPlayer!!.currentPosition.toLong(), playbackSpeed)
                        .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                        .build()
                    mediaSession.setPlaybackState(playBackStateNew)
                }
            })
        }

        startForeground(13, notification)
    }
     fun createMediaPlayer(){
        try {
            if (PlayerActivity.musicService!!.mediaPlayer==null)
                PlayerActivity.musicService!!.mediaPlayer= MediaPlayer()
            PlayerActivity.musicService!!.mediaPlayer!!.reset()
            PlayerActivity.musicService!!.mediaPlayer!!.setDataSource(PlayerActivity.musicListPA[PlayerActivity.songPosition].path)
            PlayerActivity.musicService!!.mediaPlayer!!.prepare()
            PlayerActivity.binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
            PlayerActivity.musicService!!.showNotification(R.drawable.pause_icon)

            //         the below two is for showing the time on the seekbar
            PlayerActivity.binding.tvSeekBarStart.text= formatDuration(mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.binding.tvSeekBarEnd.text= formatDuration(mediaPlayer!!.duration.toLong())

            //         the below two are for seekbar
            PlayerActivity.binding.seekBarPA.progress=0// this is for whenever we play a new song the seekbar will start from beginning
            PlayerActivity.binding.seekBarPA.max=mediaPlayer!!.duration// this is for when the song has reached its end the seekbar will go to the end

            PlayerActivity.nowPlayingId = PlayerActivity.musicListPA[PlayerActivity.songPosition].id


        }catch (e:Exception){return}
    }

    fun seekBarSetup(){
        runnable= Runnable {
            PlayerActivity.binding.tvSeekBarStart.text= formatDuration(mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.binding.seekBarPA.progress=mediaPlayer!!.currentPosition
            Handler(Looper.getMainLooper()).postDelayed(runnable,200)//this will run the runnable code after every 200 millisec
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable,0)//this will start the inner handler
    }

    override fun onAudioFocusChange(focusChange: Int) {
//        if (focusChange<=0){
//            //pause music
//            PlayerActivity.binding.playPauseBtnPA.setIconResource(R.drawable.play_icon)
//            NowPlayingFragment.binding.playPauseBtnNP.setIconResource(R.drawable.play_icon)
//            PlayerActivity.isPlaying = false
//            mediaPlayer!!.pause()
//            showNotification(R.drawable.play_icon)
//        }
//        else{
//            //play music
//            PlayerActivity.binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
//            NowPlaying.binding.playPauseBtnNP.setIconResource(R.drawable.pause_icon)
//            PlayerActivity.isPlaying = true
//            mediaPlayer!!.start()
//            showNotification(R.drawable.pause_icon)
//        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

}