package org.anyline.entity.metadata.init;

import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.Point;
import org.anyline.entity.metadata.Convert;
import org.anyline.entity.metadata.ConvertException;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.DateUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.util.Date;
import java.util.Map;

public enum DefaultConvert implements Convert {

    String2Boolean(String.class, Boolean.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            try {
                return BasicUtil.parseBoolean(value);
            }catch (Exception e){
                return value;
            }
        }
    },
    String2boolean(String.class, boolean.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            try {
                return BasicUtil.parseBoolean(value).booleanValue();
            }catch (Exception e){
                return value;
            }
        }
    },
    String2BigDecimal(String.class, BigDecimal.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            try {
                return BasicUtil.parseDecimal(value, null);
            }catch (Exception e){
                return value;
            }
        }
    },
    String2BigInteger(String.class, BigInteger.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return BigInteger.valueOf(BasicUtil.parseLong(value,null));
        }
    },
    String2Integer(String.class, Integer.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return BasicUtil.parseInt(value, null);
        }
    },
    String2int(String.class, int.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return BasicUtil.parseInt(value, null).intValue();
        }
    },
    String2Long(String.class, Long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return BasicUtil.parseLong(value, null);
        }
    },
    String2long(String.class, long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return BasicUtil.parseLong(value, null).longValue();
        }
    },
    String2Double(String.class, Double.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return BasicUtil.parseDouble(value, null);
        }
    },
    String2double(String.class, double.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return BasicUtil.parseDouble(value, null).doubleValue();
        }
    },
    String2Float(String.class, Float.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return BasicUtil.parseFloat(value, null);
        }
    },
    String2float(String.class, float.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return BasicUtil.parseFloat(value, null).floatValue();
        }
    },
    String2Short(String.class, Short.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return BasicUtil.parseShort(value, null);
        }
    },
    String2short(String.class, short.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return BasicUtil.parseShort(value, null).shortValue();
        }
    },
    String2Byte(String.class, Byte.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return BasicUtil.parseByte(value, null);
        }
    },
    String2byte(String.class, byte.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return BasicUtil.parseByte(value, null).byteValue();
        }
    },
    String2Character(String.class, Character.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return new Character(value.toString().toCharArray()[0]);
        }
    },
    String2char(String.class, char.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return value.toString().toCharArray()[0];
        }
    },
    Boolean2String(Boolean.class, String.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return value.toString();
        }
    },
    Boolean2boolean(Boolean.class, boolean.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((Boolean)value).booleanValue();
        }
    },
    Boolean2BigDecimal(Boolean.class, BigDecimal.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if((Boolean)value){
                return BigDecimal.ONE;
            }else{
                return BigDecimal.ZERO;
            }
        }
    },
    Boolean2BigInteger(Boolean.class, BigInteger.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if((Boolean)value){
                return BigInteger.ONE;
            }else{
                return BigInteger.ZERO;
            }
        }
    },
    Boolean2Integer(Boolean.class, Integer.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if((Boolean)value){
                return Integer.valueOf(1);
            }else{
                return Integer.valueOf(0);
            }
        }
    },
    Boolean2int(Boolean.class, int.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if((Boolean)value){
                return 1;
            }else{
                return 0;
            }
        }
    },
    Boolean2Long(Boolean.class, Long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if((Boolean)value){
                return Long.valueOf(1);
            }else{
                return Long.valueOf(0);
            }
        }
    },
    Boolean2long(Boolean.class, long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if((Boolean)value){
                return 1L;
            }else{
                return 0L;
            }
        }
    },
    Boolean2Double(Boolean.class, Double.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if((Boolean)value){
                return Double.valueOf(1);
            }else{
                return Double.valueOf(0);
            }
        }
    },
    Boolean2double(Boolean.class, double.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if((Boolean)value){
                return 1D;
            }else{
                return 0D;
            }
        }
    },
    Boolean2Float(Boolean.class, Float.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if((Boolean)value){
                return Float.valueOf(1);
            }else{
                return Float.valueOf(0);
            }
        }
    },
    Boolean2float(Boolean.class, float.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if((Boolean)value){
                return 1F;
            }else{
                return 0F;
            }
        }
    },
    Boolean2Short(Boolean.class, Short.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if((Boolean)value){
                return Short.valueOf("1");
            }else{
                return Short.valueOf("0");
            }
        }
    },
    Boolean2short(Boolean.class, short.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if((Boolean)value){
                return Short.valueOf("1").shortValue();
            }else{
                return Short.valueOf("0").shortValue();
            }
        }
    },
    Boolean2Byte(Boolean.class, Byte.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if((Boolean)value){
                return Byte.valueOf("1");
            }else{
                return Byte.valueOf("0");
            }
        }
    },
    Boolean2byte(Boolean.class, byte.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if((Boolean)value){
                return (byte)1;
            }else{
                return (byte)0;
            }
        }
    },
    Boolean2Character(Boolean.class, Character.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if((Boolean)value){
                return Character.valueOf('1');
            }else{
                return Character.valueOf('0');
            }
        }
    },
    Boolean2char(Boolean.class, char.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if((Boolean)value){
                return '1';
            }else{
                return '0';
            }
        }
    },
    boolean2String(boolean.class, String.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return value.toString();
        }
    },
    boolean2Boolean(boolean.class, Boolean.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Boolean.valueOf((boolean)value);
        }
    },
    boolean2BigDecimal(boolean.class, BigDecimal.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if((boolean)value){
                return BigDecimal.ONE;
            }else{
                return BigDecimal.ZERO;
            }
        }
    },
    boolean2BigInteger(boolean.class, BigInteger.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if((boolean)value){
                return BigInteger.ONE;
            }else{
                return BigInteger.ZERO;
            }
        }
    },
    boolean2Integer(boolean.class, Integer.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if((boolean)value){
                return Integer.valueOf(1);
            }else{
                return Integer.valueOf(0);
            }
        }
    },
    boolean2int(boolean.class, int.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if((boolean)value){
                return 1;
            }else{
                return 0;
            }
        }
    },
    boolean2Long(boolean.class, Long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if((boolean)value){
                return Long.valueOf(1);
            }else{
                return Long.valueOf(0);
            }
        }
    },
    boolean2long(boolean.class, long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if((boolean)value){
                return 1L;
            }else{
                return 0L;
            }
        }
    },
    boolean2Double(boolean.class, Double.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if((boolean)value){
                return Double.valueOf(1);
            }else{
                return Double.valueOf(0);
            }
        }
    },
    boolean2double(boolean.class, double.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if((boolean)value){
                return 1D;
            }else{
                return 0D;
            }
        }
    },
    boolean2Float(boolean.class, Float.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if((boolean)value){
                return Float.valueOf(1);
            }else{
                return Float.valueOf(0);
            }
        }
    },
    boolean2float(boolean.class, float.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if((boolean)value){
                return 1F;
            }else{
                return 0F;
            }
        }
    },
    boolean2Short(boolean.class, Short.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if((boolean)value){
                return Short.valueOf("1");
            }else{
                return Short.valueOf("0");
            }
        }
    },
    boolean2short(boolean.class, short.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if((boolean)value){
                return (short)1;
            }else{
                return (short)0;
            }
        }
    },
    boolean2Byte(boolean.class, Byte.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if((boolean)value){
                return Byte.valueOf("1");
            }else{
                return Byte.valueOf("0");
            }
        }
    },
    boolean2byte(boolean.class, byte.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if((boolean)value){
                return (byte)1;
            }else{
                return (byte)0;
            }
        }
    },
    boolean2Character(boolean.class, Character.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if((boolean)value){
                return Character.valueOf('1');
            }else{
                return Character.valueOf('0');
            }
        }
    },
    boolean2char(boolean.class, char.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if((boolean)value){
                return '1';
            }else{
                return '0';
            }
        }
    },
    BigDecimal2String(BigDecimal.class, String.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return value.toString();
        }
    },
    BigDecimal2Boolean(BigDecimal.class, Boolean.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if(value.equals("0")){
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }
    },
    BigDecimal2boolean(BigDecimal.class, boolean.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if(value.equals("0")){
                return false;
            }
            return true;
        }
    },
    BigDecimal2BigInteger(BigDecimal.class, BigInteger.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((BigDecimal)value).toBigInteger();
        }
    },
    BigDecimal2Integer(BigDecimal.class, Integer.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return new Integer(((BigDecimal)value).intValue());
        }
    },
    BigDecimal2int(BigDecimal.class, int.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((BigDecimal)value).intValue();
        }
    },
    BigDecimal2Long(BigDecimal.class, Long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return new Long(((BigDecimal)value).longValue());
        }
    },
    BigDecimal2long(BigDecimal.class, long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((BigDecimal)value).longValue();
        }
    },
    BigDecimal2Double(BigDecimal.class, Double.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return new Double(((BigDecimal)value).doubleValue());
        }
    },
    BigDecimal2double(BigDecimal.class, double.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((BigDecimal)value).doubleValue();
        }
    },
    BigDecimal2Float(BigDecimal.class, Float.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return new Float(((BigDecimal)value).floatValue());
        }
    },
    BigDecimal2float(BigDecimal.class, float.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((BigDecimal)value).floatValue();
        }
    },
    BigDecimal2Short(BigDecimal.class, Short.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return new Short(((BigDecimal)value).shortValue());
        }
    },
    BigDecimal2short(BigDecimal.class, short.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((BigDecimal)value).shortValue();
        }
    },
    BigDecimal2Byte(BigDecimal.class, Byte.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return new Byte(((BigDecimal)value).byteValue());
        }
    },
    BigDecimal2byte(BigDecimal.class, byte.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((BigDecimal)value).byteValue();
        }
    },
    BigDecimal2Character(BigDecimal.class, Character.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Character.valueOf(Character.forDigit(((BigDecimal)value).intValue(),10));
        }
    },
    BigDecimal2char(BigDecimal.class, char.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Character.forDigit(((BigDecimal)value).intValue(),10);
        }
    },
    BigInteger2String(BigInteger.class, String.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return value.toString();
        }
    },
    BigInteger2Boolean(BigInteger.class, Boolean.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if(value.equals("0")){
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }
    },
    BigInteger2boolean(BigInteger.class, boolean.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if(value.equals("0")){
                return false;
            }
            return true;
        }
    },
    BigInteger2BigDecimal(BigInteger.class, BigDecimal.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return new BigDecimal((BigInteger)value,0);
        }
    },
    BigInteger2Integer(BigInteger.class, Integer.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Integer.valueOf(((BigInteger)value).intValue());
        }
    },
    BigInteger2int(BigInteger.class, int.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((BigInteger)value).intValue();
        }
    },
    BigInteger2Long(BigInteger.class, Long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Long.valueOf(((BigInteger)value).longValue());
        }
    },
    BigInteger2long(BigInteger.class, long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((BigInteger)value).longValue();
        }
    },
    BigInteger2Double(BigInteger.class, Double.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Double.valueOf(((BigInteger)value).doubleValue());
        }
    },
    BigInteger2double(BigInteger.class, double.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((BigInteger)value).doubleValue();
        }
    },
    BigInteger2Float(BigInteger.class, Float.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Float.valueOf(((BigInteger)value).floatValue());
        }
    },
    BigInteger2float(BigInteger.class, float.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((BigInteger)value).floatValue();
        }
    },
    BigInteger2Short(BigInteger.class, Short.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Short.valueOf(((BigInteger)value).shortValue());
        }
    },
    BigInteger2short(BigInteger.class, short.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((BigInteger)value).shortValue();
        }
    },
    BigInteger2Byte(BigInteger.class, Byte.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Byte.valueOf(((BigInteger)value).byteValue());
        }
    },
    BigInteger2byte(BigInteger.class, byte.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((BigInteger)value).byteValue();
        }
    },
    BigInteger2Character(BigInteger.class, Character.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Character.valueOf(Character.forDigit(((BigInteger)value).intValue(),10));
        }
    },
    BigInteger2char(BigInteger.class, char.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Character.forDigit(((BigInteger)value).intValue(),10);
        }
    },
    Integer2String(Integer.class, String.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return (char)((BigInteger)value).intValue();
        }
    },
    Integer2Boolean(Integer.class, Boolean.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if("0".equals(value)){
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }
    },
    Integer2boolean(Integer.class, boolean.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if("0".equals(value)){
                return false;
            }
            return true;
        }
    },
    Integer2BigDecimal(Integer.class, BigDecimal.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return new BigDecimal((Integer)value);
        }
    },
    Integer2BigInteger(Integer.class, BigInteger.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return BigInteger.valueOf((Integer)value);
        }
    },
    Integer2int(Integer.class, int.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((Integer)value).intValue();
        }
    },
    Integer2Long(Integer.class, Long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Long.valueOf(((Integer)value).longValue());
        }
    },
    Integer2long(Integer.class, long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((Integer)value).longValue();
        }
    },
    Integer2Double(Integer.class, Double.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Double.valueOf(((Integer)value).doubleValue());
        }
    },
    Integer2double(Integer.class, double.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((Integer)value).doubleValue();
        }
    },
    Integer2Float(Integer.class, Float.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Float.valueOf(((Integer)value).floatValue());
        }
    },
    Integer2float(Integer.class, float.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((Integer)value).floatValue();
        }
    },
    Integer2Short(Integer.class, Short.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Short.valueOf(((Integer)value).shortValue());
        }
    },
    Integer2short(Integer.class, short.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((Integer)value).shortValue();
        }
    },
    Integer2Byte(Integer.class, Byte.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Byte.valueOf(((Integer)value).byteValue());
        }
    },
    Integer2byte(Integer.class, byte.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((Integer)value).byteValue();
        }
    },
    Integer2Character(Integer.class, Character.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Character.valueOf(Character.forDigit(((Integer)value).intValue(),10));
        }
    },
    Integer2char(Integer.class, char.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Character.forDigit(((Integer)value).intValue(),10);
        }
    },
    int2String(int.class, String.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return value.toString();
        }
    },
    int2Boolean(int.class, Boolean.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if("0".equals(value)){
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }
    },
    int2boolean(int.class, boolean.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if("0".equals(value)){
                return false;
            }
            return true;
        }
    },
    int2BigDecimal(int.class, BigDecimal.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return BigDecimal.valueOf((int)value);
        }
    },
    int2BigInteger(int.class, BigInteger.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return BigInteger.valueOf((int)value);
        }
    },
    int2Integer(int.class, Integer.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Integer.valueOf((int)value);
        }
    },
    int2Long(int.class, Long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Long.valueOf(((int)value));
        }
    },
    int2long(int.class, long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Long.valueOf(((int)value)).longValue();
        }
    },
    int2Double(int.class, Double.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Double.valueOf(((int)value));
        }
    },
    int2double(int.class, double.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Double.valueOf(((int)value)).doubleValue();
        }
    },
    int2Float(int.class, Float.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Float.valueOf((int)value);
        }
    },
    int2float(int.class, float.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Float.valueOf((int)value).floatValue();
        }
    },
    int2Short(int.class, Short.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Short.valueOf(Integer.valueOf((int)value).shortValue());
        }
    },
    int2short(int.class, short.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Integer.valueOf((int)value).shortValue();
        }
    },
    int2Byte(int.class, Byte.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Byte.valueOf(Integer.valueOf((int)value).byteValue());
        }
    },
    int2byte(int.class, byte.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Integer.valueOf((int)value).byteValue();
        }
    },
    int2Character(int.class, Character.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Character.valueOf(Character.forDigit(((int)value),10));
        }
    },
    int2char(int.class, char.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Character.forDigit(((int)value),10);
        }
    },
    Long2String(Long.class, String.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return value.toString();
        }
    },
    Long2Boolean(Long.class, Boolean.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if("0".equals(value)){
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }
    },
    Long2boolean(Long.class, boolean.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if("0".equals(value)){
                return false;
            }
            return true;
        }
    },
    Long2BigDecimal(Long.class, BigDecimal.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return BigDecimal.valueOf((Long)value);
        }
    },
    Long2BigInteger(Long.class, BigInteger.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return BigInteger.valueOf((Long)value);
        }
    },
    Long2Integer(Long.class, Integer.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Integer.valueOf(((Long)value).intValue());
        }
    },
    Long2int(Long.class, int.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((Long)value).intValue();
        }
    },
    Long2long(Long.class, long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((Long)value).longValue();
        }
    },
    Long2Double(Long.class, Double.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Double.valueOf(((Long)value).doubleValue());
        }
    },
    Long2double(Long.class, double.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((Long)value).doubleValue();
        }
    },
    Long2Float(Long.class, Float.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Float.valueOf(((Long)value).floatValue());
        }
    },
    Long2float(Long.class, float.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((Long)value).floatValue();
        }
    },
    Long2Short(Long.class, Short.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Short.valueOf(((Long)value).shortValue());
        }
    },
    Long2short(Long.class, short.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((Long)value).shortValue();
        }
    },
    Long2Byte(Long.class, Byte.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Byte.valueOf(((Long)value).byteValue());
        }
    },
    Long2byte(Long.class, byte.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((Long)value).byteValue();
        }
    },
    Long2Character(Long.class, Character.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Character.valueOf(Character.forDigit(((Long)value).intValue(),10));
        }
    },
    Long2char(Long.class, char.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Character.forDigit(((Long)value).intValue(),10);
        }
    },
    long2String(long.class, String.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return value.toString();
        }
    },
    long2Boolean(long.class, Boolean.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if("0".equals(value)){
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }
    },
    long2boolean(long.class, boolean.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if("0".equals(value)){
                return false;
            }
            return true;
        }
    },
    long2BigDecimal(long.class, BigDecimal.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return BigDecimal.valueOf((long)value);
        }
    },
    long2BigInteger(long.class, BigInteger.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return BigInteger.valueOf((long)value);
        }
    },
    long2Integer(long.class, Integer.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Integer.valueOf((Long.valueOf((long)value).intValue()));
        }
    },
    long2int(long.class, int.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return (Long.valueOf((long)value)).intValue();
        }
    },
    long2Long(long.class, Long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Long.valueOf((long)value);
        }
    },
    long2Double(long.class, Double.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Double.valueOf((long)value);
        }
    },
    long2double(long.class, double.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Double.valueOf((long)value).doubleValue();
        }
    },
    long2Float(long.class, Float.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Float.valueOf((long)value);
        }
    },
    long2float(long.class, float.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Float.valueOf((long)value).floatValue();
        }
    },
    long2Short(long.class, Short.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Short.valueOf(Long.valueOf((long)value).shortValue());
        }
    },
    long2short(long.class, short.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Long.valueOf((long)value).shortValue();
        }
    },
    long2Byte(long.class, Byte.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Byte.valueOf(Long.valueOf((long)value).byteValue());
        }
    },
    long2byte(long.class, byte.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Long.valueOf((long)value).byteValue();
        }
    },
    long2Character(long.class, Character.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Character.valueOf(Character.forDigit(Long.valueOf((long)value).intValue(),10));
        }
    },
    long2char(long.class, char.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Character.forDigit(Long.valueOf((long)value).intValue(),10);
        }
    },
    Double2String(Double.class, String.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return value.toString();
        }
    },
    Double2Boolean(Double.class, Boolean.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if((Double)value == 0){
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }
    },
    Double2boolean(Double.class, boolean.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if((Double)value == 0){
                return false;
            }
            return true;
        }
    },
    Double2BigDecimal(Double.class, BigDecimal.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return BigDecimal.valueOf((Double)value);
        }
    },
    Double2BigInteger(Double.class, BigInteger.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return BigInteger.valueOf(((Double)value).longValue());
        }
    },
    Double2Integer(Double.class, Integer.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Integer.valueOf(((Double)value).intValue());
        }
    },
    Double2int(Double.class, int.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((Double)value).intValue();
        }
    },
    Double2Long(Double.class, Long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Long.valueOf(((Double)value).longValue());
        }
    },
    Double2long(Double.class, long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((Double)value).longValue();
        }
    },
    Double2double(Double.class, double.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
           return ((Double)value).doubleValue();
        }
    },
    Double2Float(Double.class, Float.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Float.valueOf(((Double)value).floatValue());
        }
    },
    Double2float(Double.class, float.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((Double)value).floatValue();
        }
    },
    Double2Short(Double.class, Short.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Short.valueOf(((Double)value).shortValue());
        }
    },
    Double2short(Double.class, short.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((Double)value).shortValue();
        }
    },
    Double2Byte(Double.class, Byte.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
           return Byte.valueOf(((Double)value).byteValue());
        }
    },
    Double2byte(Double.class, byte.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((Double)value).byteValue();
        }
    },
    Double2Character(Double.class, Character.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Character.valueOf(Character.forDigit(((Double)value).intValue(),10));
        }
    },
    Double2char(Double.class, char.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Character.forDigit(((Double)value).intValue(),10);
        }
    },
    double2String(double.class, String.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return value.toString();
        }
    },
    double2Boolean(double.class, Boolean.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if((double)value == 0){
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }
    },
    double2boolean(double.class, boolean.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if((double)value == 0){
                return false;
            }
            return true;
        }
    },
    double2BigDecimal(double.class, BigDecimal.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return BigDecimal.valueOf((double)value);
        }
    },
    double2BigInteger(double.class, BigInteger.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return BigInteger.valueOf(Double.valueOf((double)value).longValue());
        }
    },
    double2Integer(double.class, Integer.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Integer.valueOf(Double.valueOf((double)value).intValue());
        }
    },
    double2int(double.class, int.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Double.valueOf((double)value).intValue();
        }
    },
    double2Long(double.class, Long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Long.valueOf(Double.valueOf((double)value).longValue());
        }
    },
    double2long(double.class, long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Double.valueOf((double)value).longValue();
        }
    },
    double2Double(double.class, Double.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Double.valueOf((double)value);
        }
    },
    double2Float(double.class, Float.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Float.valueOf(Double.valueOf((double)value).floatValue());
        }
    },
    double2float(double.class, float.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Double.valueOf((double)value).floatValue();
        }
    },
    double2Short(double.class, Short.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Short.valueOf(Double.valueOf((double)value).shortValue());
        }
    },
    double2short(double.class, short.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Double.valueOf((double)value).shortValue();
        }
    },
    double2Byte(double.class, Byte.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Byte.valueOf(Double.valueOf((double)value).byteValue());
        }
    },
    double2byte(double.class, byte.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Double.valueOf((double)value).byteValue();
        }
    },
    double2Character(double.class, Character.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Character.valueOf(Character.forDigit(((Double)value).intValue(),10));
        }
    },
    double2char(double.class, char.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Character.forDigit(((Double)value).intValue(),10);
        }
    },
    Float2String(Float.class, String.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return value.toString();
        }
    },
    Float2Boolean(Float.class, Boolean.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if((Float)value == 0){
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }
    },
    Float2boolean(Float.class, boolean.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if((Float)value == 0){
                return false;
            }
            return true;
        }
    },
    Float2BigDecimal(Float.class, BigDecimal.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return BigDecimal.valueOf((Float)value);
        }
    },
    Float2BigInteger(Float.class, BigInteger.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return BigInteger.valueOf(((Float)value).longValue());
        }
    },
    Float2Integer(Float.class, Integer.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Integer.valueOf(((Float)value).intValue());
        }
    },
    Float2int(Float.class, int.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((Float)value).intValue();
        }
    },
    Float2Long(Float.class, Long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Long.valueOf(((Float)value).longValue());
        }
    },
    Float2long(Float.class, long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((Float)value).longValue();
        }
    },
    Float2Double(Float.class, Double.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Double.valueOf(((Float)value).doubleValue());
        }
    },
    Float2double(Float.class, double.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((Float)value).doubleValue();
        }
    },
    Float2float(Float.class, float.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((Float)value).floatValue();
        }
    },
    Float2Short(Float.class, Short.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Short.valueOf(((Float)value).shortValue());
        }
    },
    Float2short(Float.class, short.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((Float)value).shortValue();
        }
    },
    Float2Byte(Float.class, Byte.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Byte.valueOf(((Float)value).byteValue());
        }
    },
    Float2byte(Float.class, byte.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((Float)value).byteValue();
        }
    },
    Float2Character(Float.class, Character.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Character.valueOf(Character.forDigit(((Float)value).intValue(),10));
        }
    },
    Float2char(Float.class, char.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Character.forDigit(((Float)value).intValue(),10);
        }
    },
    float2String(float.class, String.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return value.toString();
        }
    },
    float2Boolean(float.class, Boolean.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if((float)value == 0){
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }
    },
    float2boolean(float.class, boolean.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if((float)value == 0){
                return false;
            }
            return true;
        }
    },
    float2BigDecimal(float.class, BigDecimal.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return BigDecimal.valueOf((float)value);
        }
    },
    float2BigInteger(float.class, BigInteger.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return BigInteger.valueOf(Float.valueOf((float)value).longValue());
        }
    },
    float2Integer(float.class, Integer.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Integer.valueOf(Float.valueOf((float)value).intValue());
        }
    },
    float2int(float.class, int.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Float.valueOf((float)value).intValue();
        }
    },
    float2Long(float.class, Long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Long.valueOf(Float.valueOf((float)value).longValue());
        }
    },
    float2long(float.class, long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Float.valueOf((float)value).longValue();
        }
    },
    float2Double(float.class, Double.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Double.valueOf(Float.valueOf((float)value).doubleValue());
        }
    },
    float2double(float.class, double.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Float.valueOf((float)value).doubleValue();
        }
    },
    float2Float(float.class, Float.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Float.valueOf((float)value);
        }
    },
    float2Short(float.class, Short.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Short.valueOf(Float.valueOf((float)value).shortValue());
        }
    },
    float2short(float.class, short.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Float.valueOf((float)value).shortValue();
        }
    },
    float2Byte(float.class, Byte.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Byte.valueOf(Float.valueOf((float)value).byteValue());
        }
    },
    float2byte(float.class, byte.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Float.valueOf((float)value).byteValue();
        }
    },
    float2Character(float.class, Character.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Character.valueOf(Character.forDigit(Float.valueOf((float)value).intValue(),10));
        }
    },
    float2char(float.class, char.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Character.forDigit(Float.valueOf((float)value).intValue(),10);
        }
    },
    Short2String(Short.class, String.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return value.toString();
        }
    },
    Short2Boolean(Short.class, Boolean.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if(0 == (Short)value){
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }
    },
    Short2boolean(Short.class, boolean.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if(0 == (Short)value){
                return false;
            }
            return true;
        }
    },
    Short2BigDecimal(Short.class, BigDecimal.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return BigDecimal.valueOf((Short)value);
        }
    },
    Short2BigInteger(Short.class, BigInteger.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return BigInteger.valueOf((Short)value);
        }
    },
    Short2Integer(Short.class, Integer.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Integer.valueOf((Short)value);
        }
    },
    Short2int(Short.class, int.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((Short)value).intValue();
        }
    },
    Short2Long(Short.class, Long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Long.valueOf(((Short)value).longValue());
        }
    },
    Short2long(Short.class, long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((Short)value).longValue();
        }
    },
    Short2Double(Short.class, Double.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Double.valueOf(((Short)value).doubleValue());
        }
    },
    Short2double(Short.class, double.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((Short)value).doubleValue();
        }
    },
    Short2Float(Short.class, Float.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Float.valueOf(((Short)value).floatValue());
        }
    },
    Short2float(Short.class, float.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((Short)value).floatValue();
        }
    },
    Short2short(Short.class, short.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Short.valueOf(((Short)value).shortValue());
        }
    },
    Short2Byte(Short.class, Byte.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Byte.valueOf(((Short)value).byteValue());
        }
    },
    Short2byte(Short.class, byte.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((Short)value).byteValue();
        }
    },
    Short2Character(Short.class, Character.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Character.valueOf(Character.forDigit(((Short)value).intValue(),10));
        }
    },
    Short2char(Short.class, char.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Character.forDigit(((Short)value).intValue(),10);
        }
    },
    short2String(short.class, String.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return value.toString();
        }
    },
    short2Boolean(short.class, Boolean.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if (0 == (short)value) {
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }
    },
    short2boolean(short.class, boolean.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if (0 == (short)value) {
                return false;
            }
            return true;
        }
    },
    short2BigDecimal(short.class, BigDecimal.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return BigDecimal.valueOf((short)value);
        }
    },
    short2BigInteger(short.class, BigInteger.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return BigInteger.valueOf((short)value);
        }
    },
    short2Integer(short.class, Integer.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return new Integer((short)value);
        }
    },
    short2int(short.class, int.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return new Integer((short)value).intValue();
        }
    },
    short2Long(short.class, Long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
           return Long.valueOf((short)value);
        }
    },
    short2long(short.class, long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Long.valueOf((short)value).longValue();
        }
    },
    short2Double(short.class, Double.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Double.valueOf((short)value);
        }
    },
    short2double(short.class, double.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Double.valueOf((short)value).doubleValue();
        }
    },
    short2Float(short.class, Float.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Float.valueOf((short)value);
        }
    },
    short2float(short.class, float.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Float.valueOf((short)value).floatValue();
        }
    },
    short2Short(short.class, Short.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Short.valueOf((short)value);
        }
    },
    short2Byte(short.class, Byte.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Byte.valueOf(((Short)value).byteValue());
        }
    },
    short2byte(short.class, byte.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((Short)value).byteValue();
        }
    },
    short2Character(short.class, Character.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Character.valueOf(Character.forDigit(((short)value),10));
        }
    },
    short2char(short.class, char.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Character.forDigit(((short)value),10);
        }
    },
    Byte2String(Byte.class, String.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return value.toString();
        }
    },
    Byte2Boolean(Byte.class, Boolean.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if(0 == (Byte)value){
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }
    },
    Byte2boolean(Byte.class, boolean.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if(0 == (Byte)value){
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }
    },
    Byte2BigDecimal(Byte.class, BigDecimal.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return BigDecimal.valueOf((Byte)value);
        }
    },
    Byte2BigInteger(Byte.class, BigInteger.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return BigInteger.valueOf((Byte)value);
        }
    },
    Byte2Integer(Byte.class, Integer.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Integer.valueOf((Byte)value);
        }
    },
    Byte2int(Byte.class, int.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((Byte)value).intValue();
        }
    },
    Byte2Long(Byte.class, Long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Long.valueOf((Byte)value);
        }
    },
    Byte2long(Byte.class, long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((Byte)value).longValue();
        }
    },
    Byte2Double(Byte.class, Double.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Double.valueOf((Byte)value);
        }
    },
    Byte2double(Byte.class, double.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((Byte)value).doubleValue();
        }
    },
    Byte2Float(Byte.class, Float.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Float.valueOf((Byte)value);
        }
    },
    Byte2float(Byte.class, float.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((Byte)value).floatValue();
        }
    },
    Byte2Short(Byte.class, Short.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Short.valueOf((Byte)value);
        }
    },
    Byte2short(Byte.class, short.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((Byte)value).shortValue();
        }
    },
    Byte2byte(Byte.class, byte.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((Byte)value).byteValue();
        }
    },
    Byte2Character(Byte.class, Character.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Character.valueOf(Character.forDigit((Byte)value,10));
        }
    },
    Byte2char(Byte.class, char.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Character.forDigit((Byte)value,10);
        }
    },
    byte2String(byte.class, String.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return value.toString();
        }
    },
    byte2Boolean(byte.class, Boolean.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if(0 == (byte)value){
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }
    },
    byte2boolean(byte.class, boolean.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if(0 == (byte)value){
                return false;
            }
            return true;
        }
    },
    byte2BigDecimal(byte.class, BigDecimal.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return BigDecimal.valueOf((byte)value);
        }
    },
    byte2BigInteger(byte.class, BigInteger.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return BigInteger.valueOf((byte)value);
        }
    },
    byte2Integer(byte.class, Integer.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Integer.valueOf((byte)value);
        }
    },
    byte2int(byte.class, int.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Integer.valueOf((byte)value).intValue();
        }
    },
    byte2Long(byte.class, Long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Long.valueOf((byte)value);
        }
    },
    byte2long(byte.class, long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Long.valueOf((byte)value).longValue();
        }
    },
    byte2Double(byte.class, Double.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Double.valueOf((byte)value);
        }
    },
    byte2double(byte.class, double.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Double.valueOf((byte)value).doubleValue();
        }
    },
    byte2Float(byte.class, Float.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Float.valueOf((byte)value);
        }
    },
    byte2float(byte.class, float.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Float.valueOf((byte)value).floatValue();
        }
    },
    byte2Short(byte.class, Short.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Short.valueOf((byte)value);
        }
    },
    byte2short(byte.class, short.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Short.valueOf((byte)value).shortValue();
        }
    },
    byte2Byte(byte.class, Byte.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Byte.valueOf((byte)value);
        }
    },
    byte2Character(byte.class, Character.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Character.valueOf(Character.forDigit((byte)value,10));
        }
    },
    byte2char(byte.class, char.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Character.forDigit((byte)value,10);
        }
    },
    Character2String(Character.class, String.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return value.toString();
        }
    },
    Character2Boolean(Character.class, Boolean.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if(0 == (Character)value){
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }
    },
    Character2boolean(Character.class, boolean.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if(0 == (Character)value){
                return false;
            }
            return true;
        }
    },
    Character2BigDecimal(Character.class, BigDecimal.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return BigDecimal.valueOf((Character)value);
        }
    },
    Character2BigInteger(Character.class, BigInteger.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return BigInteger.valueOf((Character)value);
        }
    },
    Character2Integer(Character.class, Integer.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Integer.valueOf((Character)value);
        }
    },
    Character2int(Character.class, int.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Integer.valueOf((Character)value).intValue();
        }
    },
    Character2Long(Character.class, Long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Long.valueOf((Character)value);
        }
    },
    Character2long(Character.class, long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Long.valueOf((Character)value).longValue();
        }
    },
    Character2Double(Character.class, Double.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Double.valueOf((Character)value);
        }
    },
    Character2double(Character.class, double.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Double.valueOf((Character)value).doubleValue();
        }
    },
    Character2Float(Character.class, Float.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Float.valueOf((Character)value);
        }
    },
    Character2float(Character.class, float.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Float.valueOf((Character)value).floatValue();
        }
    },
    Character2Short(Character.class, Short.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Short.valueOf(Integer.valueOf((Character)value).shortValue());
        }
    },
    Character2short(Character.class, short.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Integer.valueOf((Character)value).shortValue();
        }
    },
    Character2Byte(Character.class, Byte.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Byte.valueOf(Integer.valueOf((Character)value).byteValue());
        }
    },
    Character2byte(Character.class, byte.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Integer.valueOf((Character)value).byteValue();
        }
    },
    Character2char(Character.class, char.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((Character)value).charValue();
        }
    },
    char2String(char.class, String.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return value.toString();
        }
    },
    char2Boolean(char.class, Boolean.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if(0 == (char)value){
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }
    },
    char2boolean(char.class, boolean.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            if(0 == (char)value){
                return false;
            }
            return true;
        }
    },
    char2BigDecimal(char.class, BigDecimal.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return BigDecimal.valueOf((char)value);
        }
    },
    char2BigInteger(char.class, BigInteger.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return BigDecimal.valueOf((char)value);
        }
    },
    char2Integer(char.class, Integer.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Integer.valueOf((char)value);
        }
    },
    char2int(char.class, int.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Integer.valueOf((char)value).intValue();
        }
    },
    char2Long(char.class, Long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Long.valueOf((char)value);
        }
    },
    char2long(char.class, long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Long.valueOf((char)value).longValue();
        }
    },
    char2Double(char.class, Double.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Double.valueOf((char)value);
        }
    },
    char2double(char.class, double.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Double.valueOf((char)value).doubleValue();
        }
    },
    char2Float(char.class, Float.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Float.valueOf((char)value);
        }
    },
    char2float(char.class, float.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Float.valueOf((char)value).floatValue();
        }
    },
    char2Short(char.class, Short.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Short.valueOf(Integer.valueOf((char)value).shortValue());
        }
    },
    char2short(char.class, short.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Integer.valueOf((char)value).shortValue();
        }
    },
    char2Byte(char.class, Byte.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Byte.valueOf(Integer.valueOf((char)value).byteValue());
        }
    },
    char2byte(char.class, byte.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Integer.valueOf((char)value).byteValue();
        }
    },
    char2Character(char.class, Character.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Character.valueOf((char)value);
        }
    },





    /* *****************************************************************************************************************
     *                                                  date
     * *****************************************************************************************************************/
    String2java_util_Date(String.class, java.util.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return DateUtil.parse(value);
        }
    },
    String2java_sql_Date(String.class, java.sql.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return DateUtil.sqlDate(DateUtil.parse(value));
        }
    },
    String2java_sql_Time(String.class, java.sql.Time.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return DateUtil.sqlTime(DateUtil.parse(value));
        }
    },
    String2java_sql_Timestamp(String.class, java.sql.Timestamp.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return DateUtil.sqlTimestamp(DateUtil.parse(value));
        }
    },
    String2java_time_Year(String.class, java.time.Year.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            Year year = Year.of(DateUtil.year(date));
            return year;
        }
    },
    String2java_time_YearMonth(String.class, java.time.YearMonth.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            YearMonth yearMonth = YearMonth.of(DateUtil.year(date), DateUtil.month(date));
            return yearMonth;
        }
    },
    String2java_time_Month(String.class, java.time.Month.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            Month month = Month.of(DateUtil.month(date));
            return month;
        }
    },
    String2java_time_LocalDate(String.class, java.time.LocalDate.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return DateUtil.localDate(DateUtil.parse(value));
        }
    },
    String2java_time_LocalTime(String.class, java.time.LocalTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return DateUtil.localTime(DateUtil.parse(value));
        }
    },
    String2java_time_LocalDateTime(String.class, java.time.LocalDateTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return DateUtil.localDateTime(DateUtil.parse(value));
        }
    },
    Long2java_util_Date(Long.class, java.util.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return DateUtil.parse(value);
        }
    },
    Long2java_sql_Date(Long.class, java.sql.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return DateUtil.sqlDate(DateUtil.parse(value));
        }
    },
    Long2java_sql_Time(Long.class, java.sql.Time.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return DateUtil.sqlTime(DateUtil.parse(value));
        }
    },
    Long2java_sql_Timestamp(Long.class, java.sql.Timestamp.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return DateUtil.sqlTimestamp(DateUtil.parse(value));
        }
    },
    Long2java_time_Year(Long.class, java.time.Year.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Year.of(DateUtil.year(DateUtil.parse(value)));
        }
    },
    Long2java_time_YearMonth(Long.class, java.time.YearMonth.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            YearMonth yearMonth = YearMonth.of(DateUtil.year(date), DateUtil.month(date));
            return yearMonth;
        }
    },
    Long2java_time_Month(Long.class, java.time.Month.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            Month month = Month.of(DateUtil.month(date));
            return month;
        }
    },
    Long2java_time_LocalDate(Long.class, java.time.LocalDate.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return DateUtil.localDate(DateUtil.parse(value));
        }
    },
    Long2java_time_LocalTime(Long.class, java.time.LocalTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return DateUtil.localTime(DateUtil.parse(value));
        }
    },
    Long2java_time_LocalDateTime(Long.class, java.time.LocalDateTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return DateUtil.localDateTime(DateUtil.parse(value));
        }
    },
    long2java_util_Date(long.class, java.util.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return DateUtil.parse(value);
        }
    },
    long2java_sql_Date(long.class, java.sql.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return DateUtil.sqlDate(DateUtil.parse(value));
        }
    },
    long2java_sql_Time(long.class, java.sql.Time.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return DateUtil.sqlTime(DateUtil.parse(value));
        }
    },
    long2java_sql_Timestamp(long.class, java.sql.Timestamp.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return DateUtil.sqlTimestamp(DateUtil.parse(value));
        }
    },
    long2java_time_Year(long.class, java.time.Year.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            Year year = Year.of(DateUtil.year(date));
            return year;
        }
    },
    long2java_time_YearMonth(long.class, java.time.YearMonth.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            YearMonth yearMonth = YearMonth.of(DateUtil.year(date), DateUtil.month(date));
            return yearMonth;
        }
    },
    long2java_time_Month(long.class, java.time.Month.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            Month month = Month.of(DateUtil.month(date));
            return month;
        }
    },
    long2java_time_LocalDate(long.class, java.time.LocalDate.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return DateUtil.localDate(DateUtil.parse(value));
        }
    },
    long2java_time_LocalTime(long.class, java.time.LocalTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return DateUtil.localTime(DateUtil.parse(value));
        }
    },
    long2java_time_LocalDateTime(long.class, java.time.LocalDateTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return DateUtil.localDateTime(DateUtil.parse(value));
        }
    },
    java_util_Date2String(java.util.Date.class, String.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return DateUtil.format((java.util.Date)value);
        }
    },
    java_util_Date2Long(java.util.Date.class, Long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return Long.valueOf(((java.util.Date)value).getTime());
        }
    },
    java_util_Date2long(java.util.Date.class, long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((java.util.Date)value).getTime();
        }
    },
    java_util_Date2java_sql_Date(java.util.Date.class, java.sql.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return DateUtil.sqlDate((java.util.Date)value);
        }
    },
    java_util_Date2java_sql_Time(java.util.Date.class, java.sql.Time.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return DateUtil.sqlTime((java.util.Date)value);
        }
    },
    java_util_Date2java_sql_Timestamp(java.util.Date.class, java.sql.Timestamp.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return DateUtil.sqlTimestamp((java.util.Date)value);
        }
    },
    java_util_Date2java_time_Year(java.util.Date.class, java.time.Year.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_util_Date2java_time_YearMonth(java.util.Date.class, java.time.YearMonth.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_util_Date2java_time_Month(java.util.Date.class, java.time.Month.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_util_Date2java_time_LocalDate(java.util.Date.class, java.time.LocalDate.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_util_Date2java_time_LocalTime(java.util.Date.class, java.time.LocalTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_util_Date2java_time_LocalDateTime(java.util.Date.class, java.time.LocalDateTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_sql_Date2String(java.sql.Date.class, String.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_sql_Date2Long(java.sql.Date.class, Long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_sql_Date2long(java.sql.Date.class, long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_sql_Date2java_util_Date(java.sql.Date.class, java.util.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_sql_Date2java_sql_Time(java.sql.Date.class, java.sql.Time.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_sql_Date2java_sql_Timestamp(java.sql.Date.class, java.sql.Timestamp.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_sql_Date2java_time_Year(java.sql.Date.class, java.time.Year.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_sql_Date2java_time_YearMonth(java.sql.Date.class, java.time.YearMonth.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_sql_Date2java_time_Month(java.sql.Date.class, java.time.Month.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_sql_Date2java_time_LocalDate(java.sql.Date.class, java.time.LocalDate.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_sql_Date2java_time_LocalTime(java.sql.Date.class, java.time.LocalTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_sql_Date2java_time_LocalDateTime(java.sql.Date.class, java.time.LocalDateTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_sql_Time2String(java.sql.Time.class, String.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_sql_Time2Long(java.sql.Time.class, Long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_sql_Time2long(java.sql.Time.class, long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_sql_Time2java_util_Date(java.sql.Time.class, java.util.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_sql_Time2java_sql_Date(java.sql.Time.class, java.sql.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_sql_Time2java_sql_Timestamp(java.sql.Time.class, java.sql.Timestamp.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_sql_Time2java_time_Year(java.sql.Time.class, java.time.Year.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_sql_Time2java_time_YearMonth(java.sql.Time.class, java.time.YearMonth.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_sql_Time2java_time_Month(java.sql.Time.class, java.time.Month.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_sql_Time2java_time_LocalDate(java.sql.Time.class, java.time.LocalDate.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_sql_Time2java_time_LocalTime(java.sql.Time.class, java.time.LocalTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_sql_Time2java_time_LocalDateTime(java.sql.Time.class, java.time.LocalDateTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_sql_Timestamp2String(java.sql.Timestamp.class, String.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_sql_Timestamp2Long(java.sql.Timestamp.class, Long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_sql_Timestamp2long(java.sql.Timestamp.class, long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_sql_Timestamp2java_util_Date(java.sql.Timestamp.class, java.util.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_sql_Timestamp2java_sql_Date(java.sql.Timestamp.class, java.sql.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_sql_Timestamp2java_sql_Time(java.sql.Timestamp.class, java.sql.Time.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_sql_Timestamp2java_time_Year(java.sql.Timestamp.class, java.time.Year.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_sql_Timestamp2java_time_YearMonth(java.sql.Timestamp.class, java.time.YearMonth.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_sql_Timestamp2java_time_Month(java.sql.Timestamp.class, java.time.Month.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_sql_Timestamp2java_time_LocalDate(java.sql.Timestamp.class, java.time.LocalDate.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_sql_Timestamp2java_time_LocalTime(java.sql.Timestamp.class, java.time.LocalTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_sql_Timestamp2java_time_LocalDateTime(java.sql.Timestamp.class, java.time.LocalDateTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_Year2String(java.time.Year.class, String.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_Year2Long(java.time.Year.class, Long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_Year2long(java.time.Year.class, long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_Year2java_util_Date(java.time.Year.class, java.util.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_Year2java_sql_Date(java.time.Year.class, java.sql.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_Year2java_sql_Time(java.time.Year.class, java.sql.Time.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_Year2java_sql_Timestamp(java.time.Year.class, java.sql.Timestamp.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_Year2java_time_YearMonth(java.time.Year.class, java.time.YearMonth.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_Year2java_time_Month(java.time.Year.class, java.time.Month.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_Year2java_time_LocalDate(java.time.Year.class, java.time.LocalDate.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_Year2java_time_LocalTime(java.time.Year.class, java.time.LocalTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_Year2java_time_LocalDateTime(java.time.Year.class, java.time.LocalDateTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_YearMonth2String(java.time.YearMonth.class, String.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_YearMonth2Long(java.time.YearMonth.class, Long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_YearMonth2long(java.time.YearMonth.class, long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_YearMonth2java_util_Date(java.time.YearMonth.class, java.util.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_YearMonth2java_sql_Date(java.time.YearMonth.class, java.sql.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_YearMonth2java_sql_Time(java.time.YearMonth.class, java.sql.Time.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_YearMonth2java_sql_Timestamp(java.time.YearMonth.class, java.sql.Timestamp.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_YearMonth2java_time_Year(java.time.YearMonth.class, java.time.Year.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_YearMonth2java_time_Month(java.time.YearMonth.class, java.time.Month.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_YearMonth2java_time_LocalDate(java.time.YearMonth.class, java.time.LocalDate.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_YearMonth2java_time_LocalTime(java.time.YearMonth.class, java.time.LocalTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_YearMonth2java_time_LocalDateTime(java.time.YearMonth.class, java.time.LocalDateTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_Month2String(java.time.Month.class, String.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_Month2Long(java.time.Month.class, Long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_Month2long(java.time.Month.class, long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_Month2java_util_Date(java.time.Month.class, java.util.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_Month2java_sql_Date(java.time.Month.class, java.sql.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_Month2java_sql_Time(java.time.Month.class, java.sql.Time.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_Month2java_sql_Timestamp(java.time.Month.class, java.sql.Timestamp.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_Month2java_time_Year(java.time.Month.class, java.time.Year.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_Month2java_time_YearMonth(java.time.Month.class, java.time.YearMonth.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_Month2java_time_LocalDate(java.time.Month.class, java.time.LocalDate.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_Month2java_time_LocalTime(java.time.Month.class, java.time.LocalTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_Month2java_time_LocalDateTime(java.time.Month.class, java.time.LocalDateTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_LocalDate2String(java.time.LocalDate.class, String.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_LocalDate2Long(java.time.LocalDate.class, Long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_LocalDate2long(java.time.LocalDate.class, long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_LocalDate2java_util_Date(java.time.LocalDate.class, java.util.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_LocalDate2java_sql_Date(java.time.LocalDate.class, java.sql.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_LocalDate2java_sql_Time(java.time.LocalDate.class, java.sql.Time.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_LocalDate2java_sql_Timestamp(java.time.LocalDate.class, java.sql.Timestamp.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_LocalDate2java_time_Year(java.time.LocalDate.class, java.time.Year.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_LocalDate2java_time_YearMonth(java.time.LocalDate.class, java.time.YearMonth.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_LocalDate2java_time_Month(java.time.LocalDate.class, java.time.Month.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_LocalDate2java_time_LocalTime(java.time.LocalDate.class, java.time.LocalTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_LocalDate2java_time_LocalDateTime(java.time.LocalDate.class, java.time.LocalDateTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_LocalTime2String(java.time.LocalTime.class, String.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_LocalTime2Long(java.time.LocalTime.class, Long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_LocalTime2long(java.time.LocalTime.class, long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_LocalTime2java_util_Date(java.time.LocalTime.class, java.util.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_LocalTime2java_sql_Date(java.time.LocalTime.class, java.sql.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_LocalTime2java_sql_Time(java.time.LocalTime.class, java.sql.Time.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_LocalTime2java_sql_Timestamp(java.time.LocalTime.class, java.sql.Timestamp.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_LocalTime2java_time_Year(java.time.LocalTime.class, java.time.Year.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_LocalTime2java_time_YearMonth(java.time.LocalTime.class, java.time.YearMonth.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_LocalTime2java_time_Month(java.time.LocalTime.class, java.time.Month.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_LocalTime2java_time_LocalDate(java.time.LocalTime.class, java.time.LocalDate.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_LocalTime2java_time_LocalDateTime(java.time.LocalTime.class, java.time.LocalDateTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_LocalDateTime2String(java.time.LocalDateTime.class, String.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_LocalDateTime2Long(java.time.LocalDateTime.class, Long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_LocalDateTime2long(java.time.LocalDateTime.class, long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_LocalDateTime2java_util_Date(java.time.LocalDateTime.class, java.util.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_LocalDateTime2java_sql_Date(java.time.LocalDateTime.class, java.sql.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_LocalDateTime2java_sql_Time(java.time.LocalDateTime.class, java.sql.Time.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_LocalDateTime2java_sql_Timestamp(java.time.LocalDateTime.class, java.sql.Timestamp.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_LocalDateTime2java_time_Year(java.time.LocalDateTime.class, java.time.Year.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_LocalDateTime2java_time_YearMonth(java.time.LocalDateTime.class, java.time.YearMonth.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_LocalDateTime2java_time_Month(java.time.LocalDateTime.class, java.time.Month.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_LocalDateTime2java_time_LocalDate(java.time.LocalDateTime.class, java.time.LocalDate.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },
    java_time_LocalDateTime2java_time_LocalTime(java.time.LocalDateTime.class, java.time.LocalTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
        }
    },

    //////////////////////////////////////////////////////////////

    javaTimeLocalDate_String(java.time.LocalDate.class, String.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.format(date,"yyyy-MM-dd");
        }
    },
    javaTimeLocalDate_javaUtilDate(java.time.LocalDate.class, java.util.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return date;
        }
    },
    javaTimeLocalDate_javaSQLDate(java.time.LocalDate.class, java.sql.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.sqlDate(date);
        }
    },
    javaTimeLocalDate_javaSQLTime(java.time.LocalDate.class, java.sql.Time.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.sqlTime(date);
        }
    },
    javaTimeLocalDate_javaSQLTimestamp(java.time.LocalDate.class, java.sql.Timestamp.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.sqlTimestamp(date);
        }
    },
    javaTimeLocalDate_javaTimeYear(java.time.LocalDate.class, java.time.Year.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            Year year = Year.of(DateUtil.year(date));
            return year;
        }
    },
    javaTimeLocalDate_javaTimeYearMonth(java.time.LocalDate.class, java.time.YearMonth.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            YearMonth yearMonth = YearMonth.of(DateUtil.year(date), DateUtil.month(date));
            return yearMonth;
        }
    },
    javaTimeLocalDate_javaTimeMonth(java.time.LocalDate.class, java.time.Month.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            Month month = Month.of(DateUtil.month(date));
            return month;
        }
    },
    javaTimeLocalDate_javaTimeLocalDate(java.time.LocalDate.class, java.time.LocalDate.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.localDate(date);
        }
    },
    javaTimeLocalDate_javaTimeLocalTime(java.time.LocalDate.class, java.time.LocalTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.localTime(date);

        }
    },

    javaTimeLocalDate_javaTimeLocalDateTime(java.time.LocalDate.class, java.time.LocalDateTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.localDateTime(date);
        }
    },

    /* *****************************************************************************************************************
     *                                                  java.time.LocalTime
     * *****************************************************************************************************************/

    javaTimeLocalTime_String(java.time.LocalTime.class, String.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.format(date,"HH:mm:ss");
        }
    },
    javaTimeLocalTime_javaUtilDate(java.time.LocalTime.class, java.util.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return date;
        }
    },
    javaTimeLocalTime_javaSQLDate(java.time.LocalTime.class, java.sql.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.sqlDate(date);
        }
    },
    javaTimeLocalTime_javaSQLTime(java.time.LocalTime.class, java.sql.Time.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.sqlTime(date);
        }
    },
    javaTimeLocalTime_javaSQLTimestamp(java.time.LocalTime.class, java.sql.Timestamp.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.sqlTimestamp(date);
        }
    },
    javaTimeLocalTime_javaTimeYear(java.time.LocalTime.class, java.time.Year.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            Year year = Year.of(DateUtil.year(date));
            return year;
        }
    },
    javaTimeLocalTime_javaTimeYearMonth(java.time.LocalTime.class, java.time.YearMonth.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            YearMonth yearMonth = YearMonth.of(DateUtil.year(date), DateUtil.month(date));
            return yearMonth;
        }
    },
    javaTimeLocalTime_javaTimeMonth(java.time.LocalTime.class, java.time.Month.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            Month month = Month.of(DateUtil.month(date));
            return month;
        }
    },
    javaTimeLocalTime_javaTimeLocalTime(java.time.LocalTime.class, java.time.LocalTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.localTime(date);

        }
    },

    javaTimeLocalTime_javaTimeLocalDateTime(java.time.LocalTime.class, java.time.LocalDateTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.localDateTime(date);
        }
    },

    /* *****************************************************************************************************************
     *                                                  java.time.LocalDateTime
     * *****************************************************************************************************************/

    javaTimeLocalDateTime_String(java.time.LocalDateTime.class, String.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.format(date);
        }
    },
    javaTimeLocalDateTime_Long(java.time.LocalDateTime.class, Long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return date.getTime();
        }
    },
    javaTimeLocalDateTime_javaUtilDate(java.time.LocalDateTime.class, java.util.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return date;
        }
    },
    javaTimeLocalDateTime_javaSQLDate(java.time.LocalDateTime.class, java.sql.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.sqlDate(date);
        }
    },
    javaTimeLocalDateTime_javaSQLTime(java.time.LocalDateTime.class, java.sql.Time.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.sqlTime(date);
        }
    },
    javaTimeLocalDateTime_javaSQLTimestamp(java.time.LocalDateTime.class, java.sql.Timestamp.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.sqlTimestamp(date);
        }
    },
    javaTimeLocalDateTime_javaTimeYear(java.time.LocalDateTime.class, java.time.Year.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            Year year = Year.of(DateUtil.year(date));
            return year;
        }
    },
    javaTimeLocalDateTime_javaTimeYearMonth(java.time.LocalDateTime.class, java.time.YearMonth.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            YearMonth yearMonth = YearMonth.of(DateUtil.year(date), DateUtil.month(date));
            return yearMonth;
        }
    },
    javaTimeLocalDateTime_javaTimeMonth(java.time.LocalDateTime.class, java.time.Month.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            Month month = Month.of(DateUtil.month(date));
            return month;
        }
    },
    javaTimeLocalDateTime_javaTimeLocalDateTime(java.time.LocalDateTime.class, java.time.LocalDate.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.localDate(date);
        }
    },
    javaTimeLocalDateTime_javaTimeLocalTime(java.time.LocalDateTime.class, java.time.LocalTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.localTime(date);

        }
    },



    /* *****************************************************************************************************************
     *                                               array
     * =================================================================================================================
     * java.entity.Double
     *
     * ****************************************************************************************************************/
    Doubles_Point(Double[].class, Point.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Point point = new Point((Double[])value);
            return point;
        }
    },
    double_Point(double[].class, Point.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Point point = new Point((double[])value);
            return point;
        }
    },
    bytes_String(byte[].class, String.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return new String((byte[]) value);
        }
    },
    bytes_Point(byte[].class, Point.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return new Point((byte[]) value);
        }
    },

    /* *****************************************************************************************************************
     *                                               Point
     * ==================================================================================================================
     * java.entity.Point
     *
     * *****************************************************************************************************************/
    Point_bytes(Point.class, byte[].class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Point point = (Point) value;
            return point.bytes();
        }
    },
    Point_Doubles(Point.class, Double[].class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Point point = (Point) value;
            return point.getDoubles();
        }
    },
    Point_doubles(Point.class, double[].class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Point point = (Point) value;
            try{
                value = point.doubles();
            }catch (Exception e){
                e.printStackTrace();
            }
            return value;
        }
    },


    /////////////////////////////////////////////////////////////////
    DataRow_String(DataRow.class, String.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((DataRow)value).toJSON();
        }
    },
    DataSet_String(DataSet.class, String.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((DataSet)value).toJSON();
        }
    },
    Map_String(Map.class, String.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return BeanUtil.map2json((Map)value);
        }
    },
    String_bytes(String.class, byte[].class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return value.toString().getBytes();
        }
    },
    /* *****************************************************************************************************************
     *                                               String
     * ==================================================================================================================
     * byte
     * date
     * number
     * date
     * *****************************************************************************************************************/

    String_javaUtilDate(String.class, Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return DateUtil.parse(value);
        }
    },

    string_javaUtilDate(String.class, java.util.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return date;
        }
    },
    string_javaSQLDate(String.class, java.sql.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.sqlDate(date);
        }
    },
    string_javaSQLTime(String.class, java.sql.Time.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.sqlTime(date);
        }
    },
    string_javaSQLTimestamp(String.class, java.sql.Timestamp.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.sqlTimestamp(date);
        }
    },
    string_javaTimeYear(String.class, java.time.Year.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            Year year = Year.of(DateUtil.year(date));
            return year;
        }
    },
    string_javaTimeYearMonth(String.class, java.time.YearMonth.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            YearMonth yearMonth = YearMonth.of(DateUtil.year(date), DateUtil.month(date));
            return yearMonth;
        }
    },
    string_javaTimeMonth(String.class, java.time.Month.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            Month month = Month.of(DateUtil.month(date));
            return month;
        }
    },
    string_javaTimeLocalDate(String.class, java.time.LocalDate.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.localDate(date);
        }
    },
    string_javaTimeLocalTime(String.class, java.time.LocalTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.localTime(date);

        }
    },

    string_javaTimeLocalDateTime(String.class, java.time.LocalDateTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.localDateTime(date);
        }
    },

    /* *****************************************************************************************************************
     *                                               日期
     * ==================================================================================================================
     * java.util.Date
     * java.sql.Date
     * java.sql.Time
     * java.sql.Timestamp
     * java.time.Year
     * java.time.YearMonth
     * java.time.Month
     * java.time.LocalDate
     * java.time.LocalTime
     * java.time.LocalDateTime
     * *****************************************************************************************************************/


    /* *****************************************************************************************************************
     *                                                  java.util.Date
     * *****************************************************************************************************************/
    javaUtilDate_String(java.util.Date.class, String.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = (Date)value;
            return DateUtil.format(date);
        }
    },
    javaUtilDate_Long(java.util.Date.class, Long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = (Date)value;
            return date.getTime();
        }
    },
    javaUtilDate_javaSQLDate(java.util.Date.class, java.sql.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = (Date)value;
            return DateUtil.sqlDate(date);
        }
    },
    javaUtilDate_javaSQLTime(java.util.Date.class, java.sql.Time.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = (Date)value;
            return DateUtil.sqlTime(date);
        }
    },
    javaUtilDate_javaSQLTimestamp(java.util.Date.class, java.sql.Timestamp.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = (Date)value;
            return DateUtil.sqlTimestamp(date);
        }
    },
    javaUtilDate_javaTimeYear(java.util.Date.class, java.time.Year.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = (Date)value;
            Year year = Year.of(DateUtil.year(date));
            return year;
        }
    },
    javaUtilDate_javaTimeYearMonth(java.util.Date.class, java.time.YearMonth.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = (Date) value;
            YearMonth yearMonth = YearMonth.of(DateUtil.year(date), DateUtil.month(date));
            return yearMonth;
        }
    },
    javaUtilDate_javaTimeMonth(java.util.Date.class, java.time.Month.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = (Date)value;
            Month month = Month.of(DateUtil.month(date));
            return month;
        }
    },
    javaUtilDate_javaTimeLocalDate(java.util.Date.class, java.time.LocalDate.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = (Date)value;
            return DateUtil.localDate(date);
        }
    },
    javaUtilDate_javaTimeLocalTime(java.util.Date.class, java.time.LocalTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = (Date)value;
            return DateUtil.localTime(date);

        }
    },

    javaUtilDate_javaTimeLocalDateTime(java.util.Date.class, java.time.LocalDateTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = (Date)value;
            return DateUtil.localDateTime(date);
        }
    },

    /* *****************************************************************************************************************
     *                                                  java.sql.Date
     * *****************************************************************************************************************/
    javaSQLDate_String(java.sql.Date.class, String.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.format(date);
        }
    },

    javaSQLDate_javaSQLDate(java.sql.Date.class, java.util.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return date;
        }
    },
    javaSQLDate_javaSQLTime(java.sql.Date.class, java.sql.Time.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.sqlTime(date);
        }
    },
    javaSQLDate_javaSQLTimestamp(java.sql.Date.class, java.sql.Timestamp.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.sqlTimestamp(date);
        }
    },
    javaSQLDate_javaTimeYear(java.sql.Date.class, java.time.Year.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            Year year = Year.of(DateUtil.year(date));
            return year;
        }
    },
    javaSQLDate_javaTimeYearMonth(java.sql.Date.class, java.time.YearMonth.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            YearMonth yearMonth = YearMonth.of(DateUtil.year(date), DateUtil.month(date));
            return yearMonth;
        }
    },
    javaSQLDate_javaTimeMonth(java.sql.Date.class, java.time.Month.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            Month month = Month.of(DateUtil.month(date));
            return month;
        }
    },
    javaSQLDate_javaTimeLocalDate(java.sql.Date.class, java.time.LocalDate.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.localDate(date);
        }
    },
    javaSQLDate_javaTimeLocalTime(java.sql.Date.class, java.time.LocalTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.localTime(date);
        }
    },

    javaSQLDate_javaTimeLocalDateTime(java.sql.Date.class, java.time.LocalDateTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.localDateTime(date);
        }
    },


    /* *****************************************************************************************************************
     *                                                  java.sql.Time
     * *****************************************************************************************************************/
    javaSQLTime_String(java.sql.Time.class, String.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.format(date, "HH:mm:ss");
        }
    },

    javaSQLTime_javaSQLDate(java.sql.Time.class, java.util.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return date;
        }
    },

    javaSQLTime_javaUtilDate(java.sql.Time.class, java.util.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.sqlTime(date);
        }
    },
    javaSQLTime_javaSQLTimestamp(java.sql.Time.class, java.sql.Timestamp.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.sqlTimestamp(date);
        }
    },
    javaSQLTime_javaTimeYear(java.sql.Time.class, java.time.Year.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            Year year = Year.of(DateUtil.year(date));
            return year;
        }
    },
    javaSQLTime_javaTimeYearMonth(java.sql.Time.class, java.time.YearMonth.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            YearMonth yearMonth = YearMonth.of(DateUtil.year(date), DateUtil.month(date));
            return yearMonth;
        }
    },
    javaSQLTime_javaTimeMonth(java.sql.Time.class, java.time.Month.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            Month month = Month.of(DateUtil.month(date));
            return month;
        }
    },
    javaSQLTime_javaTimeLocalDate(java.sql.Time.class, java.time.LocalDate.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.localDate(date);
        }
    },
    javaSQLTime_javaTimeLocalTime(java.sql.Time.class, java.time.LocalTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.localTime(date);
        }
    },

    javaSQLTime_javaTimeLocalDateTime(java.sql.Time.class, java.time.LocalDateTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.localDateTime(date);
        }
    },
    /* *****************************************************************************************************************
     *                                                  java.sql.Timestamp
     * *****************************************************************************************************************/


    javaSQLTimestamp_String(java.sql.Timestamp.class, String.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.format(date);
        }
    },

    javaSQLTimestamp_Long(java.sql.Timestamp.class, Long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((java.sql.Timestamp)value).getTime();
        }
    },
    javaSQLTimestamp_javaSQLDate(java.sql.Timestamp.class, java.util.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return date;
        }
    },
    javaSQLTimestamp_javaUtilDate(java.sql.Timestamp.class, java.sql.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.sqlDate(date);
        }
    },
    javaSQLTimestamp_javaSQLTimestampstamp(java.sql.Timestamp.class, java.sql.Time.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.sqlTime(date);
        }
    },
    javaSQLTimestamp_javaTimeLocalDate(java.sql.Timestamp.class, java.time.LocalDate.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.localDate(date);
        }
    },
    javaSQLTimestamp_javaTimeYear(java.sql.Timestamp.class, java.time.Year.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            Year year = Year.of(DateUtil.year(date));
            return year;
        }
    },
    javaSQLTimestamp_javaTimeYearMonth(java.sql.Timestamp.class, java.time.YearMonth.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            YearMonth yearMonth = YearMonth.of(DateUtil.year(date), DateUtil.month(date));
            return yearMonth;
        }
    },
    jjavaSQLTimestamp_javaTimeMonth(java.sql.Timestamp.class, java.time.Month.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            Month month = Month.of(DateUtil.month(date));
            return month;
        }
    },
    javaSQLTimestamp_javaTimeLocalTime(java.sql.Timestamp.class, java.time.LocalTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.localTime(date);
        }
    },

    javaSQLTimestamp_javaTimeLocalDateTime(java.sql.Timestamp.class, java.time.LocalDateTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.localDateTime(date);
        }
    },
    /* *****************************************************************************************************************
     *                                               Point
     * ==================================================================================================================
     * java.entity.Point
     *
     * *****************************************************************************************************************/
    ;
    private DefaultConvert(Class origin, Class target){
        this.origin = origin;
        this.target = target;
    }
    private final Class origin;
    private final Class target;

    public Class getOrigin() {
        return origin;
    }

    public Class getTarget() {
        return target;
    }
}