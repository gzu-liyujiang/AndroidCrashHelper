package cn.qqtheme.crash.demo;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import cn.qqtheme.framework.tool.CrashHelper;

/**
 * 描述
 *
 * @author 李玉江[QQ:1032694760]
 * @since 2015/12/27
 * Created By Android Studio
 */
public class MyCrashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_crash);
        TextView tv = (TextView) findViewById(R.id.crash_log);
        setTitle("软件奔溃啦！");
        String stackTrace = CrashHelper.getStackTraceFromIntent(getIntent());
        String deviceInfo = CrashHelper.getDeviceInfo();
        SpannableString spannableString = new SpannableString(deviceInfo + stackTrace);
        spannableString.setSpan(new ForegroundColorSpan(Color.GREEN), 0, deviceInfo.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(Color.RED), deviceInfo.length() - 1, stackTrace.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv.setText(spannableString);
    }

}
