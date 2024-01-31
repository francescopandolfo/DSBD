package dsbd.slamanager.api;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import dsbd.slamanager.services.MetricService;
import dsbd.slamanager.models.Metric;

@Controller
@RequestMapping(path= "/slamanager")
public class MetricController {

    @Autowired
    MetricService service;

    @PostMapping(path="/discoveryMetrics")
    public @ResponseBody Iterable<Metric> discoveryMetrics(@RequestBody String body){
        try{
            JSONObject obj = new JSONObject(body);
            return service.discoveryMetrics((String)obj.get("job"));
        }
        catch(Exception ex){
            return null;
        }        
    }

    @GetMapping(path="/discoveryJobs")
    public @ResponseBody String discoveryJobs(){
        return service.discoveryJobs();
    }
    
    @PutMapping(path="/metrics/register")   //PUT per garantire l'inserimento (se non esiste il record) e l'aggiornamento (se già esiste)
    public @ResponseBody String register(@RequestBody Metric metric){
        return service.add(metric);
    }

    @GetMapping(path="/metrics/{name}&{job}")
    public @ResponseBody Metric get(@PathVariable String name, @PathVariable String job){
        return service.get(name, job);
    }

    @GetMapping(path="/metrics/all")
    public @ResponseBody Iterable<Metric> getAll(){
        return service.getAll();
    }

    @DeleteMapping(path="/metrics/{name}&{job}&{method}")
    public @ResponseBody String delete(@PathVariable String name, @PathVariable String job, @PathVariable String method){
        return service.delete(name, job, method);
    }

    @GetMapping(path="/getState")
    public @ResponseBody Iterable<String> getState(){
        return service.getState();
    }

    @GetMapping(path="/violations")
    public @ResponseBody Iterable<String> getViolations(){
        return service.getViolations();
    }

    @GetMapping(path="/violations/probability/{minutes}")
    public @ResponseBody Iterable<String> getProbabilityViolation(@PathVariable String minutes){
        return service.getProbabilityViolation(minutes);

    }
}
