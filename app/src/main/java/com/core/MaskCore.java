package com.core;

import android.net.VpnService;
import android.os.ParcelFileDescriptor;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import com.config.IpConfig; // Import your configuration class
import log.MyLogger; // Import your logger

public class MaskCore {

    private ParcelFileDescriptor vpnInterface;
    private DatagramChannel tunnel;

    public MaskCore(ParcelFileDescriptor vpnInterface) {
        this.vpnInterface = vpnInterface;
        MyLogger.setupLogger(); // Ensure logger is set up
    }

    // Establishes the VPN tunnel to the remote server
    public boolean connectToServer() {
        try {
            tunnel = DatagramChannel.open();
            tunnel.connect(new InetSocketAddress(IpConfig.SERVER_IP, IpConfig.SERVER_PORT)); // IP and Port from config

            if (tunnel.isConnected()) {
                MyLogger.logInfo("Successfully connected to VPN server at " + IpConfig.SERVER_IP + ":" + IpConfig.SERVER_PORT);
                return true; // Successfully connected
            } else {
                MyLogger.logError("Failed to connect to VPN server at " + IpConfig.SERVER_IP + ":" + IpConfig.SERVER_PORT);
                return false; // Connection failed
            }
        } catch (IOException e) {
            MyLogger.logError("IOException during connection: " + e.getMessage());
            return false;
        }
    }

    // Manages the packet forwarding between the device and the VPN server
    public void handleTraffic() {
        try (FileInputStream in = new FileInputStream(vpnInterface.getFileDescriptor())) {
            ByteBuffer packet = ByteBuffer.allocate(32767);

            while (!Thread.currentThread().isInterrupted()) {
                int length = in.read(packet.array());  // Reading from VPN interface
                if (length > 0) {
                    packet.flip();
                    tunnel.write(packet);  // Forwarding packet to server
                    packet.clear();
                }
            }
        } catch (IOException e) {
            MyLogger.logError("Error handling traffic: " + e.getMessage());
        }
    }

    // Close the VPN tunnel and interface
    public void disconnect() {
        try {
            if (tunnel != null && tunnel.isOpen()) {
                tunnel.close();
                MyLogger.logInfo("VPN tunnel closed.");
            }
            if (vpnInterface != null) {
                vpnInterface.close();
                MyLogger.logInfo("VPN interface closed.");
            }
        } catch (IOException e) {
            MyLogger.logError("Error closing VPN resources: " + e.getMessage());
        }
    }
}

// Managing the connection to the server and handling data transfers.
