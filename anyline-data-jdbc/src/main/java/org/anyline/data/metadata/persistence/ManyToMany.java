package org.anyline.data.metadata.persistence;

public class ManyToMany {

    /*
        HR_EMPLOYEE				:主表 当前表
        HR_EMPLOYEE_DEPARTMENT 	:关联表
        HR_DEPARTMENT			:依赖表
         @ManyToMany
         @JoinTable(name = "HR_EMPLOYEE_DEPARTMENT"                 //中间关联表
        , joinColumns = @JoinColumn(name="EMPLOYEE_ID")             //关联表中与当前表关联的外键
        , inverseJoinColumns = @JoinColumn(name="DEPARTMENT_ID"))   //关联表中与当前表关联的外键
        List<Department> departments;
    */

    public String joinTable			; // HR_EMPLOYEE_DEPARTMENT 	: 关联表
    public String joinColumn		; // EMPLOYEE_ID				: 关联表中与当前表关联的外键
    public String inverseJoinColumn	; // DEPARTMENT_ID				: 关联表中与右表关联的外键
    public String dependencyTable	; // HR_DEPARTMENT				: 依赖表(根据Department类上的注解)
    public Object fieldInstance		; // ArrayList<Department>		:
    public Class itemClass			; // Department					:
    public String dependencyPk		; // ID							: 依赖表主键(HR_DEPARTMENT.ID)
}
