package com.hgplsoft.babylonms;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by horvath3ga on 2018.01.18..
 */

public class Util
{
    static String ID="BabylonMSUtil";
    static int usedProcessor=0;
    static int cpu = 0x0001;
    public static int ProcessorCount()
    { //TODO
        return 1;
    }
    /// <summary>
    /// Give me a number like 1,2,3,4, or 0x000f mean use all cpu in 4 core
    /// </summary>
    /// <returns></returns>
    public static int getFreeProcessor()
    {
        if (usedProcessor < ProcessorCount())
        {
            int c = cpu;
            cpu = cpu << 1;
            usedProcessor++;
            return c;
        }else
        {
            return 0x000f;
        }
    }
    /// <summary>
    /// Processor affinity if there is free like 1,2,3,4,ALL
    /// </summary>
    public static void setNextProcessor()
    {//TODO
        //Process.GetCurrentProcess().ProcessorAffinity = (System.IntPtr)getFreeProcessor();
    }
    /// <summary>
    /// Cyclic processor affinity like 1,2,3,4,1,2,3,4,1,2....
    /// </summary>
    public static void setNextProcessorCyclic()
    { //TODO
    }
    public static void setPriorityUp()
    {
        //TODO Process.GetCurrentProcess().PriorityClass = ProcessPriorityClass.AboveNormal;
    }
    public static void setPriorityDown()
    {
        //TODO Process.GetCurrentProcess().PriorityClass = ProcessPriorityClass.BelowNormal;
    }

    public static BMSField DeepCopy(BMSField value)
    {
        try {
            ByteArrayOutputStream fout = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(fout);
            out.writeObject(value);
            out.flush();
            byte[] bout = fout.toByteArray();

            ByteArrayInputStream fin = new ByteArrayInputStream(bout);
            ObjectInputStream in=new ObjectInputStream(fin);
            return (BMSField) in.readObject();

        } catch ( Exception e){}
        return null;
    }

    public static byte[] unzipper_unzip(byte[] is){
        ByteArrayOutputStream a = unzipper_unzip(new ByteArrayInputStream(is));
        return a.toByteArray();
    }

    public static ByteArrayOutputStream unzipper_unzip(InputStream is)
    {
        ByteArrayOutputStream fout;
        ZipInputStream zis;
        zis = new ZipInputStream(new BufferedInputStream(is));
        try
        {
            String code;//filename;
            ZipEntry ze;
            byte[] buffer = new byte[4096];
            int count, i =0;

            while ((ze = zis.getNextEntry()) != null)
            {
                code = ze.getName();
                if (code.compareTo("data.bin")==0) {
                    fout = new ByteArrayOutputStream();
                    long sz = ze.getSize();
                    while ((count = zis.read(buffer, 0, 4096)) != -1) {
                        fout.write(buffer, 0, count);
                    }
                    //fout.close();
                    zis.closeEntry();
                    zis.close();
                    return fout;
                }
                i++;
            }
        }
        catch(EOFException e){
        }
        catch(IOException e)
        {
            Log.e(ID,"UnzipErr:1");
            e.printStackTrace();
        }
        try{
            zis.close();
        }catch (IOException e){
            Log.e(ID,"UnzipErr:2 Close");
        };
        return null;
    }

}

