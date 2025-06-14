create database ForumSystem;
go
use ForumSystem;
go

-- 1. �û��� users
create table users (
    uid int identity(1,1) primary key,
    nickname nvarchar(50),
    gender nvarchar(10),
    age int,
    job nvarchar(50),
    hobby nvarchar(100),
    status bit default 1  -- 1 ��ʾ������0 ��ʾ�߼�ɾ��
);

-- 2. ���ӱ� posts
create table posts (
    post_id int identity(1,1) primary key,
    post_time datetime default getdate(),
    is_pinned bit default 0,
    uid int foreign key references users(uid),
    content nvarchar(max),
    status bit default 1  -- 1 ��ʾ������0 ��ʾ�߼�ɾ��
);

ALTER TABLE posts ADD title NVARCHAR(200);


-- 3. �ظ��� replies
create table replies (
    reply_id int identity(1,1) primary key,
    reply_time datetime default getdate(),
    content nvarchar(max),
    post_id int foreign key references posts(post_id),
    uid int foreign key references users(uid),
    status bit default 1  -- 1 ��ʾ������0 ��ʾ�߼�ɾ��
);
--���дʱ�
create table sensitive_words (
  id int identity(1,1) primary key,
  word nvarchar(100) not null
)



-- ��ѯ������Ч�û�
select * from users where status = 0;

-- ��ѯ������Ч����
select * from posts where status = 1;

-- ��ѯ������Ч�ظ�
select * from replies where status = 1;

--��ѯ���д��б�
select * from sensitive_words





select * from users
select * from posts 
select * from replies
select * from sensitive_words


-- ������¼�˻������ݿ������ݣ�
create login login_user with password = 'User123!';

-- �ڵ�ǰ���ݿ� ForumSystem �д����û����
use ForumSystem;
create user login_user for login login_user;

-- �����ѯ���ӡ��ظ����û���Ϣ
grant select on posts to login_user;
grant select on replies to login_user;
grant select on users to login_user;

-- ������������
grant insert on posts to login_user;
grant insert on replies to login_user;

--�Զ��������û���roleΪuser
ALTER TABLE users
ADD CONSTRAINT DF_users_role DEFAULT 'user' FOR role;


--�ֶ����¾��û���role
UPDATE users
SET role = 'user'
WHERE role IS NULL;

--. �� users ����� create_time �ֶ�
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


