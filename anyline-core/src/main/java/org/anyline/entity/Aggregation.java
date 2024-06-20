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

public enum Aggregation {
    MIN			    ("MIN"  			, "最小"),
    MIN_DECIMAL		("MIN_DECIMAL"  	, "最小"),
    MIN_DOUBLE		("MIN_DOUBLE"  	, "最小"),
    MIN_FLOAT		("MIN_FLOAT"  	, "最小"),
    MIN_LONG		("MIN_LONG"  		, "最小"),
    MIN_INT			("MIN_INT"  		, "最小"),
    MAX			    ("MAX" 			, "最大"),
    MAX_DECIMAL     ("MAX_DECIMAL"     , "最大"),
    MAX_DOUBLE      ("MAX_DOUBLE"      , "最大"),
    MAX_FLOAT	    ("MAX_FLOAT"       , "最大"),
    MAX_LONG	    ("MAX_LONG"       , "最大"),
    MAX_INT			("MAX_INT"         , "最大"),
    MEDIAN          ("MEDIAN"          ,"中位"),
    MEDIAN_DECIMAL  ("MEDIAN_DECIMAL"  ,"中位"),
    MEDIAN_DOUBLE   ("MEDIAN_DOUBLE"   ,"中位"),
    MEDIAN_FLOAT    ("MEDIAN_FLOAT"    ,"中位"),
    MEDIAN_LONG     ("MEDIAN_LONG"     ,"中位"),
    MEDIAN_INT      ("MEDIAN_INT"      ,"中位"),
    AVG		        ("AVG"  			 , "平均"),
    AVGA		    ("AVGA"  			 , "平均(空值参与)"),
    SUM			    ("SUM"  			 , "合计"),
    COUNT		    ("COUNT"  		 , "数量"),
    COUNTA		    ("COUNT"  		 , "数量(空值参与)"),
    STDEV 		    ("STDEV"  		 , "抽样标准偏差"),
    STDEVA 		    ("STDEVA"           , "抽样标准偏差(空值参与)"),
    STDEVP		    ("STDEVP"           , "总体标准偏差"),
    STDEVPA		    ("STDEVPA"          , "总体标准偏差(空值参与)"),
    VAR		        ("VAR"              , "抽样方差"),
    VARA		    ("VARA"             , "抽样方差(空值参与)"),
    VARP		    ("VARP"  		     , "总体方差"),
    VARPA		    ("VARPA"  		 , "总体方差(空值参与)");
    final String code;
    final String name;
    Aggregation(String code, String name) {
        this.code = code;
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public String getCode() {
        return code;
    }
}
