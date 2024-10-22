package com.example.mask;

import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import java.io.IOException;

public class MaskService extends VpnService {

    private ParcelFileDescriptor vpnInterface = null;

    @Override
    public void onCreate() {
        super.onCreate();
        // Setup code can go here
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Start the VPN connection
        startVpn();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Clean up the VPN connection
        stopVpn();
    }

    private void startVpn() {
        Builder builder = new Builder();
        builder.setSession("MyVPN")
               .setBlocking(true) // Optional: Block non-VPN traffic
               .addAddress("10.0.0.2", 24) // Example IP address
               .addRoute("0.0.0.0", 0); // Redirect all traffic through the VPN

        try {
            vpnInterface = builder.establish();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopVpn() {
        if (vpnInterface != null) {
            try {
                vpnInterface.close();
                vpnInterface = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
