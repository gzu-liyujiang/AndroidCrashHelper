package cn.qqtheme.framework.tool;

import java.util.LinkedList;
import android.app.Activity;

/**
 * Activity管理，以便实现退出功能
 *
 * @author 李玉江[QQ:1032694760]
 * @since 2015/12/17
 * Created By Android Studio
 */
public class ActivityManager {
    //本类的实例
    private static ActivityManager instance;
    //保存所有Activity
    private LinkedList<Activity> activities = new LinkedList<Activity>();

    public static ActivityManager getInstance() {
        if (instance == null) {
            instance = new ActivityManager();
        }
        return instance;
    }

    /**
     * 注册Activity以便集中“finish()”
     */
    public void addActivity(Activity activity) {
        activities.add(activity);
    }

    public void removeActivity(Activity activity) {
        activities.remove(activity);
    }

    /**
     * 所有的Activity
     */
    public LinkedList<Activity> getActivities() {
        return activities;
    }

    /**
     * 退出软件
     */
    public void exitApp() {
        for (Activity activity : activities) {
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);//normal exit application
    }

}
