package it.unimol.taxManager.service;

import it.unimol.taxManager.client.gestioneUtenti.GestioneUtentiClient;
import it.unimol.taxManager.client.reportAndAnalysisManager.ReportAndAnalysisClient;
import it.unimol.taxManager.dto.PagoPaDetailsDTO;
import it.unimol.taxManager.model.Student;
import it.unimol.taxManager.model.Tax;
import it.unimol.taxManager.repository.StudentRepository;
import it.unimol.taxManager.repository.TaxRepository;
import it.unimol.taxManager.util.FlyingSaucerService;
import it.unimol.taxManager.util.JWTToken;
import it.unimol.taxManager.util.StudentStatus;
import it.unimol.taxManager.util.TaxStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaxServiceTest {

    @Mock
    private TaxRepository taxRepository;

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private TaxService taxService;

    private Student student;
    private Tax tax;
    private PagoPaDetailsDTO dto;

    @BeforeEach
    void setup() throws Exception {
        student = Mockito.mock(Student.class);

        tax = new Tax(student, 100.0, TaxStatus.PENDING, LocalDate.now().plusDays(5), "123");

        // imposto creationDate perché @PrePersist non viene chiamato nei test
        Field creationField = Tax.class.getDeclaredField("creationDate");
        creationField.setAccessible(true);
        creationField.set(tax, LocalDate.now());

        dto = new PagoPaDetailsDTO("123", TaxStatus.PAID, LocalDate.now());
    }

    @Test
    void shouldThrowUnauthorizedWhenTokenInvalid() {
        assertThrows(ResponseStatusException.class,
                () -> taxService.registerPagoPaTax("Bearer WRONG", dto));
    }

    @Test
    void shouldThrowNotFoundWhenTaxNotFound() {
        when(taxRepository.findByPagoPaNoticeCode("123")).thenReturn(null);

        assertThrows(ResponseStatusException.class,
                () -> taxService.registerPagoPaTax("Bearer codiceAutenticazioneConPagoPA", dto));
    }

    @Test
    void shouldThrowNotFoundWhenTaxStatusNull() throws Exception {
        Field statusField = Tax.class.getDeclaredField("status");
        statusField.setAccessible(true);
        statusField.set(tax, null);

        when(taxRepository.findByPagoPaNoticeCode("123")).thenReturn(tax);

        assertThrows(ResponseStatusException.class,
                () -> taxService.registerPagoPaTax("Bearer codiceAutenticazioneConPagoPA", dto));
    }

    @Test
    void shouldThrowBadRequestWhenPaymentDateInvalid() {
        when(taxRepository.findByPagoPaNoticeCode("123")).thenReturn(tax);

        // paymentDate prima della creationDate → false
        dto = new PagoPaDetailsDTO("123", TaxStatus.PAID, LocalDate.now().minusDays(10));

        assertThrows(ResponseStatusException.class,
                () -> taxService.registerPagoPaTax("Bearer codiceAutenticazioneConPagoPA", dto));
    }

    @Test
    void shouldSaveTaxWhenPaymentValid() {
        when(student.getId()).thenReturn("STU123");
        when(taxRepository.findByPagoPaNoticeCode("123")).thenReturn(tax);
        when(studentRepository.findById("STU123")).thenReturn(Optional.of(student));
        when(taxRepository.findTaxesByStudentId("STU123")).thenReturn(List.of(tax));

        taxService.registerPagoPaTax("Bearer codiceAutenticazioneConPagoPA", dto);

        verify(taxRepository).save(tax);
        verify(studentRepository).save(student);
    }

    @Test
    void shouldThrowWhenStudentNotFound() {
        when(student.getId()).thenReturn("STU123");
        when(taxRepository.findByPagoPaNoticeCode("123")).thenReturn(tax);
        when(studentRepository.findById("STU123")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> taxService.registerPagoPaTax("Bearer codiceAutenticazioneConPagoPA", dto));
    }

    @Test
    void shouldDetectExpiredTaxes() {
        Tax expired = new Tax(student, 50.0, TaxStatus.PENDING, LocalDate.now().minusDays(1), "999");

        // imposto creationDate anche per expired
        try {
            Field creationField = Tax.class.getDeclaredField("creationDate");
            creationField.setAccessible(true);
            creationField.set(expired, LocalDate.now().minusDays(5));
        } catch (Exception ignored) {}

        when(student.getId()).thenReturn("STU123");
        when(taxRepository.findByPagoPaNoticeCode("123")).thenReturn(tax);
        when(studentRepository.findById("STU123")).thenReturn(Optional.of(student));
        when(taxRepository.findTaxesByStudentId("STU123")).thenReturn(List.of(expired));

        taxService.registerPagoPaTax("Bearer codiceAutenticazioneConPagoPA", dto);

        verify(taxRepository).save(expired);
        verify(studentRepository).save(student);
    }

    @Test
    void shouldDetectAllPaidTaxes() {
        Tax t1 = new Tax(student, 10.0, TaxStatus.PAID, LocalDate.now(), "A");
        Tax t2 = new Tax(student, 20.0, TaxStatus.PAID, LocalDate.now(), "B");

        when(student.getId()).thenReturn("STU123");
        when(taxRepository.findByPagoPaNoticeCode("123")).thenReturn(tax);
        when(studentRepository.findById("STU123")).thenReturn(Optional.of(student));
        when(taxRepository.findTaxesByStudentId("STU123")).thenReturn(List.of(t1, t2));

        taxService.registerPagoPaTax("Bearer codiceAutenticazioneConPagoPA", dto);

        verify(studentRepository).save(student);
    }
}
