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
    //只作为参数值为占位符赋值, 不能独立生成新的查询条件
    NONE(-1, null, null, null) {
        @Override
        public int valueCount() {
            return 1;
        }
    },
    //根据参数格式判断
    AUTO(0, null, null, null) {
        @Override
        public int valueCount() {
            return 1;
        }
    },
    EQUAL(10, "等于","eq"," = ? ") {
        public boolean compare(Object value, Object target) {
            if(null == target) {
                if(null == value) {
                    return true;
                }else {
                    return false;
                }
            }
            return target.toString().equalsIgnoreCase(value.toString());
        }
        public int valueCount() {
            return 1;
        }
    },
    EQUALS(11, "等于","eq"," == ? ") {
        public boolean compare(Object value, Object target) {
            if(null == target) {
                if(null == value) {
                    return true;
                }else {
                    return false;
                }
            }
            return target.toString().equalsIgnoreCase(value.toString());
        }
        public int valueCount() {
            return 1;
        }
    },
    GREAT(20, "大于","gt"," > ? ") {
        public boolean compare(Object value, Object target) {
            if(null == target || null == value) {
                return false;
            }
            try {
                return new BigDecimal(value.toString()).compareTo(new BigDecimal(target.toString())) > 0;
            }catch (Exception e) {
                return false;
            }
        }
        public int valueCount() {
            return 1;
        }
    },
    GREAT_EQUAL(21, "大于等于","gte"," >= ? ") {
        public boolean compare(Object value, Object target) {
            if(null == target || null == value) {
                return false;
            }
            return new BigDecimal(value.toString()).compareTo(new BigDecimal(target.toString())) >= 0;
        }
        public int valueCount() {
            return 1;
        }
    },
    LESS(30, "小于","lt"," < ? ") {
        public boolean compare(Object value, Object target) {
            if(null == target || null == value) {
                return false;
            }
            try {
                return new BigDecimal(value.toString()).compareTo(new BigDecimal(target.toString())) < 0;
            }catch (Exception e) {
                return false;
            }
        }
        public int valueCount() {
            return 1;
        }
    },
    LESS_EQUAL(31, "小于等于","lte"," <= ? ") {
        public boolean compare(Object value, Object target) {
            if(null == target || null == value) {
                return false;
            }
            try {
                return new BigDecimal(value.toString()).compareTo(new BigDecimal(target.toString())) <= 0;
            }catch (Exception e) {
                return false;
            }
        }
        public int valueCount() {
            return 1;
        }
    },
    IN(40, "in","in"," IN ")  {
        public boolean compare(Object value, Object targets) {
            if(null != targets && targets instanceof Collection) {
                Collection cols = (Collection) targets;
                for(Object v:cols) {
                    if(null != v && v.toString().equalsIgnoreCase(value.toString())) {
                        return true;
                    }
                }
            }
            return false;
        }
        public int valueCount() {
            return 9;
        }
    },
    LIKE(50, "like %?%",""," LIKE ") {
        public boolean compare(Object value, Object target) {
            if(null == target || null == value) {
                return false;
            }
            return value.toString().toUpperCase().contains(target.toString().toUpperCase());
        }
        public int valueCount() {
            return 1;
        }
    },
    LIKE_SIMPLE(50, "like ?",""," LIKE ") {
        //不添加通配符
        public boolean compare(Object value, Object target) {
            if(null == target || null == value) {
                return false;
            }
            return value.toString().toUpperCase().contains(target.toString().toUpperCase());
        }
        public int valueCount() {
            return 1;
        }
    },
    LIKE_PREFIX(51, "like ?%",""," LIKE ") {
        public boolean compare(Object value, Object target) {
            if(null == target || null == value) {
                return false;
            }
            return value.toString().toUpperCase().startsWith(target.toString().toUpperCase());
        }
        public int valueCount() {
            return 1;
        }
    },
    START_WITH(51, "like ?%",""," LIKE ") {
        public boolean compare(Object value, Object target) {
            if(null == target || null == value) {
                return false;
            }
            return value.toString().toUpperCase().startsWith(target.toString().toUpperCase());
        }
        public int valueCount() {
            return 1;
        }
    },
    LIKE_SUFFIX(52, "like %?",""," LIKE ") {
        public boolean compare(Object value, Object target) {
            if(null == target || null == value) {
                return false;
            }
            return value.toString().toUpperCase().endsWith(target.toString().toUpperCase());
        }
        public int valueCount() {
            return 1;
        }
    },
    END_WITH(52, "like %?",""," LIKE ") {
        public boolean compare(Object value, Object target) {
            if(null == target || null == value) {
                return false;
            }
            return value.toString().toUpperCase().endsWith(target.toString().toUpperCase());
        }
        public int valueCount() {
            return 1;
        }
    },

    START_WITH_IGNORE_CASE(51, "ilike ?%",""," ILIKE ") {
        public boolean compare(Object value, Object target) {
            if(null == target || null == value) {
                return false;
            }
            return value.toString().toUpperCase().startsWith(target.toString().toUpperCase());
        }
        public int valueCount() {
            return 1;
        }
    },
    LIKE_SUFFIX_IGNORE_CASE(52, "ilike %?",""," ILIKE ") {
        public boolean compare(Object value, Object target) {
            if(null == target || null == value) {
                return false;
            }
            return value.toString().toUpperCase().endsWith(target.toString().toUpperCase());
        }
        public int valueCount() {
            return 1;
        }
    },
    END_WITH_IGNORE_CASE(52, "ilike %?",""," ILIKE ") {
        public boolean compare(Object value, Object target) {
            if(null == target || null == value) {
                return false;
            }
            return value.toString().toUpperCase().endsWith(target.toString().toUpperCase());
        }
        public int valueCount() {
            return 1;
        }
    },
    //忽略大小写
    LIKE_IGNORE_CASE(50, "ilike %?%",""," ILIKE ") {
        public boolean compare(Object value, Object target) {
            if(null == target || null == value) {
                return false;
            }
            return value.toString().toUpperCase().contains(target.toString().toUpperCase());
        }
        public int valueCount() {
            return 1;
        }
    },
    LIKE_PREFIX_IGNORE_CASE(51, "ilike ?%",""," ILIKE ") {
        public boolean compare(Object value, Object target) {
            if(null == target || null == value) {
                return false;
            }
            return value.toString().toUpperCase().startsWith(target.toString().toUpperCase());
        }
        public int valueCount() {
            return 1;
        }
    },
    /**
     * 搜索引擎 匹配 多列时 生成 MULTI_MATCH
     */
    MATCH(55, "MATCH",""," ") {
        public boolean compare(Object value, Object target) {
            if(null == target || null == value) {
                return false;
            }
            return value.toString().toUpperCase().contains(target.toString().toUpperCase());
        }
        public int valueCount() {
            return 1;
        }
    },
    /**
     * 搜索引擎 匹配
     */
    MATCH_PHRASE(56, "MATCH_PHRASE",""," ") {
        public boolean compare(Object value, Object target) {
            if(null == target || null == value) {
                return false;
            }
            return value.toString().toUpperCase().contains(target.toString().toUpperCase());
        }
        public int valueCount() {
            return 1;
        }
    },
    /**
     * 搜索引擎 匹配
     */
    MATCH_PHRASE_PREFIX (56, "MATCH_PHRASE_PREFIX ",""," ") {
        public boolean compare(Object value, Object target) {
            if(null == target || null == value) {
                return false;
            }
            return value.toString().toUpperCase().startsWith(target.toString().toUpperCase());
        }
        public int valueCount() {
            return 1;
        }
    },
    /**
     * 这个专用来实现所有列LIKE
     */
    LIKES(59, "like %?%",""," LIKE ") {
        public boolean compare(Object value, Object target) {
            if(null == target || null == value) {
                return false;
            }
            return value.toString().toUpperCase().contains(target.toString().toUpperCase());
        }
        public int valueCount() {
            return 1;
        }
    },
    LIKES_IGNORE_CASE(59, "ilike %?%",""," ILIKE ") {
        public boolean compare(Object value, Object target) {
            if(null == target || null == value) {
                return false;
            }
            return value.toString().toUpperCase().contains(target.toString().toUpperCase());
        }
        public int valueCount() {
            return 1;
        }
    },
    FIND_IN_SET(60, "find in set",""," FIND_IN_SET ") { // = FIND_IN_SET_OR
        public boolean compare(Object value, Object target) {
            if(null == target || null == value) {
                return false;
            }
            String[] arrays = target.toString().split(",");
            return BeanUtil.array2list(arrays).contains(value);
        }
        public int valueCount() {
            return 1;
        }
    },
    FIND_IN_SET_OR(61, "find in set",""," FIND_IN_SET ") {
        public boolean compare(Object value, Object target) {
            if(null == target || null == value) {
                return false;
            }
            String[] arrays = target.toString().split(",");
            return BeanUtil.array2list(arrays).contains(value);
        }
        public int valueCount() {
            return 9;
        }
    },
    FIND_IN_SET_AND(62, "find in set",""," FIND_IN_SET ") {
        public boolean compare(Object value, Object target) {
            if(null == target || null == value) {
                return false;
            }
            String[] arrays = target.toString().split(",");
            return BeanUtil.array2list(arrays).contains(value);
        }
        public int valueCount() {
            return 9;
        }
    },
    JSON_CONTAINS(70, "json_contains", "", " JSON_CONTAINS ") {
        public int valueCount() {
            return 1;
        }
    },
    JSON_CONTAINS_OR(71, "json_contains", "", " JSON_CONTAINS ") {
        public int valueCount() {
            return 9;
        }
    },
    JSON_CONTAINS_AND(72, "json_contains", "", " JSON_CONTAINS ") {
        public int valueCount() {
            return 9;
        }
    },
    JSON_CONTAINS_PATH_OR(75, "json_contains_path", "", " JSON_CONTAINS_PATH ") {
        //ONE 包含1个即可
        public int valueCount() {
            return 9;
        }
    },
    JSON_CONTAINS_PATH_ONE(75, "json_contains_path", "", " JSON_CONTAINS_PATH ") {
        //ONE 包含1个即可
        public int valueCount() {
            return 9;
        }
    },
    JSON_CONTAINS_PATH_AND(76, "json_contains_path", "", " JSON_CONTAINS_PATH ") {
        //ALL 必须包含全部
        public int valueCount() {
            return 9;
        }
    },
    JSON_CONTAINS_PATH_ALL(76, "json_contains_path", "", " JSON_CONTAINS_PATH ") {
        //ALL 必须包含全部
        public int valueCount() {
            return 9;
        }
    },
    JSON_SEARCH(77, "json_search", "", " JSON_SEARCH ") {
        public int valueCount() {
            return 1;
        }
    },
    JSON_SEARCH_OEN(77, "json_search", "", " JSON_SEARCH ") {
        public int valueCount() {
            return 1;
        }
    },
    JSON_SEARCH_ALL(78, "json_search", "", " JSON_SEARCH ") {
        public int valueCount() {
            return 1;
        }
    },
    BETWEEN(80, "区间",""," BETWEEN ? AND ? ") {
        public boolean compare(Object value, Object target) {
            if(null == value) {
                return false;
            }
            if(target instanceof List) {
                List list = (List)target;
                Object min = list.get(0);
                Object max = list.get(1);
                return compare(value, BasicUtil.parseDecimal(min, null), BasicUtil.parseDecimal(max, null));
            }
            return false;
        }
        public boolean compare(Object value, BigDecimal min, BigDecimal max) {
            if(null == value || null == min || null == max) {
                return false;
            }
            try {
                BigDecimal v = new BigDecimal(value.toString());
                if (v.compareTo(min) >= 0 && v.compareTo(max) <= 0) {
                    return true;
                }
            }catch (Exception e) {
                return false;
            }
            return false;
        }
        public int valueCount() {
            return 2;
        }
    },
    NULL(90, "空",""," IS NULL ") {
        public boolean compare(Object value, Object target) {
            if(null == value) {
                return true;
            }
            return false;
        }
        public int valueCount() {
            return 0;
        }
    },
    EMPTY(91, "空",""," IS EMPTY ") {
        public boolean compare(Object value, Object target) {
            if(BasicUtil.isEmpty(true, value)) {
                return true;
            }
            return false;
        }
        public int valueCount() {
            return 0;
        }
    },
    EXISTS(92, "存在",""," EXISTS ") {
        public boolean compare(Object value, Object target) {
            if(BasicUtil.isEmpty(true, value)) {
                return false;
            }
            return true;
        }
        public int valueCount() {
            return 0;
        }
    },
    NOT_EQUAL(110, "不等于","nin"," != ? ") {
        public boolean compare(Object value, Object target) {
            if(null == target || null == value) {
                return false;
            }
            return !value.toString().toUpperCase().equals(target.toString().toUpperCase());
        }
        public int valueCount() {
            return 1;
        }
    },
    NOT_IN(140, "不包含","nin"," NOT IN ") {
        public boolean compare(Object value, Object target) {
            if(null == target || null == value) {
                return false;
            }
            if(value instanceof Collection) {
                Collection col = (Collection)value;
                return col.contains(target);
            }
            return false;
        }
        public int valueCount() {
            return 9;
        }
    },
    NOT_LIKE(150, "NOT LIKE %?%",""," NOT LIKE ")  {
        public boolean compare(Object value, Object target) {
            if(null == target || null == value) {
                return false;
            }
            if(value instanceof Collection) {
                Collection col = (Collection)value;
                return col.contains(target);
            }
            return false;
        }
        public int valueCount() {
            return 1;
        }
    },
    NOT_LIKE_SIMPLE(150, "NOT LIKE ?",""," NOT LIKE ")  {
        public boolean compare(Object value, Object target) {
            if(null == target || null == value) {
                return false;
            }
            if(value instanceof Collection) {
                Collection col = (Collection)value;
                return col.contains(target);
            }
            return false;
        }
        public int valueCount() {
            return 1;
        }
    },
    NOT_LIKE_PREFIX(151, "NOT LIKE ?%",""," NOT LIKE ") {
        public boolean compare(Object value, Object target) {
            if(null == target || null == value) {
                return false;
            }
            if(value instanceof Collection) {
                Collection col = (Collection)value;
                return col.contains(target);
            }
            return false;
        }
        public int valueCount() {
            return 1;
        }
    },
    NOT_START_WITH(151, "NOT LIKE ?%",""," NOT LIKE ")  {
        public boolean compare(Object value, Object target) {
            if(null == target || null == value) {
                return false;
            }
            if(value instanceof Collection) {
                Collection col = (Collection)value;
                return col.contains(target);
            }
            return false;
        }
        public int valueCount() {
            return 1;
        }
    },
    NOT_LIKE_SUFFIX(152, "NOT LIKE %?",""," NOT LIKE ") {
        public boolean compare(Object value, Object target) {
            if(null == target || null == value) {
                return false;
            }
            if(value instanceof Collection) {
                Collection col = (Collection)value;
                return col.contains(target);
            }
            return false;
        }
        public int valueCount() {
            return 1;
        }
    },
    NOT_END_WITH(152, "NOT LIKE %?",""," NOT LIKE ") {
        public boolean compare(Object value, Object target) {
            if(null == target || null == value) {
                return false;
            }
            if(value instanceof Collection) {
                Collection col = (Collection)value;
                return col.contains(target);
            }
            return false;
        }
        public int valueCount() {
            return 1;
        }
    },
    NOT_NULL(190, "非空",""," IS NOT NULL ") {
        public boolean compare(Object value, Object target) {
            if(null == value) {
                return false;
            }
            return true;
        }
        public int valueCount() {
            return 0;
        }
    },
    NOT_EMPTY(191, "非空",""," IS NOT EMPTY ") {
        public boolean compare(Object value, Object target) {
            if(BasicUtil.isEmpty(true, value)) {
                return false;
            }
            return true;
        }
        public int valueCount() {
            return 0;
        }
    },
    NOT_EXISTS(192, "不存在", "", " NOT EXISTS") {
        public boolean compare(Object value, Object target) {
            if(BasicUtil.isEmpty(true, value)) {
                return true;
            }
            return false;
        }
        public int valueCount() {
            return 0;
        }
    }
    //正则表达式，注意不是每个数据库都支持
    REGEX(999, "正则","regex"," REGEXP ?") {
        public boolean compare(Object value, Object target) {
            if(null == target || null == value) {
                return false;
            }
            if(value instanceof Collection) {
                Collection col = (Collection)value;
                return col.contains(target);
            }
            return false;
        }
        public int valueCount() {
            return 1;
        }
    };

    public boolean compare(Object value) {
        return false;
    }
    public boolean compare(Object value, Object target) {
        return false;
    }
    public boolean compare(Object value, Object min, Object max) {
        return false;
    }

    private final int code;
    private final String operator;
    private final String formula;
    private final String name;
    Compare(int code, String name, String operator, String formula) {
        this.code = code;
        this.name = name;
        this.formula = formula;
        this.operator = operator;
    }

    /**
     * 支持参数数量 0:没有参数 IS NULL/IS EMPTY 1:一个参数 2:两个参数 BETWEEN 9:多个参数  IN()
     * @return boolean
     */
    public abstract int valueCount();
    //public abstract boolean isMultipleValue();

    public String formula() {
        return formula;
    }

    public String formula(String column, Object value, boolean placeholder, boolean unicode) {
        //如果不需要占位符，必须在上一步把需要的引号加上
        if(!placeholder) {
            String str = "";
            if(null != value) {
                str = value.toString();
            }
            return formula.replace("?", str);
        }
        return formula;
    }
    public String formula(String column, Object value, boolean placeholder) {
        return formula(column, value, placeholder, false);
    }
    public String getOperator() {
        return operator;
    }
    public int getCode() {
        return code;
    }
    public String getName() {
        return name;
    }

    public enum EMPTY_VALUE_SWITCH {
         IGNORE   //忽略当前条件  其他条件继续执行
       , BREAK	   //中断执行 整个命令不执行
       , NULL	   //生成 WHERE ID IS NULL
       , SRC	   //原样处理 会生成 WHERE ID = NULL
       , NONE	   //根据条件判断 ++或+
    }
}
