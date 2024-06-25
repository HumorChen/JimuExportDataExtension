package cn.humorchen.jimu.export.dto;

import lombok.Data;

/**
 * @author humorchen
 * date: 2024/3/5
 * description: 积木报表数据列
 **/
@Data
public class JimuReportDataColumnDTO {
    /**
     * 下标
     */
    private Integer index;
    /**
     * 字段的中文列名
     */
    private String name;
    /**
     * 数据字段名
     */
    private String column;
}
