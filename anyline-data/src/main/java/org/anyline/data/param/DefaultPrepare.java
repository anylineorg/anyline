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


package org.anyline.data.param;

public class DefaultPrepare {
    public static String[] split(String src, String split){
        if(null == src){
            return null;
        }
        if(null == split){
            if(src.contains("|")) {
                split = "|";
            }else{
                split = ",";
            }
        }

        if("|".equals(split)){
            split = "\\|";
        }
        return src.split(split);
    }
    public static String[] split(String src){
        return split(src, null);
    }
}
