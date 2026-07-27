package com.pingpong.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pingpong.common.R;
import com.pingpong.entity.Staff;
import com.pingpong.service.IStaffService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 员工控制器
 * 提供员工的增删改查接口，支持按角色、门店筛选。
 * 数据权限：店长只能看到自己门店的员工，老板可以看全部或指定门店。
 */
@RestController
@RequestMapping("/api/staff")
public class StaffController {

    /** 员工 Service */
    @Autowired
    private IStaffService staffService;

    /**
     * 分页查询员工列表
     * 自动根据登录角色做数据隔离：
     * - boss：可通过 storeId 参数筛选，不传则查全部
     * - shop_owner：强制只看自己门店的数据
     *
     * @param staff   查询条件（角色等）
     * @param current 页码，默认第1页
     * @param size    每页条数，默认10条
     * @param storeId 门店筛选（仅 boss 生效）
     * @param request HTTP 请求，从中获取当前登录用户的角色和门店ID
     * @return 分页结果
     */
    @GetMapping
    public R<Page<Staff>> list(Staff staff,
                                @RequestParam(defaultValue = "1") Integer current,
                                @RequestParam(defaultValue = "10") Integer size,
                                @RequestParam(required = false) Long storeId,
                                HttpServletRequest request) {
        // 从请求上下文中获取当前登录用户的角色和门店（拦截器已注入）
        String role = (String) request.getAttribute("role");
        Long myStoreId = (Long) request.getAttribute("storeId");
        // 数据权限判断：老板可选门店，店长强制自己门店
        Long filterStoreId = "boss".equals(role) ? storeId : myStoreId;

        LambdaQueryWrapper<Staff> wrapper = new LambdaQueryWrapper<Staff>()
                .eq(filterStoreId != null, Staff::getStoreId, filterStoreId)
                .eq(staff.getRole() != null, Staff::getRole, staff.getRole())
                .orderByAsc(Staff::getId);
        Page<Staff> page = new Page<>(current, size);
        return R.ok(staffService.page(page, wrapper));
    }

    /**
     * 根据ID查询员工详情
     *
     * @param id 员工ID
     * @return 员工信息
     */
    @GetMapping("/{id}")
    public R<Staff> getById(@PathVariable Long id) {
        Staff staff = staffService.getById(id);
        return staff != null ? R.ok(staff) : R.fail("员工不存在");
    }

    /**
     * 新增员工
     *
     * @param staff 员工信息
     * @return 操作结果
     */
    @PostMapping
    public R<?> save(@Valid @RequestBody Staff staff) {
        boolean ok = staffService.save(staff);
        return ok ? R.ok() : R.fail("新增失败");
    }

    /**
     * 更新员工信息
     * ⚠️ 安全限制：password 和 role 字段不允许通过此接口直接修改。
     * - password 修改应走专门的改密接口
     * - role 变更需要 boss 权限审批
     * 其他字段（姓名、手机号、状态等）可正常更新。
     *
     * @param staff 员工信息（需带ID）
     * @return 操作结果
     */
    @PutMapping
    public R<?> update(@Valid @RequestBody Staff staff) {
        if (staff.getId() == null) {
            return R.fail("员工ID不能为空");
        }
        Staff existing = staffService.getById(staff.getId());
        if (existing == null) {
            return R.fail("员工不存在");
        }
        // 只更新允许修改的字段，password 和 role 不能通过 PUT 篡改
        existing.setName(staff.getName());
        existing.setPhone(staff.getPhone());
        existing.setEntryDate(staff.getEntryDate());
        existing.setStatus(staff.getStatus());
        existing.setStoreId(staff.getStoreId());
        // password 和 role 保持不变，需走专门接口修改
        boolean ok = staffService.updateById(existing);
        return ok ? R.ok() : R.fail("更新失败");
    }

    /**
     * 删除员工（逻辑删除）
     *
     * @param id 员工ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public R<?> delete(@PathVariable Long id) {
        boolean ok = staffService.removeById(id);
        return ok ? R.ok() : R.fail("删除失败");
    }
}
