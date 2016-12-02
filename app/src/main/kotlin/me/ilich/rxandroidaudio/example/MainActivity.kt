package me.ilich.rxandroidaudio.example

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.ProgressBar
import me.ilich.rxandroidaudio.*
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    var subs: Subscription? = null

    lateinit var audioLevel: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        audioLevel = findViewById(R.id.audio_level_measure) as ProgressBar
        audioLevel.max = Short.MAX_VALUE.toInt()

        val playbackAudioOptions = AudioOptions.PCM_16BIT_44100_MONO_PLAYBACK
        val recordAudioOptions = AudioOptions.PCM_16BIT_44100_MONO_RECORD
        val fileName = "/mnt/sdcard/temp.pcm"


        findViewById(R.id.record_start).setOnClickListener {
            subs?.unsubscribe()
            val source = RecordObservable.create<ShortArray>(recordAudioOptions)
            val destination = OutputStreamSubscriber.create<ShortArray>(FileOutputStream(fileName), recordAudioOptions)
            subs = Observable.
                    create(source).
                    onBackpressureDrop().
                    subscribeOn(Schedulers.newThread()).
                    subscribe(destination)
        }

        findViewById(R.id.playback_start).setOnClickListener {
            subs?.unsubscribe()
            val source = InputStreamObservable.create<ShortArray>(FileInputStream("/mnt/sdcard/temp.pcm"), playbackAudioOptions)
            val destination = PlaybackSubscriber.create<ShortArray>(playbackAudioOptions)
            subs = Observable.
                    create(source).
                    onBackpressureDrop().

                    subscribeOn(Schedulers.newThread()).
                    subscribe(destination)
        }

        findViewById(R.id.rms_start).setOnClickListener {
            val source = RecordObservable.create<ShortArray>(recordAudioOptions)
            subs = Observable.
                    create(source).
                    sample(500L, TimeUnit.MILLISECONDS).
                    map {
                        AudioLevel.db(it)
                    }.
                    subscribeOn(Schedulers.newThread()).
                    observeOn(AndroidSchedulers.mainThread()).
                    subscribe { rms ->
                        Log.v("Sokolov", "$rms")
                        audioLevel.progress = rms.toInt()
                    }
        }

        findViewById(R.id.stop).setOnClickListener {
            subs?.unsubscribe()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        subs?.unsubscribe()
    }

}
