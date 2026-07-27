package com.pingpong.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pingpong.entity.Staff;
import org.apache.ibatis.annotations.Mapper;

/**
 * 员工 Mapper 接口
 * 继承 MyBatis-Plus 的 BaseMapper，自动拥有基础 CRUD 方法。
 */
@Mapper
public interface StaffMapper extends BaseMapper<Staff> {
}
