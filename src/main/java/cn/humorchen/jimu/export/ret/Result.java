package cn.humorchen.jimu.export.ret;

import cn.hutool.core.date.DateUtil;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 接口返回结果封装
 *
 * @param <T>
 * @author humorchen
 */
@Data
@Accessors(chain = true)
public class Result<T> implements JSONResult {
    /**
     * 响应状态码
     */
    private int code;
    /**
     * 提示信息
     */
    private String msg;
    /**
     * 数据
     */
    private T data;
    /**
     * 获取服务器时间
     * 标准日期时间格式，精确到秒：yyyy-MM-dd HH:mm:ss
     *
     * @return
     * @see cn.hutool.core.date.DatePattern#NORM_DATETIME_PATTERN
     */
    private final String timestamp;

    public Result() {
        this.timestamp = DateUtil.now();
    }

    public static <T> Result<T> ok(T data) {
        Result<T> result = new Result<>();
        result.code = 0;
        result.data = data;
        return result;
    }

    public static <T> Result<T> fail(T data) {
        Result<T> result = new Result<>();
        result.code = -1;
        result.data = data;
        return result;
    }

    public static <T> Result<T> ok(String msg, T data) {
        Result<T> ok = ok(data);
        ok.msg = msg;
        return ok;
    }

    public static <T> Result<T> fail(String msg, T data) {
        Result<T> fail = fail(data);
        fail.msg = msg;
        return fail;
    }

    public static <T> Result<T> fail(int code, String msg) {
        Result<T> fail = fail(msg, null);
        fail.code = code;
        return fail;
    }

    public static <T> Result<T> fail(int code, String msg, T data) {
        Result<T> fail = fail(code, msg);
        fail.data = data;
        return fail;
    }


    public boolean isSuccess() {
        return 0 == this.code;
    }

}
