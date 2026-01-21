package it.unimol.taxManager.service;

import it.unimol.taxManager.dto.PagoPaDetailsDTO;
import it.unimol.taxManager.model.Student;
import it.unimol.taxManager.model.Tax;
import it.unimol.taxManager.repository.StudentRepository;
import it.unimol.taxManager.repository.TaxRepository;
import it.unimol.taxManager.util.StudentStatus;
import it.unimol.taxManager.util.TaxStatus;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class TaxService {
    TaxRepository taxRepository;
    StudentRepository studentRepository;

    public TaxService(TaxRepository taxRepository, StudentRepository studentRepository) {
        this.taxRepository = taxRepository;
        this.studentRepository = studentRepository;
    }

    public void registerPagoPaTax(String authToken, PagoPaDetailsDTO pagoPaDetailsDTO) {
        authToken = authToken.replace("Bearer ", "");
        if (!authToken.equals("codiceAutenticazioneConPagoPA")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Dati forniti non validi.");
        }

        Tax tax = taxRepository.findByPagoPaNoticeCode(pagoPaDetailsDTO.pagoPaNoticeCode());
        if (tax == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tassa non trovata.");
        }
        if (tax.getStatus() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tassa non trovata.");
        }
        tax.setStatus(pagoPaDetailsDTO.status());
        if (tax.setPaymentDate(pagoPaDetailsDTO.paymentDate())) {
            tax.setStatus(TaxStatus.PAID);
            taxRepository.save(tax);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data di pagamento non valida.");
        }

        Student student = studentRepository.findById(tax.getStudentId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Studente non presente nel sistema"));

        if (haveExpiredTaxes(student)) {
            student.setStato(StudentStatus.CONGELATO);
        } else if (haveAllPaidTaxes(student)) {
            student.setStato(StudentStatus.ATTIVO);
        } else {
            student.setStato(StudentStatus.ISCRITTO);
        }
        student.setStato(StudentStatus.ISCRITTO);
        studentRepository.save(student);

    }

    private boolean haveExpiredTaxes(Student student) {
        List<Tax> taxes = taxRepository.findTaxesByStudentId(student.getId());
        if (taxes.isEmpty()) {
            return false;
        }
        boolean expiredTaxesFound = false;
        for (Tax tax : taxes) {
            if (tax.getStatus() == TaxStatus.UNPAID || (tax.getExpirationDate().isBefore(LocalDate.now()) && tax.getStatus() == TaxStatus.PENDING)) {
                if (tax.getStatus() != TaxStatus.UNPAID) {
                    tax.setStatus(TaxStatus.UNPAID);
                    taxRepository.save(tax);
                }
                expiredTaxesFound = true;
            }
        }
        return expiredTaxesFound;
    }

    private boolean haveAllPaidTaxes(Student student) {
        List<Tax> taxes = taxRepository.findTaxesByStudentId(student.getId());
        if (taxes.isEmpty()) {
            return true;
        }
        for (Tax tax : taxes) {
            if (tax.getStatus() != TaxStatus.PAID) {
                return false;
            }
        }
        return true;
    }
}
