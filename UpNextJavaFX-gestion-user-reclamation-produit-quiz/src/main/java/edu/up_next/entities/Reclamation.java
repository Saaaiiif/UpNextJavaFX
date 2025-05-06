package edu.up_next.entities;

import java.time.LocalDate;
import java.util.List;

public class Reclamation {
    private Long id;
    private String titre;
    private String mail_client_rec;
    private LocalDate date_creation_rec;
    private String type_rec;
    private String description_rec;
    private String fichier_pj_rec;
    private String status_rec;
    private LocalDate date_resolution_rec;
    private User user;

    // Ajout de la liste des suivis associés à cette réclamation
    private List<Suivi> suivis;

    // Constructeur et autres méthodes

    public List<Suivi> getSuivis() {
        return suivis;
    }

    public void setSuivis(List<Suivi> suivis) {
        this.suivis = suivis;
    }

    //user ::::


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }





/*
    public Reclamation() {}

    public Reclamation(String titre, String mail_client_rec, String type_rec, String status_rec) {
        this.titre = new String(titre);
        this.mail_client_rec = new String(mail_client_rec);
        this.type_rec = new String(type_rec);
        this.status_rec = new String(status_rec);
    }
    */


    public Reclamation(Long id ,String titre, String mail_client_rec, String type_rec, String description_rec, String status_rec, LocalDate date_creation_rec, LocalDate date_resolution_rec,String fichier_pj_rec) {
        this.id = id;
        this.titre = titre;
        this.mail_client_rec = mail_client_rec;
        this.type_rec = type_rec;
        this.description_rec = description_rec;
        this.fichier_pj_rec = fichier_pj_rec;
        this.status_rec = status_rec;
        this.date_creation_rec = date_creation_rec;
        this.date_resolution_rec = date_resolution_rec;
    }



    // Getters et Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getMail_client_rec() {
        return mail_client_rec;
    }

    public void setMail_client_rec(String mail_client_rec) {
        this.mail_client_rec = mail_client_rec;
    }

    public LocalDate getDate_creation_rec() {
        return date_creation_rec;
    }

    public void setDate_creation_rec(LocalDate date_creation_rec) {
        this.date_creation_rec = date_creation_rec;
    }

    public String getType_rec() {
        return type_rec;
    }

    public void setType_rec(String type_rec) {
        this.type_rec = type_rec;
    }

    public String getDescription_rec() {
        return description_rec;
    }

    public void setDescription_rec(String description_rec) {
        this.description_rec = description_rec;
    }

    public String getFichier_pj_rec() {
        return fichier_pj_rec;
    }

    public void setFichier_pj_rec(String fichier_pj_rec) {
        this.fichier_pj_rec = fichier_pj_rec;
    }

    public String getStatus_rec() {
        return status_rec;
    }

    public void setStatus_rec(String status_rec) {
        this.status_rec = status_rec;
    }

    public LocalDate getDate_resolution_rec() {
        return date_resolution_rec;
    }

    public void setDate_resolution_rec(LocalDate date_resolution_rec) {
        this.date_resolution_rec = date_resolution_rec;
    }
    // Getters et Setters
}
