package cn.humorchen.jimu.export.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;

/**
 * @author humorchen
 * date: 2024/2/27
 * description: 请求工具
 **/
@Slf4j
public class RequestUtil {
    /**
     * 获取当前请求
     *
     * @return
     */
    public static HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            return requestAttributes.getRequest();
        }
        return null;
    }

    /**
     * 获取请求body
     *
     * @param request
     * @return
     */
    public static String getRequestBody(HttpServletRequest request) {
        // 获取request这个请求的请求body字符串
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;

        try {
            bufferedReader = request.getReader();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (Exception e) {
            log.error("获取请求body异常:" + e.getMessage());
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception e) {
                    log.error("关闭bufferedReader异常:" + e.getMessage());
                }
            }
        }

        return stringBuilder.toString();
    }
}
