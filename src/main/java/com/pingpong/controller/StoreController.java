package com.pingpong.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pingpong.common.R;
import com.pingpong.entity.Store;
import com.pingpong.service.IStoreService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 门店控制器
 * 提供门店的增删改查接口，门店是 SaaS 多门店架构的基础单位。
 * 数据权限：boss 看到全部门店，shop_owner 只能看到自己门店。
 */
@RestController
@RequestMapping("/api/stores")
public class StoreController {

    @Autowired
    private IStoreService storeService;

    /**
     * 分页查询门店列表，自动按角色隔离：
     * boss → 全部门店；shop_owner → 仅自己门店。
     */
    @GetMapping
    public R<Page<Store>> list(Store store,
                               @RequestParam(defaultValue = "1") Integer current,
                               @RequestParam(defaultValue = "10") Integer size,
                               HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        Long myStoreId = (Long) request.getAttribute("storeId");

        Page<Store> page = new Page<>(current, size);
        LambdaQueryWrapper<Store> wrapper = new LambdaQueryWrapper<Store>()
                .eq(!"boss".equals(role), Store::getId, myStoreId)
                .orderByAsc(Store::getId);
        return R.ok(storeService.page(page, wrapper));
    }

    @GetMapping("/{id}")
    public R<Store> getById(@PathVariable Long id) {
        Store store = storeService.getById(id);
        return store != null ? R.ok(store) : R.fail("门店不存在");
    }

    @PostMapping
    public R<?> save(@Valid @RequestBody Store store) {
        boolean ok = storeService.save(store);
        return ok ? R.ok() : R.fail("新增失败");
    }

    @PutMapping
    public R<?> update(@Valid @RequestBody Store store) {
        boolean ok = storeService.updateById(store);
        return ok ? R.ok() : R.fail("更新失败");
    }

    @DeleteMapping("/{id}")
    public R<?> delete(@PathVariable Long id) {
        boolean ok = storeService.removeById(id);
        return ok ? R.ok() : R.fail("删除失败");
    }
}
