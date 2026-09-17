package com.example.calculator.domain.repository;

import com.example.calculator.domain.model.Calculation;
import java.util.List;

/**
 * 计算历史仓储端口。
 *
 * <p>领域层只声明所需能力，不关心数据具体保存在内存、数据库还是
 * 其他介质。
 */
public interface CalculationHistoryRepository {

    /** 保存一条计算记录。 */
    void save(Calculation calculation);

    /** 按保存顺序返回当前保留的全部记录。 */
    List<Calculation> findAll();

    /** 清空当前保存的计算历史。 */
    void clear();
}
