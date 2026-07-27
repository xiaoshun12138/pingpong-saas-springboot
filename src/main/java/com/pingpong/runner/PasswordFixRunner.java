package com.pingpong.runner;

import cn.hutool.crypto.digest.BCrypt;
import com.pingpong.entity.Staff;
import com.pingpong.service.IStaffService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 密码修复脚本（一次性运行）
 * 启动时自动检查所有员工密码，将明文密码统一刷成 BCrypt 加密。
 * 运行完成后可删除此类，或保留用于后续安全校验。
 *
 * 触发条件：Spring Boot 启动时自动执行
 * 幂等性：已经是 BCrypt 格式的密码会被跳过
 */
@Slf4j
@Component
public class PasswordFixRunner implements CommandLineRunner {

    @Autowired
    private IStaffService staffService;

    @Override
    public void run(String... args) {
        List<Staff> allStaff = staffService.list();
        int fixed = 0;
        for (Staff staff : allStaff) {
            String pwd = staff.getPassword();
            if (pwd != null && !pwd.startsWith("$2a$") && !pwd.startsWith("$2b$")) {
                // 明文密码，需要加密
                String hashed = BCrypt.hashpw(pwd);
                staff.setPassword(hashed);
                staffService.updateById(staff);
                fixed++;
                log.info("已修复员工密码: id={}, name={}", staff.getId(), staff.getName());
            }
        }
        if (fixed > 0) {
            log.info("密码修复完成：共 {} 个账号从明文转为 BCrypt", fixed);
        } else {
            log.info("密码检查完成：所有账号已是 BCrypt 格式，无需修复");
        }
    }
}
