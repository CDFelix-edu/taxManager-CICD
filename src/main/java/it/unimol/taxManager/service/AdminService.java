package it.unimol.taxManager.service;

import it.unimol.taxManager.client.PagoPA.PagoPAClient;
import it.unimol.taxManager.client.gestioneUtenti.GestioneUtentiClient;
import it.unimol.taxManager.client.reportAndAnalysisManager.ReportAndAnalysisClient;
import it.unimol.taxManager.exception.JwtTokenException;
import it.unimol.taxManager.model.Brackets;
import it.unimol.taxManager.model.Student;
import it.unimol.taxManager.model.Tax;
import it.unimol.taxManager.repository.SogliaRepository;
import it.unimol.taxManager.repository.StudentRepository;
import it.unimol.taxManager.repository.TaxRepository;
import it.unimol.taxManager.util.JWTToken;
import it.unimol.taxManager.util.StudentStatus;
import it.unimol.taxManager.util.TaxStatus;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

//DTO
import it.unimol.taxManager.dto.BracketsDTO;
import it.unimol.taxManager.dto.IseeUpdateDTO;
import it.unimol.taxManager.dto.StudentDTO;
import it.unimol.taxManager.dto.UpdateDTO;
import it.unimol.taxManager.dto.StudentDetailsDTO;
import it.unimol.taxManager.dto.UpdateBracketsDTO;
import it.unimol.taxManager.dto.PagoPATaxDTO;

@Service
public class AdminService {

    private final JWTToken tokenManager;
    private final GestioneUtentiClient gestioneUtentiClient;
    private final SogliaRepository sogliaRepository;
    private final StudentRepository studentRepository;
    private final TaxRepository taxRepository;
    private final PagoPAClient pagoPAClient;
    private final ReportAndAnalysisClient reportAndAnalysisClient;

    public AdminService(GestioneUtentiClient gestioneUtentiClient, SogliaRepository sogliaRepository, StudentRepository studentRepository, JWTToken tokenManager, TaxRepository taxRepository, PagoPAClient pagoPAClient, ReportAndAnalysisClient reportAndAnalysisClient) {
        this.gestioneUtentiClient = gestioneUtentiClient;
        this.sogliaRepository = sogliaRepository;
        this.studentRepository = studentRepository;
        this.tokenManager = tokenManager;
        this.taxRepository = taxRepository;
        this.pagoPAClient = pagoPAClient;
        this.reportAndAnalysisClient = reportAndAnalysisClient;
    }

    public UpdateDTO updateDatabase(String token) {
        validateToken(token);

        int nuoviStudenti = 0;
        List<StudentDTO> studentiAggiornati = gestioneUtentiClient.getStudentList(token);
        for (StudentDTO dto : studentiAggiornati) {
            if ("student".equals(dto.nomeRuolo())) {
                Optional<Student> existingStudente = studentRepository.findById(dto.id());
                if (existingStudente.isEmpty()) {
                    Student nuovoStudente = new Student(dto.id()/*, dto.username(), dto.email(), dto.nome(), dto.cognome()*/);
                    studentRepository.save(nuovoStudente);
                    nuoviStudenti++;
                }
            }
        }

        // Risposta formattata
        return new UpdateDTO(nuoviStudenti);
        /*Map.of(
                "status", "success",
                "messaggio", "Database studenti aggiornato correttamente.",
                "studenti_aggiornati", nuoviStudenti
        );*/
    }


    public boolean updateStudentISEE(String token, String studentId, IseeUpdateDTO studentISEE) {
        validateToken(token);

        if (studentISEE.isee() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Il valore ISEE non può essere negativo");
        }

        // Verifica se lo studente esiste nel database
        Student student = studentRepository.findById(studentId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Studente non trovato"));

        // Se il valore ISEE è già uguale a quello fornito, non fare nulla
        if (Math.abs(student.getISEE() - studentISEE.isee()) < 0.0001) {
            return false;
        }

        // Aggiornamento del valore ISEE
        student.setISEE(studentISEE.isee());
        studentRepository.save(student); // Salva le modifiche nel database

        return true;
    }


    public boolean insertSoglieIsee(String token, BracketsDTO soglie) {
        validateToken(token);

        if ((soglie.anno() < 1900 || soglie.anno() > 2500)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Anno non conforme.");
        }

        if (sogliaRepository.existsByAnno(soglie.anno())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le soglie per l'anno " + soglie.anno() + " sono già state definite.");
        }

        checkSoglie(soglie);

        Brackets soglia = new Brackets(soglie.anno(), soglie.importoBase(), soglie.soglia1(), soglie.sconto1(), soglie.soglia2(), soglie.sconto2(), soglie.soglia3(), soglie.sconto3(), soglie.soglia4(), soglie.sconto4());
        sogliaRepository.save(soglia);
        return true;
    }

    public boolean updateSoglieIsee(String token, int anno, UpdateBracketsDTO updatedSoglieDTO) {
        validateToken(token);

        Brackets sogliaToMerge = sogliaRepository.findByAnno(anno).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "L'anno indicato non è presente nel DataBase"));

