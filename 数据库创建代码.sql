-- 创建数据库
create database forumsystem;
go
use forumsystem;
go

-- 1. 用户表 users
create table users (
    uid int identity(1,1) primary key,
    nickname nvarchar(50),
    gender nvarchar(10),
    age int,
    job nvarchar(50),
    hobby nvarchar(100),
    password nvarchar(100),
    role nvarchar(20) default 'user',    -- 用户角色，默认 user
    status bit default 1,                -- 1 正常，0 封禁/逻辑删除
    create_time datetime default getdate()
);

-- 2. 帖子表 posts
create table posts (
    post_id int identity(1,1) primary key,
    title nvarchar(200),
    content nvarchar(max),
    post_time datetime default getdate(),
    is_pinned bit default 0,              -- 是否置顶
    uid int foreign key references users(uid),
    status bit default 1                  -- 1 正常，0 删除
);

-- 3. 回复表 replies
create table replies (
    reply_id int identity(1,1) primary key,
    content nvarchar(max),
    reply_time datetime default getdate(),
    post_id int foreign key references posts(post_id),
    uid int foreign key references users(uid),
    status bit default 1                  -- 1 正常，0 删除
);

-- 4. 敏感词表 sensitive_words
create table sensitive_words (
    id int identity(1,1) primary key,
    word nvarchar(100) not null
);
