package it.unimol.taxManager.client.reportAndAnalysisManager;

import it.unimol.taxManager.dto.StudentProgressDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "reportAndAnalysisManager", url = "${microservizi.reportistica.base-url}", configuration = ReportAndAnalysisConfig.class)
public interface ReportAndAnalysisClient {
    @GetMapping("/api/v1/reports/students/{studentId}/progress")
    StudentProgressDTO getStudentProgress(@RequestHeader("Authorization") String token, @PathVariable("studentId") String studentId);
}