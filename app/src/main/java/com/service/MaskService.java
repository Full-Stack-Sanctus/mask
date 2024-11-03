package service;

import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.content.Intent;
import android.widget.Toast;

import java.io.IOException;

import core.MaskCore; // Import MaskCore

public class MaskService extends VpnService {
    private ParcelFileDescriptor vpnInterface = null;
    private Thread vpnThread = null;
    private MaskCore maskCore;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if ("START_VPN".equals(action)) {
            startVpn();
        } else if ("STOP_VPN".equals(action)) {
            stopVpn();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopVpn();
        super.onDestroy();
    }

    public void startVpn() {
        VpnService.Builder builder = new VpnService.Builder();
        builder.setSession("USA VPN")
               .addAddress("10.0.0.2", 24)
               .addRoute("0.0.0.0", 0)
               .addDnsServer("8.8.8.8")
               .setMtu(1500);

        try {
            vpnInterface = builder.establish();
            if (vpnInterface == null) {
                Log.e("MaskService", "VPN interface is null. Cannot start VPN thread.");
                showToast("VPN interface is null. Cannot start VPN thread.");
                return;
            }
        } catch (Exception e) {
            broadcastError("Failed to establish VPN interface: " + e.getMessage());
            return;
        }

        // Initialize MaskCore with the VPN interface
        maskCore = new MaskCore(vpnInterface);

        vpnThread = new Thread(() -> {
            try {
                if (maskCore.connectToServer()) {
                    showToast("VPN started successfully.");
                    maskCore.handleTraffic();  // Start handling traffic
                } else {
                    broadcastError("Failed to connect to VPN server.");
                }
            } catch (IOException e) {
                broadcastError("Error in VPN thread: " + e.getMessage());
            }
        });
        vpnThread.start();
    }

    public void stopVpn() {
        if (vpnThread != null && vpnThread.isAlive()) {
            vpnThread.interrupt();
            vpnThread = null;
        }

        try {
            if (maskCore != null) {
                maskCore.disconnect();  // Disconnect the VPN
                showToast("VPN disconnected successfully.");
            }
        } catch (IOException e) {
            broadcastError("Error closing VPN: " + e.getMessage());
        }

        Log.i("MaskService", "VPN stopped");
        showToast("VPN stopped.");
        broadcastStatus("VPN Stopped");
    }

    // Method to show a Toast
    private void showToast(String message) {
        // Show a Toast message
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    // Broadcast methods (unchanged)
    private void broadcastStatus(String statusMessage) {
        Intent statusIntent = new Intent("com.mask.VPN_STATUS");
        statusIntent.setPackage(getPackageName());
        statusIntent.putExtra("status_message", statusMessage);
        sendBroadcast(statusIntent);
    }

    private void broadcastError(String errorMessage) {
        Intent errorIntent = new Intent("com.mask.VPN_ERROR");
        errorIntent.setPackage(getPackageName());
        errorIntent.putExtra("error_message", errorMessage);
        sendBroadcast(errorIntent);
        showToast("Error: " + errorMessage); // Show error toast
    }
}
