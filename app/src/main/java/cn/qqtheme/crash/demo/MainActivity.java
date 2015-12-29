package cn.qqtheme.crash.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

/**
 * 描述
 *
 * @author 李玉江[QQ:1032694760]
 * @since 2015/12/29
 * Created By Android Studio
 */
public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    public void onCrashException(View view) {
        throw new RuntimeException("这是一个未在APP里捕获处理的异常");
    }

}
