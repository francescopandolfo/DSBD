package dsbd.gettimeseries.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import dsbd.gettimeseries.models.TimeSeries;
import dsbd.gettimeseries.services.TimeSeriesService;
import io.micrometer.core.annotation.Timed;

@Controller
@RequestMapping(path= "/gettimeseries")
public class TimeSeriesController {
    
    @Autowired
    TimeSeriesService service;

    @PostMapping(path= "/query")
    @Timed(value = "query.time", description = "Tempo necessario per richiamare i dati da Influxdb")
    public @ResponseBody String query(@RequestBody TimeSeries ts){
        return service.get(ts);
    }

    @RequestMapping(path="/hello")
    public @ResponseBody String hello(){
        return "hello world!!!!!!!!!!";
    }
}
