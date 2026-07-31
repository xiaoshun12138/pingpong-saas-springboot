package com.pingpong.controller;

import com.pingpong.common.R;
import com.pingpong.dto.DashboardVO;
import com.pingpong.dto.RankingItem;
import com.pingpong.service.IDashboardService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 仪表盘控制器
 * 提供首页看板所需的各类统计数据接口。
 * 数据权限：店长只能看到自己门店的统计数据，老板可以看全部或指定门店。
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    /** 仪表盘 Service，包含各类统计逻辑 */
    @Autowired
    private IDashboardService dashboardService;

    /**
     * 仪表盘总览数据
     * 返回门店数、员工数、学员数、本月消课、本月订单、本月退款、活跃订单等核心指标。
     * 店长自动只看自己门店的数据，老板默认看全部，可通过 storeId 参数切换门店视角。
     *
     * @param storeId 门店ID（可选，仅boss可用，null=全部门店）
     * @param request HTTP 请求，从中获取当前登录用户的角色和门店ID
     * @return 总览统计数据
     */
    @GetMapping("/overview")
    public R<DashboardVO> overview(
            @RequestParam(required = false) Long storeId,
            HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        Long myStoreId = (Long) request.getAttribute("storeId");
        Long filterStoreId = "boss".equals(role) ? storeId : myStoreId;
        return R.ok(dashboardService.overview(filterStoreId));
    }

    /**
     * 门店业绩对比（本月）
     * 返回各门店的销售额、订单数、消课总课时，用于横向对比。
     * 店长只能看到自己门店的数据，老板看到全部门店。
     *
     * @param request HTTP 请求，从中获取当前登录用户的角色和门店ID
     * @return 各门店业绩列表
     */
    @GetMapping("/store-performance")
    public R<List<Map<String, Object>>> storePerformance(HttpServletRequest request,
                                                          @RequestParam(required = false) Long storeId) {
        String role = (String) request.getAttribute("role");
        Long myStoreId = (Long) request.getAttribute("storeId");
        Long filterStoreId = "boss".equals(role) ? storeId : myStoreId;
        return R.ok(dashboardService.storePerformance(filterStoreId));
    }

    /**
     * 各门店本月课消明细
     */
    @GetMapping("/store-consumption")
    public R<List<Map<String, Object>>> storeConsumption(HttpServletRequest request,
                                                          @RequestParam(required = false) Long storeId) {
        String role = (String) request.getAttribute("role");
        Long myStoreId = (Long) request.getAttribute("storeId");
        Long filterStoreId = "boss".equals(role) ? storeId : myStoreId;
        return R.ok(dashboardService.storeConsumption(filterStoreId));
    }

    /**
     * 本月每日业绩走势（按天聚合订单金额）
     */
    @GetMapping("/daily-trend")
    public R<List<Map<String, Object>>> dailyTrend(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        Long storeId = (Long) request.getAttribute("storeId");
        Long filterStoreId = "boss".equals(role) ? null : storeId;
        return R.ok(dashboardService.dailyTrend(filterStoreId));
    }

    /**
     * 教练消课排名（本月，按消课总课时降序）
     *
     * @param storeId 门店筛选（仅 boss 可自由指定，店长强制自己门店）
     * @param topN    取前 N 名，默认前10名
     * @param request HTTP 请求，从中获取当前登录用户的角色和门店ID
     * @return 排名列表
     */
    @GetMapping("/coach-lesson-ranking")
    public R<List<RankingItem>> coachLessonRanking(
            @RequestParam(required = false) Long storeId,
            @RequestParam(defaultValue = "10") int topN,
            HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        Long myStoreId = (Long) request.getAttribute("storeId");
        Long filterStoreId = "boss".equals(role) ? storeId : myStoreId;
        return R.ok(dashboardService.coachLessonRanking(filterStoreId, topN));
    }

    /**
     * 教练业绩排名（本月，按带单总金额降序）
     *
     * @param storeId 门店筛选（仅 boss 可自由指定，店长强制自己门店）
     * @param topN    取前 N 名，默认前10名
     * @param request HTTP 请求，从中获取当前登录用户的角色和门店ID
     * @return 排名列表
     */
    @GetMapping("/coach-sales-ranking")
    public R<List<RankingItem>> coachSalesRanking(
            @RequestParam(required = false) Long storeId,
            @RequestParam(defaultValue = "10") int topN,
            HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        Long myStoreId = (Long) request.getAttribute("storeId");
        Long filterStoreId = "boss".equals(role) ? storeId : myStoreId;
        return R.ok(dashboardService.coachSalesRanking(filterStoreId, topN));
    }

    /**
     * 销售业绩排名（本月，按签单总金额降序）
     *
     * @param storeId 门店筛选（仅 boss 可自由指定，店长强制自己门店）
     * @param topN    取前 N 名，默认前10名
     * @param request HTTP 请求，从中获取当前登录用户的角色和门店ID
     * @return 排名列表
     */
    @GetMapping("/sales-ranking")
    public R<List<RankingItem>> salesRanking(
            @RequestParam(required = false) Long storeId,
            @RequestParam(defaultValue = "10") int topN,
            HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        Long myStoreId = (Long) request.getAttribute("storeId");
        Long filterStoreId = "boss".equals(role) ? storeId : myStoreId;
        return R.ok(dashboardService.salesRanking(filterStoreId, topN));
    }
}
