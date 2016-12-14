[![](https://jitpack.io/v/dsokolov/RxAndroidAudio.svg)](https://jitpack.io/#dsokolov/RxAndroidAudio)

# RxAndroidAudio

RxAndroidAudio (RxAA) is a android library for Rx-style sound input and output.

And yes, it's written in Kotlin.

## Setup

To add RxAA to your project, include the following in your project level `build.gradle` file:

```groovy
allprojects {
		repositories {
			...
			maven { url "https://jitpack.io" }
		}
	}
```

And on your application level `build.gradle`:

```groovy
compile 'com.github.dsokolov:RxAndroidAudio:0.6'
```

## Usage

First of all create an audio options. There are some predefined `AudioOptions` instances. 

```Kotlin
val playbackAudioOptions = AudioOptions.PCM_44100_MONO_PLAYBACK
val recordAudioOptions = AudioOptions.PCM_44100_MONO_RECORD
```

Then you can record raw audio to file:

```Kotlin
val source = RecordObservable.create16bit(recordAudioOptions)
val destination = OutputStreamSubscriber.create16bit(FileOutputStream(fileName), recordAudioOptions)
val subs = Observable.
        create(source).
        onBackpressureDrop().
        subscribeOn(Schedulers.newThread()).
        subscribe(destination)
```

or play raw audio from file;

```Kotlin
val source = InputStreamObservable.create16bit(FileInputStream("/mnt/sdcard/temp.pcm"), playbackAudioOptions)
val destination = PlaybackSubscriber.create16Bit(playbackAudioOptions)
val subs = Observable.
        create(source).
        onBackpressureDrop().
        subscribeOn(Schedulers.newThread()).
        subscribe(destination)
```

You can apply filters for you sound stream. For example, lowpass filter on 440Hz:

```Kotlin
val lowpassFilter = LowpassFilter.crate16bit(440f, 1f, playbackAudioOptions)
val subs = Observable.
        create(source).
        onBackpressureDrop().
        map { data -> lowpassFilter.filter(data) }.
        subscribeOn(Schedulers.newThread()).
        subscribe(destination)
```

Please take a look at `app` module for more usage examples.

## Contribution

Feel free create issues, pull requests and forks.

License
--------

    Copyright 2016 Dmitry I. Sokolov

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.