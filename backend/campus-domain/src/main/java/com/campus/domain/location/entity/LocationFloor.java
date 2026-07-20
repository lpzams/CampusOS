package com.campus.domain.location.entity;

import lombok.Data;

@Data
public class LocationFloor {
    private Long id;
    private Long locationId;
    private Integer floor;
    private String rooms;
}
