package dsbd.slamanager.services;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dsbd.slamanager.SlaManagerApplication;
import dsbd.slamanager.models.Metric;
import dsbd.slamanager.models.MetricRepository;

@Service
public class MetricService {

    public String PROMETHEUS_URL = SlaManagerApplication.debug ? "http://10.200.100.235:8003" : "http://prometheus:9090";
    
    @Autowired
    private MetricRepository repository;

    public ArrayList<Metric> discoveryMetrics(String job){
        try{
            ArrayList<Metric> toReturn = new ArrayList<Metric>();
            JSONObject allMetrics =  new JSONObject(SlaManagerApplication.sendHTTPRequest("GET", PROMETHEUS_URL + String.format("/api/v1/series?match[]={job=\"%s\"}", job), null));
            JSONArray _metrics = allMetrics.getJSONArray("data");
            if (_metrics != null){
                for (int i=0; i<_metrics.length(); i++){
                    JSONObject metric = (JSONObject)_metrics.get(i);
                    Metric x = new Metric();
                    x.setName(metric.get("__name__").toString());
                    x.setJob(job);
                    x.setMethod(metric.has("method") ? metric.get("method").toString() : "");

                    toReturn.add(x);
                }
            }
            return toReturn;
        }
        catch(Exception ex){ return null;}
    }

    public String discoveryJobs(){
        return SlaManagerApplication.sendHTTPRequest("GET", PROMETHEUS_URL + "/api/v1/series?match[]=up", null);
    }

    public String add(Metric metric){
        Iterable<Metric> all = repository.findAll();
        boolean aggiornato = false;
        for(Metric x : all) if( x.getName().equals(metric.getName()) && x.getJob().equals(metric.getJob()) && x.getMethod().equals(metric.getMethod()) ) aggiornato = true;

        repository.save(metric);
        if(aggiornato) return String.format("Metrica \"%s-%s-%s\" aggiornata con successo", metric.getName(), metric.getJob(), metric.getMethod());
        else return String.format("Nuova metrica \"%s-%s-%s\" aggiunta allo SLA-set.", metric.getName(), metric.getJob(), metric.getMethod());
    }

    public Metric get(String name, String job){
        Iterable<Metric> all = repository.findAll();
        for(Metric x : all){
            if(x.getName().equals(name) && x.getJob().equals(job)){
                return x;
            }
        }
        return null;
    }

    public Iterable<Metric> getAll(){
        return repository.findAll();
    }

    public String delete(String name, String job, String method){
        Iterable<Metric> all = repository.findAll();
        boolean cancellato = false;
        for(Metric x : all){
            if(x.getName().equals(name) && x.getJob().equals(job) && x.getMethod().equals(method)){
                repository.delete(x);
                cancellato = true;
                break;
            }
        }
        if(cancellato) return String.format("Cancellazione della metrica \"%s-%s\" dallo SLA-set avvenuta con successo!",name, job);
        else return "ERRORE: La metrica inserita non è presente nello SLA-set.";
    }

    public Iterable<String> getState(){
        ArrayList<String> metriche = new ArrayList<String>();

        for( Metric x : repository.findAll() ){
            metriche.add(getState(x));
        }

        return metriche;
    }

    private String getState(Metric metric){
        String queryUrl = PROMETHEUS_URL + "/api/v1/query";
        String res = SlaManagerApplication.sendHTTPRequest("GET", queryUrl, composeParamsState(metric));
        try{
            JSONObject response = new JSONObject(res);
            JSONObject _data = response.getJSONObject("data");
            JSONObject _result = (JSONObject)_data.getJSONArray("result").get(0);
            JSONArray _value = _result.getJSONArray("value");
            Float value = Float.valueOf(_value.get(1).toString());
            return String.format("{ %s-%s-%s : { status : %s , range : %s - %s, value : %s } }", 
                metric.getName(), metric.getJob(), metric.getMethod(), 
                value < Float.valueOf(metric.getMin_value()) || value > Float.valueOf(metric.getMax_value()) ? "KO" : "OK", 
                metric.getMin_value(), 
                metric.getMax_value(), 
                SlaManagerApplication.getFloatFormatted(value, 3)
                );

        }
        catch(Exception ex){ return "ERRORE: " + ex.getMessage();}
    }

    public ArrayList<String> getViolations(){
        ArrayList<String> violazioni = new ArrayList<String>();

        for( Metric x : repository.findAll() ){
            violazioni.add(getViolation(x, 1));
            violazioni.add(getViolation(x, 3));
            violazioni.add(getViolation(x, 6));
            violazioni.add("");
        }

        return violazioni;
    }

    private String getViolation(Metric metric, int last){   //last in minuti
        String queryUrl = PROMETHEUS_URL + "/api/v1/query_range";
        String res = SlaManagerApplication.sendHTTPRequest("GET", queryUrl, composeParamsViolation(metric, last)); 
        try{
            JSONObject response = new JSONObject(res);
            JSONObject _data = response.getJSONObject("data");
            JSONObject _result = (JSONObject)_data.getJSONArray("result").get(0);
            JSONArray _values = _result.getJSONArray("values");
            int contaViolazioni = 0;

            for(int i=0; i < _values.length(); i++){
                Float value = Float.valueOf(((JSONArray)_values.get(i)).get(1).toString());
                if (value < metric.getMin_value() || value > metric.getMax_value()) contaViolazioni++;
            }

            String vm = String.format("{ %s-%s-%s : { violations_%sh : %s} }", 
                metric.getName(), metric.getJob(), metric.getMethod(), last, contaViolazioni);
                
            return vm;
        }
        catch(Exception ex){ return "ERRORE: " + ex.getMessage();}
    }

    private String composeParamsState(Metric metric){
        String toReturn = String.format("avg_over_time(%s{job='%s', method='%s'}[1m])", metric.getName(), metric.getJob(), metric.getMethod());
        if( metric.getFunction() != null)
            switch(metric.getFunction().equals(null) ? "" : metric.getFunction()){
                case "rate":
                    toReturn = String.format("rate(%s{job='%s', method='%s'}[1m])*50", metric.getName(), metric.getJob(), metric.getMethod());
                    break;
            }
        return "query=" + toReturn;
    }

    private String composeParamsViolation(Metric metric, int last){
        String toReturn = composeParamsState(metric);
        String start = String.format("start=%s", Instant.now().plus(-1*last, ChronoUnit.HOURS).toString());
        String end = String.format("end=%s", Instant.now().toString());
        String step = "step=1m";



        //&start=2024-01-29T22:00:00.000Z&end=2024-01-29T22:10:00.000Z&step=1m
        return toReturn + String.format("&%s&%s&%s", start, end, step);
    }


}
