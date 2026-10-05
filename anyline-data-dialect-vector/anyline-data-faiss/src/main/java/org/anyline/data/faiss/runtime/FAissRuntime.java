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


package org.anyline.data.faiss.runtime;

import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.runtime.init.AbstractRuntime;

/**
 * Faiss 运行环境<br/>
 * Faiss 官方只提供 C++/Python 实现, 没有官方 Java SDK, 也没有官方服务端协议<br/>
 * 所以这里的 client 由使用方注入: 可以是自建服务(如 faiss-server)的 Java 客户端, 也可以是 JNI 封装<br/>
 * 没有注入时 Actuator 不会抛异常, 而是返回空结果并输出提示
 */
public class FAissRuntime extends AbstractRuntime implements DataRuntime {

	protected Object client;

	public FAissRuntime() {
	}

	@Override
	public Object getProcessor() {
		return client;
	}

	@Override
	public void setProcessor(Object processor) {
		this.client = processor;
	}

	@Override
	public void setAdapterKey(String adapter) {
	}

	@Override
	public String getAdapterKey() {
		return null;
	}

	public FAissRuntime(String key, Object client, DriverAdapter adapter) {
		setKey(key);
		setProcessor(client);
		setAdapter(adapter);
	}

	public Object client() {
		return client;
	}

	@Override
	public String getFeature(boolean connection) {
		if(null == feature) {
			if(null != client) {
				feature = client.getClass().getName();
			}
		}
		return feature;
	}

	public void setClient(Object client) {
		this.client = client;
	}
}