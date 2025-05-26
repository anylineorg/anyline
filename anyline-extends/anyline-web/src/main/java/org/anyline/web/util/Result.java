/*
 * Copyright 2006-2025 www.anyline.org
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

package org.anyline.web.util;

import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.EntitySet;
import org.anyline.entity.PageNavi;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

public class Result {
    private boolean success;
    private Object code;
    private Object data;
    private String message;
    private int rows = -1;
    private int vol = -1;
    private int page = -1;
    private int pages = -1;
    private Long request_time = null;
    private Long response_time = null;
    private String response_id = null;
    private String sign = null;
    private Long finish_time = null;

    private PageNavi navi;
    private HttpServletRequest request;
    private HttpServletResponse response;

    public static Result success(Object data) {
        Result result = new Result();
        result.data = data;
        result.success = true;
        result.init();
        return result;
    }
    public static Result init(boolean success, Object code, Object data, String message) {
        Result result = new Result();
        result.success = success;
        result.data = data;
        result.code = code;
        result.message = message;
        result.init();
        return result;
    }
    public static Result fail(String message) {
        Result result = new Result();
        result.message = message;
        result.success = false;
        result.init();
        return result;
    }
    public Result fail(String code, String message) {
        Result result = new Result();
        result.message = message;
        result.code = code;
        result.success = false;
        result.init();
        return result;
    }
    public Result fail(Integer code, String message) {
        Result result = new Result();
        result.message = message;
        result.code = code;
        result.success = false;
        result.init();
        return result;
    }

    public String getResponse_id() {
        return response_id;
    }

    public void setResponse_id(String response_id) {
        this.response_id = response_id;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String toString() {
        return json();
    }
    private void init() {
       request =  ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
       response =  ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        if(null != request) {
            Object request_time_ = request.getParameter(ConfigTable.getString("HTTP_REQUEST_TIME_KET","_anyline_request_time"));
            if(null != request_time_) {
                request_time = (Long)request_time_;
            }
            Object response_time_ = request.getAttribute(ConfigTable.getString("HTTP_RESPONSE_TIME_KEY","_anyline_response_time"));
            if(null != response_time_) {
                response_time = (Long)response_time_;
            }
        }
        finish_time = System.currentTimeMillis();
    }
    public String json() {
        Map<String, Object> map = new HashMap<>();
        String dataType = null; // 数据类型
        String response_key_message = ConfigTable.getString("RESPONSE_KEY_MESSAGE","message");
        String response_key_data = ConfigTable.getString("RESPONSE_KEY_DATA","data");
        String response_key_code = ConfigTable.getString("RESPONSE_KEY_CODE","code");


        String response_key_navi = ConfigTable.getString("RESPONSE_KEY_NAVI","navi");
        String response_key_navi_cur_page = ConfigTable.getString("RESPONSE_KEY_NAVI_PAGE","page");
        String response_key_navi_total_page = ConfigTable.getString("RESPONSE_KEY_NAVI_PAGES","pages");
        String response_key_navi_total_row = ConfigTable.getString("RESPONSE_KEY_NAVI_ROWS","rows");
        String response_key_navi_page_rows = ConfigTable.getString("RESPONSE_KEY_NAVI_PAGE_ROWS","vol");

        map.put("type", dataType);
        map.put("result", success);
        map.put(response_key_message, message);
        map.put(response_key_data, data);
        map.put("success", success);
        map.put(response_key_code, code);
        map.put("request_time", request_time);
        map.put("response_time", response_time);
        map.put("finish_time", finish_time);
        map.put("response_id", response_id);
        map.put("sign", sign);

        if (data instanceof DataSet || data instanceof EntitySet) {
            dataType = "list";
            if(data instanceof DataSet) {
                DataSet set = (DataSet) data;
                data = set.getRows();
                if(null == navi) {
                    navi = set.getNavi();
                }
            }else if(data instanceof EntitySet) {
                EntitySet set = (EntitySet) data;
                data = set.getDatas();
                if(null == navi) {
                    navi = set.getNavi();
                }
            }
            boolean simpleStruct = ConfigTable.getBoolean("SIMPLE_RESPONSE_STRUCT", false);
            if (null == data) {
                // 没有返回数据
            }else if (null != navi) {
                // 有分页对象
                Map<String, Object> navi_ = new HashMap<>();
                navi_.put(response_key_navi_cur_page, navi.getCurPage());          // 当前页
                if(simpleStruct && !map.containsKey(response_key_navi_cur_page)) {
                    map.put(response_key_navi_cur_page, navi.getCurPage());
                }
                navi_.put(response_key_navi_total_page, navi.getTotalPage());      // 总页数
                if(simpleStruct && !map.containsKey(response_key_navi_total_page)) {
                    map.put(response_key_navi_total_page, navi.getTotalPage());
                }
                navi_.put(response_key_navi_total_row, navi.getTotalRow());        // 总行数
                if(simpleStruct && !map.containsKey(response_key_navi_total_row)) {
                    map.put(response_key_navi_total_row, navi.getTotalRow());
                }
                navi_.put(response_key_navi_page_rows, navi.getPageRows());        // 每页行籹
                if(simpleStruct && !map.containsKey(response_key_navi_page_rows)) {
                    map.put(response_key_navi_page_rows, navi.getPageRows());
                }
                map.put(response_key_navi, navi_);
            }else if(rows != -1) {
                // 设置了总行数、每页行数
                Map<String, Object> navi_ = new HashMap<>();
                navi_.put(response_key_navi_cur_page, page);         // 当前页
                if(simpleStruct && !map.containsKey(response_key_navi_cur_page)) {
                    map.put(response_key_navi_cur_page, page);
                }
                navi_.put(response_key_navi_total_page, pages);      // 总页数
                if(simpleStruct && !map.containsKey(response_key_navi_total_page)) {
                    map.put(response_key_navi_total_page, pages);
                }
                navi_.put(response_key_navi_total_row, rows);        // 总行数
                if(simpleStruct && !map.containsKey(response_key_navi_total_row)) {
                    map.put(response_key_navi_total_row, rows);
                }
                navi_.put(response_key_navi_page_rows, vol);         // 每页行籹
                if(simpleStruct && !map.containsKey(response_key_navi_page_rows)) {
                    map.put(response_key_navi_page_rows, vol);
                }
                map.put(response_key_navi, navi_);
            }

        } else if (data instanceof Iterable) {
            dataType = "list";
        } else if (data instanceof DataRow) {
            dataType = "map";
        } else if (data instanceof Map) {
            dataType = "map";
        } else if (data instanceof String) {
            dataType = "string";
            // data = BasicUtil.convertJSONChar(data.toString());
            data = data.toString();
        } else if (data instanceof Number) {
            dataType = "number";
            data = data.toString();
        } else {
            dataType = "map";
        }


        if (null != response) {
            response.setContentType("application/json;charset=utf-8");
            response.setHeader("Content-type", "application/json;charset=utf-8");
            response.setCharacterEncoding("UTF-8");
        }
        return BeanUtil.map2json(map);
    }

    public PageNavi getNavi() {
        return navi;
    }

    public void setNavi(PageNavi navi) {
        this.navi = navi;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Object getCode() {
        return code;
    }

    public void setCode(Object code) {
        this.code = code;
    }
    public void setCode(Integer code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getVol() {
        return vol;
    }

    public void setVol(int vol) {
        this.vol = vol;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }

    public Long getRequest_time() {
        return request_time;
    }

    public void setRequest_time(Long request_time) {
        this.request_time = request_time;
    }

    public Long getResponse_time() {
        return response_time;
    }

    public void setResponse_time(Long response_time) {
        this.response_time = response_time;
    }

    public Long getFinish_time() {
        return finish_time;
    }

    public void setFinish_time(Long finish_time) {
        this.finish_time = finish_time;
    }
}
