package com.pingpong.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pingpong.entity.Student;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 学员 Mapper 接口
 * 继承 MyBatis-Plus 的 BaseMapper，自动拥有基础 CRUD 方法。
 */
@Mapper
public interface StudentMapper extends BaseMapper<Student> {

    /**
     * 客户池汇总：每个学员的缴费总额、消课课时、最近消课时间、剩余课时
     */
    @Select("""
        SELECT 
            s.id,
            s.name,
            s.phone,
            s.store_id AS storeId,
            st.name AS storeName,
            COALESCE(sf.name, '-') AS coachName,
            s.total_remaining_lessons AS remainingLessons,
            s.status,
            s.registered_at AS registeredAt,
            s.last_lesson_at AS lastLessonAt,
            COALESCE(paid.totalPaid, 0) AS totalPaid,
            COALESCE(paid.orderCount, 0) AS orderCount,
            COALESCE(con.totalLessons, 0) AS totalConsumedLessons,
            COALESCE(con.consumeCount, 0) AS consumeCount
        FROM student s
        LEFT JOIN store st ON s.store_id = st.id
        LEFT JOIN staff sf ON s.primary_coach_id = sf.id
        LEFT JOIN (
            SELECT student_id, SUM(paid_amount) AS totalPaid, COUNT(*) AS orderCount
            FROM course_order WHERE deleted = 0
            GROUP BY student_id
        ) paid ON paid.student_id = s.id
        LEFT JOIN (
            SELECT student_id, SUM(lessons) AS totalLessons, COUNT(*) AS consumeCount
            FROM course_consumption WHERE deleted = 0
            GROUP BY student_id
        ) con ON con.student_id = s.id
        WHERE s.deleted = 0
            AND (#{storeId} IS NULL OR s.store_id = #{storeId})
            AND (#{keyword} IS NULL OR #{keyword} = '' OR s.name LIKE CONCAT('%', #{keyword}, '%') OR s.phone LIKE CONCAT('%', #{keyword}, '%'))
        ORDER BY ${sortColumn} ${sortDir}
        LIMIT #{size} OFFSET #{offset}
    """)
    List<Map<String, Object>> customerPoolList(@Param("storeId") Long storeId,
                                                @Param("keyword") String keyword,
                                                @Param("sortColumn") String sortColumn,
                                                @Param("sortDir") String sortDir,
                                                @Param("size") Integer size,
                                                @Param("offset") Integer offset);

    /**
     * 客户池总数
     */
    @Select("""
        SELECT COUNT(*) FROM student s
        WHERE s.deleted = 0
            AND (#{storeId} IS NULL OR s.store_id = #{storeId})
            AND (#{keyword} IS NULL OR #{keyword} = '' OR s.name LIKE CONCAT('%', #{keyword}, '%') OR s.phone LIKE CONCAT('%', #{keyword}, '%'))
    """)
    Long countCustomerPool(@Param("storeId") Long storeId, @Param("keyword") String keyword);

    /**
     * 建议约课学员：最近上课时间超过 N 天的活跃学员（有剩余课时）
     */
    @Select("""
        SELECT 
            s.id,
            s.name,
            s.phone,
            st.name AS storeName,
            COALESCE(sf.name, '-') AS coachName,
            s.total_remaining_lessons AS remainingLessons,
            s.last_lesson_at AS lastLessonAt,
            COALESCE(paid.totalPaid, 0) AS totalPaid,
            COALESCE(con.totalLessons, 0) AS totalConsumedLessons
        FROM student s
        LEFT JOIN store st ON s.store_id = st.id
        LEFT JOIN staff sf ON s.primary_coach_id = sf.id
        LEFT JOIN (
            SELECT student_id, SUM(paid_amount) AS totalPaid
            FROM course_order WHERE deleted = 0
            GROUP BY student_id
        ) paid ON paid.student_id = s.id
        LEFT JOIN (
            SELECT student_id, SUM(lessons) AS totalLessons
            FROM course_consumption WHERE deleted = 0
            GROUP BY student_id
        ) con ON con.student_id = s.id
        WHERE s.deleted = 0
            AND s.status = 1
            AND s.total_remaining_lessons > 0
            AND (#{storeId} IS NULL OR s.store_id = #{storeId})
            AND (
                s.last_lesson_at IS NULL 
                OR s.last_lesson_at < DATE_SUB(NOW(), INTERVAL #{days} DAY)
            )
        ORDER BY s.last_lesson_at ASC
        LIMIT 100
    """)
    List<Map<String, Object>> suggestScheduleList(@Param("storeId") Long storeId, @Param("days") Integer days);

