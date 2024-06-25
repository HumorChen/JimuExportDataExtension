package cn.humorchen.jimu.export.cachekey;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

/**
 * @author humorchen
 * date: 2023/11/9
 * description: Cache Key，所有的redis key请继承该类
 **/
public abstract class CacheKey implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 基础前缀
     */
    private static final String BASE_PREFIX = "JimuExportDataExtension";
    /**
     * 参数分隔符
     */
    public static final String SEPARATOR = ":";


    /**
     * 获取redis 最终的key
     *
     * @return
     */
    public String getKey() {
        StringBuilder builder = new StringBuilder();
        // 前缀
        if (StrUtil.isNotBlank(BASE_PREFIX)) {
            builder.append(BASE_PREFIX);
        }
        // 类名
        builder.append(SEPARATOR);
        builder.append(this.getClass().getSimpleName());
        // 参数
        Field[] declaredFields = this.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            try {
                field.setAccessible(true);
                String fieldName = field.getName();
                Object object = field.get(this);
                builder.append(SEPARATOR);
                builder.append(fieldName);
                builder.append(SEPARATOR);
                if (object instanceof String || object instanceof Character) {
                    builder.append(object);
                } else {
                    builder.append(JSONObject.toJSONString(object));
                }
            } catch (Exception ignored) {

            }
        }
        return builder.toString();
    }

    /**
     * 获取过期时间
     *
     * @return
     */
    public abstract int getExpire();

    /**
     * 获取过期时间单位
     *
     * @return
     */
    public abstract TimeUnit getTimeUnit();

    /**
     * 转换为字符串方法
     *
     * @return
     */
    @Override
    public String toString() {
        return getKey();
    }
}
