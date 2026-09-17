package com.example.calculator.infrastructure.logging;

import java.util.List;

/**
 * HTTP 访问日志内存存储端口。
 *
 * <p>控制台日志用于生产日志采集，内存存储保留最近记录，
 * 方便测试和进程内诊断。
 */
public interface HttpAccessLogStore {

    /** 追加一条访问日志。 */
    void append(HttpAccessLogEntry entry);

    /** 返回按发生时间排列的只读快照。 */
    List<HttpAccessLogEntry> snapshot();

    /** 清空当前进程中的访问日志。 */
    void clear();
}
