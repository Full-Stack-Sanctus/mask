package com.service;

import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.content.Intent;
import android.widget.Toast;

import java.io.IOException;

import com.core.MaskCore;

public class MaskService extends VpnService {
    private static final String TAG = "MaskService";
    
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
            } else if ("STOP_VPN".equals(action)) {
                showToast("Stopping VPN..."); // Show message on screen
                stopVpn();
            }
        } else {
            Log.e(TAG, "Received null intent.");
            broadcastError("Received null intent.");
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
                broadcastError(message);
                return;
            }
        } catch (SecurityException e) {
            broadcastError("VPN permissions error: " + e.getMessage());
            return;
        } catch (Exception e) {
            broadcastError("Failed to establish VPN interface: " + e.getMessage());
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
                    broadcastError("Failed to connect to VPN server.");
                }
            } catch (Exception e) {
                broadcastError("Unexpected error in VPN thread: " + e.getMessage());
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
                broadcastStatus("VPN disconnected successfully.");
            }
        } catch (Exception e) {
            broadcastError("Error disconnecting VPN: " + e.getMessage());
        }

        Log.i(TAG, "VPN stopped.");
        showToast("VPN stopped.");
        broadcastStatus("VPN stopped");
    }

    // Helper method to show a toast message
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    // Helper method to broadcast status updates
    private void broadcastStatus(String statusMessage) {
        Intent statusIntent = new Intent("com.mask.VPN_STATUS");
        statusIntent.setPackage(getPackageName());
        statusIntent.putExtra("status_message", statusMessage);
        sendBroadcast(statusIntent);
        Log.i(TAG, "Broadcasting status: " + statusMessage);
    }

    // Helper method to broadcast errors
    private void broadcastError(String errorMessage) {
        Intent errorIntent = new Intent("com.mask.VPN_ERROR");
        errorIntent.setPackage(getPackageName());
        errorIntent.putExtra("error_message", errorMessage);
        sendBroadcast(errorIntent);
        Log.e(TAG, "Broadcasting error: " + errorMessage);
        showToast("Error: " + errorMessage);
    }
}
