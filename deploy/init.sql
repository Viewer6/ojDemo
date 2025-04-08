# 创建管理员表
create table tb_sys_user (
                             user_id      bigint unsigned not null comment '用户id（主键）',
                             user_account varchar(20) not null  comment '账号',
                             nick_name    varchar(20) comment '昵称',
                             password     char(60) not null  comment '密码',
                             create_by    bigint unsigned not null  comment '创建人',
                             create_time  datetime not null comment '创建时间',
                             update_by    bigint unsigned  comment '更新人',
                             update_time  datetime comment '跟新时间',
                             primary key (`user_id`),
                             unique key `idx_user_account` (`user_account`)
);

# 这里不使用自增id的原因: 方便数据迁移和备份, 删除和插入操作不会浪费空间, 解决性能问题, 可预测性强, 解决分布式环境自增id唯一的问题