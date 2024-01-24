package dsbd.usersmanager.services;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.json.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import dsbd.usersmanager.models.Subscription;
import dsbd.usersmanager.models.SubscriptionRepository;
//import dsbd.usersmanager.services.ProducerKafka;

@Service
public class SubscriptionService {

    @Value("${dsbd.gettimeseries.url}")
    private String GETTIMESERIES_URL;

    private String GETTIMESERIES_URL_QUERY = GETTIMESERIES_URL + "/query";

    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  

    @Autowired
    private SubscriptionRepository repository;

    public Subscription add(Subscription sub){
        //Subscription newOne = new Subscription(sub.),);
        return repository.save(sub);
    }

    public void remove(Subscription sub){
        repository.delete(sub);
    }

    public Iterable<Subscription> getAll(){
        return repository.findAll();
    }

    public void processSubscriptions() {
        publishLogOnTopic("... START thread processSubscription");
        try{
            int mode = 1;
            while(true){

                if (mode == 0){
                    try{
                        String[] args = new String[2];
                        args[0] = "prova";
                        args[1] = "messaggio di prova";
                        publishLogOnTopic(String.format("Nuovo messaggio su topic %s : %s ", args[0], args[1]));
                        ProducerKafka.main(args);
                    }
                    catch(Exception ex){

                    }
                }
                else{
                    ArrayList<String> subAlreadyProcessed = new ArrayList<String>();
                    for( Subscription x : getAll() ){   //interrogando ad ogni ciclo il db saranno processate anche le nuove sottoscrizioni
                        String sub = String.format("%s-%s-%s", x.getStation(),x.getThreshold(),x.getMintime());
                        if(!subAlreadyProcessed.contains(sub)){ //se più consumers si iscrivono allo stesso topic questo deve essere processato una sola volta: sarà il broker a notificare ad entrambi
                            publishLogOnTopic(String.format("CHECK %s ... ", sub));
                            checkDataOverLimit(x);
                            subAlreadyProcessed.add(sub);
                        }
                            
                    }
                }
                
                TimeUnit.SECONDS.sleep(30); //la verifica delle soglie avviene per tutte le sottoscrizioni ogni 30 secondi
            }
        }
        catch(Exception ex){

        }
    }

    private void checkDataOverLimit(Subscription sub) {
        
        try{
            if(sub.getStation() != null && sub.getThreshold() != 0 && sub.getMintime() != 0){

                String _params = "{\"stationcode\":\"%s\" , " +
                    "\"last\":\"%sm\" }"; //mintime espresso in minuti
                String params = String.format(_params, sub.getStation(), sub.getMintime() );
                
                publishLogOnTopic("... START checkDataOverLimit");        
                JSONObject res = new JSONObject( sendHTTPRequest(GETTIMESERIES_URL_QUERY, params));
                JSONArray serie = res.getJSONArray("dataframe");
                boolean sopraSoglia = false;
                boolean sottoSoglia = false;
                float maxDefo = 0; 
                for (int i = 0; i < serie.length(); i++) {
                    JSONObject epoca = (JSONObject)serie.get(i);
                    if( !JSONObject.NULL.equals(epoca.get("splp_op")) ){
                        float defo = Float.valueOf( epoca.get("splp_op").toString() );
                        maxDefo = defo > maxDefo ? defo : maxDefo;
                        if (defo > sub.getThreshold() ) 
                            sopraSoglia = true; //il valore della deformazione è sempre positivo
                        else {
                            sottoSoglia = true; //basta un solo valore sottosoglia per escludere la notifica
                            break;
                        }
                    }                    
                }

                if ( sopraSoglia && !sottoSoglia ){
                    LocalDateTime now = LocalDateTime.now();
                    //tutte le epoche sono sopra soglia, quindi si pubblica al broker
                    String[] args = new String[2];
                    args[0] = String.format("%s-%s-%s", sub.getStation(), sub.getThreshold(), sub.getMintime());
                    args[1] = String.format("%s -> %s-%s-%s: Supero soglia, max deformazione %s mm", dtf.format(now), sub.getStation(), sub.getThreshold(), sub.getMintime(), maxDefo);
                    try{
                        publishLogOnTopic(String.format("%s -> %s : %s ", dtf.format(now), args[0], args[1]));
                        ProducerKafka.main(args);
                    }
                    catch(Exception ex){

                    }
                }       
            }
        }
        catch(Exception ex){

        }
    }

    private String sendHTTPRequest(String url, String params) throws IOException{

        byte[] params_bytes = params.getBytes( StandardCharsets.UTF_8 );

        URL getTimeSeriesUrl = new URL(GETTIMESERIES_URL_QUERY);
        HttpURLConnection conn = (HttpURLConnection) getTimeSeriesUrl.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty( "Content-Type", "application/json"); 
        try( DataOutputStream wr = new DataOutputStream( conn.getOutputStream())) { wr.write( params_bytes ); }

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

    public void publishLogOnTopic(String message){
        LocalDateTime now = LocalDateTime.now();
        String[] args = new String[2];
        args[0] = "usersmanager_LOG";
        args[1] = String.format("%s -> %s", dtf.format(now), message);
        try{            
            ProducerKafka.main(args);
        }
        catch(Exception ex){

        }
    }

}
