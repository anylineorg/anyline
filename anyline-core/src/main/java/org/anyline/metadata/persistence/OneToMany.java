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

package org.anyline.metadata.persistence;

import java.lang.reflect.Field;

public class OneToMany {

    /*
        //考勤记录
        //这里写成AttendanceRecord的一个属性也可以写成对应的列名
        //把当前主键值赋值给AttendanceRecord.employeeId
        @OneToMany(mappedBy = "employeeId")
        private List<AttendanceRecord> records = null;

    */
    public String joinColumn		; // EMPLOYEE_ID				: 外键
    public Field joinField         ; // employeeId
    public Class dependencyClass	; // AttendanceRecord
    public String dependencyTable	; // HR_ATTENDANCE_RECORD				: 依赖表(根据AttendanceRecord类上的注解)
}
