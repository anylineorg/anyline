package org.anyline.data.metadata.persistence;

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
