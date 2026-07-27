package com.pingpong.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pingpong.common.R;
import com.pingpong.entity.MonthlyTarget;
import com.pingpong.service.IMonthlyTargetService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 月度目标控制器
 * 提供月度目标的增删改查接口，用于设置和追踪门店或员工的月度业绩目标。
 * 数据权限：店长只能看到自己门店的目标，老板可以看全部或指定门店。
 */
@RestController
@RequestMapping("/api/monthly-targets")
public class MonthlyTargetController {

    /** 月度目标 Service */
    @Autowired
    private IMonthlyTargetService monthlyTargetService;

    /**
     * 分页查询月度目标列表
     * 按目标月份倒序排列，自动根据登录角色做门店数据隔离。
     *
     * @param monthlyTarget 查询条件
     * @param current       页码，默认第1页
     * @param size          每页条数，默认10条
     * @param storeId       门店筛选（仅 boss 生效）
     * @param request       HTTP 请求，从中获取当前登录用户的角色和门店ID
     * @return 分页结果
     */
    @GetMapping
    public R<Page<MonthlyTarget>> list(MonthlyTarget monthlyTarget,
                                      @RequestParam(defaultValue = "1") Integer current,
                                      @RequestParam(defaultValue = "10") Integer size,
                                      @RequestParam(required = false) Long storeId,
                                      HttpServletRequest request) {
        // 数据权限判断
        String role = (String) request.getAttribute("role");
        Long myStoreId = (Long) request.getAttribute("storeId");
        Long filterStoreId = "boss".equals(role) ? storeId : myStoreId;

        LambdaQueryWrapper<MonthlyTarget> wrapper = new LambdaQueryWrapper<MonthlyTarget>()
                .eq(filterStoreId != null, MonthlyTarget::getStoreId, filterStoreId)
                .orderByDesc(MonthlyTarget::getTargetMonth);
        Page<MonthlyTarget> page = new Page<>(current, size);
        return R.ok(monthlyTargetService.page(page, wrapper));
    }

    /**
     * 根据ID查询月度目标详情
     *
     * @param id 目标ID
     * @return 目标信息
     */
    @GetMapping("/{id}")
    public R<MonthlyTarget> getById(@PathVariable Long id) {
        MonthlyTarget target = monthlyTargetService.getById(id);
        return target != null ? R.ok(target) : R.fail("目标不存在");
    }

    /**
     * 新增月度目标
     *
     * @param monthlyTarget 目标信息
     * @return 操作结果
     */
    @PostMapping
    public R<?> save(@RequestBody MonthlyTarget monthlyTarget) {
        boolean ok = monthlyTargetService.save(monthlyTarget);
        return ok ? R.ok() : R.fail("新增失败");
    }

    /**
     * 更新月度目标
     *
     * @param monthlyTarget 目标信息（需带ID）
     * @return 操作结果
     */
    @PutMapping
    public R<?> update(@RequestBody MonthlyTarget monthlyTarget) {
        boolean ok = monthlyTargetService.updateById(monthlyTarget);
        return ok ? R.ok() : R.fail("更新失败");
    }

    /**
     * 删除月度目标（逻辑删除）
     *
     * @param id 目标ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public R<?> delete(@PathVariable Long id) {
        boolean ok = monthlyTargetService.removeById(id);
        return ok ? R.ok() : R.fail("删除失败");
    }
}
