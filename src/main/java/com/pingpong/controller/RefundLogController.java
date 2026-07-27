package com.pingpong.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pingpong.common.R;
import com.pingpong.entity.RefundLog;
import com.pingpong.service.IRefundLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    /** 退款记录 Service，包含退款事务逻辑 */
    @Autowired
    private IRefundLogService refundLogService;

    /**
     * 分页查询退款记录列表
     * 自动根据登录角色做数据隔离：
     * - boss：可查看全部退款记录
     * - shop_owner：强制只看自己门店的退款记录
     *
     * @param refundLog 查询条件
     * @param current   页码，默认第1页
     * @param size      每页条数，默认10条
     * @param request   HTTP 请求，从中获取当前登录用户的角色和门店ID
     * @return 分页结果
     */
    @GetMapping
    public R<Page<RefundLog>> list(RefundLog refundLog,
                                   @RequestParam(defaultValue = "1") Integer current,
                                   @RequestParam(defaultValue = "10") Integer size,
                                   HttpServletRequest request) {
        // 数据权限判断：boss 可查全部，shop_owner 只查自己门店
        String role = (String) request.getAttribute("role");
        Long myStoreId = (Long) request.getAttribute("storeId");
        Long filterStoreId = "boss".equals(role) ? null : myStoreId;

        Page<RefundLog> page = new Page<>(current, size);
        LambdaQueryWrapper<RefundLog> wrapper = new LambdaQueryWrapper<RefundLog>()
                .eq(filterStoreId != null, RefundLog::getStoreId, filterStoreId)
                .orderByDesc(RefundLog::getCreatedAt);
        Page<RefundLog> result = refundLogService.page(page, wrapper);
        result.getRecords().forEach(refundLogService::fillNames);
        return R.ok(result);
    }

    /**
     * 根据ID查询退款记录详情
     *
     * @param id 退款记录ID
     * @return 退款记录信息
     */
    @GetMapping("/{id}")
    public R<RefundLog> getById(@PathVariable Long id) {
        RefundLog log = refundLogService.getById(id);
        return log != null ? R.ok(log) : R.fail("退款记录不存在");
    }

    /**
     * 执行退款（核心业务接口）
     * 以事务方式完成退款全流程，同时更新订单状态和学员剩余课时。
     * 任一步骤失败都会整体回滚，保证数据一致性。
     *
     * @param refundLog 退款信息（订单ID、学员ID、退款金额、退回课时、操作人等）
     * @return 操作结果
     */
    @PostMapping
    public R<?> save(@Valid @RequestBody RefundLog refundLog) {
        refundLogService.refund(refundLog);
        return R.ok("退款成功");
    }

    /**
     * 更新退款记录（仅允许修改退款原因等非核心字段）
     *
     * @param refundLog 退款信息（需带ID）
     * @return 操作结果
     */
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

    /**
     * 禁止删除退款记录。
     * 退款记录是财务凭证，只增不删，保证账本完整可审计。
     * 如需撤销退款，请走反向冲正流程。
     *
     * @param id 退款记录ID
     * @return 操作失败（业务禁止）
     */
    @DeleteMapping("/{id}")
    public R<?> delete(@PathVariable Long id) {
        return R.fail("退款记录不允许删除，退款是财务凭证只增不删");
    }
}
