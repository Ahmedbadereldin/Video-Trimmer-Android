package com.ahmedbadereldin.videotrimmerapplication.kotlinCode

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.media.MediaPlayer.OnPreparedListener
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import com.ahmedbadereldin.videotrimmer.Utility
import com.ahmedbadereldin.videotrimmer.customVideoViews.*
import com.ahmedbadereldin.videotrimmerapplication.R
import kotlinx.android.synthetic.main.activity_video_trim.*
import java.io.File
import java.util.*

class VideoTrimmerKotlinActivity : AppCompatActivity(),
    View.OnClickListener {

    private var mDuration = 0
    private var mTimeVideo = 0
    private var mStartPosition = 0
    private var mEndPosition = 0

    // set your max video trim seconds
    private val mMaxDuration = 60
    private val mHandler = Handler()
    private var mProgressDialog: ProgressDialog? = null
    var srcFile: String? = null
    var dstFile: String? = null

    var mOnVideoTrimListener: OnVideoTrimListener? = object : OnVideoTrimListener {
        override fun onTrimStarted() {
            // Create an indeterminate progress dialog
            mProgressDialog = ProgressDialog(this@VideoTrimmerKotlinActivity)
            mProgressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            mProgressDialog!!.setTitle(getString(R.string.save))
            mProgressDialog!!.isIndeterminate = true
            mProgressDialog!!.setCancelable(false)
            mProgressDialog!!.show()
        }

        override fun getResult(uri: Uri) {
            Log.d("getResult", "getResult: $uri")
            mProgressDialog!!.dismiss()
            val conData = Bundle()
            conData.putString("INTENT_VIDEO_FILE", uri.path)
            val intent = Intent()
            intent.putExtras(conData)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        override fun cancelAction() {
            mProgressDialog!!.dismiss()
        }

        override fun onError(message: String) {
            mProgressDialog!!.dismiss()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_trim)

        if (intent.extras != null) {
            srcFile = intent.extras!!.getString("EXTRA_PATH")
        }
        dstFile = (Environment.getExternalStorageDirectory()
            .toString() + "/" + getString(R.string.app_name) + Date().time
                + Utility.VIDEO_FORMAT)
        timeLineView.post(Runnable {
            setBitmap(Uri.parse(srcFile))
            videoView.setVideoURI(Uri.parse(srcFile))
        })
        txtVideoCancel.setOnClickListener(this)
        txtVideoUpload.setOnClickListener(this)
        videoView.setOnPreparedListener { mp: MediaPlayer -> onVideoPrepared(mp) }
        videoView.setOnCompletionListener { onVideoCompleted() }

        // handle your range seekbar changes
        timeLineBar.addOnRangeSeekBarListener(object : OnRangeSeekBarChangeListener {
            override fun onCreate(
                customRangeSeekBarNew: CustomRangeSeekBar,
                index: Int,
                value: Float
            ) {
                // Do nothing
            }

            override fun onSeek(
                customRangeSeekBarNew: CustomRangeSeekBar,
                index: Int,
                value: Float
            ) {
                onSeekThumbs(index, value)
            }

            override fun onSeekStart(
                customRangeSeekBarNew: CustomRangeSeekBar,
                index: Int,
                value: Float
            ) {
                if (videoView != null) {
                    mHandler.removeCallbacks(mUpdateTimeTask)
                    seekBarVideo.setProgress(0)
                    videoView!!.seekTo(mStartPosition * 1000)
                    videoView!!.pause()
                    imgPlay.setBackgroundResource(R.drawable.ic_white_play)
                }
            }

            override fun onSeekStop(
                customRangeSeekBarNew: CustomRangeSeekBar,
                index: Int,
                value: Float
            ) {
                onStopSeekThumbs()
            }
        })
        imgPlay.setOnClickListener(this)

        // handle changes on seekbar for video play
        seekBarVideo.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar,
                i: Int,
                b: Boolean
            ) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                if (videoView != null) {
                    mHandler.removeCallbacks(mUpdateTimeTask)
                    seekBarVideo.setMax(mTimeVideo * 1000)
                    seekBarVideo.setProgress(0)
                    videoView!!.seekTo(mStartPosition * 1000)
                    videoView!!.pause()
                    imgPlay.setBackgroundResource(R.drawable.ic_white_play)
                }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                mHandler.removeCallbacks(mUpdateTimeTask)
                videoView.seekTo(mStartPosition * 1000 - seekBarVideo.getProgress())
            }
        })
    }

    override fun onClick(view: View) {
        if (view === txtVideoCancel) {
            finish()
        } else if (view === txtVideoUpload) {
            val diff = mEndPosition - mStartPosition
            if (diff < 3) {
                Toast.makeText(
                    this@VideoTrimmerKotlinActivity, getString(R.string.video_length_validation),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                val mediaMetadataRetriever = MediaMetadataRetriever()
                mediaMetadataRetriever.setDataSource(
                    this@VideoTrimmerKotlinActivity,
                    Uri.parse(srcFile)
                )
                val file = File(srcFile)
                Log.d(
                    "fileLength",
                    "onClick: " + file.length() + " " + file.canExecute()
                )
                //notify that video trimming started
                if (mOnVideoTrimListener != null) mOnVideoTrimListener!!.onTrimStarted()
                BackgroundTask.execute(object : BackgroundTask.Task("", 0L, "") {
                    override fun execute() {
                        try {
                            Log.d(
                                "executeAAAA",
                                "execute: " + "Aaaa" + file.length() + " " + dstFile + " " + mStartPosition + " " + mEndPosition + " " + mOnVideoTrimListener
                            )
                            Utility.startTrim(
                                file,
                                dstFile!!,
                                mStartPosition * 1000.toLong(),
                                mEndPosition * 1000.toLong(),
                                mOnVideoTrimListener!!
                            )
                        } catch (e: Throwable) {
                            Thread.getDefaultUncaughtExceptionHandler()
                                .uncaughtException(Thread.currentThread(), e)
                        }
                    }
                }
                )
            }
        } else if (view === imgPlay) {
            if (videoView!!.isPlaying) {
                if (videoView != null) {
                    videoView!!.pause()
                    imgPlay!!.setBackgroundResource(R.drawable.ic_white_play)
                }
            } else {
                if (videoView != null) {
                    videoView!!.start()
                    imgPlay!!.setBackgroundResource(R.drawable.ic_white_pause)
                    if (seekBarVideo!!.progress == 0) {
                        txtVideoLength!!.text = "00:00"
                        updateProgressBar()
                    }
                }
            }
        }
    }

    private fun setBitmap(mVideoUri: Uri) {
        timeLineView!!.setVideo(mVideoUri)
    }

    private fun onVideoPrepared(mp: MediaPlayer) {
        // Adjust the size of the video
        // so it fits on the screen
        //TODO manage proportion for video
        /*int videoWidth = mp.getVideoWidth();
        int videoHeight = mp.getVideoHeight();
        float videoProportion = (float) videoWidth / (float) videoHeight;
        int screenWidth = llVideoView.getWidth();
        int screenHeight = llVideoView.getHeight();
        float screenProportion = (float) screenWidth / (float) screenHeight;
        ViewGroup.LayoutParams lp = videoView.getLayoutParams();

        if (videoProportion > screenProportion) {
            lp.width = screenWidth;
            lp.height = (int) ((float) screenWidth / videoProportion);
        } else {
            lp.width = (int) (videoProportion * (float) screenHeight);
            lp.height = screenHeight;
        }
        videoView.setLayoutParams(lp);*/
        mDuration = videoView!!.duration / 1000
        setSeekBarPosition()
    }

    private fun updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100)
    }

    private val mUpdateTimeTask: Runnable = object : Runnable {
        override fun run() {
            if (seekBarVideo!!.progress >= seekBarVideo!!.max) {
                seekBarVideo!!.progress = videoView!!.currentPosition - mStartPosition * 1000
                val videoLength = milliSecondsToTimer(seekBarVideo!!.progress.toLong()) + ""
                txtVideoLength!!.text = videoLength
                videoView!!.seekTo(mStartPosition * 1000)
                videoView!!.pause()
                seekBarVideo!!.progress = 0
                txtVideoLength!!.text = "00:00"
                imgPlay!!.setBackgroundResource(R.drawable.ic_white_play)
            } else {
                seekBarVideo!!.progress = videoView!!.currentPosition - mStartPosition * 1000
                val videoLength = milliSecondsToTimer(seekBarVideo!!.progress.toLong()) + ""
                txtVideoLength!!.text = videoLength
                mHandler.postDelayed(this, 100)
            }
        }
    }

    private fun setSeekBarPosition() {
        if (mDuration >= mMaxDuration) {
            mStartPosition = 0
            mEndPosition = mMaxDuration
            timeLineBar!!.setThumbValue(0, mStartPosition * 100 / mDuration.toFloat())
            timeLineBar!!.setThumbValue(1, mEndPosition * 100 / mDuration.toFloat())
        } else {
            mStartPosition = 0
            mEndPosition = mDuration
        }
        mTimeVideo = mDuration
        timeLineBar!!.initMaxWidth()
        seekBarVideo!!.max = mMaxDuration * 1000
        videoView!!.seekTo(mStartPosition * 1000)
        var mStart = mStartPosition.toString() + ""
        if (mStartPosition < 10) mStart = "0$mStartPosition"
        val startMin = mStart.toInt() / 60
        val startSec = mStart.toInt() % 60
        var mEnd = mEndPosition.toString() + ""
        if (mEndPosition < 10) mEnd = "0$mEndPosition"
        val endMin = mEnd.toInt() / 60
        val endSec = mEnd.toInt() % 60
        txtVideoTrimSeconds!!.text = String.format(
            Locale.US,
            "%02d:%02d - %02d:%02d",
            startMin,
            startSec,
            endMin,
            endSec
        )
    }

    /**
     * called when playing video completes
     */
    private fun onVideoCompleted() {
        mHandler.removeCallbacks(mUpdateTimeTask)
        seekBarVideo!!.progress = 0
        videoView!!.seekTo(mStartPosition * 1000)
        videoView!!.pause()
        imgPlay!!.setBackgroundResource(R.drawable.ic_white_play)
    }

    /**
     * Handle changes of left and right thumb movements
     *
     * @param index index of thumb
     * @param value value
     */
    private fun onSeekThumbs(index: Int, value: Float) {
        when (index) {
            BarThumb.LEFT -> {
                mStartPosition = (mDuration * value / 100L).toInt()
                videoView!!.seekTo(mStartPosition * 1000)
            }
            BarThumb.RIGHT -> {
                mEndPosition = (mDuration * value / 100L).toInt()
            }
        }
        mTimeVideo = mEndPosition - mStartPosition
        seekBarVideo!!.max = mTimeVideo * 1000
        seekBarVideo!!.progress = 0
        videoView!!.seekTo(mStartPosition * 1000)
        var mStart = mStartPosition.toString() + ""
        if (mStartPosition < 10) mStart = "0$mStartPosition"
        val startMin = mStart.toInt() / 60
        val startSec = mStart.toInt() % 60
        var mEnd = mEndPosition.toString() + ""
        if (mEndPosition < 10) mEnd = "0$mEndPosition"
        val endMin = mEnd.toInt() / 60
        val endSec = mEnd.toInt() % 60
        txtVideoTrimSeconds!!.text = String.format(
            Locale.US,
            "%02d:%02d - %02d:%02d",
            startMin,
            startSec,
            endMin,
            endSec
        )
    }

    private fun onStopSeekThumbs() {
//        mMessageHandler.removeMessages(SHOW_PROGRESS);
//        videoView.pause();
//        mPlayView.setVisibility(View.VISIBLE);
    }

    fun milliSecondsToTimer(milliseconds: Long): String {
        var finalTimerString = ""
        val secondsString: String
        val minutesString: String
        val hours = (milliseconds / (1000 * 60 * 60)).toInt()
        val minutes = (milliseconds % (1000 * 60 * 60)).toInt() / (1000 * 60)
        val seconds = (milliseconds % (1000 * 60 * 60) % (1000 * 60) / 1000).toInt()
        // Add hours if there
        if (hours > 0) {
            finalTimerString = "$hours:"
        }

        // Prepending 0 to seconds if it is one digit
        secondsString = if (seconds < 10) {
            "0$seconds"
        } else {
            "" + seconds
        }
        minutesString = if (minutes < 10) {
            "0$minutes"
        } else {
            "" + minutes
        }
        finalTimerString = "$finalTimerString$minutesString:$secondsString"

        // return timer string
        return finalTimerString
    }
}