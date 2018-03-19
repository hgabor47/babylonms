package com.hgplsoft.babylonms;

import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by horvath3ga on 2017.11.02..
 */
public class Network
{
    public static final String ID = "BMSNET";
    public static final int CONST_NetworkType_Server = 1;
    public static final int CONST_NetworkType_Client = 2;
    /// <summary>
    /// Summarized timeout for wait for connection
    /// </summary>
    public static final int CONST_CONNECT_TIMEOUT = 5000; //in ms
    /// <summary>
    /// Sleep connection thread for X ms and try again
    /// </summary>
    public static final int CONST_CONNECT_FREQ = 500; //in ms

    public int NetworkType = 0;
    private Object bms=null;
    public BMSEventSessionParameter session=null;


    public Socket client = null;
    public ServerSocket server = null;
    boolean exit = false;

    ///Received
    public interface NEventHandler {
        //public void messageReceived(String message);
        public void Event(BMSEventSessionParameter session);
    }
    public NEventHandler Received = null;
    protected void OnReceived(BMSEventSessionParameter session)
    {
        NEventHandler handler = Received;
        if (handler != null)
        {
            handler.Event(session);
        }
    }
    ///Connect

    public NEventHandler Connected = null;
    protected void OnConnected(BMSEventSessionParameter session)
    {
        NEventHandler handler = Connected;
        if (handler != null)
        {
            handler.Event(session);
        }
    }

    public NEventHandler Disconnected = null;
    protected void OnDisconnected(BMSEventSessionParameter session)
    {
        NEventHandler handler = Disconnected;
        if (handler != null)
        {
            handler.Event(session);
        }
    }

    public Network(int networkType, String ip, int port, Object bms)
    {
        this.bms = bms;
        switch (networkType)
        {
            case CONST_NetworkType_Client:
                session = null; //one session will be created when this connected another server
                Client(ip, port);
                break;
            case CONST_NetworkType_Server:
                session = null; //OnConnected will create sessions for clients
                Server(ip, port);
                break;
        }
    }
    public void Stop()
    {
        OnDisconnected(null);
        try {
            switch (NetworkType) {
                case CONST_NetworkType_Client:
                    client.close();
                    break;
                case CONST_NetworkType_Server:
                    server.close();
                    exit = true;
                    break;
            }
        }catch (Exception e){}
    }

