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


package org.anyline.data.couchdb.client;

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
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CouchDB 客户端<br/>
 * 官方文档: https://docs.couchdb.org/en/stable/api/index.html<br/>
 * CouchDB 没有官方Java SDK(官方推荐 HTTP API 或第三方 Ektorp/LightCouch), 这里按官方 REST API 实现<br/>
 * 层级: db(库, 对应 anyline 的 Table/集合) → document(文档, 对应行, 内置字段 _id/_rev)<br/>
 * 认证: Basic auth(user:password), 未配置时不发送认证头(admin party)
 */
public class CouchDBClient {

	public static final int DEFAULT_PORT = 5984;

	private final String url;
	private final String auth;
	private final HttpClient http;

	public CouchDBClient(String url, String token) {
		this(url, token, null);
	}

	public CouchDBClient(String url, String token, Map<String, Object> config) {
		this.url = format(url);
		this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
		String user = null;
		if(null != config) {
			Object u = config.get("user");
			if(null != u) {
				user = u.toString();
			}
		}
		if(BasicUtil.isNotEmpty(token)) {
			this.auth = (BasicUtil.isEmpty(user) ? "" : user + ":") + token;
		} else {
			this.auth = null;
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
			builder.header("Authorization", "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8)));
		}
		HttpResponse<String> response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		int code = response.statusCode();
		String text = response.body();
		if(code >= 400) {
			throw new Exception("[CouchDB][http:" + code + "][url:" + uri + "][body:" + text + "]");
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
	 * 过滤条件: 约定传入官方 selector 的json, 如 {"type":"user","age":{"$gt":18}}
	 */
	private Map<String, Object> selector(String filter) {
		if(BasicUtil.isEmpty(filter)) {
			return null;
		}
		return json(filter);
	}

	/* *********************************************************************************************
	 * 											库(对应Table)
	 ***********************************************************************************************/
	public boolean ping() {
		try {
			request("GET", url + "/", null);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * GET /_all_dbs
	 */
	public List<Map<String, Object>> collections() throws Exception {
		List<Map<String, Object>> result = new ArrayList<>();
		List<Object> dbs = list(json(request("GET", url + "/_all_dbs", null)).get("data"));
		if(dbs.isEmpty()) {
			//DataRow.parseJson 对纯数组的处理不一致时, 尝试直接解析
			dbs = parseArray(request("GET", url + "/_all_dbs", null));
		}
		for(Object item : dbs) {
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("name", item);
			result.add(row);
		}
		return result;
	}

	/**
	 * 兼容纯数组响应(如 ["_users","mydb"])
	 */
	private List<Object> parseArray(String text) {
		List<Object> result = new ArrayList<>();
		if(BasicUtil.isEmpty(text)) {
			return result;
		}
		String trim = text.trim();
		if(trim.startsWith("[") && trim.endsWith("]")) {
			Map<String, Object> wrap = DataRow.parseJson("{\"data\":" + trim + "}");
			result = list(wrap.get("data"));
		}
		return result;
	}

	/**
	 * PUT /{db}
	 */
	public boolean createCollection(String collection, int size, String distance) throws Exception {
		request("PUT", url + "/" + enc(collection), null);
		return true;
	}

	/**
	 * DELETE /{db}
	 */
	public boolean deleteCollection(String collection) throws Exception {
		request("DELETE", url + "/" + enc(collection), null);
		return true;
	}

	/* *********************************************************************************************
	 * 											写入/删除
	 ***********************************************************************************************/
	/**
	 * POST /{db}/_bulk_docs  批量写入(带 _rev 表示更新, 不带表示新增)
	 */
	public long upsert(String collection, List<Map<String, Object>> rows) throws Exception {
		if(null == rows || rows.isEmpty()) {
			return 0;
		}
		List<Object> docs = new ArrayList<>();
		for(Map<String, Object> row : rows) {
			Map<String, Object> doc = new LinkedHashMap<>(row);
			Object id = row.get("_id");
			if(null == id) {
				id = row.get("id");
			}
			if(null != id) {
				doc.put("_id", id);
				doc.remove("id");
			}
			docs.add(doc);
		}
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("docs", docs);
		request("POST", url + "/" + enc(collection) + "/_bulk_docs", body);
		return docs.size();
	}

	/**
	 * DELETE /{db}/{id}?rev={rev}  删除前先取当前 rev(CouchDB 的 MVCC 要求)
	 */
	public long delete(String collection, List<Object> ids) throws Exception {
		if(null == ids || ids.isEmpty()) {
			return 0;
		}
		long count = 0;
		for(Object id : ids) {
			String rev = null;
			try {
				Map<String, Object> doc = json(request("GET", url + "/" + enc(collection) + "/" + enc(String.valueOf(id)), null));
				Object value = doc.get("_rev");
				if(null != value) {
					rev = value.toString();
				}
			} catch (Exception e) {
				//文档不存在, 忽略
				continue;
			}
			String uri = url + "/" + enc(collection) + "/" + enc(String.valueOf(id));
			if(null != rev) {
				uri += "?rev=" + enc(rev);
			}
			request("DELETE", uri, null);
			count++;
		}
		return count;
	}

	/* *********************************************************************************************
	 * 											查询
	 ***********************************************************************************************/
	/**
	 * POST /{db}/_find  按 selector 查询(无 selector 时等价于遍历)
	 */
	public List<Map<String, Object>> search(String collection, List<Float> vector, int topK, String filter) throws Exception {
		List<Map<String, Object>> result = new ArrayList<>();
		if(BasicUtil.isEmpty(collection)) {
			return result;
		}
		Map<String, Object> selector = selector(filter);
		if(null == selector) {
			//没有条件时用 _all_docs 遍历
			return scroll(collection, topK <= 0 ? 10 : topK, 0, filter);
		}
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("selector", selector);
		body.put("limit", topK <= 0 ? 10 : topK);
		Map<String, Object> resp = json(request("POST", url + "/" + enc(collection) + "/_find", body));
		for(Object item : list(resp.get("docs"))) {
			result.add(doc(item));
		}
		return result;
	}

	/**
	 * GET /{db}/_all_docs?include_docs=true&limit=N&skip=M  遍历
	 */
	public List<Map<String, Object>> scroll(String collection, int limit, Object offset, String filter) throws Exception {
		List<Map<String, Object>> result = new ArrayList<>();
		if(BasicUtil.isEmpty(collection)) {
			return result;
		}
		String uri = url + "/" + enc(collection) + "/_all_docs?include_docs=true&limit=" + (limit <= 0 ? 100 : limit);
		if(null != offset) {
			uri += "&skip=" + offset;
		}
		Map<String, Object> resp = json(request("GET", uri, null));
		for(Object item : list(resp.get("rows"))) {
			Map<String, Object> row = map(item);
			Object doc = row.get("doc");
			if(null != doc) {
				result.add(doc(doc));
			} else {
				Map<String, Object> item_row = new LinkedHashMap<>();
				item_row.put("_id", row.get("id"));
				result.add(item_row);
			}
		}
		return result;
	}

	/**
	 * GET /{db}  返回 doc_count
	 */
	public long count(String collection, String filter) throws Exception {
		if(BasicUtil.isEmpty(collection)) {
			return -1;
		}
		Map<String, Object> resp = json(request("GET", url + "/" + enc(collection), null));
		Object count = resp.get("doc_count");
		if(null == count) {
			return -1;
		}
		return Long.parseLong(count.toString());
	}

	/* *********************************************************************************************
	 * 											辅助
	 ***********************************************************************************************/
	private Map<String, Object> doc(Object item) {
		Map<String, Object> row = new LinkedHashMap<>(map(item));
		Object id = row.get("_id");
		if(null != id) {
			row.put("id", id);
		}
		return row;
	}

	private String enc(String value) {
		if(null == value) {
			return "";
		}
		return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
	}
}