package cn.qqtheme.crash.demo;

import android.os.Bundle;

import cn.qqtheme.framework.activity.CrashActivity;

/**
 * 描述
 *
 * @author 李玉江[QQ:1032694760]
 * @since 2015/12/27
 * Created By Android Studio
 */
public class MyCrashActivity extends CrashActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_custom_crash);
        //TextView tv = findView(R.id.stacktrace);
        //tv.setText(stackTrace);
    }

}
