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

package org.anyline.data.jdbc.adapter.init.alias;

import org.anyline.data.metadata.PropertyCompat;
import org.anyline.metadata.*;

public enum MySQLGenusPropertyCompat implements PropertyCompat {
	DEFAULT                        (Table.class  ,"ENGINE" ,"DEFAULT"                      ,"InnoDB" ),
	AggregatingMergeTree           (Table.class  ,"ENGINE" ,"AggregatingMergeTree"         ,null     ),
	Buffer                         (Table.class  ,"ENGINE" ,"Buffer"                       ,null     ),
	CollapsingMergeTree            (Table.class  ,"ENGINE" ,"CollapsingMergeTree"          ,null     ),
	Dictionary                     (Table.class  ,"ENGINE" ,"Dictionary"                   ,null     ),
	Distributed                    (Table.class  ,"ENGINE" ,"Distributed"                  ,null     ),
	File                           (Table.class  ,"ENGINE" ,"File"                         ,null     ),
	GraphiteMergeTree              (Table.class  ,"ENGINE" ,"GraphiteMergeTree"            ,null     ),
	InnoDB                         (Table.class  ,"ENGINE" ,"InnoDB"                       ,"InnoDB" ),
	Join                           (Table.class  ,"ENGINE" ,"Join"                         ,null     ),
	Log                            (Table.class  ,"ENGINE" ,"Log"                          ,null     ),
	MaterializedView               (Table.class  ,"ENGINE" ,"MaterializedView"             ,null     ),
	Memory                         (Table.class  ,"ENGINE" ,"Memory"                       ,null     ),
	Merge                          (Table.class  ,"ENGINE" ,"Merge"                        ,null     ),
	MergeTree                      (Table.class  ,"ENGINE" ,"MergeTree"                    ,null     ),
	Null                           (Table.class  ,"ENGINE" ,"Null"                         ,null     ),
	ReplacingMergeTree             (Table.class  ,"ENGINE" ,"ReplacingMergeTree"           ,null     ),
	Set                            (Table.class  ,"ENGINE" ,"Set"                          ,null     ),
	StripeLog                      (Table.class  ,"ENGINE" ,"StripeLog"                    ,null     ),
	SummingMergeTree               (Table.class  ,"ENGINE" ,"SummingMergeTree"             ,null     ),
	TinyLog                        (Table.class  ,"ENGINE" ,"TinyLog"                      ,null     ),
	URL                            (Table.class  ,"ENGINE" ,"URL"                          ,null     ),
	VersionedCollapsingMergeTree   (Table.class  ,"ENGINE" ,"VersionedCollapsingMergeTree" ,null     ),
	View                           (Table.class  ,"ENGINE" ,"View"                         ,null     );

	private Class<? extends Metadata> metadata  ; // 适用类型 如Table.class
	private String property                     ; // 属性分组 如 ENGINE
	private String compat                   ; // 兼容属性名称或别名
	private String optimal                      ; // 适用当前数据库的属性值

	MySQLGenusPropertyCompat(Class<? extends Metadata> metadata, String property, String compat, String optimal) {
		this.metadata = metadata;
		this.property = property;
		this.compat = compat;
		this.optimal = optimal;
	}

	@Override
	public Class<? extends Metadata> metadata() {
		return metadata;
	}

	@Override
	public String property() {
		return property;
	}

	@Override
	public String compat() {
		return compat;
	}

	@Override
	public String optimal() {
		return optimal;
	}

}
