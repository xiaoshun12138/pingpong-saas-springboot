package com.pingpong.controller;

import com.pingpong.common.R;
import com.pingpong.dto.TargetDashboardVO;
import com.pingpong.service.ITargetDashboardService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 目标仪表盘控制器
 * 提供业绩目标和课消目标的综合统计看板数据，包括年度/月度/周度目标完成率、走势图表等。
 * 数据权限：店长只能看到自己门店的数据，老板可以看全部或指定门店。
 */
@RestController
@RequestMapping("/api/target-dashboard")
public class TargetDashboardController {

    @Autowired
    private ITargetDashboardService targetDashboardService;

    /**
     * 业绩目标仪表盘数据
     *
     * @param year    年份，默认当前年
     * @param month   月份，默认当前月
     * @param storeId 门店筛选（仅 boss 生效）
     * @param request HTTP 请求
     * @return 业绩目标综合数据
     */
    @GetMapping("/sales")
    public R<TargetDashboardVO> salesTargetDashboard(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Long storeId,
            HttpServletRequest request) {
        if (year == null) year = LocalDate.now().getYear();
        if (month == null) month = LocalDate.now().getMonthValue();
        String role = (String) request.getAttribute("role");
        Long myStoreId = (Long) request.getAttribute("storeId");
        Long filterStoreId = "boss".equals(role) ? storeId : myStoreId;

        return R.ok(targetDashboardService.salesTargetDashboard(year, month, filterStoreId));
    }

    /**
     * 课消目标仪表盘数据
     *
     * @param year    年份，默认当前年
     * @param month   月份，默认当前月
     * @param storeId 门店筛选（仅 boss 生效）
     * @param request HTTP 请求
     * @return 课消目标综合数据
     */
    @GetMapping("/consumption")
    public R<TargetDashboardVO> consumptionTargetDashboard(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Long storeId,
            HttpServletRequest request) {
        if (year == null) year = LocalDate.now().getYear();
        if (month == null) month = LocalDate.now().getMonthValue();
        String role = (String) request.getAttribute("role");
        Long myStoreId = (Long) request.getAttribute("storeId");
        Long filterStoreId = "boss".equals(role) ? storeId : myStoreId;

        return R.ok(targetDashboardService.consumptionTargetDashboard(year, month, filterStoreId));
    }
}
