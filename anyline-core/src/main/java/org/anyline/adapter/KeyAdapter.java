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

package org.anyline.adapter;

import org.anyline.adapter.init.LowerKeyAdapter;
import org.anyline.adapter.init.UpperKeyAdapter;
import org.anyline.adapter.init.SrcKeyAdapter;

import java.io.Serializable;

public interface KeyAdapter extends Serializable {
    public static enum KEY_CASE{
        CONFIG				{public String getCode(){return "CONFIG";} 			public String getName(){return "按配置文件";}},
        SRC					{public String getCode(){return "SRC";} 			public String getName(){return "不转换";}},
        UPPER				{public String getCode(){return "UPPER";} 			public String getName(){return "强制大写";} public String convert(String value){if(null == value) return null; else return value.toUpperCase();}},
        PUT_UPPER			{public String getCode(){return "PUT_UPPER";} 			public String getName(){return "强制put大写";} public String convert(String value){if(null == value) return null; else return value.toUpperCase();}},
        LOWER				{public String getCode(){return "LOWER";} 			public String getName(){return "强制小写";} public String convert(String value){if(null == value) return null; else return value.toLowerCase();}},
        // 以下规则取消
        // 下/中划线转成驼峰
        Camel 				{public String getCode(){return "Camel";} 			public String getName(){return "大驼峰";}},
        camel 				{public String getCode(){return "camel";} 			public String getName(){return "小驼峰";}},
        // bean驼峰属性转下划线
        CAMEL_CONFIG		{public String getCode(){return "CAMEL_CONFIG";} 	public String getName(){return "转下划线后按配置文件转换大小写";}},
        CAMEL_SRC			{public String getCode(){return "CAMEL_SRC";} 		public String getName(){return "转下划线后不转换大小写";}},
        CAMEL_UPPER			{public String getCode(){return "CAMEL_UPPER";} 	public String getName(){return "转下划线后强制大写";}},
        CAMEL_LOWER			{public String getCode(){return "CAMEL_LOWER";} 	public String getName(){return "转下划线后强制小写";}},

        AUTO				{public String getCode(){return "AUTO";} 			public String getName(){return "自动识别";}};

        public abstract String getName();
        public abstract String getCode();
        public String convert(String value){
            return value;
        }
    }
    public String key(String key);
    public KEY_CASE getKeyCase();
    public static KeyAdapter parse(KEY_CASE keyCase){
        KeyAdapter keyAdapter;
        switch (keyCase) {
            case UPPER:
                keyAdapter = UpperKeyAdapter.getInstance();
                break;
            case LOWER:
                keyAdapter = LowerKeyAdapter.getInstance();
                break;
            default:
                keyAdapter = SrcKeyAdapter.getInstance();
        }
        return keyAdapter;
    }

}
