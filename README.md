# playin-android

PlayIN ADs -The Next Generation Playable ADs Solution

README: [English](https://github.com/playinads/playin-iOS) | [中文](https://github.com/playinads/playin-android/blob/master/README_中文.md)


![image](https://github.com/lijunge/PlayINDemo/raw/master/PlayIn_1.gif) ![image](https://github.com/lijunge/PlayINDemo/raw/master/PlayIn_2.gif)

## Project introduction
The PlayIN Project, a next-generation playable advertisement technology based on cloud gaming technologies, can substantially optimize the user experience with playable ads. It is designed to advertise and promote mobile game apps. 

What makes it stand out from the Html5 playable ads is that PlayIN lets the target audience play the actual games immediately, skipping the annoying downloading and registration phase. As a result, PlayIN brings a whole new enjoyable and convenient playable ads experience to the audience. 

PlayIN is committed to helping game companies and advertisers maximize their playable Ads conversion rate in efficient manners.

Technology is at the very core of our competitiveness. The PlayIN team is highly experienced and technically strong, and looks forward to bringing more exciting technologies to better our lives along the way.

## Technical Introduction
PlayIN is a new type of playable advertisement technology, allowing users to play the actual mobile games before installing them. Playable games are deployed in the cloud among a mixture of physical and virtual devices, and the gameplay is live-streamed to the user's devices. User input through their local devices gets relayed back to the cloud in real time, and "acts on" the cloud devices. More details of the project can be found on PlayIN's official website [PlayIN](https://playinads.com).

## Functional features
The PlayIN total solution excels with these unique features:
1. *Optimized audio and video coding and decoding; optimized network protocols; multiple cloud data nodes for lower latency and higher transmission quality and availability*
2. *Cloud devices supporting Android and iOS, covering almost all mobile games*
3. *Combination of physical and virtual cloud devices for easy deployment and cost-efficiency*
4. *Multi-scene demo play on top of single-scene demo play allowing a single game to generate multiple playable scenes to fit in different audiences preferences*

PlayIN aims for the global market. An entity in Hong Kong has been established to facilitate the promotion of our technology to the world which hasn't seen anything similar to ours. And we firmly believe that nothing will come even close in the predictable future.

## Development Requirements

* Android Studio
* Android 4.0.3+

## Integration

implementation 'com.tech:playinsdk:1.0.2'

## Usage guide

Before gaining client access, go to[PlayIN](https://playinads.com)and sign up following the instructions on the website. Upload your game app's App Store download link（URL）, and create a dedicated playable demo. You need to save the sdkKey and Adid provided by the website and provide them to your development team. 

Before each demo play, it is necessary to check whether there is a cloud device available. If yes, the interface can show the demo entrance button for the user to press on. Otherwise it should reject the request and hide the demo entrance. 

The maximum time of game demo play is limited by the advertisement's actual time length set at registration. The total time set for game demo play should be equal or less than the total time set at website registration.

#### Step 1 
```java
import com.tech.playinsdk.PlayInSdk;
import com.tech.playinsdk.listener.InitListener;`
```
#### Step 2 

Initialize and check if there is any device available with your sdkKey and adid:

```java
private void configPlayin() {
  PlayInSdk.getInstance().configWithKey(Constants.SDK_KEY, new InitListener() {
    @Override
    public void success() {
      checkAvailable();
    }
    @Override
    public void failure(Exception ex) {
      Toast.makeText(MainActivity.this, "Initialization failure", Toast.LENGTH_SHORT).show();
    }
  });
}

// Check game can to play
private void checkAvailable() {
  PlayInSdk.getInstance().checkAvailable(Constants.SDK_KEY, new HttpListener<Boolean>() {
    @Override
    public void success(Boolean result) {
      // Jump to the play activity
    }
    @Override
    public void failure(HttpException e) {
      Toast.makeText(MainActivity.this, "No equipment to play with", Toast.LENGTH_SHORT).show();
    }
  });
}
```
#### Step 3

Duration is the total length of game demo play (which should be no more than the maximum time set at registration on PlayIN). Units are timed in seconds. Times is the number of game demo plays. The maximum number of demo plays is 2. 

For example: duration = 120, times = 2, it is divided into two demo plays. The single demo play time is 60s, that is, single demo play time = duration / times

For two demos, after the first demo, a page will be prompted for users to choose between continue the demo or download App from GooglePlay. After the second demo, users can choose to download App from GooglePlay or close the demo.

```java
<com.tech.playinsdk.PlayInView
  android:id="@+id/playView"
  android:layout_width="match_parent"
  android:layout_height="match_parent" />

  private void playGame() {
        String adId = "";
        String appName = "";
        String appIcon = "";
        String appCover = "";
        String appDownload = "";
        int playDuration = 0;
        int playTime = 0;
        PlayInView playView = findViewById(R.id.playView);
        playView.play(adId, appName, appIcon, appCover, appDownload,
                playDuration, playTime, this);
    }
```
#### Step 4

```java
public class PlayActivity implements PlayListener {
    @Override
    public void onPlaystart() {
        // can hide loading
    }
    @Override
    public void onPlayClose() {
        //  can finish at play over
    }
    @Override
    public void onPlayError(Exception ex) {
    }
}
```
## Contact us

If you have any questions or suggestions while using PlayIN, please email them to (lijunge@playin.tech).We welcome your feedback. If you are interested in cooperating with us, please email us or browse our official website at [PlayIN](https://playinads.com).Once you finish registering your account, Someone from our team will contact you immediately.

## License
```
The MIT License (MIT)

Copyright (c) 2019 playin

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
