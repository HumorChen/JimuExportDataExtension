package cn.humorchen.jimu.export.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * @author humorchen
 * date: 2024/1/16
 * description: 下载任务发布事件
 **/
@Getter
public class DownloadTaskPublishEvent extends ApplicationEvent {
    /**
     * 任务ID
     */
    private Integer taskId;

    public DownloadTaskPublishEvent(Integer taskId) {
        super(taskId);
        this.taskId = taskId;
    }

}
