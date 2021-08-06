# 融云音视频会议与直播 Android 示例应用

本示例应用为一个演示集合，集中演示[融云实时音视频 Android SDK] 支持的[音视频会议]与[低延迟直播]场景与功能，适用于不含呼叫流程的音视频业务场景。

<p align="center" style="background-color: #e1e5eb; padding: 10px; margin-top: 5px; margin-bottom:5px;">
<img src="../images/rtcapp-demo.png" width="20%">
</p>

如果想要直接下载 Android APK 体验应用功能，欢迎[前往融云官网下载场景的示例应用](https://www.rongcloud.cn/downloads/demo)。

## 前提条件

* 示例应用均需要有 App Key 才能换取客户端连接融云服务器的身份凭证。通过开发者后台[获取 App Key]。
* 必须使用 Android Studio。如果您尚未安装，请[下载](https://developer.android.com/studio/index.html)并[安装](https://developer.android.com/studio/install.html?pkg=studio)。
* Android SDK 最新版
* Android Build Tools 最新版

## 设置设备

本示例应用必须部署到搭载 Android 4.4 或更高版本的 Android 设备或 Android 模拟器。

* 如要使用 Android 设备，请按照在[硬件设备上运行应用](https://developer.android.com/studio/run/device.html)中的说明进行操作。
* 如要使用 Android 模拟器，您可以使用 [Android Studio 附带的 Android 虚拟设备 (AVD)](https://developer.android.com/studio/run/managing-avds.html) 管理器创建虚拟设备并安装模拟器。

## 运行示例应用

1. 克隆本存储库下载示例代码。
1. 在 Android Studio 中，选择 **Open an Existing project**。
1. 打开克隆下载的代码仓库，等待导入完成。<!-- 考虑：在导入到运行之间，容易出现什么问题，导致体验受阻?-- >
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

```
implementation project(path: ':common')
implementation project(path: ':meeting1v1')
implementation project(path: ':live')
implementation project(path: ':screenshare')
implementation project(path: ':cdnlivestream')
implementation rootProject.ext.dependencies.im_lib
```

## 文档

- [音视频会议文档]
- [低延迟直播文档]

## 支持

源码地址 [Github](https://github.com/rongcloud/rtc-quickdemo-android)，任何问题可以通过 Github Issues 提问。

<!-- License ?-->


<!-- Reference links below -->

<!-- links to official website pages-->

[音视频通话]: https://www.rongcloud.cn/product/call

[音视频会议]: https://www.rongcloud.cn/product/meeting

[低延迟直播]: https://www.rongcloud.cn/product/live

[融云实时音视频 Android SDK]: https://www.rongcloud.cn/downloads

<!-- links to docs -->

[音视频通话文档]: https://docs.rongcloud.cn/v4/5X/views/rtc/call/intro/ability.html

[音视频会议文档]: https://docs.rongcloud.cn/v4/5X/views/rtc/meeting/ios/intro/intro.html

[低延迟直播文档]: https://docs.rongcloud.cn/v4/5X/views/rtc/livevideo/ios/intro/intro.html

<!-- links to ops -->

[获取 App Key]: https://developer.rongcloud.cn/app/appkey/