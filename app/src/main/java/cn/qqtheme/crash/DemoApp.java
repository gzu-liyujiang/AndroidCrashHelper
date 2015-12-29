package cn.qqtheme.crash;

import cn.qqtheme.framework.AppContext;
import android.app.Application;

/**
 * 描述
 *
 * @author 李玉江[QQ:1032694760]
 * @since 2015/12/25
 * Created By Android Studio
 */
public class DemoApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppContext.initialize(this, MyCrashActivity.class);
    }

}
