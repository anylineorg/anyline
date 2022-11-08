package org.anyline.util.encrypt;

import org.anyline.util.NumberUtil;
import org.bouncycastle.asn1.gm.GMNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.*;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * 如果外部提供了公钥一般直接调用内部类SM2的静态方法加密解密<br/>
 * SMUtil.SM2.encrypt(publicKey, data);<br/>
 *<br/>
 * 如果外部提供了密钥对或者需要频繁加密解密一般先构造一个SM2<br/>
 * SM2 sm2 = SMUtil.sm2(publicKey, privateKey);<br/>
 * 等同于 new SM2(publicKey, privateKey);<br/>
 *<br/>
 * 如果需要生成密钥对<br/>
 * SM2 sm2 = SMUtil.sm2();<br/>
 * sm2.encrypt(data);   //因为sm2本身有公钥 方法中就不需要提供了<br/>
 * sm2.decrypt(data);<br/>
 * <br/>
 * 注意1:<br/>
 * 密钥是可以是byte[] 也可以是hex格式的String<br/>
 * 输入参数是byte[]或String<br/>
 * 如果要输入hex的String格式 先转换成byte[]<br/>
 * <br/>
 * 注意1:<br/>
 * 公钥和密文中有04前缀 根据情况去留 补上02的钥就是65位了
 * <br/>
 * 返回结果一般与输入参数对应，输入bytes[]也返回bytes 输入hex也返回hex<br/>
 * string byte hex之间转换可以调用NumberUtil.hex2byte,byte2hex等<br/>
 */
