package com.pingpong.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pingpong.common.R;
import com.pingpong.entity.CourseOrder;
import com.pingpong.entity.CourseType;
import com.pingpong.entity.Student;
import com.pingpong.mapper.StudentMapper;
import com.pingpong.service.ICourseOrderService;
import com.pingpong.service.ICourseTypeService;
import com.pingpong.service.IStudentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 课包订单控制器
 * 提供订单的增删改查接口，订单记录学员购买课包的信息和课时消耗进度。
 * 数据权限：店长只能看到自己门店的订单，老板可以看全部或指定门店。
 * 重要：课时变动（remainingLessons / consumedLessons）必须通过消课/退款接口，不允许直接 PUT 修改。
 */
@RestController
@RequestMapping("/api/course-orders")
public class CourseOrderController {

    @Autowired
    private ICourseOrderService courseOrderService;
    @Autowired
    private IStudentService studentService;
    @Autowired
    private ICourseTypeService courseTypeService;
    @Autowired
    private StudentMapper studentMapper;

    /**
     * 分页查询订单列表，支持按学员、销售、教练、状态筛选，自动按角色隔离门店数据。
     */
    @GetMapping
    public R<Page<CourseOrder>> list(CourseOrder courseOrder,
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

        Page<CourseOrder> page = new Page<>(current, size);
        LambdaQueryWrapper<CourseOrder> wrapper = new LambdaQueryWrapper<CourseOrder>()
                .eq(filterStoreId != null, CourseOrder::getStoreId, filterStoreId)
                .eq(courseOrder.getStudentId() != null, CourseOrder::getStudentId, courseOrder.getStudentId())
                .eq(courseOrder.getSalesId() != null, CourseOrder::getSalesId, courseOrder.getSalesId())
                .eq(courseOrder.getCoachId() != null, CourseOrder::getCoachId, courseOrder.getCoachId())
                .eq(courseOrder.getStatus() != null, CourseOrder::getStatus, courseOrder.getStatus())
                .ge(startDate != null && !startDate.isBlank(), CourseOrder::getCreatedAt, startDate)
                .le(endDate != null && !endDate.isBlank(), CourseOrder::getCreatedAt, endDate)
                .and(keyword != null && !keyword.isBlank(), w -> {
                    List<Long> studentIds = studentMapper.selectList(
                            new LambdaQueryWrapper<Student>()
                                    .select(Student::getId)
                                    .like(Student::getName, keyword))
                            .stream().map(Student::getId).collect(Collectors.toList());
                    if (!studentIds.isEmpty()) {
                        w.in(CourseOrder::getStudentId, studentIds);
                    } else {
                        w.eq(CourseOrder::getStudentId, -1L); // 搜不到任何学员，返回空
                    }
                })
                .orderByDesc(CourseOrder::getCreatedAt);
        Page<CourseOrder> result = courseOrderService.page(page, wrapper);
        result.getRecords().forEach(courseOrderService::fillNames);
        return R.ok(result);
    }

    @GetMapping("/{id}")
    public R<CourseOrder> getById(@PathVariable Long id) {
        CourseOrder order = courseOrderService.getById(id);
        return order != null ? R.ok(order) : R.fail("订单不存在");
    }

    // 注入订单 Mapper，用于自动生成订单号
    @Autowired
    private com.pingpong.mapper.CourseOrderMapper courseOrderMapper;

    /**
     * 新增订单。订单编号自动生成；学员按姓名查找（不存在则自动创建）；
     * 销售按姓名查找；课时从课包复制但允许覆盖（送课场景）。
     */
    @PostMapping
    public R<?> save(HttpServletRequest request, @RequestBody CourseOrder order) {
        Long myStoreId = (Long) request.getAttribute("storeId");
        String myRole = (String) request.getAttribute("role");

        // 1. 自动生成订单号
        if (order.getOrderNo() == null || order.getOrderNo().isBlank()) {
            String prefix = "ORD" + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
            // 查今天最大的序号
            List<CourseOrder> todays = courseOrderMapper.selectList(
                    new LambdaQueryWrapper<CourseOrder>().likeRight(CourseOrder::getOrderNo, prefix)
                            .orderByDesc(CourseOrder::getOrderNo).last("LIMIT 1"));
            int seq = 1;
            if (!todays.isEmpty()) {
                String last = todays.get(0).getOrderNo();
                try { seq = Integer.parseInt(last.substring(last.length() - 4)) + 1; } catch (Exception ignored) {}
            }
            order.setOrderNo(prefix + String.format("%04d", seq));
        }

        // 2. 学员：按姓名查找（仅允许新学员，老学员请去学员列表续费）
        String studentName = (String) order.getParams().get("studentName");
        if (studentName == null || studentName.isBlank()) {
            return R.fail("请输入学员姓名");
        }
        String studentPhone = (String) order.getParams().get("studentPhone");
        if (studentPhone == null || studentPhone.isBlank()) {
            return R.fail("请输入学员手机号");
        }
        Long storeId = order.getStoreId() != null ? order.getStoreId() : myStoreId;
        Student student = studentMapper.selectOne(
                new LambdaQueryWrapper<Student>().eq(Student::getName, studentName)
                        .eq(Student::getStoreId, storeId).last("LIMIT 1"));
        if (student != null) {
            return R.fail("学员「" + studentName + "」已存在，请前往学员管理页面使用续费功能");
        }
        // 新学员：自动创建
        student = new Student();
        student.setName(studentName);
        student.setStoreId(storeId);
        student.setPhone(studentPhone);
        student.setPrimaryCoachId(order.getCoachId());
        student.setStatus(1);
        student.setRegisteredAt(LocalDateTime.now());
        order.setStoreId(storeId);

        // 3. 主管教练和销售由前端下拉框传入，直接使用 order.coachId / order.salesId
        // 新建学员自动绑定主管教练

        // 4. 课包 + 课时
        if (order.getCourseTypeId() == null) {
            return R.fail("请选择课包");
        }
        CourseType courseType = courseTypeService.getById(order.getCourseTypeId());
        if (courseType == null) {
            return R.fail("课包不存在");
        }
        // 以实际填写课时为准，未填则取课包默认值
        if (order.getTotalLessons() == null || order.getTotalLessons() <= 0) {
            order.setTotalLessons(courseType.getTotalLessons());
        }

        // 5. 默认值
        order.setRemainingLessons(order.getTotalLessons());
        order.setConsumedLessons(0);
        order.setVersion(0);
        order.setStatus("active");
        order.setType("new");

        // 6. 事务保存：创建学员 + 保存订单 + 累加课时（同一事务，失败全回滚）
        courseOrderService.createOrderWithNewStudent(order, student);

        return R.ok();
    }

