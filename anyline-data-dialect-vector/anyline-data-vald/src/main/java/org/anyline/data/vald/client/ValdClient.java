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


package org.anyline.data.vald.client;

import org.anyline.entity.DataRow;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Vald 客户端<br/>
 * 官方文档: https://vald.vdaas.org/docs/api/insert/ (gRPC) 与 https://vald.vdaas.org/docs/api/search/<br/>
 * 官方Java SDK(vald-client-java)未引入, 这里按 gateway 的 REST(JSON)接口实现<br/>
 * 说明: Vald 只有"向量"一个层级, 没有集合/库的概念(table 只作为逻辑分组, 实际写入全局空间)<br/>
 *       官方没有提供 count 与遍历(scroll)接口, 这两个方法返回 -1/空
 */
public class ValdClient {

	/** gateway 默认端口 */
	public static final int DEFAULT_PORT = 8081;

	private final String url;
	private final String token;
	private final HttpClient http;

	public ValdClient(String url, String token) {
		this(url, token, null);
	}

	public ValdClient(String url, String token, Map<String, Object> config) {
		this.url = format(url);
		this.token = token;
		this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
	}

	private static String format(String url) {
		if(null == url) {
			return null;
		}
		String result = url.trim();
		while(result.endsWith("/")) {
			result = result.substring(0, result.length() - 1);
		}
		if(!result.contains("://")) {
			result = "http://" + result;
		}
		return result;
	}

	public String url() {
		return url;
	}

	/* *********************************************************************************************
	 * 											HTTP
	 ***********************************************************************************************/
	private String request(String method, String uri, Map<String, Object> body) throws Exception {
		HttpRequest.Builder builder = HttpRequest.newBuilder()
				.uri(URI.create(uri))
				.timeout(Duration.ofSeconds(30))
				.header("Accept", "application/json");
		if(null != body) {
			builder.header("Content-Type", "application/json")
					.method(method, HttpRequest.BodyPublishers.ofString(BeanUtil.object2json(body), StandardCharsets.UTF_8));
		} else {
			builder.method(method, HttpRequest.BodyPublishers.noBody());
		}
		if(null != token && !token.isEmpty()) {
			builder.header("Authorization", "Bearer " + token);
		}
		HttpResponse<String> response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		int code = response.statusCode();
		String text = response.body();
		if(code >= 400) {
			throw new Exception("[Vald][http:" + code + "][url:" + uri + "][body:" + text + "]");
		}
		return text;
	}

	private Map<String, Object> map(Object value) {
		if(value instanceof Map) {
			return (Map<String, Object>) value;
		}
		return new LinkedHashMap<>();
	}

	private List<Object> list(Object value) {
		if(value instanceof List) {
			return (List<Object>) value;
		}
		return new ArrayList<>();
	}

	private Map<String, Object> json(String text) {
		if(BasicUtil.isEmpty(text)) {
			return new LinkedHashMap<>();
		}
		return DataRow.parseJson(text);
	}

