package com.example.mask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar; // For displaying Snackbar

import androidx.core.content.ContextCompat; // Import ContextCompat

// Import MaskService from the correct package
import service.MaskService;

import config.IpConfig;

public class MainActivity extends AppCompatActivity {

    private Button startVpnButton;
    private Button stopVpnButton;

    // Define a BroadcastReceiver to listen for error broadcasts
    private final BroadcastReceiver vpnErrorReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String errorMessage = intent.getStringExtra("error_message");
            if (errorMessage != null) {
                showSnackbar(errorMessage);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startVpnButton = findViewById(R.id.startVpnButton);
        stopVpnButton = findViewById(R.id.stopVpnButton);

        startVpnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVpnService();
            }
        });

        stopVpnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopVpnService();
            }
        });

        // Register the BroadcastReceiver to listen for VPN errors
        IntentFilter filter = new IntentFilter("com.example.mask.VPN_ERROR");
        ContextCompat.registerReceiver(
        this,
        vpnErrorReceiver,
        filter,
        ContextCompat.RECEIVER_NOT_EXPORTED // Specify the receiver is not exported
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister the receiver to avoid memory leaks
        unregisterReceiver(vpnErrorReceiver);
    }

    private void startVpnService() {
        Intent intent = new Intent(this, MaskService.class);
        intent.setAction("START_VPN"); // Specify action to start VPN
        startService(intent);
    }

    private void stopVpnService() {
        Intent intent = new Intent(this, MaskService.class);
        intent.setAction("STOP_VPN"); // Specify action to stop VPN
        stopService(intent);
    }

    // Method to show the Snackbar
    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }
}
