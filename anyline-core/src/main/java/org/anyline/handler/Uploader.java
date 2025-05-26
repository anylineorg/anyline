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

package org.anyline.handler;

import java.io.File;
import java.io.InputStream;

public interface Uploader {
    /**
     * 文件上传
     * @param name 文件名
     * @param file 文件
     * @return 返回文件上传后地址
     */
    default String upload(String name, File file) {
        return null;
    }
    default String upload(String name, InputStream is) {
        return null;
    }
}
