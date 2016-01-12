package cn.qqtheme.framework.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import cn.qqtheme.framework.AppConfig;

import android.widget.Toast;
import android.graphics.Color;

import cn.qqtheme.framework.tool.CrashHelper;

import android.os.Build;
import android.app.Activity;

public class CrashActivity extends Activity {
    protected String stackTrace = "";
    protected String deviceInfo = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            setTheme(android.R.style.Theme_Material_Wallpaper);
        } else if (Build.VERSION.SDK_INT >= 11) {
            setTheme(android.R.style.Theme_Holo_Wallpaper);
        } else {
            setTheme(android.R.style.Theme_Wallpaper);
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        stackTrace = CrashHelper.getStackTraceFromIntent(getIntent());
        deviceInfo = CrashHelper.getDeviceInfo();
        View contentView = buildCustomView();
        setContentView(contentView);
    }

    @Override
    public void onBackPressed() {
        showDialog();
    }

    private void showDialog() {
        String[] menus;
        if (AppConfig.DEBUG_ENABLE) {
            menus = new String[]{"重新启动", "退出软件"};
        } else {
            menus = new String[]{"重新启动", "退出软件", "告诉开发者"};
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setItems(menus, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (which == 0) {
                    restartApp();
                } else if (which == 1) {
                    exitApp();
                } else if (which == 2) {
                    sendToQQ();
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private View buildCustomView() {
        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        rootLayout.setFocusable(true);
        rootLayout.setFocusableInTouchMode(true);
        ScrollView scrollView = new ScrollView(this);
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f));
        LinearLayout scrollableView = new LinearLayout(this);
        scrollableView.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(scrollableView);
        TextView traceView = new TextView(this);
        traceView.setPadding(10, 10, 10, 10);
        traceView.append(deviceInfo);
        traceView.append(stackTrace);
        scrollableView.addView(traceView);
        rootLayout.addView(scrollView);
        Button button = new Button(this);
        button.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        button.setBackgroundColor(Color.TRANSPARENT);
        button.setText("我知道了");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });
        rootLayout.addView(button);
        return rootLayout;
    }

    private void restartApp() {
        //必须调用getApplicationContext()才能获得正确的包名
        String packageName = getApplicationContext().getPackageName();
        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void exitApp() {
        finish();
        //不退出的话，如果已进入主界面就奔溃，可能会一直循环弹出奔溃提示
        //ActivityManager.getInstance().exitApp();
    }

    private void sendToQQ() {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("crash_log", stackTrace);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "错误日志已复制，请粘贴发送！", Toast.LENGTH_LONG).show();
        try {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            Uri uri = Uri.parse("mqqwpa://im/chat?chat_type=wpa&uin=" + AppConfig.DEVELOPER_QQ + "&version=1");
            intent.setData(uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //延迟杀掉软件，以便Toast显示完毕
        CountDownTimer timer = new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                exitApp();
            }
        };
        timer.start();
    }

}
