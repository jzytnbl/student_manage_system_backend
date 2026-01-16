package com.zwz.studentmanagebackend.repository;

import com.zwz.studentmanagebackend.entity.ApiLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ApiLogRepository extends JpaRepository<ApiLog, Long> {

    // 根据用户名查询API日志
    List<ApiLog> findByUsername(String username);

    // 分页查询API日志
    Page<ApiLog> findByUsername(String username, Pageable pageable);

    // 根据API名称查询
    Page<ApiLog> findByApiNameContaining(String apiName, Pageable pageable);

    // 根据时间范围查询
    @Query("SELECT a FROM ApiLog a WHERE a.requestTime >= :start AND a.requestTime <= :end")
    List<ApiLog> findByTimeRange(@Param("start") LocalDateTime start,
                                 @Param("end") LocalDateTime end);

    // 查询成功/失败的API日志
    Page<ApiLog> findBySuccess(Boolean success, Pageable pageable);

    // 统计API调用次数 - 修复：给聚合函数起别名
    @Query("SELECT a.apiName, COUNT(a) as callCount FROM ApiLog a " +
            "WHERE a.requestTime >= :startTime " +
            "GROUP BY a.apiName " +
            "ORDER BY callCount DESC")
    List<Object[]> countApiCalls(@Param("startTime") LocalDateTime startTime);

    // 统计成功率 - 修复：给聚合函数起别名
    @Query("SELECT a.apiName, " +
            "COUNT(a) as totalCount, " +
            "SUM(CASE WHEN a.success = true THEN 1 ELSE 0 END) as successCount " +
            "FROM ApiLog a " +
            "WHERE a.requestTime >= :startTime " +
            "GROUP BY a.apiName")
    List<Object[]> countApiSuccessRate(@Param("startTime") LocalDateTime startTime);

    // 删除旧日志（清理用）
    void deleteByRequestTimeBefore(LocalDateTime date);
}