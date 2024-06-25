package cn.humorchen.jimu.export.vo;

import cn.humorchen.jimu.export.bean.DownloadTask;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author humorchen
 * date: 2024/2/29
 * description: 下载任务vo
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class DownloadTaskVo extends DownloadTask {
    /**
     * 状态字符串
     */
    private String stateStr;


}
