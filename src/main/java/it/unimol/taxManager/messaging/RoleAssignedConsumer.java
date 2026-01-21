package it.unimol.taxManager.messaging;

import it.unimol.taxManager.model.Student;
import it.unimol.taxManager.repository.StudentRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RoleAssignedConsumer {
    private final StudentRepository studentRepository;

    public RoleAssignedConsumer(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @RabbitListener(queues = "role.assigned")
    public void handleRoleAssigned(Map<String, Object> raw) {
        if (raw.get("roleId").equals("student")) {
            Student student = new Student(raw.get("userId").toString());
            System.out.println("🟢 Nuovo utente ricevuto con ruolo nuovo e registrato: " + raw.get("eventType").toString() + " con ID: " + raw.get("userId").toString());
            studentRepository.save(student);
        }
    }
}
