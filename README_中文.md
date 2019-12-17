# playin-android

PlayIN ADs -The Next Generation Playable ADs Solution

README: [English](https://github.com/playinads/playin-android) | [中文](https://github.com/playinads/playin-android/blob/master/README_中文.md)

## 项目介绍
这是一种新型的广告技术，借助云游戏技术，应用于移动端。这种技术主要针对移动端游戏的广告推广，区别于一般游戏做的小视频推广，这种技术支持广告受众在没有下载、注册游戏的情况下，在游戏广告视频页面即点即玩，给玩家带来全新体验，让试玩方便快捷。
这种技术主要服务于游戏广告主，能够大幅度提升广告转化率，给游戏公司和广告主带来更高效的推广方式。
技术是我们的核心竞争力，目前我们拥有一支技术实力雄厚的技术团队，期待未来更广阔的发展。
## 技术简介
PlayIN Ads是一种新型的试玩广告技术，可以让用户在不安装APP的情况下，直接试玩游戏。云端使用大量真实及虚拟设备，提前安装好需要试玩的游戏。当用户选择试玩时，云端设备的画面视频以“流媒体”的方式传输到用户设备中，当用户操控自己本地设备时，所有操控的信息又被实时传送到云端，并被“作用于”云端设备上。更多项目详情可至[PlayIN](https://playinads.com)官网上查看。

## 功能特色
如何将这套技术，更好的打造成一套完整的解决方案，PlayIN在实现时采用了独创性的一些技术核心点：
1. *优化音视频编解码，优化网络协议，同时组建多数据节点，在保证传输质量的前提下，极大提高了传输速度，降低了数据延时*
2. *云端设备支持安卓和iOS系统，覆盖几乎所有移动端游戏*
3. *云端设备真虚结合，不仅解决了海量设备的部署问题，同时也大幅降低了系统成本*
4. *不仅支持定点试玩，还支持多存档试玩，一个游戏可以创建多个试玩场景，可以让单个游戏得到更多展示，以适应更多不同类型用户*

我们设想的市场为全球市场，已经在香港注册成功了一家公司，方便未来海外市场的推广。目前全球范围内还没有企业或个人开发并运用此项技术，凭借我们自身的实力相信未来一段时间也不会有竞争对手。

## 开发要求

* Android Studio
* Android 4.0.3+

## 集成方式

implementation 'com.tech:playinsdk:1.1.6'

## 使用方法

在客户端接入使用之前，需要到[PlayIN](https://playinads.com)官网上根据引导注册账户，上传广告，快速创建专属游戏试玩，您需保存网站提供的sdkKey及adid并将提供给开发人员。
每次试玩之前需要检测是否有机器可供使用，如果有机器，界面上可体现试玩入口供用户试玩点击，如果没有机器可用即检测方法返回NO，则隐藏试玩入口。游戏试玩最大时长受限于注册广告时设置的时长，在项目试玩时，设置的总时长应小于等于网站注册时设置的总时长。

#### 1 导入PlayIn引用
```java
import com.tech.playinsdk.PlayInSdk;
import com.tech.playinsdk.listener.InitListener;
```
#### 2 在每次试玩之前都必须重新初始化配置和检测是否有可用机器，分别传入sdkKey和adid进行配置和检测
```java
// 初始化PlayIN的sdk
private void configPlayin() {
  PlayInSdk.getInstance().configWithKey(Constants.SDK_KEY, new InitListener() {
    @Override
    public void success() {
      checkAvailable();
    }
    @Override
    public void failure(Exception ex) {
      Toast.makeText(MainActivity.this, "初始化失败", Toast.LENGTH_SHORT).show();
    }
  });
}

// 检查游戏是否可以试玩
private void checkAvailable() {
  PlayInSdk.getInstance().checkAvailable(Constants.SDK_KEY, new HttpListener<Boolean>() {
    @Override
    public void success(Boolean result) {
     	// 跳转到试玩界面
    }
    @Override
    public void failure(HttpException e) {
      Toast.makeText(MainActivity.this, "游戏不可以试玩", Toast.LENGTH_SHORT).show();
    }
  });
}
```
#### 3 在有可用机器的前提下，可以进行试玩

```java
<com.tech.playinsdk.PlayInView
  android:id="@+id/playView"
  android:layout_width="match_parent"
  android:layout_height="match_parent" />

  private void playGame() {
        PlayInView playView = findViewById(R.id.playView);
        playView.play(adId, this);
	      // playView.finish();
    }
```
#### 4 实现PlayIn的事件监听
```objc
public class PlayActivity implements PlayListener {
    @Override
    public void onPlayStart(int duration) {
        // 可以隐藏加载框
    }
    @Override
    public void onPlayEnd(boolean manual) {
        // 试玩结束可以返回
    }
    @Override
    public void onPlayError(Exception ex) {
    }
}
```
## 合作联系方式

如果在使用过程中有任何的问题或建议请发邮件至(lijunge@playin.tech)，我们非常欢迎得到您的反馈。如果您有意向与我们公司合作，请发邮件或浏览我们的官网[PlayIN](https://playinads.com)，注册账户后我们将安排专人为您服务。

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
