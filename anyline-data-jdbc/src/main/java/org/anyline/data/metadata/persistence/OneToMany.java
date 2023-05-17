package org.anyline.data.metadata.persistence;

public class OneToMany {

    /*
        //考勤记录
        @OneToMany(mappedBy = "EMPLOYEE_ID")
        private List<AttendanceRecord> records = null;
    */
    public String joinColumn		; // EMPLOYEE_ID				: 外键
}
