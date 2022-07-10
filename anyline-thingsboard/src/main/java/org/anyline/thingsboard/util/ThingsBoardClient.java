package org.anyline.thingsboard.util;

import com.fasterxml.jackson.databind.JsonNode;
import ognl.Ognl;
import ognl.OgnlContext;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.thingsboard.rest.client.RestClient;
import org.thingsboard.rest.client.utils.RestJsonConverter;
import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.DeviceInfo;
import org.thingsboard.server.common.data.EntityType;
import org.thingsboard.server.common.data.id.EntityId;
import org.thingsboard.server.common.data.kv.Aggregation;
import org.thingsboard.server.common.data.kv.TsKvEntry;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.common.data.page.SortOrder;
import org.thingsboard.server.common.data.page.TimePageLink;

import java.util.*;

public class ThingsBoardClient extends RestClient {

    private static final Logger log = LoggerFactory.getLogger(ThingsBoardClient.class);
    private ThingsBoardConfig config = null;
    private static Hashtable<String,ThingsBoardClient> instances = new Hashtable<String,ThingsBoardClient>();




    public static ThingsBoardClient getInstance(){
        return getInstance("default");
    }
    public static ThingsBoardClient getInstance(String key){
        if(BasicUtil.isEmpty(key)){
            key = "default";
        }
        ThingsBoardClient client = instances.get(key);
        if(null == client){
            ThingsBoardConfig config = ThingsBoardConfig.getInstance(key);
            client = new ThingsBoardClient(config.HOST);
            client.config = config;
            client.login(config.ACCOUNT, config.PASSWORD);
            instances.put(key, client);
        }
        return client;
    }

    private ThingsBoardClient(String host) {
        super(new RestTemplate(), host);
    }


    /**
     * 保存遥测数据 一交提交多条时调用，一次提交一条时调用
     * @param type 实体类型 如 设备:DEVICE
     * @param id 实体ID
     * @param scope 范围 ANY
     * @param ttl 生存时间 {"temperature":26, "humidity":87}  如果不需要可以设置成0或null 一般是调用其他不带ttl的重载
     * @param maps json结构的数据
     *           maps = [
     *             {"ts":1634712287000,"values":{"temperature":26, "humidity":87}}
     *             ,{"ts":1634712287000,"values":{"temperature":26, "humidity":87}}
     *             ,{"ts":1634712287000,"values":{"temperature":26, "humidity":87}}
     *           ]
     * @return
     */
    public boolean saveEntityTelemetry(EntityType type, String id, String scope, Long ttl, List<Map<?,?>> maps) {
        String url = baseURL + "/api/plugins/telemetry/{type}/{id}/timeseries/{scope}";
        if(null != ttl && ttl > 0){
            url += "/{ttl}";
        }
        ResponseEntity response = restTemplate.postForEntity(url, maps, Object.class, type.name(), id, scope, ttl);
        return response.getStatusCode().is2xxSuccessful();
    }

    public boolean saveEntityTelemetry(EntityType type, String id, String scope, List<Map<?,?>> maps) {
        return saveEntityTelemetry(type, id, scope, 0L, maps);
    }
    public boolean saveEntityTelemetry(EntityType type, String id, List<Map<?,?>> maps) {
        return saveEntityTelemetry(type, id, "ANY", maps);
    }
    /**
     * 保存遥测数据 一次提交一组数据
     * @param type 实体类型 如 设备:DEVICE
     * @param id 实体ID
     * @param scope 范围 ANY
     * @param ts 数据创建时间 1634712287000
     * @param ttl 生存时间   如果不需要可以设置成0或null 一般是调用其他不带ttl的重载
     * @param values json结构的数据 {"temperature":26, "humidity":87}
     *         ts与values合成最终参数  {"ts":1634712287000,"values":{"temperature":26, "humidity":87}}
     * @return
     */
    public boolean saveEntityTelemetry(EntityType type, String id, String scope, Long ttl, Long ts, Map<?,?> values) {
        Map<String,Object> map = new HashMap<>();
        map.put("ts", ts);
        map.put("values", values);
        List<Map<?,?>> maps = new ArrayList<>();
        maps.add(map);
        return saveEntityTelemetry(type, id, scope, ttl, maps);
    }

    public boolean saveEntityTelemetry(EntityType type, String id, Long ttl, Long ts, Map<?,?> values) {
        return saveEntityTelemetry(type, id, "ANY", ttl, ts, values);
    }
    public boolean saveEntityTelemetry(EntityType type, String id, Long ts, Map<?,?> values) {
        return saveEntityTelemetry(type, id, "ANY", 0L, ts, values);
    }


    public boolean saveDeviceTelemetry(String id, String scope, Long ttl, List<Map<?,?>> maps) {
        return saveEntityTelemetry(EntityType.DEVICE, id, scope, ttl, maps);
    }

    public boolean saveDeviceTelemetry(String id, Long ttl, List<Map<?,?>> maps) {
        return saveDeviceTelemetry(id, "ANY", ttl, maps);
    }
    public boolean saveDeviceTelemetry(String id, List<Map<?,?>> maps) {
        return saveDeviceTelemetry(id, "ANY", 0L, maps);
    }

