package com.hgplsoft.babylonms;

import android.os.SystemClock;

import org.apache.commons.lang.time.StopWatch;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import static android.R.attr.x;

/**
 * Created by horvath3ga on 2017.11.02..
 */
public class BMSPack
{
    public static byte HEADERBYTENUM = 14; //without marker with full length

    public String       Hdr;   /*Prelimutens*/
    public byte         Version;
    public byte     CompressFormat;
    public short Attribs; //Continue, once
    int MinTime; //minimal transfer time in ms defa = 0
    private ArrayList<BMSField> Fields;

    public long StartMinTime;
    StopWatch stopWatch;
    boolean stopper;

    public BMSPack() {
        Clear();
        stopWatch = new StopWatch();
        stopper=false;
    }

    public void Clear()
    {
        Hdr = BabylonMS.CONST_MARKER;
        Version = BabylonMS.CONST_VERSION;
        CompressFormat = 0;
        Attribs = 0;
        if (Fields != null)
        {
            for(BMSField f : Fields)
            {
                f.Dispose();
            }
        }
        Fields = new ArrayList<BMSField>();
    }

    public void setMinTimeWithoutStart(int time)
    {
        MinTime = time;
    }
    public void setMinTime(int time)
    {
        setMinTimeWithoutStart(time);
        try{
            stopWatch.start();
            stopper=true;
        } catch (Exception e){
            stopWatch.reset();
            stopWatch.start();
        }
    }
    public boolean IsReachedMinTime()
    {
        if (MinTime == 0) return true;
        if (stopper)
        {
            return stopWatch.getTime() > MinTime;
        } else { return true; }
    }
    public void WaitMinTime()
    {
        if (IsReachedMinTime()) { return; }
        if (stopper)
        {
            SystemClock.sleep((MinTime - stopWatch.getTime()));
            stopWatch.reset();
            stopWatch.start();
        }
    }

    private static byte[] BitConverter_GetBytes(int value )
    {
        ByteBuffer buffer = ByteBuffer.allocate(4).order(ByteOrder.nativeOrder());
        buffer.putInt(value);
        return buffer.array();
    }
    private static byte[] BitConverter_GetBytes(long value )
    {
        ByteBuffer buffer = ByteBuffer.allocate(8).order(ByteOrder.nativeOrder());
        buffer.putLong(value);
        return buffer.array();
    }
    private static byte[] BitConverter_GetBytes(short value )
    {
        ByteBuffer buffer = ByteBuffer.allocate(2).order(ByteOrder.nativeOrder());
        buffer.putShort(value);
        return buffer.array();
    }

    public void SetAttribs(short attr)
    {
        Attribs |= attr;
    }
    public boolean IsOnce()
    {
        return (Attribs & BabylonMS.CONST_Continous) == 0;
    }
    public boolean IsSeparate()
    {
        return (Attribs & BabylonMS.CONST_Separate) != 0;
    }
    public boolean HasFieldNames()
    {
        return (Attribs & BabylonMS.CONST_HasFieldNames) != 0;
    }
    public boolean IsAutosendOutputpack()
    {
        return (Attribs & BabylonMS.CONST_AutosendOutputpack) != 0;  //TODO eredetileg == de sztem nem volt jo.. majd kider-l
    }

    //return index of field
    public BMSField AddField(String Id,byte Type) {
        if (Fields.size() >= 255) { return null; }
        BMSField bms = new BMSField(Id, Type);
        Fields.add(bms);
        return bms;
    }
    public ArrayList<BMSField> GetFields()
    {
        return Fields;
    }
    public BMSField GetField(int index)
    {
        return Fields.get(index);
    }
    public int FieldsCount()
    {
        //if (HasFieldNames()){
        //    return Fields.size() - 1; //the last field entry is NAMES
        //} else
            return Fields.size();
    }
    public BMSField GetFieldByName(String id)
    {
        for (BMSField field : Fields) {
            if (field.MatchId(id)) {
                return field;
            }
        }
        return null;
        //return Fields.Find(x => (x.MatchId(id)));
    }
    public int GetFieldIndexByName(String id)
    {
        for (int i=0; i<Fields.size(); i++)
        {
            if (Fields.get(i).MatchId(id)) {
                return i;
            }
        }
        return -1;
        //return Fields.Find(x => (x.MatchId(id)));
    }



    public byte[] getPackToByteArray(boolean hasFieldNames){
        return getPack(hasFieldNames).toByteArray();
    }