    /**
     * 更新订单信息。
     * ⚠️ 安全限制：课时相关字段（remainingLessons / consumedLessons / version）不允许通过此接口直接修改，
     * 课时变动必须走消课或退款接口。totalLessons 也不允许修改（修改课包应新开订单）。
     */
    @PutMapping
    public R<?> update(@Valid @RequestBody CourseOrder courseOrder) {
        if (courseOrder.getId() == null) {
            return R.fail("订单ID不能为空");
        }
        // 先查出原订单，仅允许修改非敏感字段
        CourseOrder existing = courseOrderService.getById(courseOrder.getId());
        if (existing == null) {
            return R.fail("订单不存在");
        }
        // 只更新业务层面允许修改的字段，课时和锁字段从 existing 保留
        existing.setOrderNo(courseOrder.getOrderNo());
        existing.setStoreId(courseOrder.getStoreId());
        existing.setStudentId(courseOrder.getStudentId());
        existing.setSalesId(courseOrder.getSalesId());
        existing.setCoachId(courseOrder.getCoachId());
        existing.setCourseTypeId(courseOrder.getCourseTypeId());
        existing.setPaidAmount(courseOrder.getPaidAmount());
        existing.setSource(courseOrder.getSource());
        existing.setRemark(courseOrder.getRemark());
        existing.setStatus(courseOrder.getStatus());
        // remainingLessons / consumedLessons / totalLessons / version 保持不变，不能通过此接口篡改

        boolean ok = courseOrderService.updateById(existing);
        return ok ? R.ok() : R.fail("更新失败");
    }

    /**
     * 续费：老学员已有 ID，直接从学员列表发起，不再做姓名查重。
     */
    @PostMapping("/renew")
    public R<?> renew(HttpServletRequest request, @RequestBody CourseOrder order) {
        Long myStoreId = (Long) request.getAttribute("storeId");

        if (order.getStudentId() == null) {
            return R.fail("学员ID不能为空");
        }
        Student student = studentService.getById(order.getStudentId());
        if (student == null) {
            return R.fail("学员不存在");
        }

        // 自动生成订单号
        String prefix = "ORD" + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        List<CourseOrder> todays = courseOrderMapper.selectList(
                new LambdaQueryWrapper<CourseOrder>().likeRight(CourseOrder::getOrderNo, prefix)
                        .orderByDesc(CourseOrder::getOrderNo).last("LIMIT 1"));
        int seq = 1;
        if (!todays.isEmpty()) {
            try { seq = Integer.parseInt(todays.get(0).getOrderNo().substring(todays.get(0).getOrderNo().length() - 4)) + 1; } catch (Exception ignored) {}
        }
        order.setOrderNo(prefix + String.format("%04d", seq));

        order.setStoreId(order.getStoreId() != null ? order.getStoreId() : (myStoreId != null ? myStoreId : student.getStoreId()));

        if (order.getCourseTypeId() == null) return R.fail("请选择课包");
        CourseType courseType = courseTypeService.getById(order.getCourseTypeId());
        if (courseType == null) return R.fail("课包不存在");

        if (order.getTotalLessons() == null || order.getTotalLessons() <= 0) {
            order.setTotalLessons(courseType.getTotalLessons());
        }
        order.setRemainingLessons(order.getTotalLessons());
        order.setConsumedLessons(0);
        order.setVersion(0);
        order.setStatus("active");
        order.setType("renew");

        // 事务保存：保存订单 + 累加课时（同一事务，失败全回滚）
        courseOrderService.renewOrder(order, student);

        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<?> delete(@PathVariable Long id) {
        boolean ok = courseOrderService.removeById(id);
        return ok ? R.ok() : R.fail("删除失败");
    }
}
