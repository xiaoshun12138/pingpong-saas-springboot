package com.pingpong.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pingpong.dto.DashboardVO;
import com.pingpong.dto.RankingItem;
import com.pingpong.dto.StoreRankingItem;
import com.pingpong.entity.*;
import com.pingpong.mapper.*;
import com.pingpong.service.IDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 仪表盘 Service 实现类（性能优化版）
 * 用聚合 SQL 替代全表拖取内存计算
 */
@Service
public class DashboardServiceImpl implements IDashboardService {

    @Autowired
    private StoreMapper storeMapper;
    @Autowired
    private StaffMapper staffMapper;
    @Autowired
    private StudentMapper studentMapper;
    @Autowired
    private CourseConsumptionMapper consumptionMapper;
    @Autowired
    private CourseOrderMapper orderMapper;
    @Autowired
    private RefundLogMapper refundLogMapper;

    @Override
    public DashboardVO overview(Long storeId) {
        LocalDateTime monthStart = LocalDateTime.now()
                .with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime now2 = LocalDateTime.now();

        long storeCount = storeMapper.selectCount(null);
        long staffCount = storeId == null
                ? staffMapper.selectCount(null)
                : staffMapper.selectCount(new LambdaQueryWrapper<Staff>().eq(Staff::getStoreId, storeId));
        long studentCount = storeId == null
                ? studentMapper.selectCount(null)
                : studentMapper.selectCount(new LambdaQueryWrapper<Student>().eq(Student::getStoreId, storeId));

        // 活跃学员数（status=1 在读）
        LambdaQueryWrapper<Student> activeStudentQw = new LambdaQueryWrapper<Student>()
                .eq(Student::getStatus, 1);
        if (storeId != null) activeStudentQw.eq(Student::getStoreId, storeId);
        long activeStudentCount = studentMapper.selectCount(activeStudentQw);

        // 本月消课数据
        Long monthLessons = consumptionMapper.sumLessonsInRange(monthStart, now2, storeId);
        LambdaQueryWrapper<CourseConsumption> countQw = new LambdaQueryWrapper<CourseConsumption>()
                .ge(CourseConsumption::getCreatedAt, monthStart);
        if (storeId != null) countQw.eq(CourseConsumption::getStoreId, storeId);
        long monthConsumptionCount = consumptionMapper.selectCount(countQw);
        BigDecimal monthConsumptionAmount = consumptionMapper.sumAmountInRange(monthStart, now2, storeId);

        // 本月新报（type=new）
        Long monthNewCount = orderMapper.countByType(monthStart, now2, storeId, "new");
        BigDecimal monthNewAmount = orderMapper.sumAmountByType(monthStart, now2, storeId, "new");

        // 本月续费（type=renew）
        Long monthRenewCount = orderMapper.countByType(monthStart, now2, storeId, "renew");
        BigDecimal monthRenewAmount = orderMapper.sumAmountByType(monthStart, now2, storeId, "renew");

        // 本月退款
        BigDecimal monthRefundAmount = refundLogMapper.sumAmountInRange(monthStart, now2, storeId);
        Long monthRefundCount = refundLogMapper.countInRange(monthStart, now2, storeId);

        String storeName = "总部";
        if (storeId != null) {
            Store store = storeMapper.selectById(storeId);
            storeName = store != null ? store.getName() : "未知门店";
        }

        return new DashboardVO(
                storeName,
                storeCount, staffCount, studentCount, activeStudentCount,
                monthConsumptionCount, monthLessons != null ? monthLessons : 0L,
                monthConsumptionAmount != null ? monthConsumptionAmount : BigDecimal.ZERO,
                monthNewCount != null ? monthNewCount : 0L,
                monthNewAmount != null ? monthNewAmount : BigDecimal.ZERO,
                monthRenewCount != null ? monthRenewCount : 0L,
                monthRenewAmount != null ? monthRenewAmount : BigDecimal.ZERO,
                monthRefundCount, monthRefundAmount != null ? monthRefundAmount : BigDecimal.ZERO
        );
    }

