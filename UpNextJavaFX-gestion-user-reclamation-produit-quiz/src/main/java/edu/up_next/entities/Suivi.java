package edu.up_next.entities;

import java.time.LocalDate;

public class Suivi {
    private Long id;
    private Long reclamationId; // Référence à la réclamation
    private String descriptionSuivi;
    private LocalDate dateSuivi;
    private String satisfaction;


    private String titreReclamation; // Titre de la réclamation
    private String mailClientRec; // Mail du client
    private LocalDate dateResolutionRec; // Date de résolution de la réclamation

    // Constructeur
    public Suivi(Long id, Long reclamationId, String descriptionSuivi, LocalDate dateSuivi, String satisfaction, String titreReclamation, String mailClientRec) {
        this.id = id;
        this.reclamationId = reclamationId;
        this.descriptionSuivi = descriptionSuivi;
        this.dateSuivi = dateSuivi;
        this.satisfaction = satisfaction;
        this.titreReclamation = titreReclamation;
        this.mailClientRec = mailClientRec;
    }

    public Suivi(Long id, Long reclamationId, String description, LocalDate dateSuivi, String satisfaction) {
        this.id = id;
        this.reclamationId = this.reclamationId;
        this.descriptionSuivi = description;
        this.satisfaction = satisfaction;
    }


    // Constructeur avec date de résolution
    public Suivi(Long id, Long reclamationId, String descriptionSuivi, LocalDate dateSuivi, String satisfaction,
                 String titreReclamation, String mailClientRec, LocalDate dateResolutionRec) {
        this.id = id;
        this.reclamationId = reclamationId;
        this.descriptionSuivi = descriptionSuivi;
        this.dateSuivi = dateSuivi;
        this.satisfaction = satisfaction;
        this.titreReclamation = titreReclamation;
        this.mailClientRec = mailClientRec;
        this.dateResolutionRec = dateResolutionRec;
    }


    // Getters et Setters
    public Long getId() {
        return id;
    }

    public Long getReclamationId() {
        return reclamationId;
    }

    public String getDescriptionSuivi() {
        return descriptionSuivi;
    }

    public LocalDate getDateSuivi() {
        return dateSuivi;
    }

    public String getSatisfaction() {
        return satisfaction;
    }

    public String getTitreReclamation() {
        return titreReclamation;
    }

    public String getMailClientRec() {
        return mailClientRec;
    }

    public LocalDate getDateResolutionRec() {
        return dateResolutionRec;
    }

    public void setDateResolutionRec(LocalDate dateResolutionRec) {
        this.dateResolutionRec = dateResolutionRec;
    }
}



