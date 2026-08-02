package com.pingpong.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pingpong.entity.*;
import com.pingpong.mapper.CourseConsumptionMapper;
import com.pingpong.mapper.LessonScheduleMapper;
import com.pingpong.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 排课记录 Service 实现类
 * 排课+消课、取消排课+归还课时均在同一事务中完成。
 */
@Slf4j
@Service
public class LessonScheduleServiceImpl extends ServiceImpl<LessonScheduleMapper, LessonSchedule> implements ILessonScheduleService {

    @Autowired
    private ICourseConsumptionService courseConsumptionService;
    @Autowired
    private ICourseOrderService courseOrderService;
    @Autowired
    private IStudentService studentService;
    @Autowired
    private CourseConsumptionMapper courseConsumptionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAndConsume(LessonSchedule schedule) {
        schedule.setStatus("scheduled");
        try {
            boolean ok = this.save(schedule);
            if (!ok) {
                throw new RuntimeException("排课保存失败");
            }
        } catch (org.springframework.dao.DuplicateKeyException e) {
            throw new RuntimeException("该学员此时段已有排课，请勿重复排课");
        }

        // 排课成功 → 自动消课1课时（同一事务，失败回滚排课记录）
        if (schedule.getCourseOrderId() != null && schedule.getStudentId() != null
                && schedule.getCoachId() != null) {
            CourseConsumption consumption = new CourseConsumption();
            consumption.setStudentId(schedule.getStudentId());
            consumption.setCoachId(schedule.getCoachId());
            consumption.setCourseOrderId(schedule.getCourseOrderId());
            consumption.setScheduleId(schedule.getId());
            consumption.setStoreId(schedule.getStoreId());
            consumption.setLessons(1);
            consumption.setRecordDate(schedule.getScheduleDate());
            consumption.setRecordTime(schedule.getStartTime() != null
                    ? schedule.getStartTime() : LocalTime.of(9, 0));
            consumption.setRemark(schedule.getLessonContent() != null
                    ? schedule.getLessonContent() : "排课消课");
            courseConsumptionService.consumeLesson(consumption);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelAndRefund(Long scheduleId) {
        LessonSchedule schedule = this.getById(scheduleId);
        if (schedule == null) {
            throw new RuntimeException("排课记录不存在");
        }

        // 1. 物理删除排课记录（逻辑删除会与唯一索引冲突：同组合第二次取消时旧记录 deleted=1 仍在）
        boolean ok = this.getBaseMapper().physicalDeleteById(scheduleId) > 0;
        if (!ok) {
            throw new RuntimeException("取消排课失败");
        }

        // 2. 物理删除对应的消课记录（取消排课 = 撤销消课，消课记录无审计必要）
        int deletedConsumptions = courseConsumptionMapper.physicalDeleteByScheduleId(scheduleId);
        log.info("取消排课：排课ID={}, 删除消课记录{}条", scheduleId, deletedConsumptions);

        // 3. 归还已扣课时（只有已消课的排课才需要归还）
        if (schedule.getCourseOrderId() != null && schedule.getStudentId() != null && deletedConsumptions > 0) {
            CourseOrder order = courseOrderService.getById(schedule.getCourseOrderId());
            if (order != null && "active".equals(order.getStatus())) {
                // 订单剩余课时 +1，已消课时 -1
                CourseOrder updateOrder = new CourseOrder();
                updateOrder.setId(order.getId());
                updateOrder.setRemainingLessons(order.getRemainingLessons() + 1);
                updateOrder.setConsumedLessons(Math.max(0, order.getConsumedLessons() - 1));
                updateOrder.setVersion(order.getVersion());
                boolean orderOk = courseOrderService.updateById(updateOrder);
                if (!orderOk) {
                    throw new RuntimeException("订单课时归还失败，可能并发冲突，请重试");
                }

                // 学员总剩余课时 +1
                Student student = studentService.getById(schedule.getStudentId());
                if (student != null) {
                    Student updateStudent = new Student();
                    updateStudent.setId(student.getId());
                    updateStudent.setTotalRemainingLessons(student.getTotalRemainingLessons() + 1);
                    updateStudent.setVersion(student.getVersion());
                    studentService.updateById(updateStudent);
                }

                log.info("取消排课并归还课时：排课ID={}, 订单={}, 学员={}",
                        scheduleId, schedule.getCourseOrderId(), schedule.getStudentId());
            }
        }
    }
}
