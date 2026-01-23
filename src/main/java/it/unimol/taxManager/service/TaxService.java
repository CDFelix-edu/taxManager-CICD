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

    private final TaxRepository taxRepository;
    private final StudentRepository studentRepository;

    public TaxService(TaxRepository taxRepository, StudentRepository studentRepository) {
        this.taxRepository = taxRepository;
        this.studentRepository = studentRepository;
    }

    public void registerPagoPaTax(String authToken, PagoPaDetailsDTO pagoPaDetailsDTO) {

        validateAuthToken(authToken);

        Tax tax = taxRepository.findByPagoPaNoticeCode(pagoPaDetailsDTO.pagoPaNoticeCode());
        if (tax == null || tax.getStatus() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tassa non trovata.");
        }

        // Aggiorna stato e data pagamento
        if (!tax.setPaymentDate(pagoPaDetailsDTO.paymentDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data di pagamento non valida.");
        }

        tax.setStatus(TaxStatus.PAID);
        taxRepository.save(tax);

        Student student = studentRepository.findById(tax.getStudentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Studente non presente nel sistema"));

        updateStudentStatus(student);
        studentRepository.save(student);
    }

    private void validateAuthToken(String authToken) {
        String cleaned = authToken.replace("Bearer ", "");
        if (!"codiceAutenticazioneConPagoPA".equals(cleaned)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Dati forniti non validi.");
        }
    }

    private void updateStudentStatus(Student student) {
        if (haveExpiredTaxes(student)) {
            student.setStato(StudentStatus.CONGELATO);
        } else if (haveAllPaidTaxes(student)) {
            student.setStato(StudentStatus.ATTIVO);
        } else {
            student.setStato(StudentStatus.ISCRITTO);
        }
    }

    private boolean haveExpiredTaxes(Student student) {
        List<Tax> taxes = taxRepository.findTaxesByStudentId(student.getId());
        boolean expiredFound = false;

        for (Tax tax : taxes) {
            boolean isExpiredPending = tax.getStatus() == TaxStatus.PENDING &&
                    tax.getExpirationDate().isBefore(LocalDate.now());

            if (tax.getStatus() == TaxStatus.UNPAID || isExpiredPending) {
                if (tax.getStatus() != TaxStatus.UNPAID) {
                    tax.setStatus(TaxStatus.UNPAID);
                    taxRepository.save(tax);
                }
                expiredFound = true;
            }
        }
        return expiredFound;
    }

    private boolean haveAllPaidTaxes(Student student) {
        List<Tax> taxes = taxRepository.findTaxesByStudentId(student.getId());
        return taxes.isEmpty() || taxes.stream().allMatch(t -> t.getStatus() == TaxStatus.PAID);
    }
}
