package dsbd.gettimeseries.models;

import jakarta.validation.constraints.NotNull;

public class TimeSeries {
    
    //i campi vanno scritti rigorosamente in minuscolo o non vengono associati in automatico alla querystring della chiamata REST
    @NotNull(message = "campo obbligatorio")
    private String stationcode;  
    
    private String last, startdate, enddate;

    //il nome deve essere "get" + il nome del campo con sola la prima maiuscola 
    public String getStationcode(){ return stationcode; }   
    public String getStartdate(){ return startdate; }
    public String getEnddate(){ return enddate; }
    public String getLast(){ return last; }

}
