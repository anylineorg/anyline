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


package org.anyline.data.ragflow.client;

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
 * RagFlow 客户端<br/>
 * 官方文档: https://ragflow.io/docs/dev/http_api_reference<br/>
 * RagFlow 是 RAG 引擎(不是数据库), 官方只有 HTTP API, 这里按官方接口实现(零新增依赖):<br/>
 * <ul>
 *     <li>GET  /api/v1/datasets                         知识库列表(对应 anyline 的"表")</li>
 *     <li>GET  /api/v1/datasets/{dataset_id}/documents  文档列表</li>
 *     <li>POST /api/v1/retrieval                        检索({"question","dataset_ids","top_k","similarity_threshold"})</li>
 * </ul>
 * 认证: Header Authorization: Bearer {api_key}<br/>
 * 语义映射: dataset(知识库) ↔ Table, chunk(切片) ↔ 行, question ↔ 查询条件
 */
public class RagFlowClient {

	/** 默认 http 端口(RagFlow 官方默认 9380) */
	public static final int DEFAULT_PORT = 9380;

	private final String url;
	private final String apiKey;
	private final HttpClient http;

	public RagFlowClient(String url, String apiKey) {
		this(url, apiKey, null);
	}

	public RagFlowClient(String url, String apiKey, Map<String, Object> config) {
		this.url = format(url);
		this.apiKey = apiKey;
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
				.timeout(Duration.ofSeconds(60))
				.header("Accept", "application/json");
		if(null != body) {
			builder.header("Content-Type", "application/json")
					.method(method, HttpRequest.BodyPublishers.ofString(BeanUtil.object2json(body), StandardCharsets.UTF_8));
		} else {
			builder.method(method, HttpRequest.BodyPublishers.noBody());
		}
		if(BasicUtil.isNotEmpty(apiKey)) {
			builder.header("Authorization", "Bearer " + apiKey);
		}
		HttpResponse<String> response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		int code = response.statusCode();
		String text = response.body();
		if(code >= 400) {
			throw new Exception("[RagFlow][http:" + code + "][url:" + uri + "][body:" + text + "]");
		}
		return text;
	}

	/**
	 * 官方统一响应: {"code":0,"message":"","data":...}
	 */
	private Object data(String text) {
		Map<String, Object> resp = json(text);
		Object code = resp.get("code");
		if(null != code && !"0".equals(code.toString())) {
			throw new RuntimeException("[RagFlow][code:" + code + "][msg:" + resp.get("message") + "]");
		}
		return resp.get("data");
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

	private List<Object> list(Map<String, Object> map, String... keys) {
		for(String key : keys) {
			Object value = map.get(key);
			if(value instanceof List) {
				return (List<Object>) value;
			}
		}
		return new ArrayList<>();
	}

	/* *********************************************************************************************
	 * 										知识库(对应Table)
	 ***********************************************************************************************/
	public boolean ping() {
		try {
			request("GET", url + "/api/v1/datasets?page=1&page_size=1", null);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * GET /api/v1/datasets
	 */
	public List<Map<String, Object>> collections() throws Exception {
		List<Map<String, Object>> result = new ArrayList<>();
		Object data = data(request("GET", url + "/api/v1/datasets?page=1&page_size=100", null));
		if(data instanceof List) {
			for(Object item : (List<?>) data) {
				if(item instanceof Map) {
					result.add((Map<String, Object>) item);
				}
			}
		}
		return result;
	}

	/* *********************************************************************************************
	 * 											检索/文档
	 ***********************************************************************************************/
	/**
	 * POST /api/v1/retrieval 检索切片
	 * @param dataset 知识库id(可空)
	 * @param question 问题(检索语句)
	 * @param topK 返回条数
	 */
	public List<Map<String, Object>> search(String dataset, List<Float> vector, int topK, String question) throws Exception {
		if(BasicUtil.isEmpty(question)) {
			//没有检索语句时返回文档列表
			return documents(dataset, topK <= 0 ? 10 : topK, 0);
		}
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("question", question);
		List<String> ids = new ArrayList<>();
		if(BasicUtil.isNotEmpty(dataset)) {
			ids.add(dataset);
		}
		body.put("dataset_ids", ids);
		body.put("top_k", topK <= 0 ? 10 : topK);
		Object result = data(request("POST", url + "/api/v1/retrieval", body));
		List<Map<String, Object>> rows = new ArrayList<>();
		if(result instanceof Map) {
			for(Object item : list((Map<String, Object>) result, "chunks", "docs", "data")) {
				if(item instanceof Map) {
					rows.add((Map<String, Object>) item);
				} else {
					Map<String, Object> row = new LinkedHashMap<>();
					row.put("content", item);
					rows.add(row);
				}
			}
		} else if(result instanceof List) {
			for(Object item : (List<?>) result) {
				if(item instanceof Map) {
					rows.add((Map<String, Object>) item);
				}
			}
		}
		return rows;
	}

	/**
	 * GET /api/v1/datasets/{dataset_id}/documents 文档列表(遍历)
	 */
	public List<Map<String, Object>> scroll(String dataset, int limit, Object offset, String question) throws Exception {
		return documents(dataset, limit, offset);
	}

	public List<Map<String, Object>> documents(String dataset, int limit, Object offset) throws Exception {
		List<Map<String, Object>> result = new ArrayList<>();
		if(BasicUtil.isEmpty(dataset)) {
			return result;
		}
		int page = 1;
		if(null != offset) {
			page = Integer.parseInt(offset.toString()) / (limit <= 0 ? 10 : limit) + 1;
		}
		String uri = url + "/api/v1/datasets/" + enc(dataset) + "/documents?page=" + page + "&page_size=" + (limit <= 0 ? 10 : limit);
		Object data = data(request("GET", uri, null));
		if(data instanceof List) {
			for(Object item : (List<?>) data) {
				if(item instanceof Map) {
					result.add((Map<String, Object>) item);
				}
			}
		} else if(data instanceof Map) {
			for(Object item : list((Map<String, Object>) data, "docs", "documents", "data")) {
				if(item instanceof Map) {
					result.add((Map<String, Object>) item);
				}
			}
		}
		return result;
	}

	/**
	 * 切片数量(取知识库信息中的 chunk_count)
	 */
	public long count(String dataset, String question) throws Exception {
		if(BasicUtil.isEmpty(dataset)) {
			return -1;
		}
		for(Map<String, Object> item : collections()) {
			Object id = item.get("id");
			if(null != id && dataset.equals(id.toString())) {
				Object count = item.get("chunk_count");
				if(null != count) {
					return Long.parseLong(count.toString());
				}
			}
		}
		return -1;
	}

	/**
	 * 写入: RagFlow 只支持上传文件(multipart), 这里不做实现, 返回 -1
	 */
	public long upsert(String dataset, List<Map<String, Object>> rows) throws Exception {
		return -1;
	}

	/**
	 * 删除: 官方接口 DELETE /api/v1/datasets/{dataset_id}/documents({"ids":[...]}), 需要文档id(非切片)
	 */
	public long delete(String dataset, List<Object> ids) throws Exception {
		if(BasicUtil.isEmpty(dataset) || null == ids || ids.isEmpty()) {
			return 0;
		}
		Map<String, Object> body = new LinkedHashMap<>();
		List<Object> list = new ArrayList<>(ids);
		body.put("ids", list);
		request("DELETE", url + "/api/v1/datasets/" + enc(dataset) + "/documents", body);
		return ids.size();
	}

	private String enc(String value) {
		if(null == value) {
			return "";
		}
		return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
	}
}