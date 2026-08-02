package com.pingpong.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pingpong.entity.CourseOrder;
import com.pingpong.entity.RefundLog;
import com.pingpong.entity.Student;
import com.pingpong.mapper.RefundLogMapper;
import com.pingpong.service.ICourseOrderService;
import com.pingpong.service.IRefundLogService;
import com.pingpong.service.IStaffService;
import com.pingpong.service.IStudentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 退款记录 Service 实现类
 * 核心业务方法 refund 以事务方式完成退款全流程，
 * 退款金额由后端根据「实付金额 × (退款课时 / 总课时)」自动计算，不信任前端传入的金额。
 * 同时更新订单状态为已退款、扣减学员总剩余课时（均带乐观锁），保证数据一致性。
 */
@Slf4j
@Service
public class RefundLogServiceImpl extends ServiceImpl<RefundLogMapper, RefundLog> implements IRefundLogService {

    /** 课包订单 Service，用于更新订单状态和课时 */
    @Autowired
    private ICourseOrderService courseOrderService;

    /** 学员 Service，用于扣减学员总剩余课时 */
    @Autowired
    private IStudentService studentService;

    /** 员工 Service，用于查询操作人姓名 */
    @Autowired
    private IStaffService staffService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refund(RefundLog refundLog) {
        // ========== 1. 参数基础校验 ==========
        if (refundLog == null) {
            throw new IllegalArgumentException("退款记录不能为空");
        }
        if (refundLog.getCourseOrderId() == null) {
            throw new IllegalArgumentException("关联订单ID不能为空");
        }
        if (refundLog.getStudentId() == null) {
            throw new IllegalArgumentException("学员ID不能为空");
        }
        // 注意：refundAmount 不再由前端传入，后端自动计算；但如果前端传了则忽略并覆盖
        if (refundLog.getRefundLessons() == null || refundLog.getRefundLessons() <= 0) {
            throw new IllegalArgumentException("退回课时数必须大于0");
        }
        if (refundLog.getOperatorId() == null) {
            throw new IllegalArgumentException("操作人ID不能为空");
        }

        // ========== 2. 查询订单并校验状态 ==========
        CourseOrder order = courseOrderService.getById(refundLog.getCourseOrderId());
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!"active".equals(order.getStatus())) {
            throw new RuntimeException("订单状态异常，当前状态：" + order.getStatus() + "，只有 active 状态的订单可以退款");
        }
        if (!order.getRemainingLessons().equals(refundLog.getRefundLessons())) {
            throw new RuntimeException("退款课时数必须等于订单剩余课时，订单剩余：" + order.getRemainingLessons() + "，申请退款：" + refundLog.getRefundLessons());
        }

        // ========== 3. 校验退款学员与订单中的学员一致 ==========
        if (!order.getStudentId().equals(refundLog.getStudentId())) {
            throw new RuntimeException("学员ID与订单中的学员不一致，订单学员=" + order.getStudentId() + "，传入学员=" + refundLog.getStudentId());
        }

        // ========== 4. 查询学员并校验总剩余课时 ==========
        Student student = studentService.getById(refundLog.getStudentId());
        if (student == null) {
            throw new RuntimeException("学员不存在");
        }
        if (student.getTotalRemainingLessons() < refundLog.getRefundLessons()) {
            throw new RuntimeException("学员总剩余课时不足，当前剩余：" + student.getTotalRemainingLessons() + "，申请退款：" + refundLog.getRefundLessons());
        }

        // ========== 5. 后端自动计算退款金额（按比例，精度 2 位，四舍五入） ==========
        // 退款金额 = 实付金额 × (退款课时数 / 订单总课时)
        // 使用 BigDecimal 精确计算，避免浮点误差
        BigDecimal calculatedAmount = order.getPaidAmount()
                .multiply(BigDecimal.valueOf(refundLog.getRefundLessons()))
                .divide(BigDecimal.valueOf(order.getTotalLessons()), 2, RoundingMode.HALF_UP);
        refundLog.setRefundAmount(calculatedAmount);

        // ========== 6. 从订单冗余门店ID，解决退款表无 storeId 的历史问题 ==========
        refundLog.setStoreId(order.getStoreId());

        // ========== 7. 插入退款日志（只读记录，只增不删改） ==========
        boolean saveOk = this.save(refundLog);
        if (!saveOk) {
            throw new RuntimeException("退款日志保存失败");
        }

        // ========== 8. 更新订单状态为 refunded，清零剩余课时（乐观锁防并发） ==========
        CourseOrder updateOrder = new CourseOrder();
        updateOrder.setId(order.getId());
        updateOrder.setStatus("refunded");
        updateOrder.setRemainingLessons(0);
        updateOrder.setVersion(order.getVersion());

        boolean updateOk = courseOrderService.updateById(updateOrder);
        if (!updateOk) {
            throw new RuntimeException("订单状态更新失败，可能并发冲突，请重试");
        }

        // ========== 9. 扣减学员总剩余课时（乐观锁防并发扣错） ==========
        Student updateStudent = new Student();
        updateStudent.setId(student.getId());
        updateStudent.setTotalRemainingLessons(student.getTotalRemainingLessons() - refundLog.getRefundLessons());
        updateStudent.setVersion(student.getVersion());

        boolean studentUpdateOk = studentService.updateById(updateStudent);
        if (!studentUpdateOk) {
            throw new RuntimeException("学员课时扣减失败，可能并发冲突，请重试");
        }

        log.info("退款成功：学员={}, 订单={}, 退回课时={}, 计算退款金额={}, 学员剩余={}",
                refundLog.getStudentId(),
                refundLog.getCourseOrderId(),
                refundLog.getRefundLessons(),
                calculatedAmount,
                student.getTotalRemainingLessons() - refundLog.getRefundLessons());
    }

    @Override
    public void fillNames(RefundLog refundLog) {
        if (refundLog == null) return;
        if (refundLog.getStudentId() != null) {
            Student s = studentService.getById(refundLog.getStudentId());
            refundLog.setStudentName(s != null ? s.getName() : "-");
        }
        if (refundLog.getCourseOrderId() != null) {
            CourseOrder o = courseOrderService.getById(refundLog.getCourseOrderId());
            refundLog.setOrderNo(o != null ? o.getOrderNo() : "-");
        }
        if (refundLog.getOperatorId() != null) {
            com.pingpong.entity.Staff op = staffService.getById(refundLog.getOperatorId());
            refundLog.setOperatorName(op != null ? op.getName() : "-");
        }
    }
}
