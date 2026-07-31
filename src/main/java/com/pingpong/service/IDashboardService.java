package com.pingpong.service;

import com.pingpong.dto.DashboardVO;
import com.pingpong.dto.RankingItem;
import com.pingpong.dto.StoreRankingItem;

import java.util.List;
import java.util.Map;

/**
 * 仪表盘 Service 接口
 * 提供首页看板所需的各类统计数据：总览、门店业绩对比、各类排名等。
 */
public interface IDashboardService {

    /**
     * 获取仪表盘总览数据
     *
     * @param storeId 门店ID，为 null 时查询全部门店
     * @return 总览统计数据
     */
    DashboardVO overview(Long storeId);

    /**
     * 门店业绩对比（本月销售额 + 消课量）
     *
     * @param storeId 门店ID，为 null 时查询全部门店
     * @return 各门店业绩列表
     */
    List<Map<String, Object>> storePerformance(Long storeId);

    /**
     * 各门店课消明细（消课次数、消课课时、消课金额）
     *
     * @param storeId 门店ID，为 null 时查询全部门店
     * @return 各门店课消列表
     */
    List<Map<String, Object>> storeConsumption(Long storeId);

    /**
     * 本月每日业绩走势（按天聚合订单金额）
     *
     * @param storeId 门店ID，为 null 时查询全部门店
     * @return [{d: 1, amount: 12345.00}, ...]
     */
    List<Map<String, Object>> dailyTrend(Long storeId);

    /**
     * 教练消课排名（按本月消课总课时降序）
     *
     * @param storeId 门店ID，为 null 时查询全部门店
     * @param topN    取前 N 名，<=0 取全部
     * @return 排名列表
     */
    List<RankingItem> coachLessonRanking(Long storeId, int topN);

    /**
     * 教练消课排名（支持按金额或课时排序）
     *
     * @param storeId 门店ID，为 null 时查询全部门店
     * @param sortBy  排序字段：amount 按消课金额 / lessons 按消课课时
     * @param asc     是否升序
     * @param topN    取前 N 名，<=0 取全部
     * @return 排名列表
     */
    List<RankingItem> coachLessonRanking(Long storeId, String sortBy, boolean asc, int topN);

    /**
     * 教练业绩排名（按本月带单总金额降序）
     *
     * @param storeId 门店ID，为 null 时查询全部门店
     * @param topN    取前 N 名，<=0 取全部
     * @return 排名列表
     */
    List<RankingItem> coachSalesRanking(Long storeId, int topN);

    /**
     * 销售业绩排名（按本月签单总金额降序）
     *
     * @param storeId 门店ID，为 null 时查询全部门店
     * @param topN    取前 N 名，<=0 取全部
     * @return 排名列表
     */
    List<RankingItem> salesRanking(Long storeId, int topN);

    /**
     * 员工业绩排名（支持按金额或订单数排序）
     *
     * @param type    排名类型：coach 教练业绩 / sales 销售业绩 / all 所有员工
     * @param storeId 门店ID，为 null 时查询全部门店
     * @param sortBy  排序字段：amount 按业绩金额 / count 按订单数
     * @param asc     是否升序
     * @param topN    取前 N 名，<=0 取全部
     * @return 排名列表
     */
    List<RankingItem> performanceRanking(String type, Long storeId, String sortBy, boolean asc, int topN);

    /**
     * 门店消课排名（按本月各门店消课总课时降序）
     *
     * @param storeId 门店ID，为 null 时查询全部门店
     * @return 排名列表
     */
    List<StoreRankingItem> storeLessonRanking(Long storeId);

    /**
     * 门店业绩排名（按本月各门店销售额降序）
     *
     * @param storeId 门店ID，为 null 时查询全部门店
     * @return 排名列表
     */
    List<StoreRankingItem> storePerformanceRanking(Long storeId);
}
