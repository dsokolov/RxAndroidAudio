package me.ilich.rxandroidaudio

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
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
        findViewById(R.id.playback_start).setOnClickListener {
            subs?.unsubscribe()

            val options = AudioOptions.PLAYBACK_PCM_16BIT_44100_MONO

            //val source = ToneObservable.create<ShortArray>(220.0, options)
            val source = InputStreamObservable.create<ShortArray>(FileInputStream("/mnt/sdcard/temp.pcm"), options)
            val destination = PlaybackSubscriber.create<ShortArray>(options)
            //val destination = OutputStreamSubscriber.create<ShortArray>(FileOutputStream("/mnt/sdcard/temp.pcm"), options)

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
