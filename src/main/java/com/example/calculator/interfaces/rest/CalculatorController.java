package com.example.calculator.interfaces.rest;

import com.example.calculator.application.command.CalculateExpressionCommand;
import com.example.calculator.application.result.CalculationView;
import com.example.calculator.application.service.CalculationApplicationService;
import com.example.calculator.interfaces.rest.dto.CalculateRequest;
import com.example.calculator.interfaces.rest.dto.CalculationResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 科学计算器 REST 接口。
 *
 * <p>接口层只负责 HTTP 协议转换，业务用例由应用服务执行。
 */
@RestController
@RequestMapping("/api/calculations")
public class CalculatorController {

    private final CalculationApplicationService applicationService;

    public CalculatorController(
            CalculationApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    /** 执行一次科学计算。 */
    @PostMapping(consumes = "application/json", produces = "application/json")
    public CalculationResponse calculate(
            @Valid @RequestBody CalculateRequest request) {
        CalculationView view = applicationService.calculate(
                new CalculateExpressionCommand(request.expression()));
        return CalculationResponse.from(view);
    }

    /** 查询当前进程内保留的计算历史。 */
    @GetMapping(produces = "application/json")
    public List<CalculationResponse> findHistory() {
        return applicationService.findHistory().stream()
                .map(CalculationResponse::from)
                .toList();
    }

    /** 清空当前进程内的计算历史。 */
    @DeleteMapping
    public ResponseEntity<Void> clearHistory() {
        applicationService.clearHistory();
        return ResponseEntity.noContent().build();
    }
}
