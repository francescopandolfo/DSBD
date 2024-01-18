package dsbd.gettimeseries.models;

import jakarta.validation.constraints.NotNull;

public class TimeSeries {
    
    @NotNull(message = "campo <stationCode> obbligatorio")
    private String stationcode, startdate, enddate;  //i campi vanno scritti rigorosamente in minuscolo o non vengono associati in automatico alla querystring della chiamata REST

    public String getStationcode(){ //il nome deve essere "get" + il nome del campo con sola la prima maiuscola 
        return stationcode;
    }
    
   
    public String getStartdate(){
        return startdate;
    }

    public String getEnddate(){
        return enddate;
    }

}
