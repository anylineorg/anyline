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

package org.anyline.data.dify.entity;

import org.anyline.entity.OriginRow;
import org.anyline.metadata.Embedding;

import java.io.File;
import java.io.Serializable;

public class Document extends OriginRow implements Serializable {
    private static final long serialVersionUID = 1L;
    enum INDEXING_TECHNIQUE{high_quality, economy }
    enum FORMAT{text_model, hierarchical_model, qa_model}

    protected String id;
    protected Integer position;
    protected String name;
    protected String text;
    protected File file;
    protected FORMAT format;
    protected String language;
    protected Embedding embedding;
    protected INDEXING_TECHNIQUE technique;
    protected ProcessRule rule;
    protected String data_source_type; //数据来源类型 如upload_file
    protected String created_from; //创建方式 如api
    protected Integer token_count;
    protected Integer word_count;
    protected Integer hit_count;
    protected String indexing_status; //索引状态 如completed:完成  indexing:排队
    protected String error; //异常
    protected Boolean enabled; //是否可用

    @Override
    public String getPrimaryKey(){
        return "id";
    }

    @Override
    public Object getPrimaryValue(){
        return id;
    }
    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
        super.put("position", position);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        super.put("id", id);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        super.put("name", name);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        super.put("text", text);
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
        super.put("file", file);
    }

    public FORMAT getFormat() {
        return format;
    }

    public void setFormat(FORMAT format) {
        this.format = format;
        super.put("format", format);
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
        super.put("language", language);
    }

    public Embedding getEmbedding() {
        return embedding;
    }

    public void setEmbedding(Embedding embedding) {
        this.embedding = embedding;
        super.put("embedding", embedding);
    }

    public INDEXING_TECHNIQUE getTechnique() {
        return technique;
    }

    public void setTechnique(INDEXING_TECHNIQUE technique) {
        this.technique = technique;
        super.put("technique", technique);
    }

    public ProcessRule getRule() {
        return rule;
    }

    public void setRule(ProcessRule rule) {
        this.rule = rule;
        super.put("rule", rule);
    }


    public Document addMetadata(String id, String name, Object value){
        return addMetadata(new Metadata(id, name, value));
    }
    public Document addMetadata(Metadata metadata){
        metadatas.put(metadata.getName().toUpperCase(), metadata);
        return this;
    }
    public Metadata getMetadata(String name){
        return (Metadata) metadatas.get(name.toUpperCase());
    }

    public String getData_source_type() {
        return data_source_type;
    }

    public void setData_source_type(String data_source_type) {
        this.data_source_type = data_source_type;
        super.put("data_source_type", data_source_type);
    }

    public String getCreated_from() {
        return created_from;
    }

    public void setCreated_from(String created_from) {
        this.created_from = created_from;
        super.put("created_from", created_from);
    }

    public Integer getToken_count() {
        return token_count;
    }

    public void setToken_count(Integer token_count) {
        this.token_count = token_count;
        super.put("token_count", token_count);
    }

    public Integer getWord_count() {
        return word_count;
    }

    public void setWord_count(Integer word_count) {
        this.word_count = word_count;
        super.put("word_count", word_count);
    }

    public Integer getHit_count() {
        return hit_count;
    }

    public void setHit_count(Integer hit_count) {
        this.hit_count = hit_count;
        super.put("hit_count", hit_count);
    }

    public String getIndexing_status() {
        return indexing_status;
    }

    public void setIndexing_status(String indexing_status) {
        this.indexing_status = indexing_status;
        super.put("indexing_status", indexing_status);
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
        super.put("error", error);
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
        super.put("enabled", enabled);
    }

    public String toString(){
        return name;
    }
}
