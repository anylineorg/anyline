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


package org.anyline.data.qdrant.client;

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
 * Qdrant 客户端<br/>
 * 官方 Java SDK(io.qdrant:client)未引入(依赖不可用), 这里按官方 REST API 实现, HTTP 使用 JDK 自带 java.net.http<br/>
 * 接口参考 https://api.qdrant.tech/api-reference (官方文档: https://qdrant.tech/documentation/)<br/>
 * 认证: Qdrant Cloud 与开启 api-key 的实例使用 api-key 请求头
 */
public class QdrantClient {

	public static final int DEFAULT_PORT = 6333;

	private final String url;
	private final String token;
	private final HttpClient http;

	public QdrantClient(String url, String token) {
		this(url, token, null);
	}

	public QdrantClient(String url, String token, Map<String, Object> config) {
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
			//官方认证头
			builder.header("api-key", token);
		}
		HttpResponse<String> response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		int code = response.statusCode();
		String text = response.body();
		if(code >= 400) {
			throw new Exception("[Qdrant][http:" + code + "][url:" + uri + "][body:" + text + "]");
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

	/**
	 * 过滤条件: 约定传入 Qdrant filter 的json字符串, 如 {"must":[{"key":"city","match":{"value":"London"}}]}
	 */
	private Map<String, Object> filter(String filter) {
		if(BasicUtil.isEmpty(filter)) {
			return null;
		}
		return json(filter);
	}

	/* *********************************************************************************************
	 * 											集合(对应Table)
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
	 * GET /collections
	 */
	public List<Map<String, Object>> collections() throws Exception {
		List<Map<String, Object>> result = new ArrayList<>();
		Map<String, Object> resp = json(request("GET", url + "/collections", null));
		Object items = map(resp.get("result")).get("collections");
		for(Object item : list(items)) {
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("name", map(item).get("name"));
			Object vectors = map(item).get("vectors");
			if(vectors instanceof Map && !((Map) vectors).isEmpty()) {
				//命名向量时取第一个配置
				Object first = new ArrayList<>(((Map) vectors).values()).get(0);
				row.put("size", map(first).get("size"));
				row.put("distance", map(first).get("distance"));
			} else {
				row.put("size", map(vectors).get("size"));
				row.put("distance", map(vectors).get("distance"));
			}
			result.add(row);
		}
		return result;
	}

	/**
	 * PUT /collections/{name}  创建集合(向量维度+距离:Cosine/Euclid/Dot)
	 */
	public boolean createCollection(String collection, int size, String distance) throws Exception {
		Map<String, Object> vectors = new LinkedHashMap<>();
		vectors.put("size", size);
		vectors.put("distance", BasicUtil.isEmpty(distance) ? "Cosine" : distance);
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("vectors", vectors);
		request("PUT", url + "/collections/" + enc(collection), body);
		return true;
	}

	/**
	 * DELETE /collections/{name}
	 */
	public boolean deleteCollection(String collection) throws Exception {
		request("DELETE", url + "/collections/" + enc(collection), null);
		return true;
	}

	/* *********************************************************************************************
	 * 											写入/删除
	 ***********************************************************************************************/
	/**
	 * PUT /collections/{name}/points  写入(存在则覆盖)
	 * 行中的 id/_id 作为点id, vector/embedding/feature 作为向量, 其余字段作为 payload
	 */
	public long upsert(String collection, List<Map<String, Object>> rows) throws Exception {
		if(null == rows || rows.isEmpty()) {
			return 0;
		}
		List<Object> points = new ArrayList<>();
		for(Map<String, Object> row : rows) {
			Map<String, Object> point = new LinkedHashMap<>();
			point.put("id", id(row));
			List<Float> vector = vector(row);
			if(!vector.isEmpty()) {
				point.put("vector", vector);
			}
			Map<String, Object> payload = new LinkedHashMap<>(row);
			payload.remove("id");
			payload.remove("_id");
			payload.remove("vector");
			payload.remove("embedding");
			payload.remove("feature");
			point.put("payload", payload);
			points.add(point);
		}
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("points", points);
		request("PUT", url + "/collections/" + enc(collection) + "/points?wait=true", body);
		return points.size();
	}

	/**
	 * POST /collections/{name}/points/delete  按id删除
	 */
	public long delete(String collection, List<Object> ids) throws Exception {
		if(null == ids || ids.isEmpty()) {
			return 0;
		}
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("points", ids);
		request("POST", url + "/collections/" + enc(collection) + "/points/delete?wait=true", body);
		return ids.size();
	}

	/* *********************************************************************************************
	 * 											查询
	 ***********************************************************************************************/
	/**
	 * POST /collections/{name}/points/search  向量检索
	 */
	public List<Map<String, Object>> search(String collection, List<Float> vector, int topK, String filter) throws Exception {
		List<Map<String, Object>> result = new ArrayList<>();
		if(BasicUtil.isEmpty(collection) || null == vector || vector.isEmpty()) {
			return result;
		}
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("vector", vector);
		body.put("limit", topK <= 0 ? 10 : topK);
		body.put("with_payload", true);
		body.put("with_vector", false);
		Map<String, Object> ft = filter(filter);
		if(null != ft) {
			body.put("filter", ft);
		}
		Map<String, Object> resp = json(request("POST", url + "/collections/" + enc(collection) + "/points/search", body));
		for(Object item : list(map(resp.get("result")).get("result"))) {
			result.add(point(item));
		}
		return result;
	}

	/**
	 * POST /collections/{name}/points/scroll  按条件遍历(无检索向量时)
	 */
	public List<Map<String, Object>> scroll(String collection, int limit, Object offset, String filter) throws Exception {
		List<Map<String, Object>> result = new ArrayList<>();
		if(BasicUtil.isEmpty(collection)) {
			return result;
		}
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("limit", limit <= 0 ? 100 : limit);
		body.put("with_payload", true);
		body.put("with_vector", false);
		if(null != offset) {
			body.put("offset", offset);
		}
		Map<String, Object> ft = filter(filter);
		if(null != ft) {
			body.put("filter", ft);
		}
		Map<String, Object> resp = json(request("POST", url + "/collections/" + enc(collection) + "/points/scroll", body));
		Map<String, Object> data = map(map(resp.get("result")).get("result"));
		for(Object item : list(data.get("points"))) {
			result.add(point(item));
		}
		return result;
	}

	/**
	 * POST /collections/{name}/points/count
	 */
	public long count(String collection, String filter) throws Exception {
		if(BasicUtil.isEmpty(collection)) {
			return -1;
		}
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("exact", true);
		Map<String, Object> ft = filter(filter);
		if(null != ft) {
			body.put("filter", ft);
		}
		Map<String, Object> resp = json(request("POST", url + "/collections/" + enc(collection) + "/points/count", body));
		Object count = map(resp.get("result")).get("count");
		if(null == count) {
			return -1;
		}
		return Long.parseLong(count.toString());
	}

	/* *********************************************************************************************
	 * 											辅助
	 ***********************************************************************************************/
	/**
	 * 点结果转行: id + payload展开 + _score(相似度)
	 */
	private Map<String, Object> point(Object item) {
		Map<String, Object> row = new LinkedHashMap<>();
		Map<String, Object> map = map(item);
		Object id = map.get("id");
		if(null != id) {
			row.put("id", id);
			row.put("_id", id);
		}
		Object score = map.get("score");
		if(null != score) {
			row.put("_score", score);
			row.put("_distance", score);
		}
		Object payload = map.get("payload");
		if(payload instanceof Map) {
			row.putAll((Map<String, Object>) payload);
		}
		return row;
	}

	private Object id(Map<String, Object> row) {
		Object id = row.get("id");
		if(null == id) {
			id = row.get("_id");
		}
		if(null == id) {
			id = System.currentTimeMillis() + "" + (int) (Math.random() * 100000);
		}
		return id;
	}

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
			//逗号分隔的字符串
			for(String item : vector.toString().split(",")) {
				item = item.trim().replace("[", "").replace("]", "");
				if(!item.isEmpty()) {
					result.add(Float.parseFloat(item));
				}
			}
		}
		return result;
	}

	private String enc(String value) {
		if(null == value) {
			return "";
		}
		return value.replace(" ", "%20");
	}
}