package com.hgplsoft.babylonms;

/* !!!Need to compile with "allow unsafe" code in project build options!!!
 * 
 * Any others:
 * The ship has uniq identifier UUID
 *
 * command line parameters
 *
 * 0. used pipename
 * 1. Called UUID , this need to be same than own ship UUID
 *
 * if only 1 parameter then calledUUID = executable file name
 *
 * Suggest choice pipename exactly like UUID
 *
 * If no parameter then write help and exit



     PACK

        00-10 HDR
        11-14 LENGTH                                //Exclude Hdr                                   0..3
        15    VERSION                                                                           //  4
        16    COMPRESSFORMAT                        //0=CFNone, 1=CFZip                             5
        17    ATTRIBS                               //bits  0. 0=once 1=continous                   6..7
            0.  0= normal 1 = Separate Request (Exit, undock)
            1.  0=once, 1= Continuous
            2.  0= Normal 1=Tesmode (need send information to partner)
            3.  1 = send UUID

        19-22    MINTIME               (4byte in ms)                                                8..11
                                                    //if the transfer packet ready lower than mintime then wait for mintime
                                                    //mintime counter zero when this packet received or after last packet sended
                                                    //default  = 0 = off

        23,24    FIELDSCOUNT                        //2byte                                         //  12,13

        20....                                      IntegerArray
            FIELDTYPE       INTEGER
            COUNT           (1byte)                  1..255
            FIELDDATA       (4*count)

            FIELDTYPE       FLOAT
            COUNT           (1byte)                  1..255
            FIELDDATA       (4*COUNT)

            FIELDTYPE       BYTE
            COUNT           (4byte)
            FIELDDATA       (COUNT)

Special field types

            FIELDTYPE       NAME                    //Fields names if requested
            FIELDDATA       (CONST_FIELDNAME_LEN*FIELDSCOUNT)        (10*)
            ...

Not implemented yet

            FIELDTYPE       UUID                    //TODO Not implemented  own UUID
            FIELDDATA       (16byte)


 */


import android.app.Application;
import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Console;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Serializable;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Exchanger;
import java.util.concurrent.Semaphore;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.lang.time.*;

public class BabylonMS {
    public static  final String ID="BabylonMS";
    /*
    [DllImport("Shell32.dll")]
    public static extern int ShellExecuteA(IntPtr hwnd, String lpOperation, String lpFile, String lpParameters, String lpDirecotry, int nShowCmd);
*/
    public static  final byte CONST_VERSION = 1;
    public static  final String CONST_MARKER = "Prelimutens";
    public static  final byte CONST_FIELDNAME_LEN = 10;

    public static  final short CONST_Separate = 1;//0.bit
    public static  final short CONST_Continous = 2;//1.bit
    public static  final short CONST_Test = 4;//2.bit
    public static  final short CONST_AutosendOutputpack = 16;
    public static  final short CONST_HasFieldNames = 32;  //if last field is NAME type and has field's names

    public static  final byte CONST_CF_None = 0;
    public static  final byte CONST_CF_Zip = 1;

    public static final byte CONST_FT_INT8 = 0;  //1  C# bitorder
    public static final byte CONST_FT_INT16 = 1;  //2  C# bitorder
    public static final byte CONST_FT_INT32 = 2;  //4  C# bitorder
    public static final byte CONST_FT_INT64 = 3;  //8  C# bitorder
    public static final byte CONST_FT_FLOAT = 4; //4
    public static final byte CONST_FT_DOUBLE = 5; //8
    public static final byte CONST_FT_BYTE = 6; //x byte
    public static final byte CONST_FT_UUID = 7; //x byte
    public static final byte CONST_FT_NAME = 8; //x byte
    public static final byte CONST_FT_COUNT = (byte) (CONST_FT_NAME + 1);  //max fields count
    public static final byte CONST_FT_END = CONST_FT_COUNT;  //The enrgy pattern splitter

