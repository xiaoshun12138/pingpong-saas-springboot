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
            "AND deleted = 0 AND status != 'refunded' " +
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
            "AND deleted = 0 AND status != 'refunded' " +
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
            "AND deleted = 0 AND status != 'refunded' " +
            "AND created_at >= #{start} AND created_at <= #{end} " +
            "AND (#{storeId} IS NULL OR store_id = #{storeId}) " +
            "GROUP BY store_id")
    List<Map<String, Object>> sumByStore(@Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end,
                                          @Param("storeId") Long storeId);

    /**
     * 按天聚合本月订单金额（每日走势图）
     * @return [{d: 1, total: 12345.00}, {d: 2, total: 0.00}, ...]
     */
    @Select("SELECT DAY(created_at) AS d, COALESCE(SUM(paid_amount), 0) AS total " +
            "FROM course_order WHERE 1=1 " +
            "AND deleted = 0 AND status != 'refunded' " +
            "AND created_at >= #{start} AND created_at <= #{end} " +
            "AND (#{storeId} IS NULL OR store_id = #{storeId}) " +
            "GROUP BY DAY(created_at) ORDER BY d")
    List<Map<String, Object>> sumByDay(@Param("start") LocalDateTime start,
                                        @Param("end") LocalDateTime end,
                                        @Param("storeId") Long storeId);

    /**
     * 带时间范围的 COUNT
     */
    @Select("SELECT COUNT(*) FROM course_order WHERE 1=1 " +
            "AND deleted = 0 " +
            "AND created_at >= #{start} AND created_at <= #{end} " +
            "AND (#{storeId} IS NULL OR store_id = #{storeId})")
    Long countInRange(@Param("start") LocalDateTime start,
                      @Param("end") LocalDateTime end,
                      @Param("storeId") Long storeId);

    /**
     * 带时间范围的 SUM(paid_amount)
     */
    @Select("SELECT COALESCE(SUM(paid_amount), 0) FROM course_order WHERE 1=1 " +
            "AND deleted = 0 AND status != 'refunded' " +
            "AND created_at >= #{start} AND created_at <= #{end} " +
            "AND (#{storeId} IS NULL OR store_id = #{storeId})")
    BigDecimal sumAmountInRange(@Param("start") LocalDateTime start,
                                 @Param("end") LocalDateTime end,
                                 @Param("storeId") Long storeId);

    /**
     * 按订单类型统计笔数（new=新报 / renew=续费）
     */
    @Select("SELECT COUNT(*) FROM course_order WHERE 1=1 " +
            "AND deleted = 0 " +
            "AND created_at >= #{start} AND created_at <= #{end} " +
            "AND type = #{type} " +
            "AND (#{storeId} IS NULL OR store_id = #{storeId})")
    Long countByType(@Param("start") LocalDateTime start,
                     @Param("end") LocalDateTime end,
                     @Param("storeId") Long storeId,
                     @Param("type") String type);

    /**
     * 按订单类型统计金额（new=新报 / renew=续费）
     */
    @Select("SELECT COALESCE(SUM(paid_amount), 0) FROM course_order WHERE 1=1 " +
            "AND deleted = 0 AND status != 'refunded' " +
            "AND created_at >= #{start} AND created_at <= #{end} " +
            "AND type = #{type} " +
            "AND (#{storeId} IS NULL OR store_id = #{storeId})")
    BigDecimal sumAmountByType(@Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end,
                                @Param("storeId") Long storeId,
                                @Param("type") String type);

    /**
     * 查询学员名下所有课包（JOIN course_type 获取课包名称）
     */
    @Select("SELECT " +
            "  o.id AS orderId, " +
            "  o.order_no AS orderNo, " +
            "  COALESCE(ct.name, '未知课包') AS courseTypeName, " +
            "  COALESCE(sf.name, '-') AS coachName, " +
            "  o.total_lessons AS totalLessons, " +
            "  o.remaining_lessons AS remainingLessons, " +
            "  o.consumed_lessons AS consumedLessons, " +
            "  o.paid_amount AS paidAmount, " +
            "  o.status " +
            "FROM course_order o " +
            "LEFT JOIN course_type ct ON o.course_type_id = ct.id " +
            "LEFT JOIN staff sf ON o.coach_id = sf.id " +
            "WHERE o.student_id = #{studentId} " +
            "  AND o.deleted = 0 " +
            "ORDER BY o.created_at DESC")
    List<com.pingpong.vo.StudentOrderVO> getStudentOrders(@Param("studentId") Long studentId);

    // ===== 排名聚合方法（SQL GROUP BY 替代内存聚合） =====

    /**
     * 教练业绩排名聚合（按 coach_id 分组）
     */
    @Select("SELECT o.coach_id AS staffId, " +
            "COALESCE(SUM(o.paid_amount), 0) AS amount, " +
            "COUNT(*) AS cnt " +
            "FROM course_order o " +
            "WHERE o.deleted = 0 AND o.status != 'refunded' " +
            "AND o.created_at >= #{start} AND o.created_at <= #{end} " +
            "AND (#{storeId} IS NULL OR o.store_id = #{storeId}) " +
            "AND o.coach_id IS NOT NULL " +
            "GROUP BY o.coach_id")
    List<Map<String, Object>> rankByCoach(@Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end,
                                           @Param("storeId") Long storeId);

    /**
     * 销售业绩排名聚合（按 sales_id 分组）
     */
    @Select("SELECT o.sales_id AS staffId, " +
            "COALESCE(SUM(o.paid_amount), 0) AS amount, " +
            "COUNT(*) AS cnt " +
            "FROM course_order o " +
            "WHERE o.deleted = 0 AND o.status != 'refunded' " +
            "AND o.created_at >= #{start} AND o.created_at <= #{end} " +
            "AND (#{storeId} IS NULL OR o.store_id = #{storeId}) " +
            "AND o.sales_id IS NOT NULL " +
            "GROUP BY o.sales_id")
    List<Map<String, Object>> rankBySales(@Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end,
                                           @Param("storeId") Long storeId);

    /**
     * 门店业绩排名聚合（按 store_id 分组）
     */
    @Select("SELECT o.store_id AS storeId, " +
            "COALESCE(SUM(o.paid_amount), 0) AS amount, " +
            "COUNT(*) AS cnt " +
            "FROM course_order o " +
            "WHERE o.deleted = 0 AND o.status != 'refunded' " +
            "AND o.created_at >= #{start} AND o.created_at <= #{end} " +
            "AND (#{storeId} IS NULL OR o.store_id = #{storeId}) " +
            "GROUP BY o.store_id")
    List<Map<String, Object>> rankByStore(@Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end,
                                           @Param("storeId") Long storeId);
}
