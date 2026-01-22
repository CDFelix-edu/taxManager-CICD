package it.unimol.taxManager.service;

import it.unimol.taxManager.dto.TaxDTO;
import it.unimol.taxManager.model.Student;
import it.unimol.taxManager.model.Tax;
import it.unimol.taxManager.repository.StudentRepository;
import it.unimol.taxManager.repository.TaxRepository;
import it.unimol.taxManager.client.gestioneUtenti.GestioneUtentiClient;
import it.unimol.taxManager.client.reportAndAnalysisManager.ReportAndAnalysisClient;
import it.unimol.taxManager.util.FlyingSaucerService;
import it.unimol.taxManager.util.JWTToken;
import it.unimol.taxManager.util.TaxStatus;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private JWTToken tokenManager;

    @Mock
    private TaxRepository taxRepository;

    @Mock
    private ReportAndAnalysisClient reportAndAnalysisClient;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private GestioneUtentiClient gestioneUtentiClient;

    @Mock
    private FlyingSaucerService flyingSaucerService;

    @InjectMocks
    private StudentService studentService;

    @Test
    void testGetStudentTaxes_returnsList() {
        String token = "Bearer validToken";
        String studentId = "123456";

        when(tokenManager.isTokenValid("validToken")).thenReturn(true);
        when(tokenManager.extractRole("validToken")).thenReturn("admin");

        Student student = mock(Student.class);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));

        Tax tax = mock(Tax.class);
        when(tax.getId()).thenReturn("1");
        when(tax.getStudentId()).thenReturn(studentId);
        when(tax.getPagoPaNoticeCode()).thenReturn("ABC123");
        when(tax.getAmount()).thenReturn(100.0);

        when(taxRepository.findTaxesByStudentId(studentId)).thenReturn(List.of(tax));

        List<TaxDTO> result = studentService.getStudentTaxes(token, studentId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("1", result.get(0).id());
        assertEquals("ABC123", result.get(0).pagoPaNoticeCode());
        assertEquals(100.0, result.get(0).amount());
        assertEquals(studentId, result.get(0).studentId());

        verify(taxRepository).findTaxesByStudentId(studentId);
    }



}
