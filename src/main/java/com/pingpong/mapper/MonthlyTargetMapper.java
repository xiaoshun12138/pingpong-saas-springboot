package com.pingpong.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pingpong.entity.MonthlyTarget;
import org.apache.ibatis.annotations.Mapper;

/**
 * 月度目标 Mapper 接口
 * 继承 MyBatis-Plus 的 BaseMapper，自动拥有基础 CRUD 方法。
 */
@Mapper
public interface MonthlyTargetMapper extends BaseMapper<MonthlyTarget> {
}
