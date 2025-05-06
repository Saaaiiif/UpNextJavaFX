package edu.up_next.services;




import edu.up_next.entities.Commande;
import edu.up_next.entities.Produit;
import edu.up_next.tools.MyConnexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ClientCommandeService {
    private CommandeServices commandeServices = new CommandeServices();

    public void afficherCommandesClient() {
        List<Commande> commandes = commandeServices.getAllData();
        if (commandes.isEmpty()) {
            System.out.println("Aucune commande trouvée.");
            return;
        }

        for (Commande commande : commandes) {
            long secondesDepuisCommande = ChronoUnit.SECONDS.between(commande.getDateCommande(), LocalDateTime.now());
            String supprimerCouleur = secondesDepuisCommande < 24 ? "Jaune/Orange" : (secondesDepuisCommande < 48 ? "Rouge" : "Gris");
            String supprimerEtat = secondesDepuisCommande >= 48 ? "Désactivé" : "Activé";
            String envoyerMailCouleur = secondesDepuisCommande >= 72 ? "Bleu Ciel" : "Gris";
            String envoyerMailEtat = secondesDepuisCommande >= 72 && "EN_ATTENTE".equals(commande.getStatus()) ? "Activé" : "Désactivé";

            StringBuilder produitsNoms = new StringBuilder();
            for (Produit produit : commande.getProduits()) {
                produitsNoms.append(produit.getNom()).append(", ");
            }
            if (produitsNoms.length() > 0) {
                produitsNoms.setLength(produitsNoms.length() - 2);
            }

            System.out.println("Commande ID: " + commande.getId() +
                    ", Prix Total: " + commande.getPrixTotal() + " DT" +
                    ", Status: " + commande.getStatus() +
                    ", Produits: " + produitsNoms +
                    ", Bouton Supprimer: " + supprimerCouleur + " (" + supprimerEtat + ")" +
                    ", Bouton Envoyer Mail: " + envoyerMailCouleur + " (" + envoyerMailEtat + ")");
        }
    }

    public void supprimerCommandeClient(int commandeId) {
        Commande commande = getCommande(commandeId);
        long secondesDepuisCommande = ChronoUnit.SECONDS.between(commande.getDateCommande(), LocalDateTime.now());
        if (secondesDepuisCommande >= 48) {
            throw new IllegalStateException("Impossible de supprimer la commande après 48 secondes (heures).");
        }
        if (!"EN_ATTENTE".equals(commande.getStatus())) {
            throw new IllegalStateException("Seules les commandes en attente peuvent être supprimées.");
        }

        Connection conn = MyConnexion.getInstance().getCnx();
        try {
            conn.setAutoCommit(false);

            String requete = "UPDATE produit SET commande_id = NULL, statut = 'DISPONIBLE' WHERE commande_id = ?";
            PreparedStatement pst = conn.prepareStatement(requete);
            pst.setInt(1, commandeId);
            pst.executeUpdate();

            commandeServices.supprimerEntite(commande, commandeId);

            conn.commit();
            System.out.println("Commande supprimée avec succès.");
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            throw new RuntimeException("Erreur lors de la suppression de la commande : " + e.getMessage(), e);
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private Commande getCommande(int commandeId) {
        List<Commande> commandes = commandeServices.getAllData();
        for (Commande commande : commandes) {
            if (commande.getId() == commandeId) {
                return commande;
            }
        }
        throw new IllegalArgumentException("Commande non trouvée.");
    }
}