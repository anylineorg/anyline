package org.anyline.entity;

import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

public enum Compare {
    //只作为参数值为占位符赋值,不能独立生成新的查询条件
    NONE{
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
            return -1;
        }

        @Override
        public String getName() {
            return null;
        }
        public boolean isMultipleValue(){
            return false;
        }
    },
    //根据参数格式判断
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
        public boolean isMultipleValue(){
            return false;
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
        public boolean isMultipleValue(){
            return false;
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
        public boolean isMultipleValue(){
            return false;
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
        public boolean isMultipleValue(){
            return false;
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
        public boolean isMultipleValue(){
            return false;
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
        public boolean isMultipleValue(){
            return false;
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
        public boolean isMultipleValue(){
            return true;
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
        public boolean isMultipleValue(){
            return false;
        }
    },
    LIKE_PREFIX		{
        public int getCode(){return 51;}
        public String getSQL(){return " LIKE ";}
        public String getName(){return "like A%";}
        public boolean compare(Object value, Object target) {
            if(null == target || null == value){
                return false;
            }
            return value.toString().toUpperCase().startsWith(target.toString().toUpperCase());
        }
        public boolean isMultipleValue(){
            return false;
        }
    },
    START_WITH		{
        public int getCode(){return 51;}
        public String getSQL(){return " LIKE ";}
        public String getName(){return "like A%";}
        public boolean compare(Object value, Object target) {
            if(null == target || null == value){
                return false;
            }
            return value.toString().toUpperCase().startsWith(target.toString().toUpperCase());
        }
        public boolean isMultipleValue(){
            return false;
        }
    },
    LIKE_SUFFIX		{
        public int getCode(){return 52;}
        public String getSQL(){return " LIKE ";}
        public String getName(){return "like %A";}
        public boolean compare(Object value, Object target) {
            if(null == target || null == value){
                return false;
            }
            return value.toString().toUpperCase().endsWith(target.toString().toUpperCase());
        }
        public boolean isMultipleValue(){
            return false;
        }
    },
    END_WITH		{
        public int getCode(){return 52;}
        public String getSQL(){return " LIKE ";}
        public String getName(){return "like %A";}
        public boolean compare(Object value, Object target) {
            if(null == target || null == value){
                return false;
            }
            return value.toString().toUpperCase().endsWith(target.toString().toUpperCase());
        }
        public boolean isMultipleValue(){
            return false;
        }
    },
    FIND_IN_SET{ // = FIND_IN_SET_OR
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
        public boolean isMultipleValue(){
            return false;
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
        public boolean isMultipleValue(){
            return false;
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
        public boolean isMultipleValue(){
            return false;
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
        public boolean isMultipleValue(){
            return true;
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
        public boolean isMultipleValue(){
            return false;
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
        public boolean isMultipleValue(){
            return true;
        }
    },
    NOT_LIKE			{
        public int getCode(){return 150;}
        public String getSQL(){return " NOT LIKE ";}
        public String getName(){return "not like %A%";}
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
        public boolean isMultipleValue(){
            return true;
        }
    },
    NOT_LIKE_PREFIX			{
        public int getCode(){return 151;}
        public String getSQL(){return " NOT LIKE ";}
        public String getName(){return "not like A%";}
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
        public boolean isMultipleValue(){
            return true;
        }
    },
    NOT_START_WITH			{
        public int getCode(){return 151;}
        public String getSQL(){return " NOT LIKE ";}
        public String getName(){return "not like A%";}
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
        public boolean isMultipleValue(){
            return true;
        }
    },
    NOT_LIKE_SUFFIX			{
        public int getCode(){return 152;}
        public String getSQL(){return " NOT LIKE ";}
        public String getName(){return "not like %A";}
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
        public boolean isMultipleValue(){
            return true;
        }
    },
    NOT_END_WITH			{
        public int getCode(){return 152;}
        public String getSQL(){return " NOT LIKE ";}
        public String getName(){return "not like %A";}
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
        public boolean isMultipleValue(){
            return true;
        }
    };

    public abstract boolean compare(Object value, Object target);
    public abstract boolean isMultipleValue();
    public abstract String getSQL();
    public abstract int getCode();
    public abstract String getName();

    public static enum EMPTY_VALUE_SWITCH {
          IGNORE   //忽略当前条件  其他条件继续执行
        , BREAK	   //中断执行 整个SQL不执行
        , NULL	   //生成 WHERE ID IS NULL
        , SRC	   //原样处理 会生成 WHERE ID = NULL
        , NONE	   //根据条件判断 ++或+
    }
}
