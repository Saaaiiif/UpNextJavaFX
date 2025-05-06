package edu.up_next.dao;

import edu.up_next.entities.Quiz;
import java.util.List;

public interface QuizDAO {
    void ajouter(Quiz quiz);
    void modifier(Quiz quiz);
    void supprimer(int id);
    Quiz getById(int id);
    List<Quiz> getAll();
}