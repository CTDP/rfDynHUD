package test;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPClient
{
	// sends a single packet with some 'random' data
    public static void main( String[] args ) throws Exception
    {
    	int port = 4445;
    	DatagramSocket socket = null;
    	byte[] buf = new byte[508];
    	for (int i = 0; i < buf.length; i++) {
    		buf[i] = (byte)(i % 64);
    	}
    	InetAddress address = InetAddress.getByName(args.length > 0 ? args[0] : "localhost");
    	DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
    	socket.send(packet);
    }
}
