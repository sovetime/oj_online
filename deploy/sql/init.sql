
管理员端登录设计
账号 密码
表名小写  多个单词 下划线隔开  全部以tb开头
追踪数据创建、修改的历史寻找责任人
create table tb_sys_user (
     user_id      bigint unsigned not null comment '用户id（主键）',
     user_account varchar(20) not null  comment '账号',
     nick_name    varchar(20) comment '昵称',
     password     char(60) not null  comment '密码',
     create_by    bigint unsigned not null  comment '创建人',
     create_time  datetime not null comment '创建时间',
     update_by    bigint unsigned  comment '更新人',
     update_time  datetime comment '更新时间',
     primary key (`user_id`),
     unique key `idx_user_account` (`user_account`)
);

char varchar  区别：
char 定长 char(10)
varchar 动态开辟空间  varchar(10)

--题库管理

B端，列表功能、添加题目、编辑、删除
C端
