package com.radiumone.cordova.plugin;

import com.radiumone.emitter.notification.R1PushNotificationManager;
import com.radiumone.emitter.push.R1Push;
import com.radiumone.emitter.richpush.R1RichPush;
import com.radiumone.emitter.richpush.activity.R1RichPushActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;

public class CordovaPushReceiver extends BroadcastReceiver {

    public static boolean openNotification;
    public static boolean onBackground;

    public void onReceive(Context context, Intent intent) {
        try{
            Context applicationContext = context.getApplicationContext();
            if ( intent != null ){
                if (intent.getAction().equals(R1Push.OPENED_NOTIFICATION)) {
                    Intent openIntent = null;

                    Bundle extras = intent.getExtras();
                    
                    if (extras != null && onBackground) {
                        openNotification = true;
                        // getting rich push ID - no rich push id means that push is simple and no rich content for it
                        String richPushId = extras.getString(R1PushNotificationManager.RICH_PUSH);
                        if (TextUtils.isEmpty(richPushId)) {
                            openIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
                            if ( openIntent == null ){
                                return;
                            }
                        } else {
                            openIntent = new Intent(applicationContext, R1RichPushActivity.class);
                            openIntent.putExtra(R1RichPush.R1_RICH_PUSH_ID, richPushId);
                        }
                        if ( openIntent.resolveActivity(context.getPackageManager()) != null ){
                            openIntent.putExtras(extras);
                            openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                            // TaskStackBuilder from android-support-v4 library
                            final TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(applicationContext);
                            if (!TextUtils.isEmpty(richPushId)) {
                                taskStackBuilder.addParentStack(R1RichPushActivity.class);
                            }

                            taskStackBuilder.addNextIntent(openIntent);
                            taskStackBuilder.startActivities(extras);
                        }
                    }
                }
            }
        } catch (Exception ex){

        }
    }
}