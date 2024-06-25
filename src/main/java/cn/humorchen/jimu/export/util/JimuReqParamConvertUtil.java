package cn.humorchen.jimu.export.util;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @author humorchen
 * date: 2024/4/11
 * description: 积木请求参数转换工具类
 * 时间范围选择的时候会加begin和end的后缀，因此定义参数的时候要注意
 **/
@Slf4j
public class JimuReqParamConvertUtil {
    private static final List<String> RANGE_LEFT_KEY_WORD = Lists.newArrayList("Begin", "Start", "From");
    private static final List<String> RANGE_RIGHT_KEY_WORD = Lists.newArrayList("End", "Stop", "To");

    /**
     * 将请求参数转换为指定类型
     *
     * @param req
     * @param cls
     * @param <T>
     * @return
     */
    public static <T> T convert(HttpServletRequest req, Class<T> cls) {
        if (req == null) {
            return null;
        }
        return convert(req.getParameterMap(), cls, null, null);
    }

    /**
     * 将请求参数转换为指定类型
     *
     * @param req
     * @param cls
     * @param <T>
     * @return
     */
    public static <T> T convert(HttpServletRequest req, Class<T> cls, int pageNo, int pageSize) {
        if (req == null) {
            return null;
        }
        return convert(req.getParameterMap(), cls, pageNo, pageSize);
    }


    /**
     * 将请求参数转换为指定类型
     *
     * @param parameterMap
     * @param cls
     * @param <T>
     * @return
     */
    public static <T> T convert(Map<String, String[]> parameterMap, Class<T> cls, Integer pageNo, Integer pageSize) {
        if (parameterMap == null || cls == null) {
            return null;
        }
        try {
            log.info("积木报表参数转换 参数类 {} ，收到的参数：{}", cls.getName(), JSONObject.toJSONString(parameterMap));
            // 构建jsonobject
            JSONObject jsonObject = new JSONObject();
            // 分页信息
            if (pageNo != null) {
                jsonObject.put("pageNo", pageNo);
            }
            if (pageSize != null) {
                jsonObject.put("pageSize", pageSize);
            }
            // 重命名
            for (Map.Entry<String, String[]> stringEntry : parameterMap.entrySet()) {
                String key = stringEntry.getKey();
                String[] values = stringEntry.getValue();
                String finalObj = null;
                // 值处理
                if (values == null || values.length == 0) {
                    continue;
                }
                if (values.length == 1) {
                    finalObj = values[0];
                } else {
                    finalObj = JSONObject.toJSONString(values);
                }
                if (finalObj == null || finalObj.isEmpty()) {
                    continue;
                }
                // 带下划线的、范围类型的
                if (key.contains("_")) {
                    // 下划线命名转驼峰命名
                    String[] split = key.split("_");
                    // 只变换尾部的一个
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < split.length; i++) {
                        if (i == 0) {
                            sb.append(split[i]);
                        } else {
                            sb.append(split[i].substring(0, 1).toUpperCase()).append(split[i].substring(1));
                        }
                    }
                    jsonObject.put(sb.toString(), finalObj);
                    // 全部变换为驼峰法
                    jsonObject.put(StrUtil.toCamelCase(key), finalObj);
                }
                // 范围类型
                setRangeAllKeyWord(key, finalObj, jsonObject);
                // 自己这个key
                jsonObject.put(key, finalObj);
            }

            T paramObject = jsonObject.toJavaObject(cls);
            log.info("积木报表参数转换 参数类 {} ，转换后的参数：{}", cls.getName(), paramObject);
            return paramObject;
        } catch (Exception e) {
            log.error("convert error", e);
        }
        return null;
    }

    /**
     * 写入范围的所有关键词
     * 例如传入
     * createTimeFrom  , 1  ,{}
     * 输出
     * {"createTimeFrom":1,"createTimeStart":1,"createTimeBegin":1}
     *
     * @param key
     * @param value
     * @param jsonObject
     */
    private static void setRangeAllKeyWord(String key, Object value, JSONObject jsonObject) {
        key = StrUtil.toCamelCase(key);
        String subKey = null;
        List<String> rangeKeyWordList = null;
        // 找是左边还是右边
        for (String s : RANGE_LEFT_KEY_WORD) {
            if (key.endsWith(s)) {
                subKey = key.substring(0, key.length() - s.length());
                rangeKeyWordList = RANGE_LEFT_KEY_WORD;
                break;
            }
        }
        for (String s : RANGE_RIGHT_KEY_WORD) {
            if (key.endsWith(s)) {
                subKey = key.substring(0, key.length() - s.length());
                rangeKeyWordList = RANGE_RIGHT_KEY_WORD;
                break;
            }
        }
        // 写入
        if (subKey != null) {
            for (String s : rangeKeyWordList) {
                jsonObject.put(subKey + s, value);
            }
        }
    }

/*    public static void main(String[] args) {
        Map<String, String[]> map = new HashMap<>();
        // 构建map 数据：{"orderId":[""],"pageNo":["1"],"pageSize":["10"],"orderCreateTimeTo":["2024-05-23 00:00:00"],"orderCreateTimeFrom":["2024-05-22 00:00:00"]}
        map.put("orderId", new String[]{""});
        map.put("pageNo", new String[]{"1"});
        map.put("pageSize", new String[]{"10"});
        map.put("orderCreateTimeTo", new String[]{"2024-05-23 00:00:00"});
        map.put("orderCreateTimeFrom", new String[]{"2024-05-22 00:00:00"});
        DashBoardQueryDto convert = convert(map, DashBoardQueryDto.class, 1, 10);
        System.out.println(JSONObject.toJSONString(convert));
    }*/
}
