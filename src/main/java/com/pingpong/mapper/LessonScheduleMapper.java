package com.pingpong.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pingpong.entity.LessonSchedule;
import org.apache.ibatis.annotations.Mapper;

/**
 * 排课记录 Mapper 接口
 * 继承 MyBatis-Plus 的 BaseMapper，自动拥有基础 CRUD 方法。
 */
@Mapper
public interface LessonScheduleMapper extends BaseMapper<LessonSchedule> {

    /**
     * 物理删除排课记录（绕过 @TableLogic 逻辑删除）
     * 用于取消排课场景，避免逻辑删除与唯一索引冲突
     */
    @org.apache.ibatis.annotations.Delete("DELETE FROM lesson_schedule WHERE id = #{id}")
    int physicalDeleteById(@org.apache.ibatis.annotations.Param("id") Long id);
}
