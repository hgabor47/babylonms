package com.hgplsoft.babylonms;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Created by horvath3ga on 2017.11.02..
 */
public class BMSField implements Serializable
    {
    public String       Id;
    private byte      Type;        //Type of CONST_FT_...
    private ByteArrayOutputStream stream;                      //http://mindprod.com/jgloss/bytebuffer.html
    private ByteBuffer streambuffer;

    public BMSField(String Id,byte Type)
    {
        SetId(Id);
        this.Type = (byte)Math.min(Type, (BabylonMS.CONST_FT_COUNT-1));
        stream = new ByteArrayOutputStream();
    }
    public ByteArrayOutputStream GetStream() { return stream; }
    public int AtomLen()
    {
        int atomlen=4;
        switch (Type) {
            case BabylonMS.CONST_FT_INT8: atomlen = 1; break;
            case BabylonMS.CONST_FT_INT16: atomlen = 2; break;
            case BabylonMS.CONST_FT_INT32: atomlen = 4; break;
            case BabylonMS.CONST_FT_INT64: atomlen = 8; break;
            case BabylonMS.CONST_FT_DOUBLE: atomlen = 8; break;
            case BabylonMS.CONST_FT_FLOAT: atomlen = 4; break;
            case BabylonMS.CONST_FT_BYTE: atomlen = 1; break;
            case BabylonMS.CONST_FT_UUID: atomlen = 16; break;
        }
        return atomlen;
    }
    public int GetLengthInType(){
        return (int)(stream.size()/ AtomLen());
    }
    public int Length(){
        return GetLengthInType();
    }


    public byte GetTypeOfField() { return Type; }
    public byte[] GetId()
    {
        return Id.getBytes();//TODO charset?
    }

    public void SetId(String Id)
    {
        if (Id.length() >= BabylonMS.CONST_FIELDNAME_LEN)
        {
            this.Id = Id.substring(0, BabylonMS.CONST_FIELDNAME_LEN);
        }
        else {
            int l = BabylonMS.CONST_FIELDNAME_LEN - Id.length();
            if (l>0) {
                String s2 = String.format("%0" + l + "d", 0);
                this.Id = Id + s2.replace("0", "\0");
            } else {
                this.Id = Id;
            }
        }
    }
    public boolean MatchId(String anotherID)
    {
        byte[] a = anotherID.getBytes();
        byte[] b = GetId();
        int la;
        for (la = 0; (la < a.length) && (a[la] != 0); la++) { };
        int lb;
        for (lb = 0; (lb < b.length) && (b[lb] != 0); lb++) { };
        if (la!=lb) { return false; };
        for(int i = 0; i < la; i++)
        {
            if (a[i] != b[i]) return false;
        }
        return true;
    }

    public int Value(float value)
    {
        if (Type == BabylonMS.CONST_FT_FLOAT)
        {
            ByteBuffer buffer = ByteBuffer.allocate(4).order(ByteOrder.nativeOrder());
            buffer.putFloat(value);
            stream.write(buffer.array(), 0, 4);
            return 0;
        }
        else
            return -1;
    }
    public int Value(double value)
    {
        if (Type == BabylonMS.CONST_FT_DOUBLE)
        {
            ByteBuffer buffer = ByteBuffer.allocate(8).order(ByteOrder.nativeOrder());
            buffer.putDouble(value);
            stream.write(buffer.array(), 0, 8);
            return 0;
        }
        else
            return -1;
    }
    public int Value(byte value)
    {
        if (Type == BabylonMS.CONST_FT_INT8)
        {
            ByteBuffer buffer = ByteBuffer.allocate(1).order(ByteOrder.nativeOrder());
            buffer.put(value);
            stream.write(buffer.array(), 0, 1);
            return 0;
        }
        else
            return -1;
    }
    public int Value(short value)
    {
        if (Type == BabylonMS.CONST_FT_INT16)
        {
            ByteBuffer buffer = ByteBuffer.allocate(2).order(ByteOrder.nativeOrder());
            buffer.putShort(value);
            stream.write(buffer.array(), 0, 2);
            return 0;
        }
        else
            return -1;
    }
    public int Value(int value)
    {
        if (Type == BabylonMS.CONST_FT_INT32)
        {
            ByteBuffer buffer = ByteBuffer.allocate(4).order(ByteOrder.nativeOrder());
            buffer.putInt(value);
            stream.write(buffer.array(), 0, 4);
            return 0;
        }
        else
            return -1;
    }
    public int Value(long value)
    {
        if (Type == BabylonMS.CONST_FT_INT64)
        {
            ByteBuffer buffer = ByteBuffer.allocate(8).order(ByteOrder.nativeOrder());
            buffer.putLong(value);
            stream.write(buffer.array(), 0, 8);
            return 0;
        }
        else
            return -1;
    }
    // append value to inner stream
    public int Value(byte[] value)
    {
        if (Type == BabylonMS.CONST_FT_BYTE)
        {
            stream.write(value, 0, value.length);
            return 0;
        }
        else
            return -1;
    }
    public int ValueAsUUID(String UUID)
    {
        if (Type == BabylonMS.CONST_FT_UUID)
        {
            byte[] uuid = BabylonMS.toUUID128(UUID);
            stream.write(uuid, 0, uuid.length);
            return 0;
        }
        else
            return -1;
    }



    byte[] buffer;
    public long getValue(byte indexOfArray) {
        ByteBuffer buffer = ByteBuffer.allocate(stream.size()).order(ByteOrder.nativeOrder());
        buffer.put(stream.toByteArray());
        switch (Type) {
            case BabylonMS.CONST_FT_INT64:
                return buffer.getLong(indexOfArray);
            //break;
            case BabylonMS.CONST_FT_INT8:
                return buffer.get(indexOfArray);
            //break;
            case BabylonMS.CONST_FT_INT16:
                return buffer.getShort(indexOfArray);
            //break;
            case BabylonMS.CONST_FT_INT32:
                return buffer.getInt(indexOfArray);
            //break;
        }
        return 0;
    }

    public double getFloatValue(byte indexOfArray) {
        ByteBuffer buffer = ByteBuffer.allocate(stream.size()).order(ByteOrder.nativeOrder());
        buffer.put(stream.toByteArray());
        switch (Type) {
            case BabylonMS.CONST_FT_DOUBLE:
                return buffer.getDouble(indexOfArray);
            //break;
            case BabylonMS.CONST_FT_FLOAT:
                return buffer.getFloat(indexOfArray);
            //break;
        }
        return 0;
    }

    public String getUUIDValue(byte indexOfArray)
    {
        int pos = indexOfArray*16 ;
        if (Type == BabylonMS.CONST_FT_UUID)
        {
            String str="";
            byte[] buf =  Arrays.copyOfRange(stream.toByteArray(), pos, pos+16);
            try {
                str = BabylonMS.byteArrayToHexString(buf);
                //str = new String(buf, "ASCII");
            }catch (Exception e){}
            return str;
        }
        return "";
    }
    public byte[] getUUIDValueAsBytes()
    {
        if (Type == BabylonMS.CONST_FT_UUID)
        {
            return stream.toByteArray();
        }
        return null;
    }
    public boolean getBoolValue(byte indexOfArray) {
        ByteBuffer buffer = ByteBuffer.allocate(stream.size()).order(ByteOrder.nativeOrder());
        buffer.put(stream.toByteArray());
        switch (Type) {
            case BabylonMS.CONST_FT_INT8:
                return buffer.get(indexOfArray)!=0;
            //break;
        }
        return false;
    }


    public byte[] getValue()
    {
        if (Type == BabylonMS.CONST_FT_BYTE) {
            return stream.toByteArray();
        }
        return null;
    }


    public void clearValue()
    {
        Dispose();
        stream = new ByteArrayOutputStream();
    }
    public void Dispose()
    {
        try {
            stream.close();
            stream = null;
        }catch (Exception w){}
    }

}
