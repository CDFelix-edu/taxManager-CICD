package it.unimol.taxManager.model;

import jakarta.persistence.*;

@Entity
public class Brackets {
    @Id
    private int anno;
    private double importoBase;
    private double soglia1;
    private int sconto1;
    private double soglia2;
    private int sconto2;
    private double soglia3;
    private int sconto3;
    private double soglia4;
    private int sconto4;

    public Brackets() {

    }

    public Brackets(int anno, double importoBase, double soglia1, int sconto1, double soglia2, int sconto2, double soglia3, int sconto3, double soglia4, int sconto4) {
        super();
        this.anno = anno;
        this.importoBase = importoBase;
        this.soglia1 = soglia1;
        this.soglia2 = soglia2;
        this.soglia3 = soglia3;
        this.soglia4 = soglia4;
        this.sconto1 = sconto1;
        this.sconto2 = sconto2;
        this.sconto3 = sconto3;
        this.sconto4 = sconto4;
    }

    //setters
    public void setImportoBase(double importoBase) {
        this.importoBase = importoBase;
    }
    public void setSoglia1(double soglia1) {
        this.soglia1 = soglia1;
    }
    public void setSconto1(int sconto1) {
        this.sconto1 = sconto1;
    }
    public void setSoglia2(double soglia2) {
        this.soglia2 = soglia2;
    }
    public void setSconto2(int sconto2) {
        this.sconto2 = sconto2;
    }
    public void setSoglia3(double soglia3) {
        this.soglia3 = soglia3;
    }
    public void setSconto3(int sconto3) {
        this.sconto3 = sconto3;
    }
    public void setSoglia4(double soglia4) {
        this.soglia4 = soglia4;
    }
    public void setSconto4(int sconto4) {
        this.sconto4 = sconto4;
    }

    //getters
    public int getAnno() {
        return anno;
    }
    public double getImportoBase() {
        return importoBase;
    }
    public double getSoglia1() {
        return soglia1;
    }
    public int getSconto1() {
        return sconto1;
    }
    public double getSoglia2() {
        return soglia2;
    }
    public int getSconto2() {
        return sconto2;
    }
    public double getSoglia3() {
        return soglia3;
    }
    public int getSconto3() {
        return sconto3;
    }
    public double getSoglia4() {
        return soglia4;
    }
    public int getSconto4() {
        return sconto4;
    }
}
