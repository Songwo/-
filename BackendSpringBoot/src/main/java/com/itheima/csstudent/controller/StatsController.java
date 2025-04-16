package com.itheima.csstudent.controller;

import com.itheima.csstudent.service.impl.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@CrossOrigin(origins = "*")
public class StatsController {

    @Autowired
    private StatsService statsService;

    @GetMapping
    public ResponseEntity<Map<String, Long>> getStats() {
        return ResponseEntity.ok(statsService.getStats());
    }

    @PostMapping("/visits")
    public ResponseEntity<Void> incrementVisits() {
        statsService.incrementVisits();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users")
    public ResponseEntity<Void> incrementUserCount() {
        statsService.incrementUserCount();
        return ResponseEntity.ok().build();
    }
}