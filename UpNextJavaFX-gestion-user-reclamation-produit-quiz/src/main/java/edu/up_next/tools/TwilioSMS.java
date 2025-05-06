package edu.up_next.tools;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class TwilioSMS {
    // Twilio credentials


    public static final String ACCOUNT_SID = "AC1d51efc52cffd9e69a8ac1be1165ea3f";
    public static final String AUTH_TOKEN = "1f2be08cf585fffd47afaf53449bbeb4";


    // Your Twilio phone number (replace with your Twilio number)


    public static final String FROM_NUMBER = "+19786985375"; // Example: WhatsApp sandbox or Twilio number

    static {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }

    public static void sendSMS(String to, String body) {
        Message.creator(
                new PhoneNumber(to),
                new PhoneNumber(FROM_NUMBER),
                body
        ).create();
    }
} 