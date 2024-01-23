package dsbd.usersmanager.services;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import org.json.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dsbd.usersmanager.models.Subscription;
import dsbd.usersmanager.models.SubscriptionRepository;

@Service
public class SubscriptionService {

    private String GETTIMESERIES_URL = "http://localhost:8001/gettimeseries/query";
    
    @Autowired
    private SubscriptionRepository repository;

    public Subscription add(Subscription sub){
        return repository.save(sub);
    }

    public Iterable<Subscription> getAll(){
        return repository.findAll();
    }

    public void processSubscriptions() {
        try{
            int mode = 1;
            while(true){

                if (mode == 0){
                    try{
                        String[] args = new String[2];
                        args[0] = "prova";
                        args[1] = "messaggio di prova";
                        System.out.println(String.format("Nuovo messaggio su topic %s : %s ", args[0], args[1]));
                        ProducerKafka.main(args);
                    }
                    catch(Exception ex){

                    }
                }
                else{
                    for( Subscription x : getAll() ){   //interrogando ad ogni ciclo il db saranno processate anche le nuove sottoscrizioni
                        System.out.println(String.format("%s: Verifico supero soglia su stazione %s soglia di %s mm e min-tempo di %s minuti ... ", x.getUsername(), x.getStation(), x.getThreshold(), x.getMintime()));
                        checkDataOverLimit(x);
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

                String _params = "{\"stationcode\" : \"%s\"," +
                    "\"last\" : \"%sm\"}"; //mintime espresso in minuti
                String params = String.format(_params, sub.getStation(), sub.getMintime() );
                
                JSONObject res = new JSONObject( sendHTTPRequest(GETTIMESERIES_URL, params));
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

                if ( sopraSoglia && !sottoSoglia){
                    //tutte le epoche sono sopra soglia, quindi si pubblica al broker
                    String[] args = new String[2];
                    args[0] = String.format("%s-%s-%s", sub.getStation(), sub.getThreshold(), sub.getMintime());
                    args[1] = String.format("Notifica sul topic %s-%s-%s: \nSupero soglia, deformazione massima raggiunta %s mm", sub.getStation(), sub.getThreshold(), sub.getMintime(), maxDefo);
                    try{
                        System.out.println(String.format("Nuovo messaggio su topic %s : %s ", args[0], args[1]));
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

        URL getTimeSeriesUrl = new URL(GETTIMESERIES_URL);
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

}
