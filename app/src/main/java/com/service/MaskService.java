package com.service;

import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.content.Intent;
import android.widget.Toast;

import com.core.MaskCore;

import com.example.mask.R;  // Replace com.example.mask with your actual package name


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
}
