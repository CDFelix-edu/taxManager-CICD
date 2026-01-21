package it.unimol.taxManager.client.PagoPA;

import it.unimol.taxManager.dto.PagoPATaxDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "pagoPa", url = "${microservizi.pagopa.base-url}", configuration = PagoPAConfig.class)
public interface PagoPAClient {
    @PostMapping("/api/v1/pagopa/registerTax")
    String registerTax(@RequestHeader("Authorization") String token, @RequestBody PagoPATaxDTO pagoPaDTO);
}
