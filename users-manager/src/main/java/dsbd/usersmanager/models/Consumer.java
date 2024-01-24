package dsbd.usersmanager.models;

import jakarta.validation.constraints.NotNull;

import java.util.Set;

import jakarta.persistence.Column;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table (name="consumers")
public class Consumer {

    @Id
    @NotNull(message = "campo obbligatorio")
    @Column(name = "username")
    private String username;

    @NotNull(message = "campo obbligatorio")
    @Column(name = "email")
    private String email;

    @OneToMany(mappedBy = "consumer")
    private Set<_Subscription> subscriptions;

    public String getUsername(){ return username; }
    public String getEmail(){ return email; }
    
}
