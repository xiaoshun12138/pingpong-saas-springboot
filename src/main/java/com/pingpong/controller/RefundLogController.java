package com.pingpong.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pingpong.common.R;
import com.pingpong.entity.RefundLog;
import com.pingpong.entity.Student;
import com.pingpong.mapper.StudentMapper;
import com.pingpong.service.IRefundLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 退款记录控制器
 * 提供退款记录的增删改查接口。
 * 核心接口 POST /api/refund-logs 会以事务方式完成退款全流程：
 * 插入退款日志 → 订单状态置为 refunded 并清零剩余课时 → 扣减学员总剩余课时。
 * 数据权限：boss 可查看全部退款记录，shop_owner 只能查看自己门店的退款记录。
 */
@RestController
@RequestMapping("/api/refund-logs")
public class RefundLogController {

    @Autowired
    private IRefundLogService refundLogService;

    @Autowired
    private StudentMapper studentMapper;

    /**
     * 分页查询退款记录列表，支持按学员姓名搜索。
     */
    @GetMapping
    public R<Page<RefundLog>> list(RefundLog refundLog,
                                   @RequestParam(defaultValue = "1") Integer current,
                                   @RequestParam(defaultValue = "10") Integer size,
                                   @RequestParam(required = false) String keyword,
                                   @RequestParam(required = false) String startDate,
                                   @RequestParam(required = false) String endDate,
                                   HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        Long myStoreId = (Long) request.getAttribute("storeId");
        Long filterStoreId = "boss".equals(role) ? null : myStoreId;

        LambdaQueryWrapper<RefundLog> wrapper = new LambdaQueryWrapper<RefundLog>()
                .eq(filterStoreId != null, RefundLog::getStoreId, filterStoreId)
                .ge(startDate != null && !startDate.isBlank(), RefundLog::getCreatedAt, startDate)
                .le(endDate != null && !endDate.isBlank(), RefundLog::getCreatedAt, endDate)
                .orderByDesc(RefundLog::getCreatedAt);

        if (keyword != null && !keyword.isBlank()) {
            List<Long> studentIds = studentMapper.selectList(
                    new LambdaQueryWrapper<Student>()
                            .select(Student::getId)
                            .like(Student::getName, keyword))
                    .stream().map(Student::getId).collect(Collectors.toList());
            if (!studentIds.isEmpty()) {
                wrapper.in(RefundLog::getStudentId, studentIds);
            } else {
                wrapper.eq(RefundLog::getStudentId, -1L);
            }
        }

        Page<RefundLog> page = new Page<>(current, size);
        Page<RefundLog> result = refundLogService.page(page, wrapper);
        result.getRecords().forEach(refundLogService::fillNames);
        return R.ok(result);
    }

    @GetMapping("/{id}")
    public R<RefundLog> getById(@PathVariable Long id) {
        RefundLog log = refundLogService.getById(id);
        return log != null ? R.ok(log) : R.fail("退款记录不存在");
    }

    @PostMapping
    public R<?> save(@Valid @RequestBody RefundLog refundLog, HttpServletRequest request) {
        // 操作人从 JWT token 取，不信任前端传入
        Long operatorId = (Long) request.getAttribute("staffId");
        refundLog.setOperatorId(operatorId);
        refundLogService.refund(refundLog);
        return R.ok("退款成功");
    }

    @PutMapping
    public R<?> update(@RequestBody RefundLog refundLog) {
        if (refundLog.getId() == null) {
            return R.fail("退款记录ID不能为空");
        }
        RefundLog existing = refundLogService.getById(refundLog.getId());
        if (existing == null) {
            return R.fail("退款记录不存在");
        }
        existing.setReason(refundLog.getReason());
        boolean ok = refundLogService.updateById(existing);
        return ok ? R.ok() : R.fail("更新失败");
    }

    @DeleteMapping("/{id}")
    public R<?> delete(@PathVariable Long id) {
        return R.fail("退款记录不允许删除，退款是财务凭证只增不删");
    }
}
