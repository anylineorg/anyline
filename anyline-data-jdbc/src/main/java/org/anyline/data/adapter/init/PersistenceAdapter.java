package org.anyline.data.adapter.init;

import org.anyline.data.metadata.persistence.ManyToMany;
import org.anyline.data.metadata.persistence.OneToMany;
import org.anyline.entity.data.Table;
import org.anyline.proxy.EntityAdapterProxy;
import org.anyline.util.ClassUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class PersistenceAdapter {


    public static OneToMany oneToMany(Field field) throws Exception{
        OneToMany join = new OneToMany();
        join.joinColumn = ClassUtil.parseAnnotationFieldValue(field, "OneToMany.mappedBy");
        join.dependencyClass = ClassUtil.getComponentClass(field);;
        join.joinField = ClassUtil.getField(join.dependencyClass, join.joinColumn);
        if(null == join.joinField){
            //提供的是列名
            join.joinField = EntityAdapterProxy.field(join.dependencyClass, join.joinColumn);
        }
        Table table = EntityAdapterProxy.table(join.dependencyClass);
        if(null != table){
            join.dependencyTable = table.getName();
        }
        return join;
    }
    public static ManyToMany manyToMany(Field field) throws Exception{
        ManyToMany join = new ManyToMany();
        join.joinTable = ClassUtil.parseAnnotationFieldValue(field, "JoinTable.name");
        Annotation anJoinTable = ClassUtil.getFieldAnnotation(field, "JoinTable");
        if (null != anJoinTable) {
            Method methodJoinColumns = anJoinTable.annotationType().getMethod("joinColumns");
            if (null != methodJoinColumns) {
                Object[] ojoinColumns = (Object[]) methodJoinColumns.invoke(anJoinTable);
                if (null != ojoinColumns && ojoinColumns.length > 0) {
                    Annotation joinColumn = (Annotation) ojoinColumns[0];
                    join.joinColumn = (String) joinColumn.annotationType().getMethod("name").invoke(joinColumn);
                }
            }
            Method methodInverseJoinColumns = anJoinTable.annotationType().getMethod("inverseJoinColumns");
            if (null != methodInverseJoinColumns) {
                Object[] ojoinColumns = (Object[]) methodInverseJoinColumns.invoke(anJoinTable);
                if (null != ojoinColumns && ojoinColumns.length > 0) {
                    Annotation joinColumn = (Annotation) ojoinColumns[0];
                    join.inverseJoinColumn = (String) joinColumn.annotationType().getMethod("name").invoke(joinColumn);
                }
            }
        }
        join.itemClass = ClassUtil.getComponentClass(field);	//Department
        if(!ClassUtil.isPrimitiveClass(join.itemClass) && String.class != join.itemClass){
            //List<Department> departments;
            org.anyline.entity.data.Table table = EntityAdapterProxy.table(join.itemClass);
            if(null != table){
                join.dependencyTable = table.getName();
                org.anyline.entity.data.Column col = EntityAdapterProxy.primaryKey(join.itemClass);
                if(null != col){
                    join.dependencyPk = col.getName();
                }
            }
        }else{
            //List<Long> departments
            //基础类(只取ID)
        }
        return join;
    }
}
