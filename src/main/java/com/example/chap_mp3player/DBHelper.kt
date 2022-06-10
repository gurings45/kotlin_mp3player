package com.example.chap_mp3player

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.sql.SQLException

class DBHelper(context: Context, dbName: String, version: Int): SQLiteOpenHelper(context, dbName,null, version) {

    companion object{
        fun updateFavorite(music: Music) {

        }

        val TABLE_NAME = "musicTBL"
    }

    //테이블 설계
    override fun onCreate(db: SQLiteDatabase?) {
        val createQuery = "create table ${TABLE_NAME}(id TEXT primary key, title TEXT, " +
                "artist TEXT, albumId TEXT,duration INTEGER, favorite TEXT)"
        db?.execSQL(createQuery)
    }

    //테이블제거
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val dropQuery = "drop table $TABLE_NAME"
        db?.execSQL(dropQuery)
        //onCreate()
        this.onCreate(db)
    }

    //삽입 : insert into 테이블명 (~~~~) values(~,~,~)
    fun insertMusic(music: Music): Boolean{
        var insertFlag = false
        val insertQuery = "insert into $TABLE_NAME(id, title, artist, albumId,duration, favorite) " +
                "values( '${music.id}','${music.title}','${music.artist}','${music.albumId}',${music.duration}, ${music.favorite})"

        //db : SQLiteDatabase 가져오는 방법은 : writableDatabase, readableDatabase
        val db = this.writableDatabase
        try {
            db.execSQL(insertQuery)
            insertFlag = true
        }catch (e: SQLException){
            Log.d("song", e.toString())
        }finally {
            db.close()
        }

        return insertFlag
    }

    //선택 : 모든것을 선택
    fun selectMusicAll(): MutableList<Music>?{
        var musicList: MutableList<Music>? = mutableListOf()
        val selectQuery = "select * from $TABLE_NAME"
        val db = this.readableDatabase
        var cursor: Cursor? = null

        try{
            cursor = db.rawQuery(selectQuery, null)
            if(cursor.count > 0){
                while (cursor.moveToNext()){
                    val id = cursor.getString(0)
                    val title = cursor.getString(1)
                    val artist = cursor.getString(2)
                    val albumId = cursor.getString(3)
                    val duration = cursor.getLong(4)
                    val favorite = cursor.getString(5).toBoolean()

                    musicList?.add(Music(id, title, artist, albumId, duration, favorite))
                }
            }else musicList = null
        }catch (e: Exception){
            Log.d("song", "selectAll Error ${e.printStackTrace()}")
            musicList = null
        }finally {
            cursor?.close()
            db.close()
        }
        return musicList
    }

    //선택 : 조건에 맞는 선택
    fun selectMusic(id: String): Music?{
        var music: Music? = null

        val selectQuery = "select * from $TABLE_NAME where id = '${id}' "
        val db = this.readableDatabase
        val cursor: Cursor? = null

        try {
            val cursor = db.rawQuery(selectQuery, null)
            if (cursor.count > 0) {
                if (cursor.moveToFirst()) {
                    val id = cursor.getString(0)
                    val title = cursor.getString(1)
                    val artist = cursor.getString(2)
                    val albumId = cursor.getString(3)
                    val duration = cursor.getLong(4)
                    val favorite = cursor.getString(5).toBoolean()
                    music = Music(id, title, artist, albumId, duration, favorite)
                }
            }
        }
        catch (e: Exception){
            Log.d("song", e.toString())
            music = null
        }finally {
            cursor?.close()
            db.close()
        }

        return music
    }

    //좋아요 업데이트
    fun updateFavorite (id: String, favorite: Boolean): Boolean {
        var updateFlag = false
        val updateQuery = "update $TABLE_NAME set favorite = '$favorite' where id = '$id'"
        val db = this.writableDatabase

        try {
            db.execSQL(updateQuery)
            updateFlag = true
        }catch (e: Exception){
            Log.d("song", "updateFavorite error ${e.printStackTrace()}")
        }finally {
            db.close()
        }
        return updateFlag
    }

}