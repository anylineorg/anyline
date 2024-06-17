/*
 * Copyright 2006-2023 www.anyline.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package org.anyline.util;

import java.util.ArrayList;
import java.util.List;

public class ByteBuffer {
    private List<Byte> list = new ArrayList<>();
    private byte[] bytes;
    private int offset = 0;
    private byte endian = 0; //大端
    public ByteBuffer(int endian) {
        this.endian = (byte) endian;
    }
    public ByteBuffer(int length, int endian) {
        bytes = new byte[length];
        this.endian = (byte) endian;
    }
    public ByteBuffer put(byte[] bs) {
        if(null == bytes) {
            for (byte b : bs) {
                list.add(b);
            }
        }else {
            for (byte b : bs) {
                bytes[offset++] = b;
            }
        }
        return this;
    }
    public ByteBuffer put(byte b) {
        if(null == bytes) {
            list.add(b);
        }else {
            bytes[offset++] = b;
        }
        return this;
    }
    public ByteBuffer put(int b) {
        put(NumberUtil.int2bytes(b, endian==0));
        return this;
    }
    public ByteBuffer put(short s) {
        put(NumberUtil.short2bytes(s, endian==0));
        return this;
    }
    public ByteBuffer put(long b) {
        put(NumberUtil.long2bytes(b, endian==0));
        return this;
    }
    public ByteBuffer put(double b) {
        put(NumberUtil.double2bytes(b));
        return this;
    }
    public ByteBuffer offset(int offset) {
        this.offset = offset;
        return this;
    }
    public ByteBuffer(byte[] bytes, int endian) {
        this.bytes = bytes;
    }
    public ByteBuffer(byte[] bytes, int endian, int offset) {
        this.bytes = bytes;
        this.endian = (byte) endian;
        this.offset = offset;
    }
    public byte readByte() {
        byte result = bytes[offset];
        offset ++;
        return result;
    }
    public int readInt() {
        int result = NumberUtil.byte2int(bytes, offset, 4, endian==0);
        offset += 4;
        return result;
    }
    public double readDouble() {
        double result = NumberUtil.byte2double(bytes, offset);
        offset += 8;
        return result;
    }
    public ByteBuffer step(int count) {
        offset = offset + count;
        return this;
    }
    public byte[] bytes() {
        if(null == bytes) {
            byte[] bts = new byte[list.size()];
            int idx = 0;
            for(byte b:list) {
                bts[idx++] = b;
            }
            return bts;
        }
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

    public byte endian() {
        return endian;
    }

    public void endian(int endian) {
        this.endian = (byte) endian;
    }
    public ByteBuffer clear() {
        list = new ArrayList();
        bytes = null;
        return this;
    }
}
