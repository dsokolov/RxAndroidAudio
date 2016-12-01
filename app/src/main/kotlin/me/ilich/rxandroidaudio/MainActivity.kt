package me.ilich.rxandroidaudio

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import rx.Observable
import rx.Subscription
import rx.schedulers.Schedulers


class MainActivity : AppCompatActivity() {

    var subs: Subscription? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById(R.id.playback_start).setOnClickListener {

            val options = AudioOptions.PLAYBACK_PCM_16BIT_44100_MONO;

            val source = ToneObservable.create<ShortArray>(220.0, options)
            val destination = PlaybackSubscriber.create<ShortArray>(options)

            subs = Observable.
                    create(source).
                    subscribeOn(Schedulers.newThread()).
                    subscribe(destination)
        }
        findViewById(R.id.playback_stop).setOnClickListener {
            subs?.unsubscribe()
        }
    }

}
