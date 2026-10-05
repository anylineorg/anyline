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


package org.anyline.data.chroma.client;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Chroma 客户端<br/>
 * Chroma 官方没有提供 Java SDK(官方只有 Python/JS/Rust/Kotlin/Swift), Java 生态中的 tech.amikos 客户端为第三方实现<br/>
 * 这里按官方 REST API(v2) 直接实现, 不引入第三方依赖, HTTP 使用 JDK 自带 java.net.http<br/>
 * 接口参考 https://docs.trychroma.com/reference/chroma-api
 */
public class ChromaClient {

	public static final String DEFAULT_TENANT = "default_tenant";
	public static final String DEFAULT_DATABASE = "default_database";

	private final String host;
	private final String token;
	private final String tenant;
	private final String database;
	private final HttpClient http;

	public ChromaClient(String host) {
		this(host, null, DEFAULT_TENANT, DEFAULT_DATABASE);
	}

	public ChromaClient(String host, String token, String tenant, String database) {
		this.host = format(host);
		this.token = token;
		this.tenant = BasicUtil.isEmpty(tenant) ? DEFAULT_TENANT : tenant;
		this.database = BasicUtil.isEmpty(database) ? DEFAULT_DATABASE : database;
		this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
	}

	private static String format(String host) {
		if(null == host) {
			return null;
		}
		String result = host.trim();
		while(result.endsWith("/")) {
			result = result.substring(0, result.length() - 1);
		}
		if(!result.contains("://")) {
			result = "http://" + result;
		}
		return result;
	}

	public String host() {
		return host;
	}

	public String database() {
		return database;
	}

	public String tenant() {
		return tenant;
	}

	/**
	 * 集合相关接口 /api/v2/tenants/{tenant}/databases/{database}/collections
	 */
	private String path(String suffix) {
		return host + "/api/v2/tenants/" + tenant + "/databases/" + database + suffix;
	}

	/* *********************************************************************************************
	 * 												HTTP
	 ***********************************************************************************************/
	private String request(String method, String url, Map<String, Object> body) throws Exception {
		HttpRequest.Builder builder = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.timeout(Duration.ofSeconds(30))
				.header("Accept", "application/json");
		if(null != body) {
			builder.header("Content-Type", "application/json")
					.method(method, HttpRequest.BodyPublishers.ofString(BeanUtil.object2json(body), StandardCharsets.UTF_8));
		}else{
			builder.method(method, HttpRequest.BodyPublishers.noBody());
		}
		if(null != token && !token.isEmpty()) {
			//官方认证头
			builder.header("x-chroma-token", token);
		}
		HttpResponse<String> response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		int code = response.statusCode();
		if(code >= 400) {
			throw new Exception("[Chroma][http:" + code + "][url:" + url + "][body:" + response.body() + "]");
		}
		return response.body();
	}

	private DataRow json(String text) {
		if(BasicUtil.isEmpty(text)) {
			return new DataRow();
		}
		return DataRow.parseJson(text);
	}

	private List<Object> array(String text) {
		List<Object> result = new ArrayList<>();
		if(BasicUtil.isEmpty(text)) {
			return result;
		}
		//数组形式的响应包一层后再解析
		DataRow row = DataRow.parseJson("{\"data\":" + text + "}");
		Object data = row.get("data");
		if(data instanceof List) {
			result.addAll((List<Object>) data);
		}
		return result;
	}

	/* *********************************************************************************************
	 * 												system
	 ***********************************************************************************************/
	public String version() {
		try {
			return request("GET", host + "/api/v1/version", null);
		}catch (Exception e) {
			return null;
		}
	}

	public boolean heartbeat() {
		try {
			request("GET", host + "/api/v1/heartbeat", null);
			return true;
		}catch (Exception e) {
			return false;
		}
	}

	/* *********************************************************************************************
	 * 												collection
	 ***********************************************************************************************/
	/**
	 * 查询全部集合 GET /collections
	 */
	public List<DataRow> collections() throws Exception {
		List<DataRow> result = new ArrayList<>();
		List<Object> list = array(request("GET", path("/collections"), null));
		for(Object item:list) {
			if(item instanceof DataRow) {
				result.add((DataRow) item);
			}
		}
		return result;
	}

	/**
	 * 查询集合 GET /collections/{name}
	 */
	public DataRow collection(String name) throws Exception {
		return json(request("GET", path("/collections/" + name), null));
	}

	/**
	 * 创建集合 POST /collections
	 */
	public DataRow create(String name, Map<String, Object> metadata) throws Exception {
		Map<String, Object> body = new HashMap<>();
		body.put("name", name);
		body.put("get_or_create", true);
		if(null != metadata && !metadata.isEmpty()) {
			body.put("metadata", metadata);
		}
		return json(request("POST", path("/collections"), body));
	}

	/**
	 * 删除集合 DELETE /collections/{name}
	 */
	public boolean drop(String name) throws Exception {
		request("DELETE", path("/collections/" + name), null);
		return true;
	}

