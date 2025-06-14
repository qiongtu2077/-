package com.example.forum.controller;

import com.example.forum.service.AdminStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
@CrossOrigin
@RestController
@RequestMapping("/admin")
public class AdminStatsController {

    @Autowired
    private AdminStatsService adminStatsService;

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        return adminStatsService.getStats();
    }
}
