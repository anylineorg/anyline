package org.anyline.thingsboard.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.thingsboard.rest.client.RestClient;
import org.thingsboard.rest.client.utils.RestJsonConverter;
import org.thingsboard.server.common.data.id.EntityId;
import org.thingsboard.server.common.data.kv.Aggregation;
import org.thingsboard.server.common.data.kv.TsKvEntry;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.common.data.page.TimePageLink;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class ThingsBoardClient extends RestClient {
    private static final String JWT_TOKEN_HEADER_PARAM = "X-Authorization";
    private String token;
    private String refreshToken;
    private ObjectMapper objectMapper;
    private ExecutorService service;

    public ThingsBoardClient(String host) {
        super(new RestTemplate(), host);
    }


    public DataSet getTimeseries(String type, String entity, String  keys) {
        return getTimeseries(type, entity, keys, 0);
    }

    public DataSet getTimeseries(String type, String entity, String  keys, int page) {
        return getTimeseries(type, entity, keys, null, Aggregation.NONE, new TimePageLink(100, page), false );
    }

    public DataSet getTimeseries(String type, String entity, String  keys, Long interval, Aggregation agg, TimePageLink pageLink, boolean useStrictDataTypes) {
        Map<String, String> params = new HashMap();
        params.put("entityType",type);
        params.put("limit", pageLink.getPageSize()+"");
        params.put("ascOrder","false");
        params.put("entityId", entity);
        params.put("keys", keys);
        params.put("interval", interval == null ? "0" : interval.toString());
        params.put("agg", agg == null ? "NONE" : agg.name());
        params.put("useStrictDataTypes", Boolean.toString(useStrictDataTypes));
        this.addPageLinkToParam(params, pageLink);
        Map<String, List<JsonNode>> timeseries = (Map)this.restTemplate.exchange(
                this.baseURL + "/api/plugins/telemetry/{entityType}/{entityId}/values/timeseries?keys={keys}&interval={interval}&agg={agg}&useStrictDataTypes={useStrictDataTypes}&" + this.getUrlParamsTs(pageLink)
                , HttpMethod.GET
                , HttpEntity.EMPTY
                , new ParameterizedTypeReference<Map<String, List<JsonNode>>>() {
                }, params).getBody();
        List<TsKvEntry> list = RestJsonConverter.toTimeseries(timeseries);
        DataSet set = new DataSet();
        for(TsKvEntry entry:list){
            DataRow row = new DataRow();
            row.put("ts", entry.getTs());
            row.put("key", entry.getKey());
            row.put("value", entry.getValue());
            set.add(row);
        }
        return set;
    }



    @Override
    public List<TsKvEntry> getTimeseries(EntityId entityId, List<String> keys, Long interval, Aggregation agg, TimePageLink pageLink, boolean useStrictDataTypes) {
        Map<String, String> params = new HashMap();
        params.put("entityType", entityId.getEntityType().name());
        params.put("limit", "100");
        params.put("ascOrder","false");
        params.put("entityId", entityId.getId().toString());
        params.put("keys", BeanUtil.concat(keys));
        params.put("interval", interval == null ? "0" : interval.toString());
        params.put("agg", agg == null ? "NONE" : agg.name());
        params.put("useStrictDataTypes", Boolean.toString(useStrictDataTypes));
        this.addPageLinkToParam(params, pageLink);
        Map<String, List<JsonNode>> timeseries = (Map)this.restTemplate.exchange(this.baseURL + "/api/plugins/telemetry/{entityType}/{entityId}/values/timeseries?keys={keys}&interval={interval}&agg={agg}&useStrictDataTypes={useStrictDataTypes}&" + this.getUrlParamsTs(pageLink), HttpMethod.GET, HttpEntity.EMPTY, new ParameterizedTypeReference<Map<String, List<JsonNode>>>() {
        }, params).getBody();

        List<TsKvEntry> list = RestJsonConverter.toTimeseries(timeseries);
        return list;
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

}
