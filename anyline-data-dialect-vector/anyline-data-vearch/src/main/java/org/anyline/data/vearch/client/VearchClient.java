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


package org.anyline.data.vearch.client;

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
 * Vearch 客户端<br/>
 * 官方文档: https://vearch.readthedocs.io (接口: /db/_create、/space/{db}/_create、/document/{db}/{space}、/{db}/{space}/_search)<br/>
 * Vearch 的层级是 db(库) → space(空间, 对应集合/表) → document(文档)<br/>
 * 认证: 自建实例一般用 basic auth(user:password), 这里把 token 作为 basic 凭证(未在配置文件中提供user时token即密码)
 */
public class VearchClient {

	/** router(API)默认端口 */
	public static final int DEFAULT_PORT = 9001;

	/** 默认库名, 可通过 config 的 index/database 覆盖 */
	private String db = "default";
	/** 默认向量字段名 */
	private String vectorField = "vector";
	/** basic 认证凭证 user:password */
	private String auth;

	private final String url;
	private final HttpClient http;

	public VearchClient(String url, String token) {
		this(url, token, null);
	}

	public VearchClient(String url, String token, Map<String, Object> config) {
		this.url = format(url);
		this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
		String user = null;
		if(null != config) {
			Object value = config.get("index");
			if(null == value) {
				value = config.get("database");
			}
			if(null != value) {
				this.db = value.toString();
			}
			Object field = config.get("vectorField");
			if(null != field) {
				this.vectorField = field.toString();
			}
			Object u = config.get("user");
			if(null != u) {
				user = u.toString();
			}
		}
		if(BasicUtil.isNotEmpty(token)) {
			this.auth = (BasicUtil.isEmpty(user) ? "" : user + ":") + token;
		}
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

	public String db() {
		return db;
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
		if(null != auth) {
			String basic = java.util.Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
			builder.header("Authorization", "Basic " + basic);
		}
		HttpResponse<String> response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		int code = response.statusCode();
		String text = response.body();
		if(code >= 400) {
			throw new Exception("[Vearch][http:" + code + "][url:" + uri + "][body:" + text + "]");
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
	 * 											库/空间
	 ***********************************************************************************************/
	public boolean ping() {
		try {
			request("GET", url + "/list/db", null);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * GET /list/space  返回当前库下的空间(集合)列表
	 */
	public List<Map<String, Object>> collections() throws Exception {
		List<Map<String, Object>> result = new ArrayList<>();
		Map<String, Object> resp = json(request("GET", url + "/list/space", null));
		for(Object item : list(resp.get("data"))) {
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("name", map(item).get("space_name"));
			row.put("comment", map(item).get("space_desc"));
			result.add(row);
		}
		return result;
	}

	/**
	 * POST /space/{db}/_create  创建空间(集合)
	 */
	public boolean createCollection(String collection, int size, String distance) throws Exception {
		Map<String, Object> index = new LinkedHashMap<>();
		index.put("name", "gamma");
		index.put("type", "FLAT");
		Map<String, Object> params = new LinkedHashMap<>();
		params.put("metric_type", BasicUtil.isEmpty(distance) ? "InnerProduct" : distance);
		index.put("params", params);
		Map<String, Object> field = new LinkedHashMap<>();
		field.put("name", vectorField);
		field.put("type", "vector");
		field.put("index", index);
		field.put("dimension", size <= 0 ? 128 : size);
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("name", collection);
		body.put("partition_num", 1);
		body.put("replica_num", 1);
		List<Object> fields = new ArrayList<>();
		fields.add(field);
		body.put("fields", fields);
		request("POST", url + "/space/" + enc(db) + "/_create", body);
		return true;
	}

	/**
	 * DELETE /space/{db}/{space}
	 */
	public boolean deleteCollection(String collection) throws Exception {
		request("DELETE", url + "/space/" + enc(db) + "/" + enc(collection), null);
		return true;
	}

	/* *********************************************************************************************
	 * 											写入/删除
	 ***********************************************************************************************/
	/**
	 * POST /document/{db}/{space}  写入文档(_id 作为文档id)
	 */
	public long upsert(String collection, List<Map<String, Object>> rows) throws Exception {
		if(null == rows || rows.isEmpty()) {
			return 0;
		}
		long count = 0;
		for(Map<String, Object> row : rows) {
			Object id = row.get("_id");
			if(null == id) {
				id = row.get("id");
			}
			Map<String, Object> body = new LinkedHashMap<>(row);
			if(null != id) {
				body.put("_id", id);
			}
			request("POST", url + "/document/" + enc(db) + "/" + enc(collection), body);
			count++;
		}
		return count;
	}

	/**
	 * DELETE /document/{db}/{space}/{id}
	 */
	public long delete(String collection, List<Object> ids) throws Exception {
		if(null == ids || ids.isEmpty()) {
			return 0;
		}
		long count = 0;
		for(Object id : ids) {
			request("DELETE", url + "/document/" + enc(db) + "/" + enc(collection) + "/" + enc(String.valueOf(id)), null);
			count++;
		}
		return count;
	}

	/* *********************************************************************************************
	 * 											查询
	 ***********************************************************************************************/
	/**
	 * POST /{db}/{space}/_search  向量检索
	 */
	public List<Map<String, Object>> search(String collection, List<Float> vector, int topK, String filter) throws Exception {
		List<Map<String, Object>> result = new ArrayList<>();
		if(BasicUtil.isEmpty(collection) || null == vector || vector.isEmpty()) {
			return result;
		}
		Map<String, Object> feature = new LinkedHashMap<>();
		feature.put("field", vectorField);
		feature.put("feature", vector);
		Map<String, Object> query = new LinkedHashMap<>();
		List<Object> sum = new ArrayList<>();
		sum.add(feature);
		query.put("sum", sum);
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("query", query);
		body.put("size", topK <= 0 ? 10 : topK);
		if(BasicUtil.isNotEmpty(filter)) {
			//约定传入官方 filter 的json数组, 如 [{"field":"city","operator":"=","value":"London"}]
			body.put("filters", json(filter).get("filters"));
		}
		Map<String, Object> resp = json(request("POST", url + "/" + enc(db) + "/" + enc(collection) + "/_search", body));
		for(Object item : list(map(resp.get("hits")).get("hits"))) {
			result.add(hit(item));
		}
		return result;
	}

	/**
	 * POST /{db}/{space}/_search  size=0 时只返回总数, 这里用 size 取一页做遍历
	 */
	public List<Map<String, Object>> scroll(String collection, int limit, Object offset, String filter) throws Exception {
		List<Map<String, Object>> result = new ArrayList<>();
		if(BasicUtil.isEmpty(collection)) {
			return result;
		}
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("size", limit <= 0 ? 100 : limit);
		if(null != offset) {
			body.put("from", offset);
		}
		Map<String, Object> resp = json(request("POST", url + "/" + enc(db) + "/" + enc(collection) + "/_search", body));
		for(Object item : list(map(resp.get("hits")).get("hits"))) {
			result.add(hit(item));
		}
		return result;
	}

	/**
	 * 统计: _search 返回 hits.total
	 */
	public long count(String collection, String filter) throws Exception {
		if(BasicUtil.isEmpty(collection)) {
			return -1;
		}
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("size", 0);
		Map<String, Object> resp = json(request("POST", url + "/" + enc(db) + "/" + enc(collection) + "/_search", body));
		Object total = map(resp.get("hits")).get("total");
		if(null == total) {
			return -1;
		}
		return Long.parseLong(total.toString());
	}

	/* *********************************************************************************************
	 * 											辅助
	 ***********************************************************************************************/
	private Map<String, Object> hit(Object item) {
		Map<String, Object> row = new LinkedHashMap<>();
		Map<String, Object> map = map(item);
		Object id = map.get("_id");
		if(null != id) {
			row.put("_id", id);
			row.put("id", id);
		}
		Object score = map.get("_score");
		if(null != score) {
			row.put("_score", score);
			row.put("_distance", score);
		}
		Object source = map.get("_source");
		if(source instanceof Map) {
			row.putAll((Map<String, Object>) source);
		}
		return row;
	}

	private String enc(String value) {
		if(null == value) {
			return "";
		}
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}
}