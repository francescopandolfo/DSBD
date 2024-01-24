package dsbd.usersmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import dsbd.usersmanager.services.SubscriptionService;

@SpringBootApplication
public class UsersManagerApplication {
	
	private static ApplicationContext applicationContext;
	
	public static void main(String[] args) {
		applicationContext = SpringApplication.run(UsersManagerApplication.class, args);

		SubscriptionService subS = (SubscriptionService)applicationContext.getBean("subscriptionService");
		subS.publishLogOnTopic("INVOKE thread processSubscription ... ");

		new Thread(() -> {
			subS.processSubscriptions();
		}).start();
		
	}
}
