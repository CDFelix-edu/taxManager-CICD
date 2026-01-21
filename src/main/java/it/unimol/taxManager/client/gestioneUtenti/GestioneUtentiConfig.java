package it.unimol.taxManager.client.gestioneUtenti;

import feign.codec.ErrorDecoder;
import it.unimol.taxManager.client.gestioneUtenti.decoder.GestioneUtentiDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GestioneUtentiConfig {

    @Bean
    public ErrorDecoder errorDecoder(){
        return new GestioneUtentiDecoder();
    }
}
