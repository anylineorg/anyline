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

package org.anyline.runtime.init;

import org.anyline.annotation.AnylineComponent;
import org.anyline.runtime.Context;
import org.anyline.runtime.ExpressionActuator;
import org.anyline.util.BasicUtil;

import java.util.LinkedHashMap;

@AnylineComponent
public class NumberActuator implements ExpressionActuator {

    @Override
    public String tag() {
        return "number";
    }

    /**
     * 随机数字
     * @param context context
     * @param namespace namespace
     * @param tag 标签名 有可能是多重如 number:10:100 或number:8位
     * @param attributes 标签属性 min="10" max="100" length="8"
     * @param body 标题体 空
     * @return value
     */
    @Override
    public Object run(Context context, String namespace, String tag, LinkedHashMap<String, String> attributes, String body) {
        if(tag.contains((":"))){
            String[] tmps = tag.split(":");
            if(tmps.length> 3){
                //随机8位
                //aov:number:0:100
                int min = BasicUtil.parseInt(tmps[2], 0);
                int max = BasicUtil.parseInt(tmps[3], 0);
                return BasicUtil.getRandomNumber(min, max);
            }
            int len = 8;
            if(tmps.length> 2){
                //随机8位
                //aov:number:8(默认8位)
                len = BasicUtil.parseInt(tmps[2], len);
            }
            return BasicUtil.getRandomNumberString(len);
        }
        return BasicUtil.getRandomNumberString(8);
    }
}
