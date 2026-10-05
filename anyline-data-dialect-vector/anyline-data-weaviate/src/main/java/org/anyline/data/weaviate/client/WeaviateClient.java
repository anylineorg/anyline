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


package org.anyline.data.weaviate.client;

import org.anyline.entity.DataRow;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;

import java.net.URI;
import java.net.URLEncoder;
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
 * Weaviate 客户端<br/>
 * 官方提供了 Java Client(io.weaviate:client), 但REST/GraphQL已足够覆盖查询场景且引入成本低, 这里按官方 REST API 实现<br/>
 * 接口参考 https://docs.weaviate.io/weaviate/api/rest (对象:/v1/objects 批量:/v1/batch/objects GraphQL:/v1/graphql)<br/>
 * 认证: Authorization: Bearer {token}(本地无认证实例可不配置)
 */
public class WeaviateClient {

	public static final int DEFAULT_PORT = 8080;

	private final String url;
	private final String token;
	private final HttpClient http;

	public WeaviateClient(String url, String token) {
		this(url, token, null);
	}

	public WeaviateClient(String url, String token, Map<String, Object> config) {
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
			throw new Exception("[Weaviate][http:" + code + "][url:" + uri + "][body:" + text + "]");
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
			request("GET", url + "/v1/meta", null);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * GET /v1/schema  class 对应 anyline 的 Table(集合)
	 */
	public List<Map<String, Object>> collections() throws Exception {
		List<Map<String, Object>> result = new ArrayList<>();
		Map<String, Object> resp = json(request("GET", url + "/v1/schema", null));
		for(Object item : list(resp.get("classes"))) {
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("name", map(item).get("class"));
			row.put("comment", map(item).get("description"));
			result.add(row);
		}
		return result;
	}

	/**
	 * POST /v1/schema  创建class
	 */
	public boolean createCollection(String collection, int size, String distance) throws Exception {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("class", collection);
		body.put("vectorizer", "none");
		request("POST", url + "/v1/schema", body);
		return true;
	}

	/**
	 * DELETE /v1/schema/{class}
	 */
	public boolean deleteCollection(String collection) throws Exception {
		request("DELETE", url + "/v1/schema/" + enc(collection), null);
		return true;
	}

	/* *********************************************************************************************
	 * 											写入/删除
	 ***********************************************************************************************/
	/**
	 * POST /v1/batch/objects  批量写入(不传id时由服务端生成uuid)
	 */
	public long upsert(String collection, List<Map<String, Object>> rows) throws Exception {
		if(null == rows || rows.isEmpty()) {
			return 0;
		}
		List<Object> objects = new ArrayList<>();
		for(Map<String, Object> row : rows) {
			Map<String, Object> object = new LinkedHashMap<>();
			object.put("class", collection);
			Object id = row.get("id");
			if(null == id) {
				id = row.get("_id");
			}
			if(null != id) {
				object.put("id", id);
			}
			List<Float> vector = vector(row);
			if(!vector.isEmpty()) {
				object.put("vector", vector);
			}
			Map<String, Object> properties = new LinkedHashMap<>(row);
			properties.remove("id");
			properties.remove("_id");
			properties.remove("vector");
			properties.remove("embedding");
			properties.remove("feature");
			object.put("properties", properties);
			objects.add(object);
		}
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("objects", objects);
		request("POST", url + "/v1/batch/objects", body);
		return objects.size();
	}

	/**
	 * DELETE /v1/objects/{class}/{id}
	 */
	public long delete(String collection, List<Object> ids) throws Exception {
		if(null == ids || ids.isEmpty()) {
			return 0;
		}
		long count = 0;
		for(Object id : ids) {
			request("DELETE", url + "/v1/objects/" + enc(collection) + "/" + enc(String.valueOf(id)), null);
			count++;
		}
		return count;
	}

	/* *********************************************************************************************
	 * 											查询
	 ***********************************************************************************************/
	/**
	 * POST /v1/graphql  nearVector 检索<br/>
	 * 说明: GraphQL 需要显式声明返回字段, 这里只取 _additional{id distance}, 属性可通过 GET /v1/objects 按id查询
	 */
	public List<Map<String, Object>> search(String collection, List<Float> vector, int topK, String filter) throws Exception {
		List<Map<String, Object>> result = new ArrayList<>();
		if(BasicUtil.isEmpty(collection) || null == vector || vector.isEmpty()) {
			return result;
		}
		StringBuilder builder = new StringBuilder();
		builder.append("{Get{").append(collection)
				.append("(nearVector:{vector:[").append(join(vector)).append("]}")
				.append(",limit:").append(topK <= 0 ? 10 : topK).append(")");
		if(BasicUtil.isNotEmpty(filter)) {
			//约定传入 where 过滤表达式, 如 {path:["city"],operator:Equal,valueText:"London"}
			builder.append(",where:").append(filter);
		}
		builder.append("){_additional{id distance}}}}");
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("query", builder.toString());
		Map<String, Object> resp = json(request("POST", url + "/v1/graphql", body));
		Map<String, Object> data = map(resp.get("data"));
		Object items = map(data.get("Get")).get(collection);
		for(Object item : list(items)) {
			result.add(object(item));
		}
		return result;
	}

	/**
	 * GET /v1/objects?class=X&limit=N&after={uuid}  遍历<br/>
	 * Weaviate 的游标是上一个对象的id(after), 这里先把 offset 换算成游标
	 */
	public List<Map<String, Object>> scroll(String collection, int limit, Object offset, String filter) throws Exception {
		List<Map<String, Object>> result = new ArrayList<>();
		if(BasicUtil.isEmpty(collection)) {
			return result;
		}
		if(limit <= 0) {
			limit = 100;
		}
		String uri = url + "/v1/objects?class=" + enc(collection) + "&limit=" + limit;
		int idx = 0;
		if(null != offset) {
			idx = Integer.parseInt(offset.toString());
		}
		if(idx > 0) {
			//取前 idx 条, 用最后一条的id作为游标
			String cursor = uri + "&limit=" + idx;
			Map<String, Object> resp = json(request("GET", cursor, null));
			List<Object> items = list(resp.get("objects"));
			if(items.isEmpty()) {
				return result;
			}
			Object last = map(items.get(items.size() - 1)).get("id");
			if(null == last) {
				return result;
			}
			uri += "&after=" + enc(String.valueOf(last));
		}
		Map<String, Object> resp = json(request("GET", uri, null));
		for(Object item : list(resp.get("objects"))) {
			result.add(object(item));
		}
		return result;
	}

	/**
	 * POST /v1/graphql  Aggregate 统计
	 */
	public long count(String collection, String filter) throws Exception {
		if(BasicUtil.isEmpty(collection)) {
			return -1;
		}
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("query", "{Aggregate{" + collection + "{meta{count}}}}");
		Map<String, Object> resp = json(request("POST", url + "/v1/graphql", body));
		Object aggregate = map(map(resp.get("data")).get("Aggregate")).get(collection);
		List<Object> rows = list(aggregate);
		if(rows.isEmpty()) {
			return -1;
		}
		Object count = map(map(rows.get(0)).get("meta")).get("count");
		if(null == count) {
			return -1;
		}
		return Long.parseLong(count.toString());
	}

	/* *********************************************************************************************
	 * 											辅助
	 ***********************************************************************************************/
	private Map<String, Object> object(Object item) {
		Map<String, Object> row = new LinkedHashMap<>();
		Map<String, Object> map = map(item);
		Object id = map.get("id");
		if(null != id) {
			row.put("id", id);
			row.put("_id", id);
		}
		Object additional = map.get("_additional");
		if(additional instanceof Map) {
			Map<String, Object> add = map(additional);
			if(null != add.get("id")) {
				row.put("id", add.get("id"));
				row.put("_id", add.get("id"));
			}
			if(null != add.get("distance")) {
				row.put("_distance", add.get("distance"));
				row.put("_score", add.get("distance"));
			}
		}
		Object properties = map.get("properties");
		if(properties instanceof Map) {
			row.putAll((Map<String, Object>) properties);
		}
		return row;
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
			for(String item : vector.toString().split(",")) {
				item = item.trim().replace("[", "").replace("]", "");
				if(!item.isEmpty()) {
					result.add(Float.parseFloat(item));
				}
			}
		}
		return result;
	}

	private String join(List<Float> vector) {
		StringBuilder builder = new StringBuilder();
		boolean first = true;
		for(Float value : vector) {
			if(!first) {
				builder.append(",");
			}
			first = false;
			builder.append(value);
		}
		return builder.toString();
	}

	private String enc(String value) {
		if(null == value) {
			return "";
		}
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}
}