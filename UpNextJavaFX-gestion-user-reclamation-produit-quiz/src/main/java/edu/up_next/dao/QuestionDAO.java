package edu.up_next.dao;

import edu.up_next.entities.Question;
import java.util.List;

public interface QuestionDAO {
    void ajouter(Question question);
    void modifier(Question question);
    void supprimer(int id);
    Question getById(int id);
    List<Question> getAll();
    List<Question> getByQuizId(int quizId);
}