public class SMUtil {
    /**
     * 获取sm2密钥对
     * BC库使用的公钥=64个字节+1个字节（04标志位），BC库使用的私钥=32个字节
     * SM2秘钥的组成部分有 私钥D 、公钥X 、 公钥Y , 他们都可以用长度为64的16进制的HEX串表示，
     * <br/>SM2公钥并不是直接由X+Y表示 , 而是额外添加了一个头，当启用压缩时:公钥=有头+公钥X ，即省略了公钥Y的部分
     *
     * @param compress 是否压缩公钥（加密解密都使用BC库才能使用压缩）
     * @return SM2
     */
    public static SM2 sm2(boolean compress) {
        //获取一条SM2曲线参数
        X9ECParameters sm2ECParameters = GMNamedCurves.getByName(SM2.CRYPTO_NAME);
        //构造domain参数
        ECDomainParameters domainParameters = new ECDomainParameters(sm2ECParameters.getCurve(), sm2ECParameters.getG(), sm2ECParameters.getN());
        //1.创建密钥生成器
        ECKeyPairGenerator keyPairGenerator = new ECKeyPairGenerator();
        //2.初始化生成器,带上随机数
        try {
            keyPairGenerator.init(new ECKeyGenerationParameters(domainParameters, SecureRandom.getInstance("SHA1PRNG")));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        //3.生成密钥对
        AsymmetricCipherKeyPair asymmetricCipherKeyPair = keyPairGenerator.generateKeyPair();
        ECPublicKeyParameters publicKeyParameters = (ECPublicKeyParameters) asymmetricCipherKeyPair.getPublic();
        ECPoint ecPoint = publicKeyParameters.getQ();
        // 把公钥放入map中,默认压缩公钥
        // 公钥前面的02或者03表示是压缩公钥,04表示未压缩公钥,04的时候,可以去掉前面的04
        String publicKey = Hex.toHexString(ecPoint.getEncoded(compress));
        ECPrivateKeyParameters privateKeyParameters = (ECPrivateKeyParameters) asymmetricCipherKeyPair.getPrivate();
        BigInteger intPrivateKey = privateKeyParameters.getD();
        // 把私钥放入map中
        String privateKey = intPrivateKey.toString(16);
        return new SM2(publicKey, privateKey);
    }

    public static SM2 sm2() {
        return sm2(false);
    }

    public static SM2 sm2(String publicKey, String privateKey) {
        return new SM2(publicKey, privateKey);
    }
    public static SM2 sm2(byte[] publicKey, byte[] privateKey) {
        return new SM2(publicKey, privateKey);
    }


    public static class SM2 {
        /**
         * sm2曲线参数名称
         */
        public static final String CRYPTO_NAME = "sm2p256v1";

        /**
         * 公钥
         */
        private String publicKey;
        /**
         * 私钥
         */
        private String privateKey;

        public SM2() {

        }

        public SM2(String publicKey, String privateKey) {
            this.publicKey = publicKey;
            this.privateKey = privateKey;
        }
        public SM2(byte[] publicKey, byte[] privateKey) {
            this.publicKey = NumberUtil.byte2hex(publicKey);
            this.privateKey = NumberUtil.byte2hex(privateKey);
        }

        public String getPublicKey() {
            return publicKey;
        }
        public byte[] getPublicBytes(){
            return NumberUtil.hex2bytes(publicKey);
        }

        public void setPublicKey(String publicKey) {
            this.publicKey = publicKey;
        }
        public void setPublicKey(byte[] publicKey) {
            this.publicKey = NumberUtil.byte2hex(publicKey);
        }

        public String getPrivateKey() {
            return privateKey;
        }

        public byte[] getPrivateBytes() {
            return NumberUtil.hex2bytes(privateKey);
        }

        public void setPrivateKey(String privateKey) {
            this.privateKey = privateKey;
        }
        public void setPrivateKey(byte[] privateKey) {
            this.privateKey = NumberUtil.byte2hex(privateKey);
        }


        /**
         * SM2加密算法
         *
         * @param publicKey 公钥 hex格式的String
         * @param data      待加密的数据 如果是hex格式要先转成byte[]
         * @return 密文，BC库产生的密文带由04标识符，与非BC库对接时需要去掉开头的04
         */
        public static String encrypt(String publicKey, String data) {
            return encrypt(publicKey, data, SM2Engine.CIPHER_MODE_CN);
        }
        public static String encrypt(byte[] publicKey, String data) {
            return encrypt(publicKey, data, SM2Engine.CIPHER_MODE_CN);
        }

        public String encrypt(String data) {
            return encrypt(publicKey, data);
        }

        public static byte[] encrypt(String publicKey, byte[] data) {
            return encrypt(publicKey, data, SM2Engine.CIPHER_MODE_CN);
        }
        public static byte[] encrypt(byte[] publicKey, byte[] data) {
            return encrypt(publicKey, data, SM2Engine.CIPHER_MODE_CN);
        }

        public byte[] encrypt(byte[] data) {
            // 按国密排序标准加密
            return encrypt(publicKey, data);
        }

        /**
         * SM2加密算法
         *
         * @param publicKey 公钥
         * @param bytes     待加密的数据
         * @param mode      密文排列方式0-C1C2C3；1-C1C3C2；
         * @return 密文，BC库产生的密文带由04标识符，与非BC库对接时需要去掉开头的04
         */
        public static byte[] encrypt(byte[] publicKey, byte[] bytes, int mode) {
            // 获取一条SM2曲线参数
            X9ECParameters sm2ECParameters = GMNamedCurves.getByName(CRYPTO_NAME);
            // 构造ECC算法参数，曲线方程、椭圆曲线G点、大整数N
            ECDomainParameters domainParameters = new ECDomainParameters(sm2ECParameters.getCurve(), sm2ECParameters.getG(), sm2ECParameters.getN());
            //提取公钥点
            ECPoint pukPoint = sm2ECParameters.getCurve().decodePoint(publicKey);
            // 公钥前面的02或者03表示是压缩公钥，04表示未压缩公钥, 04的时候，可以去掉前面的04
            ECPublicKeyParameters publicKeyParameters = new ECPublicKeyParameters(pukPoint, domainParameters);

            SM2Engine sm2Engine = new SM2Engine();
            // 设置sm2为加密模式
            sm2Engine.init(true, mode, new ParametersWithRandom(publicKeyParameters, new SecureRandom()));

            byte[] arrayOfBytes = null;
            try {
                //byte[] in = data.getBytes();
                arrayOfBytes = sm2Engine.processBlock(bytes, 0, bytes.length);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return arrayOfBytes;
        }
        public static byte[] encrypt(String publicKey, byte[] bytes, int mode) {
            return encrypt(NumberUtil.hex2bytes(publicKey), bytes, mode);
        }

        public byte[] encrypt(byte[] bytes, int mode) {
            return encrypt(publicKey, bytes, mode);
        }

        public static String encrypt(String publicKey, String data, int mode) {
            byte[] bytes = encrypt(publicKey, data.getBytes(), mode);
            return Hex.toHexString(bytes);
        }
        public static String encrypt(byte[] publicKey, String data, int mode) {
            byte[] bytes = encrypt(publicKey, data.getBytes(), mode);
            return Hex.toHexString(bytes);
        }

        public String encrypt(String data, int mode) {
            return encrypt(publicKey, data, mode);
        }

        /**
         * SM2解密算法
         *
         * @param privateKey 私钥
         * @param data       密文数据
         * @return
         */
        public static String decrypt(String privateKey, String data) {
            //按国密排序标准解密
            byte[] bytes = decrypt(privateKey, data, SM2Engine.CIPHER_MODE_CN);
            return NumberUtil.byte2hex(bytes);
        }

        public static String decrypt(byte[] privateKey, String data) {
            //按国密排序标准解密
            byte[] bytes = decrypt(privateKey, data, SM2Engine.CIPHER_MODE_CN);
            return NumberUtil.byte2hex(bytes);
        }

        public String decrypt(String data) {
            return decrypt(privateKey, data);
        }

        /**
         * SM2解密算法
         *
         * @param privateKey 私钥
         * @param data 密文数据
         * @param mode 密文排列方式0-C1C2C3；1-C1C3C2；
         * @return
         */
        public static byte[] decrypt(String privateKey, String data, int mode) {
            // 使用BC库加解密时密文以04开头，传入的密文前面没有04则补上
            if (!data.startsWith("04")) {
                data = "04" + data;
            }
            byte[] cipherDataByte = Hex.decode(data);

            //获取一条SM2曲线参数
            X9ECParameters sm2ECParameters = GMNamedCurves.getByName(CRYPTO_NAME);
            //构造domain参数
            ECDomainParameters domainParameters = new ECDomainParameters(sm2ECParameters.getCurve(), sm2ECParameters.getG(), sm2ECParameters.getN());

            BigInteger privateKeyD = new BigInteger(privateKey, 16);
            ECPrivateKeyParameters privateKeyParameters = new ECPrivateKeyParameters(privateKeyD, domainParameters);

            SM2Engine sm2Engine = new SM2Engine();
            // 设置sm2为解密模式
            sm2Engine.init(false, mode, privateKeyParameters);

            try {
                byte[] arrayOfBytes = sm2Engine.processBlock(cipherDataByte, 0, cipherDataByte.length);
                return arrayOfBytes;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        public static byte[] decrypt(String privateKey, byte[] data, int mode) {
            return decrypt(privateKey, NumberUtil.byte2hex(data), mode);
        }

        public static byte[] decrypt(byte[] privateKey, String data, int mode) {
            return decrypt(NumberUtil.byte2hex(privateKey), data, mode);
        }
        public static byte[] decrypt(byte[] privateKey, byte[] data, int mode) {
            return decrypt(NumberUtil.byte2hex(privateKey), data, mode);
        }
        public byte[] decrypt(String data, int mode) {
            return decrypt(privateKey, data, mode);
        }
        public byte[] decrypt(byte[] data, int mode) {
            return decrypt(privateKey, data, mode);
        }
    }
}