    /**
     * 建议续费学员：剩余课时低于阈值的活跃学员
     */
    @Select("""
        SELECT 
            s.id,
            s.name,
            s.phone,
            st.name AS storeName,
            COALESCE(sf.name, '-') AS coachName,
            s.total_remaining_lessons AS remainingLessons,
            s.last_lesson_at AS lastLessonAt,
            COALESCE(paid.totalPaid, 0) AS totalPaid,
            COALESCE(paid.orderCount, 0) AS orderCount,
            COALESCE(con.totalLessons, 0) AS totalConsumedLessons
        FROM student s
        LEFT JOIN store st ON s.store_id = st.id
        LEFT JOIN staff sf ON s.primary_coach_id = sf.id
        LEFT JOIN (
            SELECT student_id, SUM(paid_amount) AS totalPaid, COUNT(*) AS orderCount
            FROM course_order WHERE deleted = 0
            GROUP BY student_id
        ) paid ON paid.student_id = s.id
        LEFT JOIN (
            SELECT student_id, SUM(lessons) AS totalLessons
            FROM course_consumption WHERE deleted = 0
            GROUP BY student_id
        ) con ON con.student_id = s.id
        WHERE s.deleted = 0
            AND s.status = 1
            AND s.total_remaining_lessons > 0
            AND s.total_remaining_lessons <= #{maxRemainingLessons}
            AND (#{storeId} IS NULL OR s.store_id = #{storeId})
        ORDER BY s.total_remaining_lessons ASC
        LIMIT 100
    """)
    List<Map<String, Object>> suggestRenewList(@Param("storeId") Long storeId, @Param("maxRemainingLessons") Integer maxRemainingLessons);

    /**
     * 流失学员：课时耗尽（剩余=0）但仍活跃的学员
     */
    @Select("""
        SELECT 
            s.id,
            s.name,
            s.phone,
            s.store_id AS storeId,
            st.name AS storeName,
            COALESCE(sf.name, '-') AS coachName,
            s.total_remaining_lessons AS remainingLessons,
            s.status,
            s.registered_at AS registeredAt,
            s.last_lesson_at AS lastLessonAt,
            COALESCE(paid.totalPaid, 0) AS totalPaid,
            COALESCE(paid.orderCount, 0) AS orderCount,
            COALESCE(con.totalLessons, 0) AS totalConsumedLessons,
            COALESCE(con.consumeCount, 0) AS consumeCount
        FROM student s
        LEFT JOIN store st ON s.store_id = st.id
        LEFT JOIN staff sf ON s.primary_coach_id = sf.id
        LEFT JOIN (
            SELECT student_id, SUM(paid_amount) AS totalPaid, COUNT(*) AS orderCount
            FROM course_order WHERE deleted = 0
            GROUP BY student_id
        ) paid ON paid.student_id = s.id
        LEFT JOIN (
            SELECT student_id, SUM(lessons) AS totalLessons, COUNT(*) AS consumeCount
            FROM course_consumption WHERE deleted = 0
            GROUP BY student_id
        ) con ON con.student_id = s.id
        WHERE s.deleted = 0
            AND s.status = 1
            AND s.total_remaining_lessons = 0
            AND (#{storeId} IS NULL OR s.store_id = #{storeId})
            AND (#{keyword} IS NULL OR #{keyword} = '' OR s.name LIKE CONCAT('%', #{keyword}, '%') OR s.phone LIKE CONCAT('%', #{keyword}, '%'))
        ORDER BY ${sortColumn} ${sortDir}
        LIMIT #{size} OFFSET #{offset}
    """)
    List<Map<String, Object>> churnedList(@Param("storeId") Long storeId,
                                           @Param("keyword") String keyword,
                                           @Param("sortColumn") String sortColumn,
                                           @Param("sortDir") String sortDir,
                                           @Param("size") Integer size,
                                           @Param("offset") Integer offset);

    /**
     * 流失学员总数
     */
    @Select("""
        SELECT COUNT(*) FROM student s
        WHERE s.deleted = 0
            AND s.status = 1
            AND s.total_remaining_lessons = 0
            AND (#{storeId} IS NULL OR s.store_id = #{storeId})
            AND (#{keyword} IS NULL OR #{keyword} = '' OR s.name LIKE CONCAT('%', #{keyword}, '%') OR s.phone LIKE CONCAT('%', #{keyword}, '%'))
    """)
    Long countChurned(@Param("storeId") Long storeId, @Param("keyword") String keyword);
}
