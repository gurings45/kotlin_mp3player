package com.example.chap_mp3player

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chap_mp3player.databinding.ItemRecyclerBinding
import java.text.SimpleDateFormat

//1. 매개변수 (컨텍스트, 컬렉션프레임워크) 3. 상속처리 RecyclerView.Adapter<MusicRecyclerAdapter.CustomViewHolder>()
class MusicRecyclerAdapter(val context: Context, val musicList: MutableList<Music>?): RecyclerView.Adapter<MusicRecyclerAdapter.CustomViewHolder>() {
    //이미지 사이즈 정의함.
    var ALBUM_IMAGE_SIZE = 80
    //4.오버라이딩하면된다. 자동으로 처리
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = ItemRecyclerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val binding = holder.binding
        val music = musicList?.get(position)
        val dbHelper = DBHelper(context, "musicDB", 1)

        binding.tvArtist.text = music?.artist //가수명
        binding.tvTitle.text = music?.title //제목

        binding.textView3.text = SimpleDateFormat("mm:ss").format(music?.duration) //경과시간
        val bitmap: Bitmap? = music?.getAlbumImage(context, ALBUM_IMAGE_SIZE)
        if(bitmap != null){
            binding.imageView.setImageBitmap(bitmap)
        }else{
            //앨범이미지가 없을경우 디폴트 이미지를 붙혀넣는다.
            binding.imageView.setImageResource(R.drawable.mp3_image)

        }

        //해당항목을 클릭하면 PlaymusicActivity 화면으로 넘어간다. 해당되는 music 객체를 가지고 넘어간다.
        binding.root.setOnClickListener{
            //액티비티 음악정보를 넘겨서 음악을 재생해주는 액티비티 설계.
            val playList: ArrayList<Parcelable>? = musicList as ArrayList<Parcelable>

            val intent = Intent(binding.root.context, PlaymusicActivity::class.java)
            //intent.putExtra("music", music)
            intent.putExtra("playList", playList)
            intent.putExtra("position", position)

            binding.root.context.startActivity(intent)
        }

        binding.ivfavorite.setOnClickListener {
            when(music?.favorite){
                true -> {
                    music?.favorite = false
                    dbHelper.updateFavorite(music.id, music.favorite)
                    holder.binding.ivfavorite.setImageResource(R.drawable.favorite_border_24)
                }
                false -> {
                    music?.favorite = true
                    dbHelper.updateFavorite(music.id, music.favorite)
                    holder.binding.ivfavorite.setImageResource(R.drawable.favorite_24)
                }
            }
        }

        when(music?.favorite){
            true -> {
                holder.binding.ivfavorite.setImageResource(R.drawable.favorite_24)

            }

            false -> {
                holder.binding.ivfavorite.setImageResource(R.drawable.favorite_border_24)

            }
        }
    }

    override fun getItemCount(): Int {
        return musicList?.size ?: 0
    }
    //2. 뷰홀더 내부선언(바인딩)
    class CustomViewHolder(val binding: ItemRecyclerBinding): RecyclerView.ViewHolder(binding.root)

}

