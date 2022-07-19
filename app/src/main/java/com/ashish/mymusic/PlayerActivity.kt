package com.ashish.mymusic

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ashish.mymusic.databinding.ActivityPlayerBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.system.exitProcess

class PlayerActivity : AppCompatActivity(),ServiceConnection,MediaPlayer.OnCompletionListener {


    companion object{
       lateinit var musicListPA :ArrayList<Music>
       var songPosition:Int=0
//        var mediaPlayer:MediaPlayer?=null
        var isPlaying:Boolean=false
        var musicService :MusicService?=null
        lateinit var binding: ActivityPlayerBinding

//        this is for loop functionality
        var repeat:Boolean=false

//         this is for timer functionality
        var min15:Boolean=false
        var min30:Boolean=false
        var min60:Boolean=false

        var nowPlayingId:String=""

//      this is to make sure that the current song is already in favourites or not
        var isFavourite: Boolean=false

        var fIndex=-1
    }





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.coolPink)
        binding= ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)



//        to catch the intent which we have passed from music adapter
        initializeLayout()

//        for back button
        binding.backBtnPA.setOnClickListener { finish() }


//        for play-pause btn
        binding.playPauseBtnPA.setOnClickListener {
            if (isPlaying)
                pauseMusic()
            else
                playMusic()
        }


//for next and previous btn
        binding.previousBtnPA.setOnClickListener { prevNextSong(increment = false) }
        binding.nextBtnPA.setOnClickListener { prevNextSong(increment = true) }

        binding.seekBarPA.setOnSeekBarChangeListener(@SuppressLint("AppCompatCustomView")
        object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser)
                    musicService!!.mediaPlayer!!.seekTo(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })

//for repeat btn
        binding.repeatBtnPA.setOnClickListener{
            if (!repeat){
                repeat=true
                binding.repeatBtnPA.setColorFilter(ContextCompat.getColor(this,R.color.purple_500))
                Toast.makeText(this,"Repeat is on!!",Toast.LENGTH_SHORT).show()
            }else
            {
                repeat=false
                binding.repeatBtnPA.setColorFilter(ContextCompat.getColor(this,R.color.cool_pink))
            }
        }

//        for equalizer Button
        binding.equalizerBtnPA.setOnClickListener {
            try {
                val eqIntent=Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                eqIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, musicService!!.mediaPlayer!!.audioSessionId)
                eqIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME,baseContext.packageName)
                eqIntent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE,AudioEffect.CONTENT_TYPE_MUSIC)
                startActivityForResult(eqIntent,13)
            }catch (e:Exception){
                Toast.makeText(this,"Equalizer Feature not supported!!",Toast.LENGTH_SHORT).show()
            }
        }

//        for timer button
        binding.timerBtnPA.setOnClickListener {
            val timer=min15 || min30 || min60
            if (!timer)
            showBottomSheetDialog()
            else
            {
                val builder= MaterialAlertDialogBuilder(this)
                builder.setTitle("Stop Timer")
                    .setMessage("Do you want to stop the timer?")
                    .setPositiveButton("Yes"){_,_->
                        min15=false
                        min30=false
                        min60=false
                        binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this,R.color.cool_pink))
                    }
                    .setNegativeButton("No"){dialog,_->
                        dialog.dismiss()
                    }
                val customDialog=builder.create()
                customDialog.show()
                customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
                customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
            }

        }

//     for share button
        binding.shareBtnPA.setOnClickListener {
            val shareIntent=Intent()
            shareIntent.action=Intent.ACTION_SEND
            shareIntent.type="audio/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(musicListPA[songPosition].path))
            startActivity(Intent.createChooser(shareIntent,"Sharing Music File"))
        }

//        for favourite button
        binding.favouriteBtnPA.setOnClickListener {
            if (isFavourite){
                isFavourite=false
                binding.favouriteBtnPA.setImageResource(R.drawable.favourite_empty)
                FavouriteActivity.favouriteSongs.removeAt(fIndex)
            }
            else{
                isFavourite=true
                binding.favouriteBtnPA.setImageResource(R.drawable.ic_favorite)
                FavouriteActivity.favouriteSongs.add(musicListPA[songPosition])
            }
        }
    }

