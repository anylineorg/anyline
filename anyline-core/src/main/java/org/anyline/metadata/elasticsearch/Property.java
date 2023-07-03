package org.anyline.metadata.elasticsearch;

public class Property {
    public static enum TYPE{
        BINARY ,BOOLEAN ,BYTE, DATE ,DOUBLE, FLOAT, GEO_POINT,GEO_SHAPE, HALF_FLOAT ,
        INTEGER, KEYWORD, LONG ,NESTED, OBJECT, SCALED_FLOAT, SHORT,TEXT
    }
    /**
     * 用于控制倒排索引记录的内容，有如下四种配置：
     *
     * docs：只记录doc id
     * freqs：记录doc id 和term frequencies
     * positions：记录doc id、 term frequencies和term position
     * offsets：记录doc id、 term frequencies、term position、character offsets
     *
     */
    public static enum OPTION{
        DOCS,FREQS,POSITIONS,OFFSETS
    }

    protected TYPE type;
    protected String analyzer;
    protected String search_analyzer;
    protected Integer boost;
    protected Boolean coerce;
    protected Double distance_error_pct;
    protected Boolean doc_values;
    protected Boolean eager_global_ordinals;
    protected Boolean geohash;
    protected String geohash_precision; //1m
    protected Boolean geohash_prefix;
    protected Integer ignore_above;
    protected Boolean ignore_malformed;
    protected Boolean include_in_all;
    protected Boolean include_in_parent;
    protected Boolean include_in_root;
    /**
     * index参数作用是控制当前字段是否被索引，默认为true，false表示不记录，即不可被搜索。
     */
    protected Boolean index;
    /**
     * 用于控制倒排索引记录的内容
     */
    protected OPTION index_options;
    protected Boolean lat_lon;
    protected Boolean normalize;
    protected Boolean normalize_lat;
    protected Boolean normalize_lon;
    protected Boolean norms;
    protected String null_value;
    protected String orientation;//ccw
    protected Integer position_increment_gap;
    protected String precision;//5km
    protected Integer precision_step;
    protected Integer scaling_factor;
    protected String similarity;
    protected Boolean store;
    protected String tree; //geohash
    protected Integer tree_levels;
    protected Boolean validate;
    protected Boolean validate_lat;
    protected Boolean validate_lon;
    /**
     * 将该字段的值复制到目标字段，实现类似_all的作用。不会出现在_source中，只能用来搜索。
     */
    protected String[] copy_to;
}
