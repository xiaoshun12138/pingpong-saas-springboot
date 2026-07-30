package com.pingpong.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pingpong.dto.TargetDashboardVO;
import com.pingpong.entity.*;
import com.pingpong.mapper.*;
import com.pingpong.service.ITargetDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 目标仪表盘 Service 实现类（性能优化版）
 * 
 * 优化策略：用 GROUP BY 聚合查询替代循环中的逐条 SQL。
 * 原来：100+ 条 SQL → 现在：约 10 条 SQL
 */
@Service
public class TargetDashboardServiceImpl implements ITargetDashboardService {

    @Autowired
    private MonthlyTargetMapper monthlyTargetMapper;
    @Autowired
    private WeeklyTargetMapper weeklyTargetMapper;
    @Autowired
    private CourseOrderMapper orderMapper;
    @Autowired
    private CourseConsumptionMapper consumptionMapper;
    @Autowired
    private StoreMapper storeMapper;

    @Override
    public TargetDashboardVO salesTargetDashboard(Integer year, Integer month, Long storeId) {
        LocalDateTime yearStart = LocalDateTime.of(year, 1, 1, 0, 0, 0);
        LocalDateTime yearEnd = LocalDateTime.of(year, 12, 31, 23, 59, 59);
        // 根据 year+month 计算目标月份
        LocalDate targetMonth = LocalDate.of(year, month, 1);
        LocalDateTime monthStart = targetMonth.atTime(0, 0, 0);
        LocalDateTime monthEnd = targetMonth.with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59);
        // 周目标取目标月内第一个周一所在周，但当前数据才有意义，这里用当前时间
        LocalDate today = LocalDate.now();
        LocalDateTime weekStart = today.with(java.time.DayOfWeek.MONDAY).atTime(0, 0, 0);
        LocalDateTime now2 = LocalDateTime.now();

