package org.anyline.entity;

import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

public enum Compare {
    AUTO{
        @Override
        public boolean compare(Object value, Object target) {
            return false;
        }

        @Override
        public String getSQL() {
            return null;
        }

        @Override
        public int getCode() {
            return 0;
        }

        @Override
        public String getName() {
            return null;
        }
    },
    EQUAL			{
        public int getCode(){return 10;}
        public String getSQL(){return " = ?";}
        public String getName(){return "等于";}
        public boolean compare(Object value, Object target) {
            if(null == target){
                if(null == value){
                    return true;
                }else {
                    return false;
                }
            }
            return target.toString().equalsIgnoreCase(value.toString());
        }
    },
    GREAT			{
        public int getCode(){return 20;}
        public String getSQL(){return " > ?";}
        public String getName(){return "大于";}
        public boolean compare(Object value, Object target) {
            if(null == target || null == value){
                return false;
            }
            try {
                return new BigDecimal(value.toString()).compareTo(new BigDecimal(target.toString())) > 0;
            }catch (Exception e){
                return false;
            }
        }
    },
    GREAT_EQUAL		{
        public int getCode(){return 21;}
        public String getSQL(){return " >= ?";}
        public String getName(){return "大于等于";}
        public boolean compare(Object value, Object target) {
            if(null == target || null == value){
                return false;
            }
            return new BigDecimal(value.toString()).compareTo(new BigDecimal(target.toString())) >= 0;
        }
    },
    LESS			{
        public int getCode(){return 30;}
        public String getSQL(){return " < ?";}
        public String getName(){return "小于";}
        public boolean compare(Object value, Object target) {
            if(null == target || null == value){
                return false;
            }
            try {
                return new BigDecimal(value.toString()).compareTo(new BigDecimal(target.toString())) < 0;
            }catch (Exception e){
                return false;
            }
        }
    },
    LESS_EQUAL		{
        public int getCode(){return 31;}
        public String getSQL(){return " <= ?";}
        public String getName(){return "小于等于";}
        public boolean compare(Object value, Object target) {
            if(null == target || null == value){
                return false;
            }
            try {
                return new BigDecimal(value.toString()).compareTo(new BigDecimal(target.toString())) <= 0;
            }catch (Exception e){
                return false;
            }
        }
    },
    IN				{
        public int getCode(){return 40;}
        public String getSQL(){return " IN ";}
        public String getName(){return "in";}
        public boolean compare(Object value, Object targets) {
            if(null != targets && targets instanceof Collection){
                Collection cols = (Collection) targets;
                for(Object v:cols){
                    if(null != v && v.toString().equalsIgnoreCase(value.toString())){
                        return true;
                    }
                }
            }
            return false;
        }
    },
    LIKE			{
        public int getCode(){return 50;}
        public String getSQL(){return " LIKE ";}
        public String getName(){return "%like%";}
        public boolean compare(Object value, Object target) {
            if(null == target || null == value){
                return false;
            }
            return value.toString().toUpperCase().contains(target.toString().toUpperCase());
        }
    },
    LIKE_PREFIX		{
        public int getCode(){return 51;}
        public String getSQL(){return " LIKE ";}
        public String getName(){return "START_WITH";}
        public boolean compare(Object value, Object target) {
            if(null == target || null == value){
                return false;
            }
            return value.toString().toUpperCase().startsWith(target.toString().toUpperCase());
        }
    },
    START_WITH		{
        public int getCode(){return 51;}
        public String getSQL(){return " LIKE ";}
        public String getName(){return "START_WITH";}
        public boolean compare(Object value, Object target) {
            if(null == target || null == value){
                return false;
            }
            return value.toString().toUpperCase().startsWith(target.toString().toUpperCase());
        }
    },
    LIKE_SUFFIX		{
        public int getCode(){return 52;}
        public String getSQL(){return " LIKE ";}
        public String getName(){return "END_WITH";}
        public boolean compare(Object value, Object target) {
            if(null == target || null == value){
                return false;
            }
            return value.toString().toUpperCase().endsWith(target.toString().toUpperCase());
        }
    },
    END_WITH		{
        public int getCode(){return 52;}
        public String getSQL(){return " LIKE ";}
        public String getName(){return "END_WITH";}
        public boolean compare(Object value, Object target) {
            if(null == target || null == value){
                return false;
            }
            return value.toString().toUpperCase().endsWith(target.toString().toUpperCase());
        }
    },
    FIND_IN_SET{
        public int getCode(){return 60;}
        public String getSQL(){return " FIND_IN_SET ";}
        public String getName(){return "find in set";}
        public boolean compare(Object value, Object target) {
            if(null == target || null == value){
                return false;
            }
            String[] arrays = target.toString().split(",");
            return BeanUtil.array2list(arrays).contains(value);
        }
    },
    FIND_IN_SET_OR{
        public int getCode(){return 61;}
        public String getSQL(){return " FIND_IN_SET ";}
        public String getName(){return "find in set";}
        public boolean compare(Object value, Object target) {
            if(null == target || null == value){
                return false;
            }
            String[] arrays = target.toString().split(",");
            return BeanUtil.array2list(arrays).contains(value);
        }
    },
    FIND_IN_SET_AND{
        public int getCode(){return 62;}
        public String getSQL(){return " FIND_IN_SET ";}
        public String getName(){return "find in set";}
        public boolean compare(Object value, Object target) {
            if(null == target || null == value){
                return false;
            }
            String[] arrays = target.toString().split(",");
            return BeanUtil.array2list(arrays).contains(value);
        }
    },
    BETWEEN			{
        public int getCode(){return 80;}
        public String getSQL(){return " BETWEEN ? AND ? ";}
        public String getName(){return "区间";}
        public boolean compare(Object value, Object target) {
            if(null == value){
                return false;
            }
            if(target instanceof List){
                List list = (List)target;
                Object min = list.get(0);
                Object max = list.get(1);
                return compare(value, BasicUtil.parseDecimal(min, null), BasicUtil.parseDecimal(max, null));
            }
            return false;
        }
        public boolean compare(Object value, BigDecimal min, BigDecimal max) {
            if(null == value || null == min || null == max){
                return false;
            }
            try {
                BigDecimal v = new BigDecimal(value.toString());
                if (v.compareTo(min) >= 0 && v.compareTo(max) <= 0) {
                    return true;
                }
            }catch (Exception e){
                return false;
            }
            return false;
        }
    },
    NOT_EQUAL		{
        public int getCode(){return 110;}
        public String getSQL(){return " != ?";}
        public String getName(){return "不等于";}
        public boolean compare(Object value, Object target) {
            if(null == target || null == value){
                return false;
            }
            return !value.toString().toUpperCase().equals(target.toString().toUpperCase());
        }
    },
    NOT_IN			{
        public int getCode(){return 140;}
        public String getSQL(){return " NOT IN ";}
        public String getName(){return "不包含";}
        public boolean compare(Object value, Object target) {
            if(null == target || null == value){
                return false;
            }
            if(value instanceof Collection){
                Collection col = (Collection)value;
                return col.contains(target);
            }
            return false;
        }
    };

    public abstract boolean compare(Object value, Object target);
    public abstract String getSQL();
    public abstract int getCode();
    public abstract String getName();
}
