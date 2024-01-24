package dsbd.notifier.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

@Service
public class ConsumerService {

    
    private static String USERSMANAGER_URL = "http://localhost:8002/subscriptions/getall";
    //private static String USERSMANAGER_URL = "http://usersmanager:8080/subscriptions/getall";
    
    public static String getSubscriptions(){
        String toReturn = "NO RESPONSE!";
        try{
            URL usersManagerUrl = new URL(USERSMANAGER_URL);
            HttpURLConnection conn = (HttpURLConnection) usersManagerUrl.openConnection();

            conn.setRequestMethod("GET");
            return sendHTTPRequest(usersManagerUrl,conn, null);
        }
        catch(Exception ex){

        }
        
        return toReturn;
    }

    private static String sendHTTPRequest(URL url, HttpURLConnection con, String params) throws IOException{
        con.setDoOutput(true);
        
        if(params != null){
            OutputStream os = con.getOutputStream();
            os.write(params.getBytes());
            os.flush();
            os.close();
        }
        
        BufferedReader in;
        int responseCode = con.getResponseCode();
        if(responseCode == HttpURLConnection.HTTP_OK) in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        else in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
        
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) response.append(inputLine);

        in.close();
        return response.toString();        
    }
}
