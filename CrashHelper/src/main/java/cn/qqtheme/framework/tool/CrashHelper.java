package cn.qqtheme.framework.tool;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import cn.qqtheme.framework.AppConfig;
import cn.qqtheme.framework.activity.CrashActivity;

import android.view.WindowManager;
import android.util.DisplayMetrics;

import java.util.Calendar;

/**
 * UncaughtException处理类,当程序发生Uncaught异常的时候,由该类来接管程序,并记录发送错误报告.
 *
 * @author 李玉江[QQ:1023694760]
 * @version 2014-10-05，20151227
 * @link https://github.com/Ereza/CustomActivityOnCrash
 * @see MyUncaughtExceptionHandler
 * @see MyActivityLifecycleCallbacks
 * @see CrashActivity
 */
public final class CrashHelper {
    private static final String INTENT_EXTRA_STACK_TRACE = "liyujiang.intent.EXTRA_STACK_TRACE";
    private static final String INTENT_EXTRA_URL = "liyujiang.intent.EXTRA_URL";
    private static final String INTENT_ACTION_CRASH_ACTIVITY = "liyujiang.intent.action.CRASH_ERROR";
    private static final String INTENT_ACTION_BROWSER_ACTIVITY = "liyujiang.intent.action.WEB_BROWSER";

    //General constants
    private final static String TAG = AppConfig.DEBUG_TAG + "-" + CrashHelper.class.getSimpleName();
    private static final int MAX_STACK_TRACE_SIZE = 131071; //128 KB - 1

    //Internal variables
    private static Application application;
    private static WeakReference<Activity> lastActivityCreated = new WeakReference<>(null);

    private static Class<? extends Activity> crashActivityClass = null;
    private static Class<? extends Activity> browserActivityClass = null;
    private static boolean isInBackground = false;

    public static void install(Context context) {
        install(context, null);
    }