//    to set the image of current song and current song name in the player
    private fun setLayout(){
    fIndex= favouriteChecker(musicListPA[songPosition].id)//this will tell that the current song which is playing is in favourites or not.


    Glide.with(this)
            .load(musicListPA[songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music_note).centerCrop())
            .into(binding.songImgPA)

    binding.songNamePA.text= musicListPA[songPosition].title

    if (repeat){
        binding.repeatBtnPA.setColorFilter(ContextCompat.getColor(this,R.color.purple_500))
    }

    if (min15 || min30 || min60){
        binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this,R.color.purple_500))
    }
    if (isFavourite) binding.favouriteBtnPA.setImageResource(R.drawable.ic_favorite)
    else binding.favouriteBtnPA.setImageResource(R.drawable.favourite_empty)
    }

    private fun createMediaPlayer(){
     try {
         if (musicService!!.mediaPlayer==null)
             musicService!!.mediaPlayer= MediaPlayer()
         musicService!!.mediaPlayer!!.reset()
         musicService!!.mediaPlayer!!.setDataSource(musicListPA[songPosition].path)
         musicService!!.mediaPlayer!!.prepare()
         musicService!!.mediaPlayer!!.start()
         isPlaying=true
         binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
         musicService!!.showNotification(R.drawable.pause_icon)

//         the below two is for showint the time on the seekbar
         binding.tvSeekBarStart.text= formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
         binding.tvSeekBarEnd.text= formatDuration(musicService!!.mediaPlayer!!.duration.toLong())

//         the below two are for seekbar
         binding.seekBarPA.progress=0// this is for whenever we play a new song the seekbar will start from beginning
         binding.seekBarPA.max= musicService!!.mediaPlayer!!.duration// this is for when the song has reached its end the seekbar will go to the end

         musicService!!.mediaPlayer!!.setOnCompletionListener(this)

         nowPlayingId= musicListPA[songPosition].id
     }catch (e:Exception){return}
    }

