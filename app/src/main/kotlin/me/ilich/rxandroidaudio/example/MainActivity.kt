package me.ilich.rxandroidaudio.example

import android.Manifest
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import com.tbruyelle.rxpermissions.RxPermissions
import me.ilich.rxandroidaudio.*
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    var permisionSubs: Subscription? = null
    var subs: Subscription? = null

    lateinit var audioLevel: ProgressBar
    lateinit var startRecordButton: Button
    lateinit var startPlaybackButton: Button
    lateinit var startPlaybackLowpassButton: Button
    lateinit var startLevelButton: Button
    lateinit var stopButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        audioLevel = findViewById(R.id.audio_level_measure) as ProgressBar
        audioLevel.max = 200
        startRecordButton = findViewById(R.id.record_start) as Button
        startPlaybackButton = findViewById(R.id.playback_start) as Button
        startPlaybackLowpassButton = findViewById(R.id.playback_lowpass_start) as Button
        startLevelButton = findViewById(R.id.rms_start) as Button
        stopButton = findViewById(R.id.stop) as Button

        val playbackAudioOptions = AudioOptions.PCM_44100_MONO_PLAYBACK
        val recordAudioOptions = AudioOptions.PCM_44100_MONO_RECORD
        val fileName = "/mnt/sdcard/temp.pcm"

        permisionSubs = RxPermissions(this).
                request(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE).
                subscribe { granted ->
                    startRecordButton.isEnabled = granted
                    startPlaybackButton.isEnabled = granted
                    startPlaybackLowpassButton.isEnabled = granted
                    startLevelButton.isEnabled = granted
                    stopButton.isEnabled = granted
                }


        startRecordButton.setOnClickListener {
            subs?.unsubscribe()
            val source = RecordObservable.create16bit(recordAudioOptions)
            val destination = OutputStreamSubscriber.create16bit(FileOutputStream(fileName), recordAudioOptions)
            subs = Observable.
                    create(source).
                    onBackpressureDrop().
                    subscribeOn(Schedulers.newThread()).
                    subscribe(destination)
        }

        startPlaybackButton.setOnClickListener {
            subs?.unsubscribe()
            val source = InputStreamObservable.create16bit(FileInputStream("/mnt/sdcard/temp.pcm"), playbackAudioOptions)
            val destination = PlaybackSubscriber.create16Bit(playbackAudioOptions)
            subs = Observable.
                    create(source).
                    onBackpressureDrop().
                    subscribeOn(Schedulers.newThread()).
                    subscribe(destination)
        }

        startPlaybackLowpassButton.setOnClickListener {
            subs?.unsubscribe()
            val lowpassFilter = LowpassFilter.crate16bit(2000f, 1f, playbackAudioOptions)
            val source = InputStreamObservable.create16bit(FileInputStream("/mnt/sdcard/temp.pcm"), playbackAudioOptions)
            val destination = PlaybackSubscriber.create16Bit(playbackAudioOptions)
            subs = Observable.
                    create(source).
                    onBackpressureDrop().
                    map { data -> lowpassFilter.filter(data) }.
                    subscribeOn(Schedulers.newThread()).
                    subscribe(destination)
        }

        startLevelButton.setOnClickListener {
            val source = RecordObservable.create16bit(recordAudioOptions)
            subs = Observable.
                    create(source).
                    sample(500L, TimeUnit.MILLISECONDS).
                    map { samples ->
                        AudioLevel.maxDecibel(samples)
                    }.
                    map { peek -> peek + 110.0 }.
                    subscribeOn(Schedulers.newThread()).
                    observeOn(AndroidSchedulers.mainThread()).
                    subscribe {
                        Log.v("Sokolov", "$it")
                        audioLevel.progress = it.toInt()
                    }
        }

        stopButton.setOnClickListener {
            subs?.unsubscribe()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        subs?.unsubscribe()
    }

}
