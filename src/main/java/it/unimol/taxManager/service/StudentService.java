package it.unimol.taxManager.service;

import it.unimol.taxManager.client.gestioneUtenti.GestioneUtentiClient;
import it.unimol.taxManager.client.reportAndAnalysisManager.ReportAndAnalysisClient;
import it.unimol.taxManager.exception.JwtTokenException;
import it.unimol.taxManager.model.Tax;
import it.unimol.taxManager.repository.StudentRepository;
import it.unimol.taxManager.repository.TaxRepository;
import it.unimol.taxManager.util.FlyingSaucerService;
import it.unimol.taxManager.util.JWTToken;
import it.unimol.taxManager.util.StudentStatus;
import it.unimol.taxManager.util.TaxStatus;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.NoSuchElementException;
import java.util.Objects;

//DTO
import it.unimol.taxManager.dto.StudentDetailsDTO;
import it.unimol.taxManager.dto.StudentProgressDTO;
import it.unimol.taxManager.dto.StudentStatusDTO;
import it.unimol.taxManager.dto.TaxDTO;
@Service
public class StudentService {

    private final JWTToken tokenManager;
    private final TaxRepository taxRepository;
    private final ReportAndAnalysisClient reportAndAnalysisClient;
    private final StudentRepository studentRepository;
    private final GestioneUtentiClient gestioneUtentiClient;
    private final FlyingSaucerService flyingSaucerService;


    public StudentService(JWTToken tokenManager, TaxRepository taxRepository,
                          ReportAndAnalysisClient reportAndAnalysisClient, StudentRepository studentRepository,
                          GestioneUtentiClient gestioneUtentiClient,
                          FlyingSaucerService flyingSaucerService) {
        this.tokenManager = tokenManager;
        this.taxRepository = taxRepository;
        this.reportAndAnalysisClient = reportAndAnalysisClient;
        this.studentRepository = studentRepository;
        this.gestioneUtentiClient = gestioneUtentiClient;
        this.flyingSaucerService = flyingSaucerService;
    }

    /**
     * Restituisce la lista delle tasse associate a uno studente specifico.
     *
     * @param token     Il token JWT dell'utente che effettua la richiesta.
     * @param studentId L'ID dello studente di cui si vogliono recuperare le tasse.
     * @return Una lista di oggetti TaxDTO che rappresentano le tasse dello studente.
     */
    public List<TaxDTO> getStudentTaxes(String token, String studentId) {
        //restituisce la lista di tutte le tasse dello studente specifico in formato testo (solo studente stesso o amministrativi)
        validateToken(token, studentId);

        checkStudentId(studentId);
        List<Tax> taxes = taxRepository.findTaxesByStudentId(studentId);
        List<TaxDTO> taxDTOs = new ArrayList<>();

        if (taxes == null || taxes.isEmpty()) {
            return new ArrayList<>(); // Restituisce una lista vuota se non ci sono tasse
        }
        for (Tax tax : taxes) {
            taxDTOs.add(new TaxDTO(
                    tax.getId(),
                    tax.getStudentId(),
                    tax.getPagoPaNoticeCode(),
                    tax.getAmount(),
                    tax.getStatus(),
                    tax.getExpirationDate(),
                    tax.getPaymentDate(),
                    tax.getCreationDate()
            ));
        }
        return taxDTOs;
    }


    /**
     * Restituisce lo stato dello studente in base alle tasse e al progresso accademico.
     *
     * @param token     Il token JWT dell'utente che effettua la richiesta.
     * @param studentId L'ID dello studente di cui si vuole conoscere lo stato.
     * @return Un oggetto StudentStatusDTO che rappresenta lo stato dello studente.
     */
    public StudentStatusDTO getStudentStatus(String token, String studentId) {
        //controlla nel database e se ci sono tasse scadute lo segnala
        validateToken(token, studentId);
        checkStudentId(studentId);

        // chiamata al microservizio di analisi e reportistica per ottenere lo stato dello studente
        StudentProgressDTO stdProgress = reportAndAnalysisClient.getStudentProgress(token, studentId);

        if (stdProgress.progressPercentage() == 100) {
            return new StudentStatusDTO(StudentStatus.COMPLETATO.getStatus(), true);
        }

        List<Tax> taxes = taxRepository.findTaxesByStudentId(studentId);

        if (taxes == null || taxes.isEmpty()) {
            if (studentRepository.findById(studentId).get().getStato().equals(StudentStatus.ATTIVO)) {
                return new StudentStatusDTO(StudentStatus.ATTIVO.getStatus(), true);
            } else {
                return new StudentStatusDTO(StudentStatus.IMMATRICOLATO.getStatus(), false);
            }
        }

        int mese = LocalDate.now().getMonthValue();
        int anno = LocalDate.now().getYear();
        if (mese < 10) {
            anno--;
        }
        boolean attivo = true, congelato = false, iscritto = false;

        for (Tax tax : taxes) {
            if (tax.getStatus().equals(TaxStatus.UNPAID)) {
                attivo = false;
                congelato = true;
            }
            if (tax.getStatus().equals(TaxStatus.PENDING)) {
                attivo = false;
                if (tax.getExpirationDate().isBefore(LocalDate.now())) {
                    tax.setStatus(TaxStatus.UNPAID);
                    taxRepository.save(tax);
                    congelato = true;
                }
            }
            if (tax.getExpirationDate().isAfter(LocalDate.of(anno, 7, 30)) && tax.getStatus().equals(TaxStatus.PAID)) {
                iscritto = true;
            }
        }
        if (attivo) {
            return new StudentStatusDTO(StudentStatus.ATTIVO.getStatus(),true);
        } else if (iscritto) {
            if (congelato) {
                return new StudentStatusDTO(StudentStatus.CONGELATO.getStatus(), false);
            }
            return new StudentStatusDTO(StudentStatus.ISCRITTO.getStatus(), true);
        }
        return new StudentStatusDTO(StudentStatus.NON_ISCRITTO.getStatus(),false);
    }


