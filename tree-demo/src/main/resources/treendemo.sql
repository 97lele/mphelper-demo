create database if not exists treedemo;
use treedemo;
drop table if exists adjacency_list;
create table if not exists adjacency_list
(
    node_id      bigint not null comment 'id',
    pid     bigint not null comment '父id',
    content varchar(128) comment '内容',
    primary key (node_id, pid)
);

drop table if exists nested_set;
create table if not exists nested_set
(
    node_id       bigint not null comment 'id',
    pid      bigint not null comment 'pid',
    root_id  bigint not null comment 'root_id',
    left_no  bigint not null comment '左顺序',
    content  varchar(128) comment '内容',
    right_no bigint not null comment '右顺序',
    depth    int    not null comment '深度',
    primary key (left_no, right_no, root_id)
);


drop table if exists path_node;
create table if not exists path_node
(
    node_id      bigint                   not null comment 'id',
    pid     bigint                   not null comment 'pid',
    path    varchar(300) primary key not null comment '全路径',
    content varchar(128) comment '内容',
    level   int                      not null comment '层级'
);


drop table if exists closure_table;
create table if not exists closure_table
(
    node_id      bigint not null comment 'id',
    pid     bigint not null comment 'pid',
    content varchar(128) comment '内容',
    primary key (node_id, pid)
);

drop table if exists closure_table_ref;
create table if not exists closure_table_ref
(
    root_id bigint     not null comment '根节点id',
    pid     bigint     not null comment '父节点',
    node_id bigint     not null comment '节点id',
    is_leaf tinyint(1) not null comment '是否叶子节点',
    depth    int        not null comment '深度'
);

