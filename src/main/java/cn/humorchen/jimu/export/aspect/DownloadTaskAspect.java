package cn.humorchen.jimu.export.aspect;

import cn.humorchen.jimu.export.bean.DownloadTask;
import cn.humorchen.jimu.export.dto.JimuPageDto;
import cn.humorchen.jimu.export.dto.JimuReportDataColumnDTO;
import cn.humorchen.jimu.export.enums.DownloadTaskStateEnum;
import cn.humorchen.jimu.export.event.DownloadTaskPublishEvent;
import cn.humorchen.jimu.export.param.DownloadCenterBaseParam;
import cn.humorchen.jimu.export.pojo.DownloadTaskJson;
import cn.humorchen.jimu.export.service.IDownloadTaskService;
import cn.humorchen.jimu.export.service.IJimuExportExtensionNeedImplMethodService;
import cn.humorchen.jimu.export.util.DownloadCenterUtil;
import cn.humorchen.jimu.export.util.RequestUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author humorchen
 * date: 2024/1/15
 * description: 下载任务切面
 * 对加上了@UseDownloadTaskCenter注解的方法进行切面，使用下载任务中心代理掉，完成下载任务
 **/
@Aspect
@Component
@Slf4j
public class DownloadTaskAspect {
    @Autowired
    private IDownloadTaskService downloadTaskService;
    /**
     * 注入spring 事件发布器
     */
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private IJimuExportExtensionNeedImplMethodService jimuExportDataExtensionDataGetService;
    /**
     * 注入jm report分页大小
     */
    @Value("${jeecg.jmreport.page-size-number:5000}")
    private int jmReportPageSizeNumber;


    /**
     * 环绕通知
     *
     * @return
     */
    @Around("@annotation(cn.humorchen.jimu.export.annotation.UseDownloadTaskCenter))")
    @Order(50)
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("【下载中心】进入下载中心切面");
        // 是下载中心发的请求则直接执行分页数据
        if (DownloadCenterUtil.isDownloadCenterRequest()) {
            log.info("【下载中心】下载中心发的请求，直接执行分页数据");
            return joinPoint.proceed();
        }

        // 识别下载请求
        int pageNo = 1;
        int pageSize = 20;
        Object[] args = joinPoint.getArgs();
        if (args != null && args.length > 0) {
            DownloadCenterBaseParam downloadCenterBaseParam = null;
            // 找到参数
            for (Object arg : args) {
                if (arg instanceof DownloadCenterBaseParam) {
                    downloadCenterBaseParam = (DownloadCenterBaseParam) arg;
                    break;
                }
            }
            // 检查参数
            if (downloadCenterBaseParam != null) {
                pageNo = Optional.ofNullable(downloadCenterBaseParam.getPageNo()).orElse(pageNo);
                pageSize = Optional.ofNullable(downloadCenterBaseParam.getPageSize()).orElse(pageSize);
            }
            log.info("【下载中心】下载中心切面，downloadCenterBaseParam:{}", downloadCenterBaseParam);
            if (downloadCenterBaseParam != null) {
                Object target = joinPoint.getTarget();
                Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
                Class<?> returnType = method.getReturnType();
                // 返回值类型检查
                if (returnType.equals(JimuPageDto.class)) {
                    // 如果是导出请求，则使用下载任务中心代理掉
                    if (jimuExportDataExtensionDataGetService.isExportFirstPageRequest(pageNo, pageSize, jmReportPageSizeNumber)) {
                        // 如果是导出第一页请求，则使用下载任务中心代理掉
                        DownloadTask downloadTask = registerTask(downloadCenterBaseParam, target, method, args);

                        if (downloadTask == null || downloadTask.getId() == null) {
                            log.error("【下载中心】注册下载任务失败，任务信息：{}", downloadTask);
                            return joinPoint.proceed();
                        }
                        log.info("【下载中心】注册下载任务成功，任务信息：{}", downloadTask);
                        // 返回积木所需要的数据
                        JimuPageDto<JSONObject> jimuPageDto = new JimuPageDto<>();
                        jimuPageDto.setTotal(0);
                        jimuPageDto.setCount(0);
                        JSONObject jsonObject = new JSONObject();
                        String downloadTaskJsonStr = downloadTask.getJson();
                        DownloadTaskJson downloadTaskJson = JSONObject.parseObject(downloadTaskJsonStr, DownloadTaskJson.class);
                        String requestBody = downloadTaskJson.getRequestBody();
                        String reportId = downloadTaskService.getReportIdFromRequestBody(requestBody);
                        List<JimuReportDataColumnDTO> reportHead = downloadTaskService.getReportHead(reportId);
                        log.info("【下载中心】reportHead:{}", reportHead);
                        if (CollectionUtil.isNotEmpty(reportHead) && reportHead.size() > 1) {
                            String column = reportHead.get(1).getColumn();
                            jsonObject.put(column, "请前往报表中台-下载中心查看（任务ID " + downloadTask.getId() + "）");
                            log.info("【下载中心】返回数据：{}", jsonObject);
                        } else {
                            log.info("【下载中心】返回数据为空");
                        }
                        List<JSONObject> list = Collections.singletonList(jsonObject);
                        jimuPageDto.setData(list);
                        eventPublisher.publishEvent(new DownloadTaskPublishEvent(downloadTask.getId()));
                        return jimuPageDto;
                    } else {
                        log.info("【下载中心】不是导出请求，直接执行分页数据");
                    }
                } else {
                    log.error("【下载中心】返回值类型不是JimuPageDto，无法使用下载任务中心代理掉");
                }

            }

        }

