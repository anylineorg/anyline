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

package org.anyline.metadata.persistence;

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