    private static final int CONST_STARTSHIP_SUCCESS = 0;
    private static final int CONST_STARTSHIP_ERROR = -1;
    private static final int CONST_STARTSHIP_NOTFOUND = -2;
    private static final int CONST_STARTSHIP_FOUNDPIPE = -3;
    private static final int CONST_STARTSHIP_UNEXPEXTEDUUID = -4;

    private static final int CONST_SHIPDOCKINGEXIT_SUCCESS = 0;
    private static final int CONST_SHIPDOCKINGEXIT_FEWPARAMS = 1;
    private static final int CONST_SHIPDOCKINGEXIT_ERROR = 2;

    public static final boolean DEBUG_WriteConsole = false;
    public static final boolean DEBUG_WithoutStartShip = false;

    /// <summary>
    /// TCP IP Address for server or client
    /// </summary>
    String IP="";
    /// <summary>
    /// Port number for network connection server or client side
    /// </summary>
    int PORT = 0;
    /// <summary>
    /// Socket communication over IP for this instance
    /// </summary>
    boolean networked=false;
    Network NET;
    String PipeName;
    String ShipUUID;  //app UUID
    String StationUUID;  //station UUID
    //public BMSPack inputpack;  //started clientside
    //public BMSPack outputpack; //started clientside
    Context ctx;

    public interface BMSEventHandler {
        public void Event(BMSEventSessionParameter session);
    }
    ///Client connect
    public interface ClientConnectedEventHandler {
        public void Event(Socket client);
    }
    public void setClientConnectedEventHandler(BMSEventHandler listener) {
        this.ClientConnected = listener;
    }
    public BMSEventHandler ClientConnected = null;
    protected void OnClientConnected(BMSEventSessionParameter session)
    {
        try
        {
            //PrintStream writer = new PrintStream(client.getOutputStream());
            session.writer.println(ShipUUID);  //client first things send own (ship) UUID with stringline
            session.writer.flush();
        }
        catch (Exception e4) {
            if (DEBUG_WriteConsole) Log.d(ID,"ErrWriteLine");
        }
        BMSEventHandler handler = ClientConnected;
        if (handler != null)
        {
            handler.Event(session);
        }
    }




    public void setNewInputFrameEventHandler(BMSEventHandler listener) {
        this.NewInputFrame = listener;
    }
    public BMSEventHandler NewInputFrame=null;
    protected void OnNewInputFrame(BMSEventSessionParameter session)
    {
        if (DEBUG_WriteConsole) Log.d(ID,"Packet received.");
        BMSEventHandler handler = NewInputFrame;
        if (handler != null)
        {
            handler.Event(session);
        }
    }



    public void setDisconnectedEventHandler(BMSEventHandler listener) {
        this.Disconnected = listener;
    }
    public BMSEventHandler Disconnected=null;
    protected void OnDisconnected(BMSEventSessionParameter session)
    {
        BMSEventHandler handler = Disconnected;
        if (handler != null)
        {
            handler.Event(session);
        }
    }


