package me.ilich.rxandroidaudio

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import rx.Observable
import rx.Subscriber
import rx.Subscription
import rx.schedulers.Schedulers


class MainActivity : AppCompatActivity() {

    var t: Thread? = null //Object that hold the audio processing thread
    val sr = 44100 //sampling rate
    var isRunning = true //means of switching on and off

    var subs: Subscription? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById(R.id.playback_start).setOnClickListener {

            val buffsize = AudioTrack.getMinBufferSize(sr, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
            val audioTrack = AudioTrack(AudioManager.STREAM_MUSIC, sr,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, buffsize,
                    AudioTrack.MODE_STREAM)
            subs = Observable.
                    create<ShortArray> { subscriber ->
                        val samples = ShortArray(buffsize)
                        val amp: Short = 10000

                        var ph = Math.PI
                        val fr = 440.0
                        for (i in 0..buffsize - 1) {
                            val s = amp * Math.sin(ph)
                            if (s > 0.0) {
                                samples[i] = 32767
                            }
                            if (s < 0.0) {
                                samples[i] = -32767
                            }
                            ph += 2 * Math.PI * fr / sr
                        }
                        while (!subscriber.isUnsubscribed) {
                            subscriber.onNext(samples)
                            audioTrack.write(samples, 0, buffsize)
                        }
                        subscriber.onCompleted()
                    }.
                    subscribeOn(Schedulers.newThread()).
                    subscribe(object : Subscriber<ShortArray>() {

                        override fun onStart() {
                            audioTrack.play()
                        }

                        override fun onCompleted() {
                            audioTrack.stop()
                            audioTrack.release()
                        }

                        override fun onError(e: Throwable) {
                            audioTrack.stop()
                            audioTrack.release()
                            throw e
                        }

                        override fun onNext(data: ShortArray) {
                            audioTrack.write(data, 0, data.size)
                        }

                    })

/*            isRunning = true
            t = createToneThread()
            t?.start()*/
        }
        findViewById(R.id.playback_stop).setOnClickListener {
            subs?.unsubscribe()
            /*isRunning = false
            t = null*/
        }
    }


    fun createToneThread() = Thread() {
        //set process priority to maximum to get good performance
        //priority = Thread.MAX_PRIORITY
        //set the buffer size
        val buffsize = AudioTrack.getMinBufferSize(sr, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
        //create an audiotrack  object
        val audioTrack = AudioTrack(AudioManager.STREAM_MUSIC, sr,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, buffsize,
                AudioTrack.MODE_STREAM)

        //create signal buffer and define the parameters
        val samples = ShortArray(buffsize)
        val amp: Short = 10000

        var i = 0
        while (i < buffsize) {
            samples[i] = (-amp).toShort()
            samples[i + 1] = (-amp).toShort()
            samples[i + 2] = amp
            samples[i + 3] = amp
            i += 4
        }

        //start audio
        audioTrack.play()

        //define the synthesis loop
        while (isRunning) {
            audioTrack.write(samples, 0, buffsize)
        }
        //closing  of the audio device
        audioTrack.stop()
        audioTrack.release()

    }

}
