package com.hgplsoft.babylonms;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.Semaphore;

/**
 * Created by horvath3ga on 09/01/2018.
 */

public class BMSEventSessionParameter {

    public BMSPack inputPack = null;
    public BMSPack outputPack = null;
    public Socket client = null;
    public InputStream reader = null;
    public PrintStream writer = null;
    public String shipUUID = null;
    public Object bms=null;
    public Date pingtime;

    public Semaphore writelock = new Semaphore(1);
    //public Semaphore newframe = new Semaphore(1);

    public BMSEventSessionParameter(Object bms)
    {
        this.bms = bms;
        //Ping();
        inputPack = new BMSPack();
        outputPack = new BMSPack();
    }

    /*
    public void Ping()
    {
        pingtime = DateTime.Now;
    }
    public bool IsNeedPing(int afterms) {
        long dateticks = afterms + (pingtime.Ticks / 10000);
        if (dateticks < (DateTime.Now.Ticks / 10000))
            return true;
        return false;
    }
    */
    public boolean TransferPacket(boolean hasFieldNames)
    {
            /*
            if ((client == null) || (!client.Connected))
            {
                return false;
            }
            */

        try
        {
            if (writelock != null) writelock.acquire();
            byte[] mem = outputPack.getPackToByteArray(hasFieldNames);
            writer.write(mem,0,mem.length);
            writer.flush();
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
        finally
        {
            if (writelock != null) writelock.release();
        }
    }

}
