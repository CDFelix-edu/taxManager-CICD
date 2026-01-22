package it.unimol.taxManager.service;

import it.unimol.taxManager.client.PagoPA.PagoPAClient;
import it.unimol.taxManager.client.gestioneUtenti.GestioneUtentiClient;
import it.unimol.taxManager.client.reportAndAnalysisManager.ReportAndAnalysisClient;
import it.unimol.taxManager.dto.*;
import it.unimol.taxManager.exception.JwtTokenException;
import it.unimol.taxManager.model.Brackets;
import it.unimol.taxManager.model.Student;
import it.unimol.taxManager.repository.SogliaRepository;
import it.unimol.taxManager.repository.StudentRepository;
import it.unimol.taxManager.repository.TaxRepository;
import it.unimol.taxManager.util.JWTToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private GestioneUtentiClient gestioneUtentiClient;

    @Mock
    private SogliaRepository sogliaRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private JWTToken tokenManager;

    @Mock
    private TaxRepository taxRepository;

    @Mock
    private PagoPAClient pagoPAClient;

    @Mock
    private ReportAndAnalysisClient reportAndAnalysisClient;

    @InjectMocks
    private AdminService adminService;

    private final String VALID_TOKEN = "Bearer VALID";
    private final String RAW_TOKEN = "VALID";

    // ---------------------------------------------------------
    // updateDatabase
    // ---------------------------------------------------------

    @Test
    void updateDatabase_shouldAddNewStudents() {
        StudentDTO s1 = new StudentDTO("S1", "student");
        StudentDTO s2 = new StudentDTO("S2", "student");

        when(tokenManager.isTokenValid(RAW_TOKEN)).thenReturn(true);
        when(tokenManager.extractRole(RAW_TOKEN)).thenReturn("admin");

        when(gestioneUtentiClient.getStudentList(VALID_TOKEN))
                .thenReturn(List.of(s1, s2));

        when(studentRepository.findById("S1")).thenReturn(Optional.empty());
        when(studentRepository.findById("S2")).thenReturn(Optional.empty());

        UpdateDTO result = adminService.updateDatabase(VALID_TOKEN);

        assertEquals(2, result.nuoviUtenti());
        verify(studentRepository, times(2)).save(any(Student.class));
    }

    @Test
    void updateDatabase_shouldNotAddExistingStudents() {
        StudentDTO s1 = new StudentDTO("S1", "student");

        when(tokenManager.isTokenValid(RAW_TOKEN)).thenReturn(true);
        when(tokenManager.extractRole(RAW_TOKEN)).thenReturn("admin");

        when(gestioneUtentiClient.getStudentList(VALID_TOKEN))
                .thenReturn(List.of(s1));

        when(studentRepository.findById("S1")).thenReturn(Optional.of(new Student("S1")));

        UpdateDTO result = adminService.updateDatabase(VALID_TOKEN);

        assertEquals(0, result.nuoviUtenti());
    }

    // ---------------------------------------------------------
    // updateStudentISEE
    // ---------------------------------------------------------

    @Test
    void updateStudentISEE_shouldRejectNegativeIsee() {

        when(tokenManager.isTokenValid(RAW_TOKEN)).thenReturn(true);
        when(tokenManager.extractRole(RAW_TOKEN)).thenReturn("admin");
        assertThrows(ResponseStatusException.class,
                () -> adminService.updateStudentISEE(VALID_TOKEN, "S1", new IseeUpdateDTO(-10)));
    }

    @Test
    void updateStudentISEE_shouldThrowWhenStudentNotFound() {
        when(tokenManager.isTokenValid(RAW_TOKEN)).thenReturn(true);
        when(tokenManager.extractRole(RAW_TOKEN)).thenReturn("admin");
        when(studentRepository.findById("S1")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> adminService.updateStudentISEE(VALID_TOKEN, "S1", new IseeUpdateDTO(1000)));
    }

    @Test
    void updateStudentISEE_shouldReturnFalseWhenSameValue() {
        when(tokenManager.isTokenValid(RAW_TOKEN)).thenReturn(true);
        when(tokenManager.extractRole(RAW_TOKEN)).thenReturn("admin");
        Student s = new Student("S1");
        s.setISEE(2000);

        when(studentRepository.findById("S1")).thenReturn(Optional.of(s));

        boolean result = adminService.updateStudentISEE(VALID_TOKEN, "S1", new IseeUpdateDTO(2000));

        assertFalse(result);
    }

    @Test
    void updateStudentISEE_shouldUpdateValue() {
        when(tokenManager.isTokenValid(RAW_TOKEN)).thenReturn(true);
        when(tokenManager.extractRole(RAW_TOKEN)).thenReturn("admin");
        Student s = new Student("S1");
        s.setISEE(1000);

        when(studentRepository.findById("S1")).thenReturn(Optional.of(s));

        boolean result = adminService.updateStudentISEE(VALID_TOKEN, "S1", new IseeUpdateDTO(1500));

        assertTrue(result);
        verify(studentRepository).save(s);
    }

    // ---------------------------------------------------------
    // insertSoglieIsee
    // ---------------------------------------------------------

    @Test
    void insertSoglieIsee_shouldRejectInvalidYear() {
        when(tokenManager.isTokenValid(RAW_TOKEN)).thenReturn(true);
        when(tokenManager.extractRole(RAW_TOKEN)).thenReturn("admin");
        BracketsDTO dto = new BracketsDTO(1800, 100.0, 18000.0, 75, 25000.0, 50, 35000.0, 25, 50000.0, 0);

        assertThrows(ResponseStatusException.class,
                () -> adminService.insertSoglieIsee(VALID_TOKEN, dto));
    }

    @Test
    void insertSoglieIsee_shouldRejectExistingYear() {
        when(tokenManager.isTokenValid(RAW_TOKEN)).thenReturn(true);
        when(tokenManager.extractRole(RAW_TOKEN)).thenReturn("admin");
        BracketsDTO dto = new BracketsDTO(2024, 100.0, 18000.0, 75, 25000.0, 50, 35000.0, 25, 50000.0, 0);

        when(sogliaRepository.existsByAnno(2024)).thenReturn(true);

        assertThrows(ResponseStatusException.class,
                () -> adminService.insertSoglieIsee(VALID_TOKEN, dto));
    }

    @Test
    void insertSoglieIsee_shouldInsertCorrectly() {
        when(tokenManager.isTokenValid(RAW_TOKEN)).thenReturn(true);
        when(tokenManager.extractRole(RAW_TOKEN)).thenReturn("admin");
        BracketsDTO dto = new BracketsDTO(2024, 100.0, 18000.0, 75, 25000.0, 50, 35000.0, 25, 50000.0, 0);

        when(sogliaRepository.existsByAnno(2024)).thenReturn(false);

        boolean result = adminService.insertSoglieIsee(VALID_TOKEN, dto);

        assertTrue(result);
        verify(sogliaRepository).save(any(Brackets.class));
    }

    // ---------------------------------------------------------
    // updateSoglieIsee
    // ---------------------------------------------------------

    @Test
    void updateSoglieIsee_shouldRejectEmptyUpdate() {
        when(tokenManager.isTokenValid(RAW_TOKEN)).thenReturn(true);
        when(tokenManager.extractRole(RAW_TOKEN)).thenReturn("admin");
        Brackets existing = new Brackets(2024, 3200.0, 18000.0, 75, 25000.0, 50, 35000.0, 25, 50000.0, 0);

        when(sogliaRepository.findByAnno(2024)).thenReturn(Optional.of(existing));

        UpdateBracketsDTO update = new UpdateBracketsDTO(null, null, null, null, null, null, null, null, null);

        assertThrows(ResponseStatusException.class,
                () -> adminService.updateSoglieIsee(VALID_TOKEN, 2024, update));
    }

    @Test
    void updateSoglieIsee_shouldReturnFalseWhenNoChanges() {
        when(tokenManager.isTokenValid(RAW_TOKEN)).thenReturn(true);
        when(tokenManager.extractRole(RAW_TOKEN)).thenReturn("admin");
        Brackets existing = new Brackets(2024, 3200.0, 18000.0, 75, 25000.0, 50, 35000.0, 25, 50000.0, 0);

        when(sogliaRepository.findByAnno(2024)).thenReturn(Optional.of(existing));

        UpdateBracketsDTO update = new UpdateBracketsDTO(
                3200.0, 18000.0, 75,
                25000.0, 50,
                35000.0, 25,
                50000.0, 0
        );

        boolean result = adminService.updateSoglieIsee(VALID_TOKEN, 2024, update);

        assertFalse(result);
    }

    @Test
    void updateSoglieIsee_shouldUpdateCorrectly() {
        when(tokenManager.isTokenValid(RAW_TOKEN)).thenReturn(true);
        when(tokenManager.extractRole(RAW_TOKEN)).thenReturn("admin");
        Brackets existing = new Brackets(2024, 3200.0, 18000.0, 75, 25000.0, 50, 35000.0, 25, 50000.0, 0);

        when(sogliaRepository.findByAnno(2024)).thenReturn(Optional.of(existing));

        UpdateBracketsDTO update = new UpdateBracketsDTO(
                3500.0, null, null,
                null, null,
                null, null,
                null, null
        );

        boolean result = adminService.updateSoglieIsee(VALID_TOKEN, 2024, update);

        assertTrue(result);
        verify(sogliaRepository).save(any(Brackets.class));
    }

    @Test
    void updateSoglieIsee_shouldRejectUnorderedThresholds() {
        when(tokenManager.isTokenValid(RAW_TOKEN)).thenReturn(true);
        when(tokenManager.extractRole(RAW_TOKEN)).thenReturn("admin");
        Brackets existing = new Brackets(2024, 3200.0, 18000.0, 75, 25000.0, 50, 35000.0, 25, 50000.0, 0);

        when(sogliaRepository.findByAnno(2024)).thenReturn(Optional.of(existing));

        UpdateBracketsDTO update = new UpdateBracketsDTO(
                null,
                30000.0, 75,
                20000.0, 50,
                null, null,
                null, null
        );

        assertThrows(ResponseStatusException.class,
                () -> adminService.updateSoglieIsee(VALID_TOKEN, 2024, update));
    }

    @Test
    void updateSoglieIsee_shouldRejectUnorderedDiscounts() {
        when(tokenManager.isTokenValid(RAW_TOKEN)).thenReturn(true);
        when(tokenManager.extractRole(RAW_TOKEN)).thenReturn("admin");
        Brackets existing = new Brackets(2024, 3200.0, 18000.0, 75, 25000.0, 50, 35000.0, 25, 50000.0, 0);

        when(sogliaRepository.findByAnno(2024)).thenReturn(Optional.of(existing));

        UpdateBracketsDTO update = new UpdateBracketsDTO(
                null,
                null, 10,
                null, 20,
                null, null,
                null, null
        );

        assertThrows(ResponseStatusException.class,
                () -> adminService.updateSoglieIsee(VALID_TOKEN, 2024, update));
    }

    // ---------------------------------------------------------
    // validateToken
    // ---------------------------------------------------------

    @Test
    void validateToken_shouldRejectInvalidToken() {
        when(tokenManager.isTokenValid("INVALID")).thenReturn(false);

        assertThrows(JwtTokenException.class,
                () -> adminService.updateDatabase("Bearer INVALID"));
    }

    @Test
    void validateToken_shouldRejectUnauthorizedRole() {
        assertThrows(JwtTokenException.class,
                () -> adminService.updateDatabase(VALID_TOKEN));
    }
}
