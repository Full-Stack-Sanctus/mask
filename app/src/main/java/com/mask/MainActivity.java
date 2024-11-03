package com.mask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast; // Import Toast
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

import com.google.android.material.snackbar.Snackbar; // For displaying Snackbar

import service.MaskService; // Import your MaskService
import log.MyLogger; // Import your logger
import config.IpConfig; // Import your configuration class

public class MainActivity extends AppCompatActivity {

    // Define your BroadcastReceiver to listen for error broadcasts
    private final BroadcastReceiver vpnErrorReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.mask.VPN_ERROR".equals(intent.getAction())) {
                String errorMessage = intent.getStringExtra("error_message");
                if (errorMessage != null) {
                    showSnackbar("Error: " + errorMessage);
                }
            } else if ("com.mask.VPN_STATUS".equals(intent.getAction())) {
                String statusMessage = intent.getStringExtra("status_message");
                if (statusMessage != null) {
                    showSnackbar("Status: " + statusMessage);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Setup the logger
        MyLogger.setupLogger();

        Button startVpnButton = findViewById(R.id.startVpnButton);
        startVpnButton.setOnClickListener(v -> {
            // Start VPN service
            Toast.makeText(MainActivity.this, "Start VPN Button Clicked!", Toast.LENGTH_SHORT).show();
            MyLogger.logInfo("Start button has been clicked!");
            
            startVpnService();
        });

        Button stopVpnButton = findViewById(R.id.stopVpnButton);
        stopVpnButton.setOnClickListener(v -> {
            // Stop VPN service
            Toast.makeText(MainActivity.this, "Stop VPN Button Clicked!", Toast.LENGTH_SHORT).show();
            
            stopVpnService();
        });

        // Register the BroadcastReceiver to listen for VPN errors and status
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.mask.VPN_ERROR");
        filter.addAction("com.mask.VPN_STATUS");
        registerReceiver(vpnErrorReceiver, filter, Context.RECEIVER_NOT_EXPORTED); // Removed Context.RECEIVER_NOT_EXPORTED for compatibility
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister the receiver to avoid memory leaks
        unregisterReceiver(vpnErrorReceiver);
    }

    private void startVpnService() {
        Intent intent = new Intent(this, com.service.MaskService.class);
        intent.setAction("START_VPN"); // Specify action to start VPN
        startService(intent);
    }

    private void stopVpnService() {
        Intent intent = new Intent(this, com.service.MaskService.class);
        intent.setAction("STOP_VPN"); // Specify action to stop VPN
        stopService(intent);
    }

    // Method to show the Snackbar
    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
        Log.i("MainActivity", "Showing Snackbar: " + message); // Log the Snackbar event
    }
}
