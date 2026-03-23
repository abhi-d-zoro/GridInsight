package com.gridinsight.backend.ATMM_7.controller;

import com.gridinsight.backend.ATMM_7.dto.ThresholdRuleRequestDTO;
import com.gridinsight.backend.ATMM_7.entity.ThresholdRule;
import com.gridinsight.backend.ATMM_7.service.ThresholdRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/threshold-rules")
@RequiredArgsConstructor
public class ThresholdRuleController {

    private final ThresholdRuleService service;

    @PostMapping
    public ThresholdRule create(@Valid @RequestBody ThresholdRuleRequestDTO request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public ThresholdRule update(@PathVariable Long id,
                                @Valid @RequestBody ThresholdRuleRequestDTO request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @GetMapping("/{id}")
    public ThresholdRule getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping
    public List<ThresholdRule> getAll() {
        return service.getAll();
    }
}