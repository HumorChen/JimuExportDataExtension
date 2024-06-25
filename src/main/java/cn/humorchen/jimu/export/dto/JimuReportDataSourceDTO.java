package cn.humorchen.jimu.export.dto;

import lombok.Data;

/**
 * @author humorchen
 * date: 2024/3/5
 * description: 积木报表数据源(来源于接口或者SQL)
 **/
@Data
public class JimuReportDataSourceDTO {
    /**
     * sql
     */
    private String sql;
    /**
     * api url
     */
    private String apiUrl;
}
