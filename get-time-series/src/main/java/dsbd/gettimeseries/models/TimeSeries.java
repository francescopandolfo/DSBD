package dsbd.gettimeseries.models;

import jakarta.validation.constraints.NotNull;

public class TimeSeries {
    
    @NotNull(message = "campo obbligatorio")
    private String stationcode, startdate, enddate, last;  //i campi vanno scritti rigorosamente in minuscolo o non vengono associati in automatico alla querystring della chiamata REST

    //il nome deve essere "get" + il nome del campo con sola la prima maiuscola 
    public String getStationcode(){ return stationcode; }   
    public String getStartdate(){ return startdate; }
    public String getEnddate(){ return enddate; }
    public String getLast(){ return last; }

}
