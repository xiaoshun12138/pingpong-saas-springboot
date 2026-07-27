package com.pingpong.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pingpong.entity.CourseConsumption;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 消课记录 Mapper 接口
 */
@Mapper
public interface CourseConsumptionMapper extends BaseMapper<CourseConsumption> {

    /**
     * 按月份聚合消课课时（单条SQL替掉12条逐月查询）
     * @return [{m: 1, lessons: 5000}, ...]
     */
    @Select("SELECT MONTH(created_at) AS m, COALESCE(SUM(lessons), 0) AS lessons " +
            "FROM course_consumption WHERE 1=1 " +
            "AND created_at >= #{start} AND created_at <= #{end} " +
            "AND (#{storeId} IS NULL OR store_id = #{storeId}) " +
            "GROUP BY MONTH(created_at) ORDER BY m")
    List<Map<String, Object>> sumByMonth(@Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end,
                                         @Param("storeId") Long storeId);

    /**
     * 按门店+月份聚合消课课时（单条SQL替掉 门店数×12 条）
     * @return [{storeId: 1, m: 1, lessons: 23188}, ...]
     */
    @Select("SELECT store_id AS storeId, MONTH(created_at) AS m, COALESCE(SUM(lessons), 0) AS lessons " +
            "FROM course_consumption WHERE 1=1 " +
            "AND created_at >= #{start} AND created_at <= #{end} " +
            "AND (#{storeId} IS NULL OR store_id = #{storeId}) " +
            "GROUP BY store_id, MONTH(created_at) ORDER BY store_id, m")
    List<Map<String, Object>> sumByStoreAndMonth(@Param("start") LocalDateTime start,
                                                  @Param("end") LocalDateTime end,
                                                  @Param("storeId") Long storeId);

    /**
     * 按门店聚合指定时间范围的消课课时
     * @return [{storeId: 1, lessons: 23188, cnt: 50}, ...]
     */
    @Select("SELECT store_id AS storeId, COALESCE(SUM(lessons), 0) AS lessons, COUNT(*) AS cnt " +
            "FROM course_consumption WHERE 1=1 " +
            "AND created_at >= #{start} AND created_at <= #{end} " +
            "AND (#{storeId} IS NULL OR store_id = #{storeId}) " +
            "GROUP BY store_id")
    List<Map<String, Object>> sumByStore(@Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end,
                                          @Param("storeId") Long storeId);

    /**
     * 带时间范围的 SUM(lessons)
     */
    @Select("SELECT COALESCE(SUM(lessons), 0) FROM course_consumption WHERE 1=1 " +
            "AND created_at >= #{start} AND created_at <= #{end} " +
            "AND (#{storeId} IS NULL OR store_id = #{storeId})")
    Long sumLessonsInRange(@Param("start") LocalDateTime start,
                            @Param("end") LocalDateTime end,
                            @Param("storeId") Long storeId);

    // ===== 金额聚合方法（消课金额 = 课时 × 订单单价） =====

    /**
     * 按月份聚合消课金额（JOIN 订单表按课时单价折算）
     * @return [{m: 1, amount: 16320900.00}, ...]
     */
    @Select("SELECT MONTH(cc.created_at) AS m, " +
            "COALESCE(SUM(cc.lessons * co.paid_amount / co.total_lessons), 0) AS amount " +
            "FROM course_consumption cc " +
            "JOIN course_order co ON cc.course_order_id = co.id AND co.total_lessons > 0 " +
            "WHERE cc.created_at >= #{start} AND cc.created_at <= #{end} " +
            "AND (#{storeId} IS NULL OR cc.store_id = #{storeId}) " +
            "GROUP BY MONTH(cc.created_at) ORDER BY m")
    List<Map<String, Object>> sumAmountByMonth(@Param("start") LocalDateTime start,
                                               @Param("end") LocalDateTime end,
                                               @Param("storeId") Long storeId);

    /**
     * 按门店+月份聚合消课金额
     * @return [{storeId: 1, m: 1, amount: 2241276.00}, ...]
     */
    @Select("SELECT cc.store_id AS storeId, MONTH(cc.created_at) AS m, " +
            "COALESCE(SUM(cc.lessons * co.paid_amount / co.total_lessons), 0) AS amount " +
            "FROM course_consumption cc " +
            "JOIN course_order co ON cc.course_order_id = co.id AND co.total_lessons > 0 " +
            "WHERE cc.created_at >= #{start} AND cc.created_at <= #{end} " +
            "AND (#{storeId} IS NULL OR cc.store_id = #{storeId}) " +
            "GROUP BY cc.store_id, MONTH(cc.created_at) ORDER BY cc.store_id, m")
    List<Map<String, Object>> sumAmountByStoreAndMonth(@Param("start") LocalDateTime start,
                                                        @Param("end") LocalDateTime end,
                                                        @Param("storeId") Long storeId);

    /**
     * 按门店聚合消课金额
     * @return [{storeId: 1, amount: 3725175.00, cnt: 120}, ...]
     */
    @Select("SELECT cc.store_id AS storeId, " +
            "COALESCE(SUM(cc.lessons * co.paid_amount / co.total_lessons), 0) AS amount, " +
            "COUNT(*) AS cnt " +
            "FROM course_consumption cc " +
            "JOIN course_order co ON cc.course_order_id = co.id AND co.total_lessons > 0 " +
            "WHERE cc.created_at >= #{start} AND cc.created_at <= #{end} " +
            "AND (#{storeId} IS NULL OR cc.store_id = #{storeId}) " +
            "GROUP BY cc.store_id")
    List<Map<String, Object>> sumAmountByStore(@Param("start") LocalDateTime start,
                                                @Param("end") LocalDateTime end,
                                                @Param("storeId") Long storeId);

    /**
     * 带时间范围的消课金额总和
     */
    @Select("SELECT COALESCE(SUM(cc.lessons * co.paid_amount / co.total_lessons), 0) " +
            "FROM course_consumption cc " +
            "JOIN course_order co ON cc.course_order_id = co.id AND co.total_lessons > 0 " +
            "WHERE cc.created_at >= #{start} AND cc.created_at <= #{end} " +
            "AND (#{storeId} IS NULL OR cc.store_id = #{storeId})")
    BigDecimal sumAmountInRange(@Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end,
                                @Param("storeId") Long storeId);
}
