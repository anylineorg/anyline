package org.anyline.data.dify.entity;

import org.anyline.entity.OriginRow;
import org.anyline.metadata.Embedding;

import java.io.File;
import java.io.Serializable;

public class Document extends OriginRow implements Serializable {
    private static final long serialVersionUID = 1L;
    enum INDEXING_TECHNIQUE{high_quality, economy }
    enum FORMAT{text_model, hierarchical_model, qa_model}

    private String id;
    private String name;
    private String text;
    private File file;
    private FORMAT format;
    private String language;
    private Embedding embedding;
    private INDEXING_TECHNIQUE technique;
    private ProcessRule rule;
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public FORMAT getFormat() {
        return format;
    }

    public void setFormat(FORMAT format) {
        this.format = format;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Embedding getEmbedding() {
        return embedding;
    }

    public void setEmbedding(Embedding embedding) {
        this.embedding = embedding;
    }

    public INDEXING_TECHNIQUE getTechnique() {
        return technique;
    }

    public void setTechnique(INDEXING_TECHNIQUE technique) {
        this.technique = technique;
    }

    public ProcessRule getRule() {
        return rule;
    }

    public void setRule(ProcessRule rule) {
        this.rule = rule;
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
}