    @Override
    public List<Map<String, Object>> storePerformance(Long storeId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = now.with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime monthEnd = now.with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59);

        List<Store> stores = storeId == null
                ? storeMapper.selectList(null)
                : storeMapper.selectList(new LambdaQueryWrapper<Store>().eq(Store::getId, storeId));

        // 聚合SQL
        List<Map<String, Object>> salesRows = orderMapper.sumByStore(monthStart, monthEnd, storeId);
        List<Map<String, Object>> consumptionRows = consumptionMapper.sumByStore(monthStart, monthEnd, storeId);

        Map<Long, BigDecimal> salesMap = new HashMap<>();
        Map<Long, Long> orderCountMap = new HashMap<>();
        for (Map<String, Object> row : salesRows) {
            long sid = ((Number) row.get("storeId")).longValue();
            salesMap.put(sid, row.get("total") == null ? BigDecimal.ZERO : new BigDecimal(row.get("total").toString()));
            if (row.get("cnt") != null) orderCountMap.put(sid, ((Number) row.get("cnt")).longValue());
        }

        Map<Long, Long> lessonsMap = new HashMap<>();
        Map<Long, BigDecimal> consumptionAmountMap = new HashMap<>();
        for (Map<String, Object> row : consumptionRows) {
            long sid = ((Number) row.get("storeId")).longValue();
            if (row.get("lessons") != null) lessonsMap.put(sid, ((Number) row.get("lessons")).longValue());
            if (row.get("cnt") != null) consumptionAmountMap.put(sid, row.get("total") == null ? BigDecimal.ZERO : new BigDecimal(row.get("total").toString()));
        }

