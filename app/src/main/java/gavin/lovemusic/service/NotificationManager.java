package gavin.lovemusic.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.NotificationTarget;

import gavin.lovemusic.constant.R;
import gavin.lovemusic.entity.Music;
import gavin.lovemusic.mainview.MainActivity;

import static android.os.Build.VERSION.SDK_INT;

/**
 * Created by GavinLi
 * on 4/15/17.
 */

public class NotificationManager {
    private static final int NOTIFICATION_ID = 1;

    public static final String NOTIFICATION_PLAY = "gavin.notification.play";
    public static final String NOTIFICATION_NEXT = "gavin.notification.next";
    public static final String NOTIFICATION_STOP = "gavin.notification.stop";

    private final PlayService mService;

    private Notification notification;
    private RemoteViews contentView;

    public NotificationManager(PlayService service) {
        mService = service;

        IntentFilter intentFilterPlay = new IntentFilter(NotificationManager.NOTIFICATION_PLAY);
        mService.registerReceiver(broadcastReceiver, intentFilterPlay);
        IntentFilter intentFilterNext = new IntentFilter(NotificationManager.NOTIFICATION_NEXT);
        mService.registerReceiver(broadcastReceiver, intentFilterNext);
        IntentFilter intentFilterStop = new IntentFilter(NotificationManager.NOTIFICATION_STOP);
        mService.registerReceiver(broadcastReceiver, intentFilterStop);
    }

    public void showNotification(Music currentMusic) {
        Intent startActivity = new Intent(mService, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        Notification.Builder builder = new Notification.Builder(mService)
                .setContentIntent(PendingIntent
                        .getActivity(mService, 0, startActivity, 0));
        if(SDK_INT >= 19) {
            builder.setSmallIcon(R.drawable.ic_launcher_alpha);
        } else {
            builder.setSmallIcon(R.mipmap.ic_launcher);
        }
        notification = builder.build();

        buildRemoteView(currentMusic);

        mService.startForeground(NOTIFICATION_ID, notification);
    }

    private void buildRemoteView(Music currentMusic) {

        contentView = new RemoteViews
                (mService.getPackageName(), R.layout.notification_small);
        notification.contentView = contentView;
        notification.flags = Notification.FLAG_ONGOING_EVENT;

        PendingIntent pIntentPlay = PendingIntent.getBroadcast(
                mService, 0, new Intent(NOTIFICATION_PLAY), 0);
        contentView.setOnClickPendingIntent(R.id.playButton, pIntentPlay);

        PendingIntent pIntentNext = PendingIntent.getBroadcast(
                mService, 0, new Intent(NOTIFICATION_NEXT), 0);
        contentView.setOnClickPendingIntent(R.id.nextButton, pIntentNext);

        PendingIntent pIntentClose = PendingIntent.getBroadcast(
                mService, 0, new Intent(NOTIFICATION_STOP), 0);
        contentView.setOnClickPendingIntent(R.id.closeService, pIntentClose);

        if (PlayService.musicState == PlayService.PLAYING) {
            contentView.setImageViewResource(R.id.playButton, R.drawable.pause_grey);
        } else {
            contentView.setImageViewResource(R.id.playButton, R.drawable.play_grey);
        }

        contentView.setTextViewText(R.id.musicName, currentMusic.getTitle());
        contentView.setTextViewText(R.id.artist, currentMusic.getArtist());
        NotificationTarget target = new NotificationTarget(
                mService, contentView, R.id.musicAlbum, notification, NOTIFICATION_ID
        );
        if(currentMusic.getImage() != null && !currentMusic.getImage().isEmpty()) {
            Glide.with(mService)
                    .load(currentMusic.getImage())
                    .asBitmap()
                    .into(target);
        } else {
            Glide.with(mService)
                    .load(R.drawable.defalut_album)
                    .asBitmap()
                    .into(target);
        }
    }

    public void showPause() {
        contentView.setImageViewResource
                (R.id.playButton, R.drawable.play_grey);
        mService.startForeground(NOTIFICATION_ID, notification);
    }

    public void showResume() {
        contentView.setImageViewResource(R.id.playButton, R.drawable.pause_grey);
        mService.startForeground(NOTIFICATION_ID, notification);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case NotificationManager.NOTIFICATION_PLAY:
                    if (PlayService.musicState == PlayService.PLAYING) {
                        mService.pauseMusic();
                    } else {
                        mService.resumeMusic();
                    }
                    break;
                case NotificationManager.NOTIFICATION_NEXT:
                    mService.nextMusic();
                    break;
                case NotificationManager.NOTIFICATION_STOP:
                    mService.pauseMusic();
                    mService.stopForeground(true);
            }
        }
    };

    public void release() {
        mService.unregisterReceiver(broadcastReceiver);
    }
}
