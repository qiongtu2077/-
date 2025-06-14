package com.example.forum.service.impl;

import com.example.forum.service.SensitiveWordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

// 敏感词服务实现
@Service
public class SensitiveWordServiceImpl implements SensitiveWordService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 获取所有敏感词
    @Override
    public List<String> getAllWords() {
        String sql = "SELECT word FROM sensitive_words";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    // 检查文本是否包含敏感词
    @Override
    public boolean containsSensitive(String text) {
        List<String> words = getAllWords();
        for (String word : words) {
            if (word != null && !word.isEmpty() && text.contains(word)) {
                return true;
            }
        }
        return false;
    }
}