    private boolean UnexectedShipDocked = false;
    public boolean IsReady = false;
    /*
    public interface ServerReadyEventHandler {
        public void Event(Socket client);
    }
    */
    public void setServerReadyEventHandler(BMSEventHandler listener) {
        this.ServerReadyForTransfer = listener;
    }
    public BMSEventHandler ServerReadyForTransfer=null;
    //protected String OnServerReadyForTransfer(Socket client)
    protected String OnServerReadyForTransfer(BMSEventSessionParameter session)
    {
        String shipUUID="";
        UnexectedShipDocked = false;
        try {
            shipUUID = Network.readLine(session);
            //BufferedReader reader = new BufferedReader(new InputStreamReader(session.client.getInputStream()));
            //shipUUID = reader.readLine();
            if (this.ShipUUID.compareTo(shipUUID) != 0)
            {
                // A ship azonos\t=ja nem egyezik meg az elv'rt azonos\t=val, szétkapcsolás
                UnexectedShipDocked = true;
                if (networked)
                {
                    NET.client.close();
                }
                else
                {
                    //TODO Named Pipe
                }
                if (DEBUG_WriteConsole) Log.d(ID,"Unexpected Ship will separate!");
            } else
            {
                if (DEBUG_WriteConsole) Log.d(ID,"Packet ready to transfer.");

                //PrintStream writer = new PrintStream(session.client.getOutputStream());
                session.writer.println(StationUUID);  //client first things send own (ship) UUID with stringline
                session.writer.flush();

            }
        }
        catch (Exception e4)
        {
            if (DEBUG_WriteConsole) Log.d(ID,"ErrReadLine");
        }
        if (!UnexectedShipDocked) // ha minden rendben akkor a user is dolgozhat...
        {
            IsReady = true;
            BMSEventHandler handler = ServerReadyForTransfer;
            if (handler != null)
            {
                handler.Event(session);
            }
        }
        return shipUUID;
    }



    //----------------eddig OK


    //Server wait
    public interface EventHandler {
        public void Event();
    }
    public EventHandler ServerWaitConnection=null;
    protected void OnServerWaitConnection()
    {
        EventHandler handler = ServerWaitConnection;
        if (handler != null)
        {
            handler.Event();
        }
    }
    public void setServerConnectedEventHandler(EventHandler listener) {
        this.ServerConnected = listener;
    }
    public EventHandler ServerConnected=null;
    protected void OnServerConnected()
    {
        EventHandler handler = ServerConnected;
        if (handler != null)
        {
            handler.Event();
        }
    }


    public BabylonMS(String pipeName, String shipUUID,String stationUUID,Context ctx)
    {
        this.StationUUID = stationUUID;
        this.ShipUUID = shipUUID;
        //inputpack = new BMSPack();
        //outputpack = new BMSPack();
        this.PipeName = pipeName;
        this.ctx = ctx;
    }


    public static byte[] FromHexString(String s) {
        int len = s.length();
        byte[] data = new byte[len/2];

        for(int i = 0; i < len; i+=2){
            data[i/2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
        }

        return data;
    }
    final protected static char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
    public static String byteArrayToHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length*2];
        int v;

