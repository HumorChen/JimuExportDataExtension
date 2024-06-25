create table `report`.t_download_task(
id int unsigned auto_increment primary key comment 'ID',
account varchar(64) not null comment '提交任务的账号',
title varchar(256) not null comment '下载任务标题',
icon varchar(1024) comment '图标',
url varchar(1024)  comment '文件URL',
file_size varchar(16) comment '文件大小',
percent varchar(16)  comment '进度（例如50%）',
state tinyint default 0 comment '任务状态（0 等待执行，1执行中，2执行成功，3执行失败）',
error varchar(1024) comment '执行报错信息（有则填）',
json varchar(4096) not null default '{}' comment '预留的json扩展字段',
create_time datetime default current_timestamp not null comment '创建时间',
update_time datetime default current_timestamp on update current_timestamp not null comment '更新时间',
index idx_account_create_time(account,create_time),
index idx_create_time(create_time)
)comment '积木报表下载中心的任务';