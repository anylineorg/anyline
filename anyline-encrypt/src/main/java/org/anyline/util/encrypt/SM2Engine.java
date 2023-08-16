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


package org.anyline.util.encrypt;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.crypto.params.*;
import org.bouncycastle.math.ec.ECConstants;
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.BigIntegers;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * 对org.bouncycastle:bcprov-jdk15on:1.57-1.70扩展<br/>
 * BC库加密按C1C2C3,国密标准按C1C3C2(加密芯片)<br/>
 * 本扩展主要实现加密结果排列方式可选(通过mode属性设置)
 */
public class SM2Engine {
    private final Digest digest;

    /**是否为加密模式*/
    private boolean encryption;
    private ECKeyParameters ecKey;
    private ECDomainParameters ecParams;
    private int curveLength;
    private SecureRandom random;
    /**密文排序方式*/
    private int mode = 1;

    /**BC库默认排序方式-C1C2C3*/
    public static int CIPHER_MODE_BC = 0;
    /**国密标准排序方式-C1C3C2*/
    public static int CIPHER_MODE_CN = 1;

    public SM2Engine() {
        this(new SM3Digest());
    }

    public SM2Engine(Digest digest) {
        this.digest = digest;
    }

    /**
     * 设置密文排序方式
     * @param mode 排序方式
     */
    public void setMode(int mode){
        this.mode = mode;
    }

    /**
     * 默认初始化方法,使用国密排序标准
     * @param encryption  是否以加密模式初始化
     * @param param  曲线参数
     */
    public void init(boolean encryption, CipherParameters param) {
        init(encryption, CIPHER_MODE_CN, param);
    }

    /**
     * 默认初始化方法,使用国密排序标准
     * @param encryption 是否以加密模式初始化
     * @param mode 加密数据排列模式：1:标准排序；0:BC默认排序
     * @param param 曲线参数
     */
    public void init(boolean encryption, int mode, CipherParameters param) {
        this.encryption = encryption;
        this.mode = mode;
        if (encryption) {
            ParametersWithRandom rdm = (ParametersWithRandom) param;
            ecKey = (ECKeyParameters) rdm.getParameters();
            ecParams = ecKey.getParameters();
            ECPoint s = ((ECPublicKeyParameters) ecKey).getQ().multiply(ecParams.getH());
            if (s.isInfinity()) {
                throw new IllegalArgumentException("invalid key: [h]Q at infinity");
            }
            random = rdm.getRandom();
        } else {
            ecKey = (ECKeyParameters) param;
            ecParams = ecKey.getParameters();
        }
        curveLength = (ecParams.getCurve().getFieldSize() + 7) / 8;
    }

    /**
     * 加密或解密输入数据
     * @param bytes bytes
     * @param offset offset
     * @param len len
     * @return bytes
     * @throws InvalidCipherTextException
     */
    public byte[] processBlock( byte[] bytes, int offset, int len) throws InvalidCipherTextException {
        if (encryption) {
            // 加密
            return encrypt(bytes, offset, len);
        } else {
            return decrypt(bytes, offset, len);
        }
    }

    /**
     * 加密实现,根据cipherMode输出指定排列的结果,默认按标准方式排列
     * @param bytes bytes
     * @param offset offset
     * @param len len
     * @return bytes
     * @throws InvalidCipherTextException
     */
    private byte[] encrypt(byte[] bytes, int offset, int len)  throws InvalidCipherTextException {
        byte[] c2 = new byte[len];
        System.arraycopy(bytes, offset, c2, 0, c2.length);
        byte[] c1;
        ECPoint kPB;
        do {
            BigInteger k = next();
            ECPoint c1P = ecParams.getG().multiply(k).normalize();
            c1 = c1P.getEncoded(false);
            kPB = ((ECPublicKeyParameters) ecKey).getQ().multiply(k).normalize();
            kdf(digest, kPB, c2);
        }
        while (notEncrypted(c2, bytes, offset));
        byte[] c3 = new byte[digest.getDigestSize()];
        add(digest, kPB.getAffineXCoord());
        digest.update(bytes, offset, len);
        add(digest, kPB.getAffineYCoord());
        digest.doFinal(c3, 0);
        if (mode == CIPHER_MODE_CN){
            return Arrays.concatenate(c1, c3, c2);
        }
        return Arrays.concatenate(c1, c2, c3);
    }

