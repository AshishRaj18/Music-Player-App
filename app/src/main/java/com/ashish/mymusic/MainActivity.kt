package com.ashish.mymusic

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.ashish.mymusic.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle:ActionBarDrawerToggle

    private lateinit var musicAdapter: MusicAdapter

//we can make static object in companion object
    companion object{
        lateinit var MusicListMA : ArrayList<Music>

        lateinit var musicListSearch:ArrayList<Music>
        var search:Boolean=false
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.coolPinkNav)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        for nav drawer
        toggle= ActionBarDrawerToggle(this,binding.root,R.string.open,R.string.close)
        binding.root.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if(requestRuntimePermission()) {
            initializeLayout()


//          for retrieving favourites data using shared preferences
            FavouriteActivity.favouriteSongs= ArrayList()
            val editor=getSharedPreferences("FAVOURITES", MODE_PRIVATE)
            val jsonString=editor.getString("FavouriteSongs",null)
            val typeToken=object :TypeToken<ArrayList<Music>>(){}.type
            if (jsonString!=null){
                val data:ArrayList<Music> =GsonBuilder().create().fromJson(jsonString,typeToken)
                FavouriteActivity.favouriteSongs.addAll(data)
            }

            PlaylistActivity.musicPlaylist= MusicPlaylist()
            val jsonStringPlaylist=editor.getString("MusicPlaylist",null)
            if (jsonStringPlaylist!=null){
                val dataPlaylist:MusicPlaylist =GsonBuilder().create().fromJson(jsonStringPlaylist,MusicPlaylist::class.java)
                PlaylistActivity.musicPlaylist= dataPlaylist
            }
        }



        binding.shuffleBtn.setOnClickListener {
            val intent=Intent(this@MainActivity,PlayerActivity::class.java)
            intent.putExtra("index",0)
            intent.putExtra("class","MainActivity")
            startActivity(intent)
        }
        binding.favouriteBtn.setOnClickListener {
            startActivity(Intent(this@MainActivity,FavouriteActivity::class.java))
        }
        binding.playlistBtn.setOnClickListener {
            startActivity(Intent(this@MainActivity,PlaylistActivity::class.java))
        }


//        Navigation drawer with it's menu
        binding.navView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.navFeedback -> Toast.makeText(this,"Feedback",Toast.LENGTH_SHORT).show()
                R.id.navSettings -> Toast.makeText(this,"Settings",Toast.LENGTH_SHORT).show()
                R.id.navAbout -> Toast.makeText(this,"About",Toast.LENGTH_SHORT).show()
                R.id.navExit -> {
                    val builder=MaterialAlertDialogBuilder(this)
                    builder.setTitle("Exit")
                        .setMessage("Do you want to close the app?")
                        .setPositiveButton("Yes"){_,_->
                            exitApplication() // this will exit the application
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
            true
        }
    }

//    For requesting permission
    private fun requestRuntimePermission():Boolean{
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 13)
            return false
    }
    return true
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode==13)
        {
            if (grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this@MainActivity,"Permission Granted",Toast.LENGTH_SHORT).show()
                initializeLayout()
            }
            else
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),13)

        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item))
        {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("SetTextI18n")
    private fun initializeLayout(){
//        We will set our music adapter to recycler view here

//        val musicList=ArrayList<String>()
//         musicList.add("First Song")
//         musicList.add("Second Song")
//         musicList.add("Third Song")
//         musicList.add("Fourth Song")
//         musicList.add("Fifth Song")

        search=false

        MusicListMA = getAllAudio()
        binding.musicRv.setHasFixedSize(true)//it will create a limited amt of contents in the recycler view which is required and not extra.

        binding.musicRv.setItemViewCacheSize(13)//it will give us how many do we want in cache (which is 13 in this case)

        binding.musicRv.layoutManager = LinearLayoutManager(this@MainActivity)
        musicAdapter = MusicAdapter(this@MainActivity, MusicListMA)
        binding.musicRv.adapter = musicAdapter

        binding.totalSongs.text  = "Total Songs : "+musicAdapter.itemCount//to show the total number of songs present
    }


    //    this func will access all the music or audio files from our phone
    @SuppressLint("Recycle", "Range")
    private fun getAllAudio(): ArrayList<Music>{
        val tempList=ArrayList<Music>()

//    Now to access music from our storage cursor helps us(Basically cursor helps us to access any type of files from the phone storage)

        val selection=MediaStore.Audio.Media.IS_MUSIC + "!=0" // it will tell the cursor what type of files cursor has to look for

//    this projection will tell the cursor what what data do i want from music files
        val projection= arrayOf(MediaStore.Audio.Media._ID,MediaStore.Audio.Media.TITLE,MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,MediaStore.Audio.Media.DURATION,MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATA,MediaStore.Audio.Media.ALBUM_ID)

        val cursor = this.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection,selection,null,
            MediaStore.Audio.Media.DATE_ADDED +  " DESC", null)

        if (cursor!=null){
            if (cursor.moveToFirst())
                do {
//                    these are the objects of cursor to get different things
                    val titleC=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                    val idC=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
                    val albumC=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                    val artistC=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                    val pathC=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                    val durationC= cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                    val albumIdC=cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)).toString()


                    val uri=Uri.parse("content://media/external/audio/albumart")
                    val artUriC=Uri.withAppendedPath(uri,albumIdC).toString()


                    val music=Music(id=idC,title=titleC,album = albumC,artist = artistC,duration = durationC,path = pathC,artUri=artUriC)

//                there can be a case where the path we get might not be the actual file we want
                    val file= File(music.path)
                    if (file.exists())
                        tempList.add(music)



                }while (cursor.moveToNext())
            cursor.close()
        }
        return tempList
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!PlayerActivity.isPlaying && PlayerActivity.musicService != null)
        {
           exitApplication()
        }
    }

    override fun onResume() {
        super.onResume()

        //        for storing favourites data using SharedPreferences(SharedPreferences: It helps in storing and retrieving small
        //        data in our device. But there is a problem SharedPreferences and that is it stores only primitive data type
        //        so we can't store music list or ArrayList<Music> type dta so we will be using SharedPreferences with JSON
        //        in which JSON will help us to convert the music list or ArrayList<Music> in some format so that it can be used
        //        by SharedPreferences.)
        val editor=getSharedPreferences("FAVOURITES", MODE_PRIVATE).edit()
        val jsonString=GsonBuilder().create().toJson(FavouriteActivity.favouriteSongs)
        editor.putString("FavouriteSongs",jsonString)
        val jsonStringPlaylist=GsonBuilder().create().toJson(PlaylistActivity.musicPlaylist)
        editor.putString("MusicPlaylist",jsonStringPlaylist)
        editor.apply()
    }

//    For search bar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_view_menu,menu)
        val searchView=menu?.findItem(R.id.searchView)?.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean =true

            override fun onQueryTextChange(newText: String?): Boolean {
                musicListSearch=ArrayList()
                if (newText!=null)
                {
                    val userInput=newText.lowercase()
                    for (song in MusicListMA)
                        if (song.title.lowercase().contains(userInput))
                            musicListSearch.add(song)
                    search=true
                    musicAdapter.updateMusicList(searchList = musicListSearch)
                }
                return true
            }
        })
        return super.onCreateOptionsMenu(menu)

    }

    }

