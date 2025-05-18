package com.example.foodhub;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class NotificationService extends Service {

    private static final String CHANNEL_ID = "foodhub_channel";
    private static final int NOTIFICATION_ID = 1;
    private static final String TAG = "NotificationService";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "NotificationService created");
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String message = intent.getStringExtra("message");
        String recipeId = intent.getStringExtra("recipeId"); // Új: recept ID átvétele
        Log.d(TAG, "NotificationService started with message: " + message);
        sendNotification(message,recipeId);
        stopSelf(); // Stop the service after sending the notification
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "FoodHub Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notifications for FoodHub app");
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setLightColor(android.graphics.Color.BLUE);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void sendNotification(String message, String recipeId) {
        Log.d(TAG, "sendNotification() called with message: " + message + ", recipeId: " + recipeId);
        if (recipeId != null && !recipeId.isEmpty()) {
            // Intent a RecipeDetailsActivity-hez
            Intent intent = new Intent(this, RecipeDetailActivity.class);
            intent.putExtra("recipeId", recipeId); // Recept ID átadása
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.foodhub_not_logo)
                    .setContentTitle("FoodHub Notification")
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent) // PendingIntent beállítása
                    .setAutoCancel(true); // Értesítés eltűnik, ha rákattintanak

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_ID, builder.build());
            Log.d(TAG, "Notification sent: " + message + ", recipeId: " + recipeId);
        } else {
            Log.e(TAG, "No recipeId provided for notification!");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "NotificationService destroyed");
    }
}