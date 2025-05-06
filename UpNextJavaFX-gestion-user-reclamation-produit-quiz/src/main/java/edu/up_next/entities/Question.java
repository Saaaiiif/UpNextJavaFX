package edu.up_next.entities;

public class Question {
    private int id;
    private String texteQuestion;
    private String option1;
    private String option2;
    private String option3;
    private String option4;
    private String reponseCorrecte;
    private Quiz quiz;

    public Question() {}

    public Question(String texteQuestion, String option1, String option2,
                    String option3, String option4, String reponseCorrecte) {
        this.texteQuestion = texteQuestion;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.option4 = option4;
        this.reponseCorrecte = reponseCorrecte;
    }

    public Question(String texteQuestion, String option1, String option2,
                    String option3, String option4, String reponseCorrecte, Quiz quiz) {
        this(texteQuestion, option1, option2, option3, option4, reponseCorrecte);
        this.quiz = quiz;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTexteQuestion() { return texteQuestion; }
    public void setTexteQuestion(String texteQuestion) { this.texteQuestion = texteQuestion; }

    public String getOption1() { return option1; }
    public void setOption1(String option1) { this.option1 = option1; }

    public String getOption2() { return option2; }
    public void setOption2(String option2) { this.option2 = option2; }

    public String getOption3() { return option3; }
    public void setOption3(String option3) { this.option3 = option3; }

    public String getOption4() { return option4; }
    public void setOption4(String option4) { this.option4 = option4; }

    public String getReponseCorrecte() { return reponseCorrecte; }
    public void setReponseCorrecte(String reponseCorrecte) { this.reponseCorrecte = reponseCorrecte; }

    public Quiz getQuiz() { return quiz; }
    public void setQuiz(Quiz quiz) { this.quiz = quiz; }

    @Override
    public String toString() {
        return "Question [id=" + id + ", texte=" + texteQuestion + ", options=" +
                option1 + "/" + option2 + "/" + option3 + "/" + option4 +
                ", réponse=" + reponseCorrecte + "]";
    }
}