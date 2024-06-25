package cn.humorchen.jimu.export.bean;

/**
 * 下载任务
 *
 * @author humorchen
 * date: 2024/6/25
 * description: 下载任务实体类
 **/

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_download_task")
public class DownloadTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * 创建下载任务的账号
     */
    @TableField("account")
    private String account;

    /**
     * 下载任务标题
     */
    @TableField("title")
    private String title;

    /**
     * 图标
     */
    @TableField("icon")
    private String icon;

    /**
     * 文件URL
     */
    @TableField("url")
    private String url;

    /**
     * 文件大小
     */
    @TableField("file_size")
    private String fileSize;

    /**
     * 进度（例如50%）
     */
    @TableField("percent")
    private String percent;

    /**
     * 任务状态（0 等待执行，1执行中，2执行成功，3执行失败）
     */
    @TableField("state")
    private Integer state;

    /**
     * 执行报错信息（有则填）
     */
    @TableField("error")
    private String error;

    /**
     * 预留的json扩展字段
     */
    @TableField("json")
    private String json;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private Date updateTime;


    public static final String ID = "id";

    public static final String ACCOUNT = "account";

    public static final String TITLE = "title";

    public static final String ICON = "icon";

    public static final String URL = "url";

    public static final String FILE_SIZE = "file_size";

    public static final String PERCENT = "percent";

    public static final String STATE = "state";

    public static final String ERROR = "error";

    public static final String JSON = "json";

    public static final String CREATE_TIME = "create_time";

    public static final String UPDATE_TIME = "update_time";

}

