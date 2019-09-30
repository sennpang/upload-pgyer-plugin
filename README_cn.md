[![license](https://img.shields.io/github/license/mashape/apistatus.svg)](http://opensource.org/licenses/MIT)&#8194;&#8194;
[![upload-pgyer](https://img.shields.io/badge/upload--pgyer-1.24-brightgreen.svg)](https://github.com/myroid/jenkins-upload-pgyer-plugin)&#8194;&#8194;
[![downloads](https://img.shields.io/badge/downloads-%3C1K-orange.svg)](https://github.com/myroid/jenkins-upload-pgyer-plugin)&#8194;&#8194;
[![blog](https://img.shields.io/badge/blog-dafan.tech-red.svg)](http://dafan.tech)

### 简单介绍

蒲公英官方网址 [https://www.pgyer.com/](https://www.pgyer.com/)

蒲公英平台可以让开发者和企业将应用上传到网站，生成安装链接和二维码用户在手机上打开安装链接，或扫码二维码，即可开始安装！

因此，这款upload-pgyer的Jenkins插件可以让开发者将apk/ipa文件上传到蒲公英平台！**并且这款插件可以将蒲公英平台返回的应用信息解析后注入到Jenkins的全局变量中，这样你就可以很方便的在其他构建步骤中使用这些返回的信息**，你可以在Jenkins的job配置页面的`构建`和`构建后操作`这两个操作中点击添加构建步骤选择`upload to pgyer with apiVx`。然后你就可以看到类似下面图片的操作界面：

### 界面截图
![](https://blog-1251473749.cos.ap-beijing.myqcloud.com/jenkins_upload_pgyer/upload-pgyer-1.png)

### 参数介绍
需要填写的字段|字段的解释
----:|:----------
pgyer uKey|`(APIV1必填，APIV2无此参数)` 用户Key，用来标识当前用户的身份，<br/>对于同一个蒲公英的注册用户来说，这个值在固定的。<br/>[点击获取_ukey](https://www.pgyer.com/account/api)
pgyer api_key|(必填) API Key，用来识别API调用者的身份，<br/>如不特别说明，每个接口中都需要含有此参数。<br/>对于同一个蒲公英的注册用户来说，这个值在固定的。<br/>[点击获取_api_key](https://www.pgyer.com/account/api)
scandir|`(必填)` 需要上传的apk/ipa文件所在的文件夹或者父文件夹，<br/>当前默认路径是`${WORKSPACE}`，它代表了当前项目的绝对路径。<br/>这个功能的实现使用了ant框架的DirectoryScanner类，[点击查看DirectoryScanner类](https://ant.apache.org/manual/api/org/apache/tools/ant/DirectoryScanner.html)，<br/>这个字段就是DirectoryScanner类中的basedir方法的参数[点击查看basedir方法](https://ant.apache.org/manual/api/org/apache/tools/ant/DirectoryScanner.html#basedir)
file wildcard|`(必填)` 需要上传的apk/ipa文件的名字，支持通配符，<br/>就像这样: **/Test?/*.apk，<br/>这个功能的实现使用了ant框架的DirectoryScanner类，[点击查看DirectoryScanner类](https://ant.apache.org/manual/api/org/apache/tools/ant/DirectoryScanner.html)，<br/>这个字段就是DirectoryScanner类中的includes方法的参数，[点击查看includes方法](https://ant.apache.org/manual/api/org/apache/tools/ant/DirectoryScanner.html#includes)
installType|`(选填)` 应用安装方式，值为(1,2,3)。<br/>1：公开，2：密码安装，3：邀请安装。<br/>默认为1公开
password|`(选填)` 设置App安装密码，如果不想设置密码，请传空字符串，或不传。
updateDescription|`(选填)` 版本更新描述，请传空字符串，或不传。
qrcodePath|`(选填)` 如果你需要下载蒲公英返回的二维码，那么这里填写二维码的存储路径，<br/>如果你不需要下载，那么你不需要在这里填写任何内容。
envVarsPath |`(选填)` 如果你想存储蒲公英返回的上传信息，那么这里填写保存信息的文件路径，<br/>如果你不需要保存，那么你不需要在这里填写任何内容。

### 运行截图
![](https://blog-1251473749.cos.ap-beijing.myqcloud.com/jenkins_upload_pgyer/jenkins_log.png)

当你的应用上传成功后，在Jenkins中你就能看到上面图片中的信息。同时，你就可以在其他构建步骤中使用蒲公英返回来的信息，例如我的经验：

![](https://blog-1251473749.cos.ap-beijing.myqcloud.com/jenkins_upload_pgyer/upload-pgyer-3.png)

### 蒲公英APIV1 返回字段说明
可以使用的环境变量|作用或解释
----:|:----------
appKey|App Key
appType|应用类型（1:iOS; 2:Android）
appIsLastest|是否是最新版（1:是; 2:否）
appFileSize|App 文件大小
appName| 应用名称
appVersion|版本号
appVersionNo|适用于Android的版本编号，iOS始终为0
appBuildVersion|蒲公英生成的用于区分历史版本的build号
appIdentifier|应用程序包名，iOS为BundleId，Android为包名
appIcon|应用的Icon图标key，访问地址为 http://o1wh05aeh.qnssl.com/image/view/app_icons/[appIcon]
appDescription|应用介绍
appUpdateDescription|应用更新说明
appScreenshots|应用截图的key，获取地址为 http://o1whyeemo.qnssl.com/image/view/app_screenshots/[appScreenshots]
appShortcutUrl|应用短链接
appCreated|应用上传时间
appUpdated|应用更新时间
appQRCodeURL|应用二维码地址
appPgyerURL|应用主页地址
appBuildURL|本次上传的应用主页

### 蒲公英APIV2 返回字段说明
可以使用的环境变量|作用或解释
----:|:----------
buildKey | Build Key是唯一标识应用的索引ID
buildType | 应用类型（1:iOS; 2:Android）
buildIsFirst | 是否是第一个App（1:是; 2:否）
buildIsLastest | 是否是最新版（1:是; 2:否）
buildFileSize | App 文件大小
buildName | 应用名称
buildVersion | 版本号, 默认为1.0 (是应用向用户宣传时候用到的标识，例如：1.1、8.2.1等。)
buildVersionNo |上传包的版本编号，默认为1 (即编译的版本号，一般来说，编译一次会变动一次这个版本号, 在 Android 上叫 Version Code。对于 iOS 来说，是字符串类型；对于 Android 来说是一个整数。例如：1001，28等。)
buildBuildVersion | 蒲公英生成的用于区分历史版本的build号
buildIdentifier | 应用程序包名，iOS为BundleId，Android为包名
buildIcon | 应用的Icon图标key，访问地址为 https://www.pgyer.com/image/view/app_icons/[应用的Icon图标key]
buildDescription | 应用介绍
buildUpdateDescription | 应用更新说明
buildScreenShots | 应用截图的key，获取地址为 https://www.pgyer.com/image/view/app_screenshots/[应用截图的key]
buildShortcutUrl | 应用短链接
buildCreated | 应用上传时间
buildUpdated | 应用更新时间
buildQRCodeURL |应用二维码地址
appPgyerURL| 应用主页地址
appBuildURL| 本次上传的应用主页

### Change Log
版本 1.33(2019-09-07)

- 修复[SECURITY-1044](https://issues.jenkins-ci.org/browse/SECURITY-1044)
- 升级Gson 2.8.5

版本 1.31(2018-05-07)

- 升级Gson 2.8.4
- 优化上传日志
- 更换readme.md图片地址

版本 1.30(2018-04-16）

- 移除Jsoup，并引入okhttp作为网络库
- 增加文件上传进度
- 增加蒲公英[APIV2](https://www.pgyer.com/doc/view/api#uploadApp)