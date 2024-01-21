package dsbd.usersmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import dsbd.usersmanager.services.ProducerKafka;

@SpringBootApplication
public class UsersManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(UsersManagerApplication.class, args);
		//ProducerKafka.main(args);

	}

}
