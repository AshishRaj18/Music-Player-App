package com.ashish.mymusic

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.ashish.mymusic.databinding.ActivityPlaylistBinding
import com.ashish.mymusic.databinding.AddPlaylistDialogBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*

class PlaylistActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlaylistBinding
    private lateinit var adapter: PlaylistViewAdapter

    companion object{
        var musicPlaylist: MusicPlaylist= MusicPlaylist()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.coolPink)
        binding= ActivityPlaylistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.playlistRV.setHasFixedSize(true)//it will create a limited amt of contents in the recycler view which is required and not extra.

        binding.playlistRV.setItemViewCacheSize(13)//it will give us how many do we want in cache (which is 13 in this case)

        binding.playlistRV.layoutManager = GridLayoutManager(this@PlaylistActivity,2)
        adapter = PlaylistViewAdapter(this,playlistList = musicPlaylist.ref)
        binding.playlistRV.adapter = adapter

        binding.addPlaylistBtn.setOnClickListener {
            customAlertDialog()
        }
    }

    private fun customAlertDialog(){
        val customDialog=LayoutInflater.from(this@PlaylistActivity).inflate(R.layout.add_playlist_dialog,binding.root,false)
        val binder = AddPlaylistDialogBinding.bind(customDialog)
        val builder= MaterialAlertDialogBuilder(this)
        builder.setView(customDialog)
            .setTitle("Enter Playlist Name")
            .setPositiveButton("Add"){dialog,_->
               val playlistName= binder.playlistName.text
                if (playlistName!=null)
                    if (playlistName.isNotEmpty())
                    {
                        addPlaylist(playlistName.toString())
                    }
                 dialog.dismiss()
            }.show()

    }

    private fun addPlaylist(name:String){
        var playlistExist=false
        for(i in musicPlaylist.ref){
            if (name.equals(i.name)){
                playlistExist=true
                break
            }
        }
        if (playlistExist) Toast.makeText(this,"Playlist Exist", Toast.LENGTH_SHORT).show()

        else{
            val tempPlaylist = Playlist()
            tempPlaylist.name=name
            tempPlaylist.playlist=ArrayList()
            val calendar=Calendar.getInstance().time
            val sdf=SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
            tempPlaylist.createdOn=sdf.format(calendar)
            musicPlaylist.ref.add(tempPlaylist)

            adapter.refreshPlaylist()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()

        adapter.notifyDataSetChanged()
    }
}