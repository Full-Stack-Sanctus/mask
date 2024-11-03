package com.mask;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.config.IpConfig; // Import your configuration class
import com.service.MaskService; // Import MaskService
import log.MyLogger; // Import logger

public class MainActivity extends AppCompatActivity {

    private EditText serverIpInput;
    private EditText serverPortInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup the logger
        MyLogger.setupLogger();

        serverIpInput = findViewById(R.id.serverIpInput);
        serverPortInput = findViewById(R.id.serverPortInput);

        Button startVpnButton = findViewById(R.id.startVpnButton);
        startVpnButton.setOnClickListener(v -> {
            String ip = serverIpInput.getText().toString();
            String portText = serverPortInput.getText().toString();

            if (ip.isEmpty() || portText.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter both IP and Port", Toast.LENGTH_SHORT).show();
                return;
            }

            int port;
            try {
                port = Integer.parseInt(portText);
            } catch (NumberFormatException e) {
                Toast.makeText(MainActivity.this, "Invalid port number", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save IP and Port in IpConfig before starting VPN
            IpConfig.SERVER_IP = ip;
            IpConfig.SERVER_PORT = port;

            MyLogger.logInfo("Starting VPN with IP: " + ip + " and Port: " + port);
            startVpnService();
        });

        Button stopVpnButton = findViewById(R.id.stopVpnButton);
        stopVpnButton.setOnClickListener(v -> stopVpnService());
    }

    private void startVpnService() {
        Intent intent = new Intent(this, MaskService.class);
        intent.setAction("START_VPN");
        startService(intent);
    }

    private void stopVpnService() {
        Intent intent = new Intent(this, MaskService.class);
        intent.setAction("STOP_VPN");
        stopService(intent);
    }
}
