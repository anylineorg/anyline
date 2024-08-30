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




package org.anyline.web.listener;

import org.anyline.data.param.ConfigStore;

import javax.servlet.http.HttpServletRequest;

public interface ControllerListener {
    /**
     * 封装完查询条件后调用
     * @param request rquest
     * @param configs 过滤条件及相关配置
     * @return ConfigStore
     */
    public ConfigStore after(HttpServletRequest request, ConfigStore configs);
}
