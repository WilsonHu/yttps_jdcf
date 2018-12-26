package com.eservice.iot.model.record;

import java.util.Date;
import javax.persistence.*;

public class Record {
    /**
     * 自增ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 记录时间
     */
    @Id
    @Column(name = "record_time")
    private Date recordTime;

    /**
     * 员工号
     */
    @Id
    @Column(name = "staff_id")
    private String staffId;

    /**
     * 姓名
     */
    private String name;

    /**
     * 部门名称
     */
    private String department;

    /**
     * 获取自增ID
     *
     * @return id - 自增ID
     */
    public Integer getId() {
        return id;
    }

    /**
     * 设置自增ID
     *
     * @param id 自增ID
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 获取记录时间
     *
     * @return record_time - 记录时间
     */
    public Date getRecordTime() {
        return recordTime;
    }

    /**
     * 设置记录时间
     *
     * @param recordTime 记录时间
     */
    public void setRecordTime(Date recordTime) {
        this.recordTime = recordTime;
    }

    /**
     * 获取员工号
     *
     * @return staff_id - 员工号
     */
    public String getStaffId() {
        return staffId;
    }

    /**
     * 设置员工号
     *
     * @param staffId 员工号
     */
    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    /**
     * 获取姓名
     *
     * @return name - 姓名
     */
    public String getName() {
        return name;
    }

    /**
     * 设置姓名
     *
     * @param name 姓名
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取部门名称
     *
     * @return department - 部门名称
     */
    public String getDepartment() {
        return department;
    }

    /**
     * 设置部门名称
     *
     * @param department 部门名称
     */
    public void setDepartment(String department) {
        this.department = department;
    }
}