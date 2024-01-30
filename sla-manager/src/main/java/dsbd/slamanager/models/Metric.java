package dsbd.slamanager.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@IdClass(MetricCompositeKey.class)
@Table (name="metrics")
public class Metric {

    @Id
    @Column(name = "name")
    private String name;

    @Id
    @Column(name = "job")
    private String job;

    @Id
    @Column(name = "method")
    private String method;

    @NotNull(message = "exception: the name parameter cannot be blank!")
    @Column(name = "valuemin")
    private Float min_value;
    @Column(name = "valuemax")
    private Float max_value; //la creazione del campo maxvalue andava in errore, con "_" non più ...

    @Column(name = "func")
    private String function;

    public String getName(){ return name; }
    public String getJob(){ return job; }
    public String getMethod(){ return method; }
    public Float getMin_value(){ return min_value; }
    public Float getMax_value(){ return max_value; }
    public String getFunction(){ return function; }

    public void setName(String n){ this.name = n; }
    public void setJob(String j){ this.job = j; }
    public void setMethod(String m){ this.method = m; }
    
}
