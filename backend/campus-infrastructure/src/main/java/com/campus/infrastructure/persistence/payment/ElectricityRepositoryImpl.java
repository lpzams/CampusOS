package com.campus.infrastructure.persistence.payment;

import com.campus.domain.payment.entity.ElectricityRecord;
import com.campus.domain.payment.repository.ElectricityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ElectricityRepositoryImpl implements ElectricityRepository {
    private final ElectricityMapper electricityMapper;
    private final ElectricityRecordMapper recordMapper;
    private final PaymentConverter converter;

    @Override
    public BigDecimal getBalance(String dormitoryId) {
        BigDecimal balance = electricityMapper.selectBalance(dormitoryId);
        return balance != null ? balance : BigDecimal.ZERO;
    }

    @Override
    public void updateBalance(String dormitoryId, BigDecimal newBalance) {
        int rows = electricityMapper.updateBalance(dormitoryId, newBalance);
        if (rows == 0) {
            // 首次充值，插入初始余额
            ElectricityBalancePO po = new ElectricityBalancePO();
            po.setDormitoryId(dormitoryId);
            po.setBalance(newBalance);
            electricityMapper.insert(po);
        }
    }

    @Override
    public void saveRecord(ElectricityRecord record) {
        recordMapper.insert(converter.toElectricityRecordPO(record));
    }

    @Override
    public List<ElectricityRecord> findRecordsByUserId(Long userId) {
        return converter.toElectricityRecordList(recordMapper.selectByUserId(userId));
    }
}