        return joinPoint.proceed();
    }

    /**
     * 生成下载任务
     *
     * @param downloadTaskParam
     * @return
     */
    private DownloadTask registerTask(DownloadCenterBaseParam downloadTaskParam, Object proxyTarget, Method method, Object[] args) {
        String account = jimuExportDataExtensionDataGetService.getAccount();
        HttpServletRequest currentRequest = RequestUtil.getCurrentRequest();
        String requestBody = DownloadCenterUtil.getRequestBodyFromHeader(currentRequest);

        // 防止10秒内重复点击
        if (!downloadTaskService.setSameTaskLock(account, requestBody)) {
            log.error("【下载中心】10秒内重复点击，不给再次注册下载任务");
            return null;
        }

        String title = "导出-" + DateUtil.now().replace(" ", "_") + ".xlsx";
        try {
            title = downloadTaskService.getReportNameByReportId(downloadTaskService.getReportIdFromRequestBody(requestBody)) + title;
        } catch (Exception e) {
            log.error("【下载中心】获取报表名称失败", e);
        }
        DownloadTask downloadTask = new DownloadTask();
        downloadTask.setAccount(account);
        downloadTask.setTitle(title);
        downloadTask.setIcon("");
        downloadTask.setUrl("");
        downloadTask.setFileSize("");
        downloadTask.setPercent("0%");
        downloadTask.setState(DownloadTaskStateEnum.WAIT.getState());
        DownloadTaskJson downloadTaskJson = new DownloadTaskJson();
        // 拷贝最开始请求积木的token和requestBody，执行下载任务时需要
        downloadTaskJson.setRequestToken(jimuExportDataExtensionDataGetService.getToken(currentRequest));
        downloadTaskJson.setRequestBody(requestBody);
        downloadTaskJson.setProxyMethod(method.getName());
        if (args != null) {
            for (Object arg : args) {
                if (arg instanceof DownloadCenterBaseParam) {
                    downloadTaskJson.setParam((DownloadCenterBaseParam) arg);
                    break;
                }
            }
        }
        downloadTask.setJson(JSONObject.toJSONString(downloadTaskJson));

        downloadTask = downloadTaskService.registerTask(downloadTask);
        return downloadTask;
    }


}