        if (updatedSoglieDTO.importoBase() == null && updatedSoglieDTO.soglia1() == null && updatedSoglieDTO.sconto1() == null && updatedSoglieDTO.soglia2() == null && updatedSoglieDTO.sconto2() == null && updatedSoglieDTO.soglia3() == null && updatedSoglieDTO.sconto3() == null && updatedSoglieDTO.soglia4() == null && updatedSoglieDTO.sconto4() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nessun valore da aggiornare fornito.");
        }

        BracketsDTO mergedSoglie = new BracketsDTO(sogliaToMerge.getAnno(), updatedSoglieDTO.importoBase() != null ? updatedSoglieDTO.importoBase() : sogliaToMerge.getImportoBase(), updatedSoglieDTO.soglia1() != null ? updatedSoglieDTO.soglia1() : sogliaToMerge.getSoglia1(), updatedSoglieDTO.sconto1() != null ? updatedSoglieDTO.sconto1() : sogliaToMerge.getSconto1(), updatedSoglieDTO.soglia2() != null ? updatedSoglieDTO.soglia2() : sogliaToMerge.getSoglia2(), updatedSoglieDTO.sconto2() != null ? updatedSoglieDTO.sconto2() : sogliaToMerge.getSconto2(), updatedSoglieDTO.soglia3() != null ? updatedSoglieDTO.soglia3() : sogliaToMerge.getSoglia3(), updatedSoglieDTO.sconto3() != null ? updatedSoglieDTO.sconto3() : sogliaToMerge.getSconto3(), updatedSoglieDTO.soglia4() != null ? updatedSoglieDTO.soglia4() : sogliaToMerge.getSoglia4(), updatedSoglieDTO.sconto4() != null ? updatedSoglieDTO.sconto4() : sogliaToMerge.getSconto4());

        if (mergedSoglie.equals(new BracketsDTO(sogliaToMerge.getAnno(), sogliaToMerge.getImportoBase(), sogliaToMerge.getSoglia1(), sogliaToMerge.getSconto1(), sogliaToMerge.getSoglia2(), sogliaToMerge.getSconto2(), sogliaToMerge.getSoglia3(), sogliaToMerge.getSconto3(), sogliaToMerge.getSoglia4(), sogliaToMerge.getSconto4()))) {
            return false;
        }

        checkSoglie(mergedSoglie);

