package com.pingpong.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pingpong.common.R;
import com.pingpong.entity.CourseConsumption;
import com.pingpong.service.ICourseConsumptionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 消课记录控制器
 * 提供消课记录的增删改查接口。
 * 核心接口 POST /api/course-consumptions 会以事务方式完成消课全流程：
 * 插入消课记录 → 扣减订单剩余课时（乐观锁）→ 扣减学员总剩余课时。
 */
@RestController
@RequestMapping("/api/course-consumptions")
public class CourseConsumptionController {

    /** 消课记录 Service，包含消课事务逻辑 */
    @Autowired
    private ICourseConsumptionService courseConsumptionService;

    /**
     * 分页查询消课记录列表
     * 自动根据登录角色做数据隔离：
     * - boss：可通过 storeId 参数筛选，不传则查全部
     * - shop_owner：强制只看自己门店的数据
     *
     * @param courseConsumption 查询条件
     * @param current           页码，默认第1页
     * @param size              每页条数，默认10条
     * @param storeId           门店筛选（仅 boss 生效）
     * @param request           HTTP 请求，从中获取当前登录用户的角色和门店ID
     * @return 分页结果
     */
    @GetMapping
    public R<Page<CourseConsumption>> list(CourseConsumption courseConsumption,
                                           @RequestParam(defaultValue = "1") Integer current,
                                           @RequestParam(defaultValue = "10") Integer size,
                                           @RequestParam(required = false) Long storeId,
                                           HttpServletRequest request) {
        // 数据权限判断
        String role = (String) request.getAttribute("role");
        Long myStoreId = (Long) request.getAttribute("storeId");
        Long filterStoreId = "boss".equals(role) ? storeId : myStoreId;

        LambdaQueryWrapper<CourseConsumption> wrapper = new LambdaQueryWrapper<CourseConsumption>()
                .eq(filterStoreId != null, CourseConsumption::getStoreId, filterStoreId)
                .orderByDesc(CourseConsumption::getId);
        Page<CourseConsumption> page = new Page<>(current, size);
        Page<CourseConsumption> result = courseConsumptionService.page(page, wrapper);
        // 填充关联名称
        result.getRecords().forEach(courseConsumptionService::fillNames);
        return R.ok(result);
    }

    /**
     * 根据ID查询消课记录详情
     *
     * @param id 消课记录ID
     * @return 消课记录信息
     */
    @GetMapping("/{id}")
    public R<CourseConsumption> getById(@PathVariable Long id) {
        CourseConsumption consumption = courseConsumptionService.getById(id);
        return consumption != null ? R.ok(consumption) : R.fail("消课记录不存在");
    }

    /**
     * 执行消课（核心业务接口）
     * 以事务方式完成消课全流程，同时更新订单和学员的剩余课时。
     * 任一步骤失败都会整体回滚，保证数据一致性。
     *
     * @param courseConsumption 消课信息（订单ID、学员ID、教练ID、消课时数等）
     * @return 操作结果
     */
    @PostMapping
    public R<?> save(@Valid @RequestBody CourseConsumption courseConsumption) {
        courseConsumptionService.consumeLesson(courseConsumption);
        return R.ok("消课成功");
    }

    /**
     * 更新消课记录（仅允许修改备注、备注等非核心字段）。
     * 课时、学员、订单、教练等核心字段不可通过此接口修改。
     *
     * @param courseConsumption 消课信息（需带ID）
     * @return 操作结果
     */
    @PutMapping
    public R<?> update(@RequestBody CourseConsumption courseConsumption) {
        if (courseConsumption.getId() == null) {
            return R.fail("消课记录ID不能为空");
        }
        CourseConsumption existing = courseConsumptionService.getById(courseConsumption.getId());
        if (existing == null) {
            return R.fail("消课记录不存在");
        }
        // 只允许修改备注和日期时间，核心字段保持不变
        existing.setRemark(courseConsumption.getRemark());
        existing.setRecordDate(courseConsumption.getRecordDate());
        existing.setRecordTime(courseConsumption.getRecordTime());
        boolean ok = courseConsumptionService.updateById(existing);
        return ok ? R.ok() : R.fail("更新失败");
    }

    /**
     * 禁止删除消课记录。
     * 消课记录涉及课时扣减，直接删除会导致订单和学员课时对不上。
     * 如需撤销消课，请走退款流程。
     *
     * @param id 消课记录ID
     * @return 操作失败（业务禁止）
     */
    @DeleteMapping("/{id}")
    public R<?> delete(@PathVariable Long id) {
        return R.fail("消课记录不允许删除，如需撤销消课请走退款流程");
    }
}
