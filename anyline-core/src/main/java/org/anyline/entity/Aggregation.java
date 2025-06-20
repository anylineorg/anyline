/*
 * Copyright 2006-2025 www.anyline.org
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
    MIN			    ("MIN"  			," MIN", "最小"),
    MIN_DECIMAL		("MIN_DECIMAL"  	, " MIN","最小"),
    MIN_DOUBLE		("MIN_DOUBLE"  	, " MIN","最小"),
    MIN_FLOAT		("MIN_FLOAT"  	, " MIN","最小"),
    MIN_LONG		("MIN_LONG"  		, " MIN","最小"),
    MIN_INT			("MIN_INT"  		, " MIN","最小"),
    MAX			    ("MAX" 			, " MAX","最大"),
    MAX_DECIMAL     ("MAX_DECIMAL"     , " MAX","最大"),
    MAX_DOUBLE      ("MAX_DOUBLE"      , " MAX","最大"),
    MAX_FLOAT	    ("MAX_FLOAT"       , " MAX","最大"),
    MAX_LONG	    ("MAX_LONG"       , " MAX","最大"),
    MAX_INT			("MAX_INT"         , " MAX","最大"),
    MEDIAN          ("MEDIAN"          ,"MEDIAN","中位"),
    MEDIAN_DECIMAL  ("MEDIAN_DECIMAL"  ,"MEDIAN","中位"),
    MEDIAN_DOUBLE   ("MEDIAN_DOUBLE"   ,"MEDIAN","中位"),
    MEDIAN_FLOAT    ("MEDIAN_FLOAT"    ,"MEDIAN","中位"),
    MEDIAN_LONG     ("MEDIAN_LONG"     ,"MEDIAN","中位"),
    MEDIAN_INT      ("MEDIAN_INT"      ,"MEDIAN","中位"),
    AVG		        ("AVG"  			 , "AVG","平均"),
    AVGA		    ("AVGA"  			 , "AVG","平均(空值参与)"),
    SUM			    ("SUM"  			 , "SUM","合计"),
    COUNT		    ("COUNT"  		 , "COUNT","数量"),
    COUNTA		    ("COUNT"  		 , "COUNT","数量(空值参与)"),
    STDEV 		    ("STDEV"  		 , "STDEV","抽样标准偏差"),
    STDEVA 		    ("STDEVA"           , "STDEVA","抽样标准偏差(空值参与)"),
    STDEVP		    ("STDEVP"           , "STDEVP","总体标准偏差"),
    STDEVPA		    ("STDEVPA"          , "STDEVPA","总体标准偏差(空值参与)"),
    VAR		        ("VAR"              , "VAR","抽样方差"),
    VARA		    ("VARA"             , "VARA" ,"抽样方差(空值参与)"),
    VARP		    ("VARP"  		     , "VARP" ,"总体方差"),
    VARPA		    ("VARPA"  		 , "VARPA","总体方差(空值参与)");
    final String code;
    final String formula;
    final String title;
    Aggregation(String code, String formula, String title) {
        this.code = code;
        this.title = title;
        this.formula = formula;
    }
    public String title() {
        return title;
    }
    public String code() {
        return code;
    }
    public String formula(){
        return formula;
    }
}
