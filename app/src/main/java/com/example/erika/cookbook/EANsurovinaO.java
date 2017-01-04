package com.example.erika.cookbook;

/**
 * Created by Erika on 1. 11. 2016.
 */
public class EANsurovinaO {
    public int eanNumber;
    public String surovina;

    public EANsurovinaO(){}

    public EANsurovinaO (int eanNumber, String surovina){
        this.setEanNumber(eanNumber);
        this.setSurovina(surovina);
    }

    public int getEanNumber() {
        return eanNumber;
    }

    public void setEanNumber(int eanNumber) {
        this.eanNumber = eanNumber;
    }

    public String getSurovina() {
        return surovina;
    }

    public void setSurovina(String surovina) {
        this.surovina = surovina;
    }
}
