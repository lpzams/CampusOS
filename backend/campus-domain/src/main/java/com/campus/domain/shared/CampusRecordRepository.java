package com.campus.domain.shared;

import java.util.List;
import java.util.Optional;

public interface CampusRecordRepository {
    CampusRecord save(CampusRecord record);
    Optional<CampusRecord> findById(String type, Long id);
    List<CampusRecord> findByType(String type);
    void delete(String type, Long id);
}
