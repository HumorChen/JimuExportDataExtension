package cn.humorchen.jimu.export.param;

import lombok.Data;

/**
 * @author humorchen
 * date: 2024/1/15
 * description: 下载中心基础参数类
 **/
@Data
public class DownloadCenterBaseParam {

    /**
     * 分页数据页号和页大小
     */
    private Integer pageNo;
    /**
     * 分页数据页号和页大小
     */
    private Integer pageSize;
    /**
     * 导出数量
     */
    private Integer size;
}