	/* *********************************************************************************************
	 * 											元数据
	 ***********************************************************************************************/
	public boolean ping() {
		try {
			request("GET", url + "/healthz", null);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Vald 没有集合概念, 返回空
	 */
	public List<Map<String, Object>> collections() throws Exception {
		return new ArrayList<>();
	}

	public boolean createCollection(String collection, int size, String distance) throws Exception {
		//Vald 的索引结构由 agent 配置决定, 没有运行时建集合接口
		throw new Exception("[Vald][不支持运行时创建集合][请通过agent配置dimension:" + size + "]");
	}

	public boolean deleteCollection(String collection) throws Exception {
		throw new Exception("[Vald][不支持运行时删除集合][" + collection + "]");
	}

	/* *********************************************************************************************
	 * 											写入/删除
	 ***********************************************************************************************/
	/**
	 * POST /insert {"vector":{"id":"...","vector":[...]}}
	 */
	public long upsert(String collection, List<Map<String, Object>> rows) throws Exception {
		if(null == rows || rows.isEmpty()) {
			return 0;
		}
		long count = 0;
		for(Map<String, Object> row : rows) {
			Object id = row.get("id");
			if(null == id) {
				id = row.get("_id");
			}
			if(null == id) {
				continue;
			}
			List<Float> vector = vector(row);
			if(vector.isEmpty()) {
				continue;
			}
			Map<String, Object> vectorBody = new LinkedHashMap<>();
			vectorBody.put("id", String.valueOf(id));
			vectorBody.put("vector", vector);
			Map<String, Object> body = new LinkedHashMap<>();
			body.put("vector", vectorBody);
			request("POST", url + "/insert", body);
			count++;
		}
		return count;
	}

	/**
	 * POST /remove {"id":{"id":"..."}}
	 */
	public long delete(String collection, List<Object> ids) throws Exception {
		if(null == ids || ids.isEmpty()) {
			return 0;
		}
		long count = 0;
		for(Object id : ids) {
			Map<String, Object> idBody = new LinkedHashMap<>();
			idBody.put("id", String.valueOf(id));
			Map<String, Object> body = new LinkedHashMap<>();
			body.put("id", idBody);
			request("POST", url + "/remove", body);
			count++;
		}
		return count;
	}

	/* *********************************************************************************************
	 * 											查询
	 ***********************************************************************************************/
	/**
	 * POST /search {"vector":[...],"config":{"num":N}}  官方 ANN 检索接口
	 */
	public List<Map<String, Object>> search(String collection, List<Float> vector, int topK, String filter) throws Exception {
		List<Map<String, Object>> result = new ArrayList<>();
		if(null == vector || vector.isEmpty()) {
			return result;
		}
		Map<String, Object> config = new LinkedHashMap<>();
		config.put("num", topK <= 0 ? 10 : topK);
		if(BasicUtil.isNotEmpty(filter)) {
			//约定传入 config 的扩展配置(e.g. {"radius":-1,"epsilon":0.01,"timeout":"3s"})
			Map<String, Object> extend = json(filter);
			for(Map.Entry<String, Object> entry : extend.entrySet()) {
				config.put(entry.getKey(), entry.getValue());
			}
		}
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("vector", vector);
		body.put("config", config);
		Map<String, Object> resp = json(request("POST", url + "/search", body));
		for(Object item : list(resp.get("results"))) {
			Map<String, Object> row = new LinkedHashMap<>();
			Map<String, Object> map = map(item);
			Object id = map.get("id");
			if(null != id) {
				row.put("id", id);
				row.put("_id", id);
			}
			Object distance = map.get("distance");
			if(null != distance) {
				row.put("_distance", distance);
				row.put("_score", distance);
			}
			result.add(row);
		}
		return result;
	}

	/**
	 * Vald 官方没有提供遍历/列表接口
	 */
	public List<Map<String, Object>> scroll(String collection, int limit, Object offset, String filter) throws Exception {
		return new ArrayList<>();
	}

	/**
	 * Vald 官方没有提供 count 接口
	 */
	public long count(String collection, String filter) throws Exception {
		return -1;
	}

	/* *********************************************************************************************
	 * 											辅助
	 ***********************************************************************************************/
	private List<Float> vector(Map<String, Object> row) {
		List<Float> result = new ArrayList<>();
		Object vector = row.get("vector");
		if(null == vector) {
			vector = row.get("embedding");
		}
		if(null == vector) {
			vector = row.get("feature");
		}
		if(vector instanceof List) {
			for(Object item : (List<?>) vector) {
				result.add(Float.parseFloat(item.toString()));
			}
		} else if(vector instanceof float[]) {
			for(float item : (float[]) vector) {
				result.add(item);
			}
		} else if(null != vector && !(vector instanceof Map)) {
			for(String item : vector.toString().split(",")) {
				item = item.trim().replace("[", "").replace("]", "");
				if(!item.isEmpty()) {
					result.add(Float.parseFloat(item));
				}
			}
		}
		return result;
	}
}