    /**
     * 解密实现,默认按标准排列方式解密,解密时解出c2部分原文并校验c3部分
     * @param bytes bytes
     * @param offset offset
     * @param len len
     * @return bytes
     * @throws InvalidCipherTextException
     */
    private byte[] decrypt(byte[] bytes, int offset, int len) throws InvalidCipherTextException {
        byte[] c1 = new byte[curveLength * 2 + 1];
        System.arraycopy(bytes, offset, c1, 0, c1.length);
        ECPoint c1P = ecParams.getCurve().decodePoint(c1);
        ECPoint s = c1P.multiply(ecParams.getH());
        if (s.isInfinity()) {
            throw new InvalidCipherTextException("[h]C1 at infinity");
        }
        c1P = c1P.multiply(((ECPrivateKeyParameters) ecKey).getD()).normalize();
        byte[] c2 = new byte[len - c1.length - digest.getDigestSize()];
        if (mode == CIPHER_MODE_BC) {
            System.arraycopy(bytes, offset + c1.length, c2, 0, c2.length);
        }else{
            // C1 C3 C2
            System.arraycopy(bytes, offset + c1.length + digest.getDigestSize(), c2, 0, c2.length);
        }
        kdf(digest, c1P, c2);
        byte[] c3 = new byte[digest.getDigestSize()];
        add(digest, c1P.getAffineXCoord());
        digest.update(c2, 0, c2.length);
        add(digest, c1P.getAffineYCoord());
        digest.doFinal(c3, 0);
        int check = 0;
        // 检查密文输入值C3部分和由摘要生成的C3是否一致
        if (mode == CIPHER_MODE_BC) {
            for (int i = 0; i != c3.length; i++) {
                check |= c3[i] ^ bytes[c1.length + c2.length + i];
            }
        }else{
            for (int i = 0; i != c3.length; i++) {
                check |= c3[i] ^ bytes[c1.length + i];
            }
        }
        clear(c1);
        clear(c3);
        if (check != 0) {
            clear(c2);
            throw new InvalidCipherTextException("invalid cipher text");
        }
        return c2;
    }

    private boolean notEncrypted(byte[] encData, byte[] in, int offset) {
        for (int i = 0; i != encData.length; i++) {
            if (encData[i] != in[offset]) {
                return false;
            }
        }
        return true;
    }

    private void kdf(Digest digest, ECPoint c1, byte[] encData) {
        int ct = 1;
        int v = digest.getDigestSize();
        byte[] buf = new byte[digest.getDigestSize()];
        int off = 0;
        for (int i = 1; i <= ((encData.length + v - 1) / v); i++) {
            add(digest, c1.getAffineXCoord());
            add(digest, c1.getAffineYCoord());
            digest.update((byte) (ct >> 24));
            digest.update((byte) (ct >> 16));
            digest.update((byte) (ct >> 8));
            digest.update((byte) ct);
            digest.doFinal(buf, 0);
            if (off + buf.length < encData.length) {
                xor(encData, buf, off, buf.length);
            } else {
                xor(encData, buf, off, encData.length - off);
            }
            off += buf.length;
            ct++;
        }
    }

    private void xor(byte[] data, byte[] kdfOut, int dOff, int dRemaining) {
        for (int i = 0; i != dRemaining; i++) {
            data[dOff + i] ^= kdfOut[i];
        }
    }

    private BigInteger next() {
        int qBitLength = ecParams.getN().bitLength();
        BigInteger k;
        do {
            k = new BigInteger(qBitLength, random);
        }
        while (k.equals(ECConstants.ZERO) || k.compareTo(ecParams.getN()) >= 0);
        return k;
    }

    private void add(Digest digest, ECFieldElement v) {
        byte[] p = BigIntegers.asUnsignedByteArray(curveLength, v.toBigInteger());
        digest.update(p, 0, p.length);
    }

    private void clear(byte[] bytes) {
        for (int i = 0; i != bytes.length; i++) {
            bytes[i] = 0;
        }
    }
}
