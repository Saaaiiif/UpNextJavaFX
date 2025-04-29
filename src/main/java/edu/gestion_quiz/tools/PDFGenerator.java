package edu.gestion_quiz.tools;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import edu.gestion_quiz.dao.QuestionDAO;
import edu.gestion_quiz.entities.Question;
import edu.gestion_quiz.entities.Quiz;

import java.util.List;

public class PDFGenerator {
    private final QuestionDAO questionDAO;

    public PDFGenerator(QuestionDAO questionDAO) {
        this.questionDAO = questionDAO;
    }

    public void generateAllQuizzesPDF(List<Quiz> quizzes, String outputPath) throws Exception {
        PdfWriter writer = new PdfWriter(outputPath);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Titre du document
        Paragraph title = new Paragraph("Liste des quiz")
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(20)
            .setBold();
        document.add(title);
        document.add(new Paragraph("\n"));

        // Pour chaque quiz
        for (Quiz quiz : quizzes) {
            // Titre du quiz
            Paragraph quizTitle = new Paragraph("Quiz: " + quiz.getTitre())
                .setTextAlignment(TextAlignment.LEFT)
                .setFontSize(16)
                .setBold();
            document.add(quizTitle);

            // Sujet du quiz
            Paragraph quizSubject = new Paragraph("Sujet: " + quiz.getSujet())
                .setTextAlignment(TextAlignment.LEFT)
                .setFontSize(14);
            document.add(quizSubject);
            document.add(new Paragraph("\n"));

            // Table des questions
            Table questionTable = new Table(UnitValue.createPercentArray(new float[]{10, 40, 30, 20}));
            questionTable.setWidth(UnitValue.createPercentValue(100));

            // En-têtes
            questionTable.addHeaderCell("N°");
            questionTable.addHeaderCell("Question");
            questionTable.addHeaderCell("Options");
            questionTable.addHeaderCell("Réponse correcte");

            // Questions
            List<Question> questions = questionDAO.getByQuizId(quiz.getId());
            int questionNumber = 1;
            for (Question question : questions) {
                questionTable.addCell(String.valueOf(questionNumber++));
                questionTable.addCell(question.getTexteQuestion());
                
                // Options
                StringBuilder options = new StringBuilder();
                options.append("1. ").append(question.getOption1()).append("\n");
                options.append("2. ").append(question.getOption2()).append("\n");
                options.append("3. ").append(question.getOption3()).append("\n");
                options.append("4. ").append(question.getOption4());
                questionTable.addCell(options.toString());
                
                // Réponse correcte
                questionTable.addCell(question.getReponseCorrecte());
            }

            document.add(questionTable);
            document.add(new Paragraph("\n\n"));
        }

        document.close();
    }
} 