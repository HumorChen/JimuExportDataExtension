package cn.humorchen.jimu.export.enums;

import lombok.Getter;

/**
 * @author humorchen
 * date: 2024/1/5
 * description: 下载任务状态
 **/
@Getter
public enum DownloadTaskStateEnum {
    WAIT(0, "等待执行"),
    RUNNING(1, "执行中"),
    SUCCESS(2, "执行成功"),
    FAILED(3, "执行失败"),

    ;

    private final int state;
    private final String title;

    DownloadTaskStateEnum(int state, String title) {
        this.state = state;
        this.title = title;
    }

    /**
     * 根据状态获取枚举
     *
     * @param state
     * @return
     */
    public static DownloadTaskStateEnum of(int state) {
        for (DownloadTaskStateEnum value : values()) {
            if (value.state == state) {
                return value;
            }
        }
        return null;
    }
}
