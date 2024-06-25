package cn.humorchen.jimu.export.event.handler;

import cn.humorchen.jimu.export.bean.DownloadTask;
import cn.humorchen.jimu.export.config.AsyncConfig;
import cn.humorchen.jimu.export.dto.JimuReportDataColumnDTO;
import cn.humorchen.jimu.export.dto.JimuReportDataSourceDTO;
import cn.humorchen.jimu.export.enums.DownloadTaskStateEnum;
import cn.humorchen.jimu.export.event.DownloadTaskPublishEvent;
import cn.humorchen.jimu.export.pojo.DownloadTaskJson;
import cn.humorchen.jimu.export.service.IDownloadTaskService;
import cn.humorchen.jimu.export.service.IJimuExportExtensionNeedImplMethodService;
import cn.humorchen.jimu.export.service.impl.JimuReportDynamicEasyExcelImpl;
import cn.humorchen.jimu.export.util.DynamicColumnEasyExcelUtil;
import cn.hutool.core.date.StopWatch;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author humorchen
 * date: 2024/1/16
 * description: 处理下载任务发布事件
 **/
@Component
@Slf4j
public class DownloadTaskPublishEventHandler implements ApplicationListener<DownloadTaskPublishEvent> {
    @Autowired
    private IDownloadTaskService downloadTaskService;
    @Autowired
    private IJimuExportExtensionNeedImplMethodService jimuExportExtensionNeedImplMethodService;


    /**
     * 注入jm report分页大小
     */
    @Value("${jeecg.jmreport.page-size-number:5000}")
    private int jmReportPageSizeNumber;


    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @Override
    @Async(AsyncConfig.ASYNC_THREAD_POOL)
    public void onApplicationEvent(DownloadTaskPublishEvent event) {
        Integer taskId = event.getTaskId();

        log.info("【下载中心】执行下载任务 taskId:{}", taskId);
        DownloadTask downloadTask = downloadTaskService.getById(taskId);
        if (downloadTask == null) {
            log.error("【下载中心】下载任务不存在，taskId:{}", taskId);
            return;
        }
        if (downloadTask.getState() == DownloadTaskStateEnum.RUNNING.getState()) {
            log.error("【下载中心】下载任务正在执行中，taskId:{}", taskId);
            return;
        }

        try {
            log.info("【下载中心】下载任务开始执行，taskId:{}", taskId);
            // 改状态到执行中
            DownloadTaskStateEnum downloadTaskStateEnum = Optional.ofNullable(DownloadTaskStateEnum.of(downloadTask.getState())).orElse(DownloadTaskStateEnum.WAIT);
            int compareAndSwapTaskState = downloadTaskService.compareAndSwapTaskState(taskId, downloadTaskStateEnum, DownloadTaskStateEnum.RUNNING);
            if (compareAndSwapTaskState < 1) {
                log.info("【下载中心】下载任务状态不对，taskId:{}, state:{}", taskId, downloadTaskStateEnum);
                return;
            }
            DownloadTaskJson downloadTaskJson = JSONObject.parseObject(downloadTask.getJson(), DownloadTaskJson.class);

            // 获取数据
            String requestBody = downloadTaskJson.getRequestBody();
            String requestToken = downloadTaskJson.getRequestToken();
            String reportId = downloadTaskService.getReportIdFromRequestBody(requestBody);
            String reportName = downloadTaskService.getReportNameByReportId(reportId);
            String requestParam = downloadTaskService.getRequestParamFromJson(downloadTask.getJson());
            JimuReportDataSourceDTO dataSourceDTO = downloadTaskService.getReportApiOrSqlByReportId(reportId);
            List<JimuReportDataColumnDTO> reportHead = downloadTaskService.getReportHead(reportId);
            // 打印上面拿到的数据
            log.info("reportId ：{} \n reportName：{} \n requestParam：{} \n requestBody:{}  \n dataSourceDTO：{} \n reportHead：{}", reportId, reportName, requestParam, requestBody, dataSourceDTO, reportHead);
            JimuReportDynamicEasyExcelImpl jimuReportDynamicEasyExcel = new JimuReportDynamicEasyExcelImpl(reportId, reportName, taskId, downloadTaskService, requestParam, requestToken, dataSourceDTO, reportHead);
            // 生成excel文件
            List<List<String>> head = reportHead.stream().map(d -> Collections.singletonList(d.getName())).collect(Collectors.toList());
            // 分页写数据
            InputStream inputStream = DynamicColumnEasyExcelUtil.writePageData(head, jimuReportDynamicEasyExcel, jmReportPageSizeNumber);


            // 上传excel文件到oss
            downloadTask.setPercent("100%");
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            String url = jimuExportExtensionNeedImplMethodService.uploadExcelToOss(downloadTask.getAccount(), downloadTask.getTitle(), inputStream);
            stopWatch.stop();
            log.info("【下载中心】上传文件到OSS，耗时：{} ms，uri：{}", stopWatch.getLastTaskTimeMillis(), url);
            // 更新任务信息
            downloadTask.setUrl(url);
            downloadTask.setState(DownloadTaskStateEnum.SUCCESS.getState());
            log.info("【下载中心】下载任务成功，taskId:{}，task：{}", taskId, downloadTask);
            boolean updated = downloadTaskService.updateById(downloadTask);
            log.info("【下载中心】下载任务更新结果，taskId:{}, updated:{}", taskId, updated);
        } catch (Exception e) {
            log.error("【下载中心】下载任务执行失败", e);
            // 更新任务信息
            downloadTask.setState(DownloadTaskStateEnum.FAILED.getState());
            downloadTask.setError("【下载中心】执行失败（" + e.getMessage() + "）");
            boolean updated = downloadTaskService.updateById(downloadTask);
            log.info("【下载中心】下载任务更新结果，taskId:{}, updated:{}", taskId, updated);
        } finally {
            log.info("【下载中心】下载任务 {} 执行完毕", taskId);
        }


    }


}