	/**
	 * 集合中记录数 GET /collections/{name}/count
	 */
	public long count(String name) throws Exception {
		String text = request("GET", path("/collections/" + name + "/count"), null);
		if(BasicUtil.isEmpty(text)) {
			return 0;
		}
		try {
			return Long.parseLong(text.trim());
		}catch (Exception e) {
			return json(text).getLong("count", 0L);
		}
	}

	/* *********************************************************************************************
	 * 												record
	 ***********************************************************************************************/

	/**
	 * 按id或元数据过滤查询记录 POST /collections/{name}/get
	 * @param name 集合
	 * @param ids id
	 * @param where 元数据过滤条件 如 {key:{$eq:value}}
	 * @param limit 返回行数
	 * @param offset 起始行
	 * @return ids/documents/metadatas/embeddings
	 */
	public DataRow get(String name, List<String> ids, Map<String, Object> where, Integer limit, Integer offset) throws Exception {
		Map<String, Object> body = new HashMap<>();
		if(null != ids && !ids.isEmpty()) {
			body.put("ids", ids);
		}
		if(null != where && !where.isEmpty()) {
			body.put("where", where);
		}
		if(null != limit && limit > 0) {
			body.put("limit", limit);
		}
		if(null != offset && offset > 0) {
			body.put("offset", offset);
		}
		body.put("include", new String[]{"documents", "metadatas", "embeddings"});
		return json(request("POST", path("/collections/" + name + "/get"), body));
	}

	/**
	 * 向量检索 POST /collections/{name}/query
	 * @param name 集合
	 * @param vector 查询向量
	 * @param nResults 返回条数
	 * @param where 元数据过滤条件
	 * @return ids/documents/metadatas/distances(二维数组, 每个查询向量一组)
	 */
	public DataRow query(String name, List<Float> vector, int nResults, Map<String, Object> where) throws Exception {
		Map<String, Object> body = new HashMap<>();
		List<List<Float>> embeddings = new ArrayList<>();
		embeddings.add(vector);
		body.put("query_embeddings", embeddings);
		body.put("n_results", nResults > 0 ? nResults : 10);
		if(null != where && !where.isEmpty()) {
			body.put("where", where);
		}
		body.put("include", new String[]{"documents", "metadatas", "distances", "embeddings"});
		return json(request("POST", path("/collections/" + name + "/query"), body));
	}

	/**
	 * 写入记录 POST /collections/{name}/add
	 */
	public boolean add(String name, List<String> ids, List<List<Float>> embeddings, List<Map<String, Object>> metadatas, List<String> documents) throws Exception {
		Map<String, Object> body = new HashMap<>();
		body.put("ids", ids);
		if(null != embeddings && !embeddings.isEmpty()) {
			body.put("embeddings", embeddings);
		}
		if(null != metadatas && !metadatas.isEmpty()) {
			body.put("metadatas", metadatas);
		}
		if(null != documents && !documents.isEmpty()) {
			body.put("documents", documents);
		}
		request("POST", path("/collections/" + name + "/add"), body);
		return true;
	}

	/**
	 * 写入或更新记录 POST /collections/{name}/upsert
	 */
	public boolean upsert(String name, List<String> ids, List<List<Float>> embeddings, List<Map<String, Object>> metadatas, List<String> documents) throws Exception {
		Map<String, Object> body = new HashMap<>();
		body.put("ids", ids);
		if(null != embeddings && !embeddings.isEmpty()) {
			body.put("embeddings", embeddings);
		}
		if(null != metadatas && !metadatas.isEmpty()) {
			body.put("metadatas", metadatas);
		}
		if(null != documents && !documents.isEmpty()) {
			body.put("documents", documents);
		}
		request("POST", path("/collections/" + name + "/upsert"), body);
		return true;
	}

	/**
	 * 更新记录 POST /collections/{name}/update
	 */
	public boolean update(String name, List<String> ids, List<List<Float>> embeddings, List<Map<String, Object>> metadatas, List<String> documents) throws Exception {
		Map<String, Object> body = new HashMap<>();
		body.put("ids", ids);
		if(null != embeddings && !embeddings.isEmpty()) {
			body.put("embeddings", embeddings);
		}
		if(null != metadatas && !metadatas.isEmpty()) {
			body.put("metadatas", metadatas);
		}
		if(null != documents && !documents.isEmpty()) {
			body.put("documents", documents);
		}
		request("POST", path("/collections/" + name + "/update"), body);
		return true;
	}

	/**
	 * 删除记录 POST /collections/{name}/delete
	 */
	public int delete(String name, List<String> ids, Map<String, Object> where) throws Exception {
		Map<String, Object> body = new HashMap<>();
		if(null != ids && !ids.isEmpty()) {
			body.put("ids", ids);
		}
		if(null != where && !where.isEmpty()) {
			body.put("where", where);
		}
		request("POST", path("/collections/" + name + "/delete"), body);
		return null != ids ? ids.size() : 0;
	}
}