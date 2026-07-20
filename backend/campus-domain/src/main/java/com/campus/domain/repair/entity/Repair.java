package com.campus.domain.repair.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Repair {
    private Long id;
    private Long userId;
    private String type;
    private String typeCode;
    private String title;
    private String description;
    private String images;
    private String building;
    private String room;
    private String contactPhone;
    private String status;
    private String statusDesc;
    private LocalDateTime expectedTime;
    private LocalDateTime completeTime;
    private String handler;
    private String handlerPhone;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 常量
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    public static final String TYPE_WATER_ELECTRIC = "WATER_ELECTRIC";
    public static final String TYPE_EQUIPMENT = "EQUIPMENT";
    public static final String TYPE_DOOR_WINDOW = "DOOR_WINDOW";
    public static final String TYPE_NETWORK = "NETWORK";
    public static final String TYPE_OTHER = "OTHER";

    public String getStatusDesc() {
        switch (status) {
            case STATUS_PENDING: return "待接单";
            case STATUS_PROCESSING: return "维修中";
            case STATUS_COMPLETED: return "已维修";
            case STATUS_CANCELLED: return "已取消";
            default: return status;
        }
    }

    public static String typeNameToCode(String typeName) {
        switch (typeName) {
            case "水电": return TYPE_WATER_ELECTRIC;
            case "设备": return TYPE_EQUIPMENT;
            case "门窗": return TYPE_DOOR_WINDOW;
            case "网络": return TYPE_NETWORK;
            default: return TYPE_OTHER;
        }
    }

    // 工厂方法
    public static Repair create(Long userId, String type, String typeCode, String title,
                                 String description, String images, String building,
                                 String room, String contactPhone) {
        Repair r = new Repair();
        r.setUserId(userId);
        r.setType(type);
        r.setTypeCode(typeCode != null ? typeCode : typeNameToCode(type));
        r.setTitle(title);
        r.setDescription(description);
        r.setImages(images);
        r.setBuilding(building);
        r.setRoom(room);
        r.setContactPhone(contactPhone);
        r.setStatus(STATUS_PENDING);
        r.setCreateTime(LocalDateTime.now());
        r.setUpdateTime(LocalDateTime.now());
        return r;
    }

    /** 更新状态 */
    public void updateStatus(String newStatus, String handler, String handlerPhone) {
        this.status = newStatus;
        if (handler != null) this.handler = handler;
        if (handlerPhone != null) this.handlerPhone = handlerPhone;
        if (STATUS_COMPLETED.equals(newStatus)) this.completeTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }
}
