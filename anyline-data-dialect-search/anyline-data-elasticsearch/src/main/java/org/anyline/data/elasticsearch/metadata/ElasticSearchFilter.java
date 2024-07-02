package org.anyline.data.elasticsearch.metadata;

import java.util.LinkedHashMap;

public class ElasticSearchFilter {
    private String type;
    private String synonymsPath;   // 同义词文件path
    private LinkedHashMap<String, String> synonyms = new LinkedHashMap<>(); //同义词

    public String getType() {
        return type;
    }

    public ElasticSearchFilter setType(String type) {
        this.type = type;
        return this;
    }

    public String getSynonymsPath() {
        return synonymsPath;
    }

    public ElasticSearchFilter setSynonymsPath(String synonymsPath) {
        this.synonymsPath = synonymsPath;
        return this;
    }

    public LinkedHashMap<String, String> getSynonyms() {
        return synonyms;
    }

    public ElasticSearchFilter setSynonyms(LinkedHashMap<String, String> synonyms) {
        this.synonyms = synonyms;
        return this;
    }

    /**
     * 添加同义词
     * @param origin 原文
     * @param synonym 同义词
     * @return this
     */
    public ElasticSearchFilter addSynonym(String origin, String synonym){
        synonyms.put(origin, synonym);
        return this;
    }
}