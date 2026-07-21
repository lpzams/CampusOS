package com.campus.domain.repair.repository;

import com.campus.domain.repair.entity.Repair;
import com.campus.domain.repair.entity.RepairEvaluation;
import com.campus.domain.repair.entity.RepairProgress;
import java.util.List;

public interface RepairRepository {
    // Repair CRUD
    void save(Repair repair);
    void update(Repair repair);
    Repair findById(Long id);
    List<Repair> findByUserId(Long userId);
    List<Repair> findAll(String status, int offset, int size);
    long count(String status);

    // Progress
    void saveProgress(RepairProgress progress);
    List<RepairProgress> findProgressByRepairId(Long repairId);

    // Evaluation
    void saveEvaluation(RepairEvaluation evaluation);
    RepairEvaluation findEvaluationByRepairId(Long repairId);
}
