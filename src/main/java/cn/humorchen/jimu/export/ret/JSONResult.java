package cn.humorchen.jimu.export.ret;

/**
 * 前后端分离项目，后端输出json响应结果的最外层结构
 * <p>
 * 严禁非接口层方法的返回值使用此类型
 */
public interface JSONResult {

    /**
     * 获取错误码，默认为0表示没有错误
     *
     * @return
     */
    int getCode();

    /**
     * 获取错误消息
     *
     * @return
     */
    String getMsg();

    /**
     * 获取响应数据
     *
     * @return
     */

    Object getData();

    /**
     * 获取服务器时间
     * 标准日期时间格式，精确到秒：yyyy-MM-dd HH:mm:ss
     *
     * @return
     * @see cn.hutool.core.date.DatePattern#NORM_DATETIME_PATTERN
     */
    String getTimestamp();
}
