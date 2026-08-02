package com.pingpong.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pingpong.common.R;
import com.pingpong.entity.Student;
import com.pingpong.mapper.CourseOrderMapper;
import com.pingpong.service.IStudentService;
import com.pingpong.vo.StudentOrderVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    /** 订单 Mapper（用于查学员课包列表） */
    @Autowired
    private CourseOrderMapper courseOrderMapper;

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
                                 @RequestParam(required = false) String keyword,
                                 @RequestParam(required = false) String sortBy,
                                 @RequestParam(required = false) String sortOrder,
                                 HttpServletRequest request) {
        // 从请求上下文中获取当前登录用户的角色和门店
        String role = (String) request.getAttribute("role");
        Long myStoreId = (Long) request.getAttribute("storeId");
        // 数据权限判断：老板可选门店，店长强制自己门店
        Long filterStoreId = "boss".equals(role) ? storeId : myStoreId;

        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<Student>()
                .eq(filterStoreId != null, Student::getStoreId, filterStoreId)
                .eq(student.getStatus() != null, Student::getStatus, student.getStatus())
                .eq(student.getPrimaryCoachId() != null, Student::getPrimaryCoachId, student.getPrimaryCoachId())
                .and(keyword != null && !keyword.isBlank(), w -> w
                        .like(Student::getName, keyword)
                        .or()
                        .like(Student::getPhone, keyword));
        // 排序支持
        if (sortBy != null && !sortBy.isBlank()) {
            boolean asc = "asc".equalsIgnoreCase(sortOrder);
            switch (sortBy) {
                case "totalRemainingLessons" -> wrapper.orderBy(true, asc, Student::getTotalRemainingLessons);
                case "registeredAt" -> wrapper.orderBy(true, asc, Student::getRegisteredAt);
                case "lastLessonAt" -> wrapper.orderBy(true, asc, Student::getLastLessonAt);
                case "name" -> wrapper.orderBy(true, asc, Student::getName);
                default -> wrapper.orderByDesc(Student::getId);
            }
        } else {
            wrapper.orderByDesc(Student::getId);
        }
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
        existing.setPrimaryCoachId(student.getPrimaryCoachId());
        boolean ok = studentService.updateById(existing);
        return ok ? R.ok() : R.fail("更新失败");
    }

    /**
     * 查询学员名下的课包列表
     *
     * @param id 学员ID
     * @return 课包列表（含课包名、总课时、剩余课时、金额）
     */
    @GetMapping("/{id}/orders")
    public R<List<StudentOrderVO>> getStudentOrders(@PathVariable Long id) {
        Student student = studentService.getById(id);
        if (student == null) {
            return R.fail("学员不存在");
        }
        List<StudentOrderVO> orders = courseOrderMapper.getStudentOrders(id);
        return R.ok(orders);
    }

    /**
     * 切换学员停课/复课状态
     * 老板和店长可操作。
     *
     * @param id     学员ID
     * @param status 新状态：1在读 0停课
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
    public R<?> toggleStatus(@PathVariable Long id, @RequestParam Integer status,
                             HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"boss".equals(role) && !"shop_owner".equals(role)) {
            return R.fail("无权限");
        }
        Student student = studentService.getById(id);
        if (student == null) {
            return R.fail("学员不存在");
        }
        // 店长只能操作自己门店的学员
        if ("shop_owner".equals(role)) {
            Long myStoreId = (Long) request.getAttribute("storeId");
            if (!myStoreId.equals(student.getStoreId())) {
                return R.fail("无权限，该学员不在您的门店");
            }
        }
        if (status != 0 && status != 1) {
            return R.fail("状态值无效，只能传 0（停课）或 1（在读）");
        }
        student.setStatus(status);
        boolean ok = studentService.updateById(student);
        return ok ? R.ok() : R.fail("操作失败");
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
