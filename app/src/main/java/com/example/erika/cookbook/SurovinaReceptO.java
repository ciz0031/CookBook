package com.example.erika.cookbook;

/**
 * Created by Erika on 10. 9. 2016.
 */
public class SurovinaReceptO {
    public String surovina;
    public String nazev_receptu;
    public float mnozstvi;
    public String typ_mnozstvi;

    public SurovinaReceptO(){}
    public SurovinaReceptO(String surovina, String nazev_receptu, float mnozstvi, String typ_mnozstvi){
        this.setSurovina(surovina);
        this.setNazev_receptu(nazev_receptu);
        this.setMnozstvi(mnozstvi);
        this.setTyp_mnozstvi(typ_mnozstvi);
    }

    public String getSurovina() {
        return surovina;
    }

    public void setSurovina(String surovina) {
        this.surovina = surovina;
    }

    public String getNazev_receptu() {
        return nazev_receptu;
    }

    public void setNazev_receptu(String nazev_receptu) {
        this.nazev_receptu = nazev_receptu;
    }

    public float getMnozstvi() {
        return mnozstvi;
    }

    public void setMnozstvi(float mnozstvi) {
        this.mnozstvi = mnozstvi;
    }

    public String getTyp_mnozstvi() {
        return typ_mnozstvi;
    }

    public void setTyp_mnozstvi(String typ_mnozstvi) {
        this.typ_mnozstvi = typ_mnozstvi;
    }
}
