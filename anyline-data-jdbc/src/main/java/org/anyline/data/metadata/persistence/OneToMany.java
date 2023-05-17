package org.anyline.data.metadata.persistence;

public class OneToMany {

    /*
        //考勤记录
        //这里写成AttendanceRecord的一个属性也可以写成对应的列名
        //把当前主键值赋值给AttendanceRecord.employeeId
        @OneToMany(mappedBy = "employeeId")
        private List<AttendanceRecord> records = null;

    */
    public String joinColumn		; // employeeId/EMPLOYEE_ID				: 外键
}
