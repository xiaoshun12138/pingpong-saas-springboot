package com.pingpong.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pingpong.common.R;
import com.pingpong.entity.CourseConsumption;
import com.pingpong.entity.Student;
import com.pingpong.mapper.StudentMapper;
import com.pingpong.service.ICourseConsumptionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 消课记录控制器
 * 核心接口 POST /api/course-consumptions 以事务方式完成消课全流程：
 * 插入消课记录 → 扣减订单剩余课时（乐观锁）→ 扣减学员总剩余课时。
 */
@RestController
@RequestMapping("/api/course-consumptions")
public class CourseConsumptionController {

    @Autowired
    private ICourseConsumptionService courseConsumptionService;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private com.pingpong.service.ILessonScheduleService lessonScheduleService;

    @Autowired
    private com.pingpong.service.ICourseOrderService courseOrderService;

    /**
     * 分页查询消课记录，支持按学员姓名搜索。
     */
    @GetMapping
    public R<Page<CourseConsumption>> list(CourseConsumption courseConsumption,
                                           @RequestParam(defaultValue = "1") Integer current,
                                           @RequestParam(defaultValue = "10") Integer size,
                                           @RequestParam(required = false) Long storeId,
                                           @RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) String startDate,
                                           @RequestParam(required = false) String endDate,
                                           HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        Long myStoreId = (Long) request.getAttribute("storeId");
        Long filterStoreId = "boss".equals(role) ? storeId : myStoreId;

        LambdaQueryWrapper<CourseConsumption> wrapper = new LambdaQueryWrapper<CourseConsumption>()
                .eq(filterStoreId != null, CourseConsumption::getStoreId, filterStoreId)
                .ge(startDate != null && !startDate.isBlank(), CourseConsumption::getRecordDate, startDate)
                .le(endDate != null && !endDate.isBlank(), CourseConsumption::getRecordDate, endDate)
                .orderByDesc(CourseConsumption::getId);

        if (keyword != null && !keyword.isBlank()) {
            List<Long> studentIds = studentMapper.selectList(
                    new LambdaQueryWrapper<Student>()
                            .select(Student::getId)
                            .like(Student::getName, keyword))
                    .stream().map(Student::getId).collect(Collectors.toList());
            if (!studentIds.isEmpty()) {
                wrapper.in(CourseConsumption::getStudentId, studentIds);
            } else {
                wrapper.eq(CourseConsumption::getStudentId, -1L);
            }
        }

        Page<CourseConsumption> page = new Page<>(current, size);
        Page<CourseConsumption> result = courseConsumptionService.page(page, wrapper);
        result.getRecords().forEach(courseConsumptionService::fillNames);
        return R.ok(result);
    }

    @GetMapping("/{id}")
    public R<CourseConsumption> getById(@PathVariable Long id) {
        CourseConsumption consumption = courseConsumptionService.getById(id);
        return consumption != null ? R.ok(consumption) : R.fail("消课记录不存在");
    }

    @PostMapping
    public R<?> save(@Valid @RequestBody CourseConsumption courseConsumption) {
        // 消课记录页面新增消课 → 同时创建排课记录（统一入口，保证排课页面可见）
        if (courseConsumption.getScheduleId() == null) {
            // 从订单获取 storeId（前端表单不传）
            Long storeId = courseConsumption.getStoreId();
            if (storeId == null && courseConsumption.getCourseOrderId() != null) {
                com.pingpong.entity.CourseOrder order = courseOrderService.getById(courseConsumption.getCourseOrderId());
                if (order != null) {
                    storeId = order.getStoreId();
                }
            }
            // 没有 scheduleId 说明是从消课记录页面直接新增的，创建对应排课记录
            com.pingpong.entity.LessonSchedule schedule = new com.pingpong.entity.LessonSchedule();
            schedule.setStoreId(storeId);
            schedule.setCoachId(courseConsumption.getCoachId());
            schedule.setStudentId(courseConsumption.getStudentId());
            schedule.setCourseOrderId(courseConsumption.getCourseOrderId());
            schedule.setScheduleDate(courseConsumption.getRecordDate());
            schedule.setStartTime(courseConsumption.getRecordTime() != null
                    ? courseConsumption.getRecordTime() : java.time.LocalTime.of(9, 0));
            schedule.setEndTime(courseConsumption.getRecordTime() != null
                    ? courseConsumption.getRecordTime().plusHours(1).withMinute(30)
                    : java.time.LocalTime.of(10, 30));
            schedule.setLessonContent(courseConsumption.getRemark() != null ? courseConsumption.getRemark() : "消课记录新增");
            schedule.setStatus("scheduled");
            lessonScheduleService.saveAndConsume(schedule);
        } else {
            courseConsumptionService.consumeLesson(courseConsumption);
        }
        return R.ok("消课成功");
    }

    @PutMapping
    public R<?> update(@RequestBody CourseConsumption courseConsumption) {
        if (courseConsumption.getId() == null) {
            return R.fail("消课记录ID不能为空");
        }
        CourseConsumption existing = courseConsumptionService.getById(courseConsumption.getId());
        if (existing == null) {
            return R.fail("消课记录不存在");
        }
        existing.setRemark(courseConsumption.getRemark());
        existing.setRecordDate(courseConsumption.getRecordDate());
        existing.setRecordTime(courseConsumption.getRecordTime());
        boolean ok = courseConsumptionService.updateById(existing);
        return ok ? R.ok() : R.fail("更新失败");
    }

    @DeleteMapping("/{id}")
    public R<?> delete(@PathVariable Long id) {
        return R.fail("消课记录不允许删除，如需撤销消课请走退款流程");
    }
}
