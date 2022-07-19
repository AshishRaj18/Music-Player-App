package com.ashish.mymusic

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ashish.mymusic.databinding.MusicViewBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions


//This adapter class will help in connecting the recycler view which we can see in 'music_view.xml'.

//Also here context means the reference of the activity.
class MusicAdapter(private val context:Context, private var musicList:ArrayList<Music>, private val playlistDetails: Boolean=false,
private val selectionActivity: Boolean = false): RecyclerView.Adapter<MusicAdapter.MyHolder>() {
    class MyHolder(binding:MusicViewBinding) : RecyclerView.ViewHolder(binding.root) {
        val title=binding.songNameMV
        val album=binding.songAlbumMV
        val image=binding.imageMV
        val duration=binding.songDuration

        val root =binding.root
    }


//    Whenever our class 'MyHolder' object will be made then this function will be called
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicAdapter.MyHolder {
        return MyHolder(MusicViewBinding.inflate(LayoutInflater.from(context),parent,false))
    }


//    This is called when our music views are ready to be displayed
    override fun onBindViewHolder(holder: MusicAdapter.MyHolder, position: Int) {
        holder.title.text=musicList[position].title
        holder.album.text=musicList[position].album
        holder.duration.text= formatDuration(musicList[position].duration)

        Glide.with(context)
            .load(musicList[position].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music_note).centerCrop())
            .into(holder.image)

    when{

        playlistDetails->{
            holder.root.setOnClickListener {
                sendIntent(reference = "PlaylistDetailsAdapter",pos = position)
            }
        }
        selectionActivity->{
            holder.root.setOnClickListener {
                if (addSong(musicList[position]))
                    holder.root.setBackgroundColor(ContextCompat.getColor(context, R.color.cool_pink))
                else
                    holder.root.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
            }
        }
        else->{  holder.root.setOnClickListener {
            when{
                MainActivity.search->sendIntent(reference = "MusicAdapterSearch",pos = position)
                musicList[position].id==PlayerActivity.nowPlayingId->
                    sendIntent(reference = "NowPlaying",pos = PlayerActivity.songPosition)
                else->sendIntent(reference = "MusicAdapter",pos = position)
            }
        }
        }
    }


    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateMusicList(searchList:ArrayList<Music>){
        musicList= ArrayList()
        musicList.addAll(searchList)
        notifyDataSetChanged()
    }

    private fun sendIntent(reference:String,pos:Int){
        val intent= Intent(context,PlayerActivity::class.java)

//        this two will send the intent to 'PlayerActivity' when a particular song is clicked
        intent.putExtra("index",pos)
        intent.putExtra("class",reference)


        ContextCompat.startActivity(context,intent,null)
    }

    private fun addSong(song: Music): Boolean{
        PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist.forEachIndexed { index, music ->
            if (song.id==music.id){
                PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist.removeAt(index)
                return false
            }
        }
        PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist.add(song)
        return true
    }

    fun refreshPlaylist(){
        musicList = ArrayList()
        musicList= PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist
        notifyDataSetChanged()
    }
}