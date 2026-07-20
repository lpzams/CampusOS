package com.campus.infrastructure.persistence.repair;

import com.campus.domain.repair.entity.*;
import com.campus.domain.repair.repository.RepairRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class RepairRepositoryImpl implements RepairRepository {
    private final RepairMapper repairMapper;
    private final RepairProgressMapper progressMapper;
    private final RepairEvaluationMapper evaluationMapper;

    private Repair toRepair(RepairPO po) { Repair r = new Repair(); BeanUtils.copyProperties(po, r); return r; }
    private RepairPO toPO(Repair r) { RepairPO po = new RepairPO(); BeanUtils.copyProperties(r, po); return po; }
    private RepairProgress toProgress(RepairProgressPO po) { RepairProgress p = new RepairProgress(); BeanUtils.copyProperties(po, p); return p; }
    private RepairProgressPO toProgressPO(RepairProgress p) { RepairProgressPO po = new RepairProgressPO(); BeanUtils.copyProperties(p, po); return po; }
    private RepairEvaluation toEval(RepairEvaluationPO po) { RepairEvaluation e = new RepairEvaluation(); BeanUtils.copyProperties(po, e); return e; }
    private RepairEvaluationPO toEvalPO(RepairEvaluation e) { RepairEvaluationPO po = new RepairEvaluationPO(); BeanUtils.copyProperties(e, po); return po; }

    @Override public void save(Repair repair) { RepairPO po = toPO(repair); repairMapper.insert(po); repair.setId(po.getId()); }
    @Override public void update(Repair repair) { repairMapper.updateById(toPO(repair)); }
    @Override public Repair findById(Long id) { return toRepair(repairMapper.selectById(id)); }
    @Override public List<Repair> findByUserId(Long userId) {
        return repairMapper.selectByUserId(userId).stream().map(this::toRepair).collect(Collectors.toList());
    }
    @Override public List<Repair> findAll(String status, int offset, int size) {
        return repairMapper.selectPage(status, offset, size).stream().map(this::toRepair).collect(Collectors.toList());
    }
    @Override public long count(String status) { return repairMapper.count(status); }
    @Override public void saveProgress(RepairProgress progress) { progressMapper.insert(toProgressPO(progress)); }
    @Override public List<RepairProgress> findProgressByRepairId(Long repairId) {
        return progressMapper.selectByRepairId(repairId).stream().map(this::toProgress).collect(Collectors.toList());
    }
    @Override public void saveEvaluation(RepairEvaluation evaluation) { evaluationMapper.insert(toEvalPO(evaluation)); }
    @Override public RepairEvaluation findEvaluationByRepairId(Long repairId) { return toEval(evaluationMapper.selectByRepairId(repairId)); }
}
