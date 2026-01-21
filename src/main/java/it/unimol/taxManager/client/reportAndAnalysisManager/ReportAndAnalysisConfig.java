package it.unimol.taxManager.client.reportAndAnalysisManager;

import feign.codec.ErrorDecoder;
import it.unimol.taxManager.client.reportAndAnalysisManager.decoder.ReportAndAnalysisDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReportAndAnalysisConfig {
    @Bean
    public ErrorDecoder errorAnalysisAndReportDecoder() {
        return new ReportAndAnalysisDecoder();
    }
}