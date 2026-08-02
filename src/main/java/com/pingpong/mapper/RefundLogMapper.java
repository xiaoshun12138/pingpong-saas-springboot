package com.pingpong.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pingpong.entity.RefundLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款记录 Mapper 接口
 */
@Mapper
public interface RefundLogMapper extends BaseMapper<RefundLog> {

    @Select("SELECT COALESCE(SUM(refund_amount), 0) FROM refund_log WHERE 1=1 " +
            "AND deleted = 0 " +
            "AND created_at >= #{start} AND created_at <= #{end} " +
            "AND (#{storeId} IS NULL OR store_id = #{storeId})")
    BigDecimal sumAmountInRange(@Param("start") LocalDateTime start,
                                 @Param("end") LocalDateTime end,
                                 @Param("storeId") Long storeId);

    @Select("SELECT COUNT(*) FROM refund_log WHERE 1=1 " +
            "AND deleted = 0 " +
            "AND created_at >= #{start} AND created_at <= #{end} " +
            "AND (#{storeId} IS NULL OR store_id = #{storeId})")
    Long countInRange(@Param("start") LocalDateTime start,
                      @Param("end") LocalDateTime end,
                      @Param("storeId") Long storeId);
}
