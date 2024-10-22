package service;

import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import android.content.Intent; // For creating an Intent to broadcast
import android.content.Context; // If needed for certain context operations




import config.IpConfig; // Adjust the import path based on your project structure


public class MaskService extends VpnService {
    private ParcelFileDescriptor vpnInterface = null;
    private Thread vpnThread = null;

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
        Builder builder = new Builder();

        builder.setSession("USA VPN")
               .addAddress("10.0.0.2", 24)
               .addRoute("0.0.0.0", 0)
               .addDnsServer("8.8.8.8")
               .setMtu(1500);
        
        try {
            vpnInterface = builder.establish();
        } catch (Exception e) {
            Log.e("MaskService", "Failed to establish VPN interface", e);
        }
        
        if (vpnInterface != null) {
            vpnThread = new Thread(() -> {
                try {
                    DatagramChannel tunnel = DatagramChannel.open();
                    tunnel.connect(new InetSocketAddress(IpConfig.SERVER_IP, IpConfig.SERVER_PORT)); // Replace with actual server IP and port
                    
                    FileInputStream in = new FileInputStream(vpnInterface.getFileDescriptor());
                    ByteBuffer packet = ByteBuffer.allocate(32767);
                    while (!Thread.currentThread().isInterrupted()) {
                        int length = in.read(packet.array());  // Reading from VPN interface
                        if (length > 0) {
                            packet.flip();
                            tunnel.write(packet);  // Forwarding packet to USA server
                            packet.clear();
                        }
                    }
                } catch (Exception e) {
                    Log.e("MaskService", "Error in VPN thread", e);
                }
            });
            vpnThread.start();
        }
    }

    public void stopVpn() {
        if (vpnThread != null && vpnThread.isAlive()) {
            vpnThread.interrupt();
            vpnThread = null;
        }
        
        if (vpnInterface != null) {
            try {
                vpnInterface.close();
            } catch (IOException e) {
                Log.e("MaskService", "Error closing VPN interface", e);
            }
            vpnInterface = null;
        }
        
        Log.i("MaskService", "VPN stopped");
    }
    
    
    
    // Method to broadcast error messages
    private void broadcastError(String errorMessage) {
        Intent errorIntent = new Intent("com.example.mask.VPN_ERROR");
        errorIntent.putExtra("error_message", errorMessage);
        sendBroadcast(errorIntent);
    }
    
    
}
