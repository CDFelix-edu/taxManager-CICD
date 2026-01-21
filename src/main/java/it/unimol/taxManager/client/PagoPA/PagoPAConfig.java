package it.unimol.taxManager.client.PagoPA;

import feign.codec.ErrorDecoder;
import it.unimol.taxManager.client.PagoPA.decoder.PagoPADecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PagoPAConfig {

    @Bean
    public ErrorDecoder errorDecoder2() {return new PagoPADecoder();
    }
}