    public boolean saveDeviceTelemetry(String id, String scope, Long ttl, Long ts, Map<?,?> values) {
        return saveEntityTelemetry(EntityType.DEVICE, id, scope, ttl, ts, values);
    }

    public boolean saveDeviceTelemetry(String id, Long ttl, Long ts, Map<?,?> values) {
        return saveDeviceTelemetry(id, "ANY", ttl, ts, values);
    }
    public boolean saveDeviceTelemetry(String id, Long ts, Map<?,?> values) {
        return saveDeviceTelemetry(id, "ANY", 0L, ts, values);
    }

    /**
     * 最近的遥测数据
     * @param type 类型
     * @param id ID
     * @param keys 查询的属性
     * @param useStrictDataTypes 格式转换
     * @return DataSet
     */

    public DataSet getLatestTimeseries(EntityType type, String id,  String  keys, boolean useStrictDataTypes) {
        Map<String, List<JsonNode>> maps = this.restTemplate.exchange(
                        this.baseURL + "/api/plugins/telemetry/{entityType}/{entityId}/values/timeseries?keys={keys}&useStrictDataTypes={useStrictDataTypes}"
                        , HttpMethod.GET, HttpEntity.EMPTY,
                        new ParameterizedTypeReference<Map<String, List<JsonNode>>>() {
                        }, type.name(), id, keys, useStrictDataTypes )
                .getBody();
        return map2set(maps);
    }

    public DataSet getLatestTimeseries(EntityType type, String id,  String  keys) {
        return getLatestTimeseries(type, id, keys, true);
    }

    public DataSet getLatestDeviceTimeseries(String id,  String  keys, boolean useStrictDataTypes) {
        return getLatestTimeseries(EntityType.DEVICE, id, keys, useStrictDataTypes);
    }
    public DataSet getLatestDeviceTimeseries(String id,  String  keys) {
        return getLatestTimeseries(EntityType.DEVICE, id, keys, true);
    }



    /**
     * 时间段内遥测数据
     * @param type 类型 如DEVIDE
     * @param entity id
     * @param keys 查询属性
     * @param interval 聚合统计时 数据之间的间隔(ms) 如 第interval毫秒分一组,每组算出平均值
     * @param agg 聚合函数 min sum
     * @param order 排序方式 DESC
     * @param start 开始时间
     * @param end 结束时间
     * @param limit 第页行数
     * @param strict string是否转换成原始格式
     * @return
     */

    public DataSet getTimeseries(EntityType type, String entity, String keys, Long interval, Aggregation agg, SortOrder.Direction order, Long start, Long end, Integer limit, boolean strict) {
        Map<String, String> params = new HashMap<>();
        params.put("type", type.name());
        params.put("entity", entity);
        params.put("keys", keys);
        params.put("interval", interval == null ? "0" : interval.toString());
        params.put("agg", agg == null ? "NONE" : agg.name());
        params.put("limit", limit != null ? limit.toString() : "100");
        params.put("order", order != null ? order.name() : "DESC");
        params.put("strict", Boolean.toString(strict));

        StringBuilder urlBuilder = new StringBuilder(baseURL);
        urlBuilder.append("/api/plugins/telemetry/{type}/{entity}/values/timeseries?keys={keys}&interval={interval}&limit={limit}&agg={agg}&useStrictDataTypes={strict}&orderBy={order}");

        if (start != null) {
            urlBuilder.append("&startTs={start}");
            params.put("start", String.valueOf(start));
        }
        if (end != null) {
            urlBuilder.append("&endTs={end}");
            params.put("end", String.valueOf(end));
        }

        Map<String, List<JsonNode>> timeseries = restTemplate.exchange(
                urlBuilder.toString(),
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<Map<String, List<JsonNode>>>() {
                },
                params).getBody();
        System.out.println(BeanUtil.object2json(timeseries));
        return map2set(timeseries);
    }

    public DataSet getTimeseries(EntityType type, String entity, String keys,  SortOrder.Direction order, Long start, Long end, Integer limit, boolean strict) {
        return getTimeseries(type, entity, keys, null, null, order ,start, end, limit , strict);
    }

    public DataSet getTimeseries(EntityType type, String entity, String keys,  SortOrder.Direction order, Long start, Long end, Integer limit) {
        return getTimeseries(type, entity, keys, null, null, order ,start, end, limit , true);
    }
    public DataSet getTimeseries(EntityType type, String entity, String keys,  Long start, Long end, Integer limit) {
        return getTimeseries(type, entity, keys, null, null, SortOrder.Direction.DESC ,start, end, limit , true);
    }

    public DataSet getTimeseries(String type, String entity, String keys, Long interval, Aggregation agg, SortOrder.Direction order, Long start, Long end, Integer limit, boolean strict) {
        return getTimeseries(EntityType.valueOf(type), entity, keys, interval, agg, order, start, end, limit, strict);
    }

