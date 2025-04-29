package edu.gestion_quiz.dao;

import edu.gestion_quiz.entities.Question;
import java.util.List;

public interface QuestionDAO {
    void ajouter(Question question);
    void modifier(Question question);
    void supprimer(int id);
    Question getById(int id);
    List<Question> getAll();
    List<Question> getByQuizId(int quizId);
}