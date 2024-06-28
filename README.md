# 积木导出数据扩展

## 集成步骤

### 引入依赖

```xml

```

### 建表

    执行 CreateTable.sql 脚本，创建表格。
    (src/main/resources/init/CreateTable.sql)

### 实现上传接口

    在项目中实现 IJimuExportExtensionNeedImplMethodService

    接口中其他方法根据实际情况覆盖实现，无特殊要求则可不覆盖。

    若区分账户，每个账户只能看到自己的导出记录，则将token相关方法实现

### 报表导出接口加注解、参数

    在需要导出报表的接口上加上注解 @UseDownloadTaskCenter。
    在接口参数中添加类型为DownloadCenterBaseParam的实现类对象

### 前端对接接口实现下载中心界面

示范
![image-20240625155901292](https://chpic.oss-cn-shanghai.aliyuncs.com/202406251559645.png)

### 启动项目