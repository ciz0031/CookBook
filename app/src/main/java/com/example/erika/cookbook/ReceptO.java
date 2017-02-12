package com.example.erika.cookbook;

/**
 * Created by Erika on 10. 9. 2016.
 */
public class ReceptO {
    public int ID_receptu;
    public String nazev_receptu;
    public String postup;
    public int doba_pripravy;
    public int doba_peceni;
    public int stupne;
    public String prilohy;
    public int ID_kategorie;
    public int ID_podkategorie;
    public String foto;
    public int pocet_porci;
    public int oblibeny;
    public int hodnoceni;
    public String komentar;

    public ReceptO(){}

    public ReceptO(int ID_receptu, String nazev_receptu, String postup, int doba_pripravy, int doba_peceni, int stupne,
                   String prilohy, int ID_kategorie, int ID_podkategorie, String foto, int pocet_porci, int oblibeny, int hodnoceni,
                   String komentar){
        this.setID_receptu(ID_receptu);
        this.setNazev_receptu(nazev_receptu);
        this.setPostup(postup);
        this.setDoba_pripravy(doba_pripravy);
        this.setDoba_peceni(doba_peceni);
        this.setStupne(stupne);
        this.setPrilohy(prilohy);
        this.setID_kategorie(ID_kategorie);
        this.setID_podkategorie(ID_podkategorie);
        this.setFoto(foto);
        this.setPocet_porci(pocet_porci);
        this.setOblibeny(oblibeny);
        this.setHodnoceni(hodnoceni);
        this.setKomentar(komentar);
    }

    public String getKomentar() {
        return komentar;
    }

    public void setKomentar(String komentar) {
        this.komentar = komentar;
    }

    public int getID_receptu() {
        return ID_receptu;
    }

    public void setID_receptu(int ID_receptu) {
        this.ID_receptu = ID_receptu;
    }

    public String getNazev_receptu() {
        return nazev_receptu;
    }

    public void setNazev_receptu(String nazev_receptu) {
        this.nazev_receptu = nazev_receptu;
    }

    public String getPostup() {
        return postup;
    }

    public void setPostup(String postup) {
        this.postup = postup;
    }

    public int getDoba_pripravy() {
        return doba_pripravy;
    }

    public void setDoba_pripravy(int doba_pripravy) {
        this.doba_pripravy = doba_pripravy;
    }

    public int getDoba_peceni() {
        return doba_peceni;
    }

    public void setDoba_peceni(int doba_peceni) {
        this.doba_peceni = doba_peceni;
    }

    public int getStupne() {
        return stupne;
    }

    public void setStupne(int stupne) {
        this.stupne = stupne;
    }

    public String getPrilohy() {
        return prilohy;
    }

    public void setPrilohy(String prilohy) {
        this.prilohy = prilohy;
    }

    public int getID_kategorie() {
        return ID_kategorie;
    }

    public void setID_kategorie(int ID_kategorie) {
        this.ID_kategorie = ID_kategorie;
    }

    public int getID_podkategorie() {
        return ID_podkategorie;
    }

    public void setID_podkategorie(int ID_podkategorie) {
        this.ID_podkategorie = ID_podkategorie;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public int getPocet_porci() {
        return pocet_porci;
    }

    public void setPocet_porci(int pocet_porci) {
        this.pocet_porci = pocet_porci;
    }


    public int getOblibeny() {
        return oblibeny;
    }

    public void setOblibeny(int oblibeny) {
        this.oblibeny = oblibeny;
    }
    public int getHodnoceni() {
        return hodnoceni;
    }

    public void setHodnoceni(int hodnoceni) {
        this.hodnoceni = hodnoceni;
    }
}
