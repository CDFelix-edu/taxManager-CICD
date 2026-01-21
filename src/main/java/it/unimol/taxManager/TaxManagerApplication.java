package it.unimol.taxManager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableFeignClients//(basePackages = "it.unimol.taxManager.client")
public class TaxManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaxManagerApplication.class, args);
    }

}