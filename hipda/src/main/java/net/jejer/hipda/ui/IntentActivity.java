package net.jejer.hipda.ui;

import android.content.Intent;
import android.os.Bundle;

import net.jejer.hipda.BuildConfig;
import net.jejer.hipda.service.NotiHelper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

/**
 * Created by GreenSkinMonster on 2017-06-22.
 */

public class IntentActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleIntent();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent();
    }

    private void handleIntent() {
        Intent intent = new Intent(this, MainFrameActivity.class);
        Intent srcIntent = getIntent();
        if (srcIntent != null) {
            intent.setAction(srcIntent.getAction());
            intent.putExtras(srcIntent);
            intent.setData(srcIntent.getData());
        }

        //to send a test notification
        boolean finished = false;
        if (BuildConfig.DEBUG) {
            if ("test_sms".equals(intent.getAction())) {
                NotiHelper.sendNotification(this, 0, 1, "绿皮怪兽", "723379", "测试短消息内容");
                finished = true;
                finish();
            } else if ("test_thread".equals(intent.getAction())) {
                NotiHelper.sendNotification(this, 1, 0, "", "", "");
                finished = true;
                finish();
            } else if ("test_all".equals(intent.getAction())) {
                NotiHelper.sendNotification(this, 1, 1, "", "", "");
                finished = true;
                finish();
            }
        }

        if (!finished) {
            boolean clearActivities = !HiApplication.isAppVisible();
            FragmentArgs args = FragmentUtils.parse(intent);
            if (!clearActivities) {
                clearActivities = args != null && args.getType() == FragmentArgs.TYPE_FORUM;
            }
            if (clearActivities) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                ActivityCompat.startActivity(this, intent, null);
            } else {
                if (args != null) {
                    FragmentUtils.show(this, args);
                }
            }
            finish();
        }
    }
}
