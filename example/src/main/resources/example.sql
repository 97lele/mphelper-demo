use demo;


drop table if exists order_info;
create table if not exists order_info
(
    order_id bigint primary key not null comment '订单主键',
    user_id bigint not null comment '用户id',
    create_time datetime not null comment '创建时间',
    update_time datetime null default current_timestamp comment '最后修改时间',
    user_name varchar (64) null comment '用户名称',
    total_amount decimal(18,6) null comment '订单金额'
    ) engine = innodb charset = utf8mb4;

drop table if exists order_detail;
create table if not exists order_detail
(
    detail_id     bigint         not null primary key comment '主键',
    order_id      bigint         not null comment '订单id',
    product_name  varchar(64)    not null comment '商品名称',
    unit_price    decimal(18, 6) not null comment '单价',
    product_count int            not null comment '商品数量',
    create_time   datetime       not null comment '创建时间',
    update_time   datetime       null default current_timestamp comment '最后修改时间'
    ) engine = innodb charset = utf8mb4;