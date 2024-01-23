package dsbd.gettimeseries.services;

import org.springframework.stereotype.Service;

import dsbd.gettimeseries.models.TimeSeries;

import java.net.URL;
import java.util.ArrayList;
//import java.util.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import org.json.*;
//import java.util.Optional;


@Service
public class TimeSeriesService {
    private String INFLUXDB_URL = "http://10.200.81.108:8086/query";
    private String INFLUXDB_DATABASE = "oe";
    private String INFLUXDB_QUERY_FROM_TO = "SELECT * FROM SPIDER.%s WHERE reference_site = 'ENIC' and time >= '%sT00:00:00Z' and time <= '%sT23:59:59Z' limit 1000";
    private String INFLUXDB_QUERY_LAST = "SELECT * FROM SPIDER.%s WHERE reference_site = 'ENIC' and time >= now() - %s and time <= now() ";
    private String INFLUXDB_QUERY_LAST_GROUPED = "SELECT time, last(site) as site, median(delta_north) as delta_north, median(delta_east) as delta_east, median(delta_up) as delta_up, median(splp_op) as splp_op FROM SPIDER.%s WHERE reference_site = 'ENIC' and time >= now() - %s and time <= now() GROUP BY time(%s/1000)";
    
    public String get(TimeSeries ts){
        //return String.format("%s - %s - %s", ts.getStationcode(), ts.getStartdate(), ts.getEnddate());
        return JsonRawToJsonDataFrame(createQuery_Influxdb(ts));
    }

    private String createQuery_Influxdb(TimeSeries ts) {
        
        String toReturn = "NO RESPONSE!";
        try{
            URL InfluxdbUrl = new URL(INFLUXDB_URL);
            HttpURLConnection connToInfluxdb = (HttpURLConnection) InfluxdbUrl.openConnection();

            connToInfluxdb.setRequestMethod("GET");
            //connToInfluxdb.setRequestProperty("Content-Type", "application/json");
            
            String _INFLUXDB_QUERY = null;

            if (ts.getLast() != null){
                if(ts.getLast().toUpperCase().contains("D"))
                    _INFLUXDB_QUERY = String.format("db=" + INFLUXDB_DATABASE + "&q=" + INFLUXDB_QUERY_LAST_GROUPED, ts.getStationcode(), ts.getLast(), ts.getLast() );
                else _INFLUXDB_QUERY = String.format("db=" + INFLUXDB_DATABASE + "&q=" + INFLUXDB_QUERY_LAST, ts.getStationcode(), ts.getLast() );
            }
            else _INFLUXDB_QUERY = String.format("db=" + INFLUXDB_DATABASE + "&q=" + INFLUXDB_QUERY_FROM_TO, ts.getStationcode(), ts.getStartdate(), ts.getEnddate() );

            return sendHTTPRequest(InfluxdbUrl, connToInfluxdb, _INFLUXDB_QUERY);
            
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

    private String JsonRawToJsonDataFrame(String json_raw){
        String toReturn = "{ \"dataframe\" : [] }";
        try{
            JSONObject obj = new JSONObject(json_raw);
            JSONObject _results = (JSONObject)obj.getJSONArray("results").get(0);
            if(_results.has("series")){
                JSONObject series = (JSONObject)((JSONObject)obj.getJSONArray("results").get(0)).getJSONArray("series").get(0);
  
                /* estraggo i campi per ricavarne gli indici*/
                ArrayList<String> columns = new ArrayList<String>();     
                JSONArray _columns = series.getJSONArray("columns"); 
                if (_columns != null)
                    for (int i=0; i<_columns.length(); i++)columns.add(_columns.getString(i));
                
                String jsonForDataFrame = "{ \"dataframe\" : [";
                JSONArray _records = series.getJSONArray("values");
                for(int i = 0; i < _records.length(); i++){
                    jsonForDataFrame += String.format("{\"time\": \"%s\",", ((JSONArray)_records.get(i)).get(columns.indexOf("time")));
                    jsonForDataFrame += String.format("\"site\": \"%s\",", ((JSONArray)_records.get(i)).get(columns.indexOf("site")));
                    jsonForDataFrame += String.format("\"delta_north\": %s,", ((JSONArray)_records.get(i)).get(columns.indexOf("delta_north")));
                    jsonForDataFrame += String.format("\"delta_east\": %s,", ((JSONArray)_records.get(i)).get(columns.indexOf("delta_east")));
                    jsonForDataFrame += String.format("\"delta_up\": %s,", ((JSONArray)_records.get(i)).get(columns.indexOf("delta_up")));
                    jsonForDataFrame += String.format("\"splp_op\": %s", ((JSONArray)_records.get(i)).get(columns.indexOf("splp_op")));
                    jsonForDataFrame += "},";
                }
                jsonForDataFrame = jsonForDataFrame.substring(0, jsonForDataFrame.length()-1);
                jsonForDataFrame += "]}";
                    
                toReturn = jsonForDataFrame;
            }
            
        }
        catch(Exception ex){
            toReturn = ex.getMessage();
        }
        

        return toReturn;
    }

}
