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


package org.anyline.data.tugraph.client;

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
 * TuGraph 客户端<br/>
 * 官方文档: https://tugraph.tech/docs/  /  https://tugraph-db.readthedocs.io<br/>
 * TuGraph 官方 Java SDK(RPC)离线不可用, 这里按官方 REST API 实现(零新增依赖):<br/>
 * <ul>
 *     <li>POST /login          登录取 jwt({"user":"admin","password":"***"} → {"jwt":"..."})</li>
 *     <li>POST /cypher         执行 openCypher({"graph":"default","script":"MATCH (n) RETURN n","format":"row"})</li>
 *     <li>GET  /graphs         子图列表</li>
 * </ul>
 * 默认 REST 端口 7070(Bolt 端口为 7687, 走 bolt 需要 neo4j-java-driver, 本实现不用)
 */
public class TuGraphClient {

	public static final int DEFAULT_PORT = 7070;
	/** 默认子图名 */
	public static final String DEFAULT_GRAPH = "default";

	private final String url;
	private final String user;
	private final String password;
	private final HttpClient http;
	private String token;

	public TuGraphClient(String url, String password) {
		this(url, password, null);
	}

	/**
	 * @param url http://host:7070
	 * @param password 密码(也可直接传已获取的 jwt)
	 * @param config 附加配置: user(用户名, 默认 admin)
	 */
	public TuGraphClient(String url, String password, Map<String, Object> config) {
		this.url = format(url);
		this.password = password;
		this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
		String user = null;
		if(null != config && null != config.get("user")) {
			user = config.get("user").toString();
		}
		this.user = BasicUtil.isEmpty(user) ? "admin" : user;
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
	 * 											认证/HTTP
	 ***********************************************************************************************/
	/**
	 * POST /login → jwt
	 */
	public String login() throws Exception {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("user", user);
		body.put("password", password);
		String text = request("POST", url + "/login", body, false);
		Map<String, Object> resp = json(text);
		Object jwt = resp.get("jwt");
		if(null == jwt) {
			jwt = resp.get("token");
		}
		if(null != jwt) {
			token = jwt.toString();
		}
		return token;
	}

	private String request(String method, String uri, Map<String, Object> body, boolean auth) throws Exception {
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
		if(auth) {
			if(BasicUtil.isEmpty(token)) {
				try {
					login();
				} catch (Exception e) {
					//未开启认证时忽略
				}
			}
			if(BasicUtil.isNotEmpty(token)) {
				builder.header("Authorization", "Bearer " + token);
			}
		}
		HttpResponse<String> response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		int code = response.statusCode();
		String text = response.body();
		if(code >= 400) {
			throw new Exception("[TuGraph][http:" + code + "][url:" + uri + "][body:" + text + "]");
		}
		return text;
	}

	private Map<String, Object> json(String text) {
		if(BasicUtil.isEmpty(text)) {
			return new LinkedHashMap<>();
		}
		return DataRow.parseJson(text);
	}

	private List<Object> list(Object value) {
		if(value instanceof List) {
			return (List<Object>) value;
		}
		return new ArrayList<>();
	}

	/* *********************************************************************************************
	 * 											接口
	 ***********************************************************************************************/
	public boolean ping() {
		try {
			request("GET", url + "/info", null, true);
			return true;
		} catch (Exception e) {
			try {
				//部分版本没有 /info, 用子图列表探测
				request("GET", url + "/graphs", null, true);
				return true;
			} catch (Exception e1) {
				return false;
			}
		}
	}

	/**
	 * GET /graphs 子图列表(对应 anyline 的"表")
	 */
	public List<Map<String, Object>> collections() throws Exception {
		List<Map<String, Object>> result = new ArrayList<>();
		Map<String, Object> resp = json(request("GET", url + "/graphs", null, true));
		for(Object item : list(resp.get("data"))) {
			Map<String, Object> row = new LinkedHashMap<>();
			if(item instanceof Map) {
				row.putAll((Map<String, Object>) item);
			} else {
				row.put("name", item);
			}
			if(null == row.get("name")) {
				row.put("name", row.get("graph"));
			}
			result.add(row);
		}
		return result;
	}

	/**
	 * POST /cypher 查询: 返回行(Map)
	 * @param graph 子图名, 为空时用 default
	 * @param script openCypher
	 */
	public List<Map<String, Object>> cypher(String graph, String script) throws Exception {
		List<Map<String, Object>> result = new ArrayList<>();
		if(BasicUtil.isEmpty(script)) {
			return result;
		}
		String text = request("POST", url + "/cypher", body(graph, script), true);
		Map<String, Object> resp = json(text);
		Object rows = resp.get("result");
		if(null == rows) {
			rows = resp.get("data");
		}
		if(null == rows) {
			rows = resp.get("rows");
		}
		for(Object item : list(rows)) {
			if(item instanceof Map) {
				result.add((Map<String, Object>) item);
			} else {
				Map<String, Object> row = new LinkedHashMap<>();
				row.put("value", item);
				result.add(row);
			}
		}
		return result;
	}

	/**
	 * POST /cypher 执行写操作(CREATE/MERGE/DELETE/DDL)
	 * @return 影响行数(TuGraph 不返回时返回 0)
	 */
	public long execute(String graph, String script) throws Exception {
		if(BasicUtil.isEmpty(script)) {
			return 0;
		}
		Map<String, Object> resp = json(request("POST", url + "/cypher", body(graph, script), true));
		Object size = resp.get("size");
		if(null != size) {
			return Long.parseLong(size.toString());
		}
		return 0;
	}

	private Map<String, Object> body(String graph, String script) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("graph", BasicUtil.isEmpty(graph) ? DEFAULT_GRAPH : graph);
		body.put("script", script);
		body.put("format", "row");
		return body;
	}
}