# AndroidCrashHelper
Android Crash Helper。安卓APP自定义异常处理器，Release状态下意外奔溃提示更加友好（代替系统默认的那个讨厌的对话框），不影响Debug模式下LogCat的打印。   
```
************************************************************************
**                              _oo0oo_                               **
**                             o8888888o                              **
**                             88" . "88                              **
**                             (| -_- |)                              **
**                             0\  =  /0                              **
**                           ___/'---'\___                            **
**                        .' \\\|     |// '.                          **
**                       / \\\|||  :  |||// \\                        **
**                      / _ ||||| -:- |||||- \\                       **
**                      | |  \\\\  -  /// |   |                       **
**                      | \_|  ''\---/''  |_/ |                       **
**                      \  .-\__  '-'  __/-.  /                       **
**                    ___'. .'  /--.--\  '. .'___                     **
**                 ."" '<  '.___\_<|>_/___.' >'  "".                  **
**                | | : '-  \'.;'\ _ /';.'/ - ' : | |                 **
**                \  \ '_.   \_ __\ /__ _/   .-' /  /                 **
**            ====='-.____'.___ \_____/___.-'____.-'=====             **
**                              '=---='                               **
************************************************************************
```   
# How to use
###下载AndroidCrashHelper，复制其中的“CrashHelper”到新项目作为依赖项：   
```
dependencies {
    compile project(':CrashHelper')
}
```    
### 最简单暴力的使用方法只需在AndroidManifest.xml中加入一句代码：
```xml
<application
   android:name="cn.qqtheme.framework.AppContext"
   ...>
      ...
</application>
```   
下面是更灵活的应用。。。   
### 分别建立一个Application子类及Activity子类，并在AndroidManifest.xml中声明（需要READ_PHONE_STATE权限）：   
```xml
<uses-permission android:name="android.permission.READ_PHONE_STATE" />

<application
   android:name=".DemoApp"
   ...>
      ...
      <activity
          android:name=".MyCrashActivity"
          android:exported="false">
            <intent-filter>
              <action android:name="liyujiang.intent.action.CRASH_ERROR" />
              <category android:name="android.intent.category.DEFAULT" />
            </intent-filter>
      </activity>
      ...
</application>
```   
注：AppContext类负责初始化设置自定义的奔溃报告页，CrashActivity为默认的奔溃报告页。
如果未在Application的onCreate方法里调用“AppContext.initialize()”，自定义异常处理器将不会生效。
如果没有调用“AppContext.initialize()”或CrashHelper.setCrashActivityClass()”设置Activity，
或者没有在AndroidManifest.xml中声明CrashActivity或含有action为“liyujiang.intent.action.CRASH_ERROR”的Activity，
那么将在SD卡根目录下生成crash.log文件（前提是要有WRITE_EXTERNAL_STORAGE权限）。    
法一、可以继承自任何Application及Activity，然后调用AppContext.initialize()初始化：   
```java
public class DemoApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppContext.initialize(this, MyCrashActivity.class);
    }

}
```   
```java
public class MyCrashActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_crash);
        String stackTrace = CrashHelper.getStackTraceFromIntent(getIntent());
        String deviceInfo = CrashHelper.getDeviceInfo();
        ...
    }

}
```   
法二、可以直接继承自AppContext及CrashActivity：   
```java
public class DemoApp extends AppContext {

    @Override
    public void onCreate() {
        super.onCreate();
        AppContext.initialize(this, MyCrashActivity.class);
    }

}
```   
```java
public class MyCrashActivity extends CrashActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_crash);
        TextView tv = findView(R.id.stacktrace);
        tv.setText(stackTrace);
        ...
    }

}
```   

# Screenshots
![效果图](/screenshots/1.png)    
![效果图](/screenshots/2.gif)   
![LogCat](/screenshots/3.jpg)   

# Thanks
此项目核心代码来源于下面这个开源项目，大家去pull来学习吧：   
https://github.com/Ereza/CustomActivityOnCrash   

# Contacts
Tencent QQ: 1032694760   
 