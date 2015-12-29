package cn.qqtheme.framework;

import android.app.Application;
import android.content.Context;

import cn.qqtheme.framework.tool.CrashHelper;

import android.app.Activity;

/**
 * ************************************************************************
 * **                              _oo0oo_                               **
 * **                             o8888888o                              **
 * **                             88" . "88                              **
 * **                             (| -_- |)                              **
 * **                             0\  =  /0                              **
 * **                           ___/'---'\___                            **
 * **                        .' \\\|     |// '.                          **
 * **                       / \\\|||  :  |||// \\                        **
 * **                      / _ ||||| -:- |||||- \\                       **
 * **                      | |  \\\\  -  /// |   |                       **
 * **                      | \_|  ''\---/''  |_/ |                       **
 * **                      \  .-\__  '-'  __/-.  /                       **
 * **                    ___'. .'  /--.--\  '. .'___                     **
 * **                 ."" '<  '.___\_<|>_/___.' >'  "".                  **
 * **                | | : '-  \'.;'\ _ /';.'/ - ' : | |                 **
 * **                \  \ '_.   \_ __\ /__ _/   .-' /  /                 **
 * **            ====='-.____'.___ \_____/___.-'____.-'=====             **
 * **                              '=---='                               **
 * ************************************************************************
 * **                        佛祖保佑      镇类之宝                         **
 * ************************************************************************
 *
 * @author 李玉江[QQ:1023694760]
 * @version 2014-3-18
 */
public class AppContext extends Application {
    /**
     * Global application context.
     */
    private static Context applicationContext;


    /**
     * Construct, initialize application context.
     */
    public AppContext() {
        applicationContext = this;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        initialize(this);
    }

    public static void initialize(Context context) {
        initialize(context, null);
    }

    /**
     * Initialize to make framework ready to work. If you didn't configure
     * {@link AppContext}
     * in the AndroidManifest.xml, make sure you call this method as soon as
     * possible. In
     * Application's onCreate() method will be fine.
     *
     * @param context Application context.
     */
    public static void initialize(Context context, Class<? extends Activity> clazz) {
        if (context instanceof Application) {
            applicationContext = context;
        } else {
            applicationContext = context.getApplicationContext();
        }
        CrashHelper.install(applicationContext, clazz);
    }


    /**
     * Get the global application context.
     *
     * @return Application context.
     * @see #initialize(Context)
     */
    public static Context getGlobalContext() {
        if (applicationContext == null) {
            throw new RuntimeException("Application context is null");
        }
        return applicationContext;
    }
}
