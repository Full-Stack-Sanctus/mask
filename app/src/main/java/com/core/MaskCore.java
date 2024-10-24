package core;

import android.net.VpnService;
import android.os.ParcelFileDescriptor;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import config.IpConfig; // Import your configuration class

public class MaskCore {

    private ParcelFileDescriptor vpnInterface;
    private DatagramChannel tunnel;

    public MaskCore(ParcelFileDescriptor vpnInterface) {
        this.vpnInterface = vpnInterface;
    }

    // Establishes the VPN tunnel to the remote server
    public boolean connectToServer() throws IOException {
        tunnel = DatagramChannel.open();
        tunnel.connect(new InetSocketAddress(IpConfig.SERVER_IP, IpConfig.SERVER_PORT)); // IP and Port from config

        if (tunnel.isConnected()) {
            return true; // Successfully connected
        } else {
            return false; // Connection failed
        }
    }

    // Manages the packet forwarding between the device and the VPN server
    public void handleTraffic() throws IOException {
        FileInputStream in = new FileInputStream(vpnInterface.getFileDescriptor());
        ByteBuffer packet = ByteBuffer.allocate(32767);

        while (!Thread.currentThread().isInterrupted()) {
            int length = in.read(packet.array());  // Reading from VPN interface
            if (length > 0) {
                packet.flip();
                tunnel.write(packet);  // Forwarding packet to server
                packet.clear();
            }
        }
    }

    // Close the VPN tunnel and interface
    public void disconnect() throws IOException {
        if (tunnel != null && tunnel.isOpen()) {
            tunnel.close();
        }
        if (vpnInterface != null) {
            vpnInterface.close();
        }
    }
}

//managing the connection to the server and handling data transfers. 