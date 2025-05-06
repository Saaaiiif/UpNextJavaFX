package edu.up_next.entities;

import java.util.ArrayList;
import java.util.List;
import edu.up_next.tools.MyConnexion;
import java.sql.Statement;

public class Quiz {
    private int id;
    private String titre;
    private String sujet;
    private List<Question> questions = new ArrayList<>();

    public Quiz() {}

    public Quiz(String titre, String sujet) {
        this.titre = titre;
        this.sujet = sujet;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getSujet() { return sujet; }
    public void setSujet(String sujet) { this.sujet = sujet; }
    public List<Question> getQuestions() { return questions; }
}