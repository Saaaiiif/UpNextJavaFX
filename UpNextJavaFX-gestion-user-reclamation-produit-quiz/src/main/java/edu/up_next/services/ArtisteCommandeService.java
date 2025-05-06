package edu.up_next.services;





import edu.up_next.entities.Commande;
import edu.up_next.entities.Produit;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ArtisteCommandeService {
    private CommandeServices commandeServices = new CommandeServices();
    private final ClientCommandeService clientCommandeService;

    public ArtisteCommandeService(ClientCommandeService clientCommandeService) {
        this.clientCommandeService = clientCommandeService;
    }

    public void afficherCommandesArtiste() {
        List<Commande> commandes = commandeServices.getAllData();
        if (commandes.isEmpty()) {
            System.out.println("Aucune commande trouvée.");
            return;
        }

        for (Commande commande : commandes) {
            long secondesDepuisCommande = ChronoUnit.SECONDS.between(commande.getDateCommande(), LocalDateTime.now());
            String fixerDateCouleur = secondesDepuisCommande >= 48 ? "Vert" : "Gris";
            String fixerDateEtat = secondesDepuisCommande >= 48 ? "Activé" : "Désactivé";

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
                    ", Bouton Fixer Date: " + fixerDateCouleur + " (" + fixerDateEtat + ")");
        }
    }

    public void fixerDateLivraison(int commandeId) {
        Commande commande = getCommande(commandeId);
        if ("LIVREE".equals(commande.getStatus())) {
            throw new IllegalStateException("La commande est déjà livrée.");
        }
        long secondesDepuisCommande = ChronoUnit.SECONDS.between(commande.getDateCommande(), LocalDateTime.now());
        if (secondesDepuisCommande < 48) {
            throw new IllegalStateException("Impossible de fixer la date de livraison avant 48 secondes (heures).");
        }

        commande.setDeliveryDate(LocalDateTime.now().plusSeconds(48));
        commande.setStatus("LIVREE");
        commandeServices.modifierEntite(commande);
        System.out.println("Date de livraison fixée et commande marquée comme LIVREE.");
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