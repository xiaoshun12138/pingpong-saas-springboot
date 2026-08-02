package com.pingpong.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pingpong.common.R;
import com.pingpong.dto.RankingItem;
import com.pingpong.dto.StoreRankingItem;
import com.pingpong.service.IDashboardService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 排名数据控制器
 * 提供课消排名、业绩排名、门店排名的分页查询接口。
 * 数据权限：店长只能看到自己门店的数据，老板可以看全部或指定门店。
 */
@RestController
@RequestMapping("/api/ranking")
public class RankingController {

    @Autowired
    private IDashboardService dashboardService;

    /**
     * 教练课消排名（分页）
     * 支持按消课课时或消课金额排序，可切换升序/降序。
     */
    @GetMapping("/lesson")
    public R<Page<RankingItem>> lessonRanking(
            @RequestParam(required = false) Long storeId,
            @RequestParam(defaultValue = "lessons") String sortBy,
            @RequestParam(defaultValue = "false") Boolean asc,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        Long myStoreId = (Long) request.getAttribute("storeId");
        Long filterStoreId = "boss".equals(role) ? storeId : myStoreId;

        List<RankingItem> all = dashboardService.coachLessonRanking(filterStoreId, sortBy, asc, 0);
        if (keyword != null && !keyword.isBlank()) {
            all = all.stream().filter(r -> r.getStaffName() != null && r.getStaffName().contains(keyword)).collect(Collectors.toList());
        }
        return R.ok(pageList(all, current, size));
    }

    /**
     * 员工业绩排名（分页）
     * 支持按业绩金额或订单数排序，可切换升序/降序。
     */
    @GetMapping("/performance")
    public R<Page<RankingItem>> performanceRanking(
            @RequestParam(defaultValue = "all") String type,
            @RequestParam(required = false) Long storeId,
            @RequestParam(defaultValue = "amount") String sortBy,
            @RequestParam(defaultValue = "false") Boolean asc,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        Long myStoreId = (Long) request.getAttribute("storeId");
        Long filterStoreId = "boss".equals(role) ? storeId : myStoreId;

        List<RankingItem> all = dashboardService.performanceRanking(type, filterStoreId, sortBy, asc, 0);
        if (keyword != null && !keyword.isBlank()) {
            all = all.stream().filter(r -> r.getStaffName() != null && r.getStaffName().contains(keyword)).collect(Collectors.toList());
        }
        return R.ok(pageList(all, current, size));
    }

    /**
     * 门店消课排名（分页）
     */
    @GetMapping("/store-lesson")
    public R<Page<StoreRankingItem>> storeLessonRanking(
            @RequestParam(required = false) Long storeId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        Long myStoreId = (Long) request.getAttribute("storeId");
        Long filterStoreId = "boss".equals(role) ? storeId : myStoreId;

        List<StoreRankingItem> all = dashboardService.storeLessonRanking(filterStoreId);
        return R.ok(pageStoreList(all, current, size));
    }

    /**
     * 门店业绩排名（分页）
     */
    @GetMapping("/store-performance")
    public R<Page<StoreRankingItem>> storePerformanceRanking(
            @RequestParam(required = false) Long storeId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        Long myStoreId = (Long) request.getAttribute("storeId");
        Long filterStoreId = "boss".equals(role) ? storeId : myStoreId;

        List<StoreRankingItem> all = dashboardService.storePerformanceRanking(filterStoreId);
        return R.ok(pageStoreList(all, current, size));
    }

    private Page<RankingItem> pageList(List<RankingItem> list, int current, int size) {
        Page<RankingItem> page = new Page<>(current, size);
        page.setTotal(list.size());
        int from = (current - 1) * size;
        int to = Math.min(from + size, list.size());
        if (from < list.size()) {
            page.setRecords(list.subList(from, to));
        } else {
            page.setRecords(List.of());
        }
        return page;
    }

    private Page<StoreRankingItem> pageStoreList(List<StoreRankingItem> list, int current, int size) {
        Page<StoreRankingItem> page = new Page<>(current, size);
        page.setTotal(list.size());
        int from = (current - 1) * size;
        int to = Math.min(from + size, list.size());
        if (from < list.size()) {
            page.setRecords(list.subList(from, to));
        } else {
            page.setRecords(List.of());
        }
        return page;
    }
}
