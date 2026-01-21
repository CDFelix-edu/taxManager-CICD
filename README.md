# 📚 Microservizio Gestione Tasse Universitarie — Documentazione Tecnica

Compito:

Gestione delle Tasse Universitarie (responsabile della gestione delle informazioni relative alle tasse universitarie, le scadenze, i metodi di pagamento e lo stato dei pagamenti degli studenti):
-	(Amministrativi) Gestione delle soglie ISEE per la determinazione delle tasse degli studenti
-	(Amministrativi) Generazione degli avvisi di pagamento per tutti gli studenti (in PDF)
-	(Studenti) Pagamento delle tasse (simulando un pagamento con PagoPA)
 
---

## 📌 Scopo del microservizio

Gestione delle tasse universitarie per studenti, incluse:
- Gestione soglie ISEE
- Generazione di avvisi di pagamento
- Gestione degli ISEE dei singoli studenti
- Tracciamento dei pagamenti e condivisione rapida dello stato
- Gestione e registrazione dei pagamenti (con integrazione a PagoPA)
- Notifiche delle scadenze delle tasse agli studenti
---

## 🧱 Architettura Tecnica

| Componente | Tecnologia |
|------------|------------|
| Linguaggio | Java |
| Framework | Spring Boot |
| Database | PostgreSQL |
| Build Tool | Maven |
| API Docs | Swagger / OpenAPI |
| Sicurezza | JWT Token |
| Comunicazione | REST, RabbitMQ |
| Container | Docker |

---

## 🗂️ Modello Dati

### 👤 `Student`

| Campo      | Tipo            | Descrizione                                                                 |
|------------|-----------------|-----------------------------------------------------------------------------|
| `id`       | `String`        | Matricola dello studente _(chiave primaria)_                                |
| `ISEE`     | `double`        | Indicatore della situazione economica equivalente                          |
| `stato`    | `StudentStatus` | Stato dello studente (`IMMATRICOLATO`, ecc.)<br>Enum definito internamente |
|
---

### 💰 `Tax`

| Campo                | Tipo             | Descrizione                                                                      |
|----------------------|------------------|----------------------------------------------------------------------------------|
| `id`                 | `Long` (via `toString()`) | ID della tassa _(chiave primaria)_                                       |
| `student`            | `Student`        | Studente associato (relazione `@ManyToOne`)                                      |
| `amount`             | `Double`         | Importo totale della tassa                                                      |
| `status`             | `TaxStatus`      | Stato della tassa (`PAID`, `UNPAID`, ecc.)<br>Enum                              |
| `expirationDate`     | `LocalDate`      | Data di scadenza                                                                |
| `creationDate`       | `LocalDate`      | Data di creazione _(settata automaticamente con `@PrePersist`)_, non modificabile |
| `paymentDate`        | `LocalDate?`     | Data del pagamento (se presente)                                                |
| `pagoPaNoticeCode`   | `String`         | Codice avviso per PagoPA (es: `001000000039557384`)                             |

---

### 📊 `Brackets`

| Campo         | Tipo     | Descrizione                               |
|---------------|----------|-------------------------------------------|
| `anno`        | `int`    | Anno accademico _(chiave primaria)_       |
| `importoBase` | `double` | Tassa base su cui si applicano gli sconti |
| `soglia1`     | `double` | Prima soglia ISEE                         |
| `sconto1`     | `int`    | Sconto percentuale associato              |
| `soglia2`     | `double` | Seconda soglia ISEE                       |
| `sconto2`     | `int`    | Sconto percentuale associato              |
| `soglia3`     | `double` | Terza soglia ISEE                         |
| `sconto3`     | `int`    | Sconto percentuale associato              |
| `soglia4`     | `double` | Quarta soglia ISEE                        |
| `sconto4`     | `int`    | Sconto percentuale associato              |

---

### 🎛️ Enum utilizzati

#### `StudentStatus`

| Valore        | Descrizione                                                                 |
|---------------|------------------------------------------------------------------------------|
| `IMMATRICOLATO` | Lo studente è immatricolato ma non ha ancora tasse generate                |
| `ISCRITTO`      | Ha pagato la prima rata, non ha tasse scadute                               |
| `NON_ISCRITTO`  | Non ha ancora pagato nemmeno una rata                                       |
| `CONGELATO`     | Iscritto ma con tasse scadute                                               |
| `ATTIVO`        | Tutte le rate risultano pagate                                              |
| `COMPLETATO`    | Ha completato il percorso di studi                                          |
| `FUORI_CORSO`   | Etichetta possibile per studenti fuori corso (non usata attivamente)       |
| `CESSATO`       | Ha rinunciato agli studi                                                    |

#### `TaxStatus`

| Valore   | Descrizione                  |
|----------|------------------------------|
| `PAID`   | Tassa pagata                 |
| `UNPAID` | Tassa non ancora pagata     |
| `PENDING`| In attesa di conferma (es. PagoPA) |


## 🔁 Interazioni con altri microservizi

| Servizio | Funzione | Tipo |
|----------|----------|------|
| **Gestione Utenti e Ruoli** | Ottenere elenco utenti + Ottenere dettagli utente + eventi RabbitMQ su nuove iscrizioni o modifiche agli studenti | REST + MQ |
| **PagoPA** | Registrazioni tasse e pagamento tasse | REST |
| **Analisi & Report** | Verificare stato carriera studente (blocco generazione tasse) | REST |

---

## 🔐 Autenticazione

| Ruolo | Permessi |
|-------|----------|
| `admin` | Gestione soglie, tasse, ISEE |
| `student` | Consultazione tasse, pagamento |
| `super_admin` | Accesso completo |
| ⚠️ | JWT Token da includere nei header delle richieste |

---

## 📬 Endpoint principali

### 🧾 Student

- `GET /api/v1/student/{id}` → Elenco completo delle tasse
- `GET /api/v1/student/status/{id}` → stato studente
- `GET /api/v1/student/taxes/{taxId}` → dettaglio tassa
- `GET /api/v1/student/taxes/pdf/{taxId}` → avviso di pagamento in formato PDF

### 🛠 Admin

- `PUT /api/v1/admin/student/isee/{id}` → aggiorna ISEE dello studente
- `POST /api/v1/admin/brackets` → inserisce soglie ISEE
- `PATCH /api/v1/admin/brackets/{anno}` → aggiorna soglie di uno specifico anno
- `POST /api/v1/admin/generate/{anno}` → genera tasse per anno
- `POST /api/v1/admin/UpdateData` → sincronizza studenti da Gestione Utenti

### 💰 Pagamento (PagoPA)

- `POST /api/v1/tax/PagoPa/registerTax`  
  Riceve conferma di pagamento da PagoPA.

#### 📋 Esempio Body JSON:

```json
{
  "codiceAvviso": "123456789",
  "statoPagamento": "PAGATO",
  "importo": 150.00,
  "dataPagamento": "2025-06-13T11:30:00Z",
  "idTransazione": "ABCDEF123456"
}
```

---
## 🚦 Continuous Integration (GitHub Actions):

Il progetto include una pipeline CI che esegue automaticamente:
- build Maven
- test con profilo test (H2)
- verifica compilazione

Workflow: `.github/workflows/ci.yml`
