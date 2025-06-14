package com.example.forum.service;

import java.util.List;

// 敏感词服务接口
public interface SensitiveWordService {
    // 查询全部敏感词
    List<String> getAllWords();

    // 检查文本是否包含敏感词，包含返回true
    boolean containsSensitive(String text);
}