    public DataSet getTimeseries(String type, String entity, String keys,  SortOrder.Direction order, Long start, Long end, Integer limit, boolean strict) {
        return getTimeseries(EntityType.valueOf(type), entity, keys, null, null, order ,start, end, limit , strict);
    }

    public DataSet getTimeseries(String type, String entity, String keys,  SortOrder.Direction order, Long start, Long end, Integer limit) {
        return getTimeseries(EntityType.valueOf(type), entity, keys, null, null, order ,start, end, limit , true);
    }
    public DataSet getTimeseries(String type, String entity, String keys,  Long start, Long end, Integer limit) {
        return getTimeseries(EntityType.valueOf(type), entity, keys, null, null, SortOrder.Direction.DESC ,start, end, limit , true);
    }


    public DataSet getDeviceTimeseries(String id, String keys, Long interval, Aggregation agg, SortOrder.Direction order, Long start, Long end, Integer limit, boolean strict) {
        return getTimeseries(EntityType.DEVICE, id, keys, interval, agg, order, start, end, limit, strict) ;
    }

    public DataSet getDeviceTimeseries(String id, String keys,  SortOrder.Direction order, Long start, Long end, Integer limit, boolean strict) {
        return getTimeseries(EntityType.DEVICE, id, keys, null, null, order ,start, end, limit , strict);
    }

    public DataSet getDeviceTimeseries(String id, String keys,  SortOrder.Direction order, Long start, Long end, Integer limit) {
        return getTimeseries(EntityType.DEVICE, id, keys, null, null, order ,start, end, limit , true);
    }
    public DataSet getDeviceTimeseries(String id, String keys,  Long start, Long end, Integer limit) {
        return getTimeseries(EntityType.DEVICE, id, keys, null, null, SortOrder.Direction.DESC ,start, end, limit , true);
    }



    private String getTimeUrlParams(TimePageLink pageLink) {
        return this.getUrlParams(pageLink);
    }

    private String getUrlParams(TimePageLink pageLink) {
        return this.getUrlParams(pageLink, "startTime", "endTime");
    }

    private String getUrlParamsTs(TimePageLink pageLink) {
        return this.getUrlParams(pageLink, "startTs", "endTs");
    }

    private String getUrlParams(TimePageLink pageLink, String startTime, String endTime) {
        String urlParams = "limit={limit}&ascOrder={ascOrder}";
        if (pageLink.getStartTime() != null) {
            urlParams = urlParams + "&" + startTime + "={startTime}";
        }

        if (pageLink.getEndTime() != null) {
            urlParams = urlParams + "&" + endTime + "={endTime}";
        }

        return urlParams;
    }

    private String getUrlParams(PageLink pageLink) {
        String urlParams = "pageSize={pageSize}&page={page}";
        if (!StringUtils.isEmpty(pageLink.getTextSearch())) {
            urlParams = urlParams + "&textSearch={textSearch}";
        }

        if (pageLink.getSortOrder() != null) {
            urlParams = urlParams + "&sortProperty={sortProperty}&sortOrder={sortOrder}";
        }

        return urlParams;
    }

    private void addTimePageLinkToParam(Map<String, String> params, TimePageLink pageLink) {
        this.addPageLinkToParam(params, pageLink);
        if (pageLink.getStartTime() != null) {
            params.put("startTime", String.valueOf(pageLink.getStartTime()));
        }

        if (pageLink.getEndTime() != null) {
            params.put("endTime", String.valueOf(pageLink.getEndTime()));
        }

    }

    private void addPageLinkToParam(Map<String, String> params, PageLink pageLink) {
        params.put("pageSize", String.valueOf(pageLink.getPageSize()));
        params.put("page", String.valueOf(pageLink.getPage()));
        if (!StringUtils.isEmpty(pageLink.getTextSearch())) {
            params.put("textSearch", pageLink.getTextSearch());
        }

        if (pageLink.getSortOrder() != null) {
            params.put("sortProperty", pageLink.getSortOrder().getProperty());
            params.put("sortOrder", pageLink.getSortOrder().getDirection().name());
        }

    }

    private DataSet map2set(Map<String, List<JsonNode>> timeseries){
        DataSet set = new DataSet();
        try {

        /*
        [{"TS":1657325325061,"KEY":"lng","VALUE":"1120.373291"},{"TS":1657325325061,"KEY":"lat","VALUE":"136.089957"}]
        没有数据时 异常
        json:{"lng":[{"ts":1657216877526,"value":null}],"lat":[{"ts":1657216877527,"value":null}]}
        Exception in thread "main" java.lang.RuntimeException: Can't parse value: null
        */

            List<TsKvEntry> list = RestJsonConverter.toTimeseries(timeseries);
            for (TsKvEntry entry : list) {
                DataRow row = new DataRow();
                row.put("ts", entry.getTs());
                row.put("key", entry.getKey());
                row.put("value", entry.getValue());
                set.add(row);
            }
        }catch (Exception e){
            System.out.println(BeanUtil.object2json(timeseries));
            e.printStackTrace();
        }


        return set;
    }

}
