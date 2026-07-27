package com.pingpong.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pingpong.common.R;
import com.pingpong.entity.CourseType;
import com.pingpong.service.ICourseTypeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 课包类型控制器
 * 提供课包（课程套餐）的增删改查接口，课包是学员下单时的商品模板。
 */
@RestController
@RequestMapping("/api/course-types")
public class CourseTypeController {

    /** 课包类型 Service */
    @Autowired
    private ICourseTypeService courseTypeService;

    /**
     * 分页查询课包列表
     *
     * @param courseType 查询条件
     * @param current    页码，默认第1页
     * @param size       每页条数，默认10条
     * @return 分页结果
     */
    @GetMapping
    public R<Page<CourseType>> list(CourseType courseType,
                                     @RequestParam(defaultValue = "1") Integer current,
                                     @RequestParam(defaultValue = "10") Integer size) {
        Page<CourseType> page = new Page<>(current, size);
        Page<CourseType> result = courseTypeService.page(page);
        return R.ok(result);
    }

    /**
     * 根据ID查询课包详情
     *
     * @param id 课包ID
     * @return 课包信息
     */
    @GetMapping("/{id}")
    public R<CourseType> getById(@PathVariable Long id) {
        CourseType courseType = courseTypeService.getById(id);
        return courseType != null ? R.ok(courseType) : R.fail("课包不存在");
    }

    /**
     * 新增课包
     *
     * @param courseType 课包信息
     * @return 操作结果
     */
    @PostMapping
    public R<?> save(@Valid @RequestBody CourseType courseType) {
        boolean ok = courseTypeService.save(courseType);
        return ok ? R.ok() : R.fail("新增失败");
    }

    /**
     * 更新课包信息
     *
     * @param courseType 课包信息（需带ID）
     * @return 操作结果
     */
    @PutMapping
    public R<?> update(@Valid @RequestBody CourseType courseType) {
        boolean ok = courseTypeService.updateById(courseType);
        return ok ? R.ok() : R.fail("更新失败");
    }

    /**
     * 删除课包（逻辑删除）
     *
     * @param id 课包ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public R<?> delete(@PathVariable Long id) {
        boolean ok = courseTypeService.removeById(id);
        return ok ? R.ok() : R.fail("删除失败");
    }
}
