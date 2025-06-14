create database ForumSystem;
go
use ForumSystem;
go

-- 1. 用户表 users
create table users (
    uid int identity(1,1) primary key,
    nickname nvarchar(50),
    gender nvarchar(10),
    age int,
    job nvarchar(50),
    hobby nvarchar(100),
    status bit default 1  -- 1 表示正常，0 表示逻辑删除
);

-- 2. 帖子表 posts
create table posts (
    post_id int identity(1,1) primary key,
    post_time datetime default getdate(),
    is_pinned bit default 0,
    uid int foreign key references users(uid),
    content nvarchar(max),
    status bit default 1  -- 1 表示正常，0 表示逻辑删除
);

ALTER TABLE posts ADD title NVARCHAR(200);


-- 3. 回复表 replies
create table replies (
    reply_id int identity(1,1) primary key,
    reply_time datetime default getdate(),
    content nvarchar(max),
    post_id int foreign key references posts(post_id),
    uid int foreign key references users(uid),
    status bit default 1  -- 1 表示正常，0 表示逻辑删除
);
--敏感词表
create table sensitive_words (
  id int identity(1,1) primary key,
  word nvarchar(100) not null
)



-- 查询所有有效用户
select * from users where status = 0;

-- 查询所有有效帖子
select * from posts where status = 1;

-- 查询所有有效回复
select * from replies where status = 1;

--查询敏感词列表
select * from sensitive_words





select * from users
select * from posts 
select * from replies
select * from sensitive_words


-- 创建登录账户（数据库外层身份）
create login login_user with password = 'User123!';

-- 在当前数据库 ForumSystem 中创建用户身份
use ForumSystem;
create user login_user for login login_user;

-- 允许查询帖子、回复、用户信息
grant select on posts to login_user;
grant select on replies to login_user;
grant select on users to login_user;

-- 允许发帖、评论
grant insert on posts to login_user;
grant insert on replies to login_user;

--自动设置新用户的role为user
ALTER TABLE users
ADD CONSTRAINT DF_users_role DEFAULT 'user' FOR role;


--手动更新旧用户的role
UPDATE users
SET role = 'user'
WHERE role IS NULL;

--. 给 users 表加上 create_time 字段
ALTER TABLE users
ADD create_time DATETIME DEFAULT GETDATE();

--
UPDATE users
SET create_time = GETDATE()
WHERE create_time IS NULL;
UPDATE users
SET create_time = '2025-06-08 20:01:01.000'
WHERE uid = 9;


SELECT 
  COUNT(*) AS userTotal,
  SUM(CASE WHEN status=1 THEN 1 ELSE 0 END) AS userNormal,
  SUM(CASE WHEN status=0 THEN 1 ELSE 0 END) AS userBanned,
  SUM(CASE WHEN CONVERT(date, create_time) = CONVERT(date, GETDATE()) THEN 1 ELSE 0 END) AS userNewToday,
  SUM(CASE WHEN DATEPART(week, create_time) = DATEPART(week, GETDATE()) AND YEAR(create_time)=YEAR(GETDATE()) THEN 1 ELSE 0 END) AS userNewWeek,
  SUM(CASE WHEN MONTH(create_time) = MONTH(GETDATE()) AND YEAR(create_time)=YEAR(GETDATE()) THEN 1 ELSE 0 END) AS userNewMonth
FROM users
WHERE status IN (0,1);


