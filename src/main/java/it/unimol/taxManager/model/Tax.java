package it.unimol.taxManager.model;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unimol.taxManager.util.TaxStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.PrePersist;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;

import java.time.LocalDate;

@Entity
@Table(name = "taxes")
public class Tax {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

   // @Column(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "studentId", nullable = false)
    private Student student;  // ID studente collegato (codice fiscale o altro)

    @Column(nullable = false)
    private Double amount;     // Importo dovuto

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaxStatus status;  // STATO: PAID, UNPAID, PENDING...

    //@Temporal(TemporalType.DATE)
    @Column(nullable = false)
    private LocalDate expirationDate;   // Data di scadenza

    //@Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private LocalDate creationDate;     // Data di creazione (settata all'inserimento)

    //@Temporal(TemporalType.TIMESTAMP)
    private LocalDate paymentDate;      // Data di pagamento, se pagata

    private String pagoPaNoticeCode;  // Codice avviso pagoPA (es: "001000000039557384")

    public Tax() {

    }

    // Costruttori, getter/setter, toString...

    @PrePersist
    protected void onCreate() {
        this.creationDate = LocalDate.now();
    }

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public Tax(Student student, Double amount, TaxStatus status, LocalDate expirationDate, String pagoPaNoticeCode) {
        this.student = student;
        this.amount = amount;
        this.status = status;
        this.expirationDate = expirationDate;
        this.pagoPaNoticeCode = pagoPaNoticeCode;
    }

    public String getId() {
        return id.toString();
    }

    public double getAmount() {
        return amount;
    }

    public TaxStatus getStatus() {
        return status;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }
    public String getPagoPaNoticeCode() {
        return pagoPaNoticeCode;
    }

    public String getStudentId() {
        return student.getId();
    }

    public void setStatus(TaxStatus taxStatus) {
        this.status = taxStatus;
    }

    public boolean setPaymentDate(LocalDate paymentDate) {
        if(paymentDate == null || paymentDate.isBefore(creationDate)) {
            return false;
        }
        this.paymentDate = paymentDate;
        return true;
    }
}
