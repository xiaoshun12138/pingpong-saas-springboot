package com.pingpong.controller;

import cn.hutool.crypto.digest.BCrypt;
import com.pingpong.common.JwtUtil;
import com.pingpong.common.R;
import com.pingpong.dto.LoginRequest;
import com.pingpong.dto.LoginResponse;
import com.pingpong.entity.Staff;
import com.pingpong.service.IStaffService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 提供后台管理系统登录接口，校验账号密码并签发 JWT Token。
 * 登录接口不经过 AuthInterceptor 拦截，是唯一免鉴权的接口。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /** 员工 Service，用于查询登录账号 */
    @Autowired
    private IStaffService staffService;

    /**
     * 员工登录
     * 校验手机号和密码，通过后生成 JWT Token 返回。
     * 权限限制：只有 boss（老板）和 shop_owner（店长）能登录后台管理系统，
     * 教练和销售只能使用小程序端登录。
     *
     * @param req 登录请求体（手机号 + 密码）
     * @return 登录响应（Token + 员工基本信息）
     */
    @PostMapping("/login")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        // 根据手机号查询员工
        Staff staff = staffService.getOne(new LambdaQueryWrapper<Staff>()
                .eq(Staff::getPhone, req.getPhone()));
        if (staff == null) {
            return R.fail(400, "手机号未注册");
        }

        // 校验密码：兼容 BCrypt 密文和明文（历史数据兼容）
        // 注意：PasswordFixRunner 会在启动时自动将明文密码刷为 BCrypt，此分支仅做兜底兼容
        String dbPwd = staff.getPassword();
        boolean ok;
        if (dbPwd != null && dbPwd.startsWith("$2a$")) {
            // BCrypt 加密密码
            ok = BCrypt.checkpw(req.getPassword(), dbPwd);
        } else {
            // 明文密码（兼容旧数据，PasswordFixRunner 会自动修复）
            ok = req.getPassword().equals(dbPwd);
        }
        if (!ok) {
            return R.fail(400, "密码错误");
        }

        // 校验账号状态
        if (staff.getStatus() != null && staff.getStatus() != 1) {
            return R.fail(400, "账号已被禁用");
        }

        // 后台登录权限校验：只有老板和店长允许登录后台
        if (!"boss".equals(staff.getRole()) && !"shop_owner".equals(staff.getRole())) {
            return R.fail(403, "教练和销售无后台登录权限，请使用小程序端");
        }

        // 生成 JWT Token 并返回
        String token = JwtUtil.generate(staff.getId(), staff.getName(), staff.getStoreId(), staff.getRole());
        return R.ok(new LoginResponse(token, staff.getId(), staff.getName(), staff.getStoreId(), staff.getRole()));
    }
}
