package dsbd.usersmanager.models;

import jakarta.validation.constraints.NotNull;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Consumer {

    @Id
    @NotNull(message = "campo obbligatorio")
    private String username;

    @NotNull(message = "campo obbligatorio")
    private String email;

    public String getUsername(){ return username; }
    public String getEmail(){ return email; }
    
}
