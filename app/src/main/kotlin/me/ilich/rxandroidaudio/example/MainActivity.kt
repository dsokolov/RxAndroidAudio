package me.ilich.rxandroidaudio.example

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import me.ilich.rxandroidaudio.AudioOptions
import me.ilich.rxandroidaudio.RecordObservable
import me.ilich.rxandroidaudio.InputStreamObservable
import me.ilich.rxandroidaudio.OutputStreamSubscriber
import me.ilich.rxandroidaudio.PlaybackSubscriber
import rx.Observable
import rx.Subscription
import rx.schedulers.Schedulers
import java.io.FileInputStream
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    var subs: Subscription? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
        findViewById(R.id.record_stop).setOnClickListener {
            subs?.unsubscribe()
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
        findViewById(R.id.playback_stop).setOnClickListener {
            subs?.unsubscribe()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        subs?.unsubscribe()
    }

}
