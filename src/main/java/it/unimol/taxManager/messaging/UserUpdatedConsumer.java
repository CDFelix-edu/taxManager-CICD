package it.unimol.taxManager.messaging;

import it.unimol.taxManager.model.Student;
import it.unimol.taxManager.repository.StudentRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class UserUpdatedConsumer {
    private final StudentRepository studentRepository;

    public UserUpdatedConsumer(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @RabbitListener(queues = "user.updated")
    public void handleUserUpdated(Map<String, Object> raw) {
        if (raw.get("roleId").equals("student") && !studentRepository.existsById(raw.get("userId").toString())) {
            Student student = new Student(raw.get("userId").toString());
            System.out.println("🟢 Nuovo utente ricevuto e registrato: " + raw.get("eventType").toString() + " con ID: " + raw.get("userId").toString());
            studentRepository.save(student);
        }
    }
}
