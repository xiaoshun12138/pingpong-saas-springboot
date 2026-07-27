package com.pingpong.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pingpong.common.R;
import com.pingpong.entity.WeeklyTarget;
import com.pingpong.service.IWeeklyTargetService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 周度目标控制器
 * 提供周度目标的增删改查接口，用于设置和追踪门店或员工的周度业绩目标。
 * 数据权限：店长只能看到自己门店的目标，老板可以看全部或指定门店。
 */
@RestController
@RequestMapping("/api/weekly-targets")
public class WeeklyTargetController {

    /** 周度目标 Service */
    @Autowired
    private IWeeklyTargetService weeklyTargetService;

    /**
     * 分页查询周度目标列表
     * 按目标周倒序排列，自动根据登录角色做门店数据隔离。
     *
     * @param weeklyTarget 查询条件
     * @param current      页码，默认第1页
     * @param size         每页条数，默认10条
     * @param storeId      门店筛选（仅 boss 生效）
     * @param request      HTTP 请求，从中获取当前登录用户的角色和门店ID
     * @return 分页结果
     */
    @GetMapping
    public R<Page<WeeklyTarget>> list(WeeklyTarget weeklyTarget,
                                      @RequestParam(defaultValue = "1") Integer current,
                                      @RequestParam(defaultValue = "10") Integer size,
                                      @RequestParam(required = false) Long storeId,
                                      HttpServletRequest request) {
        // 数据权限判断
        String role = (String) request.getAttribute("role");
        Long myStoreId = (Long) request.getAttribute("storeId");
        Long filterStoreId = "boss".equals(role) ? storeId : myStoreId;

        LambdaQueryWrapper<WeeklyTarget> wrapper = new LambdaQueryWrapper<WeeklyTarget>()
                .eq(filterStoreId != null, WeeklyTarget::getStoreId, filterStoreId)
                .orderByDesc(WeeklyTarget::getTargetWeek);
        Page<WeeklyTarget> page = new Page<>(current, size);
        return R.ok(weeklyTargetService.page(page, wrapper));
    }

    /**
     * 根据ID查询周度目标详情
     *
     * @param id 目标ID
     * @return 目标信息
     */
    @GetMapping("/{id}")
    public R<WeeklyTarget> getById(@PathVariable Long id) {
        WeeklyTarget target = weeklyTargetService.getById(id);
        return target != null ? R.ok(target) : R.fail("目标不存在");
    }

    /**
     * 新增周度目标
     *
     * @param weeklyTarget 目标信息
     * @return 操作结果
     */
    @PostMapping
    public R<?> save(@RequestBody WeeklyTarget weeklyTarget) {
        boolean ok = weeklyTargetService.save(weeklyTarget);
        return ok ? R.ok() : R.fail("新增失败");
    }

    /**
     * 更新周度目标
     *
     * @param weeklyTarget 目标信息（需带ID）
     * @return 操作结果
     */
    @PutMapping
    public R<?> update(@RequestBody WeeklyTarget weeklyTarget) {
        boolean ok = weeklyTargetService.updateById(weeklyTarget);
        return ok ? R.ok() : R.fail("更新失败");
    }

    /**
     * 删除周度目标（逻辑删除）
     *
     * @param id 目标ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public R<?> delete(@PathVariable Long id) {
        boolean ok = weeklyTargetService.removeById(id);
        return ok ? R.ok() : R.fail("删除失败");
    }
}
