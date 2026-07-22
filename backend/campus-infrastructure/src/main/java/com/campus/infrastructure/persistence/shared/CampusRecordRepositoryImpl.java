package com.campus.infrastructure.persistence.shared;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.domain.shared.CampusRecord;
import com.campus.domain.shared.CampusRecordRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class CampusRecordRepositoryImpl implements CampusRecordRepository {
    private final CampusRecordMapper mapper;
    private final ObjectMapper json;

    public CampusRecordRepositoryImpl(CampusRecordMapper mapper, ObjectMapper json) {
        this.mapper = mapper;
        this.json = json;
    }

    @Override
    public CampusRecord save(CampusRecord record) {
        CampusRecordPO po = toPO(record);
        if (po.getId() == null) mapper.insert(po); else mapper.updateById(po);
        record.setId(po.getId());
        return record;
    }

    @Override
    public Optional<CampusRecord> findById(String type, Long id) {
        CampusRecordPO po = mapper.selectOne(new LambdaQueryWrapper<CampusRecordPO>()
                .eq(CampusRecordPO::getId, id).eq(CampusRecordPO::getRecordType, type));
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public List<CampusRecord> findByType(String type) {
        return mapper.selectList(new LambdaQueryWrapper<CampusRecordPO>()
                        .eq(CampusRecordPO::getRecordType, type).orderByDesc(CampusRecordPO::getId))
                .stream().map(this::toDomain).toList();
    }

    @Override
    public void delete(String type, Long id) {
        mapper.delete(new LambdaQueryWrapper<CampusRecordPO>()
                .eq(CampusRecordPO::getId, id).eq(CampusRecordPO::getRecordType, type));
    }

    private CampusRecordPO toPO(CampusRecord record) {
        try {
            CampusRecordPO po = new CampusRecordPO();
            po.setId(record.getId());
            po.setRecordType(record.getType());
            po.setRecordData(json.writeValueAsString(record.getData()));
            return po;
        } catch (Exception e) { throw new IllegalStateException("业务数据序列化失败", e); }
    }

    private CampusRecord toDomain(CampusRecordPO po) {
        try {
            Map<String, Object> data = json.readValue(po.getRecordData(), new TypeReference<>() {});
            return new CampusRecord(po.getId(), po.getRecordType(), data);
        } catch (Exception e) { throw new IllegalStateException("业务数据反序列化失败", e); }
    }
}
