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

    @InjectMocks
    private AdminService adminService;

    private final String VALID_TOKEN = "Bearer VALID";
    private final String RAW_TOKEN = "VALID";

    // ---------------------------------------------------------
    // updateDatabase
    // ---------------------------------------------------------

    //test che verifica che vengano aggiunti nuovi studenti
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

    //test che verifica che non vengano aggiunti studenti esistenti
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

    // test che verifica che venga rifiutato un valore ISEE negativo
    @Test
    void updateStudentISEE_shouldRejectNegativeIsee() {

        when(tokenManager.isTokenValid(RAW_TOKEN)).thenReturn(true);
        when(tokenManager.extractRole(RAW_TOKEN)).thenReturn("admin");
        assertThrows(ResponseStatusException.class,
                () -> adminService.updateStudentISEE(VALID_TOKEN, "S1", new IseeUpdateDTO(-10)));
    }

    // test che verifica che venga lanciata un'eccezione quando lo studente non viene trovato
    @Test
    void updateStudentISEE_shouldThrowWhenStudentNotFound() {
        when(tokenManager.isTokenValid(RAW_TOKEN)).thenReturn(true);
        when(tokenManager.extractRole(RAW_TOKEN)).thenReturn("admin");
        when(studentRepository.findById("S1")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> adminService.updateStudentISEE(VALID_TOKEN, "S1", new IseeUpdateDTO(1000)));
    }

    // test che verifica che non venga effettuato l'aggiornamento se il valore ISEE è lo stesso
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

    // test che verifica che l'aggiornamento del valore ISEE venga effettuato correttamente
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

    // test che verifica che venga rifiutato un anno non valido
    @Test
    void insertSoglieIsee_shouldRejectInvalidYear() {
        when(tokenManager.isTokenValid(RAW_TOKEN)).thenReturn(true);
        when(tokenManager.extractRole(RAW_TOKEN)).thenReturn("admin");
        BracketsDTO dto = new BracketsDTO(1800, 100.0, 18000.0, 75, 25000.0, 50, 35000.0, 25, 50000.0, 0);

        assertThrows(ResponseStatusException.class,
                () -> adminService.insertSoglieIsee(VALID_TOKEN, dto));
    }

    // test che verifica che venga rifiutato un anno già esistente
    @Test
    void insertSoglieIsee_shouldRejectExistingYear() {
        when(tokenManager.isTokenValid(RAW_TOKEN)).thenReturn(true);
        when(tokenManager.extractRole(RAW_TOKEN)).thenReturn("admin");
        BracketsDTO dto = new BracketsDTO(2024, 100.0, 18000.0, 75, 25000.0, 50, 35000.0, 25, 50000.0, 0);

        when(sogliaRepository.existsByAnno(2024)).thenReturn(true);

        assertThrows(ResponseStatusException.class,
                () -> adminService.insertSoglieIsee(VALID_TOKEN, dto));
    }

    // test che verifica che venga rifiutato un importo base negativo
    @Test
    void insertSoglieIsee_shouldRejectNegativeImportoBase() {
        when(tokenManager.isTokenValid(RAW_TOKEN)).thenReturn(true);
        when(tokenManager.extractRole(RAW_TOKEN)).thenReturn("admin");
        when(sogliaRepository.existsByAnno(2024)).thenReturn(false);

        BracketsDTO dto = new BracketsDTO(
                2024,
                -1.0,          // importoBase negativo
                18000.0, 75,
                25000.0, 50,
                35000.0, 25,
                50000.0, 0
        );

        assertThrows(ResponseStatusException.class,
                () -> adminService.insertSoglieIsee(VALID_TOKEN, dto));
    }

    // test che verifica che vengano rifiutati valori di soglia non ordinati
    @Test
    void insertSoglieIsee_shouldRejectUnorderedThresholds() {
        when(tokenManager.isTokenValid(RAW_TOKEN)).thenReturn(true);
        when(tokenManager.extractRole(RAW_TOKEN)).thenReturn("admin");
        when(sogliaRepository.existsByAnno(2024)).thenReturn(false);

        BracketsDTO dto = new BracketsDTO(
                2024,
                100.0,
                30000.0, 75,
                20000.0, 50,   // soglia2 < soglia1 → errore
                35000.0, 25,
                50000.0, 0
        );

        assertThrows(ResponseStatusException.class,
                () -> adminService.insertSoglieIsee(VALID_TOKEN, dto));
    }

    // test che verifica che vengano rifiutati valori soglie negative
    @Test
    void insertSoglieIsee_shouldRejectNegativeThresholds() {
        when(tokenManager.isTokenValid(RAW_TOKEN)).thenReturn(true);
        when(tokenManager.extractRole(RAW_TOKEN)).thenReturn("admin");
        when(sogliaRepository.existsByAnno(2024)).thenReturn(false);

        BracketsDTO dto = new BracketsDTO(
                2024,
                100.0,
                -1.0, 75,      // soglia1 negativa
                25000.0, 50,
                35000.0, 25,
                50000.0, 0
        );

        assertThrows(ResponseStatusException.class,
                () -> adminService.insertSoglieIsee(VALID_TOKEN, dto));
    }

    //test che verifica che vengano rifiutati valori di sconto negativi
    @Test
    void insertSoglieIsee_shouldRejectNegativeDiscounts() {
        when(tokenManager.isTokenValid(RAW_TOKEN)).thenReturn(true);
        when(tokenManager.extractRole(RAW_TOKEN)).thenReturn("admin");
        when(sogliaRepository.existsByAnno(2024)).thenReturn(false);

        BracketsDTO dto = new BracketsDTO(
                2024,
                100.0,
                18000.0, 75,   // sconto1 negativo
                25000.0, 50,
                35000.0, 25,
                50000.0, -5
        );

        assertThrows(ResponseStatusException.class,
                () -> adminService.insertSoglieIsee(VALID_TOKEN, dto));
    }

    // test che verifica che vengano rifiutati valori di sconto non ordinati
    @Test
    void insertSoglieIsee_shouldRejectUnorderedDiscounts() {
        when(tokenManager.isTokenValid(RAW_TOKEN)).thenReturn(true);
        when(tokenManager.extractRole(RAW_TOKEN)).thenReturn("admin");
        when(sogliaRepository.existsByAnno(2024)).thenReturn(false);

        BracketsDTO dto = new BracketsDTO(
                2024,
                100.0,
                18000.0, 50,
                25000.0, 60,   // sconto2 > sconto1 → errore
                35000.0, 25,
                50000.0, 0
        );

        assertThrows(ResponseStatusException.class,
                () -> adminService.insertSoglieIsee(VALID_TOKEN, dto));
    }


    // test che verifica che l'inserimento delle soglie ISEE venga effettuato correttamente
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

    // test che verifica che venga rifiutato un aggiornamento vuoto
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

    // test che verifica che non venga effettuato l'aggiornamento se non ci sono cambiamenti
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

    // test che verifica che l'aggiornamento delle soglie ISEE venga effettuato correttamente
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

    // test che verifica che vengano rifiutati valori di soglia non ordinati
    @Test
    void updateSoglieIsee_shouldRejectUnorderedThresholds() {
        when(tokenManager.isTokenValid(RAW_TOKEN)).thenReturn(true);
        when(tokenManager.extractRole(RAW_TOKEN)).thenReturn("admin");
        Brackets existing = new Brackets(2024, 3200.0, 18000.0, 75, 25000.0, 50, 35000.0, 25, 50000.0, 0);

        when(sogliaRepository.findByAnno(2024)).thenReturn(Optional.of(existing));

        UpdateBracketsDTO update = new UpdateBracketsDTO(
                3200.0,
                20000.0, 75,
                25000.0, 50,
                22000.0, 30,
                32000.0, 0
        );

        assertThrows(ResponseStatusException.class,
                () -> adminService.updateSoglieIsee(VALID_TOKEN, 2024, update));
    }

    // test che verifica che vengano rifiutati valori di sconto non ordinati
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