        Brackets sogliaToSave = new Brackets(mergedSoglie.anno(), mergedSoglie.importoBase(), mergedSoglie.soglia1(), mergedSoglie.sconto1(), mergedSoglie.soglia2(), mergedSoglie.sconto2(), mergedSoglie.soglia3(), mergedSoglie.sconto3(), mergedSoglie.soglia4(), mergedSoglie.sconto4());
        sogliaRepository.save(sogliaToSave);
        return true;
    }

    public Map<String, Object> generatePaymentNotices(String token, int anno) {
        int maxNumRate = 5; // Numero massimo di rate da calcolare per ogni studente
        validateToken(token);

        Brackets soglia = sogliaRepository.findByAnno(anno).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Soglie per l'anno " + anno + " non trovate."));
        List<Student> studenti = studentRepository.findAll();

        if (studenti.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nessuno studente trovato nel database.");
        }

        //Utile per prevedere che tutti devono pagare un minimo di tassa (iscrizione e cosa regionale)
        double importoMinimo = 0;
        double minRata = 25;

        for (Student studente : studenti) {
            //TODO: controllare se tramite chiamata al microservizio di Analisi e Reportistica si può ottenere lo stato carriera dello studente (sapere se si è ritirato)
            if (studente.getStato() != StudentStatus.CESSATO && studente.getStato() != StudentStatus.COMPLETATO) {
                System.out.println("Calcolo tasse per lo studente: " + studente.getId());
                if (reportAndAnalysisClient.getStudentProgress(token, studente.getId()).progressPercentage() == 100) {
                    studente.setStato(StudentStatus.COMPLETATO);
                    studentRepository.save(studente);
                } else {
                    double importoStudente = getImportoStudente(studente, soglia, importoMinimo);
                    if (importoStudente > 0.00 && taxRepository.findTaxesByStudentId(studente.getId()).stream().noneMatch(tax -> tax.getExpirationDate().isAfter(LocalDate.of(LocalDate.now().getYear(), 6, 1)))) {
                        // Calcolo delle rate
                        int numeroRate = maxNumRate;
                        while (importoStudente / numeroRate < minRata && numeroRate > 1) {
                            numeroRate--;
                        }
                        double importoPerRata = importoStudente / numeroRate;

                        //Chiamata al microservizio di gestione utenti e ruoli per ritornare nome e cognome
                        StudentDetailsDTO studentDetails = gestioneUtentiClient.getStudentById(token, studente.getId());

                        for (int i = 0; i < numeroRate; i++) {
                            PagoPATaxDTO pagoPaDetails = new PagoPATaxDTO(studentDetails.id(), studentDetails.surname(), studentDetails.name(), "Università degli studi del Molise", importoPerRata, getExpiration(i + 1, anno));
                            //TODO: migliorare la gestione del token di autenticazione se possibile
                            String avvisoPagoPa = pagoPAClient.registerTax("Bearer codiceAutenticazioneConPagoPA", pagoPaDetails);
                            taxRepository.save(new Tax(studente, importoPerRata, TaxStatus.PENDING, getExpiration(i + 1, anno), avvisoPagoPa));
                        }
                        studente.setStato(StudentStatus.NON_ISCRITTO);
                        studentRepository.save(studente);
                    } else {
                        studente.setStato(StudentStatus.ATTIVO);
                        studentRepository.save(studente);
                    }
                }
            }
        }

        return Map.of("status", "success", "messaggio", "Tasse calcolate e avvisi di pagamento generati correttamente.");
    }

    private static LocalDate getExpiration(int numRata, int anno) {
        return switch (numRata) {
            case 1 -> LocalDate.of(anno, 9, 30);
            case 2 -> LocalDate.of(anno, 12, 10);
            case 3 -> LocalDate.of(anno + 1, 1, 31);
            case 4 -> LocalDate.of(anno + 1, 3, 31);
            default -> LocalDate.of(anno + 1, 5, 30);
        };
    }

    private static double getImportoStudente(Student studente, Brackets soglia, double importoMinimo) {
        double importoStudente = 0.00;
        if (studente.getISEE() < soglia.getSoglia1()) {
            importoStudente = soglia.getImportoBase() * (1 - (double) soglia.getSconto1() / 100) + importoMinimo;
        } else if (studente.getISEE() >= soglia.getSoglia1() && studente.getISEE() < soglia.getSoglia2()) {
            importoStudente = soglia.getImportoBase() * (1 - (double) soglia.getSconto2() / 100) + importoMinimo;
        } else if (studente.getISEE() >= soglia.getSoglia2() && studente.getISEE() < soglia.getSoglia3()) {
            importoStudente = soglia.getImportoBase() * (1 - (double) soglia.getSconto3() / 100) + importoMinimo;
        } else if (studente.getISEE() >= soglia.getSoglia3() && studente.getISEE() < soglia.getSoglia4()) {
            importoStudente = soglia.getImportoBase() * (1 - (double) soglia.getSconto4() / 100) + importoMinimo;
        } else if (studente.getISEE() >= soglia.getSoglia4()) {
            importoStudente = soglia.getImportoBase() + importoMinimo;
        }
        return importoStudente;
    }

    private void checkSoglie(BracketsDTO sogliaToCheck) {
        if (sogliaToCheck.importoBase() < 0 || sogliaToCheck.soglia1() < 0 || sogliaToCheck.sconto1() < 0 || sogliaToCheck.soglia2() < 0 || sogliaToCheck.sconto2() < 0 || sogliaToCheck.soglia3() < 0 || sogliaToCheck.sconto3() < 0 || sogliaToCheck.soglia4() < 0 || sogliaToCheck.sconto4() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "I valori delle soglie e degli sconti devono essere positivi.");
        }

        if (sogliaToCheck.sconto1() > 100 || sogliaToCheck.sconto2() > 100 || sogliaToCheck.sconto3() > 100 || sogliaToCheck.sconto4() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Impossibile applicare una riduzione delle tasse superiore al 100%.");
        }

        if (sogliaToCheck.soglia1() > sogliaToCheck.soglia2() || sogliaToCheck.soglia2() > sogliaToCheck.soglia3() || sogliaToCheck.soglia3() > sogliaToCheck.soglia4()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Errore di ordinamento delle soglie: una fascia successiva non può essere minore della precedente");
        }

        if (sogliaToCheck.sconto1() < sogliaToCheck.sconto2() || sogliaToCheck.sconto2() < sogliaToCheck.sconto3() || sogliaToCheck.sconto3() < sogliaToCheck.sconto4()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Errore di ordinamento degli sconti: una fascia successiva non può avere uno sconto maggiore della precedente");
        }
    }


    //Validazione Token
    private void validateToken(String token) {
        token = token.replace("Bearer ", "");
        if (!tokenManager.isTokenValid(token)) {
            throw new JwtTokenException("Token scaduto o non valido.", JwtTokenException.Reason.INVALID);
        }
        if (!Objects.equals(tokenManager.extractRole(token), "admin") && !Objects.equals(tokenManager.extractRole(token), "sadmin")) {
            throw new JwtTokenException("L'utente non ha i permessi sufficienti.", JwtTokenException.Reason.UNAUTHORIZED_ROLE);
        }
    }
     /*
    private Date getExpirationDate(int anno, int mese, int giorno) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(anno, mese, giorno, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }*/

}
