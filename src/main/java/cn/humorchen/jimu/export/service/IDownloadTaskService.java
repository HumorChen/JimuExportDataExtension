package cn.humorchen.jimu.export.service;

import cn.humorchen.jimu.export.bean.DownloadTask;
import cn.humorchen.jimu.export.dto.JimuReportDataColumnDTO;
import cn.humorchen.jimu.export.dto.JimuReportDataSourceDTO;
import cn.humorchen.jimu.export.dto.PageListDownloadTaskDto;
import cn.humorchen.jimu.export.enums.DownloadTaskStateEnum;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import lombok.NonNull;

import java.util.List;

/**
 * <p>
 * 下载任务 服务类
 * </p>
 *
 * @author humorchen
 * @since 2024-01-05
 */
public interface IDownloadTaskService extends IService<DownloadTask> {
    /**
     * 注册任务
     *
     * @param downloadTask
     * @return
     */
    DownloadTask registerTask(@NonNull DownloadTask downloadTask);

    /**
     * 10秒内是否有相同任务未完成，不给再次注册下载任务
     *
     * @param account
     * @param requestBody
     * @return
     */
    boolean setSameTaskLock(String account, String requestBody);

    /**
     * 更新任务
     *
     * @param downloadTask
     * @return
     */
    int updateTaskById(@NonNull DownloadTask downloadTask);

    /**
     * 更新任务进度
     *
     * @param id
     * @param percent
     * @return
     */
    int changeTaskPercent(int id, @NonNull String percent);

    /**
     * 更新任务状态
     *
     * @param id
     * @param state
     * @return
     */
    int changeTaskState(int id, @NonNull DownloadTaskStateEnum state);

    /**
     * 更新任务状态
     *
     * @param id
     * @param expectState
     * @param targetState
     * @return
     */
    int compareAndSwapTaskState(int id, @NonNull DownloadTaskStateEnum expectState, @NonNull DownloadTaskStateEnum targetState);

    /**
     * 根据任务ID获取任务
     *
     * @param id
     * @return
     */
    DownloadTask getDownloadTaskById(int id);

    /**
     * 分页查下载任务
     *
     * @param pageListDownloadTaskDto
     * @return
     */
    IPage<DownloadTask> pageListDownloadTask(PageListDownloadTaskDto pageListDownloadTaskDto);

    /**
     * 重新执行下载任务
     *
     * @param taskId
     */
    void rerunTask(Integer taskId);

    /**
     * 根据报表ID获取报表名称
     *
     * @param reportId
     * @return
     */
    String getReportNameByReportId(String reportId);

    /**
     * 从请求体中获取报表ID
     *
     * @param requestBody
     * @return
     */
    String getReportIdFromRequestBody(String requestBody);

    /**
     * 根据报表ID获取报表API地址或者SQL
     *
     * @param reportId
     * @return
     */
    JimuReportDataSourceDTO getReportApiOrSqlByReportId(String reportId);

    /**
     * 获取积木报表的头
     *
     * @param reportId
     * @return
     */
    List<JimuReportDataColumnDTO> getReportHead(String reportId);

    /**
     * 从积木请求体中获取请求参数
     *
     * @param json
     * @return
     */
    String getRequestParamFromJson(String json);

    /**
     * 获取扩展的service
     *
     * @return
     */
    IJimuExportExtensionNeedImplMethodService getJimuExportExtensionNeedImplMethodService();
}