    public ByteArrayOutputStream getPack(boolean hasFieldNames)
    {
        int Length=0;                 /*Exclude Hdr and exclude Length*/
        byte fiType;
        ByteArrayOutputStream mem = new ByteArrayOutputStream();
        try
        {
            if (hasFieldNames) {
                SetAttribs(BabylonMS.CONST_HasFieldNames);
            };
            mem.write(Hdr.getBytes(), 0, Hdr.length());   //0-10
            mem.write(BitConverter_GetBytes(Length), 0, 4);         //11-14
            mem.write(BitConverter_GetBytes(Version),0,1);                                 //15
            mem.write(BitConverter_GetBytes(CompressFormat),0,1);                          //16
            mem.write(BitConverter_GetBytes(Attribs), 0, 2);          //17. 2byte
            mem.write(BitConverter_GetBytes(MinTime), 0, 4);          //19. 4byte
            if (hasFieldNames)
            { mem.write(BitConverter_GetBytes((short)(Fields.size()+1)),0,2);                      //23.
            }else {
                mem.write(BitConverter_GetBytes((short)(Fields.size())),0,2);                      //23.
            }
            Length += (HEADERBYTENUM-4);            //without length  (5)
            int len;
            for(BMSField fi :Fields)
            {
                fiType = fi.GetTypeOfField();
                mem.write(BitConverter_GetBytes((byte)(fiType)),0,1);
                Length++;
                switch (fiType)
                {
                    case BabylonMS.CONST_FT_INT8:
                        len = fi.GetLengthInType();
                        mem.write(BitConverter_GetBytes((byte)(len)),0,1);
                        fi.GetStream().writeTo(mem);
                        Length += (len ) + 1; //count
                        break;
                    case BabylonMS.CONST_FT_INT16:
                        len = fi.GetLengthInType();
                        mem.write(BitConverter_GetBytes((byte)(len)),0,1);
                        fi.GetStream().writeTo(mem);
                        Length += (len * 4) + 1; //count
                        break;
                    case BabylonMS.CONST_FT_INT32:
                    case BabylonMS.CONST_FT_FLOAT:
                        len = fi.GetLengthInType();
                        mem.write(BitConverter_GetBytes((byte)(len)),0,1);
                        fi.GetStream().writeTo(mem);
                        Length += (len * 4) + 1; //count
                        break;
                    case BabylonMS.CONST_FT_INT64:
                    case BabylonMS.CONST_FT_DOUBLE:
                        len = fi.GetLengthInType();
                        mem.write(BitConverter_GetBytes((byte)(len)),0,1);
                        fi.GetStream().writeTo(mem);
                        Length += (len * 8) + 1; //count
                        break;
                    case BabylonMS.CONST_FT_UUID:
                        len = fi.GetLengthInType();
                        mem.write(BitConverter_GetBytes((byte)(len)),0,1);
                        fi.GetStream().writeTo(mem);
                        Length += (len * 16) + 1; //count
                        break;
                    case BabylonMS.CONST_FT_BYTE:
                        len = (int)fi.GetStream().size();
                        mem.write(BitConverter_GetBytes(len), 0, 4); //length = X....
                        fi.GetStream().writeTo(mem);
                        Length += len + 4; //count(4)
                        break;
                }
            }
            if (hasFieldNames)
            {
                mem.write(BitConverter_GetBytes((byte)(BabylonMS.CONST_FT_NAME)),0,1);
                Length++;
                len = Fields.size();
                mem.write(BitConverter_GetBytes((byte)(len)),0,1);
                Length++;
                for(BMSField fi : Fields)
                {
                    mem.write(fi.GetId(), 0, BabylonMS.CONST_FIELDNAME_LEN);
                    Length += BabylonMS.CONST_FIELDNAME_LEN;
                }
            }
            byte[] tmp = mem.toByteArray();
            mem = null;
            mem = new ByteArrayOutputStream(tmp.length);
            mem.write(tmp,0,11);
            mem.write(BitConverter_GetBytes(Length), 0, 4); //11-től
            mem.write(tmp,15,tmp.length-15);
//            mem.Position = 11;
//            mem.Write(BitConverter.GetBytes(Length), 0, 4);         //11-14
        }
        catch (Exception e2) { }
        return mem;
    }

    public void CopyTo(BMSPack destination)
    {
        destination.Clear();
        destination.Hdr = this.Hdr;
        destination.Version = this.Version;
        destination.CompressFormat = this.CompressFormat;
        destination.Attribs = this.Attribs;
        destination.MinTime = this.MinTime;
        destination.StartMinTime = this.StartMinTime;
        if (this.Fields != null)
        {
            BMSField f2;
            for(BMSField f : this.Fields)
            {
                f2 = Util.DeepCopy(f);
                destination.Fields.add(f2);
            }
        }
    }
}
