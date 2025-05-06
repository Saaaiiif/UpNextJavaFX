package edu.up_next.services;




import edu.up_next.entities.Commande;
import edu.up_next.entities.Event;
import edu.up_next.entities.Produit;
import edu.up_next.interfaces.IService;
import edu.up_next.tools.MyConnexion;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandeServices implements IService<Commande> {
    public void viderTableCommande() {
        try {
            // D'abord, dissocier tous les produits des commandes
            String dissocierRequete = "UPDATE produit SET commande_id = NULL";
            PreparedStatement pstDissocier = MyConnexion.getInstance().getCnx().prepareStatement(dissocierRequete);
            pstDissocier.executeUpdate();

            // Ensuite, vider la table commande
            String requete = "DELETE FROM commande";
            PreparedStatement pst = MyConnexion.getInstance().getCnx().prepareStatement(requete);
            pst.executeUpdate();
            System.out.println("Table commande vidée avec succès !");
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du vidage de la table commande : " + e.getMessage(), e);
        }
    }

    private void validerPrixTotal(double prixTotal) {
        if (prixTotal <= 0) {
            throw new IllegalArgumentException("Le prix total doit être positif et non nul.");
        }
    }

    private void validerDateCommande(LocalDateTime dateCommande) {
        if (dateCommande == null) {
            throw new IllegalArgumentException("La date de commande ne peut pas être vide.");
        }
        if (dateCommande.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("La date de commande ne peut pas être dans le futur.");
        }
    }

    private void validerStatus(String status) {
        List<String> statutsValides = List.of("EN_ATTENTE", "LIVREE");
        if (status != null && !statutsValides.contains(status)) {
            throw new IllegalArgumentException("status doit être l'un des suivants : " + statutsValides);
        }
    }

    private void validerDeliveryDate(LocalDateTime dateCommande, LocalDateTime deliveryDate) {
        if (deliveryDate != null) {
            if (deliveryDate.isBefore(dateCommande)) {
                throw new IllegalArgumentException("La date de livraison doit être postérieure à la date de commande.");
            }
            long secondesEntreDates = ChronoUnit.SECONDS.between(dateCommande, deliveryDate);
            if (secondesEntreDates < 48) {
                throw new IllegalArgumentException("La date de livraison doit être au moins 48 secondes (heures) après la date de commande.");
            }
        }
    }

    @Override
    public int ajouterEntite(Commande commande) {
        validerPrixTotal(commande.getPrixTotal());
        validerDateCommande(commande.getDateCommande());
        validerStatus(commande.getStatus());
        validerDeliveryDate(commande.getDateCommande(), commande.getDeliveryDate());

        try {
            String requete = "INSERT INTO commande (prix_total, date_commande, delivery_date) VALUES (?, ?, ?)";
            PreparedStatement pst = MyConnexion.getInstance().getCnx().prepareStatement(requete, Statement.RETURN_GENERATED_KEYS);
            pst.setDouble(1, commande.getPrixTotal());
            pst.setObject(2, commande.getDateCommande());
            pst.setObject(3, commande.getDeliveryDate());
            int rowsInserted = pst.executeUpdate();
            if (rowsInserted > 0) {
                ResultSet generatedKeys = pst.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    commande.setId(generatedId);
                    System.out.println("Commande ajoutée avec succès ! ID: " + generatedId);
                    return generatedId;
                }
            }
            throw new SQLException("Échec de la récupération de l'ID généré pour la commande.");
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'ajout de la commande : " + e.getMessage(), e);
        }
    }

    @Override
    public void modifierEntite(Commande commande) {
        if (commande.getId() == 0) {
            throw new IllegalArgumentException("L'ID de la commande ne peut pas être 0.");
        }

        // Valider les champs actuels et modifiés
        double prixTotalActuel = getCommandePrixTotal(commande.getId());
        validerPrixTotal(prixTotalActuel);
        if (commande.getPrixTotal() != 0) {
            validerPrixTotal(commande.getPrixTotal());
        }
        LocalDateTime dateCommandeActuelle = getCommandeDate(commande.getId());
        validerDateCommande(dateCommandeActuelle);
        if (commande.getDateCommande() != null) {
            validerDateCommande(commande.getDateCommande());
        }
        validerStatus(commande.getStatus());
        if (commande.getDeliveryDate() != null) {
            validerDeliveryDate(
                    commande.getDateCommande() != null ? commande.getDateCommande() : dateCommandeActuelle,
                    commande.getDeliveryDate()
            );
        }

        try {
            StringBuilder requete = new StringBuilder("UPDATE commande SET ");
            List<Object> params = new ArrayList<>();
            boolean hasUpdates = false;

            if (commande.getPrixTotal() != 0) {
                requete.append("prix_total = ?, ");
                params.add(commande.getPrixTotal());
                hasUpdates = true;
            }
            if (commande.getDateCommande() != null) {
                requete.append("date_commande = ?, ");
                params.add(commande.getDateCommande());
                hasUpdates = true;
            }
            if (commande.getStatus() != null) {
                requete.append("status = ?, ");
                params.add(commande.getStatus());
                hasUpdates = true;
            }
            if (commande.getDeliveryDate() != null) {
                requete.append("delivery_date = ?, ");
                params.add(commande.getDeliveryDate());
                hasUpdates = true;
            }

            if (!hasUpdates) {
                System.out.println("Aucune modification à effectuer pour la commande.");
                return;
            }

            requete.setLength(requete.length() - 2);
            requete.append(" WHERE id = ?");
            params.add(commande.getId());

            PreparedStatement pst = MyConnexion.getInstance().getCnx().prepareStatement(requete.toString());
            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof Double) {
                    pst.setDouble(i + 1, (Double) param);
                } else if (param instanceof LocalDateTime) {
                    pst.setObject(i + 1, param);
                } else if (param instanceof String) {
                    pst.setString(i + 1, (String) param);
                } else if (param instanceof Integer) {
                    pst.setInt(i + 1, (Integer) param);
                }
            }

            int rowsUpdated = pst.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Commande modifiée avec succès !");
            } else {
                throw new IllegalArgumentException("Aucune commande trouvée avec l'ID : " + commande.getId());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la modification de la commande : " + e.getMessage(), e);
        }
    }

    private double getCommandePrixTotal(int commandeId) {
        try {
            String requete = "SELECT prix_total FROM commande WHERE id = ?";
            PreparedStatement pst = MyConnexion.getInstance().getCnx().prepareStatement(requete);
            pst.setInt(1, commandeId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getDouble("prix_total");
            }
            throw new IllegalArgumentException("Commande non trouvée.");
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération du prix total : " + e.getMessage(), e);
        }
    }

    private LocalDateTime getCommandeDate(int commandeId) {
        try {
            String requete = "SELECT date_commande FROM commande WHERE id = ?";
            PreparedStatement pst = MyConnexion.getInstance().getCnx().prepareStatement(requete);
            pst.setInt(1, commandeId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getObject("date_commande", LocalDateTime.class);
            }
            throw new IllegalArgumentException("Commande non trouvée.");
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération de la date de commande : " + e.getMessage(), e);
        }
    }

    @Override
    public void supprimerEntite(Commande commande, int id) {
        try {
            String requete = "DELETE FROM commande WHERE id = ?";
            PreparedStatement pst = MyConnexion.getInstance().getCnx().prepareStatement(requete);
            pst.setInt(1, id);
            int rowsDeleted = pst.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Commande supprimée avec succès !");
            } else {
                throw new IllegalArgumentException("Aucune commande trouvée avec l'ID : " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de la commande : " + e.getMessage(), e);
        }
    }

    @Override
    public void addUser(Commande user) {

    }

    @Override
    public void deleteUser(Commande user) {

    }

    @Override
    public void updateUser(Commande user) {

    }

    @Override
    public List<Commande> getAllData() {
        List<Commande> data = new ArrayList<>();
        try {
            String requete = "SELECT * FROM commande";
            PreparedStatement pst = MyConnexion.getInstance().getCnx().prepareStatement(requete);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Commande c = new Commande();
                c.setId(rs.getInt("id"));
                c.setPrixTotal(rs.getDouble("prix_total"));
                c.setDateCommande(rs.getObject("date_commande", LocalDateTime.class));
                c.setStatus(rs.getString("status"));
                c.setDeliveryDate(rs.getObject("delivery_date", LocalDateTime.class));

                // Charger les produits associés
                String requeteProduits = "SELECT * FROM produit WHERE commande_id = ?";
                PreparedStatement pstProduits = MyConnexion.getInstance().getCnx().prepareStatement(requeteProduits);
                pstProduits.setInt(1, c.getId());
                ResultSet rsProduits = pstProduits.executeQuery();
                while (rsProduits.next()) {
                    Produit p = new Produit();
                    p.setId(rsProduits.getInt("id"));
                    p.setCommandeId(rsProduits.getInt("commande_id"));
                    p.setNom(rsProduits.getString("nom"));
                    p.setImage(rsProduits.getString("image"));
                    p.setPrix(rsProduits.getDouble("prix"));
                    p.setCategorie(rsProduits.getString("categorie"));
                    p.setDescription(rsProduits.getString("description"));
                    p.setApprovalStatus(rsProduits.getString("approval_status"));
                    p.setStatut(rsProduits.getString("statut"));
                    c.ajouterProduit(p);
                }
                data.add(c);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des commandes : " + e.getMessage(), e);
        }
        return data;
    }

    @Override
    public void addEntity(Commande commande) {

    }

    @Override
    public void deleteEntity(Commande commande) throws SQLException {

    }

    @Override
    public void updateEntity(Commande commande, int id) throws SQLException {

    }

    @Override
    public void updateEntity(Event event) {

    }

    public Map<String, Double> getChiffreAffairesParCategorie() {
        Map<String, Double> stats = new HashMap<>();
        List<Commande> commandes = getAllData();
        for (Commande commande : commandes) {
            for (Produit produit : commande.getProduits()) {
                String categorie = produit.getCategorie();
                double prix = produit.getPrix();
                stats.put(categorie, stats.getOrDefault(categorie, 0.0) + prix);
            }
        }
        return stats;
    }
}