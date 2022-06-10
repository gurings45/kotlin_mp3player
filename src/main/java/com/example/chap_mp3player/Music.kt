package com.example.chap_mp3player

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Parcel
import android.os.ParcelFileDescriptor
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import java.io.IOException

@Parcelize
class Music(var id: String, var title: String?, var artist: String?, var albumId: String?, var duration: Long,var favorite: Boolean ): Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readLong(),
        parcel.readByte() != 0.toByte()) {
    }

        companion object : Parceler<Music> {

        override fun Music.write(parcel: Parcel, flags: Int) {
            parcel.writeString(id)
            parcel.writeString(title)
            parcel.writeString(artist)
            parcel.writeString(albumId)
            parcel.writeLong(duration)
            parcel.writeByte(if (favorite) 1 else 0)
        }

        override fun create(parcel: Parcel): Music {
            return Music(parcel)
        }
    }


/*    //멤버변수
    var id: String = ""
    var title: String? = null
    var artist: String? = null
    var albumId: String? = null
    var duration : Long? = 0

    //생성자 멤버변수 초기화
    init {
        this.id = id
        this.title = title
        this.artist = artist
        this.albumId = albumId
        this.duration = duration
        this.favorite = favorite

    }*/
    //음악정보를 가져오기 위한 경로 Uri 얻기 (음악정보)
    fun getMusicUri(): Uri{
        return Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
    }

    //컨텐트리졸버를 이용해서 앨범정보를 가져오기 위한 경로 Uri 얻기
    fun getAlbumUri(): Uri{
        return Uri.parse("content://media/external/audio/albumart/"+albumId)
    }

    //해당되는 음악에 이미지를 내가 원하는 사이즈로 비트맵 만들기
    fun getAlbumImage(context: Context, albumImageSize: Int): Bitmap?{
        val contentResolver: ContentResolver = context.getContentResolver()
        //앨범 경로
        val uri = getAlbumUri()
        //앨범에 대한 정보를 저장하기 위함
        val options = BitmapFactory.Options()

        if (uri != null){
            var parcelFileDescriptor: ParcelFileDescriptor? = null

            try {
                //외부파일에 있는 이미지파일을 가져오기 위한 스트림
                parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")

                var bitmap = BitmapFactory.decodeFileDescriptor(parcelFileDescriptor!!.fileDescriptor, null, options)

                //비트맵을 가져와서 사이즈 결정 (원본이미지 사이즈가 내가 원하는 사이즈하고 맞지 않을경우
                if(bitmap != null){
                    if(options.outHeight !== albumImageSize || options.outWidth !== albumImageSize ){
                        val tempBitmap = Bitmap.createScaledBitmap(bitmap,albumImageSize,albumImageSize,true)
                        bitmap.recycle()

                        bitmap = tempBitmap

                    }
                }

                return bitmap

            }catch( e: Exception){
                Log.d("song", " getAlbumImage() ${e.toString()}")
            }finally {
                try {
                    parcelFileDescriptor?.close()
                }catch (e: IOException){
                    Log.d("song", " getAlbumImage() ${e.toString()}")
                }
            }
        }
        return null
    }


}//end of parce