        // ===== 目标数据（目标表数据量小，直接查） =====
        BigDecimal yearTarget = sumMonthlyTarget("sales", LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 1), storeId);
        BigDecimal monthTarget = sumMonthlyTargetByMonth("sales", targetMonth, storeId);
        BigDecimal weekTarget = sumWeeklyTargetByWeek("sales", today.with(java.time.DayOfWeek.MONDAY), storeId);

        // ===== 实际数据（用聚合SQL，一条替代N条） =====
        BigDecimal yearActual = orderMapper.sumAmountInRange(yearStart, yearEnd, storeId);
        BigDecimal monthActual = orderMapper.sumAmountInRange(monthStart, monthEnd, storeId);
        BigDecimal weekActual = orderMapper.sumAmountInRange(weekStart, now2, storeId);

        // ===== 各月走势（GROUP BY 一次查出12个月） =====
        List<Map<String, Object>> monthRows = orderMapper.sumByMonth(yearStart, yearEnd, storeId);
        List<BigDecimal> monthlyTrend = buildMonthlyArray(monthRows, "total");

        // ===== 各门店对比（目标月） =====
        List<Map<String, Object>> storeActuals = orderMapper.sumByStore(monthStart, monthEnd, storeId);
        List<Map<String, Object>> storeMonthlyTargets = sumMonthlyTargetByStore("sales", targetMonth, storeId);

        List<Store> stores = getStores(storeId);
        List<TargetDashboardVO.StoreComparisonItem> storeComparison = new ArrayList<>();
        for (Store s : stores) {
            BigDecimal target = findStoreVal(storeMonthlyTargets, s.getId(), "targetAmount");
            BigDecimal actual = findStoreVal(storeActuals, s.getId(), "total");
            storeComparison.add(new TargetDashboardVO.StoreComparisonItem(s.getId(), s.getName(), target, actual));
        }

        // ===== 各门店每月明细（GROUP BY store_id + month 一次查出） =====
        List<Map<String, Object>> storeMonthRows = orderMapper.sumByStoreAndMonth(yearStart, yearEnd, storeId);
        List<TargetDashboardVO.StoreMonthlyItem> storeMonthlyData = buildStoreMonthlyData(stores, storeMonthRows, "total");

        return new TargetDashboardVO(yearTarget, yearActual, monthTarget, monthActual,
                weekTarget, weekActual, monthlyTrend, storeComparison, storeMonthlyData);
    }

    @Override
    public TargetDashboardVO consumptionTargetDashboard(Integer year, Integer month, Long storeId) {
        LocalDateTime yearStart = LocalDateTime.of(year, 1, 1, 0, 0, 0);
        LocalDateTime yearEnd = LocalDateTime.of(year, 12, 31, 23, 59, 59);
        LocalDate targetMonth = LocalDate.of(year, month, 1);
        LocalDateTime monthStart = targetMonth.atTime(0, 0, 0);
        LocalDateTime monthEnd = targetMonth.with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59);
        LocalDate today = LocalDate.now();
        LocalDateTime weekStart = today.with(java.time.DayOfWeek.MONDAY).atTime(0, 0, 0);
        LocalDateTime now2 = LocalDateTime.now();

        // ===== 目标数据（金额） =====
        BigDecimal yearTarget = sumMonthlyTarget("consumption", LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 1), storeId);
        BigDecimal monthTarget = sumMonthlyTargetByMonth("consumption", targetMonth, storeId);
        BigDecimal weekTarget = sumWeeklyTargetByWeek("consumption", today.with(java.time.DayOfWeek.MONDAY), storeId);

        // ===== 实际消课金额（JOIN order 按单价折算） =====
        BigDecimal yearActual = consumptionMapper.sumAmountInRange(yearStart, yearEnd, storeId);
        BigDecimal monthActual = consumptionMapper.sumAmountInRange(monthStart, monthEnd, storeId);
        BigDecimal weekActual = consumptionMapper.sumAmountInRange(weekStart, now2, storeId);

        // ===== 各月走势（金额） =====
        List<Map<String, Object>> monthRows = consumptionMapper.sumAmountByMonth(yearStart, yearEnd, storeId);
        List<BigDecimal> monthlyTrend = buildMonthlyArray(monthRows, "amount");

        // ===== 各门店对比（目标月金额） =====
        List<Map<String, Object>> storeActuals = consumptionMapper.sumAmountByStore(monthStart, monthEnd, storeId);
        List<Map<String, Object>> storeMonthlyTargets = sumMonthlyTargetByStore("consumption", targetMonth, storeId);

        List<Store> stores = getStores(storeId);
        List<TargetDashboardVO.StoreComparisonItem> storeComparison = new ArrayList<>();
        for (Store s : stores) {
            BigDecimal target = findStoreVal(storeMonthlyTargets, s.getId(), "targetAmount");
            BigDecimal actual = findStoreVal(storeActuals, s.getId(), "amount");
            storeComparison.add(new TargetDashboardVO.StoreComparisonItem(s.getId(), s.getName(), target, actual));
        }

        // ===== 各门店每月明细（金额） =====
        List<Map<String, Object>> storeMonthRows = consumptionMapper.sumAmountByStoreAndMonth(yearStart, yearEnd, storeId);
        List<TargetDashboardVO.StoreMonthlyItem> storeMonthlyData = buildStoreMonthlyData(stores, storeMonthRows, "amount");

        return new TargetDashboardVO(yearTarget, yearActual, monthTarget, monthActual,
                weekTarget, weekActual, monthlyTrend, storeComparison, storeMonthlyData);
    }

    // ==================== 辅助方法 ====================

    private List<Store> getStores(Long storeId) {
        if (storeId == null) return storeMapper.selectList(null);
        return storeMapper.selectList(new LambdaQueryWrapper<Store>().eq(Store::getId, storeId));
    }

    /** 把 [{m:1, total:100}, {m:3, total:300}] 变成 [100, 0, 300, 0, 0, ...] */
    private List<BigDecimal> buildMonthlyArray(List<Map<String, Object>> rows, String key) {
        Map<Integer, BigDecimal> map = new HashMap<>();
        for (Map<String, Object> row : rows) {
            int m = ((Number) row.get("m")).intValue();
            Object val = row.get(key);
            map.put(m, val == null ? BigDecimal.ZERO : new BigDecimal(val.toString()));
        }
        List<BigDecimal> result = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            result.add(map.getOrDefault(i, BigDecimal.ZERO));
        }
        return result;
    }

    /** 组装门店每月明细 */
    private List<TargetDashboardVO.StoreMonthlyItem> buildStoreMonthlyData(
            List<Store> stores, List<Map<String, Object>> rows, String key) {
        // key -> storeId -> {月份 -> 值}
        Map<Long, Map<Integer, BigDecimal>> storeMonthMap = new HashMap<>();
        for (Map<String, Object> row : rows) {
            long sid = ((Number) row.get("storeId")).longValue();
            int m = ((Number) row.get("m")).intValue();
            Object val = row.get(key);
            BigDecimal v = val == null ? BigDecimal.ZERO : new BigDecimal(val.toString());
            storeMonthMap.computeIfAbsent(sid, k -> new HashMap<>()).put(m, v);
        }

        List<TargetDashboardVO.StoreMonthlyItem> result = new ArrayList<>();
        for (Store s : stores) {
            Map<Integer, BigDecimal> monthData = new LinkedHashMap<>();
            BigDecimal total = BigDecimal.ZERO;
            Map<Integer, BigDecimal> sm = storeMonthMap.getOrDefault(s.getId(), Collections.emptyMap());
            for (int m = 1; m <= 12; m++) {
                BigDecimal v = sm.getOrDefault(m, BigDecimal.ZERO);
                monthData.put(m, v);
                total = total.add(v);
            }
            result.add(new TargetDashboardVO.StoreMonthlyItem(s.getId(), s.getName(), monthData, total));
        }
        return result;
    }

    private BigDecimal findStoreVal(List<Map<String, Object>> rows, long storeId, String key) {
        return rows.stream()
                .filter(r -> ((Number) r.get("storeId")).longValue() == storeId)
                .findFirst()
                .map(r -> {
                    Object v = r.get(key);
                    return v == null ? BigDecimal.ZERO : new BigDecimal(v.toString());
                })
                .orElse(BigDecimal.ZERO);
    }

    private long findStoreLong(List<Map<String, Object>> rows, long storeId, String key) {
        return rows.stream()
                .filter(r -> ((Number) r.get("storeId")).longValue() == storeId)
                .findFirst()
                .map(r -> {
                    Object v = r.get(key);
                    return v == null ? 0L : ((Number) v).longValue();
                })
                .orElse(0L);
    }

    // 月度目标金额聚合
    private BigDecimal sumMonthlyTarget(String type, LocalDate from, LocalDate to, Long storeId) {
        return monthlyTargetMapper.selectList(
                new LambdaQueryWrapper<MonthlyTarget>()
                        .eq(MonthlyTarget::getTargetType, type)
                        .isNull(MonthlyTarget::getStaffId)
                        .ge(MonthlyTarget::getTargetMonth, from)
                        .le(MonthlyTarget::getTargetMonth, to)
                        .eq(storeId != null, MonthlyTarget::getStoreId, storeId)
        ).stream().map(MonthlyTarget::getTargetAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumMonthlyTargetByMonth(String type, LocalDate month, Long storeId) {
        return monthlyTargetMapper.selectList(
                new LambdaQueryWrapper<MonthlyTarget>()
                        .eq(MonthlyTarget::getTargetType, type)
                        .isNull(MonthlyTarget::getStaffId)
                        .eq(MonthlyTarget::getTargetMonth, month)
                        .eq(storeId != null, MonthlyTarget::getStoreId, storeId)
        ).stream().map(MonthlyTarget::getTargetAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // 月度目标课时聚合
    private BigDecimal sumMonthlyTargetCount(String type, LocalDate from, LocalDate to, Long storeId) {
        return monthlyTargetMapper.selectList(
                new LambdaQueryWrapper<MonthlyTarget>()
                        .eq(MonthlyTarget::getTargetType, type)
                        .isNull(MonthlyTarget::getStaffId)
                        .ge(MonthlyTarget::getTargetMonth, from)
                        .le(MonthlyTarget::getTargetMonth, to)
                        .eq(storeId != null, MonthlyTarget::getStoreId, storeId)
        ).stream().map(MonthlyTarget::getTargetCount).filter(Objects::nonNull).map(BigDecimal::valueOf).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumMonthlyTargetCountByMonth(String type, LocalDate month, Long storeId) {
        return monthlyTargetMapper.selectList(
                new LambdaQueryWrapper<MonthlyTarget>()
                        .eq(MonthlyTarget::getTargetType, type)
                        .isNull(MonthlyTarget::getStaffId)
                        .eq(MonthlyTarget::getTargetMonth, month)
                        .eq(storeId != null, MonthlyTarget::getStoreId, storeId)
        ).stream().map(MonthlyTarget::getTargetCount).filter(Objects::nonNull).map(BigDecimal::valueOf).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // 月度目标按门店聚合
    private List<Map<String, Object>> sumMonthlyTargetByStore(String type, LocalDate month, Long storeId) {
        return monthlyTargetMapper.selectList(
                new LambdaQueryWrapper<MonthlyTarget>()
                        .eq(MonthlyTarget::getTargetType, type)
                        .isNull(MonthlyTarget::getStaffId)
                        .eq(MonthlyTarget::getTargetMonth, month)
                        .eq(storeId != null, MonthlyTarget::getStoreId, storeId)
        ).stream().map(mt -> {
            Map<String, Object> map = new HashMap<>();
            map.put("storeId", mt.getStoreId());
            map.put("targetAmount", mt.getTargetAmount());
            map.put("targetCount", mt.getTargetCount());
            return map;
        }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> sumMonthlyTargetCountByStore(String type, LocalDate month, Long storeId) {
        // 复用同一方法，返回值里 targetCount 字段已包含
        return sumMonthlyTargetByStore(type, month, storeId);
    }

    // 周度目标
    private BigDecimal sumWeeklyTargetByWeek(String type, LocalDate week, Long storeId) {
        return weeklyTargetMapper.selectList(
                new LambdaQueryWrapper<WeeklyTarget>()
                        .eq(WeeklyTarget::getTargetType, type)
                        .isNull(WeeklyTarget::getStaffId)
                        .eq(WeeklyTarget::getTargetWeek, week)
                        .eq(storeId != null, WeeklyTarget::getStoreId, storeId)
        ).stream().map(WeeklyTarget::getTargetAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumWeeklyTargetCountByWeek(String type, LocalDate week, Long storeId) {
        return weeklyTargetMapper.selectList(
                new LambdaQueryWrapper<WeeklyTarget>()
                        .eq(WeeklyTarget::getTargetType, type)
                        .isNull(WeeklyTarget::getStaffId)
                        .eq(WeeklyTarget::getTargetWeek, week)
                        .eq(storeId != null, WeeklyTarget::getStoreId, storeId)
        ).stream().map(WeeklyTarget::getTargetCount).filter(Objects::nonNull).map(BigDecimal::valueOf).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
