package org.anyline.data.param;

import java.util.LinkedHashMap;
import java.util.List;

public class Highlight {
    protected LinkedHashMap<String, Highlight> fields = new LinkedHashMap<>()	        ; // 指定检索高亮显示的字段。可以使用通配符来指定字段。例如，可以指定comment_*来获取以comment_开头的所有文本和关键字字段的高亮显示。
    protected String boundary_chars			        ; // 包含每个边界字符的字符串。默认为,! ?\ \ n。
    protected Integer boundary_max_scan		        ; // 扫描边界字符的距离。默认为20。
    protected String boundary_scanner		        ; // 指定如何分割突出显示的片段，支持chars, sentence, or word三种方式。
    protected String boundary_scanner_locale	    ; // 用来设置搜索和确定单词边界的本地化设置，此参数使用语言标记的形式（“en-US”, “fr-FR”, “ja-JP”）
    protected String encoder					    ; // 表示代码段应该是HTML编码的:默认(无编码)还是HTML (HTML-转义代码段文本，然后插入高亮标记)
    protected Boolean force_source              	; // 根据源高亮显示。默认值为false。
    protected String fragmenter				        ; // 指定文本应如何在突出显示片段中拆分:支持参数simple或者span。
    protected String fragment_offset			    ; // 控制要开始突出显示的空白。仅在使用fvh highlighter时有效。
    protected Integer fragment_size			        ; // 字符中突出显示的片段的大小。默认为100。
    protected String highlight_query			    ; // 突出显示搜索查询之外的其他查询的匹配项。这在使用重打分查询时特别有用，因为默认情况下高亮显示不会考虑这些问题。
    protected List<String> matched_fields			; // 组合多个匹配结果以突出显示单个字段，对于使用不同方式分析同一字符串的多字段。所有的matched_fields必须将term_vector设置为with_positions_offsets，但是只有将匹配项组合到的字段才会被加载，因此只有将store设置为yes才能使该字段受益。只适用于fvh highlighter。
    protected Integer no_match_size			       ; // 如果没有要突出显示的匹配片段，则希望从字段开头返回的文本量。默认为0(不返回任何内容)。
    protected Integer number_of_fragments       	    ; // 返回的片段的最大数量。如果片段的数量设置为0，则不会返回任何片段。相反，突出显示并返回整个字段内容。当需要突出显示短文本(如标题或地址)，但不需要分段时，使用此配置非常方便。如果number_of_fragments为0，则忽略fragment_size。默认为5。
    protected String order					        ; // 设置为score时，按分数对突出显示的片段进行排序。默认情况下，片段将按照它们在字段中出现的顺序输出(order:none)。将此选项设置为score将首先输出最相关的片段。每个高亮应用自己的逻辑来计算相关性得分。
    protected Integer phrase_limit			       ; // 控制文档中所考虑的匹配短语的数量。防止fvh highlighter分析太多的短语和消耗太多的内存。提高限制会增加查询时间并消耗更多内存。默认为256。
    protected String pre_tags				        ; // 与post_tags一起使用，定义用于突出显示文本的HTML标记。默认情况下，突出显示的文本被包装在和标记中。指定为字符串数组。
    protected String post_tags				        ; // 与pre_tags一起使用，定义用于突出显示文本的HTML标记。默认情况下，突出显示的文本被包装在和标记中。指定为字符串数组。
    protected Boolean require_field_match     ; // 默认情况下，只突出显示包含查询匹配的字段。将require_field_match设置为false以突出显示所有字段。默认值为true。
    protected String tags_schema				    ; // 设置为使用内置标记模式的样式。
    protected String type					        ; // 使用的高亮模式:unified, plain, or fvh. 默认为 unified。
    public Highlight getHighlight(String field){
        return fields.get(field);
    }
    public LinkedHashMap<String, Highlight> getFields() {
        return fields;
    }
    public Highlight getFields(String key) {
        return fields.get(key);
    }

    public void setFields(LinkedHashMap<String, Highlight> fields) {
        this.fields = fields;
    }
    public void addField(String key, Highlight highlight) {
        if(null != highlight){
            highlight.setFields(null);
        }
        this.fields.put(key, highlight);
    }
    public void addField(String ... fields) {
        for(String filed:fields) {
            addField(filed, new Highlight());
        }
    }
    public void clear(){
        fields.clear();
    }

    public String getBoundary_chars() {
        return boundary_chars;
    }

    public void setBoundary_chars(String boundary_chars) {
        this.boundary_chars = boundary_chars;
    }

    public Integer getBoundary_max_scan() {
        return boundary_max_scan;
    }

    public void setBoundary_max_scan(Integer boundary_max_scan) {
        this.boundary_max_scan = boundary_max_scan;
    }

    public String getBoundary_scanner() {
        return boundary_scanner;
    }

    public void setBoundary_scanner(String boundary_scanner) {
        this.boundary_scanner = boundary_scanner;
    }

    public String getBoundary_scanner_locale() {
        return boundary_scanner_locale;
    }

    public void setBoundary_scanner_locale(String boundary_scanner_locale) {
        this.boundary_scanner_locale = boundary_scanner_locale;
    }

    public String getEncoder() {
        return encoder;
    }

    public void setEncoder(String encoder) {
        this.encoder = encoder;
    }

    public Boolean getForce_source() {
        return force_source;
    }

    public void setForce_source(Boolean force_source) {
        this.force_source = force_source;
    }

    public String getFragmenter() {
        return fragmenter;
    }

    public void setFragmenter(String fragmenter) {
        this.fragmenter = fragmenter;
    }

    public String getFragment_offset() {
        return fragment_offset;
    }

    public void setFragment_offset(String fragment_offset) {
        this.fragment_offset = fragment_offset;
    }

    public Integer getFragment_size() {
        return fragment_size;
    }

    public void setFragment_size(Integer fragment_size) {
        this.fragment_size = fragment_size;
    }

    public String getHighlight_query() {
        return highlight_query;
    }

    public void setHighlight_query(String highlight_query) {
        this.highlight_query = highlight_query;
    }

    public List<String> getMatched_fields() {
        return matched_fields;
    }

    public void setMatched_fields(List<String> matched_fields) {
        this.matched_fields = matched_fields;
    }

    public Integer getNo_match_size() {
        return no_match_size;
    }

    public void setNo_match_size(Integer no_match_size) {
        this.no_match_size = no_match_size;
    }

    public Integer getNumber_of_fragments() {
        return number_of_fragments;
    }

    public void setNumber_of_fragments(Integer number_of_fragments) {
        this.number_of_fragments = number_of_fragments;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public Integer getPhrase_limit() {
        return phrase_limit;
    }

    public void setPhrase_limit(Integer phrase_limit) {
        this.phrase_limit = phrase_limit;
    }

    public String getPre_tags() {
        return pre_tags;
    }

    public void setPre_tags(String pre_tags) {
        this.pre_tags = pre_tags;
    }

    public String getPost_tags() {
        return post_tags;
    }

    public void setPost_tags(String post_tags) {
        this.post_tags = post_tags;
    }

    public Boolean getRequire_field_match() {
        return require_field_match;
    }

    public void setRequire_field_match(Boolean require_field_match) {
        this.require_field_match = require_field_match;
    }

    public String getTags_schema() {
        return tags_schema;
    }

    public void setTags_schema(String tags_schema) {
        this.tags_schema = tags_schema;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
