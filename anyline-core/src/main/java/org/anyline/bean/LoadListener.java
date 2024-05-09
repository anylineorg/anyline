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



package org.anyline.bean;

public interface LoadListener {
    /**
     * 1.上下文环境加初始化时(就是有了上下文对象时)调用一次
     *  因为加载过程中有需要引用数据源的类(如果没有这次调用，依赖数据源的注解需要@Lazy)
     * 2.上下文中所有实例完成注入后调用一次
     * anyline需要等项目中的jdbc/datasource实例完成后 用来创建默认数据源的service
     */
    void load();
}
