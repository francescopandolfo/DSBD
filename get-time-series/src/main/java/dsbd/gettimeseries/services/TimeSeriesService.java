package dsbd.gettimeseries.services;

import org.springframework.stereotype.Service;

import dsbd.gettimeseries.models.TimeSeries;

import java.net.URL;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
//import java.util.Optional;


@Service
public class TimeSeriesService {
    private String INFLUXDB_URL = "http://10.200.81.108:8086/query";
    private String INFLUXDB_DATABASE = "oe";
    private String INFLUXDB_SAMPLEQUERY = "SELECT * FROM SPIDER.%s WHERE reference_site = 'ENIC' and time >= '%sT00:00:00Z' and time <= '%sT23:59:59Z' limit 10";
    private String INFLUXDB_PARAMS = "db=" + INFLUXDB_DATABASE + "&q=" + INFLUXDB_SAMPLEQUERY;
    
    public String get(TimeSeries ts){
        //return String.format("%s - %s - %s", ts.getStationcode(), ts.getStartdate(), ts.getEnddate());
        return createQuery_Influxdb(ts);
    }

    private String createQuery_Influxdb(TimeSeries ts) {
        
        String toReturn = "NO RESPONSE!";
        try{
            URL InfluxdbUrl = new URL(INFLUXDB_URL);
            HttpURLConnection connToInfluxdb = (HttpURLConnection) InfluxdbUrl.openConnection();

            connToInfluxdb.setRequestMethod("GET");
            //connToInfluxdb.setRequestProperty("Content-Type", "application/json");

            return sendHTTPRequest(InfluxdbUrl, connToInfluxdb, String.format(INFLUXDB_PARAMS, ts.getStationcode(), ts.getStartdate(), ts.getEnddate() ));
            //connToInfluxdb.setReadTimeout(6000);
        }
        catch(Exception ex){

        }
        
        return toReturn;
    }

    private String sendHTTPRequest(URL url, HttpURLConnection con, String params) throws IOException{
        con.setDoOutput(true);
        OutputStream os = con.getOutputStream();
        os.write(params.getBytes());
        os.flush();
        os.close();

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
