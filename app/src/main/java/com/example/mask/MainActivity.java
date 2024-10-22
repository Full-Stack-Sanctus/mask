package com.example.mask;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

// Import MaskService from the correct package
import service.MaskService;

import config.IpConfig;

public class MainActivity extends AppCompatActivity {

    private Button startVpnButton;
    private Button stopVpnButton;

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
}
