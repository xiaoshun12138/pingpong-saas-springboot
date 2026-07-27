package com.pingpong.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pingpong.common.R;
import com.pingpong.entity.Student;
import com.pingpong.service.IStudentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 学员控制器
 * 提供学员的增删改查接口，学员是消课和订单的核心主体。
 * 数据权限：店长只能看到自己门店的学员，老板可以看全部或指定门店。
 */
@RestController
@RequestMapping("/api/students")
public class StudentController {

    /** 学员 Service */
    @Autowired
    private IStudentService studentService;

    /**
     * 分页查询学员列表
     * 自动根据登录角色做数据隔离：
     * - boss：可通过 storeId 参数筛选，不传则查全部
     * - shop_owner：强制只看自己门店的数据
     *
     * @param student 查询条件
     * @param current 页码，默认第1页
     * @param size    每页条数，默认10条
     * @param storeId 门店筛选（仅 boss 生效）
     * @param request HTTP 请求，从中获取当前登录用户的角色和门店ID
     * @return 分页结果
     */
    @GetMapping
    public R<Page<Student>> list(Student student,
                                 @RequestParam(defaultValue = "1") Integer current,
                                 @RequestParam(defaultValue = "10") Integer size,
                                 @RequestParam(required = false) Long storeId,
                                 HttpServletRequest request) {
        // 从请求上下文中获取当前登录用户的角色和门店
        String role = (String) request.getAttribute("role");
        Long myStoreId = (Long) request.getAttribute("storeId");
        // 数据权限判断：老板可选门店，店长强制自己门店
        Long filterStoreId = "boss".equals(role) ? storeId : myStoreId;

        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<Student>()
                .eq(filterStoreId != null, Student::getStoreId, filterStoreId)
                .orderByDesc(Student::getId);
        Page<Student> page = new Page<>(current, size);
        return R.ok(studentService.page(page, wrapper));
    }

    /**
     * 根据ID查询学员详情
     *
     * @param id 学员ID
     * @return 学员信息
     */
    @GetMapping("/{id}")
    public R<Student> getById(@PathVariable Long id) {
        Student student = studentService.getById(id);
        return student != null ? R.ok(student) : R.fail("学员不存在");
    }

    /**
     * 新增学员
     *
     * @param student 学员信息
     * @return 操作结果
     */
    @PostMapping
    public R<?> save(@Valid @RequestBody Student student) {
        boolean ok = studentService.save(student);
        return ok ? R.ok() : R.fail("新增失败");
    }

    /**
     * 更新学员信息（仅允许修改姓名、电话、地址等基本资料）。
     * totalRemainingLessons/version 由消课/退款接口维护，不可通过此接口篡改。
     *
     * @param student 学员信息（需带ID）
     * @return 操作结果
     */
    @PutMapping
    public R<?> update(@Valid @RequestBody Student student) {
        if (student.getId() == null) {
            return R.fail("学员ID不能为空");
        }
        Student existing = studentService.getById(student.getId());
        if (existing == null) {
            return R.fail("学员不存在");
        }
        // 只更新基本资料字段，课时和锁字段保持不变
        existing.setName(student.getName());
        existing.setPhone(student.getPhone());
        existing.setAge(student.getAge());
        existing.setAddress(student.getAddress());
        existing.setSource(student.getSource());
        existing.setStatus(student.getStatus());
        existing.setStoreId(student.getStoreId());
        boolean ok = studentService.updateById(existing);
        return ok ? R.ok() : R.fail("更新失败");
    }

    /**
     * 删除学员（逻辑删除）
     *
     * @param id 学员ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public R<?> delete(@PathVariable Long id) {
        boolean ok = studentService.removeById(id);
        return ok ? R.ok() : R.fail("删除失败");
    }
}
