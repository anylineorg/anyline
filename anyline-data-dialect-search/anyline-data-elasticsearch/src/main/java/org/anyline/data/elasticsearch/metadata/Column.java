package org.anyline.data.elasticsearch.metadata;

public class Column extends org.anyline.metadata.Column{
    protected Boolean index                       ; // 是否需要创建索引(ES里用的其他数据库应该通过new Index()创建索引)
    protected Boolean store                       ; // 是否需要存储
    protected String analyzer                     ; // 分词器
    protected String searchAnalyzer               ; // 查询分词器
    protected Integer ignoreAbove                 ; // 可创建索引的最大词长度
    private String coerce;
    private String copyTo;
    private String docValues;
    private String dynamic;
    private String eagerGlobalOrdinals;
    private String enabled;
    private String format;
    private String ignoreMalformed;
    private String indexOptions;
    private String indexPhrases;
    private String indexPrefixes;
    private String meta;
    private String fields;
    private String normalizer;
    private String norms;
    private String nullValue;
    private String positionIncrementGap;
    private String properties;
    private String similarity;
    private String subObjects;
    private String termVector;

    public Boolean getIndex() {
        return index;
    }

    public void setIndex(Boolean index) {
        this.index = index;
    }

    public Boolean getStore() {
        return store;
    }

    public void setStore(Boolean store) {
        this.store = store;
    }

    public String getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(String analyzer) {
        this.analyzer = analyzer;
    }

    public String getSearchAnalyzer() {
        return searchAnalyzer;
    }

    public void setSearchAnalyzer(String searchAnalyzer) {
        this.searchAnalyzer = searchAnalyzer;
    }

    public Integer getIgnoreAbove() {
        return ignoreAbove;
    }

    public void setIgnoreAbove(Integer ignoreAbove) {
        this.ignoreAbove = ignoreAbove;
    }

    public String getCoerce() {
        return coerce;
    }

    public void setCoerce(String coerce) {
        this.coerce = coerce;
    }

    public String getCopyTo() {
        return copyTo;
    }

    public void setCopyTo(String copyTo) {
        this.copyTo = copyTo;
    }

    public String getDocValues() {
        return docValues;
    }

    public void setDocValues(String docValues) {
        this.docValues = docValues;
    }

    public String getDynamic() {
        return dynamic;
    }

    public void setDynamic(String dynamic) {
        this.dynamic = dynamic;
    }

    public String getEagerGlobalOrdinals() {
        return eagerGlobalOrdinals;
    }

    public void setEagerGlobalOrdinals(String eagerGlobalOrdinals) {
        this.eagerGlobalOrdinals = eagerGlobalOrdinals;
    }

    public String getEnabled() {
        return enabled;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getIgnoreMalformed() {
        return ignoreMalformed;
    }

    public void setIgnoreMalformed(String ignoreMalformed) {
        this.ignoreMalformed = ignoreMalformed;
    }

    public String getIndexOptions() {
        return indexOptions;
    }

    public void setIndexOptions(String indexOptions) {
        this.indexOptions = indexOptions;
    }

    public String getIndexPhrases() {
        return indexPhrases;
    }

    public void setIndexPhrases(String indexPhrases) {
        this.indexPhrases = indexPhrases;
    }

    public String getIndexPrefixes() {
        return indexPrefixes;
    }

    public void setIndexPrefixes(String indexPrefixes) {
        this.indexPrefixes = indexPrefixes;
    }

    public String getMeta() {
        return meta;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }

    public String getFields() {
        return fields;
    }

    public void setFields(String fields) {
        this.fields = fields;
    }

    public String getNormalizer() {
        return normalizer;
    }

    public void setNormalizer(String normalizer) {
        this.normalizer = normalizer;
    }

    public String getNorms() {
        return norms;
    }

    public void setNorms(String norms) {
        this.norms = norms;
    }

    public String getNullValue() {
        return nullValue;
    }

    public void setNullValue(String nullValue) {
        this.nullValue = nullValue;
    }

    public String getPositionIncrementGap() {
        return positionIncrementGap;
    }

    public void setPositionIncrementGap(String positionIncrementGap) {
        this.positionIncrementGap = positionIncrementGap;
    }

    public String getProperties() {
        return properties;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    public String getSimilarity() {
        return similarity;
    }

    public void setSimilarity(String similarity) {
        this.similarity = similarity;
    }

    public String getSubObjects() {
        return subObjects;
    }

    public void setSubObjects(String subObjects) {
        this.subObjects = subObjects;
    }

    public String getTermVector() {
        return termVector;
    }

    public void setTermVector(String termVector) {
        this.termVector = termVector;
    }
}
