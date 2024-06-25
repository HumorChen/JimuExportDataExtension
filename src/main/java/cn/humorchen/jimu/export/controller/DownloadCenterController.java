package cn.humorchen.jimu.export.controller;

import cn.humorchen.jimu.export.bean.DownloadTask;
import cn.humorchen.jimu.export.dto.PageListDownloadTaskDto;
import cn.humorchen.jimu.export.enums.DownloadTaskStateEnum;
import cn.humorchen.jimu.export.ret.Result;
import cn.humorchen.jimu.export.service.IDownloadTaskService;
import cn.humorchen.jimu.export.vo.DownloadTaskVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author humorchen
 * date: 2024/2/28
 * description: 下载中心
 **/
@RestController
@RequestMapping("/report/export/downloadCenter")
public class DownloadCenterController {
    @Autowired
    private IDownloadTaskService downloadTaskService;


    /**
     * 分页查下载任务
     *
     * @param pageListDownloadTaskDto
     * @return
     */
    @RequestMapping("/pageListDownloadTask")
    public Result<IPage<DownloadTaskVo>> pageListDownloadTask(PageListDownloadTaskDto pageListDownloadTaskDto) {
        IPage<DownloadTask> downloadTaskPages = downloadTaskService.pageListDownloadTask(pageListDownloadTaskDto);
        Page<DownloadTaskVo> downloadTaskVoPage = new Page<>();
        downloadTaskVoPage.setCurrent(downloadTaskPages.getCurrent());
        downloadTaskVoPage.setPages(downloadTaskPages.getPages());
        downloadTaskVoPage.setSize(downloadTaskPages.getSize());
        downloadTaskVoPage.setTotal(downloadTaskPages.getTotal());
        List<DownloadTaskVo> downloadTaskVos = downloadTaskPages.getRecords().stream().map(downloadTask -> {
            DownloadTaskVo downloadTaskVo = new DownloadTaskVo();
            BeanUtils.copyProperties(downloadTask, downloadTaskVo);
            downloadTaskVo.setStateStr(Optional.ofNullable(DownloadTaskStateEnum.of(downloadTask.getState())).orElse(DownloadTaskStateEnum.WAIT).getTitle());
            return downloadTaskVo;
        }).collect(Collectors.toList());
        downloadTaskVoPage.setRecords(downloadTaskVos);

        return Result.ok(downloadTaskVoPage);
    }

    /**
     * 删除下载任务
     *
     * @param taskId
     * @return
     */
    @RequestMapping("/deleteTask")
    public Result<String> deleteTask(Integer taskId) {
        return downloadTaskService.removeById(taskId) ? Result.ok("删除成功") : Result.fail("未找到该任务，请刷新后重试）");
    }

    /**
     * 重新执行下载任务
     *
     * @param taskId
     * @return
     */
    @RequestMapping("/rerunTask")
    public Result<String> rerunTask(Integer taskId) {
        downloadTaskService.rerunTask(taskId);
        return Result.ok("任务重新执行 发起成功");
    }
}
