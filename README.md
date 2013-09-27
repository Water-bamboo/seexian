看西安 [![Google Play](http://developer.android.com/images/brand/en_generic_rgb_wo_45.png)](https://play.google.com/store/apps/details?id=com.comic.seexian)
=======
<img src="https://www.evernote.com/shard/s153/sh/95507f06-61e9-4842-b63a-95862a71a8a0/cec25dd4f8c199f25c825347d49345b4/deep/0/device-2013-09-16-210146-1.png">
简介
=======
一个介绍西安风景的应用，通过“看西安”拍摄你在西安旅行时的照片，“看西安”将会把这些照片通过新浪微博分享给你的朋友，
同时你也可以看到这些风景的介绍，了解背后的历史。还可以提供给你拍摄地点周边的信息，使旅行不再为找地方吃饭而麻烦，
提供一些西安的旅游信息，叫你不用做功课，也可以玩的很开心。
Build
=======
Require Android SDK and develop tools:
* [Android SDK](http://developer.android.com/sdk/index.html)
* [Android歷史版本](http://zh.wikipedia.org/zh-cn/Android%E6%AD%B7%E5%8F%B2%E7%89%88%E6%9C%AC)

Base on SDK version 10 (Gingerbread)

Necessary libs need to add into build path.
* baidumapapi_v2_1_3.jar
* weiboSDK2.1_130806.jar

And two .so files in folder armebi
* libBaiduMapSDK_v2_1_3.so
* libBaiduMapVOS_v2_1_3.so

Knowledge
=======
This project base on two open api, to get or post data.
* [新浪微博开放平台](http://open.weibo.com/)
* [百度LBS开放平台](http://developer.baidu.com/map/)

Uses open source pull down refresh, and enhance
* [PullToRefresh-ListView](https://github.com/erikwt/PullToRefresh-ListView)

Licence
=======
* [Apache Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)

