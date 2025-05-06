package edu.up_next.dao.impl;

import edu.up_next.dao.QuizDAO;
import edu.up_next.entities.Quiz;
import edu.up_next.tools.MyConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuizDAOImpl implements QuizDAO {

    @Override
    public void ajouter(Quiz quiz) {
        String sql = "INSERT INTO quiz (titre_quiz, sujet) VALUES (?, ?)";
        try (PreparedStatement pst = MyConnexion.getInstance().getCnx().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, quiz.getTitre());
            pst.setString(2, quiz.getSujet());
            pst.executeUpdate();

            ResultSet rs = pst.getGeneratedKeys();
            if (rs.next()) {
                quiz.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void modifier(Quiz quiz) {
        String sql = "UPDATE quiz SET titre_quiz = ?, sujet = ? WHERE id = ?";
        try (PreparedStatement pst = MyConnexion.getInstance().getCnx().prepareStatement(sql)) {
            pst.setString(1, quiz.getTitre());
            pst.setString(2, quiz.getSujet());
            pst.setInt(3, quiz.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void supprimer(int id) {
        String sql = "DELETE FROM quiz WHERE id = ?";
        try (PreparedStatement pst = MyConnexion.getInstance().getCnx().prepareStatement(sql)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Quiz getById(int id) {
        String sql = "SELECT * FROM quiz WHERE id = ?";
        try (PreparedStatement pst = MyConnexion.getInstance().getCnx().prepareStatement(sql)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                Quiz quiz = new Quiz();
                quiz.setId(rs.getInt("id"));
                quiz.setTitre(rs.getString("titre_quiz"));
                quiz.setSujet(rs.getString("sujet"));
                return quiz;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Quiz> getAll() {
        List<Quiz> quizzes = new ArrayList<>();
        String sql = "SELECT * FROM quiz";
        try (Statement st = MyConnexion.getInstance().getCnx().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Quiz quiz = new Quiz();
                quiz.setId(rs.getInt("id"));
                quiz.setTitre(rs.getString("titre_quiz"));
                quiz.setSujet(rs.getString("sujet"));
                quizzes.add(quiz);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return quizzes;
    }
}