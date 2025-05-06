package edu.up_next.entities;

import java.util.List;

public class Produit {
    private int id;
    private int commandeId; // Clé étrangère vers Commande
    private String nom;
    private String image;
    double prix;
    private String categorie;
    private String description;
    private String approvalStatus;
    private String statut;

    // Constructeur par défaut
    public Produit() {
        this.approvalStatus = "PENDING"; // Valeur par défaut de la base de données
        this.statut = "DISPONIBLE"; // Valeur par défaut corrigée
    }

    // Constructeur pour ajouter un produit
    public Produit(String nom, String image, double prix, String categorie, String description) {
        this.nom = nom;
        this.image = image;
        this.prix = prix;
        this.categorie = categorie;
        this.description = description;
        this.approvalStatus = "PENDING";
        this.statut = "DISPONIBLE";
    }

    // Constructeur complet
    public Produit(int id, String nom, String image, double prix, String categorie, String description,
                   String approvalStatus, String statut) {
        this.id = id;
        this.nom = nom;
        this.image = image;
        this.prix = prix;
        this.categorie = categorie;
        this.description = description;
        this.approvalStatus = approvalStatus != null ? approvalStatus : "PENDING";
        this.statut = statut != null ? statut : "DISPONIBLE";
    }

    // Getters et Setters avec validation
    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id < 0) {
            throw new IllegalArgumentException("L'ID ne peut pas être négatif.");
        }
        this.id = id;
    }

    public int getCommandeId() {
        return commandeId;
    }

    public void setCommandeId(int commandeId) {
        this.commandeId = commandeId;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        if (nom == null || nom.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom ne peut pas être vide.");
        }
        this.nom = nom;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image; // Validation sera faite dans ProduitServices
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        if (prix <= 0) {
            throw new IllegalArgumentException("Le prix doit être positif.");
        }
        this.prix = prix;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        if (categorie == null || categorie.trim().isEmpty()) {
            throw new IllegalArgumentException("La catégorie ne peut pas être vide.");
        }
        this.categorie = categorie;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("La description ne peut pas être vide.");
        }
        this.description = description;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(String approvalStatus) {
        if (approvalStatus != null && !List.of("PENDING", "ACCEPTED", "REJECTED").contains(approvalStatus)) {
            throw new IllegalArgumentException("Statut d'approbation invalide.");
        }
        this.approvalStatus = approvalStatus != null ? approvalStatus : "PENDING";
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        if (statut != null && !List.of("DISPONIBLE", "VENDU").contains(statut)) {
            throw new IllegalArgumentException("Statut invalide.");
        }
        this.statut = statut != null ? statut : "DISPONIBLE";
    }

    @Override
    public String toString() {
        return "Produit{" +
                "id=" + id +
                ", commandeId=" + commandeId +
                ", nom='" + nom + '\'' +
                ", image='" + image + '\'' +
                ", prix=" + prix +
                ", categorie='" + categorie + '\'' +
                ", description='" + description + '\'' +
                ", approvalStatus='" + approvalStatus + '\'' +
                ", statut='" + statut + '\'' +
                '}';
    }
}