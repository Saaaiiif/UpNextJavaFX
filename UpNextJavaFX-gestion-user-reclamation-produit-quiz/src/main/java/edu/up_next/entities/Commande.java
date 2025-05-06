package edu.up_next.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;



public class Commande {
    private int id;
    private double prixTotal;
    private LocalDateTime dateCommande;
    private String status;
    private LocalDateTime deliveryDate;
    private List<Produit> produits;

    public Commande() {
        this.produits = new ArrayList<>();
        this.status = "EN_ATTENTE"; // Valeur par défaut
    }

    public Commande(int id, double prixTotal, LocalDateTime dateCommande, String status, LocalDateTime deliveryDate) {
        this.id = id;
        this.prixTotal = prixTotal;
        this.dateCommande = dateCommande;
        this.status = status != null ? status : "EN_ATTENTE";
        this.deliveryDate = deliveryDate;
        this.produits = new ArrayList<>();
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

    public double getPrixTotal() {
        return prixTotal;
    }

    public void setPrixTotal(double prixTotal) {
        if (prixTotal <= 0) {
            throw new IllegalArgumentException("Le prix total doit être positif.");
        }
        this.prixTotal = prixTotal;
    }

    public LocalDateTime getDateCommande() {
        return dateCommande;
    }

    public void setDateCommande(LocalDateTime dateCommande) {
        if (dateCommande == null) {
            throw new IllegalArgumentException("La date de commande ne peut pas être vide.");
        }
        if (dateCommande.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("La date de commande ne peut pas être dans le futur.");
        }
        this.dateCommande = dateCommande;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        if (status != null && !List.of("EN_ATTENTE", "LIVREE").contains(status)) {
            throw new IllegalArgumentException("Statut invalide.");
        }
        this.status = status != null ? status : "EN_ATTENTE";
    }

    public LocalDateTime getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDateTime deliveryDate) {
        if (deliveryDate != null && dateCommande != null && deliveryDate.isBefore(dateCommande)) {
            throw new IllegalArgumentException("La date de livraison doit être postérieure à la date de commande.");
        }
        this.deliveryDate = deliveryDate;
    }

    public List<Produit> getProduits() {
        return produits;
    }

    public void setProduits(List<Produit> produits) {
        this.produits = produits != null ? produits : new ArrayList<>();
    }

    public void ajouterProduit(Produit produit) {
        if (produit == null) {
            throw new IllegalArgumentException("Le produit ne peut pas être null.");
        }
        this.produits.add(produit);
        produit.setCommandeId(this.id); // Maintenir la relation
    }

    @Override
    public String toString() {
        return "Commande{" +
                "id=" + id +
                ", prixTotal=" + prixTotal +
                ", dateCommande=" + dateCommande +
                ", status='" + status + '\'' +
                ", deliveryDate=" + deliveryDate +
                ", produits=" + produits +
                '}';
    }
}