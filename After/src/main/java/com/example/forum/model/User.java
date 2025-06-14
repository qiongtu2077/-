package com.example.forum.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users") // 对应数据库表 users
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 对应 uid 自增
    @Column(name = "uid") // 数据库字段名为 uid
    private Integer uid;

    private String username;
    private String password; // ✅ 补上 password 字段
    private String nickname;
    private String gender;
    private Integer age;
    private String job;
    private String hobby;
    private Integer status; // 1 正常，0 删除（SQL Server 的 bit 映射为 Integer）

    // ========== Getter / Setter 方法 ==========
    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getHobby() {
        return hobby;
    }

    public void setHobby(String hobby) {
        this.hobby = hobby;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
