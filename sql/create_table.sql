create table user
(
    id            bigint auto_increment comment '用户id'
        primary key,
    user_name     varchar(256)                           null comment '用户昵称',
    user_account  varchar(256)                           null comment '用户账号',
    user_password varchar(512)                           null comment '用户密码',
    user_avatar   varchar(1024)                          null comment '用户头像',
    user_profile  varchar(512)                           null comment '用户简介',
    user_role     varchar(256) default 'user'            null comment '用户角色(0-普通用户,1-管理员)',
    is_delete     tinyint      default 0                 null comment '逻辑删除(1-删除)',
    edit_time     datetime     default CURRENT_TIMESTAMP null comment '编辑时间',
    create_time   datetime     default CURRENT_TIMESTAMP null comment '创建时间',
    update_time   datetime     default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_user_account unique (user_account),
    index idx_user_name (user_name)
) comment '用户表';