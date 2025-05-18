package com.example.foodhub;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;
import java.util.Date;
public class DailyNotificationJobService extends JobService {
    private static final String TAG = "DailyNotificationJob";
    private static final String CHANNEL_ID = "daily_notification_channel";
    private static final int NOTIFICATION_ID = 2;

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "DailyNotificationJobService started");
        countNewRecipesAndSendNotification();
        return false; // false, mert a feladat nem igényel aszinkron műveletet
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "DailyNotificationJobService stopped");
        return true; // true, hogy újrapróbálkozzon, ha a feladat megszakadt
    }

    private void countNewRecipesAndSendNotification() {
        // Számoljuk meg az új recepteket az előző nap dél óta
        Date yesterdayNoon = getYesterdaysNoon();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("recipes")
                .whereGreaterThanOrEqualTo("createdAt", yesterdayNoon)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        int recipeCount = querySnapshot.size();
                        String message = "Tegnap óta " + recipeCount + " receptet töltöttek fel!";
                        createNotificationChannel();
                        sendNotification(message);
                    } else {
                        Log.e(TAG, "Hiba a receptek számolásakor: ", task.getException());
                        createNotificationChannel();
                        sendNotification("Hiba a receptek számolásakor!");
                    }
                });
    }

    private Date getYesterdaysNoon() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1); // Előző nap
        calendar.set(Calendar.HOUR_OF_DAY, 12); // Dél
        calendar.set(Calendar.MINUTE, 5);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Daily Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Daily notifications for FoodHub app");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendNotification(String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.foodhub_not_logo)
                .setContentTitle("FoodHub Napi Értesítés")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
        Log.d(TAG, "Notification sent: " + message);
    }
}
