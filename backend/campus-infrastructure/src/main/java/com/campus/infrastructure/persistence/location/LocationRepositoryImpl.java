package com.campus.infrastructure.persistence.location;

import com.campus.domain.location.entity.Location;
import com.campus.domain.location.entity.LocationFloor;
import com.campus.domain.location.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class LocationRepositoryImpl implements LocationRepository {

    private final LocationMapper locationMapper;
    private final LocationFloorMapper locationFloorMapper;

    private Location toLocation(LocationPO po) { Location l = new Location(); BeanUtils.copyProperties(po, l); return l; }
    private LocationFloor toFloor(LocationFloorPO po) { LocationFloor f = new LocationFloor(); BeanUtils.copyProperties(po, f); return f; }

    @Override
    public List<Location> findAll(String category) {
        return locationMapper.selectAll(category).stream().map(this::toLocation).collect(Collectors.toList());
    }

    @Override
    public Location findById(Long id) {
        return toLocation(locationMapper.selectById(id));
    }

    @Override
    public List<Location> searchByKeyword(String keyword) {
        return locationMapper.searchByKeyword(keyword).stream().map(this::toLocation).collect(Collectors.toList());
    }

    @Override
    public List<LocationFloor> findFloorsByLocationId(Long locationId) {
        return locationFloorMapper.selectByLocationId(locationId).stream().map(this::toFloor).collect(Collectors.toList());
    }
}
