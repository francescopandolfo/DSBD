package dsbd.notifier.services;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONObject;
//import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import dsbd.notifier.NotifierApplication;

@Service
public class EmailService {

    private String USERSMANAGER_URL = NotifierApplication.debug ? "http://10.200.100.235:8002" : "http://usersmanager:8080";
    private String GETTIMESERIES_URL = NotifierApplication.debug ? "http://10.200.100.235:8001/gettimeseries" : "http://gettimeseries:8080/gettimeseries";

    private String USERSMANAGER_CONSUMER = USERSMANAGER_URL + "/consumers";
    private String GETTIMESERIES_QUERY = GETTIMESERIES_URL + "/query";

    private void sendEmail(String to, String subject, String body) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("dsbd@ct.ingv.it");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        JavaMailSender mailSender = getJavaMailSender();
        ConsumerKafka.publishLogOnTopic(String.format("INVIO EMAIL A %s: %s",to,subject));
        mailSender.send(message);
    }

    public void newEmail(String username, String topic, String station, String threshold, String mintime){
        //estrai email da consumer
        try{ 
            JSONObject consumer = new JSONObject( sendHTTPRequest_GET(USERSMANAGER_CONSUMER+"/"+username) );
            String emailTo = (String)consumer.get("email");

            //estraggo la serie degli ultimi "minitime" minuti per poterne calcolare ..
            String _params = "{\"stationcode\":\"%s\" , " +
                    "\"last\":\"%sm\" }";
            String params = String.format(_params, station, mintime );

            JSONObject serieTemporale = new JSONObject( sendHTTPRequest_POST(GETTIMESERIES_QUERY, params ) );
            String[] mean_max = getMeanTS(serieTemporale);

            String body = String.format("Ciao %s,\n\n"
                            + "la deformazione del suolo calcolata su %s é sopra la soglia di %smm da più di %s minuti,\n"
                            + "la cui media nel tempo considerato è di %smm e il valore massimo di %smm.\n\n"
                            + "Saluti", username, station, threshold, mintime, mean_max[0], mean_max[1]);

            String mailObject = String.format("Supero soglia deformazione del suolo per %s", station+"-"+threshold+"-"+mintime);
            
            sendEmail(emailTo, mailObject, body);    
        }
        catch(Exception ex){}
    }

    public void newEmailToClose(String username, String topic, String station, String threshold, String mintime){
        //estrai email da consumer
        try{ 
            JSONObject consumer = new JSONObject( sendHTTPRequest_GET(USERSMANAGER_CONSUMER+"/"+username) );
            String emailTo = (String)consumer.get("email");

            //estraggo la serie degli ultimi "minitime" minuti per poterne calcolare ..
            String _params = "{\"stationcode\":\"%s\" , " +
                    "\"last\":\"%sm\" }";
            String params = String.format(_params, station, mintime );

            JSONObject serieTemporale = new JSONObject( sendHTTPRequest_POST(GETTIMESERIES_QUERY, params ) );
            String[] mean_max = getMeanTS(serieTemporale);

            String body = String.format("Ciao %s,\n\n"
                            + "la deformazione del suolo calcolata su %s é rientrata nei valori ordinari.\n\n"
                            + "Saluti", username, station);

            String mailObject = String.format("Supero soglia deformazione del suolo per %s - CLOSED", station+"-"+threshold+"-"+mintime);
            
            sendEmail(emailTo, mailObject, body);    
        }
        catch(Exception ex){}
    }

    private String[] getMeanTS(JSONObject ts){
        String[] toReturn = new String[2];
        try{
            JSONArray serie = ts.getJSONArray("dataframe");
            Double maxDefo = 0.0;
            ArrayList<Double> splp_op = new ArrayList<Double>();

            for (int i = 0; i < serie.length(); i++) {
                JSONObject epoca = (JSONObject)serie.get(i);
                if( !JSONObject.NULL.equals(epoca.get("splp_op")) ){
                    Double defo = Double.valueOf( epoca.get("splp_op").toString() );
                    splp_op.add(defo);
                    maxDefo = defo > maxDefo ? defo : maxDefo;
                }                    
            }

            //calcolo la media dei valori dell'Array
            Double sum = 0.0;
            for(Double x : splp_op)sum += x;
            Double mean = sum / splp_op.size();

            DecimalFormat df = new DecimalFormat("#." + String.format("%02d", 0).replace("0","#"));
            toReturn[0] = df.format(mean);
            //toReturn[0] = String.format("%.2f", mean);
            toReturn[1] = df.format(maxDefo);
        }
        catch(Exception ex){}
        
        return toReturn;
    }

    @Bean
    private JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("mail.ct.ingv.it");
        mailSender.setPort(25);
                
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "false");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");
        
        return mailSender;
    }

    private String sendHTTPRequest_GET(String url) throws IOException{
        URL getTimeSeriesUrl = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) getTimeSeriesUrl.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("GET");

        return sendHTTPRequest(conn);
    }

    private String sendHTTPRequest_POST(String url, String params) throws IOException{

        byte[] params_bytes = params.getBytes( StandardCharsets.UTF_8 );

        URL getTimeSeriesUrl = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) getTimeSeriesUrl.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty( "Content-Type", "application/json"); 
        try( DataOutputStream wr = new DataOutputStream( conn.getOutputStream())) { wr.write( params_bytes ); }

        return sendHTTPRequest(conn);
    }

    private String sendHTTPRequest(HttpURLConnection conn) throws IOException{
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