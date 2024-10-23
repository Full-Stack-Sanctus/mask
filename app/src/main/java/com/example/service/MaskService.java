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

import android.util.Log;



import config.IpConfig; // Adjust the import path based on your project structure


public class MaskService extends VpnService {
    private ParcelFileDescriptor vpnInterface = null;
    private Thread vpnThread = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if ("START_VPN".equals(action)) {
            startVpnService();
        } else if ("STOP_VPN".equals(action)) {
            stopVpnService();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopVpnService();
        super.onDestroy();
    }

    public void startVpnService() {
        Builder builder = new Builder();

        builder.setSession("USA VPN")
               .addAddress("10.0.0.2", 24)
               .addRoute("0.0.0.0", 0)
               .addDnsServer("8.8.8.8")
               .setMtu(1500);
        
        try {
            vpnInterface = builder.establish();
        } catch (Exception e) {
            // Log.e("MaskService", "Failed to establish VPN interface", e);
            broadcastError("Failed to establish VPN interface: " + e.getMessage());  // Broadcast error message
        
        }
        
        if (vpnInterface == null) {
            Log.e("MaskService", "VPN interface is null. Cannot start VPN thread.");
            return;
        }

        
        if (vpnInterface != null) {
            vpnThread = new Thread(() -> {
                try {
                    DatagramChannel tunnel = DatagramChannel.open();
                    // Attempt to connect to the VPN server
                    try {
                        tunnel.connect(new InetSocketAddress(IpConfig.SERVER_IP, IpConfig.SERVER_PORT)); // Attempt to connect
                    } catch (IOException e) {
                        // Log.e("MaskService", "Invalid server IP or port", e);
                        broadcastError("Invalid server IP or port: " + e.getMessage());  // Broadcast error message
                        return; // Exit the thread if connection fails
                    }
                    
                    if (tunnel == null || !tunnel.isConnected()) {
                        broadcastError("VPN tunnel failed to connect");
                        return;
                    }

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
                    // Log.e("MaskService", "Error in VPN thread", e);
                    broadcastError("Error in VPN thread: " + e.getMessage());  // Broadcast error message
                }
            });
            vpnThread.start();
        }
    }

    public void stopVpnService() {
        if (vpnThread != null && vpnThread.isAlive()) {
            vpnThread.interrupt();
            vpnThread = null;
        }
        
        if (vpnInterface != null) {
            try {
                vpnInterface.close();
            } catch (IOException e) {
                Log.e("MaskService", "Error closing VPN interface", e);
                broadcastError("Error closing VPN interface: " + e.getMessage());  // Broadcast error message
            }
            vpnInterface = null;
        }
        
        Log.i("MaskService", "VPN stopped");
        broadcastStatus("VPN Stopped");  // Broadcast error message
    }
        
    // Method to broadcast status messages
    private void broadcastStatus(String statusMessage) {
        Intent statusIntent = new Intent("com.example.mask.VPN_STATUS");
        statusIntent.setPackage(getPackageName());
        statusIntent.putExtra("status_message", statusMessage);
        sendBroadcast(statusIntent);
        Log.i("MaskService", "Broadcasting status: " + statusMessage); // Log the broadcast
    }

    
    // Method to broadcast error messages
    private void broadcastError(String errorMessage) {
        Intent errorIntent = new Intent("com.example.mask.VPN_ERROR");
        errorIntent.setPackage(getPackageName());
        errorIntent.putExtra("error_message", errorMessage);
        sendBroadcast(errorIntent);
        Log.i("MaskService", "Broadcasting error: " + errorMessage); // Log the broadcast
    }
    
    
}
