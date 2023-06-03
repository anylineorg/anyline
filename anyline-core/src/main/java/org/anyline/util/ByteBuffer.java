package org.anyline.util;

public class ByteBuffer {
    private byte[] bytes;
    private int offset = 0;
    private int endian = 0; //大端
    public ByteBuffer offset(int offset){
        this.offset = offset;
        return this;
    }
    public ByteBuffer(byte[] bytes, int endian){
        this.bytes = bytes;
    }
    public ByteBuffer(byte[] bytes, int endian, int offset){
        this.bytes = bytes;
        this.endian = endian;
        this.offset = offset;
    }
    public byte readByte(){
        byte result = bytes[offset];
        offset ++;
        return result;
    }
    public int readInt(){
        int result = NumberUtil.byte2int(bytes, offset, 4, endian==0);
        offset += 4;
        return result;
    }
    public double readDouble(){
        double result = NumberUtil.byte2double(bytes, offset);
        offset += 8;
        return result;
    }
    public ByteBuffer step(int count){
        offset = offset + count;
        return this;
    }
}
