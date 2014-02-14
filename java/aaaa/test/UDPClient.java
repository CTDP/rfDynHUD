package test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPClient
{
	private static InetAddress address;
	private static int port;
	private static DatagramSocket socket;

	// sends a single packet with some 'random' data
    public static void main( String[] args ) throws Exception
    {
    	address = InetAddress.getByName(args.length > 0 ? args[0] : "localhost");
    	port = 12345;
		socket = new DatagramSocket();
		System.out.println("Sending data...");
		send("This is a test".getBytes());
		System.out.println("Done.");
    }
    
    public static void send(byte[] data) throws IOException {
    	byte[] buf = new byte[508];
    	for (int i = 0; i < data.length; i += 508) {
    		int length = data.length - i;
    		System.arraycopy(data, i, buf, 0, length > 508 ? 508 : length);
    		DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
    		socket.send(packet);
    	}
    }
}