    public Network Server(String ip,int port)
    {
        try {
            if (ip.isEmpty()) {
                server = new ServerSocket(port);
            } else {
                server = new ServerSocket(port, 1000, InetAddress.getByName(ip));
            }
        }catch (Exception e){}
        if (server!=null) {
            NetworkType = CONST_NetworkType_Server;
            class RunTask implements Runnable {
                public void run() {
                    while (!Thread.currentThread().isInterrupted() && (!exit)) {

                        try {
                            Socket client = server.accept();
                            BMSEventSessionParameter session1 =new BMSEventSessionParameter(bms);
                            session1.client = client;
                            OnConnected(session1); // All clients will got new session
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            Thread t = new Thread(new RunTask ());
            t.start();
        }
        return this;
    }

    //Client

    public Network Client(String ip,int port)
    {
        client = null;
        NetworkType = CONST_NetworkType_Client;

        class RunTask implements Runnable {
            String ip;int port;
            RunTask(String ip,int port) {
                this.ip= ip;this.port=port;
            }
            public void run() {
                int WaitConnection = CONST_CONNECT_TIMEOUT / CONST_CONNECT_FREQ;
                while ((client==null) && (--WaitConnection>0) ) {
                    try {
                        client = new Socket(ip, port);
                    } catch (Exception e){}
                    SystemClock.sleep(CONST_CONNECT_FREQ);
                }
                if (client!=null) {
                    ClientCore(client);
                }
                Log.d(ID,"Client not connected/disconnect.");
                Stop();
            }
        }
        Thread t = new Thread(new RunTask (ip,port));
        t.start();
        return this;
    }

    public InputStream clientstream;
    private void ClientCore(Socket client)
    {
        try {
            clientstream = client.getInputStream();
            session = new BMSEventSessionParameter(bms); //This session for this client (1)
            session.client = client;
            OnConnected(session);
            while ((!exit)) {
                if (clientstream.available()>0) {
                    OnReceived(session);
                }
                Thread.sleep(10);
            }
        }catch (Exception e){}
    }

    public void Send2Client(Socket client,BMSPack pack)
    {
        byte[] b = pack.getPackToByteArray(false);
        try {
            client.getOutputStream().write(b, 0, b.length);
        }catch (Exception e){}
    }
    /*public void Send2Server(BMSPack pack)
    {
        Send2Server(, pack);
    }
    */
    public void Send2Server(Socket client, BMSPack pack)
    {
    }
    public void Send2Server(Socket client, String value)
    {
        byte[] b = value.getBytes();
        try{
            client.getOutputStream().write(b,0,b.length);
        }catch (Exception e){}
    }
    public static boolean Fill(InputStream source, byte[] destination, int count)
    {
        int bytesRead, offset = 0;
        try {
            while (count > 0 &&
                    (bytesRead = source.read(destination, offset, count)) > 0) {
                offset += bytesRead;
                count -= bytesRead;
            }
        }catch (Exception e){

        }
        return count == 0;
    }

    public static String readLine(BMSEventSessionParameter session) {
        String str = "";
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(session.client.getInputStream()));
            str = reader.readLine();
        }catch (Exception e){}
        return str;
    }


    public static String readLine(InputStream source)
    {
        int res;
        char c;
        String ress = "";
        try {
            do {
                res = source.read();
                c = (char)((short)res);
                ress = ress + c;
            } while (res!=13);

        }catch (Exception e){

        }
        return ress.replace("\r\n", "");
    }

    public static boolean compareMarker(byte[] buffer, int index)
    {
        for(int i=0;i<BabylonMS.CONST_MARKER.length(); i++)
        {
            if (buffer[i + index] != BabylonMS.CONST_MARKER.charAt(i)) return false;
        }
        return true;
    }
    public static int ToInt8(byte[] buf,int indexOfArray){
        ByteBuffer buffer = ByteBuffer.allocate(buf.length).order(ByteOrder.nativeOrder());
        buffer.put(buf);
        return buffer.get(indexOfArray);
    }
    public static int ToInt16(byte[] buf,int indexOfArray){
        ByteBuffer buffer = ByteBuffer.allocate(buf.length).order(ByteOrder.nativeOrder());
        buffer.put(buf);
        return buffer.getShort(indexOfArray);
    }
    public static int ToInt32(byte[] buf,int indexOfArray){
        ByteBuffer buffer = ByteBuffer.allocate(buf.length).order(ByteOrder.nativeOrder());
        buffer.put(buf);
        return buffer.getInt(indexOfArray);
    }
    public static int ToUInt32(byte[] buf,int indexOfArray){
        ByteBuffer buffer = ByteBuffer.allocate(buf.length).order(ByteOrder.nativeOrder());
        buffer.put(buf);
        return buffer.getInt(indexOfArray);
    }
    public static short ToUInt16(byte[] buf,int indexOfArray){
        ByteBuffer buffer = ByteBuffer.allocate(buf.length).order(ByteOrder.nativeOrder());
        buffer.put(buf);
        return buffer.getShort(indexOfArray);
    }
    public static long ToInt64(byte[] buf,int indexOfArray){
        ByteBuffer buffer = ByteBuffer.allocate(buf.length).order(ByteOrder.nativeOrder());
        buffer.put(buf);
        return buffer.getLong(indexOfArray);
    }
    public static float ToSingle(byte[] buf,int indexOfArray){
        ByteBuffer buffer = ByteBuffer.allocate(buf.length).order(ByteOrder.nativeOrder());
        buffer.put(buf);
        return buffer.getFloat(indexOfArray);
    }
    public static double ToDouble(byte[] buf,int indexOfArray){
        ByteBuffer buffer = ByteBuffer.allocate(buf.length).order(ByteOrder.nativeOrder());
        buffer.put(buf);
        return buffer.getDouble(indexOfArray);
    }
    public static String ToString(byte[] buf,int indexOfArray,int onelen){
        return new String(buf, indexOfArray*onelen ,onelen);
    }
    public static String GetString(byte[] buf,int position,int length){
        return new String(buf, position ,length);
    }

    public static OutputStream readPack(InputStream clientstream)
    {
        try {
            int oneByte;
            byte[] buffer = new byte[512];
            if (!Fill(clientstream, buffer, BabylonMS.CONST_MARKER.length())) return null;
            if (BabylonMS.DEBUG_WriteConsole) Log.d(ID, "readPack");
            while (!compareMarker(buffer, 0)) {
                System.arraycopy(buffer, 1, buffer, 0, BabylonMS.CONST_MARKER.length() - 1);
                //Buffer.BlockCopy(buffer, 1, buffer, 0, BabylonMS.CONST_MARKER.length() - 1);
                oneByte = clientstream.read();
                if (oneByte!=-1)
                    buffer[BabylonMS.CONST_MARKER.length() - 1] = (byte)oneByte;
            }
            //!!!!!!!!!!!found marker
            try {
                ByteArrayOutputStream mem = new ByteArrayOutputStream();
                mem.write(BabylonMS.CONST_MARKER.getBytes(), 0, BabylonMS.CONST_MARKER.length());
                if (!Fill(clientstream, buffer, BMSPack.HEADERBYTENUM)) return null;
                mem.write(buffer, 0, BMSPack.HEADERBYTENUM);

                int length = ToInt32(buffer, 0);
                int fieldscount = buffer[12];
                length = length - (BMSPack.HEADERBYTENUM - 4);
                if (buffer.length < length) buffer = new byte[length];
                if (!Fill(clientstream, buffer, length)) return null;
                return mem;
            } catch (Exception e1) {}
        }catch (Exception e2){}
        return null;
    }

    public String readString(InputStream s)
    {
        String st = "";
        try {
            byte[] buffer = new byte[4];
            s.read(buffer, 0, 4);
            int len = ToInt32(buffer, 0);
            buffer = new byte[len];
            s.read(buffer, 0, len);
            st = buffer.toString(); // Encoding.ASCII.GetString(buffer);
        } catch (Exception e){}
        return st;
    }

}
