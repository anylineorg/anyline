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

package org.anyline.environment.boot.nacos;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration("anyline.environment.boot.nacos")
@ConfigurationProperties(prefix = "anyline.nacos")
public class NacosProperty {

    public String address       ;
    public int port 			;
    public int timeout 			;
    public String namespace 	;   // 注意这里的命名空间要写ID而不是NAME, 如果用默认的public写成空白不要写public
    public String group 		;
    public boolean autoScan 	;
    public String scanPackage	;
    public String scanClass	;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public boolean isAutoScan() {
        return autoScan;
    }

    public void setAutoScan(boolean autoScan) {
        this.autoScan = autoScan;
    }

    public String getScanPackage() {
        return scanPackage;
    }

    public void setScanPackage(String scanPackage) {
        this.scanPackage = scanPackage;
    }

    public String getScanClass() {
        return scanClass;
    }

    public void setScanClass(String scanClass) {
        this.scanClass = scanClass;
    }
}
