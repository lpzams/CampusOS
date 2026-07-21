package com.campus.domain.location.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Location {
    private Long id;
    private String name;
    private String category;
    private Double longitude;
    private Double latitude;
    private String address;
    private String building;
    private String image;
    private String description;
    private String facilities;
    private String openTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // ===== 分类常量 =====
    public static final String CATEGORY_BUILDING = "BUILDING";
    public static final String CATEGORY_LIBRARY = "LIBRARY";
    public static final String CATEGORY_CANTEEN = "CANTEEN";
    public static final String CATEGORY_DORMITORY = "DORMITORY";
    public static final String CATEGORY_GYM = "GYM";

    public String getCategoryDesc() {
        if (CATEGORY_BUILDING.equals(category)) return "教学楼";
        if (CATEGORY_LIBRARY.equals(category)) return "图书馆";
        if (CATEGORY_CANTEEN.equals(category)) return "食堂";
        if (CATEGORY_DORMITORY.equals(category)) return "宿舍";
        if (CATEGORY_GYM.equals(category)) return "体育馆";
        return category;
    }
}
