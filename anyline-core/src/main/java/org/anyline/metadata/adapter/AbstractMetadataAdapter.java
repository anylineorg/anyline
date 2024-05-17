/*
 * Copyright 2006-2023 www.anyline.org
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



package org.anyline.metadata.adapter;

import org.anyline.util.BasicUtil;

/**
 * 读取元数据结果集依据(列名)
 */
public class AbstractMetadataAdapter<T extends AbstractMetadataAdapter> {
    /**
     * 名称<br/>
     * 注意在读取column元数据时,表名要用name而不是column,其他类似<br/>
     *
     */
    private String[] nameRefer;
    /**
     * 在非catalog自身元数据时使用,如table元数据时读取catalog时依据列
     */
    private String[] catalogRefer;
    private String[] schemaRefer;
    private String[] tableRefer;
    private String[] columnRefer;
    private String[] typeRefer; //view table
    private String[] commentRefer;
    private String[] defineRefer;
    private String[] defaultRefer;

    public String[] getNameRefers() {
        return nameRefer;
    }

    public String getNameRefer(){
        if(null != nameRefer && nameRefer.length > 0){
            return nameRefer[0];
        }
        return null;
    }
    public T setNameRefer(String[] nameRefer) {
        this.nameRefer = nameRefer;
        return (T)this;
    }
    public T setNameRefer(String name) {
        if(BasicUtil.isNotEmpty(name)) {
            this.nameRefer = name.split(",");
        }else{
            this.nameRefer = null;
        }
        return (T)this;
    }

    public String[] getCommentRefers() {
        return commentRefer;
    }

    public String getCommentRefer(){
        if(null != commentRefer && commentRefer.length > 0){
            return commentRefer[0];
        }
        return null;
    }
    public T setCommentRefer(String[] commentRefer) {
        this.commentRefer = commentRefer;
        return (T)this;
    }
    public T setCommentRefer(String comment) {
        if(BasicUtil.isNotEmpty(comment)) {
            this.commentRefer = comment.split(",");
        }else{
            this.commentRefer = null;
        }
        return (T)this;
    }

    public String[] getDefineRefers() {
        return defineRefer;
    }

    public String getDefineRefer(){
        if(null != defineRefer && defineRefer.length > 0){
            return defineRefer[0];
        }
        return null;
    }
    public T setDefineRefer(String[] defineRefer) {
        this.defineRefer = defineRefer;
        return (T)this;
    }
    public T setDefineRefer(String define) {
        if(BasicUtil.isNotEmpty(define)) {
            this.defineRefer = define.split(",");
        }else{
            this.defineRefer = null;
        }
        return (T)this;
    }
    public String[] getCatalogRefers() {
        return catalogRefer;
    }

    public String getCatalogRefer(){
        if(null != catalogRefer && catalogRefer.length > 0){
            return catalogRefer[0];
        }
        return null;
    }
    public T setCatalogRefer(String[] catalogRefer) {
        this.catalogRefer = catalogRefer;
        return (T)this;
    }

    public T setCatalogRefer(String catalog) {
        if(BasicUtil.isNotEmpty(catalog)) {
            this.catalogRefer = catalog.split(",");
        }else{
            this.catalogRefer = null;
        }
        return (T)this;
    }

    public String[] getSchemaRefers() {
        return schemaRefer;
    }

    public String getSchemaRefer(){
        if(null != schemaRefer && schemaRefer.length > 0){
            return schemaRefer[0];
        }
        return null;
    }
    public T setSchemaRefer(String[] schemaRefer) {
        this.schemaRefer = schemaRefer;
        return (T)this;
    }

    public T setSchemaRefer(String schema) {
        if(BasicUtil.isNotEmpty(schema)) {
            this.schemaRefer = schema.split(",");
        }else{
            this.schemaRefer = null;
        }
        return (T)this;
    }
    public String[] getTableRefers() {
        return tableRefer;
    }

    public String getTableRefer(){
        if(null != tableRefer && tableRefer.length > 0){
            return tableRefer[0];
        }
        return null;
    }
    public T setTableRefer(String[] tableRefer) {
        this.tableRefer = tableRefer;
        return (T)this;
    }

    public T setTableRefer(String table) {
        if(BasicUtil.isNotEmpty(table)) {
            this.tableRefer = table.split(",");
        }else{
            this.tableRefer = null;
        }
        return (T)this;
    }
    public String[] getColumnRefers() {
        return columnRefer;
    }

    public String getColumnRefer(){
        if(null != columnRefer && columnRefer.length > 0){
            return columnRefer[0];
        }
        return null;
    }
    public T setColumnRefer(String[] columnRefer) {
        this.columnRefer = columnRefer;
        return (T)this;
    }
    public T setColumnRefer(String column) {
        if(BasicUtil.isNotEmpty(column)) {
            this.columnRefer = column.split(",");
        }else{
            this.columnRefer = null;
        }
        return (T)this;
    }
    public String[] getTypeRefers() {
        return typeRefer;
    }
    public String getTypeRefer(){
        if(null != typeRefer && typeRefer.length > 0){
            return typeRefer[0];
        }
        return null;
    }

    public T setTypeRefer(String[] typeRefer) {
        this.typeRefer = typeRefer;
        return (T)this;
    }
    public T setTypeRefer(String typeRefer) {
        if(BasicUtil.isNotEmpty(typeRefer)) {
            this.typeRefer = typeRefer.split(",");
        }else{
            this.typeRefer = null;
        }
        return (T)this;
    }

    public String[] getDefaultRefers() {
        return defaultRefer;
    }
    public String getDefaultRefer(){
        if(null != defaultRefer && defaultRefer.length > 0){
            return defaultRefer[0];
        }
        return null;
    }

    public T setDefaultRefer(String[] defaultRefer) {
        this.defaultRefer = defaultRefer;
        return (T)this;
    }
    public T setDefaultRefer(String defaultRefer) {
        if(BasicUtil.isNotEmpty(defaultRefer)) {
            this.defaultRefer = defaultRefer.split(",");
        }else{
            this.defaultRefer = null;
        }
        return (T)this;
    }
}
