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



package org.anyline.data.handler;

import org.anyline.entity.DataRow;

public interface DataRowHandler extends StreamHandler{

    /**
     * 在while(ResultSet.next())遍历中调用
     * @param row 返回通过ResultSet中的一行
     * @return boolean 返回false表示中断遍历,read方法不再再次被调用
     */
    boolean read(DataRow row);
}
