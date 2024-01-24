package dsbd.notifier.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Value("${dsbd.usersmanager.url}")
    private String USERSMANAGER_URL = "http://10.200.180.237:8002/consumers";

    

    private void sendEmail(String to, String subject, String body) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("dsbd@ct.ingv.it");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        JavaMailSender mailSender = getJavaMailSender();
        mailSender.send(message);
    }

    public void newEmail(String username, String topic, String station, String threshold, String mintime){

        //estrai email da consumer
        try{ 
            JSONObject consumer = new JSONObject( sendHTTPRequest_GET(USERSMANAGER_URL+"/"+username) );
            String emailTo = (String)consumer.get("email");

            String body = String.format("Ciao %s,\n\n"
                            + "la deformazione del suolo sulla stazione %s é sopra la soglia di %smm da più di %s minuti.\n\n"
                            + "Saluti", username, station, threshold, mintime);

            String mailObject = String.format("Supero soglia deformazione del suolo per %s", station+"-"+threshold+"-"+mintime);
            
            sendEmail(emailTo, mailObject, body);    
        }
        catch(Exception ex){}
    }

    @Bean
    private JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("mail.ct.ingv.it");
        mailSender.setPort(25);
        
        //mailSender.setUsername("robogps@ct.ingv.it");
        //mailSender.setPassword("Utopia69");
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "false");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");
        
        return mailSender;
    }

    private String sendHTTPRequest_GET(String url) throws IOException{

        //byte[] params_bytes = params.getBytes( StandardCharsets.UTF_8 );

        URL getTimeSeriesUrl = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) getTimeSeriesUrl.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("GET");
        //conn.setRequestProperty( "Content-Type", "application/json"); 
        //try( DataOutputStream wr = new DataOutputStream( conn.getOutputStream())) { wr.write( params_bytes ); }

        BufferedReader in;
        int responseCode = conn.getResponseCode();
        if(responseCode == HttpURLConnection.HTTP_OK) in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        else in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) response.append(inputLine);

        in.close();
        return response.toString();        
    }
}