package com.campus.infrastructure.persistence.shared;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("t_campus_record")
public class CampusRecordPO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String recordType;
    private String recordData;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRecordType() { return recordType; }
    public void setRecordType(String recordType) { this.recordType = recordType; }
    public String getRecordData() { return recordData; }
    public void setRecordData(String recordData) { this.recordData = recordData; }
}
