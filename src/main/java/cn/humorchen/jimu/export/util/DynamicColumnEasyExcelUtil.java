package cn.humorchen.jimu.export.util;

import cn.humorchen.jimu.export.dto.JimuPageDto;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.StopWatch;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.style.column.AbstractHeadColumnWidthStyleStrategy;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author humorchen
 * date: 2024/3/5
 * description: 动态列easyexcel工具类
 **/
@Slf4j
public class DynamicColumnEasyExcelUtil {

    private static final int MAX_SIZE_PER_SHEET = 1048575;
    public static final String ROW = "=row()";

    public static interface DynamicColumnEasyExcelInterface<T> {
        /**
         * 分页获取数据
         *
         * @param page
         * @param size
         * @return
         */
        JimuPageDto<T> pageGetData(int page, int size);

        /**
         * 数据对象转换为字符串
         *
         * @param t
         * @return
         */
        List<String> mapDataToStringList(T t);

        /**
         * 分页获取数据加载第i页时触发函数，用于实现进度变更
         *
         * @param pageNo
         * @param pageSize
         */
        void onLoadedPage(int pageNo, int pageSize, int pages);
    }

    /**
     * 从数据库分页读数据并写入成Excel文件，把文件内容写到输出流
     *
     * @param head
     * @param dynamicColumnEasyExcelInterface
     * @param pageSize
     * @param <T>
     * @return
     */
    public static <T> ByteArrayInputStream writePageData(List<List<String>> head, DynamicColumnEasyExcelInterface<T> dynamicColumnEasyExcelInterface, int pageSize) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ExcelWriter excelWriter = EasyExcel.write(outputStream).head(head).build();
        // 当前分页
        int currentPage = 1;
        // 总页数
        long pages = 1;
        // 当前写入的sheet页
        int sheetNo = 1;
        // 写入计数（用于自动翻sheet页）
        int writeRowCount = 0;
        // 积木序号编号
        int index = 1;
        StopWatch stopWatch = new StopWatch("报表分页写入Excel");
        WriteSheet sheet = getWriteSheet(sheetNo);

        do {
            // 加载数据
            stopWatch.start("加载第" + currentPage + "页数据");
            JimuPageDto<T> jimuPageDto = dynamicColumnEasyExcelInterface.pageGetData(currentPage, pageSize);
            stopWatch.stop();
            // 数据判空
            List<T> records = jimuPageDto.getData();
            if (CollectionUtil.isEmpty(records)) {
                break;
            }
            // 转换数据
            stopWatch.start("转换第" + currentPage + "页数据");
            List<List<String>> data = records.stream().map(dynamicColumnEasyExcelInterface::mapDataToStringList).collect(Collectors.toList());
            stopWatch.stop();
            // 处理序号 row()
            if (CollectionUtil.isNotEmpty(data) && CollectionUtil.isNotEmpty(data.get(0)) && ROW.equals(data.get(0).get(0))) {
                for (List<String> stringList : data) {
                    if (CollectionUtil.isNotEmpty(stringList) && ROW.equals(stringList.get(0))) {
                        stringList.set(0, String.valueOf(index));
                        ++index;
                    }
                }
            }
            // 自动跳sheet页
            if (writeRowCount + data.size() >= MAX_SIZE_PER_SHEET) {
                ++sheetNo;
                writeRowCount = 0;
                index = 1;
                sheet = getWriteSheet(sheetNo);
            }
            // 写入数据
            stopWatch.start("写入第" + currentPage + "页数据（" + data.size() + "条数据）");
            excelWriter.write(data, sheet);
            stopWatch.stop();

            pages = jimuPageDto.getTotal();
            // 更新进度
            dynamicColumnEasyExcelInterface.onLoadedPage(currentPage, pageSize, (int) pages);

            log.info("【下载中心】 分页获取数据，第{}页，总页数：{} 第一行数据是：{}", currentPage, pages, data.get(0));
            // 自增
            currentPage++;
            writeRowCount += data.size();
        } while (currentPage <= pages);
        log.info("【下载中心】 耗时打印");
        for (StopWatch.TaskInfo taskInfo : stopWatch.getTaskInfo()) {
            log.info("【下载中心】 {} 耗时：{} ms", taskInfo.getTaskName(), taskInfo.getTimeMillis());
        }
        excelWriter.finish();


        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    /**
     * 生成WriteSheet对象
     *
     * @param sheetNo 1开始
     * @return WriteSheet
     */

    private static WriteSheet getWriteSheet(int sheetNo) {
        WriteSheet sheet = new WriteSheet();
        sheet.setSheetName("sheet" + sheetNo);
        sheet.setSheetNo(sheetNo - 1);
        return sheet;
    }

    /**
     * 获取字段宽度策略
     *
     * @return
     */
    private AbstractHeadColumnWidthStyleStrategy getAbstractColumnWidthStyleStrategy() {
        return new AbstractHeadColumnWidthStyleStrategy() {
            /**
             * Returns the column width corresponding to each column head.
             *
             * <p>
             * if return null, ignore
             *
             * @param head        Nullable.
             * @param columnIndex Not null.
             * @return
             */
            @Override
            protected Integer columnWidth(Head head, Integer columnIndex) {
                return null;
            }
        };
    }

}