//    Important Function
    private fun initializeLayout(){
        songPosition=intent.getIntExtra("index",0)//this will give us that user has clicked which position of songs list
        when(intent.getStringExtra("class")){
            "FavouriteAdapter"->{
                //to start the service
                val intent= Intent(this,MusicService::class.java)
                bindService(intent,this, BIND_AUTO_CREATE)
                startService(intent)

                musicListPA= ArrayList()
                musicListPA.addAll(FavouriteActivity.favouriteSongs)
                setLayout()
            }
            "NowPlaying"->{
                setLayout()
                binding.tvSeekBarStart.text= formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                binding.tvSeekBarEnd.text= formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
                binding.seekBarPA.progress= musicService!!.mediaPlayer!!.currentPosition
                binding.seekBarPA.max=musicService!!.mediaPlayer!!.duration
                if (isPlaying)
                    binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
                else
                    binding.playPauseBtnPA.setIconResource(R.drawable.play_icon)
            }
            "MusicAdapterSearch"->{
                //to start the service
                val intent= Intent(this,MusicService::class.java)
                bindService(intent,this, BIND_AUTO_CREATE)
                startService(intent)

                musicListPA= ArrayList()
                musicListPA.addAll(MainActivity.musicListSearch)
                setLayout()
            }
            "MusicAdapter"->{
                //to start the service
                val intent= Intent(this,MusicService::class.java)
                bindService(intent,this, BIND_AUTO_CREATE)
                startService(intent)

                musicListPA= ArrayList()
                musicListPA.addAll(MainActivity.MusicListMA)
                setLayout()
            }
            "MainActivity"->{
                //to start the service
                val intent= Intent(this,MusicService::class.java)
                bindService(intent,this, BIND_AUTO_CREATE)
                startService(intent)

                musicListPA= ArrayList()
                musicListPA.addAll(MainActivity.MusicListMA)
                musicListPA.shuffle()
                setLayout()
            }
            "FavouriteShuffle"->{
                //to start the service
                val intent= Intent(this,MusicService::class.java)
                bindService(intent,this, BIND_AUTO_CREATE)
                startService(intent)

                musicListPA= ArrayList()
                musicListPA.addAll(FavouriteActivity.favouriteSongs)
                musicListPA.shuffle()
                setLayout()
            }
            "PlaylistDetailsAdapter"->{
                val intent= Intent(this,MusicService::class.java)
                bindService(intent,this, BIND_AUTO_CREATE)
                startService(intent)

                musicListPA= ArrayList()
                musicListPA.addAll(PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist)
                setLayout()
            }
            "PlaylistDetailsShuffle"->{
                val intent= Intent(this,MusicService::class.java)
                bindService(intent,this, BIND_AUTO_CREATE)
                startService(intent)

                musicListPA= ArrayList()
                musicListPA.addAll(PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist)
                musicListPA.shuffle()
                setLayout()
            }
        }
    }

    private fun playMusic(){
        binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
        musicService!!.showNotification(R.drawable.pause_icon)
        isPlaying=true
        musicService!!.mediaPlayer!!.start()
    }

    private fun pauseMusic(){
        binding.playPauseBtnPA.setIconResource(R.drawable.play_icon)
        musicService!!.showNotification(R.drawable.play_icon)
        isPlaying=false
        musicService!!.mediaPlayer!!.pause()
    }

    private fun prevNextSong(increment:Boolean){
        if (increment)
        {
            setSongPosition(increment = true)//to check that we have reached either the last song or we are in the first song of the list
            setLayout()
            createMediaPlayer()
        }
        else
        {
            setSongPosition(increment = false)//to check that we have reached either the last song or we are in the first song of the list
            setLayout()
            createMediaPlayer()
        }
    }




    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicService.MyBinder
        musicService=binder.currentService()
        createMediaPlayer()
        musicService!!.seekBarSetup()
        musicService!!.audioManager= getSystemService(Context.AUDIO_SERVICE) as AudioManager
        musicService!!.audioManager.requestAudioFocus(musicService, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService=null
    }

    override fun onCompletion(mp: MediaPlayer?) {
        setSongPosition(increment = true)
        createMediaPlayer()
        try {
            setLayout()
        }catch (e:Exception){
            return
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode==13 || resultCode== RESULT_OK)
            return
    }

//    for showing bottom sheet dialog
    private fun showBottomSheetDialog(){
        val dialog=BottomSheetDialog(this@PlayerActivity)
        dialog.setContentView(R.layout.bottom_sheet_dialog)
        dialog.show()

//    the bellow functions will dismiss the bottom sheet as soon as one of the options are selected
        dialog.findViewById<LinearLayout>(R.id.min_15)?.setOnClickListener {
            Toast.makeText(baseContext,"Music will stop after 15 minutes",Toast.LENGTH_SHORT).show()
            binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this,R.color.purple_500))
            min15=true

            //start() wil start the thread's timer and after
            // 15 min and it will check that min15 variable is true or not.
            //If it becomes false then it won't exit the app but if it does become true then it will exit the app
            Thread{Thread.sleep((15 * 60000).toLong())
            if (min15) exitProcess(1)}.start()
            dialog.dismiss()
        }

        dialog.findViewById<LinearLayout>(R.id.min_30)?.setOnClickListener {
        Toast.makeText(baseContext,"Music will stop after 30 minutes",Toast.LENGTH_SHORT).show()
            binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this,R.color.purple_500))
            min30=true
            Thread{Thread.sleep((30 * 60000).toLong())
                if (min30) exitProcess(1)}.start()
        dialog.dismiss()
    }

        dialog.findViewById<LinearLayout>(R.id.min_60)?.setOnClickListener {
        Toast.makeText(baseContext,"Music will stop after 60 minutes",Toast.LENGTH_SHORT).show()
            binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this,R.color.purple_500))
            min60=true
            Thread{Thread.sleep((60 * 60000).toLong())
                if (min60) exitProcess(1)}.start()
        dialog.dismiss()
    }
    }
}