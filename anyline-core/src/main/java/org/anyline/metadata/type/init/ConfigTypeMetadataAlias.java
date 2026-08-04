/*
 * Copyright 2006-2026 DeepBit Co.,Ltd. All rights reserved.
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


package org.anyline.metadata.type.init;

import org.anyline.entity.DataRow;
import org.anyline.metadata.type.TypeMetadata;
import org.anyline.metadata.type.TypeMetadataAlias;

public class ConfigTypeMetadataAlias implements TypeMetadataAlias {
    private String input                     ; // 输入名称(根据输入名称转换成标准类型)(名称与枚举名不一致的需要,如带空格的)
    private TypeMetadata standard            ; // 标准类型
    private String meta                      ; // SQL数据类型名称
    private String formula                   ; // SQL最终数据类型公式
    private int ignoreLength            = -1 ; // 是否忽略长度
    private int ignorePrecision         = -1 ; // 是否忽略有效位数
    private int ignoreScale             = -1 ; // 是否忽略小数位数
    private int supportTimeZone         = -1 ; // 是否支持时区数
    private int supportLocalTimeZone    = -1 ; // 是否支持本地时区
    private int maxLength               = -1 ; // 最大长度
    private int maxPrecision            = -1 ; // 最大有效位数
    private int maxScale                = -1 ; // 最大小数位数
    private String lengthRefer               ; // 读取元数据依据-长度
    private String precisionRefer            ; // 读取元数据依据-有效位数
    private String scaleRefer                ; // 读取元数据依据-小数位数
    private TypeMetadata.Refer refer         ; // 集成元数据读写配置

    public ConfigTypeMetadataAlias(){}
    public ConfigTypeMetadataAlias(DataRow config){
        this.input = config.getString("ALIAS_INPUT");
        this.meta = config.getString("SQL_META_CODE");
        this.formula = config.getString("SQL_FORMULA");
        this.ignoreLength = config.getInt("IGNORE_LENGTH", -1);
        this.ignorePrecision = config.getInt("IGNORE_PRECISION", -1);
        this.ignoreScale = config.getInt("IGNORE_SCALE", -1);
        this.lengthRefer = config.getString("LENGTH_REFER");
        this.precisionRefer = config.getString("PRECISION_REFER");
        this.scaleRefer = config.getString("SCALE_REFER");
        this.maxLength = config.getInt("MAX_LENGTH", -1);
        this.maxPrecision = config.getInt("MAX_PRECISION", -1);
        this.maxScale = config.getInt("MAX_SCALE", -1);
        this.supportTimeZone = config.getInt("SUPPORT_TIME_ZONE", -1);
        this.supportLocalTimeZone = config.getInt("SUPPORT_LOCAL_TIME_ZONE", -1);
        if(config.isNotEmpty("STANDARD_ENUM_VAR")) {
            this.standard = StandardTypeMetadata.valueOf(config.getString("STANDARD_ENUM_VAR"));
        }
    }

    @Override
    public String input() {
        return input;
    }

    @Override
    public TypeMetadata standard() {
        return standard;
    }

    @Override
    public TypeMetadata.Refer refer() {
        if(null == refer) {
            refer = new TypeMetadata.Refer();
            if(null != meta) {
                refer.setMeta(meta);
            }
            if(null != formula) {
                refer.setFormula(formula);
            }
            if(null != lengthRefer) {
                refer.setLengthRefer(lengthRefer);
            }
            if(null != precisionRefer) {
                refer.setPrecisionRefer(precisionRefer);
            }
            if(null != scaleRefer) {
                refer.setScaleRefer(scaleRefer);
            }
            if(-1 != ignoreLength) {
                refer.ignoreLength(ignoreLength);
            }
            if(-1 != ignorePrecision) {
                refer.ignorePrecision(ignorePrecision);
            }
            if(-1 != ignoreScale) {
                refer.ignoreScale(ignoreScale);
            }
            if(-1 != supportTimeZone) {
                refer.supportTimeZone(supportTimeZone);
            }
            if(-1 != supportLocalTimeZone) {
                refer.supportLocalTimeZone(supportLocalTimeZone);
            }
            if(-1 != maxLength) {
                refer.maxLength(maxLength);
            }
            if(-1 != maxPrecision) {
                refer.maxPrecision(maxPrecision);
            }
            if(-1 != maxScale) {
                refer.maxScale(maxScale);
            }
        }
        return refer;
    }
}