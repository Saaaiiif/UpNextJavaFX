package edu.up_next.services;






import edu.up_next.entities.Event;
import edu.up_next.entities.Produit;
import edu.up_next.interfaces.IService;
import edu.up_next.tools.MyConnexion;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProduitServices implements IService<Produit> {
    public void viderTableProduit() {
        try {
            String requete = "DELETE FROM produit";
            PreparedStatement pst = MyConnexion.getInstance().getCnx().prepareStatement(requete);
            pst.executeUpdate();
            System.out.println("Table produit vidée avec succès !");
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du vidage de la table produit : " + e.getMessage(), e);
        }
    }
    private String getProduitNom(int produitId) {
        try {
            String requete = "SELECT nom FROM produit WHERE id = ?";
            PreparedStatement pst = MyConnexion.getInstance().getCnx().prepareStatement(requete);
            pst.setInt(1, produitId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getString("nom");
            }
            throw new IllegalArgumentException("Produit non trouvé.");
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération du nom du produit : " + e.getMessage(), e);
        }
    }

    private String getProduitImage(int produitId) {
        try {
            String requete = "SELECT image FROM produit WHERE id = ?";
            PreparedStatement pst = MyConnexion.getInstance().getCnx().prepareStatement(requete);
            pst.setInt(1, produitId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getString("image");
            }
            throw new IllegalArgumentException("Produit non trouvé.");
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération de l'image du produit : " + e.getMessage(), e);
        }
    }

    // Validations
    private void validerNom(String nom) {
        if (nom == null || nom.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom du produit ne peut pas être vide.");
        }
        if (nom.length() > 32) {
            throw new IllegalArgumentException("Le nom du produit ne peut pas dépasser 32 caractères.");
        }
        if (!nom.matches("^[a-zA-Z][a-zA-Z0-9\\s]*$")) {
            throw new IllegalArgumentException("Le nom doit commencer par une lettre et ne contenir que des lettres, chiffres ou espaces.");
        }
        if (nom.matches(".*[~,\"'\\(\\[\\)\\}@¤\\$*!\\?;\\./,].*")) {
            throw new IllegalArgumentException("Le nom ne peut pas contenir des caractères spéciaux comme ~,\"'([})@¤$*!?;./,");
        }
    }

    private void validerImage(String image) {
        if (image == null) return; // Nullable dans la base de données
        if (image.trim().isEmpty()) {
            throw new IllegalArgumentException("L'image du produit ne peut pas être vide.");
        }
        if (!image.matches(".*\\.(png|jpg|PNG|JPG)$")) {
            throw new IllegalArgumentException("L'image doit être au format .png ou .jpg.");
        }
    }

    private void validerPrix(double prix) {
        if (prix <= 0) {
            throw new IllegalArgumentException("Le prix doit être positif et non nul.");
        }
    }

    private void validerCategorie(String categorie) {
        List<String> categoriesValides = List.of("Dessin", "Peinture", "Sculpture", "Edition", "Photographie","Art Numerique");
        if (categorie == null || categorie.trim().isEmpty()) {
            throw new IllegalArgumentException("La catégorie ne peut pas être vide.");
        }
        if (!categoriesValides.contains(categorie)) {
            throw new IllegalArgumentException("La catégorie doit être l'une des suivantes : " + categoriesValides);
        }
    }

    private void validerDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("La description ne peut pas être vide.");
        }
        if (description.length() > 50) {
            throw new IllegalArgumentException("La description ne peut pas dépasser 50 caractères.");
        }
    }

    private void validerApprovalStatus(String approvalStatus) {
        List<String> statutsValides = List.of("PENDING", "ACCEPTED", "REJECTED");
        if (approvalStatus != null && !statutsValides.contains(approvalStatus)) {
            throw new IllegalArgumentException("approval_status doit être l'un des suivants : " + statutsValides);
        }
    }

    private void validerStatut(String statut) {
        List<String> statutsValides = List.of("DISPONIBLE", "VENDU");
        if (statut != null && !statutsValides.contains(statut)) {
            throw new IllegalArgumentException("statut doit être l'un des suivants : " + statutsValides);
        }
    }

    private void validerCommandeId(Integer commandeId) {
        if (commandeId != null && commandeId != 0) {
            try {
                String requete = "SELECT COUNT(*) FROM commande WHERE id = ?";
                PreparedStatement pst = MyConnexion.getInstance().getCnx().prepareStatement(requete);
                pst.setInt(1, commandeId);
                ResultSet rs = pst.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    throw new IllegalArgumentException("La commande spécifiée n'existe pas.");
                }
            } catch (SQLException e) {
                throw new RuntimeException("Erreur lors de la vérification de la commande : " + e.getMessage(), e);
            }
        }
    }

    // Modification de la méthode isProduitUnique pour exclure un ID
    public boolean isProduitUnique(String nom, String image, int idToExclude) {
        try {
            String requete = "SELECT COUNT(*) FROM produit WHERE nom = ? AND image = ? AND id != ?";
            PreparedStatement pst = MyConnexion.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, nom);
            pst.setString(2, image);
            pst.setInt(3, idToExclude);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
            throw new SQLException("Erreur lors de la vérification d'unicité.");
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la vérification d'unicité : " + e.getMessage(), e);
        }
    }

    // Surcharge pour l'ancienne méthode (utilisée dans ajouterEntite)
    public boolean isProduitUnique(String nom, String image) {
        return isProduitUnique(nom, image, 0); // 0 signifie qu'on ne veut exclure aucun ID
    }

    public List<Produit> getProduitsAcceptes() {
        List<Produit> data = new ArrayList<>();
        try {
            String requete = "SELECT * FROM produit WHERE approval_status = 'ACCEPTED' AND statut = 'DISPONIBLE'";
            PreparedStatement pst = MyConnexion.getInstance().getCnx().prepareStatement(requete);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Produit p = new Produit();
                p.setId(rs.getInt("id"));
                p.setNom(rs.getString("nom"));
                p.setImage(rs.getString("image"));
                p.setPrix(rs.getDouble("prix"));
                p.setCategorie(rs.getString("categorie"));
                p.setDescription(rs.getString("description"));
                p.setApprovalStatus(rs.getString("approval_status"));
                p.setStatut(rs.getString("statut"));
                data.add(p);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des produits acceptés : " + e.getMessage(), e);
        }
        return data;
    }

    @Override
    public int ajouterEntite(Produit produit) {
        validerNom(produit.getNom());
        validerImage(produit.getImage());
        validerPrix(produit.getPrix());
        validerCategorie(produit.getCategorie());
        validerDescription(produit.getDescription());
        validerCommandeId(produit.getCommandeId() == 0 ? null : produit.getCommandeId());

        if (!isProduitUnique(produit.getNom(), produit.getImage())) {
            throw new IllegalArgumentException("Un produit avec ce nom et cette image existe déjà.");
        }

        validerApprovalStatus(produit.getApprovalStatus());
        validerStatut(produit.getStatut());

        try {
            String requete = "INSERT INTO produit (commande_id, nom, image, prix, categorie, description) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pst = MyConnexion.getInstance().getCnx().prepareStatement(requete, Statement.RETURN_GENERATED_KEYS);
            pst.setObject(1, produit.getCommandeId() == 0 ? null : produit.getCommandeId());
            pst.setString(2, produit.getNom());
            pst.setString(3, produit.getImage());
            pst.setDouble(4, produit.getPrix());
            pst.setString(5, produit.getCategorie());
            pst.setString(6, produit.getDescription());
            int rowsInserted = pst.executeUpdate();
            if (rowsInserted > 0) {
                ResultSet generatedKeys = pst.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    produit.setId(generatedId);
                    System.out.println("Produit ajouté avec succès ! ID: " + generatedId);
                    return generatedId;
                }
            }
            throw new SQLException("Échec de la récupération de l'ID généré pour le produit.");
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'ajout du produit : " + e.getMessage(), e);
        }
    }

    @Override
    public void modifierEntite(Produit produit) {
        if (produit.getId() == 0) {
            throw new IllegalArgumentException("L'ID du produit ne peut pas être 0.");
        }

        if (produit.getNom() != null) {
            validerNom(produit.getNom());
            String imageToCheck = produit.getImage() != null ? produit.getImage() : getProduitImage(produit.getId());
            if (!isProduitUnique(produit.getNom(), imageToCheck, produit.getId())) { // Ajout de l'exclusion de l'ID
                throw new IllegalArgumentException("Un produit avec ce nom et cette image existe déjà.");
            }
        }
        if (produit.getImage() != null) {
            validerImage(produit.getImage());
            String nomToCheck = produit.getNom() != null ? produit.getNom() : getProduitNom(produit.getId());
            if (!isProduitUnique(nomToCheck, produit.getImage(), produit.getId())) { // Ajout de l'exclusion de l'ID
                throw new IllegalArgumentException("Un produit avec ce nom et cette image existe déjà.");
            }
        }
        if (produit.getPrix() != 0) {
            validerPrix(produit.getPrix());
        }
        if (produit.getCategorie() != null) {
            validerCategorie(produit.getCategorie());
        }
        if (produit.getDescription() != null) {
            validerDescription(produit.getDescription());
        }
        validerApprovalStatus(produit.getApprovalStatus());
        validerStatut(produit.getStatut());
        validerCommandeId(produit.getCommandeId() == 0 ? null : produit.getCommandeId());

        try {
            StringBuilder requete = new StringBuilder("UPDATE produit SET ");
            List<Object> params = new ArrayList<>();
            boolean hasUpdates = false;

            if (produit.getNom() != null) {
                requete.append("nom = ?, ");
                params.add(produit.getNom());
                hasUpdates = true;
            }
            if (produit.getImage() != null) {
                requete.append("image = ?, ");
                params.add(produit.getImage());
                hasUpdates = true;
            }
            if (produit.getPrix() != 0) {
                requete.append("prix = ?, ");
                params.add(produit.getPrix());
                hasUpdates = true;
            }
            if (produit.getCategorie() != null) {
                requete.append("categorie = ?, ");
                params.add(produit.getCategorie());
                hasUpdates = true;
            }
            if (produit.getDescription() != null) {
                requete.append("description = ?, ");
                params.add(produit.getDescription());
                hasUpdates = true;
            }
            if (produit.getApprovalStatus() != null) {
                requete.append("approval_status = ?, ");
                params.add(produit.getApprovalStatus());
                hasUpdates = true;
            }
            if (produit.getStatut() != null) {
                requete.append("statut = ?, ");
                params.add(produit.getStatut());
                hasUpdates = true;
            }
            if (produit.getCommandeId() != 0) {
                requete.append("commande_id = ?, ");
                params.add(produit.getCommandeId());
                hasUpdates = true;
            }

            if (!hasUpdates) {
                System.out.println("Aucune modification à effectuer.");
                return;
            }

            requete.setLength(requete.length() - 2);
            requete.append(" WHERE id = ?");
            params.add(produit.getId());

            PreparedStatement pst = MyConnexion.getInstance().getCnx().prepareStatement(requete.toString());
            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof String) {
                    pst.setString(i + 1, (String) param);
                } else if (param instanceof Double) {
                    pst.setDouble(i + 1, (Double) param);
                } else if (param instanceof Integer) {
                    pst.setInt(i + 1, (Integer) param);
                }
            }

            int rowsUpdated = pst.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Produit modifié avec succès !");
            } else {
                throw new IllegalArgumentException("Aucun produit trouvé avec l'ID : " + produit.getId());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la modification du produit : " + e.getMessage(), e);
        }
    }

    @Override
    public void supprimerEntite(Produit produit, int id) {
        try {
            String requete = "DELETE FROM produit WHERE id = ?";
            PreparedStatement pst = MyConnexion.getInstance().getCnx().prepareStatement(requete);
            pst.setInt(1, id);
            int rowsDeleted = pst.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Produit supprimé avec succès !");
            } else {
                throw new IllegalArgumentException("Aucun produit trouvé avec l'ID : " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression du produit : " + e.getMessage(), e);
        }
    }

    @Override
    public void addUser(Produit user) {

    }

    @Override
    public void deleteUser(Produit user) {

    }

    @Override
    public void updateUser(Produit user) {

    }

    @Override
    public List<Produit> getAllData() {
        List<Produit> data = new ArrayList<>();
        try {
            String requete = "SELECT * FROM produit";
            PreparedStatement pst = MyConnexion.getInstance().getCnx().prepareStatement(requete);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Produit p = new Produit();
                p.setId(rs.getInt("id"));
                p.setCommandeId(rs.getInt("commande_id"));
                p.setNom(rs.getString("nom"));
                p.setImage(rs.getString("image"));
                p.setPrix(rs.getDouble("prix"));
                p.setCategorie(rs.getString("categorie"));
                p.setDescription(rs.getString("description"));
                p.setApprovalStatus(rs.getString("approval_status"));
                p.setStatut(rs.getString("statut"));
                data.add(p);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des produits : " + e.getMessage(), e);
        }
        return data;
    }

    @Override
    public void addEntity(Produit produit) {

    }

    @Override
    public void deleteEntity(Produit produit) throws SQLException {

    }

    @Override
    public void updateEntity(Produit produit, int id) throws SQLException {

    }

    @Override
    public void updateEntity(Event event) {

    }
}