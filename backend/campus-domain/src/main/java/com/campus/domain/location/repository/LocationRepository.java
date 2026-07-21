package com.campus.domain.location.repository;

import com.campus.domain.location.entity.Location;
import com.campus.domain.location.entity.LocationFloor;

import java.util.List;

public interface LocationRepository {

    // Location
    List<Location> findAll(String category);
    Location findById(Long id);
    List<Location> searchByKeyword(String keyword);

    // Floor
    List<LocationFloor> findFloorsByLocationId(Long locationId);
}
