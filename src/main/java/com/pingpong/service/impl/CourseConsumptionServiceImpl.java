package com.pingpong.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pingpong.entity.CourseConsumption;
import com.pingpong.entity.CourseOrder;
import com.pingpong.entity.Staff;
import com.pingpong.entity.Student;
import com.pingpong.mapper.CourseConsumptionMapper;
import com.pingpong.service.ICourseConsumptionService;
import com.pingpong.service.ICourseOrderService;
import com.pingpong.service.IStaffService;
import com.pingpong.service.IStudentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 消课记录 Service 实现类
 * 核心业务方法 consumeLesson 以事务方式完成消课全流程，
 * 同时扣减订单剩余课时和学员总剩余课时（均带乐观锁），保证数据一致性。
 */
@Slf4j
@Service
public class CourseConsumptionServiceImpl extends ServiceImpl<CourseConsumptionMapper, CourseConsumption> implements ICourseConsumptionService {

    /** 课包订单 Service，用于扣减订单课时 */
    @Autowired
    private ICourseOrderService courseOrderService;

    /** 学员 Service，用于扣减学员总剩余课时 */
    @Autowired
    private IStudentService studentService;

    /** 员工 Service，用于校验教练是否属于目标门店 */
    @Autowired
    private IStaffService staffService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void consumeLesson(CourseConsumption consumption) {
        // ========== 1. 参数基础校验 ==========
        if (consumption == null) {
            throw new IllegalArgumentException("消课记录不能为空");
        }
        if (consumption.getCourseOrderId() == null) {
            throw new IllegalArgumentException("关联订单ID不能为空");
        }
        if (consumption.getStudentId() == null) {
            throw new IllegalArgumentException("学员ID不能为空");
        }
        if (consumption.getCoachId() == null) {
            throw new IllegalArgumentException("教练ID不能为空");
        }
        if (consumption.getLessons() == null || consumption.getLessons() <= 0) {
            throw new IllegalArgumentException("消课课时必须大于0");
        }

        // ========== 2. 查询订单并校验状态与剩余课时 ==========
        CourseOrder order = courseOrderService.getById(consumption.getCourseOrderId());
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!"active".equals(order.getStatus())) {
            throw new RuntimeException("订单状态异常，当前状态：" + order.getStatus() + "，只有 active 状态的订单可以消课");
        }
        if (order.getRemainingLessons() < consumption.getLessons()) {
            throw new RuntimeException("订单剩余课时不足，当前剩余：" + order.getRemainingLessons() + "，需要消课：" + consumption.getLessons());
        }

        // ========== 3. 校验消课的学员与订单记录的学员一致 ==========
        if (!order.getStudentId().equals(consumption.getStudentId())) {
            throw new RuntimeException("学员ID与订单中的学员不一致，订单学员=" + order.getStudentId() + "，传入学员=" + consumption.getStudentId());
        }

        // ========== 4. 校验教练属于目标门店 ==========
        Staff coach = staffService.getById(consumption.getCoachId());
        if (coach == null) {
            throw new RuntimeException("教练不存在，教练ID=" + consumption.getCoachId());
        }
        if (!"coach".equals(coach.getRole()) && !"shop_owner".equals(coach.getRole())) {
            throw new RuntimeException("该员工不是教练或店长，角色=" + coach.getRole());
        }
        if (!order.getStoreId().equals(coach.getStoreId())) {
            throw new RuntimeException("教练不属于目标门店，订单门店=" + order.getStoreId() + "，教练门店=" + coach.getStoreId());
        }

        // ========== 5. 查询学员并校验状态与总剩余课时 ==========
        Student student = studentService.getById(consumption.getStudentId());
        if (student == null) {
            throw new RuntimeException("学员不存在");
        }
        // 停课学员不允许消课
        if (student.getStatus() == null || student.getStatus() == 0) {
            throw new RuntimeException("学员已停课，无法消课。请先在学员管理中恢复在读状态");
        }
        if (student.getTotalRemainingLessons() < consumption.getLessons()) {
            throw new RuntimeException("学员总剩余课时不足，当前剩余：" + student.getTotalRemainingLessons() + "，需要消课：" + consumption.getLessons());
        }

        // ========== 6. 自动填写门店ID（从订单冗余） ==========
        consumption.setStoreId(order.getStoreId());

        // ========== 7. 插入消课记录 ==========
        boolean saveOk = this.save(consumption);
        if (!saveOk) {
            throw new RuntimeException("消课记录保存失败");
        }

        // ========== 8. 扣减订单剩余课时（乐观锁防并发超扣） ==========
        // MyBatis-Plus 乐观锁插件自动生成 SQL：
        // UPDATE course_order SET remaining_lessons=?, consumed_lessons=?, version=version+1
        // WHERE id=? AND version=?
        CourseOrder updateOrder = new CourseOrder();
        updateOrder.setId(order.getId());
        updateOrder.setRemainingLessons(order.getRemainingLessons() - consumption.getLessons());
        updateOrder.setConsumedLessons(order.getConsumedLessons() + consumption.getLessons());
        updateOrder.setVersion(order.getVersion());

        boolean updateOk = courseOrderService.updateById(updateOrder);
        if (!updateOk) {
            throw new RuntimeException("订单课时扣减失败，可能并发冲突，请重试");
        }

        // ========== 9. 扣减学员总剩余课时（乐观锁防并发扣错） ==========
        Student updateStudent = new Student();
        updateStudent.setId(student.getId());
        updateStudent.setTotalRemainingLessons(student.getTotalRemainingLessons() - consumption.getLessons());
        updateStudent.setVersion(student.getVersion());
        updateStudent.setLastLessonAt(LocalDateTime.now());

        boolean studentUpdateOk = studentService.updateById(updateStudent);
        if (!studentUpdateOk) {
            throw new RuntimeException("学员课时扣减失败，可能并发冲突，请重试");
        }

        log.info("消课成功：学员={}, 订单={}, 消课课时={}, 订单剩余={}, 学员剩余={}",
                consumption.getStudentId(),
                consumption.getCourseOrderId(),
                consumption.getLessons(),
                order.getRemainingLessons() - consumption.getLessons(),
                student.getTotalRemainingLessons() - consumption.getLessons());
    }

    @Override
    public void fillNames(CourseConsumption consumption) {
        if (consumption == null) return;
        if (consumption.getStudentId() != null) {
            Student s = studentService.getById(consumption.getStudentId());
            consumption.setStudentName(s != null ? s.getName() : "-");
        }
        if (consumption.getCoachId() != null) {
            Staff c = staffService.getById(consumption.getCoachId());
            consumption.setCoachName(c != null ? c.getName() : "-");
        }
        if (consumption.getCourseOrderId() != null) {
            CourseOrder o = courseOrderService.getById(consumption.getCourseOrderId());
            consumption.setOrderNo(o != null ? o.getOrderNo() : "-");
        }
        if (consumption.getStoreId() != null) {
            // 门店名称填充在Controller层做，避免循环依赖
            consumption.setStoreName("-");
        }
    }
}
