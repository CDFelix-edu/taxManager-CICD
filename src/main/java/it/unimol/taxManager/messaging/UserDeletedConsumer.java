package it.unimol.taxManager.messaging;

import it.unimol.taxManager.model.Student;
import it.unimol.taxManager.model.Tax;
import it.unimol.taxManager.repository.StudentRepository;
import it.unimol.taxManager.repository.TaxRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class UserDeletedConsumer {

    private final StudentRepository studentRepository;
    private final TaxRepository taxRepository;

    public UserDeletedConsumer(StudentRepository studentRepository, TaxRepository taxRepository) {
        this.studentRepository = studentRepository;
        this.taxRepository = taxRepository;
    }

    @RabbitListener(queues = "user.deleted")
    public void handleUserDeleted(Map<String, Object> raw) {
        System.out.println("RICEZIONE: " + raw);
        if (!raw.isEmpty() && raw.get("eventType").equals("USER_DELETED")) {
            System.out.println("🔴 Nuovo utente ricevuto ed ELIMINATO: " + raw.get("eventType").toString() + " con ID: " + raw.get("userId").toString());
            taxRepository.deleteAll(taxRepository.findTaxesByStudent_Id(raw.get("userId").toString()));
            studentRepository.deleteById(raw.get("userId").toString());
        }
    }
}