        // 消课金额（sumAmountByStore）
        List<Map<String, Object>> consumptionAmountRows = consumptionMapper.sumAmountByStore(monthStart, monthEnd, storeId);
        for (Map<String, Object> row : consumptionAmountRows) {
            long sid = ((Number) row.get("storeId")).longValue();
            if (row.get("amount") != null) {
                consumptionAmountMap.put(sid, new BigDecimal(row.get("amount").toString()));
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Store s : stores) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("storeName", s.getName());
            item.put("salesAmount", salesMap.getOrDefault(s.getId(), BigDecimal.ZERO));
            item.put("orderCount", orderCountMap.getOrDefault(s.getId(), 0L));
            item.put("lessonsConsumed", lessonsMap.getOrDefault(s.getId(), 0L));
            item.put("consumptionAmount", consumptionAmountMap.getOrDefault(s.getId(), BigDecimal.ZERO));
            result.add(item);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> storeConsumption(Long storeId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = now.with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime monthEnd = now.with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59);

        List<Store> stores = storeId == null
                ? storeMapper.selectList(null)
                : storeMapper.selectList(new LambdaQueryWrapper<Store>().eq(Store::getId, storeId));

        List<Map<String, Object>> consumptionRows = consumptionMapper.sumByStore(monthStart, monthEnd, storeId);
        List<Map<String, Object>> amountRows = consumptionMapper.sumAmountByStore(monthStart, monthEnd, storeId);

        Map<Long, Long> lessonsMap = new HashMap<>();
        Map<Long, Long> countMap = new HashMap<>();
        for (Map<String, Object> row : consumptionRows) {
            long sid = ((Number) row.get("storeId")).longValue();
            if (row.get("lessons") != null) lessonsMap.put(sid, ((Number) row.get("lessons")).longValue());
            if (row.get("cnt") != null) countMap.put(sid, ((Number) row.get("cnt")).longValue());
        }
        Map<Long, BigDecimal> amountMap = new HashMap<>();
        for (Map<String, Object> row : amountRows) {
            long sid = ((Number) row.get("storeId")).longValue();
            if (row.get("amount") != null) amountMap.put(sid, new BigDecimal(row.get("amount").toString()));
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Store s : stores) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("storeName", s.getName());
            item.put("consumptionCount", countMap.getOrDefault(s.getId(), 0L));
            item.put("lessonsConsumed", lessonsMap.getOrDefault(s.getId(), 0L));
            item.put("consumptionAmount", amountMap.getOrDefault(s.getId(), BigDecimal.ZERO));
            result.add(item);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> dailyTrend(Long storeId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = now.with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime monthEnd = now.with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59);

        // 查本月有数据的那些天
        List<Map<String, Object>> rows = orderMapper.sumByDay(monthStart, monthEnd, storeId);
        Map<Integer, BigDecimal> dayMap = new HashMap<>();
        for (Map<String, Object> row : rows) {
            int d = ((Number) row.get("d")).intValue();
            BigDecimal total = row.get("total") == null ? BigDecimal.ZERO : new BigDecimal(row.get("total").toString());
            dayMap.put(d, total);
        }

        // 本月所有天，无数据的补 0
        int daysInMonth = monthEnd.getDayOfMonth();
        List<Map<String, Object>> result = new ArrayList<>();
        for (int d = 1; d <= daysInMonth; d++) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("day", d);
            item.put("amount", dayMap.getOrDefault(d, BigDecimal.ZERO));
            result.add(item);
        }
        return result;
    }

    // ==================== 课消排名 ====================

    @Override
    public List<RankingItem> coachLessonRanking(Long storeId, int topN) {
        return coachLessonRanking(storeId, "lessons", false, topN);
    }

    @Override
    public List<RankingItem> coachLessonRanking(Long storeId, String sortBy, boolean asc, int topN) {
        LocalDateTime monthStart = LocalDateTime.now()
                .with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime monthEnd = LocalDateTime.now()
                .with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59);

        // 查询教练
        LambdaQueryWrapper<Staff> staffQw = new LambdaQueryWrapper<Staff>()
                .in(Staff::getRole, "coach", "shop_owner")
                .eq(Staff::getStatus, 1);
        if (storeId != null) staffQw.eq(Staff::getStoreId, storeId);
        List<Staff> coaches = staffMapper.selectList(staffQw);

        // SQL 聚合查询
        List<Map<String, Object>> rows = consumptionMapper.rankByCoach(monthStart, monthEnd, storeId);
        Map<Long, Long> lessonsMap = new HashMap<>();
        Map<Long, Long> countMap = new HashMap<>();
        Map<Long, BigDecimal> amountMap = new HashMap<>();
        for (Map<String, Object> row : rows) {
            long coachId = ((Number) row.get("coachId")).longValue();
            lessonsMap.put(coachId, row.get("lessons") != null ? ((Number) row.get("lessons")).longValue() : 0L);
            countMap.put(coachId, row.get("cnt") != null ? ((Number) row.get("cnt")).longValue() : 0L);
            amountMap.put(coachId, row.get("amount") != null ? new BigDecimal(row.get("amount").toString()) : BigDecimal.ZERO);
        }

        Map<Long, String> storeNameMap = storeMapper.selectList(null).stream()
                .collect(Collectors.toMap(Store::getId, Store::getName));

        List<RankingItem> list = new ArrayList<>();
        for (Staff coach : coaches) {
            long l = lessonsMap.getOrDefault(coach.getId(), 0L);
            BigDecimal amt = amountMap.getOrDefault(coach.getId(), BigDecimal.ZERO);
            list.add(new RankingItem(
                    coach.getId(), coach.getName(),
                    storeNameMap.getOrDefault(coach.getStoreId(), "未知"),
                    BigDecimal.valueOf(l),
                    countMap.getOrDefault(coach.getId(), 0L),
                    amt, 0, null
            ));
        }

        // 先排序再分配 rank
        if ("amount".equals(sortBy)) {
            list.sort((a, b) -> asc
                    ? a.getLessonAmount().compareTo(b.getLessonAmount())
                    : b.getLessonAmount().compareTo(a.getLessonAmount()));
        } else {
            list.sort((a, b) -> asc
                    ? a.getValue().compareTo(b.getValue())
                    : b.getValue().compareTo(a.getValue()));
        }
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setRank(i + 1);
        }

        return list.stream().limit(topN > 0 ? topN : list.size()).collect(Collectors.toList());
    }

    // ==================== 业绩排名 ====================

    @Override
    public List<RankingItem> coachSalesRanking(Long storeId, int topN) {
        LocalDateTime monthStart = LocalDateTime.now()
                .with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime monthEnd = LocalDateTime.now()
                .with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59);

        LambdaQueryWrapper<Staff> staffQw = new LambdaQueryWrapper<Staff>()
                .in(Staff::getRole, "coach", "shop_owner")
                .eq(Staff::getStatus, 1);
        if (storeId != null) staffQw.eq(Staff::getStoreId, storeId);
        List<Staff> coaches = staffMapper.selectList(staffQw);

        // SQL 聚合查询
        List<Map<String, Object>> rows = orderMapper.rankByCoach(monthStart, monthEnd, storeId);
        Map<Long, BigDecimal> amountMap = new HashMap<>();
        Map<Long, Long> countMap = new HashMap<>();
        for (Map<String, Object> row : rows) {
            long staffId = ((Number) row.get("staffId")).longValue();
            amountMap.put(staffId, row.get("amount") != null ? new BigDecimal(row.get("amount").toString()) : BigDecimal.ZERO);
            countMap.put(staffId, row.get("cnt") != null ? ((Number) row.get("cnt")).longValue() : 0L);
        }

        Map<Long, String> storeNameMap = storeMapper.selectList(null).stream()
                .collect(Collectors.toMap(Store::getId, Store::getName));

        List<RankingItem> list = new ArrayList<>();
        for (Staff coach : coaches) {
            BigDecimal amt = amountMap.getOrDefault(coach.getId(), BigDecimal.ZERO);
            list.add(new RankingItem(
                    coach.getId(), coach.getName(),
                    storeNameMap.getOrDefault(coach.getStoreId(), "未知"),
                    amt, countMap.getOrDefault(coach.getId(), 0L),
                    BigDecimal.ZERO, 0, "教练"
            ));
        }
        list.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        for (int i = 0; i < list.size(); i++) list.get(i).setRank(i + 1);
        return list.stream().limit(topN > 0 ? topN : list.size()).collect(Collectors.toList());
    }

    @Override
    public List<RankingItem> salesRanking(Long storeId, int topN) {
        LocalDateTime monthStart = LocalDateTime.now()
                .with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime monthEnd = LocalDateTime.now()
                .with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59);

        LambdaQueryWrapper<Staff> staffQw = new LambdaQueryWrapper<Staff>()
                .eq(Staff::getRole, "sales")
                .eq(Staff::getStatus, 1);
        if (storeId != null) staffQw.eq(Staff::getStoreId, storeId);
        List<Staff> salesPeople = staffMapper.selectList(staffQw);

        // SQL 聚合查询
        List<Map<String, Object>> rows = orderMapper.rankBySales(monthStart, monthEnd, storeId);
        Map<Long, BigDecimal> amountMap = new HashMap<>();
        Map<Long, Long> countMap = new HashMap<>();
        for (Map<String, Object> row : rows) {
            long staffId = ((Number) row.get("staffId")).longValue();
            amountMap.put(staffId, row.get("amount") != null ? new BigDecimal(row.get("amount").toString()) : BigDecimal.ZERO);
            countMap.put(staffId, row.get("cnt") != null ? ((Number) row.get("cnt")).longValue() : 0L);
        }

        Map<Long, String> storeNameMap = storeMapper.selectList(null).stream()
                .collect(Collectors.toMap(Store::getId, Store::getName));

        List<RankingItem> list = new ArrayList<>();
        for (Staff sales : salesPeople) {
            BigDecimal amt = amountMap.getOrDefault(sales.getId(), BigDecimal.ZERO);
            list.add(new RankingItem(
                    sales.getId(), sales.getName(),
                    storeNameMap.getOrDefault(sales.getStoreId(), "未知"),
                    amt, countMap.getOrDefault(sales.getId(), 0L),
                    BigDecimal.ZERO, 0, "销售"
            ));
        }
        list.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        for (int i = 0; i < list.size(); i++) list.get(i).setRank(i + 1);
        return list.stream().limit(topN > 0 ? topN : list.size()).collect(Collectors.toList());
    }

    @Override
    public List<RankingItem> performanceRanking(String type, Long storeId, String sortBy, boolean asc, int topN) {
        LocalDateTime monthStart = LocalDateTime.now()
                .with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime monthEnd = LocalDateTime.now()
                .with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59);

        Map<Long, String> storeNameMap = storeMapper.selectList(null).stream()
                .collect(Collectors.toMap(Store::getId, Store::getName));

        List<RankingItem> list = new ArrayList<>();

        if (!"sales".equals(type)) {
            LambdaQueryWrapper<Staff> coachQw = new LambdaQueryWrapper<Staff>()
                    .in(Staff::getRole, "coach", "shop_owner")
                    .eq(Staff::getStatus, 1);
            if (storeId != null) coachQw.eq(Staff::getStoreId, storeId);
            List<Staff> coaches = staffMapper.selectList(coachQw);

            // SQL 聚合查询
            List<Map<String, Object>> rows = orderMapper.rankByCoach(monthStart, monthEnd, storeId);
            Map<Long, BigDecimal> coachAmount = new HashMap<>();
            Map<Long, Long> coachCount = new HashMap<>();
            for (Map<String, Object> row : rows) {
                long staffId = ((Number) row.get("staffId")).longValue();
                coachAmount.put(staffId, row.get("amount") != null ? new BigDecimal(row.get("amount").toString()) : BigDecimal.ZERO);
                coachCount.put(staffId, row.get("cnt") != null ? ((Number) row.get("cnt")).longValue() : 0L);
            }
            for (Staff coach : coaches) {
                list.add(new RankingItem(
                        coach.getId(), coach.getName(),
                        storeNameMap.getOrDefault(coach.getStoreId(), "未知"),
                        coachAmount.getOrDefault(coach.getId(), BigDecimal.ZERO),
                        coachCount.getOrDefault(coach.getId(), 0L),
                        BigDecimal.ZERO, 0, "教练"
                ));
            }
        }

        if (!"coach".equals(type)) {
            LambdaQueryWrapper<Staff> salesQw = new LambdaQueryWrapper<Staff>()
                    .eq(Staff::getRole, "sales")
                    .eq(Staff::getStatus, 1);
            if (storeId != null) salesQw.eq(Staff::getStoreId, storeId);
            List<Staff> salesPeople = staffMapper.selectList(salesQw);

            // SQL 聚合查询
            List<Map<String, Object>> rows = orderMapper.rankBySales(monthStart, monthEnd, storeId);
            Map<Long, BigDecimal> salesAmount = new HashMap<>();
            Map<Long, Long> salesCount = new HashMap<>();
            for (Map<String, Object> row : rows) {
                long staffId = ((Number) row.get("staffId")).longValue();
                salesAmount.put(staffId, row.get("amount") != null ? new BigDecimal(row.get("amount").toString()) : BigDecimal.ZERO);
                salesCount.put(staffId, row.get("cnt") != null ? ((Number) row.get("cnt")).longValue() : 0L);
            }
            for (Staff sales : salesPeople) {
                list.add(new RankingItem(
                        sales.getId(), sales.getName(),
                        storeNameMap.getOrDefault(sales.getStoreId(), "未知"),
                        salesAmount.getOrDefault(sales.getId(), BigDecimal.ZERO),
                        salesCount.getOrDefault(sales.getId(), 0L),
                        BigDecimal.ZERO, 0, "销售"
                ));
            }
        }

        // 先排序再分配 rank
        if ("count".equals(sortBy)) {
            list.sort((a, b) -> asc
                    ? Long.compare(a.getCount(), b.getCount())
                    : Long.compare(b.getCount(), a.getCount()));
        } else {
            list.sort((a, b) -> asc
                    ? a.getValue().compareTo(b.getValue())
                    : b.getValue().compareTo(a.getValue()));
        }
        for (int i = 0; i < list.size(); i++) list.get(i).setRank(i + 1);

        return list.stream().limit(topN > 0 ? topN : list.size()).collect(Collectors.toList());
    }

    // ==================== 门店排名 ====================

    @Override
    public List<StoreRankingItem> storeLessonRanking(Long storeId) {
        LocalDateTime monthStart = LocalDateTime.now()
                .with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime monthEnd = LocalDateTime.now()
                .with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59);

        List<Store> stores = storeId == null
                ? storeMapper.selectList(null)
                : storeMapper.selectList(new LambdaQueryWrapper<Store>().eq(Store::getId, storeId));

        // SQL 聚合查询
        List<Map<String, Object>> rows = consumptionMapper.rankByStore(monthStart, monthEnd, storeId);
        Map<Long, Long> lessonsMap = new HashMap<>();
        Map<Long, Long> countMap = new HashMap<>();
        Map<Long, BigDecimal> amountMap = new HashMap<>();
        for (Map<String, Object> row : rows) {
            long sid = ((Number) row.get("storeId")).longValue();
            lessonsMap.put(sid, row.get("lessons") != null ? ((Number) row.get("lessons")).longValue() : 0L);
            countMap.put(sid, row.get("cnt") != null ? ((Number) row.get("cnt")).longValue() : 0L);
            amountMap.put(sid, row.get("amount") != null ? new BigDecimal(row.get("amount").toString()) : BigDecimal.ZERO);
        }

        List<StoreRankingItem> list = new ArrayList<>();
        for (Store s : stores) {
            list.add(new StoreRankingItem(
                    s.getId(), s.getName(),
                    BigDecimal.ZERO, 0L,
                    lessonsMap.getOrDefault(s.getId(), 0L),
                    amountMap.getOrDefault(s.getId(), BigDecimal.ZERO),
                    countMap.getOrDefault(s.getId(), 0L),
                    0
            ));
        }
        list.sort((a, b) -> b.getLessonAmount().compareTo(a.getLessonAmount()));
        for (int i = 0; i < list.size(); i++) list.get(i).setRank(i + 1);
        return list;
    }

    @Override
    public List<StoreRankingItem> storePerformanceRanking(Long storeId) {
        LocalDateTime monthStart = LocalDateTime.now()
                .with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime monthEnd = LocalDateTime.now()
                .with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59);

        List<Store> stores = storeId == null
                ? storeMapper.selectList(null)
                : storeMapper.selectList(new LambdaQueryWrapper<Store>().eq(Store::getId, storeId));

        // SQL 聚合查询
        List<Map<String, Object>> salesRows = orderMapper.rankByStore(monthStart, monthEnd, storeId);
        Map<Long, BigDecimal> salesMap = new HashMap<>();
        Map<Long, Long> orderCountMap = new HashMap<>();
        for (Map<String, Object> row : salesRows) {
            long sid = ((Number) row.get("storeId")).longValue();
            salesMap.put(sid, row.get("amount") != null ? new BigDecimal(row.get("amount").toString()) : BigDecimal.ZERO);
            orderCountMap.put(sid, row.get("cnt") != null ? ((Number) row.get("cnt")).longValue() : 0L);
        }

        List<Map<String, Object>> consumptionRows = consumptionMapper.rankByStore(monthStart, monthEnd, storeId);
        Map<Long, Long> lessonsMap = new HashMap<>();
        for (Map<String, Object> row : consumptionRows) {
            long sid = ((Number) row.get("storeId")).longValue();
            lessonsMap.put(sid, row.get("lessons") != null ? ((Number) row.get("lessons")).longValue() : 0L);
        }

        List<StoreRankingItem> list = new ArrayList<>();
        for (Store s : stores) {
            list.add(new StoreRankingItem(
                    s.getId(), s.getName(),
                    salesMap.getOrDefault(s.getId(), BigDecimal.ZERO),
                    orderCountMap.getOrDefault(s.getId(), 0L),
                    lessonsMap.getOrDefault(s.getId(), 0L),
                    BigDecimal.ZERO, 0L, 0
            ));
        }
        list.sort((a, b) -> b.getSalesAmount().compareTo(a.getSalesAmount()));
        for (int i = 0; i < list.size(); i++) list.get(i).setRank(i + 1);
        return list;
    }
}