    /**
     * Installs this crash tool on the application using the default error activity.
     *
     * @param context Application to use for obtaining the ApplicationContext. Must not be null.
     * @see Application
     */
    public static void install(Context context, Class<? extends Activity> clazz) {
        try {
            if (context == null) {
                Log.e(TAG, "Install failed: context is null!");
                return;
            }
            application = (Application) context.getApplicationContext();
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                Log.w(TAG, "Crash tool will be installed, but may not be reliable in API lower than 14");
            }

            //INSTALL!
            Thread.UncaughtExceptionHandler oldHandler = Thread.getDefaultUncaughtExceptionHandler();

            String pkgName = application.getPackageName();
            Log.d(TAG, "current application package name is " + pkgName);
            if (oldHandler != null && oldHandler.getClass().getName().startsWith(pkgName)) {
                Log.e(TAG, "You have already installed crash tool, doing nothing!");
                return;
            }
            if (oldHandler != null && !oldHandler.getClass().getName().startsWith("com.android.internal.os")) {
                Log.e(TAG, "IMPORTANT WARNING! You already have an UncaughtExceptionHandler, are you sure this is correct? If you use ACRA, Crashlytics or similar libraries, you must initialize them AFTER this crash tool! Installing anyway, but your original handler will not be called.");
            }

            //We define a default exception handler that does what we want so it can be called from Crashlytics/ACRA
            Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtExceptionHandler());
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                application.registerActivityLifecycleCallbacks(new MyActivityLifecycleCallbacks());
            }

            Log.i(TAG, "Crash tool has been installed.");
        } catch (Throwable t) {
            Log.e(TAG, "An unknown error occurred while installing crash tool, it may not have been properly initialized. Please report this as a bug if needed.", t);
        }

        if (clazz != null) {
            setCrashActivityClass(clazz);
        }
    }

    public static void startWebBrowser(String url) {
        if (browserActivityClass == null) {
            browserActivityClass = guessBrowserActivityClass();
        }
        if (browserActivityClass == null) {
            throw new RuntimeException("Your browser activity not available, must declare in AndroidManifest.xml use intent-filter action: " + INTENT_ACTION_BROWSER_ACTIVITY);
        }
        Intent intent = new Intent(application, browserActivityClass);
        intent.putExtra(INTENT_EXTRA_URL, url);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        application.startActivity(intent);
    }

    /**
     * 是否在后台运行
     */
    public static boolean isInBackground() {
        return isInBackground;
    }

    /**
     * Sets the error activity class to launch when a crash occurs.
     * If null,the default error activity will be used.
     */
    public static void setCrashActivityClass(Class<? extends Activity> crashClass) {
        crashActivityClass = crashClass;
    }

    public static void setBrowserActivityClass(Class<? extends Activity> browserClass) {
        browserActivityClass = browserClass;
    }

    /**
     * Given an Intent, returns the stack trace extra from it.
     *
     * @param intent The Intent. Must not be null.
     * @return The stacktrace, or null if not provided.
     */
    public static String getStackTraceFromIntent(Intent intent) {
        return intent.getStringExtra(INTENT_EXTRA_STACK_TRACE);
    }

    public static String getUrlFromIntent(Intent intent) {
        return intent.getStringExtra(INTENT_EXTRA_URL);
    }

    /**
     * 获取崩溃异常日志
     */
    public static String getDeviceInfo() {
        StringBuilder builder = new StringBuilder();
        PackageInfo pi = getPackageInfo();
        String dateTime = DateFormat.getDateTimeInstance().format(Calendar.getInstance(Locale.CHINA).getTime());
        String appName = pi.applicationInfo.loadLabel(application.getPackageManager()).toString();
        int[] pixels = getPixels();
        String cpu;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cpu = Arrays.deepToString(Build.SUPPORTED_ABIS);
        } else {
            //noinspection deprecation
            cpu = Build.CPU_ABI;
        }
        builder.append("Date Time: ").append(dateTime).append("\n");
        builder.append("App Version: ").append(appName).append(" v").append(pi.versionName).append("(").append(pi.versionCode).append(")\n");
        builder.append("Android OS: ").append(Build.VERSION.RELEASE).append("(").append(cpu).append(")\n");
        builder.append("Phone Model: ").append(getDeviceModelName()).append("\n");
        builder.append("Screen Pixel: ").append(pixels[0]).append("x").append(pixels[1]).append(",").append(pixels[2]).append("\n\n");
        return builder.toString();
    }

    /**
     * 获取App安装包信息
     */
    private static PackageInfo getPackageInfo() {
        PackageInfo info = null;
        try {
            info = application.getPackageManager().getPackageInfo(application.getPackageName(), 0);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        if (info == null) {
            info = new PackageInfo();
        }
        return info;
    }

    /**
     * 获取屏幕宽高像素
     */
    private static int[] getPixels() {
        int[] pixels = new int[3];
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager windowMgr = (WindowManager) application.getSystemService(Context.WINDOW_SERVICE);
        windowMgr.getDefaultDisplay().getMetrics(dm);
        pixels[0] = dm.widthPixels;
        pixels[1] = dm.heightPixels;
        pixels[2] = dm.densityDpi;
        return pixels;// e.g. 1080,1920,480
    }

    /**
     * INTERNAL method that returns the device model name with correct capitalization.
     * Taken from: http://stackoverflow.com/a/12707479/1254846
     *
     * @return The device model name (i.e., "LGE Nexus 5")
     */
    private static String getDeviceModelName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    /**
     * INTERNAL method that capitalizes the first character of a string
     *
     * @param s The string to capitalize
     * @return The capitalized string
     */
    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    /**
     * @since 20151227
     * 判断某个Activity是否已在AndroidManifest.xml里声明
     */
    private static boolean activityAvailable(Class<? extends Activity> clazz) {
        Intent intent = new Intent();
        intent.setClass(application, clazz);
        List<ResolveInfo> list = application.getPackageManager().queryIntentActivities(intent, 0);
        return list.size() != 0;
    }

    /**
     * @see #INTENT_ACTION_CRASH_ACTIVITY
     * @see #INTENT_ACTION_BROWSER_ACTIVITY
     * 获取某个意图相关的Activity，未知在AndroidManifest.xml里声明的话将获取不到
     * @since 20151227
     */
    private static Class<? extends Activity> obtainActivityByIntentAction(String action) {
        List<ResolveInfo> resolveInfos = application.getPackageManager().queryIntentActivities(
                new Intent().setAction(action), PackageManager.GET_RESOLVED_FILTER);
        if (resolveInfos != null && resolveInfos.size() > 0) {
            ResolveInfo resolveInfo = resolveInfos.get(0);
            try {
                //noinspection unchecked
                return (Class<? extends Activity>) Class.forName(resolveInfo.activityInfo.name);
            } catch (ClassNotFoundException e) {
                //Should not happen, print it to the log!
                Log.e(TAG, "Failed when resolving the error activity class via intent filter, stack trace follows!", e);
            }
        }
        return null;
    }

    /**
     * INTERNAL method that checks if the stack trace that just crashed is conflictive. This is true in the following scenarios:
     * - The application has crashed while initializing (handleBindApplication is in the stack)
     * - The error activity has crashed (activityClass is in the stack)
     *
     * @param throwable     The throwable from which the stack trace will be checked
     * @param activityClass The activity class to launch when the app crashes
     * @return true if this stack trace is conflict and the activity must not be launched, false otherwise
     */
    private static boolean isStackTraceLikelyConflict(Throwable throwable, Class<? extends Activity> activityClass) {
        do {
            StackTraceElement[] stackTrace = throwable.getStackTrace();
            for (StackTraceElement element : stackTrace) {
                if ((element.getClassName().equals("android.app.ActivityThread") && element.getMethodName().equals("handleBindApplication")) || element.getClassName().equals(activityClass.getName())) {
                    return true;
                }
            }
        } while ((throwable = throwable.getCause()) != null);
        return false;
    }

    /**
     * INTERNAL method used to guess which error activity must be called when the app crashes.
     * It will first get activities from the AndroidManifest with intent filter <action android:name="cat.ereza.customactivityoncrash.ERROR" />,
     * if it cannot find them, then it will use the default error activity.
     *
     * @return The guessed error activity class, or the default error activity if not found
     */
    private static Class<? extends Activity> guessCrashActivityClass() {
        Class<? extends Activity> resolvedActivityClass;

        //If action is defined, use that
        resolvedActivityClass = obtainActivityByIntentAction(INTENT_ACTION_CRASH_ACTIVITY);

        //Else, get the default activity
        if (resolvedActivityClass == null && activityAvailable(CrashActivity.class)) {
            resolvedActivityClass = CrashActivity.class;
        }

        return resolvedActivityClass;
    }

    private static Class<? extends Activity> guessBrowserActivityClass() {
        Class<? extends Activity> resolvedActivityClass;

        //If action is defined, use that
        resolvedActivityClass = obtainActivityByIntentAction(INTENT_ACTION_BROWSER_ACTIVITY);

        //Else, get the default activity
        /*if (resolvedActivityClass == null && activityAvailable(BrowserActivity.class)) {
            resolvedActivityClass = BrowserActivity.class;
        }*/

        return resolvedActivityClass;
    }

    /**
     * INTERNAL method that kills the current process.
     * It is used after restarting or killing the app.
     */
    private static void killCurrentProcess() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }

    private static class MyUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

        @Override
        public void uncaughtException(Thread thread, final Throwable throwable) {
            Log.e(TAG, "App has crashed, executing UncaughtExceptionHandler", throwable);
            final String stackTraceString = toStackTraceString(throwable);

            if (crashActivityClass == null) {
                crashActivityClass = guessCrashActivityClass();
            }

            if (crashActivityClass == null) {
                Log.e(TAG, "Your crash activity not available, must declare in AndroidManifest.xml use intent-filter action: " + INTENT_ACTION_CRASH_ACTIVITY);

                new Thread() {
                    @Override
                    public void run() {
                        try {
                            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "crash.log");
                            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                                    new FileOutputStream(file)));
                            writer.write(getDeviceInfo() + "\n\n" + stackTraceString);
                            writer.close();
                            Log.i(TAG, "Save stack trace: " + file.getAbsolutePath());
                        } catch (Exception e) {
                            Log.e(TAG, "Save stack trace failed", e);
                        }
                    }
                }.start();
            } else {
                if (isStackTraceLikelyConflict(throwable, crashActivityClass)) {
                    Log.e(TAG, "Your application class or your crash activity have crashed, the custom activity will not be launched!");
                } else {
                    Intent intent = new Intent(application, crashActivityClass);
                    intent.putExtra(INTENT_EXTRA_STACK_TRACE, stackTraceString);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    application.startActivity(intent);
                }
            }
            Activity lastActivity = lastActivityCreated.get();
            if (lastActivity != null) {
                Log.i(TAG, "Last activity: " + lastActivity.getClass().getSimpleName());
                //We finish the activity, this solves a bug which causes infinite recursion.
                //This is unsolvable in API<14, so beware!
                //See: https://github.com/ACRA/acra/issues/42
                lastActivity.finish();
                lastActivityCreated.clear();
            }
            killCurrentProcess();
        }

        private String toStackTraceString(Throwable throwable) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            String stackTraceString = sw.toString();

            //Reduce data to 128KB so we don't get a TransactionTooLargeException when sending the intent.
            //The limit is 1MB on Android but some devices seem to have it lower.
            //See: http://developer.android.com/reference/android/os/TransactionTooLargeException.html
            //And: http://stackoverflow.com/questions/11451393/what-to-do-on-transactiontoolargeexception#comment46697371_12809171
            if (stackTraceString.length() > MAX_STACK_TRACE_SIZE) {
                String disclaimer = " [stack trace too large]";
                stackTraceString = stackTraceString.substring(0, MAX_STACK_TRACE_SIZE - disclaimer.length()) + disclaimer;
            }
            return stackTraceString;
        }

    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private static class MyActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
        int currentlyStartedActivities = 0;

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            if (activity.getClass() != crashActivityClass) {
                // Copied from ACRA:
                // Ignore activityClass because we want the last
                // application Activity that was started so that we can
                // explicitly kill it off.
                lastActivityCreated = new WeakReference<>(activity);
            }
        }

        @Override
        public void onActivityStarted(Activity activity) {
            currentlyStartedActivities++;
            isInBackground = (currentlyStartedActivities == 0);
            //Do nothing
        }

        @Override
        public void onActivityResumed(Activity activity) {
            //Do nothing
        }

        @Override
        public void onActivityPaused(Activity activity) {
            //Do nothing
        }

        @Override
        public void onActivityStopped(Activity activity) {
            //Do nothing
            currentlyStartedActivities--;
            isInBackground = (currentlyStartedActivities == 0);
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            //Do nothing
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            //Do nothing
        }

    }

}
