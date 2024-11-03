package com.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.VpnService;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.core.MaskCore;
import com.mask.MainActivity; // Update this with your actual MainActivity class path

public class MaskService extends VpnService {
    private static final String TAG = "MaskService";
    private static final String CHANNEL_ID = "VPNChannel";
    private static final int NOTIFICATION_ID = 1;
    
    private ParcelFileDescriptor vpnInterface = null;
    private Thread vpnThread = null;
    private MaskCore maskCore;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if ("START_VPN".equals(action)) {
                showToast("Starting VPN..."); // Show message on screen
                startVpn();
                showNotification("VPN Active", "Your VPN connection is active.");
            } else if ("STOP_VPN".equals(action)) {
                showToast("Stopping VPN..."); // Show message on screen
                stopVpn();
                stopForeground(true); // Stop the notification when VPN is stopped
            }
        } else {
            Log.e(TAG, "Received null intent.");
            showToast("Received null intent.");
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopVpn();
        super.onDestroy();
    }

    private void startVpn() {
        Log.i(TAG, "Attempting to start VPN...");
        VpnService.Builder builder = new VpnService.Builder();
        
        builder.setSession("USA VPN")
               .addAddress("10.0.0.2", 24)
               .addRoute("0.0.0.0", 0)
               .addDnsServer("8.8.8.8")
               .setMtu(1500);

        try {
            vpnInterface = builder.establish();
            if (vpnInterface == null) {
                String message = "VPN interface is null. Cannot start VPN.";
                Log.e(TAG, message);
                showToast(message);
                return;
            }
        } catch (SecurityException e) {
            showToast("VPN permissions error: " + e.getMessage());
            return;
        } catch (Exception e) {
            showToast("Failed to establish VPN interface: " + e.getMessage());
            return;
        }

        maskCore = new MaskCore(vpnInterface);

        vpnThread = new Thread(() -> {
            try {
                if (maskCore.connectToServer()) {
                    Log.i(TAG, "VPN started successfully.");
                    showToast("VPN started successfully.");
                    maskCore.handleTraffic();
                } else {
                    showToast("Failed to connect to VPN server.");
                }
            } catch (Exception e) {
                showToast("Unexpected error in VPN thread: " + e.getMessage());
            }
        });
        vpnThread.start();
    }

    private void stopVpn() {
        Log.i(TAG, "Attempting to stop VPN...");

        if (vpnThread != null && vpnThread.isAlive()) {
            vpnThread.interrupt();
            vpnThread = null;
        }

        try {
            if (maskCore != null) {
                maskCore.disconnect();
                showToast("VPN disconnected successfully.");
            }
        } catch (Exception e) {
            showToast("Error disconnecting VPN: " + e.getMessage());
        }

        Log.i(TAG, "VPN stopped.");
        showToast("VPN stopped.");
    }

    // Helper method to show a toast message
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    // Method to show a persistent notification when VPN is active
    private void showNotification(String title, String message) {
        createNotificationChannel();

        Intent notificationIntent = new Intent(this, MainActivity.class); // Update MainActivity with the correct path
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.mipmap.ic_launcher) // Use your app icon
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();

        startForeground(NOTIFICATION_ID, notification);
    }

    // Method to create the notification channel for Android 8.0 and above
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "VPN Connection", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Shows VPN status");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