    /**
     * Restituisce una tassa specifica dello studente in formato testo.
     *
     * @param token Il token JWT dell'utente che effettua la richiesta.
     * @param taxId L'ID della tassa da recuperare.
     * @return Un oggetto TaxDTO che rappresenta la tassa specifica dello studente.
     */
    public TaxDTO getStudentTax(String token, String taxId) {
        //restituisce la tassa specifica in formato testo (solo studente stesso o amministrativi)
        Tax tax = taxRepository.findById(Long.parseLong(taxId)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tassa non trovata con ID: " + taxId));
        validateToken(token, tax.getStudentId());
        return new TaxDTO(
                tax.getId(),
                tax.getStudentId(),
                tax.getPagoPaNoticeCode(),
                tax.getAmount(),
                tax.getStatus(),
                tax.getExpirationDate(),
                tax.getPaymentDate(),
                tax.getCreationDate()
        );
    }

    /**
     * Restituisce una tassa specifica dello studente in formato PDF.
     *
     * @param token Il token JWT dell'utente che effettua la richiesta.
     * @param taxId L'ID della tassa da recuperare in formato PDF.
     * @return Un array di byte che rappresenta il contenuto del PDF della tassa.
     */
    public byte[] getPaymentNotice(String token, String taxId) {
        //devo restituire un PDF della tassa da un codice xhtml mediante l'utilizzo di flying saucer

        Tax tax = taxRepository.findById(Long.parseLong(taxId)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tassa non trovata con ID: " + taxId));
        validateToken(token, tax.getStudentId());
        checkStudentId(tax.getStudentId());

        //chiamata al microservizio gestione utenti e ruoli per recuperare i dati dello studente
        StudentDetailsDTO studente = gestioneUtentiClient.getStudentById(token, tax.getStudentId());

        Map<String, String> variables = Map.of(
                "nome", studente.name(),
                "cognome", studente.surname(),
                "matricola", studente.id(),
                "email", studente.email(),
                "importo", String.format("%.2f", tax.getAmount()),
                "scadenza", tax.getExpirationDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                "paymentNotice", tax.getPagoPaNoticeCode()
        );

        String html = generateHtmlFromRawTemplate("PaymentNotice.raw.xhtml", variables);
        return flyingSaucerService.htmlToPdf(html);
    }


    /**
     * Valida il token JWT e verifica i permessi dell'utente.
     *
     * @param token     Il token JWT da validare.
     * @param studentId L'ID dello studente per cui si stanno verificando i permessi.
     * @throws JwtTokenException Se il token non è valido o l'utente non ha i permessi sufficienti.
     */
    private void validateToken(String token, String studentId) {
        token = token.replace("Bearer ", "");
        if (!tokenManager.isTokenValid(token)) {
            throw new JwtTokenException("Token scaduto o non valido.", JwtTokenException.Reason.INVALID);
        }

        if (!Objects.equals(tokenManager.extractRole(token), "admin") && !Objects.equals(tokenManager.extractRole(token), "sadmin") && !Objects.equals(tokenManager.extractUserId(token), studentId)) {
            throw new JwtTokenException("L'utente non ha i permessi sufficienti.", JwtTokenException.Reason.UNAUTHORIZED_ROLE);
        }
    }


    /**
     * Controlla se l'ID dello studente è valido e se lo studente esiste nel sistema.
     *
     * @param studentId L'ID dello studente da verificare.
     * @throws ResponseStatusException Se l'ID non è valido o lo studente non esiste.
     */
    private void checkStudentId(String studentId) {
        if (studentId == null || studentId.length() != 6) {
            System.out.println("Dati forniti non validi.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dati forniti non validi.");
        }
        studentRepository.findById(studentId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Studente non presente nel sistema"));
    }

    public String generateHtmlFromRawTemplate(String templateName, Map<String, String> variables) {
        try (InputStream is = StudentService.class.getResourceAsStream("/templates/" + templateName);

             Scanner scanner = new Scanner(is, StandardCharsets.UTF_8)) {

            String html = scanner.useDelimiter("\\A").next();

            for (Map.Entry<String, String> entry : variables.entrySet()) {
                html = html.replace("{{" + entry.getKey() + "}}", entry.getValue());
            }

            return html;

        } catch (IOException | NoSuchElementException e) {
            throw new RuntimeException("Errore nel parsing del template: " + templateName, e);
        }
    }

}
