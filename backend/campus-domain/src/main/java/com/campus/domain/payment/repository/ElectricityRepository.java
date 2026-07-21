package com.campus.domain.payment.repository;

import com.campus.domain.payment.entity.ElectricityRecord;

import java.math.BigDecimal;
import java.util.List;

public interface ElectricityRepository {
    /** 查宿舍电费余额 */
    BigDecimal getBalance(String dormitoryId);
    /** 更新宿舍电费余额 */
    void updateBalance(String dormitoryId, BigDecimal newBalance);
    /** 新增电费记录 */
    void saveRecord(ElectricityRecord record);
    /** 查充值记录 */
    List<ElectricityRecord> findRecordsByUserId(Long userId);
}
