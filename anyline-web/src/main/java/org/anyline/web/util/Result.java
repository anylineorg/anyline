package org.anyline.web.util;

import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.PageNavi;
import org.anyline.util.BasicUtil;
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

    private PageNavi navi;
    private HttpServletRequest request;
    private HttpServletResponse response;

    public static Result success(Object data){
        Result result = new Result();
        result.data = data;
        result.success = true;
        result.init();
        return result;
    }
    public static Result fail(String message){
        Result result = new Result();
        result.message = message;
        result.success = false;
        result.init();
        return result;
    }
    public Result fail(String code, String message){
        Result result = new Result();
        result.message = message;
        result.code = code;
        result.success = false;
        result.init();
        return result;
    }
    public Result fail(Integer code, String message){
        Result result = new Result();
        result.message = message;
        result.code = code;
        result.success = false;
        result.init();
        return result;
    }

    public String toString(){
        return json();
    }
    private void init(){
       request =  ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
       response =  ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
    }
    public String json() {
        Map<String, Object> map = new HashMap<String, Object>();
        String dataType = null; // 数据类型
        if (data instanceof DataSet) {
            DataSet set = (DataSet) data;
            dataType = "list";
            data = set.getRows();
            if(null == navi){
                navi = set.getNavi();
            }
            if (null == data) {
                //没有返回数据
            }else if (null != navi) {
                Map<String, Object> navi_ = new HashMap<>();
                navi_.put("page", navi.getCurPage());         //当前页
                navi_.put("pages", navi.getTotalPage());      //总页数
                navi_.put("rows", navi.getTotalRow());        //总行数
                navi_.put("vol", navi.getPageRows());         //第页行籹
                map.put("navi", navi_);
            }else if(rows != -1){
                Map<String, Object> navi_ = new HashMap<String, Object>();
                navi_.put("page", page);        //当前页
                navi_.put("pages", pages);      //总页数
                navi_.put("rows", rows);        //总行数
                navi_.put("vol", vol);          //第页行籹
                map.put("navi", navi_);
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
        map.put("type", dataType);
        map.put("result", success);
        map.put("message", message);
        map.put("data", data);
        map.put("success", success);
        map.put("code", code);
        if(null != request) {
            map.put("request_time", request.getParameter(ConfigTable.getString("HTTP_REQUEST_TIME_KET","_anyline_request_time")));
            map.put("response_time", request.getAttribute(ConfigTable.getString("HTTP_RESPONSE_TIME_KEY","_anyline_response_time")));
        }
        map.put("finish_time", System.currentTimeMillis());
        if (null != response){
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
}
