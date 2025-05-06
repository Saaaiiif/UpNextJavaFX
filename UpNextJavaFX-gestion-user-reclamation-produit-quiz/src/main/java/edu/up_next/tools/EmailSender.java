package edu.up_next.tools;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailSender {

    // Fonction pour envoyer un e-mail
    public static void sendEmail(String to, String subject, String body) {
        // Configuration des propriétés du serveur SMTP
        String host = "smtp.gmail.com"; // Serveur SMTP Gmail
        final String user = "symfonymailing34@gmail.com"; // Votre adresse email
        final String password = "mxnh mmzj lgyv jndc"; // Votre mot de passe (ou un mot de passe d'application Gmail)

        // Paramètres de la session
        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", "587"); // ✅ Utiliser 587
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true"); // ✅ Activer STARTTLS
        props.put("mail.smtp.ssl.protocols", "TLSv1.2"); // ✅ S'assurer que TLSv1.2 est utilisé
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com"); // ✅ Éviter les erreurs SSL

        // Création de la session
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        });

        try {
            // Création du message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(user)); // Expéditeur
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to)); // Destinataire dynamique
            message.setSubject(subject); // Sujet de l'e-mail
            message.setText(body); // Corps de l'e-mail

            // Envoi de l'e-mail
            Transport.send(message);
            System.out.println("✅ Email envoyé avec succès !");
        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("❌ Erreur lors de l'envoi de l'email.");
        }
    }

/*
        try {
            // Créer un objet MimeMessage
            MimeMessage message = new MimeMessage(session);

            // Définir l'expéditeur
            message.setFrom(new InternetAddress(user));

            // Définir le destinataire
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            // Définir le sujet
            message.setSubject(subject);

            // Définir le corps de l'e-mail
            message.setText(body);

            // Envoyer l'e-mail
            Transport.send(message);
            System.out.println("Email envoyé avec succès !");
        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("Erreur lors de l'envoi de l'email.");
        }


 */
    }

