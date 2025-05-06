package edu.up_next.services;





import edu.up_next.entities.Commande;
import edu.up_next.entities.Produit;
import edu.up_next.tools.MyConnexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PanierService {
    private List<Produit> panier = new ArrayList<>();
    private ProduitServices produitServices = new ProduitServices();
    private CommandeServices commandeServices = new CommandeServices();
    private static final double FRAIS_LIVRAISON = 5.0;
    private static PanierService instance;

    public static PanierService getInstance() {
        if (instance == null) {
            instance = new PanierService();
        }
        return instance;
    }

    public void ajouterAuPanier(int produitId) {
        // Contrôle d'unicité : ne pas ajouter si déjà présent
        for (Produit p : panier) {
            if (p.getId() == produitId) {
                throw new IllegalArgumentException("Ce produit est déjà dans le panier.");
            }
        }
        try {
            String requete = "SELECT * FROM produit WHERE id = ? AND approval_status = 'ACCEPTED' AND statut = 'DISPONIBLE' FOR UPDATE";
            PreparedStatement pst = MyConnexion.getInstance().getCnx().prepareStatement(requete);
            pst.setInt(1, produitId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                Produit produit = new Produit();
                produit.setId(rs.getInt("id"));
                produit.setNom(rs.getString("nom"));
                produit.setImage(rs.getString("image"));
                produit.setPrix(rs.getDouble("prix"));
                produit.setCategorie(rs.getString("categorie"));
                produit.setDescription(rs.getString("description"));
                produit.setApprovalStatus(rs.getString("approval_status"));
                produit.setStatut(rs.getString("statut"));
                panier.add(produit);
                System.out.println("Produit ajouté au panier : " + produit.getNom() + " (Nombre d'articles : " + panier.size() + ")");
            } else {
                throw new IllegalArgumentException("Produit non disponible ou non accepté.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'ajout au panier : " + e.getMessage(), e);
        }
    }

    public void voirPanier() {
        if (panier.isEmpty()) {
            System.out.println("Le panier est vide.");
            return;
        }
        System.out.println("Contenu du panier :");
        for (Produit produit : panier) {
            System.out.println("ID: " + produit.getId() +
                    ", Nom: " + produit.getNom() +
                    ", Description: " + produit.getDescription() +
                    ", Prix: " + produit.getPrix() + " DT");
        }
        double sousTotal = panier.stream().mapToDouble(Produit::getPrix).sum();
        System.out.println("Récapitulatif :");
        System.out.println("Sous-total : " + sousTotal + " DT");
        System.out.println("Livraison Standard : " + FRAIS_LIVRAISON + " DT");
        System.out.println("Total : " + (sousTotal + FRAIS_LIVRAISON) + " DT");
    }

    public void supprimerDuPanier(int produitId) {
        Produit produitASupprimer = null;
        for (Produit produit : panier) {
            if (produit.getId() == produitId) {
                produitASupprimer = produit;
                break;
            }
        }
        if (produitASupprimer != null) {
            panier.remove(produitASupprimer);
            System.out.println("Produit supprimé du panier : " + produitASupprimer.getNom());
        } else {
            System.out.println("Produit non trouvé dans le panier.");
        }
    }

    // Méthode pour appliquer les remises selon la date
    private double appliquerRemise(double prixTotal, java.time.LocalDate dateCommande) {
        // Remise fête des mamans (mai)
        if (dateCommande.getMonthValue() == 5 && dateCommande.getDayOfMonth() >= 1 && dateCommande.getDayOfMonth() <= 31) {
            return prixTotal * 0.60; // 40% de remise
        }
        // Remise Saint-Valentin (1-14 février)
        if (dateCommande.getMonthValue() == 2 && dateCommande.getDayOfMonth() >= 1 && dateCommande.getDayOfMonth() <= 14) {
            return prixTotal * 0.74; // 26% de remise
        }
        // Remise test du 27 au 30 avril
        if (dateCommande.getMonthValue() == 4 && dateCommande.getDayOfMonth() >= 26 && dateCommande.getDayOfMonth() <= 30) {
            return prixTotal * 0.63; // 37% de remise
        }
        // Pas de remise
        return prixTotal;
    }

    // Méthode publique pour obtenir le pourcentage de remise active à une date donnée
    public double getRemiseActivePourcentage(java.time.LocalDate date) {
        if (date.getMonthValue() == 5 && date.getDayOfMonth() >= 1 && date.getDayOfMonth() <= 31) {
            return 40.0;
        }
        if (date.getMonthValue() == 2 && date.getDayOfMonth() >= 1 && date.getDayOfMonth() <= 14) {
            return 26.0;
        }
        if (date.getMonthValue() == 4 && date.getDayOfMonth() >= 26 && date.getDayOfMonth() <= 30) {
            return 37.0;
        }
        return 0.0;
    }

    // Nouvelle méthode pour appliquer la remise sur le total TTC (produits + livraison)
    private double appliquerRemiseSurTotal(double totalBrut, java.time.LocalDate dateCommande) {
        double remise = getRemiseActivePourcentage(dateCommande);
        if (remise > 0) {
            return totalBrut - (totalBrut * (remise / 100.0));
        }
        return totalBrut;
    }

    public void finaliserCommande() {
        if (panier.isEmpty()) {
            throw new IllegalStateException("Le panier est vide.");
        }

        Connection conn = MyConnexion.getInstance().getCnx();
        try {
            conn.setAutoCommit(false);

            // Vérifier que tous les produits sont disponibles
            for (Produit produit : panier) {
                String requete = "SELECT statut, approval_status FROM produit WHERE id = ?";
                PreparedStatement pst = conn.prepareStatement(requete);
                pst.setInt(1, produit.getId());
                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    String statut = rs.getString("statut");
                    String approvalStatus = rs.getString("approval_status");
                    if (!"DISPONIBLE".equals(statut) || !"ACCEPTED".equals(approvalStatus)) {
                        throw new IllegalStateException("Le produit " + produit.getNom() + " n'est plus disponible ou non approuvé.");
                    }
                } else {
                    throw new IllegalStateException("Le produit ID " + produit.getId() + " n'existe pas.");
                }
            }

            // Créer une commande
            double sousTotal = panier.stream().mapToDouble(Produit::getPrix).sum();
            double totalBrut = sousTotal + FRAIS_LIVRAISON;
            LocalDateTime now = LocalDateTime.now();
            double totalRemise = appliquerRemiseSurTotal(totalBrut, now.toLocalDate());
            Commande commande = new Commande();
            commande.setPrixTotal(totalRemise);
            commande.setDateCommande(now);
            commande.setStatus("EN_ATTENTE");
            commande.setProduits(new ArrayList<>(panier));
            int commandeId = commandeServices.ajouterEntite(commande);

            // Associer les produits à la commande et mettre à jour leur statut
            for (Produit produit : panier) {
                produit.setCommandeId(commandeId);
                produit.setStatut("VENDU");
                produitServices.modifierEntite(produit);
            }

            conn.commit();
            System.out.println("Commande finalisée avec succès ! ID: " + commandeId);

            // Vider le panier
            panier.clear();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            throw new RuntimeException("Erreur lors de la finalisation de la commande : " + e.getMessage(), e);
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public int getNombreArticles() {
        return panier.size();
    }





    // Nouvelle méthode pour obtenir le panier
    public List<Produit> getPanier() {
        return new ArrayList<>(panier); // Retourne une copie pour éviter les modifications directes
    }

    // Nouvelle méthode pour calculer le sous-total
    public double getSousTotal() {
        return panier.stream().mapToDouble(Produit::getPrix).sum();
    }

    // Nouvelle méthode pour obtenir les frais de livraison
    public double getFraisLivraison() {
        return FRAIS_LIVRAISON;
    }

    // Nouvelle méthode pour calculer le total avec remise selon la date du jour
    public double getTotal() {
        double totalBrut = getSousTotal() + FRAIS_LIVRAISON;
        java.time.LocalDate today = java.time.LocalDate.now();
        return appliquerRemiseSurTotal(totalBrut, today);
    }

}