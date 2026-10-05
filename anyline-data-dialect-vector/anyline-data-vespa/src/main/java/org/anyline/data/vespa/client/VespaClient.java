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


package org.anyline.data.vespa.client;

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
 * Vespa 客户端<br/>
 * 官方文档: https://docs.vespa.ai/en/document-v1-api-guide.html (写入) https://docs.vespa.ai/en/query-api.html (查询)<br/>
 * 这里按官方 REST API 实现: 文档读写 /document/v1/{documentType}/docid/{id}, 查询 /search/?yql=...<br/>
 * 认证: Vespa Cloud 使用 Bearer token(或mTLS证书), 自建实例通常无认证
 */
public class VespaClient {

	public static final int DEFAULT_PORT = 8080;

	/** 默认向量字段名, 可通过 config 的 vectorField 覆盖 */
	private String vectorField = "vector";
	/** 默认命名空间, 空表示使用Vespa默认namespace */
	private String namespace = "";

	private final String url;
	private final String token;
	private final HttpClient http;

	public VespaClient(String url, String token) {
		this(url, token, null);
	}

	public VespaClient(String url, String token, Map<String, Object> config) {
		this.url = format(url);
		this.token = token;
		this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
		if(null != config) {
			Object field = config.get("vectorField");
			if(null != field) {
				this.vectorField = field.toString();
			}
			Object ns = config.get("namespace");
			if(null != ns) {
				this.namespace = ns.toString();
			}
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

	public String vectorField() {
		return vectorField;
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
			throw new Exception("[Vespa][http:" + code + "][url:" + uri + "][body:" + text + "]");
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
	 * 											文档类型(对应Table)
	 ***********************************************************************************************/
	public boolean ping() {
		try {
			request("GET", url + "/ApplicationStatus", null);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * GET /document/v1/  Vespa没有"集合列表"接口, 文档类型由部署的schema决定, 这里从 /document/v1/ 解析
	 */
	public List<Map<String, Object>> collections() throws Exception {
		List<Map<String, Object>> result = new ArrayList<>();
		Map<String, Object> resp = json(request("GET", url + "/document/v1/", null));
		Object types = resp.get("documentTypes");
		if(types instanceof Map) {
			for(Object key : ((Map<?, ?>) types).keySet()) {
				Map<String, Object> row = new LinkedHashMap<>();
				row.put("name", key);
				result.add(row);
			}
		} else {
			for(Object item : list(types)) {
				Map<String, Object> row = new LinkedHashMap<>();
				row.put("name", map(item).get("name"));
				result.add(row);
			}
		}
		return result;
	}

	public boolean createCollection(String collection, int size, String distance) throws Exception {
		//Vespa 的文档类型由应用包部署(application package)创建, 没有运行时建表接口
		throw new Exception("[Vespa][不支持运行时创建文档类型][请通过应用包部署schema:" + collection + "]");
	}

	public boolean deleteCollection(String collection) throws Exception {
		throw new Exception("[Vespa][不支持运行时删除文档类型][" + collection + "]");
	}

	/* *********************************************************************************************
	 * 											写入/删除
	 ***********************************************************************************************/
	/**
	 * POST /document/v1/{documentType}/docid/{id}  写入(存在则覆盖)
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
			Map<String, Object> fields = new LinkedHashMap<>(row);
			fields.remove("id");
			fields.remove("_id");
			Map<String, Object> body = new LinkedHashMap<>();
			body.put("fields", fields);
			request("POST", path(collection) + "/" + enc(String.valueOf(id)), body);
			count++;
		}
		return count;
	}

	/**
	 * DELETE /document/v1/{documentType}/docid/{id}
	 */
	public long delete(String collection, List<Object> ids) throws Exception {
		if(null == ids || ids.isEmpty()) {
			return 0;
		}
		long count = 0;
		for(Object id : ids) {
			request("DELETE", path(collection) + "/" + enc(String.valueOf(id)), null);
			count++;
		}
		return count;
	}

	/* *********************************************************************************************
	 * 											查询
	 ***********************************************************************************************/
	/**
	 * GET /search/?yql=select * from sources {type} where ({targetHits:N}nearestNeighbor({field},query_vector))<br/>
	 * 官方向量检索语法: nearestNeighbor(field, query_vector), 向量通过 ranking.features.query(query_vector) 传入
	 */
	public List<Map<String, Object>> search(String collection, List<Float> vector, int topK, String filter) throws Exception {
		List<Map<String, Object>> result = new ArrayList<>();
		if(BasicUtil.isEmpty(collection) || null == vector || vector.isEmpty()) {
			return result;
		}
		int hits = topK <= 0 ? 10 : topK;
		StringBuilder yql = new StringBuilder();
		yql.append("select * from ").append(collection).append(" where ");
		if(BasicUtil.isNotEmpty(filter)) {
			yql.append("(").append(filter).append(") and ");
		}
		yql.append("({targetHits:").append(hits).append("}nearestNeighbor(").append(vectorField).append(",query_vector))");
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("yql", yql.toString());
		body.put("hits", hits);
		Map<String, Object> query = new LinkedHashMap<>();
		query.put("query_vector", vector);
		body.put("ranking.features", query);
		Map<String, Object> resp = json(request("POST", url + "/search/", body));
		for(Object item : list(map(resp.get("root")).get("children"))) {
			result.add(hit(item));
		}
		return result;
	}

	/**
	 * GET /search/?yql=select * from {type} where true&hits=N&offset=M  遍历
	 */
	public List<Map<String, Object>> scroll(String collection, int limit, Object offset, String filter) throws Exception {
		List<Map<String, Object>> result = new ArrayList<>();
		if(BasicUtil.isEmpty(collection)) {
			return result;
		}
		int hits = limit <= 0 ? 100 : limit;
		StringBuilder yql = new StringBuilder();
		yql.append("select * from ").append(collection).append(" where ");
		if(BasicUtil.isNotEmpty(filter)) {
			yql.append(filter);
		} else {
			yql.append("true");
		}
		String uri = url + "/search/?yql=" + enc(yql.toString()) + "&hits=" + hits;
		if(null != offset) {
			uri += "&offset=" + offset;
		}
		Map<String, Object> resp = json(request("GET", uri, null));
		for(Object item : list(map(resp.get("root")).get("children"))) {
			result.add(hit(item));
		}
		return result;
	}

	/**
	 * 统计: 查询 hits=0 返回 totalCount
	 */
	public long count(String collection, String filter) throws Exception {
		if(BasicUtil.isEmpty(collection)) {
			return -1;
		}
		StringBuilder yql = new StringBuilder();
		yql.append("select * from ").append(collection).append(" where ");
		if(BasicUtil.isNotEmpty(filter)) {
			yql.append(filter);
		} else {
			yql.append("true");
		}
		String uri = url + "/search/?yql=" + enc(yql.toString()) + "&hits=0";
		Map<String, Object> resp = json(request("GET", uri, null));
		Object total = map(resp.get("root")).get("totalCount");
		if(null == total) {
			return -1;
		}
		return Long.parseLong(total.toString());
	}

	/* *********************************************************************************************
	 * 											辅助
	 ***********************************************************************************************/
	private String path(String documentType) {
		if(BasicUtil.isEmpty(namespace)) {
			return url + "/document/v1/" + documentType + "/docid";
		}
		return url + "/document/v1/" + namespace + "/" + documentType + "/docid";
	}

	private Map<String, Object> hit(Object item) {
		Map<String, Object> row = new LinkedHashMap<>();
		Map<String, Object> map = map(item);
		Object id = map.get("id");
		if(null != id) {
			row.put("id", id);
			row.put("_id", id);
		}
		Object relevance = map.get("relevance");
		if(null != relevance) {
			row.put("_score", relevance);
			row.put("_distance", relevance);
		}
		Object fields = map.get("fields");
		if(fields instanceof Map) {
			row.putAll((Map<String, Object>) fields);
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