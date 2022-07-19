package com.ashish.mymusic

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

//BroadcastReceiver helps in sending a message from one class to another class for when we click at
// notification we should know that which icon of notification has been clicked.

class NotificationReceiver:BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action){
            ApplicationClass.PREVIOUS-> prevNextSong(increment = false,context=context!!)
            ApplicationClass.PLAY-> if (PlayerActivity.isPlaying) pauseMusic() else playMusic()
            ApplicationClass.NEXT-> prevNextSong(increment = true,context=context!!)
            ApplicationClass.EXIT-> {
               exitApplication()//this will take us out of the app
            }

        }
    }
    private fun playMusic(){
        PlayerActivity.isPlaying=true
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        PlayerActivity.musicService!!.showNotification(R.drawable.pause_icon)
        PlayerActivity.binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
        NowPlayingFragment.binding.playPauseBtnNP.setIconResource(R.drawable.pause_icon)
    }

    private fun pauseMusic(){
        PlayerActivity.isPlaying=false
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        PlayerActivity.musicService!!.showNotification(R.drawable.play_icon)
        PlayerActivity.binding.playPauseBtnPA.setIconResource(R.drawable.play_icon)
        NowPlayingFragment.binding.playPauseBtnNP.setIconResource(R.drawable.play_icon)

    }

    private fun prevNextSong(increment:Boolean, context: Context){
        setSongPosition(increment=increment)
        PlayerActivity.musicService!!.createMediaPlayer()
        Glide.with(context)
            .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music_note).centerCrop())
            .into(PlayerActivity.binding.songImgPA)

        PlayerActivity.binding.songNamePA.text= PlayerActivity.musicListPA[PlayerActivity.songPosition].title

        Glide.with(context)
            .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music_note).centerCrop())
            .into(NowPlayingFragment.binding.songImgNP)
        NowPlayingFragment.binding.songNameNP.text=PlayerActivity.musicListPA[PlayerActivity.songPosition].title

        playMusic()

        PlayerActivity.fIndex= favouriteChecker(PlayerActivity.musicListPA[PlayerActivity.songPosition].id)
        if (PlayerActivity.isFavourite)PlayerActivity.binding.favouriteBtnPA.setImageResource(R.drawable.ic_favorite)
        else PlayerActivity.binding.favouriteBtnPA.setImageResource(R.drawable.favourite_empty)
    }
}