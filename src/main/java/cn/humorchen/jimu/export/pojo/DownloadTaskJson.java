package cn.humorchen.jimu.export.pojo;

import cn.humorchen.jimu.export.param.DownloadCenterBaseParam;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

/**
 * @author humorchen
 * date: 2024/1/16
 * description: 下载中心json字段的对象
 **/
@Data
@FieldNameConstants
public class DownloadTaskJson {
    /**
     * 代理的方法
     * 例如 dashboardNewVersion
     */
    private String proxyMethod;
    /**
     * 导出时的查询参数
     */
    private DownloadCenterBaseParam param;
    /**
     * 请求token
     */
    private String requestToken;
    /**
     * 请求体
     */
    private String requestBody;

    /**
     * toString
     *
     * @return
     */
    public String toString() {
        return JSONObject.toJSONString(this);
    }

}
