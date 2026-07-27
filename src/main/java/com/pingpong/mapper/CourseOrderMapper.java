package com.pingpong.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pingpong.entity.CourseOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 课包订单 Mapper 接口
 */
@Mapper
public interface CourseOrderMapper extends BaseMapper<CourseOrder> {

    /**
     * 按月份聚合订单金额（单条SQL替掉12条逐月查询）
     * @return [{m: 1, total: 16320900.00}, ...]
     */
    @Select("SELECT MONTH(created_at) AS m, COALESCE(SUM(paid_amount), 0) AS total " +
            "FROM course_order WHERE 1=1 " +
            "AND created_at >= #{start} AND created_at <= #{end} " +
            "AND (#{storeId} IS NULL OR store_id = #{storeId}) " +
            "GROUP BY MONTH(created_at) ORDER BY m")
    List<Map<String, Object>> sumByMonth(@Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end,
                                         @Param("storeId") Long storeId);

    /**
     * 按门店+月份聚合订单金额（单条SQL替掉 门店数×12 条查表语句）
     * @return [{storeId: 1, m: 1, total: 2241276.00}, ...]
     */
    @Select("SELECT store_id AS storeId, MONTH(created_at) AS m, COALESCE(SUM(paid_amount), 0) AS total " +
            "FROM course_order WHERE 1=1 " +
            "AND created_at >= #{start} AND created_at <= #{end} " +
            "AND (#{storeId} IS NULL OR store_id = #{storeId}) " +
            "GROUP BY store_id, MONTH(created_at) ORDER BY store_id, m")
    List<Map<String, Object>> sumByStoreAndMonth(@Param("start") LocalDateTime start,
                                                  @Param("end") LocalDateTime end,
                                                  @Param("storeId") Long storeId);

    /**
     * 按门店聚合指定时间范围的订单金额（用于月度汇总）
     * @return [{storeId: 1, total: 3725175.00, cnt: 120}, ...]
     */
    @Select("SELECT store_id AS storeId, COALESCE(SUM(paid_amount), 0) AS total, COUNT(*) AS cnt " +
            "FROM course_order WHERE 1=1 " +
            "AND created_at >= #{start} AND created_at <= #{end} " +
            "AND (#{storeId} IS NULL OR store_id = #{storeId}) " +
            "GROUP BY store_id")
    List<Map<String, Object>> sumByStore(@Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end,
                                          @Param("storeId") Long storeId);

    /**
     * 带时间范围的 COUNT
     */
    @Select("SELECT COUNT(*) FROM course_order WHERE 1=1 " +
            "AND created_at >= #{start} AND created_at <= #{end} " +
            "AND (#{storeId} IS NULL OR store_id = #{storeId})")
    Long countInRange(@Param("start") LocalDateTime start,
                      @Param("end") LocalDateTime end,
                      @Param("storeId") Long storeId);

    /**
     * 带时间范围的 SUM(paid_amount)
     */
    @Select("SELECT COALESCE(SUM(paid_amount), 0) FROM course_order WHERE 1=1 " +
            "AND created_at >= #{start} AND created_at <= #{end} " +
            "AND (#{storeId} IS NULL OR store_id = #{storeId})")
    BigDecimal sumAmountInRange(@Param("start") LocalDateTime start,
                                 @Param("end") LocalDateTime end,
                                 @Param("storeId") Long storeId);
}
