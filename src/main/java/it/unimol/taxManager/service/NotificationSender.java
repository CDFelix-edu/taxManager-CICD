package it.unimol.taxManager.service;

import it.unimol.taxManager.client.gestioneUtenti.GestioneUtentiClient;
import it.unimol.taxManager.dto.LoginRequestDTO;
import it.unimol.taxManager.dto.StudentDetailsDTO;
import it.unimol.taxManager.dto.TokenJWTDTO;
import it.unimol.taxManager.model.Student;
import it.unimol.taxManager.model.Tax;
import it.unimol.taxManager.repository.StudentRepository;
import it.unimol.taxManager.repository.TaxRepository;
import it.unimol.taxManager.util.StudentStatus;
import it.unimol.taxManager.util.TaxStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class NotificationSender {
    private final StudentRepository studentRepository;
    private final GestioneUtentiClient gestioneUtentiClient;
    private final TaxRepository taxRepository;
    private final JavaMailSender mailSender;

    public NotificationSender(StudentRepository studentRepository, TaxRepository taxRepository, JavaMailSender mailSender,
                              GestioneUtentiClient gestioneUtentiClient) {
        this.studentRepository = studentRepository;
        this.taxRepository = taxRepository;
        this.mailSender = mailSender;
        this.gestioneUtentiClient = gestioneUtentiClient;
    }


    @Scheduled(cron = "0 0 0 * * Mon")
    public void sendNotification() {
        List<Student> students = studentRepository.findAll();
        TokenJWTDTO token = gestioneUtentiClient.login(new LoginRequestDTO("username", "password"));
        SimpleMailMessage message = new SimpleMailMessage();
        for (Student student : students) {
            if (!student.getStato().equals(StudentStatus.COMPLETATO) && !student.getStato().equals(StudentStatus.ATTIVO) && !student.getStato().equals(StudentStatus.CESSATO)) {
                List<Tax> studentTaxes = taxRepository.findTaxesByStudent_Id(student.getId());
                for (Tax tax : studentTaxes) {
                    if (tax.getStatus().equals(TaxStatus.PENDING) && tax.getExpirationDate().isBefore(LocalDate.now().plusDays(8))) {
                        StudentDetailsDTO studentDetailsDTO = gestioneUtentiClient.getStudentById(token.token(), student.getId());
                        System.out.println("La tassa n° " + tax.getId() + " dello studente " + student.getId() + " con email: " + studentDetailsDTO.email() + " sta per scadere\n");
                        message.setTo(studentDetailsDTO.email());
                        message.setSubject("Tassa in scadenza");
                        message.setText("La tassa n° " + tax.getId() + " scadrà la prossima settimana");
                        message.setFrom("mail.unimol.it");
                        mailSender.send(message);
                    }
                }
            }
        }
    }
}