        for(int j=0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j*2] = hexArray[v>>>4];
            hexChars[j*2 + 1] = hexArray[v & 0x0F];
        }

        return new String(hexChars);
    }
    public static byte[] toUUID128(String UUID)
    {
        byte[] uuid = FromHexString(UUID.replace("-", ""));
        return uuid;
    }
    public static String UUID128ToString(byte[] UUID)
    {
        String result = byteArrayToHexString(UUID);
        return result;
    }
    public static String UUID128ToString(byte[] UUID,int sfrom)
    {
        byte[] ui = Arrays.copyOfRange(UUID, sfrom, sfrom+16);
        String result = byteArrayToHexString(ui);
        return result;
    }




    //args[0] = pipename
    //removed>args[1] = called UUID
    /// <summary>
    /// Connect
    /// </summary>
    /// <param name="shipUUID"></param>
    /// <param name="args">0 = pipename</param>
    /// <returns></returns>
    public static BabylonMS ShipDocking(String ip,int port,String shipUUID,Context ctx)
    {
        BabylonMS bms = null;
        if (DEBUG_WriteConsole) Log.d(ID,shipUUID);
        bms = new BabylonMS(null, shipUUID, null,ctx); //stationUUID will fill with connection
        bms.networked = true;
        bms.IP = ip;
        bms.PORT = port;
        return bms;
    }
    public static BabylonMS LaunchMiniShip(String ip,int port, String pipename, String calledUUID, String callerUUID,Context ctx)
    {
        BabylonMS bms = new BabylonMS(null, calledUUID, callerUUID,ctx);
        bms.networked = true;
        bms.IP = ip;
        bms.PORT = port;
        return bms;
    }


    Semaphore readbufferFill = new Semaphore(1);
    int cntenter = 0;
    //boolean readInputToInputPack(BMSPack[] inputPack,InputStream reader)
    boolean readInputToInputPack(BMSEventSessionParameter session)
    {
        BMSPack inputpack = session.inputPack;
        InputStream reader = session.reader;
        try
        {
            if (cntenter>0) {
                Log.d(ID, "Second enter");
            }
            cntenter++;

            int oneByte;
            byte[] buffer = new byte[512];
            if (!Network.Fill(reader, buffer, BabylonMS.CONST_MARKER.length())) return false;
            if (BabylonMS.DEBUG_WriteConsole) Log.d(ID, "readPack");


            if (DEBUG_WriteConsole) Log.d(ID,"Begin2-Waitone");
            readbufferFill.acquire();
            if (DEBUG_WriteConsole) Log.d(ID,"Begin2-ReadInput");
            //header.WaitOne();
            //OnNewInputHeaderDetected(new EventArgs());

            while (!Network.compareMarker(buffer, 0))
            {
                System.arraycopy(buffer, 1, buffer, 0, BabylonMS.CONST_MARKER.length() - 1);
                oneByte = reader.read();
                if (oneByte!=-1)
                    buffer[BabylonMS.CONST_MARKER.length() - 1] = (byte)oneByte;
            }
            //!!!!!!!!!!!found marker
            try {
                if (!Network.Fill(reader, buffer, BMSPack.HEADERBYTENUM)) return false;
                if (inputpack != null){
                    inputpack.Clear();
                }else
                {
                    inputpack = new BMSPack();
                }
                //BMSPack inputpack = inputPack;

                int length = Network.ToInt32(buffer, 0);
                inputpack.Version = buffer[4];
                inputpack.CompressFormat = buffer[5];
                inputpack.Attribs = Network.ToUInt16(buffer, 6);
                inputpack.setMinTime(Network.ToUInt32(buffer, 8));
                int fieldscount = Network.ToUInt16(buffer, 12);

                length = length - (BMSPack.HEADERBYTENUM - 4);
                if (buffer.length < length) buffer = new byte[length];
                if (!Network.Fill(reader, buffer, length)) return false;

                //minden adat itt van
                if (inputpack.CompressFormat == BabylonMS.CONST_CF_Zip)
                {
                    //TODO uncompress to new buffer NOT TESTED and anither side (zipper) not implemented
                    buffer = Util.unzipper_unzip(buffer);
                    //
                }
                int c = 0;
                byte Type;
                //length new

                float FLOAT;
                int INTEGER;
                long INT64;
                double DOUBLE;
                BMSField field = null;
                for (int i = 0; i < fieldscount; i++)
                {
                    Type = buffer[c];
                    switch (Type)
                    {
                        case CONST_FT_INT8:
                            length = (byte)buffer[++c];
                            c++;
                            field = inputpack.AddField("", Type);
                            if (field != null)
                            {
                                for (int k = 0; k < length; k++)
                                {
                                    INTEGER = Network.ToInt8(buffer, c);
                                    c += 1;
                                    field.Value((byte)INTEGER);
                                }
                            }
                            break;
                        case CONST_FT_INT16:
                            length = (byte)buffer[++c];
                            c++;
                            field = inputpack.AddField("", Type);
                            if (field != null)
                            {
                                for (int k = 0; k < length; k++)
                                {
                                    INTEGER = Network.ToInt16(buffer, c);
                                    c += 2;
                                    field.Value((short)INTEGER);
                                }
                            }
                            break;
                        case CONST_FT_INT32:
                            length = (byte)buffer[++c];
                            c++;
                            field = inputpack.AddField("", Type);
                            if (field != null)
                            {
                                for (int k = 0; k < length; k++)
                                {
                                    INTEGER = Network.ToInt32(buffer, c);
                                    c += 4;
                                    field.Value(INTEGER);
                                }
                            }
                            break;
                        case CONST_FT_INT64:
                            length = (byte)buffer[++c];
                            c++;
                            field = inputpack.AddField("", Type);
                            if (field != null)
                            {
                                for (int k = 0; k < length; k++)
                                {
                                    INT64 = Network.ToInt64(buffer, c);
                                    c += 8;
                                    field.Value(INT64);
                                }
                            }
                            break;
                        case CONST_FT_FLOAT:
                            length = (byte)buffer[++c];
                            c++;
                            field = inputpack.AddField("", Type);
                            if (field != null)
                            {
                                for (int k = 0; k < length; k++)
                                {
                                    FLOAT = Network.ToSingle(buffer, c);
                                    c += 4;
                                    field.Value(FLOAT);
                                }
                            }
                            break;
                        case CONST_FT_DOUBLE:
                            length = (byte)buffer[++c];
                            c++;
                            field = inputpack.AddField("", Type);
                            if (field != null)
                            {
                                for (int k = 0; k < length; k++)
                                {
                                    DOUBLE = Network.ToDouble(buffer, c);
                                    c += 8;
                                    field.Value(DOUBLE);
                                }
                            }
                            break;
                        case CONST_FT_UUID:
                            length = (byte)buffer[++c];
                            c++;
                            field = inputpack.AddField("", Type);
                            if (field != null)
                            {
                                for (int k = 0; k < length; k++)
                                {
                                    String s = BabylonMS.UUID128ToString(buffer,c);
                                    //DOUBLE = Network.ToDouble(buffer, c);
                                    c += 16;
                                    field.ValueAsUUID(s);
                                }
                            }
                            break;
                        case CONST_FT_BYTE:
                            length = Network.ToInt32(buffer, ++c); //data length
                            c += 4;
                            field = inputpack.AddField("", Type);
                            if (field != null)
                            {
                                field.GetStream().write(buffer, c, length);
                            }
                            c += length;
                            break;
                        case CONST_FT_NAME:
                            length = fieldscount - 1;
                            c+=2;
                            for (int k = 0; k < length; k++)
                            {
                                String s = Network.GetString(buffer, c, CONST_FIELDNAME_LEN);
                                inputpack.GetField(k).SetId(s);
                                c += CONST_FIELDNAME_LEN;
                            }
                            break;
                    }
                }
                readbufferFill.release();
                if (DEBUG_WriteConsole) Log.d(ID,"End2");
                return true;
            } catch (Exception e1) {
                Log.d(ID,"Error");
                readbufferFill.release();
            }
        }catch (Exception e)
        {

        }
        return false;
    }


    void UnCompress(BMSPack inputpack)
    {
        if (inputpack.CompressFormat == CONST_CF_Zip)
        {
            //TODO with stream
        }
        return;
    }

    Semaphore newframe = new Semaphore(1);
    boolean exit = false;
    public void OpenGate(boolean blocking)
    {
        if (MODE != 0) return;
        MODE = MODE_OpenGate;
        if (DEBUG_WriteConsole) Log.d(ID,"Open gate, ");
        exit = false;
        while (!exit)
        {
            if (networked)
            {
                NET = new Network(Network.CONST_NetworkType_Server,IP, PORT,this); //SERVER!!! more instance
                if (NET != null)
                {
                    boolean conn = false;

                    NET.Disconnected = new Network.NEventHandler() {
                        @Override
                        public void Event(BMSEventSessionParameter session) {
                            try {
                                newframe.acquire();
                                OnDisconnected(session);
                                newframe.release();
                            }catch(Exception e){}
                        }
                    };

                    NET.Connected = new Network.NEventHandler() {
                        @Override
                        public void Event(BMSEventSessionParameter session) {
                            //(client, stream, arg) =>


                            class RunTask implements Runnable {
                                //BMSPack[] inputpack = new BMSPack[1];
                                Socket client;
                                BMSEventSessionParameter session;
                                RunTask(BMSEventSessionParameter session) {
                                    this.client = session.client;
                                    this.session = session;
                                }
                                public void run() {
                                    boolean exit = false;

                                    //BMSEventSessionParameter session = new BMSEventSessionParameter(this);
                                    //session.client = client;

                                    try {
                                        session.reader = client.getInputStream();
                                        session.writer = new PrintStream(client.getOutputStream());

                                        //InputStream reader = client.getInputStream();
                                        //OutputStream writer = client.getOutputStream();

                                        OnClientConnected(session);
                                        String stationUUID = Network.readLine(session.reader);
                                        session.shipUUID = stationUUID; //Todo ?? de lehet hog ykellene egy külön?

                                        if (DEBUG_WriteConsole) Log.d(ID," and object docked ");
                                        while (!exit) {
                                            if (readInputToInputPack(session)) {
                                                UnCompress(session.inputPack);
                                                exit = session.inputPack.IsSeparate();
                                                newframe.acquire();
                                                OnNewInputFrame(session);
                                                //OnNewInputFrame(inputpack[0]);
                                                newframe.release();
                                                session.inputPack.WaitMinTime();
                                                //client.WaitForPipeDrain();
                                                if (!session.inputPack.IsOnce()) {
                                                    //TODO NOT TESTED continuous on new thread  főként az original változót!!

                                                    class RunTask2 implements Runnable {
                                                        BMSEventSessionParameter session;
                                                        //Socket client;
                                                        RunTask2(BMSEventSessionParameter session) {
                                                            //this.client = client;
                                                            this.session = session;
                                                        }
                                                        public void run() {
                                                            boolean runi = true;
                                                            while (runi)   //A readinputpack-ot lejjebb ciklus intézi ha jön de mivel isOnce false ezért folyamatosan küldök anyagot az eredeti inputpack-al
                                                            {
                                                                try {
                                                                    if (DEBUG_WriteConsole) Log.d(ID, "Begin1");
                                                                    readbufferFill.acquire();
                                                                    newframe.acquire();
                                                                    OnNewInputFrame(session);
                                                                    newframe.release();
                                                                    //client.WaitForPipeDrain();
                                                                    runi = !session.inputPack.IsOnce() && !session.inputPack.IsSeparate();
                                                                    readbufferFill.release();
                                                                    if (DEBUG_WriteConsole) Log.d(ID, "End1");
                                                                    session.inputPack.WaitMinTime();
                                                                }catch (Exception e) {
                                                                    runi =false;
                                                                }
                                                            }
                                                        }
                                                    }
                                                    Thread t2 = new Thread(new RunTask2 (session));
                                                    t2.start();
                                                    while (!exit && t2.isAlive()) {
                                                        if (DEBUG_WriteConsole) Log.d(ID,"Async read");
                                                        if (readInputToInputPack(session)) {
                                                            if (DEBUG_WriteConsole) Log.d(ID,"Async read success");
                                                            UnCompress(session.inputPack);
                                                            exit = session.inputPack.IsSeparate();
                                                            if (!t2.isAlive()){
                                                                //ha épp kilép az IsOnce ciklusból akkor az első bejött packetet itt kell feldolgozni
                                                                //aztán megy a főciklusba
                                                                OnNewInputFrame(session);
                                                            }
                                                            session.inputPack.WaitMinTime();
                                                        } else {
                                                            exit = true;
                                                        }
                                                    }
                                                } else {

                                                }
                                            } else {
                                                exit = true;
                                            }
                                        }
                                        client.close();
                                    }catch (Exception e){
                                        Log.d(ID,"ClientErr "+e.getMessage());
                                    };
                                }
                            }
                            Thread t = new Thread(new RunTask (session));
                            t.start();
                        }
                    } ;
                    while (blocking)  //TODO Nincs ;rtelmezve a kil;p;s hiszen a kliens nem l;ptetheti ki mert nem is ő léptette be
                    {
                        try {
                            Thread.sleep(500);
                        }catch (Exception e){}
                    }
                    exit=true;
                };
            }
            else
            {
                //TODO not networked theme
            }
        }

    }

    public void OpenGate() { OpenGate(true); }

    public void Disengage()
    {
        if (MODE == 0) return;
        try
        {
            if (networked)
            {
                if (MODE == MODE_PrepareGate)
                {
                    NET.client.close();
                } else
                {
                    NET.server.close();
                }
            } else
            {
                //TODO Not networked mode
                /*
                if (MODE == MODE_PrepareGate)
                {
                    server.Close();
                    serverWriter.Close();
                }
                else
                {
                    client.Close();
                    clientWriter.Close();
                }
                */
            }
            MODE = 0;
        }
        catch (Exception e) { }
    }

    //InputStream reader ;
    //OutputStream writer ;
    boolean conn = false;
    private byte MODE_PrepareGate = 1;
    private byte MODE_OpenGate = 2;
    private int MODE=0;
    private Semaphore sema3 = new Semaphore(1);
    public void PrepareGate(){
        if (MODE != 0) return;
        MODE = MODE_PrepareGate;

        class RunTask implements Runnable {
            boolean shipSeparated;
            boolean exit = false;
            BMSPack[] inputpack = new BMSPack[1];
            RunTask() {

            }
            public void run() {
                boolean exit = false;
                while (!exit)
                {
                    if (networked) {
                        //BMSEventSessionParameter session = new BMSEventSessionParameter(this);  //Will created before connect
                        NET = new Network(Network.CONST_NetworkType_Client,IP, PORT,(Object)this); //CLient type Network (one session)
                        if (NET != null)
                        {
                            shipSeparated = false;
                            //boolean conn = false;
                            NET.Disconnected = new Network.NEventHandler() {
                                @Override
                                public void Event(BMSEventSessionParameter session) {
                                    OnDisconnected(session);
                                    shipSeparated = true;
                                    NET = null;
                                }
                            };
                            NET.Connected = new Network.NEventHandler() {
                                @Override
                                public void Event(BMSEventSessionParameter session) {
                                    //(client, stream, arg) =>
                                    try {
                                        session.reader = session.client.getInputStream();
                                        session.writer = new PrintStream(session.client.getOutputStream());
                                        OnServerConnected();
                                    }catch (Exception e){};
                                    conn = true;
                                }
                            };

                            while ((!conn) && (!shipSeparated)) //WaitforConnection
                            {
                                try {
                                    Thread.sleep(500);
                                }catch (Exception e){}
                            }

                            if (!shipSeparated)
                            {

                                String UUID=OnServerReadyForTransfer(NET.session); //NET.client);   //reader.ReadLine();
                                NET.session.shipUUID = UUID;
                                if (!UnexectedShipDocked)
                                {
                                    while ((!shipSeparated)&&(NET.client.isConnected()))
                                    {
                                        try
                                        {
                                            if (readInputToInputPack(NET.session))
                                            {
                                                UnCompress(NET.session.inputPack);
                                                sema3.acquire();
                                                OnNewInputFrame(NET.session);
                                                sema3.release();
                                                NET.session.inputPack.WaitMinTime();
                                            }
                                            else
                                            {
                                                shipSeparated = true;
                                            }
                                        }
                                        catch (Exception e1)
                                        {
                                            shipSeparated = true;
                                            IsReady = false;
                                        }
                                    }
                                }
                            }
                            exit = true;
                            IsReady = false;
                            shipSeparated = true;
                            //NET.Stop();
                        }

                    } else{
                        //TODO PrepareGate notnetworked named pipe equ in android like intents
                    }
                }
            }
        }
        Thread t = new Thread(new RunTask ());
        t.start();
    }

}



