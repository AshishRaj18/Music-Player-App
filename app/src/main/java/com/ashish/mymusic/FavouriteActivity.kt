package com.ashish.mymusic

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.ashish.mymusic.databinding.ActivityFavouriteBinding

class FavouriteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavouriteBinding
    private lateinit var adapter: FavouriteAdapter

//    favourite list
    companion object{
        var favouriteSongs: ArrayList<Music> = ArrayList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.coolPink)
        binding= ActivityFavouriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        favouriteSongs= checkPlaylist(favouriteSongs)

        binding.favouriteRV.setHasFixedSize(true)//it will create a limited amt of contents in the recycler view which is required and not extra.

        binding.favouriteRV.setItemViewCacheSize(13)//it will give us how many do we want in cache (which is 13 in this case)

        binding.favouriteRV.layoutManager = GridLayoutManager(this,3)
        adapter = FavouriteAdapter(this, favouriteSongs)
        binding.favouriteRV.adapter = adapter

//        condition for if there is no song in the favourite list than it will hide the shuffle button
        if (favouriteSongs.size<1) binding.shuffleBtnFA.visibility=View.INVISIBLE


        binding.shuffleBtnFA.setOnClickListener {
            val intent= Intent(this,PlayerActivity::class.java)
            intent.putExtra("index",0)
            intent.putExtra("class","FavouriteShuffle")
            startActivity(intent)
        }

    }
}