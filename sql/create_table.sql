create table if not exists user
(
    id            bigint auto_increment comment '用户id' primary key,
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

create table if not exists picture
(
    id           bigint auto_increment comment '图片id' primary key,
    pic_name     varchar(128)                       not null comment '图片名称',
    pic_url      varchar(512)                       not null comment '图片url',
    pic_intro    varchar(512)                       null comment '图片简介',
    pic_category varchar(64)                        null comment '图片分类',
    pic_tags     varchar(512)                       null comment '图片标签（JSON 数组）',
    pic_size     bigint                             null comment '图片体积',
    pic_width    int                                null comment '图片宽度',
    pic_height   int                                null comment '图片高度',
    pic_scale    double                             null comment '图片宽高比例',
    pic_format   varchar(32)                        null comment '图片格式',
    user_id      bigint                             not null COMMENT '创建用户id',
    is_delete    tinyint  default 0                 null comment '逻辑删除(1-删除)',
    edit_time    datetime default CURRENT_TIMESTAMP null comment '编辑时间',
    create_time  datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    index idx_name (pic_name),          -- 提升基于图片名称的查询性能
    index idx_introduction (pic_intro), -- 用于模糊搜索图片简介
    index idx_category (pic_category),  -- 提升基于分类的查询性能
    index idx_tags (pic_tags),          -- 提升基于标签的查询性能
    index idx_user_id (user_id)         -- 提升基于用户 ID 的查询性能
) comment '图片表';