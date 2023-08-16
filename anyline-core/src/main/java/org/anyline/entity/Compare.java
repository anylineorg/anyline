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


package org.anyline.entity;

import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

public enum Compare {
    //只作为参数值为占位符赋值,不能独立生成新的查询条件
    NONE(-1, null, null, null){
        @Override
        public boolean compare(Object value, Object target) {
            return false;
        }

        @Override
        public boolean isMultipleValue(){
            return false;
        }
    },
    //根据参数格式判断
    AUTO(0, null, null, null){
        @Override
        public boolean compare(Object value, Object target) {
            return false;
        }

        @Override
        public boolean isMultipleValue(){
            return false;
        }
    },
    EQUAL(10, "等于", "eq", " = ? ")			{
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
    GREAT(20, "大于", "gt", " > ? ")			{
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
    GREAT_EQUAL(21, "大于等于", "gte", " >= ? ")		{
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
    LESS(30, "小于", "lt", " < ? ")			{
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
    LESS_EQUAL(31, "小于等于", "lte", " <= ? ")		{
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
    IN(40, "in","in"," IN ")				{
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
    LIKE(50, "like %?%", "", " LIKE ")			{
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
    LIKE_PREFIX(51, "like ?%", "", " LIKE ")		{
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
    START_WITH(51, "like ?%", "", " LIKE ")		{
        public int getCode(){return 51;}
        public String getSQL(){return " LIKE ";}
        public String getName(){return "like ?%";}
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
    LIKE_SUFFIX(52, "like %?", "", " LIKE ")		{
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
    END_WITH(52, "like %?", "", " LIKE ")		{
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
    FIND_IN_SET(60, "find in set","", " FIND_IN_SET "){ // = FIND_IN_SET_OR
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
    FIND_IN_SET_OR(61, "find in set","", " FIND_IN_SET "){
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
    FIND_IN_SET_AND(62, "find in set","", " FIND_IN_SET "){
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
    BETWEEN(80, "区间", "", " BETWEEN ? AND ? ")			{
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
    NOT_EQUAL(110, "不等于","nin"," != ? ")		{
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
    NOT_IN(150, "不包含","nin"," NOT IN ")			{
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
    NOT_LIKE(150, "NOT LIKE %?%",""," NOT LIKE ")				{
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
    NOT_LIKE_PREFIX(151, "NOT LIKE ?%",""," NOT LIKE ")			{
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
    NOT_START_WITH(151, "NOT LIKE ?%",""," NOT LIKE ")				{
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
    NOT_LIKE_SUFFIX(152, "NOT LIKE %?",""," NOT LIKE ")			{
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
    NOT_END_WITH(152, "NOT LIKE %?",""," NOT LIKE ")			{
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
    //正则表达式，注意不是每个数据库都支持
    REGEX(999, "正则", "regex", "")			{
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

    private final int code;
    private final String operator;
    private final String sql;
    private final String name;
    Compare(int code, String name, String operator, String sql){
        this.code = code;
        this.name = name;
        this.sql = sql;
        this.operator = operator;
    }

    /**
     * 是否支持多个值
     * @return boolean
     */
    public abstract boolean isMultipleValue();
    public String getSQL(){
        return sql;
    }
    public String getOperator(){
        return operator;
    }
    public int getCode(){
        return code;
    }
    public String getName(){
        return name;
    }

    public enum EMPTY_VALUE_SWITCH {
          IGNORE   //忽略当前条件  其他条件继续执行
        , BREAK	   //中断执行 整个SQL不执行
        , NULL	   //生成 WHERE ID IS NULL
        , SRC	   //原样处理 会生成 WHERE ID = NULL
        , NONE	   //根据条件判断 ++或+
    }
}
