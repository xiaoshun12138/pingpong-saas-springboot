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

        long storeCount = storeMapper.selectCount(null);
        long staffCount = storeId == null
                ? staffMapper.selectCount(null)
                : staffMapper.selectCount(new LambdaQueryWrapper<Staff>().eq(Staff::getStoreId, storeId));
        long studentCount = storeId == null
                ? studentMapper.selectCount(null)
                : studentMapper.selectCount(new LambdaQueryWrapper<Student>().eq(Student::getStoreId, storeId));

        // 聚合SQL替代 selectList 全量拖内存
        LocalDateTime now2 = LocalDateTime.now();
        Long monthLessons = consumptionMapper.sumLessonsInRange(monthStart, now2, storeId);
        Long monthConsumptionCount = null;
        // 消课笔数还是需要查
        LambdaQueryWrapper<CourseConsumption> countQw = new LambdaQueryWrapper<CourseConsumption>()
                .ge(CourseConsumption::getCreatedAt, monthStart);
        if (storeId != null) countQw.eq(CourseConsumption::getStoreId, storeId);
        monthConsumptionCount = consumptionMapper.selectCount(countQw);

        BigDecimal monthOrderAmount = orderMapper.sumAmountInRange(monthStart, now2, storeId);
        Long monthOrderCount = orderMapper.countInRange(monthStart, now2, storeId);

        BigDecimal monthRefundAmount = refundLogMapper.sumAmountInRange(monthStart, now2, storeId);
        Long monthRefundCount = refundLogMapper.countInRange(monthStart, now2, storeId);

        LambdaQueryWrapper<CourseOrder> activeBase = new LambdaQueryWrapper<CourseOrder>()
                .eq(CourseOrder::getStatus, "active");
        if (storeId != null) activeBase.eq(CourseOrder::getStoreId, storeId);
        long activeOrderCount = orderMapper.selectCount(activeBase);

        return new DashboardVO(
                storeCount, staffCount, studentCount,
                monthConsumptionCount, monthLessons != null ? monthLessons : 0L,
                monthOrderCount, monthOrderAmount != null ? monthOrderAmount : BigDecimal.ZERO,
                monthRefundCount, monthRefundAmount != null ? monthRefundAmount : BigDecimal.ZERO,
                activeOrderCount
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
        for (Map<String, Object> row : consumptionRows) {
            long sid = ((Number) row.get("storeId")).longValue();
            if (row.get("lessons") != null) lessonsMap.put(sid, ((Number) row.get("lessons")).longValue());
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Store s : stores) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("storeName", s.getName());
            item.put("salesAmount", salesMap.getOrDefault(s.getId(), BigDecimal.ZERO));
            item.put("orderCount", orderCountMap.getOrDefault(s.getId(), 0L));
            item.put("lessonsConsumed", lessonsMap.getOrDefault(s.getId(), 0L));
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

        // 查询教练
        LambdaQueryWrapper<Staff> staffQw = new LambdaQueryWrapper<Staff>()
                .in(Staff::getRole, "coach", "shop_owner")
                .eq(Staff::getStatus, 1);
        if (storeId != null) staffQw.eq(Staff::getStoreId, storeId);
        List<Staff> coaches = staffMapper.selectList(staffQw);

        // 查询本月消课记录
        LambdaQueryWrapper<CourseConsumption> cw = new LambdaQueryWrapper<CourseConsumption>()
                .ge(CourseConsumption::getCreatedAt, monthStart);
        if (storeId != null) cw.eq(CourseConsumption::getStoreId, storeId);
        List<CourseConsumption> consumptions = consumptionMapper.selectList(cw);

        // 批量查询关联订单（避免 N+1）
        Set<Long> orderIds = consumptions.stream()
                .map(CourseConsumption::getCourseOrderId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, CourseOrder> orderMap = new HashMap<>();
        if (!orderIds.isEmpty()) {
            orderMapper.selectBatchIds(orderIds).forEach(o -> orderMap.put(o.getId(), o));
        }

        // 聚合：课时、次数、金额
        Map<Long, Long> lessonsMap = new HashMap<>();
        Map<Long, Long> countMap = new HashMap<>();
        Map<Long, BigDecimal> amountMap = new HashMap<>();
        for (CourseConsumption c : consumptions) {
            lessonsMap.merge(c.getCoachId(), (long) c.getLessons(), Long::sum);
            countMap.merge(c.getCoachId(), 1L, Long::sum);
            CourseOrder order = orderMap.get(c.getCourseOrderId());
            if (order != null && order.getTotalLessons() != null && order.getTotalLessons() > 0) {
                BigDecimal unitPrice = order.getPaidAmount()
                        .divide(BigDecimal.valueOf(order.getTotalLessons()), 2, RoundingMode.HALF_UP);
                amountMap.merge(c.getCoachId(), unitPrice.multiply(BigDecimal.valueOf(c.getLessons())), BigDecimal::add);
            }
        }

        Map<Long, String> storeNameMap = storeMapper.selectList(null).stream()
                .collect(Collectors.toMap(Store::getId, Store::getName));

        List<RankingItem> list = new ArrayList<>();
        for (Staff coach : coaches) {
            long l = lessonsMap.getOrDefault(coach.getId(), 0L);
            BigDecimal amt = amountMap.getOrDefault(coach.getId(), BigDecimal.ZERO);
            RankingItem item = new RankingItem(
                    coach.getId(), coach.getName(),
                    storeNameMap.getOrDefault(coach.getStoreId(), "未知"),
                    BigDecimal.valueOf(l),
                    countMap.getOrDefault(coach.getId(), 0L),
                    amt, 0, null
            );
            list.add(item);
        }

        // 先按默认规则排好，算出固定排名序号
        list.sort((a, b) -> {
            int cmp = b.getValue().compareTo(a.getValue());
            if (cmp != 0) return cmp;
            return b.getLessonAmount().compareTo(a.getLessonAmount());
        });
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setRank(i + 1);
        }

        // 再按用户选择的排序方向重新排
        if ("amount".equals(sortBy)) {
            list.sort((a, b) -> asc
                    ? a.getLessonAmount().compareTo(b.getLessonAmount())
                    : b.getLessonAmount().compareTo(a.getLessonAmount()));
        } else {
            list.sort((a, b) -> asc
                    ? a.getValue().compareTo(b.getValue())
                    : b.getValue().compareTo(a.getValue()));
        }

        return list.stream().limit(topN > 0 ? topN : list.size()).collect(Collectors.toList());
    }

    // ==================== 业绩排名 ====================

    @Override
    public List<RankingItem> coachSalesRanking(Long storeId, int topN) {
        LocalDateTime monthStart = LocalDateTime.now()
                .with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0);

        LambdaQueryWrapper<Staff> staffQw = new LambdaQueryWrapper<Staff>()
                .in(Staff::getRole, "coach", "shop_owner")
                .eq(Staff::getStatus, 1);
        if (storeId != null) staffQw.eq(Staff::getStoreId, storeId);
        List<Staff> coaches = staffMapper.selectList(staffQw);

        LambdaQueryWrapper<CourseOrder> ow = new LambdaQueryWrapper<CourseOrder>()
                .ge(CourseOrder::getCreatedAt, monthStart);
        if (storeId != null) ow.eq(CourseOrder::getStoreId, storeId);
        List<CourseOrder> orders = orderMapper.selectList(ow);

        Map<Long, BigDecimal> amountMap = new HashMap<>();
        Map<Long, Long> countMap = new HashMap<>();
        for (CourseOrder o : orders) {
            if (o.getCoachId() != null) {
                amountMap.merge(o.getCoachId(), o.getPaidAmount(), BigDecimal::add);
                countMap.merge(o.getCoachId(), 1L, Long::sum);
            }
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
        List<RankingItem> top = list.stream().limit(topN > 0 ? topN : list.size()).collect(Collectors.toList());
        for (int i = 0; i < top.size(); i++) {
            top.get(i).setRank(i + 1);
        }
        return top;
    }

    @Override
    public List<RankingItem> salesRanking(Long storeId, int topN) {
        LocalDateTime monthStart = LocalDateTime.now()
                .with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0);

        LambdaQueryWrapper<Staff> staffQw = new LambdaQueryWrapper<Staff>()
                .eq(Staff::getRole, "sales")
                .eq(Staff::getStatus, 1);
        if (storeId != null) staffQw.eq(Staff::getStoreId, storeId);
        List<Staff> salesPeople = staffMapper.selectList(staffQw);

        LambdaQueryWrapper<CourseOrder> ow = new LambdaQueryWrapper<CourseOrder>()
                .ge(CourseOrder::getCreatedAt, monthStart);
        if (storeId != null) ow.eq(CourseOrder::getStoreId, storeId);
        List<CourseOrder> orders = orderMapper.selectList(ow);

        Map<Long, BigDecimal> amountMap = new HashMap<>();
        Map<Long, Long> countMap = new HashMap<>();
        for (CourseOrder o : orders) {
            if (o.getSalesId() != null) {
                amountMap.merge(o.getSalesId(), o.getPaidAmount(), BigDecimal::add);
                countMap.merge(o.getSalesId(), 1L, Long::sum);
            }
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
        List<RankingItem> top = list.stream().limit(topN > 0 ? topN : list.size()).collect(Collectors.toList());
        for (int i = 0; i < top.size(); i++) {
            top.get(i).setRank(i + 1);
        }
        return top;
    }

    @Override
    public List<RankingItem> performanceRanking(String type, Long storeId, String sortBy, boolean asc, int topN) {
        LocalDateTime monthStart = LocalDateTime.now()
                .with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0);

        Map<Long, String> storeNameMap = storeMapper.selectList(null).stream()
                .collect(Collectors.toMap(Store::getId, Store::getName));

        LambdaQueryWrapper<CourseOrder> ow = new LambdaQueryWrapper<CourseOrder>()
                .ge(CourseOrder::getCreatedAt, monthStart);
        if (storeId != null) ow.eq(CourseOrder::getStoreId, storeId);
        List<CourseOrder> orders = orderMapper.selectList(ow);

        List<RankingItem> list = new ArrayList<>();

        if (!"sales".equals(type)) {
            LambdaQueryWrapper<Staff> coachQw = new LambdaQueryWrapper<Staff>()
                    .in(Staff::getRole, "coach", "shop_owner")
                    .eq(Staff::getStatus, 1);
            if (storeId != null) coachQw.eq(Staff::getStoreId, storeId);
            List<Staff> coaches = staffMapper.selectList(coachQw);

            Map<Long, BigDecimal> coachAmount = new HashMap<>();
            Map<Long, Long> coachCount = new HashMap<>();
            for (CourseOrder o : orders) {
                if (o.getCoachId() != null) {
                    coachAmount.merge(o.getCoachId(), o.getPaidAmount(), BigDecimal::add);
                    coachCount.merge(o.getCoachId(), 1L, Long::sum);
                }
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

            Map<Long, BigDecimal> salesAmount = new HashMap<>();
            Map<Long, Long> salesCount = new HashMap<>();
            for (CourseOrder o : orders) {
                if (o.getSalesId() != null) {
                    salesAmount.merge(o.getSalesId(), o.getPaidAmount(), BigDecimal::add);
                    salesCount.merge(o.getSalesId(), 1L, Long::sum);
                }
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

        list.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        for (int i = 0; i < list.size(); i++) list.get(i).setRank(i + 1);

        if ("count".equals(sortBy)) {
            list.sort((a, b) -> asc
                    ? Long.compare(a.getCount(), b.getCount())
                    : Long.compare(b.getCount(), a.getCount()));
        } else {
            list.sort((a, b) -> asc
                    ? a.getValue().compareTo(b.getValue())
                    : b.getValue().compareTo(a.getValue()));
        }

        return list.stream().limit(topN > 0 ? topN : list.size()).collect(Collectors.toList());
    }

    // ==================== 门店排名 ====================

    @Override
    public List<StoreRankingItem> storeLessonRanking(Long storeId) {
        LocalDateTime monthStart = LocalDateTime.now()
                .with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0);

        List<Store> stores = storeId == null
                ? storeMapper.selectList(null)
                : storeMapper.selectList(new LambdaQueryWrapper<Store>().eq(Store::getId, storeId));

        // 聚合SQL
        LambdaQueryWrapper<CourseConsumption> cw = new LambdaQueryWrapper<CourseConsumption>()
                .ge(CourseConsumption::getCreatedAt, monthStart);
        if (storeId != null) cw.eq(CourseConsumption::getStoreId, storeId);
        List<CourseConsumption> consumptions = consumptionMapper.selectList(cw);

        Set<Long> orderIds = consumptions.stream()
                .map(CourseConsumption::getCourseOrderId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, CourseOrder> orderMap = new HashMap<>();
        if (!orderIds.isEmpty()) {
            orderMapper.selectBatchIds(orderIds).forEach(o -> orderMap.put(o.getId(), o));
        }

        Map<Long, Long> lessonsMap = new HashMap<>();
        Map<Long, Long> countMap = new HashMap<>();
        Map<Long, BigDecimal> amountMap = new HashMap<>();
        for (CourseConsumption c : consumptions) {
            lessonsMap.merge(c.getStoreId(), (long) c.getLessons(), Long::sum);
            countMap.merge(c.getStoreId(), 1L, Long::sum);
            CourseOrder order = orderMap.get(c.getCourseOrderId());
            if (order != null && order.getTotalLessons() != null && order.getTotalLessons() > 0) {
                BigDecimal unitPrice = order.getPaidAmount()
                        .divide(BigDecimal.valueOf(order.getTotalLessons()), 2, RoundingMode.HALF_UP);
                amountMap.merge(c.getStoreId(), unitPrice.multiply(BigDecimal.valueOf(c.getLessons())), BigDecimal::add);
            }
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

        List<Store> stores = storeId == null
                ? storeMapper.selectList(null)
                : storeMapper.selectList(new LambdaQueryWrapper<Store>().eq(Store::getId, storeId));

        LambdaQueryWrapper<CourseOrder> ow = new LambdaQueryWrapper<CourseOrder>()
                .ge(CourseOrder::getCreatedAt, monthStart);
        if (storeId != null) ow.eq(CourseOrder::getStoreId, storeId);
        List<CourseOrder> orders = orderMapper.selectList(ow);

        LambdaQueryWrapper<CourseConsumption> cw = new LambdaQueryWrapper<CourseConsumption>()
                .ge(CourseConsumption::getCreatedAt, monthStart);
        if (storeId != null) cw.eq(CourseConsumption::getStoreId, storeId);
        List<CourseConsumption> consumptions = consumptionMapper.selectList(cw);

        Map<Long, BigDecimal> salesMap = new HashMap<>();
        Map<Long, Long> orderCountMap = new HashMap<>();
        for (CourseOrder o : orders) {
            salesMap.merge(o.getStoreId(), o.getPaidAmount(), BigDecimal::add);
            orderCountMap.merge(o.getStoreId(), 1L, Long::sum);
        }
        Map<Long, Long> lessonsMap = new HashMap<>();
        for (CourseConsumption c : consumptions) {
            lessonsMap.merge(c.getStoreId(), (long) c.getLessons(), Long::sum);
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
