# RongCloud-RTC Android Quick Demo

融云音视频 Android 端快速集成示例源码。

一个工程共包含两个 App 及多个相关模块，具体如下：

| 名称        | 类型   | 依赖 SDK | 简介                                                      |
| ----------- | ------ | -------- | --------------------------------------------------------- |
| rtcapp      | App    | IMLib    | RTCLib 展示 Demo，包含 meeting1v1, live, screenshare 模块 |
| callapp     | App    | IMLib    | CallLib, CallKit 展示 Demo，包含 calllib, callkit 模块    |
| common      | Module | 公用组件 | 包含模拟 AppServer 提供获取 Token 能力；通用 UI 功能函数  |
| meeting1v1  | Module | RTCLib   | 会议展示，支持两人会议                                    |
| live        | Module | RTCLib   | 直播展示，直播观众之间音视频互动                          |
| screenshare | Module | RTCLib   | 屏幕共享展示                                              |
| calllib     | Module | CallLib  | 不带 UI 的呼叫功能展示                                    |
| callkit     | Module | CallKit  | 带 UI 的呼叫功能展示                                      |

编译运行前，请将各自 App 里 DemoApplication.java 中的 `APP_KEY` 和 `APP_SECRET` 改成在融云开发者后台申请的值。
