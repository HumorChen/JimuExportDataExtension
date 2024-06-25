package cn.humorchen.jimu.export.dto;

import lombok.Data;

/**
 * @author humorchen
 * date: 2024/1/5
 * description: 分页查询下载任务
 **/
@Data
public class PageListDownloadTaskDto {
    /**
     * 任务ID
     */
    private Integer id;
    /**
     * 文件名
     */
    private String fileName;
    /**
     * 下载任务创建起止时间，填则起止时间都要填
     */
    private String startTime;
    private String endTime;
    /**
     * 分页
     */
    private Integer pageNo;
    private Integer pageSize;
    /**
     * 任务状态
     */
    private Integer taskState;
}
