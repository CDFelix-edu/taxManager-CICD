package it.unimol.taxManager.model;

import it.unimol.taxManager.util.StudentStatus;
import jakarta.persistence.*;

@Entity
public class Student {
    @Id
    private String id; // Matricola dello studente
    /*FIXME: meglio richiedere le informazioni al microservizio di mauro piuttosto che salvarle nel DB
    private String username;
    private String email;
    private String nome;
    private String cognome;
    TODO: troppo tempo per implementarlo, ma sarebbe bello poter gestire anche il tipo di studente (es. part-time, full-time)
    private String StudentType;*/
    private StudentStatus stato;

    private double ISEE;

    public Student() {
    }

    public Student(String id/*, String username, String email, String nome, String cognome*/) {
        this.id = id;

        this.stato = StudentStatus.IMMATRICOLATO;
        this.ISEE = 36000;
    }

    public void setISEE(double ISEE) {
        this.ISEE = ISEE;
    }

    public double getISEE() {
        return ISEE;
    }

    public String getId() {
        return id;
    }

    public void setStato(StudentStatus status) {
        this.stato = status;
    }

    public StudentStatus getStato() {
        return stato;
    }
}
