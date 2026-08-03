package com.pingpong.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pingpong.common.R;
import com.pingpong.mapper.CourseConsumptionMapper;
import com.pingpong.mapper.CourseOrderMapper;
import com.pingpong.mapper.StudentMapper;
import com.pingpong.entity.Student;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 客户池控制器
 * 提供学员维度的汇总数据：缴费排名、消课排名、需约课学员等。
 */
@RestController
@RequestMapping("/api/customer-pool")
public class CustomerPoolController {

    @Autowired
    private StudentMapper studentMapper;

    /**
     * 客户池汇总查询：一条 SQL 查出每个学员的缴费总额、消课课时、最近消课时间、剩余课时等
     * 支持按门店筛选、关键词搜索、排序。
     */
    @GetMapping
    public R<Page<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "totalPaid") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder,
            HttpServletRequest request) {

        String role = (String) request.getAttribute("role");
        Long myStoreId = (Long) request.getAttribute("storeId");
        Long filterStoreId = "boss".equals(role) ? storeId : myStoreId;

        // 排序字段白名单
        String sortColumn = switch (sortBy) {
            case "totalPaid" -> "totalPaid";
            case "totalConsumedLessons" -> "totalConsumedLessons";
            case "remainingLessons" -> "remainingLessons";
            case "lastLessonAt" -> "lastLessonAt";
            case "orderCount" -> "orderCount";
            default -> "totalPaid";
        };
        String sortDir = "asc".equalsIgnoreCase(sortOrder) ? "ASC" : "DESC";

        // 先查总数
        Long total = studentMapper.countCustomerPool(filterStoreId, keyword);
        if (total == null) total = 0L;

        // 再查分页数据
        Integer offset = (current - 1) * size;
        List<Map<String, Object>> records = studentMapper.customerPoolList(
                filterStoreId, keyword, sortColumn, sortDir, size, offset);

        // 填充门店名
        if (records != null && !records.isEmpty()) {
            for (Map<String, Object> r : records) {
                // 计算"距今天数"
                Object lastLessonAt = r.get("lastLessonAt");
                if (lastLessonAt != null) {
                    java.time.LocalDateTime ldt;
                    if (lastLessonAt instanceof java.time.LocalDateTime l) {
                        ldt = l;
                    } else if (lastLessonAt instanceof String s) {
                        ldt = java.time.LocalDateTime.parse(s.replace(" ", "T"));
                    } else {
                        ldt = null;
                    }
                    if (ldt != null) {
                        long days = java.time.Duration.between(ldt, java.time.LocalDateTime.now()).toDays();
                        r.put("daysSinceLastLesson", days);
                    }
                }
            }
        }

        Page<Map<String, Object>> page = new Page<>(current, size);
        page.setTotal(total);
        page.setRecords(records != null ? records : List.of());
        return R.ok(page);
    }

    /**
     * 建议约课学员列表：最近上课时间超过 N 天的活跃学员（有剩余课时）
     */
    @GetMapping("/suggest-schedule")
    public R<List<Map<String, Object>>> suggestSchedule(
            @RequestParam(defaultValue = "14") Integer days,
            @RequestParam(required = false) Long storeId,
            HttpServletRequest request) {

        String role = (String) request.getAttribute("role");
        Long myStoreId = (Long) request.getAttribute("storeId");
        Long filterStoreId = "boss".equals(role) ? storeId : myStoreId;

        List<Map<String, Object>> list = studentMapper.suggestScheduleList(filterStoreId, days);
        return R.ok(list);
    }

    /**
     * 建议续费学员列表：剩余课时低于阈值的活跃学员
     */
    @GetMapping("/suggest-renew")
    public R<List<Map<String, Object>>> suggestRenew(
            @RequestParam(defaultValue = "5") Integer maxRemainingLessons,
            @RequestParam(required = false) Long storeId,
            HttpServletRequest request) {

        String role = (String) request.getAttribute("role");
        Long myStoreId = (Long) request.getAttribute("storeId");
        Long filterStoreId = "boss".equals(role) ? storeId : myStoreId;

        List<Map<String, Object>> list = studentMapper.suggestRenewList(filterStoreId, maxRemainingLessons);
        return R.ok(list);
    }

    /**
     * 流失学员列表：课时耗尽（剩余=0）但仍活跃的学员
     */
    @GetMapping("/churned")
    public R<Page<Map<String, Object>>> churned(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "totalPaid") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder,
            HttpServletRequest request) {

        String role = (String) request.getAttribute("role");
        Long myStoreId = (Long) request.getAttribute("storeId");
        Long filterStoreId = "boss".equals(role) ? storeId : myStoreId;

        String sortColumn = switch (sortBy) {
            case "totalPaid" -> "totalPaid";
            case "totalConsumedLessons" -> "totalConsumedLessons";
            case "remainingLessons" -> "remainingLessons";
            case "lastLessonAt" -> "lastLessonAt";
            case "orderCount" -> "orderCount";
            default -> "totalPaid";
        };
        String sortDir = "asc".equalsIgnoreCase(sortOrder) ? "ASC" : "DESC";

        Long total = studentMapper.countChurned(filterStoreId, keyword);
        if (total == null) total = 0L;

        Integer offset = (current - 1) * size;
        List<Map<String, Object>> records = studentMapper.churnedList(
                filterStoreId, keyword, sortColumn, sortDir, size, offset);

        if (records != null && !records.isEmpty()) {
            for (Map<String, Object> r : records) {
                Object lastLessonAt = r.get("lastLessonAt");
                if (lastLessonAt != null) {
                    java.time.LocalDateTime ldt;
                    if (lastLessonAt instanceof java.time.LocalDateTime l) {
                        ldt = l;
                    } else if (lastLessonAt instanceof String s) {
                        ldt = java.time.LocalDateTime.parse(s.replace(" ", "T"));
                    } else {
                        ldt = null;
                    }
                    if (ldt != null) {
                        long days = java.time.Duration.between(ldt, java.time.LocalDateTime.now()).toDays();
                        r.put("daysSinceLastLesson", days);
                    }
                }
            }
        }

        Page<Map<String, Object>> page = new Page<>(current, size);
        page.setTotal(total);
        page.setRecords(records != null ? records : List.of());
        return R.ok(page);
    }
}
