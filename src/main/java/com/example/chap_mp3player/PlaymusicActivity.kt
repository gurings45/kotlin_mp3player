package com.example.chap_mp3player

import android.graphics.Bitmap
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.example.chap_mp3player.databinding.ActivityPlaymusicBinding
import kotlinx.coroutines.*
import java.text.SimpleDateFormat

class PlaymusicActivity() : AppCompatActivity() {

lateinit var binding: ActivityPlaymusicBinding

//1. 뮤직플레이어 변수

    private var mediaPlayer: MediaPlayer? = null

//2. 음악정보객체 변수

    private var music: Music? = null
    private var playList: ArrayList<Parcelable>? = null
    private var position: Int = 0

//3. 음악앨범이미지 사이즈

    private val ALBUM_IMAGE_SIZE = 150

//4. 코루틴 스코프 launch


    private var playerJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = com.example.chap_mp3player.databinding.ActivityPlaymusicBinding.inflate(layoutInflater)

        setContentView(binding.root)

        //music = intent.getSerializableExtra("music") as Music
        playList = intent.getParcelableArrayListExtra("playList")
        position = intent.getIntExtra("position", 0)
        music = playList?.get(position) as Music

        if(music != null){

            //뷰셋팅

            binding.tvTitle.text = music?.title

            binding.tvArtist.text = music?.artist

            binding.tvDurationStart.text = "00:00"

            binding.tvDurationStop.text = SimpleDateFormat("mm:ss").format(music?.duration)

            //binding.ivfavorite.setImageResource(R.drawable.favorite_border_24) //좋아요

            //앨범이미지
            val bitmap: Bitmap? = music?.getAlbumImage(this, ALBUM_IMAGE_SIZE)

            if(bitmap != null){

                binding.ivSing.setImageBitmap(bitmap)

            }else{

                binding.ivSing.setImageResource(R.drawable.music_video_24)

            }



//음원실행 생성및 재생

            mediaPlayer = MediaPlayer.create(this, music?.getMusicUri() )



            binding.seekBar.max = music?.duration!!.toInt()



//시크바 이벤트설정을 해서 노래와 같이 동기화 처리 된다.

            binding.seekBar.setOnSeekBarChangeListener( object: SeekBar.OnSeekBarChangeListener{

//시크바를 터치하고 이동할때 발생되는 이벤트 fromUser : 유저에 의한 터치유무

                override fun onProgressChanged(seekbar: SeekBar?, progress: Int, fromUser: Boolean) {

                    if(fromUser){

                        mediaPlayer?.seekTo(progress)

                    }

                }

//시크바를 터치하는 순간 이벤트 발생

                override fun onStartTrackingTouch(p0: SeekBar?) {



                }

//시크바를 터치를 놓는순간 이벤트 발생

                override fun onStopTrackingTouch(p0: SeekBar?) {
                }
            })
        }
    }

    fun onClickView(view: View?){

        when(view?.id){

            R.id.ivMain ->{     //음악정지, 코루틴취소, 음악객체 해제, 음악객체 = null

                mediaPlayer?.stop()

                playerJob?.cancel()

                mediaPlayer?.release()

                mediaPlayer = null

                finish()

            }

            R.id.ivStart ->{

                if(mediaPlayer?.isPlaying == true){

                    mediaPlayer?.pause()

//                    binding.seekBar.progress = mediaPlayer?.currentPosition!!

                    binding.ivStart.setImageResource(R.drawable.play_circle_filled_24)

                }else{

                    mediaPlayer?.start()

                    binding.ivStart.setImageResource(R.drawable.pause_circle_outline_24)

//여기서 음악이 나오는데. 시크바하고, 시작시간진행을 코루틴으로 진행

                    val backgroundScope = CoroutineScope(Dispatchers.Default + Job())



                    playerJob = backgroundScope.launch {

//음악노래진행사항을 가져와서 시크바와 , 시작진행사항값을 변화시켜줘야 한다.

                        //중요: 사용자가 만든 스레드에서 화면에 뷰값을 변경을 하게 되면 문제가 발생한다.

                        //해결방법: 스레드안에서 뷰에 있는값을 변경하고 싶으면 runOnUiThread{ ~~~~}

                        while (mediaPlayer?.isPlaying == true) {

                            runOnUiThread {

                                var currentPosition = mediaPlayer?.currentPosition!!

                                binding.seekBar.progress = currentPosition

                                binding.tvDurationStart.text =

                                    SimpleDateFormat("mm:ss").format(currentPosition)

                            }

//노래진행하면서 진행위치값을 시크바 위치에 적용한다.

                            try {

                                delay(500)

                            } catch (e: Exception) {

                                Log.d("song", "delay(500) = ${e.toString()}")

                            }

                        }//end of while

                        Log.d("song", " currentPosition ${mediaPlayer!!.currentPosition}")

                        Log.d("song", " .max ${binding.seekBar.max}")

                        runOnUiThread {

                            if (mediaPlayer!!.currentPosition >= (binding.seekBar.max - 1000)) {

                                binding.seekBar.progress = 0

                                binding.tvDurationStart.text = "00:00"

                            }

                            binding.ivStart.setImageResource(R.drawable.play_circle_filled_24)

            }

//                        binding.seekBar.progress = 0
        }

    }// end of backgroundScope.launch

}//end of if(mediaPlayer?.isPlaying == true)else

                R.id.ivStop ->{

            mediaPlayer?.stop()

            playerJob?.cancel()

            mediaPlayer = MediaPlayer.create(this, music?.getMusicUri())

            binding.seekBar.progress = 0

            binding.tvDurationStart.text = "00:00"

            binding.ivStart.setImageResource(R.drawable.play_circle_filled_24)

            }
        }
    }
}