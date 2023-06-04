package org.anyline.util;

import java.util.ArrayList;
import java.util.List;

public class ByteBuffer {
    private List list = new ArrayList<>();
    private byte[] bytes;
    private int offset = 0;
    private int endian = 0; //大端
    public ByteBuffer(){}
    public ByteBuffer(int length, int endian){
        bytes = new byte[length];
        this.endian = endian;
    }
    public ByteBuffer put(byte[] bs){
        for(byte b:bs){
            bytes[offset++] = b;
        }
        return this;
    }
    public ByteBuffer put(byte b){
        bytes[offset++] = b;
        return this;
    }
    public ByteBuffer put(int b){
        put(NumberUtil.int2bytes(b));
        return this;
    }
    public ByteBuffer put(long b){
        put(NumberUtil.long2bytes(b));
        return this;
    }
    public ByteBuffer put(double b){
        put(NumberUtil.double2bytes(b));
        return this;
    }
    public ByteBuffer add(byte[] bs){
        for(byte b:bs){
            list.add(b);
        }
        return this;
    }
    public ByteBuffer add(byte b){
        list.add(b);
        return this;
    }
    public ByteBuffer add(int b){
        add(NumberUtil.int2bytes(b));
        return this;
    }
    public ByteBuffer add(long b){
        add(NumberUtil.long2bytes(b));
        return this;
    }
    public ByteBuffer add(double b){
        add(NumberUtil.double2bytes(b));
        return this;
    }
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
    public byte[] bytes(){
        return bytes;
    }

    public List getList() {
        return list;
    }

    public void setList(List list) {
        this.list = list;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getEndian() {
        return endian;
    }

    public void setEndian(int endian) {
        this.endian = endian;
    }
    public ByteBuffer clear(){
        list = new ArrayList();
        bytes = null;
        return this;
    }
}
