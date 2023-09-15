# 融云音视频 Android 示例应用

本示例应用为一个演示集合，集中演示[融云实时音视频 Android SDK] 支持的[音视频会议]、[音视频通话]与[低延迟直播]场景与功能。

<p align="center" style="background-color: #e1e5eb; padding: 10px; margin-top: 5px; margin-bottom:5px;">
<img src="../images/rtcapp-demo.png" width="20%">
</p>

如果想要直接下载 Android APK 体验应用功能，欢迎[前往融云官网下载场景的示例应用](https://www.rongcloud.cn/downloads/demo)。

## 前提条件

* 示例应用与组件目录下的 README 分别列出了具体的前提条件要求。
* 示例应用需要有 App Key 才能换取客户端连接融云服务器的身份凭证。请开通融云开发者账号，并在后台 [获取 App Key](https://developer.rongcloud.cn/app/appkey)。
* 使用 Android Studio 3.0 或以上版本。如果您尚未安装，请在 [官网下载](https://developer.android.com/studio/index.html) 并安装。
* Android SDK 5.0 或以上版本，即 minSdkVersion >= 21。
* Android Build Tools 21 或以上版本。为获得更好的编译体验，建议使用最新版。
* JDK 1.8 或以上版本。
* Gradle 4.0 及以上版本。

具体参考如下设置：
        
```groovy
compileSdkVersion: 33,
minSdkVersion    : 21,
targetSdkVersion : 29,
```

## 设置设备

本示例应用必须部署到搭载 Android 5.0 或更高版本的 Android 设备或 Android 模拟器。

* 如要使用 Android 设备，请按照在[硬件设备上运行应用](https://developer.android.com/studio/run/device.html)中的说明进行操作。
* 如要使用 Android 模拟器，您可以使用 [Android Studio 附带的 Android 虚拟设备 (AVD)](https://developer.android.com/studio/run/managing-avds.html) 管理器创建虚拟设备并安装模拟器。

## 运行示例应用

1. 克隆本存储库下载示例代码。
1. 在 Android Studio 中，选择 **Open an Existing project**。
1. 打开克隆下载的代码仓库，等待导入完成。
1. 在示例应用的 `DemoApplication.java` 中，填入从融云开发者获取的 App Key 与 App Secret。

    路径：**Project** 视图下 `rtcapp/src/main/cn.rongcloud.demo`。

    ```java
    /**
    * TODO: 请替换成您自己申请的 AppKey
    */
    public static final String APP_KEY = "";
    
    /**
    * TODO: 请替换成您自己 AppKey 对应的 Secret
    * 这里仅用于模拟从 App Server 获取 UserID 对应的 Token, 开发者在上线应用时客户端代码不要存储该 Secret，
    * 否则有被用户反编译获取的风险，拥有 Secret 可以向融云 Server 请求高级权限操作，对应用安全造成恶劣影响。
    */
    public static final String APP_SECRET = "";
    ```

1. 在 Android Studio 顶部选择 `rtcapp`，点击运行。

## 依赖项

本示例应用已添加以下融云相关依赖项：

```groovy
implementation project(path: ':common')
implementation project(path: ':meeting1v1')
implementation project(path: ':live')
implementation project(path: ':screenshare')
implementation project(path: ':cdnlivestream')
implementation project(path: ':callplus')
implementation rootProject.ext.dependencies.im_lib
```

## 文档

- [音视频通话（CallLib/Kit）文档]
- [音视频通话（CallPlus）文档]
- [音视频直播/会议文档]

## 支持

源码地址 [Github](https://github.com/rongcloud/rtc-quickdemo-android)，任何问题可以通过 Github Issues 提问。

Demo 源码遵循 MIT 开源协议。

<!-- Reference links below -->

<!-- links to official website pages-->

[音视频通话]: https://www.rongcloud.cn/product/call

[音视频会议]: https://www.rongcloud.cn/product/meeting

[低延迟直播]: https://www.rongcloud.cn/product/live

[融云实时音视频 Android SDK]: https://www.rongcloud.cn/downloads

<!-- links to docs -->

[音视频通话（CallLib/Kit）文档]: https://doc.rongcloud.cn/call/Android/5.X/demo

[音视频通话（CallPlus）文档]: https://doc.rongcloud.cn/callplus/Android/1.X/make-first-call

[音视频直播/会议文档]: https://doc.rongcloud.cn/rtc/Android/5.X/demo

<!-- links to ops -->

[获取 App Key]: https://developer.rongcloud.cn/app/appkey/