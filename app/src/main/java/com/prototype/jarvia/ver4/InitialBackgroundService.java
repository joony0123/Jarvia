package com.prototype.jarvia.ver4;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class InitialBackgroundService extends Service {


    public InitialBackgroundService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationCompat.Builder mBuilder =
               (NotificationCompat.Builder) new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.bot)
                       .setContentTitle("Anonymous-Bot")
                        .setContentText("Activated")
                       .setPriority(Notification.PRIORITY_MAX);

//        Intent showTaskIntent = new Intent(getApplicationContext(), MainActivity.class);
//        showTaskIntent.setAction(Intent.ACTION_MAIN);
//        showTaskIntent.addCategory(Intent.CATEGORY_LAUNCHER);
//        showTaskIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PackageManager packageManager = getApplicationContext().getPackageManager();
        Intent Lintent = packageManager.getLaunchIntentForPackage(getPackageName());
            Lintent.setPackage(null);
            Lintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(Lintent);

        PendingIntent contentIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                Lintent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(contentIntent);

        startForeground(1337, mBuilder.build());
        Intent googleAccntChooser = new Intent(getApplicationContext(), GoogleAccntChooser.class);
        googleAccntChooser.addFlags(FLAG_ACTIVITY_NEW_TASK);
        startActivity(googleAccntChooser);
        // To prevent restarting
//        if(intent != null)
//            startActivity(jarvis);
//        else
//            onDestroy();
        return START_NOT_STICKY;

    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        //Rethink about not killing it. Let user kill it? or only kill it when user says "Terminate"

       // ProcessPhoenix.triggerRebirth(getApplicationContext());

        stopForeground(true);
        stopSelf();
        /*     stopService(new Intent(this, this.getClass()));
        startService(new Intent(this, YourService.class));
        Intent restartService = new Intent(getApplicationContext(), this.getClass());
        restartService.setPackage(getPackageName());

        startService(restartService);*/


        // NOT do b/c it will not restart when swiped away
        //stopSelf();
        //super.onTaskRemoved(rootIntent);

    }


}
