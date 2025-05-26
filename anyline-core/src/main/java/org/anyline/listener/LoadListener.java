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

package org.anyline.listener;

public interface LoadListener {
    /**
     * 顺序 0
     */
    default void before(Object object) {}
    /**
     * 顺序 1.上下文环境加初始化时(就是有了上下文对象ConfigTable.work)调用一次
     */
    void start();
    /**
     * 顺序 2.上下文中所有实例完成注入后调用一次
     * 需要等项目中的jdbc/datasource实例完成后 用来创建默认数据源的service
     * 以及在DataSourceHolder实例加载完成前就调用了reg产生的缓存数据源 所以需要延迟加载
     */
    default void finish() {}

    /**
     * 顺序 3
     */
    default void after() {}

    default int index() {
        return 0;
    }
}
