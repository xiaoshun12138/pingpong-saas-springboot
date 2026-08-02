package com.pingpong.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pingpong.common.R;
import com.pingpong.entity.LessonSchedule;
import com.pingpong.service.ILessonScheduleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 排课记录控制器
 * 提供排课的增删改查接口，支持按教练、按日期查询排课表。
 * 数据权限：店长只能看到自己门店的排课，老板可以看全部。
 */
@RestController
@RequestMapping("/api/schedules")
public class LessonScheduleController {

    /** 排课记录 Service */
    @Autowired
    private ILessonScheduleService lessonScheduleService;

    /**
     * 分页查询排课列表
     * 支持按教练、按日期筛选，结果按上课时间升序排列。
     * 自动根据登录角色做门店数据隔离。
     *
     * @param coachId 教练ID（可选）
     * @param date    排课日期（可选，格式 yyyy-MM-dd）
     * @param current 页码，默认第1页
     * @param size    每页条数，默认10条
     * @param request HTTP 请求，从中获取当前登录用户的角色和门店ID
     * @return 分页结果
     */
    @GetMapping
    public R<Page<LessonSchedule>> list(
            @RequestParam(required = false) Long coachId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        // 数据权限判断：老板可看全部，店长只看自己门店
        Long myStoreId = (Long) request.getAttribute("storeId");
        String role = (String) request.getAttribute("role");
        Long filterStoreId = "boss".equals(role) ? null : myStoreId;

        LambdaQueryWrapper<LessonSchedule> wrapper = new LambdaQueryWrapper<LessonSchedule>()
                .eq(filterStoreId != null, LessonSchedule::getStoreId, filterStoreId)
                .eq(coachId != null, LessonSchedule::getCoachId, coachId)
                .eq(date != null, LessonSchedule::getScheduleDate, date)
                .orderByAsc(LessonSchedule::getStartTime);

        Page<LessonSchedule> page = new Page<>(current, size);
        return R.ok(lessonScheduleService.page(page, wrapper));
    }

    /**
     * 根据ID查询排课详情
     *
     * @param id 排课ID
     * @return 排课信息
     */
    @GetMapping("/{id}")
    public R<LessonSchedule> getById(@PathVariable Long id) {
        LessonSchedule s = lessonScheduleService.getById(id);
        return s != null ? R.ok(s) : R.fail("排课记录不存在");
    }

    /**
     * 新增排课（同时自动消课1课时，同一事务）
     */
    @PostMapping
    public R<?> save(@Valid @RequestBody LessonSchedule schedule) {
        try {
            lessonScheduleService.saveAndConsume(schedule);
        } catch (Exception e) {
            return R.fail(e.getMessage());
        }
        return R.ok("排课并消课成功");
    }

    /**
     * 更新排课信息
     *
     * @param schedule 排课信息（需带ID）
     * @return 操作结果
     */
    @PutMapping
    public R<?> update(@Valid @RequestBody LessonSchedule schedule) {
        boolean ok = lessonScheduleService.updateById(schedule);
        return ok ? R.ok("更新成功") : R.fail("更新失败");
    }

    /**
     * 取消排课（逻辑删除 + 归还已扣课时，同一事务）
     */
    @DeleteMapping("/{id}")
    public R<?> delete(@PathVariable Long id) {
        try {
            lessonScheduleService.cancelAndRefund(id);
        } catch (Exception e) {
            return R.fail(e.getMessage());
        }
        return R.ok("已取消排课并归还课时");
    }
}
