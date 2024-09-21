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

package org.anyline.data.jdbc.clickhouse;

import org.anyline.data.metadata.PropertyCompat;
import org.anyline.metadata.*;

public enum ClickHousePropertyCompat implements PropertyCompat {
	DEFAULT                        (Table.class  ,"ENGINE" ,"DEFAULT"                      ,"MergeTree"                    ),
	AggregatingMergeTree           (Table.class  ,"ENGINE" ,"AggregatingMergeTree"         ,"AggregatingMergeTree"         ),
	Buffer                         (Table.class  ,"ENGINE" ,"Buffer"                       ,"Buffer"                       ),
	CollapsingMergeTree            (Table.class  ,"ENGINE" ,"CollapsingMergeTree"          ,"CollapsingMergeTree"          ),
	Dictionary                     (Table.class  ,"ENGINE" ,"Dictionary"                   ,"Dictionary"                   ),
	Distributed                    (Table.class  ,"ENGINE" ,"Distributed"                  ,"Distributed"                  ),
	File                           (Table.class  ,"ENGINE" ,"File"                         ,"File"                         ),
	GraphiteMergeTree              (Table.class  ,"ENGINE" ,"GraphiteMergeTree"            ,"GraphiteMergeTree"            ),
	InnoDB                         (Table.class  ,"ENGINE" ,"InnoDB"                       ,null                           ),
	Join                           (Table.class  ,"ENGINE" ,"Join"                         ,"Join"                         ),
	Log                            (Table.class  ,"ENGINE" ,"Log"                          ,"Log"                          ),
	MaterializedView               (Table.class  ,"ENGINE" ,"MaterializedView"             ,"MaterializedView"             ),
	Memory                         (Table.class  ,"ENGINE" ,"Memory"                       ,"Memory"                       ),
	Merge                          (Table.class  ,"ENGINE" ,"Merge"                        ,"Merge"                        ),
	MergeTree                      (Table.class  ,"ENGINE" ,"MergeTree"                    ,"MergeTree"                    ),
	Null                           (Table.class  ,"ENGINE" ,"Null"                         ,"Null"                         ),
	ReplacingMergeTree             (Table.class  ,"ENGINE" ,"ReplacingMergeTree"           ,"ReplacingMergeTree"           ),
	Set                            (Table.class  ,"ENGINE" ,"Set"                          ,"Set"                          ),
	StripeLog                      (Table.class  ,"ENGINE" ,"StripeLog"                    ,"StripeLog"                    ),
	SummingMergeTree               (Table.class  ,"ENGINE" ,"SummingMergeTree"             ,"SummingMergeTree"             ),
	TinyLog                        (Table.class  ,"ENGINE" ,"TinyLog"                      ,"TinyLog"                      ),
	URL                            (Table.class  ,"ENGINE" ,"URL"                          ,"URL"                          ),
	VersionedCollapsingMergeTree   (Table.class  ,"ENGINE" ,"VersionedCollapsingMergeTree" ,"VersionedCollapsingMergeTree" ),
	View                           (Table.class  ,"ENGINE" ,"View"                         ,"View"                         );

	private Class<? extends Metadata> metadata  ; // 适用类型 如Table.class
	private String property                     ; // 属性分组 如 ENGINE
	private String compat                   ; // 兼容属性名称或别名
	private String optimal                      ; // 适用当前数据库的属性值

	ClickHousePropertyCompat(Class<? extends Metadata> metadata, String property, String compat, String optimal) {
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
