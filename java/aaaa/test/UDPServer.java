package test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class UDPServer
{
    private static final int PORT = 12345;
    
    private volatile DatagramSocket socket = null;
    private volatile InetAddress clientAddress = null;
    private volatile int clientPort = -1;
    
    private volatile boolean running = false;
    
    private Runnable sender = new Runnable()
    {
        @Override
        public void run()
        {
            while ( running )
            {
                // TODO: Send some response...
                
                try
                {
                    Thread.sleep( 10L );
                }
                catch ( InterruptedException e )
                {
                    e.printStackTrace();
                    break;
                }
            }
        }
    };
    
    private void dumpDatagram( byte[] buffer, int length )
    {
        System.out.println( "received message (" + length + "): " + new String( buffer, 0, length ) );
    }
    
    private Runnable receiver = new Runnable()
    {
        private byte[] buffer = new byte[ 1024 * 1024 ];
        private DatagramPacket datagram = new DatagramPacket( buffer, buffer.length );
        
        @Override
        public void run()
        {
            while ( running )
            {
                try
                {
                    socket.receive( datagram );
                    
                    if ( clientAddress == null )
                    {
                        clientAddress = datagram.getAddress();
                        clientPort = datagram.getPort();
                    }
                    
                    dumpDatagram( buffer, datagram.getLength() );
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                }
                
                try
                {
                    Thread.sleep( 10L );
                }
                catch ( InterruptedException e )
                {
                    e.printStackTrace();
                    break;
                }
            }
        }
    };
    
    public void start()
    {
        running = true;
        
        new Thread( sender ).start();
        new Thread( receiver ).start();
    }
    
    public void stop()
    {
        running = false;
        
        if ( socket != null )
        {
            socket.close();
        }
    }
    
    public UDPServer() throws SocketException
    {
        socket = new DatagramSocket( PORT );
    }
    
    public static void main( String[] args ) throws Throwable
    {
        UDPServer server = new UDPServer();
        
        server.start();
        
        Thread.sleep( 60000 );
        
        server.stop();
    }
}
