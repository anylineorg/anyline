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


package org.anyline.data.dify.entity;

import org.anyline.entity.OriginRow;

import java.io.Serializable;
import java.util.List;

public class Segment extends OriginRow implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String id;
    protected Integer position;
    protected String document_id;
    protected String content;
    protected Integer word_count;
    protected Integer tokens;
    protected List<String> keywords;
    protected String index_node_id;
    protected String index_node_hash;
    protected Integer hit_count;
    protected Boolean enabled;
    protected Long disabled_at;
    protected String disabled_by;
    protected String status;
    protected String created_by;
    protected Long created_at;
    protected Long indexing_at;
    protected Long completed_at;
    protected String error;
    protected Long stopped_at;
    protected Document document;
    protected Double score;

    @Override
    public String getPrimaryKey(){
        return "id";
    }

    @Override
    public Object getPrimaryValue(){
        return id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        super.put("id", id);
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
        super.put("position", position);
    }

    public String getDocument_id() {
        return document_id;
    }

    public void setDocument_id(String document_id) {
        this.document_id = document_id;
        super.put("document_id", document_id);
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        super.put("content", content);
    }

    public Integer getWord_count() {
        return word_count;
    }

    public void setWord_count(Integer word_count) {
        this.word_count = word_count;
        super.put("word_count", word_count);
    }

    public Integer getTokens() {
        return tokens;
    }

    public void setTokens(Integer tokens) {
        this.tokens = tokens;
        super.put("tokens", tokens);
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
        super.put("keywords", keywords);
    }

    public String getIndex_node_id() {
        return index_node_id;
    }

    public void setIndex_node_id(String index_node_id) {
        this.index_node_id = index_node_id;
        super.put("index_node_id", index_node_id);
    }

    public String getIndex_node_hash() {
        return index_node_hash;
    }

    public void setIndex_node_hash(String index_node_hash) {
        this.index_node_hash = index_node_hash;
        super.put("index_node_hash", index_node_hash);
    }

    public Integer getHit_count() {
        return hit_count;
    }

    public void setHit_count(Integer hit_count) {
        this.hit_count = hit_count;
        super.put("hit_count", hit_count);
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
        super.put("enabled", enabled);
    }

    public Long getDisabled_at() {
        return disabled_at;
    }

    public void setDisabled_at(Long disabled_at) {
        this.disabled_at = disabled_at;
        super.put("disabled_at", disabled_at);
    }

    public String getDisabled_by() {
        return disabled_by;
    }

    public void setDisabled_by(String disabled_by) {
        this.disabled_by = disabled_by;
        super.put("disabled_by", disabled_by);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        super.put("status", status);
    }

    public String getCreated_by() {
        return created_by;
    }

    public void setCreated_by(String created_by) {
        this.created_by = created_by;
        super.put("created_by", created_by);
    }

    public Long getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Long created_at) {
        this.created_at = created_at;
        super.put("created_at", created_at);
    }

    public Long getIndexing_at() {
        return indexing_at;
    }

    public void setIndexing_at(Long indexing_at) {
        this.indexing_at = indexing_at;
        super.put("indexing_at", indexing_at);
    }

    public Long getCompleted_at() {
        return completed_at;
    }

    public void setCompleted_at(Long completed_at) {
        this.completed_at = completed_at;
        super.put("completed_at", completed_at);
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
        super.put("error", error);
    }

    public Long getStopped_at() {
        return stopped_at;
    }

    public void setStopped_at(Long stopped_at) {
        this.stopped_at = stopped_at;
        super.put("stopped_at", stopped_at);
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
        super.put("document", document);
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
        super.put("score", score);
    }
}