package edu.up_next.dao.impl;

import edu.up_next.dao.QuestionDAO;
import edu.up_next.entities.Question;
import edu.up_next.entities.Quiz;
import edu.up_next.tools.MyConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionDAOImpl implements QuestionDAO {

    @Override
    public void ajouter(Question question) {
        String sql = "INSERT INTO question (texte_question, option1, option2, option3, option4, reponse_correcte, quiz_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = MyConnexion.getInstance().getCnx().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, question.getTexteQuestion());
            pst.setString(2, question.getOption1());
            pst.setString(3, question.getOption2());
            pst.setString(4, question.getOption3());
            pst.setString(5, question.getOption4());
            pst.setString(6, question.getReponseCorrecte());
            pst.setInt(7, question.getQuiz().getId());
            pst.executeUpdate();

            ResultSet rs = pst.getGeneratedKeys();
            if (rs.next()) {
                question.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void modifier(Question question) {
        String sql = "UPDATE question SET texte_question = ?, option1 = ?, option2 = ?, option3 = ?, option4 = ?, reponse_correcte = ?, quiz_id = ? WHERE id = ?";
        try (PreparedStatement pst = MyConnexion.getInstance().getCnx().prepareStatement(sql)) {
            pst.setString(1, question.getTexteQuestion());
            pst.setString(2, question.getOption1());
            pst.setString(3, question.getOption2());
            pst.setString(4, question.getOption3());
            pst.setString(5, question.getOption4());
            pst.setString(6, question.getReponseCorrecte());
            pst.setInt(7, question.getQuiz().getId());
            pst.setInt(8, question.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void supprimer(int id) {
        String sql = "DELETE FROM question WHERE id = ?";
        try (PreparedStatement pst = MyConnexion.getInstance().getCnx().prepareStatement(sql)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Question getById(int id) {
        String sql = "SELECT q.*, qz.titre_quiz as quiz_titre, qz.sujet as quiz_sujet FROM question q " +
                "JOIN quiz qz ON q.quiz_id = qz.id WHERE q.id = ?";
        try (PreparedStatement pst = MyConnexion.getInstance().getCnx().prepareStatement(sql)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return createQuestionFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Question> getAll() {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT q.*, qz.titre_quiz as quiz_titre, qz.sujet as quiz_sujet FROM question q " +
                "JOIN quiz qz ON q.quiz_id = qz.id";
        try (Statement st = MyConnexion.getInstance().getCnx().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                questions.add(createQuestionFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return questions;
    }

    @Override
    public List<Question> getByQuizId(int quizId) {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT q.*, qz.titre_quiz as quiz_titre, qz.sujet as quiz_sujet FROM question q " +
                "JOIN quiz qz ON q.quiz_id = qz.id WHERE q.quiz_id = ?";
        try (PreparedStatement pst = MyConnexion.getInstance().getCnx().prepareStatement(sql)) {
            pst.setInt(1, quizId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                questions.add(createQuestionFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return questions;
    }

    private Question createQuestionFromResultSet(ResultSet rs) throws SQLException {
        Quiz quiz = new Quiz();
        quiz.setId(rs.getInt("quiz_id"));
        quiz.setTitre(rs.getString("quiz_titre"));
        quiz.setSujet(rs.getString("quiz_sujet"));

        Question question = new Question();
        question.setId(rs.getInt("id"));
        question.setTexteQuestion(rs.getString("texte_question"));
        question.setOption1(rs.getString("option1"));
        question.setOption2(rs.getString("option2"));
        question.setOption3(rs.getString("option3"));
        question.setOption4(rs.getString("option4"));
        question.setReponseCorrecte(rs.getString("reponse_correcte"));
        question.setQuiz(quiz);

        return question;
    }
}