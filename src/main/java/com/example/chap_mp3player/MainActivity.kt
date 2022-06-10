package com.example.chap_mp3player

import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chap_mp3player.databinding.ActivityMainBinding



class MainActivity() : AppCompatActivity() {

    companion object {

        val DB_NAME = "musicDB"

        val VERSION = 1

    }


    lateinit var binding: ActivityMainBinding

//승인받아야할 퍼미션 정의

    val permission = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)

    val REQUEST_READ = 200


//2. 데이타베이스 객체화

    val dbHelper: DBHelper by lazy { DBHelper(this, DB_NAME, VERSION) }


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(this.layoutInflater)

        setContentView(binding.root)


//승인이 되었으면 노래파일을 가져오고, 승인이 되지않으면 토스트메세지와 재요청을 준다.

        if (isPermitted() == true) {

            startProcess()

        } else {

//승인을 다시 요청

//요청이 승인이 되면 콜백함수로 승인결과 값을 알려준다.

            ActivityCompat.requestPermissions(this, permission, REQUEST_READ)

        }

    }


//다이얼로그창에서 퍼미션요청을 했을때 사용자가 요청허락을 했는지 거절했는지 알려줌

    override fun onRequestPermissionsResult(

        requestCode: Int,

        permissions: Array<out String>,

        grantResults: IntArray

    ) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_READ) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

//실행하면됨. 외부파일을 가져와서, 컬렉션프레임워크 저장하고, 어뎁터를 부른다.

                startProcess()

            } else {

                Toast.makeText(this, "승인을 하지않으면 앱을 사용하실수 없습니다.", Toast.LENGTH_SHORT).show()

                finish()

            }

        }

    }

//외부파일 읽기 승인요청

    fun isPermitted(): Boolean{

        if(ContextCompat.checkSelfPermission(this, permission[0]) != PackageManager.PERMISSION_GRANTED) {

            return false

        }else{

            return true

        }

    }
//외부파일로부터 노래정보를 다 가져오는 함수

    private fun startProcess() {

        var musicList: MutableList<Music>? = mutableListOf<Music>()



//music 테이블에서 자료를 가져온다.

        //music 테이블에서 자료가 있으면 -> 리사클러뷰 보여주고

//music 테이블에 없으면 -> getMusicList 가져오고 -> music 테이블에 모두 저장하고 -> 리사클러뷰 보여줌.



        musicList = dbHelper.selectMusicAll()



        if(musicList == null || musicList.size <= 0){

//getMusicList 가져오고

            musicList = getMusicList()

//music 테이블에 모두 저장

            for (i in 0..(musicList!!.size - 1)){

                val music = musicList.get(i)

                if(dbHelper.insertMusic(music) == false){

                    Log.d("song", "삽입 오류 ${music.toString()}가 발생했습니다!")

                }

            }

            Log.d("song", "테이블에 없어서 getMusicList()")

        }else{

            Log.d("song", "테이블에 있어서 내용을 가져와서 보여줌")



        }



//1.음악정보를 가져와야된다.

        // val musicList: MutableList<Music>? = getMusicList()



        //2.데이터베이스 저장한다.(중복반드시 점걸할것 : id primary key)



        //3.어뎁터를 만들고 뮤터블리스트에 제공

        binding.recyclerview.adapter = MusicRecyclerAdapter(this, musicList)

//화면에 출력

        binding.recyclerview.layoutManager = LinearLayoutManager(this)

    }



    private fun getMusicList(): MutableList<Music>? {

//노래정보를 저장 mp3 외부파일에 음악정보주소위치

        val listUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

//여기서 노랫말이라던지 추가로 추가할것이 있으면..요청해야될 음악정보 컬럼들

        val proj = arrayOf(

            MediaStore.Audio.Media._ID, //노래 아이디

            MediaStore.Audio.Media.TITLE,  //노래 타이틀

            MediaStore.Audio.Media.ARTIST, //가수이름

            MediaStore.Audio.Media.ALBUM_ID,  //노래이미지

            MediaStore.Audio.Media.DURATION //재생시간



        )

//콘텐트 리졸버 쿼리에 Uri, 요청 노래정보컬럼을 요구하고, 결과값을 cursor로 반환받는다.

        val cursor = contentResolver.query(listUri, proj, null, null, null)

//Music : mp3정보를 5가지 기억, mp3 파일경로 가져옴, mp3 이미지경로, 이미지경로를 내가 원하는 사이즈로 비트맵

        val musicList: MutableList<Music>? = mutableListOf<Music>()

        while(cursor?.moveToNext() == true){

            val id = cursor.getString(0)

            val title = cursor.getString(1).replace("'","")

            val artist = cursor.getString(2)

            val albumId = cursor.getString(3)

            val duration = cursor.getLong(4)

            //val favorite = cursor.getString(5).toBoolean()



//Music : mp3정보를 5가지 기억, mp3 파일경로 가져옴, mp3 이미지경로, 이미지경로를 내가 원하는 사이즈로 비트맵

            val music = Music(id, title,artist, albumId, duration, false)

            musicList?.add(music)

        }

        cursor?.close()



        return musicList

    }

}