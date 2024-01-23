package dsbd.notifier;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import dsbd.notifier.services.ConsumerKafka;
import dsbd.notifier.services.ConsumerService;

@SpringBootApplication
public class NotifierApplication {

	private static String GROUP_ID = "defo-group";
	private static ArrayList<String> oldSubs; //usata per verificare se vengono creati nuovi topic o eliminati di esistenti

	public static void main(String[] args) {
		SpringApplication.run(NotifierApplication.class, args);

		oldSubs = new ArrayList<String>();
		subscribeTopics();

		new Thread(() -> {
			checkNewSubscriptions();
		}, "checkNewSubscriptions").start();
		
	}

	private static void subscribeTopics(){
		
		try{
			String _subs = ConsumerService.getSubscriptions();
			JSONArray subs = new JSONArray(_subs);
			for (int i = 0; i < subs.length(); i++){
				JSONObject sub = (JSONObject)subs.get(i);
				String[] args = new String[2];
				args[0] = String.format( "%s-%s-%s", sub.get("station"), sub.get("threshold"), sub.get("mintime") ); //nome del topic
				args[1] = GROUP_ID;
				if( !oldSubs.contains(args[0]) ) oldSubs.add(args[0]);

				new Thread(() -> {
					ConsumerKafka.main(args);
				}, args[0]).start();
			}
		}
		catch(Exception ex){}
	}

	private static void checkNewSubscriptions(){
		try{
			while(true){
				//verifica nuove sottoscrizioni
				try{
					String _subs = ConsumerService.getSubscriptions();
					JSONArray subs = new JSONArray(_subs);
					for (int i=0; i < subs.length(); i++){
						JSONObject sub = (JSONObject)subs.get(i);
						String nomeTopic = String.format( "%s-%s-%s", sub.get("station"), sub.get("threshold"), sub.get("mintime") );
						if( !oldSubs.contains(nomeTopic) ){
							//nuova sottoscrizione
							String[] args = new String[2];
							args[0] = nomeTopic;
							args[1] = GROUP_ID;
							oldSubs.add(args[0]);

							Thread thNuovo = getThreadByName(args[0]);
							if(thNuovo == null){
								new Thread(() -> {
									ConsumerKafka.main(args);
								}, args[0]).start();
							}
							else if (thNuovo.isInterrupted()) thNuovo.start(); //esisteva in passato ma era stato fermato 					
						}
					}

					//verifica sottoscrizioni cancellate
					ArrayList<String> daCancellare = new ArrayList<>();
					for(String x : oldSubs){
						Boolean ancoraAttivo = false; 
						for( int i=0; i<subs.length(); i++ ){
							JSONObject sub = (JSONObject)subs.get(i);
							String nomeTopic = String.format( "%s-%s-%s", sub.get("station"), sub.get("threshold"), sub.get("mintime") );
							if(nomeTopic.equals(x)){
								ancoraAttivo = true;
								break;	
							}
						}
						if(!ancoraAttivo){
							Thread thAttivo = getThreadByName(x);
							if(thAttivo != null) thAttivo.interrupt(); //fermo il thread non più utilizzato
							daCancellare.add(x);
						}
					}
					
					for(String x : daCancellare) oldSubs.remove(x);
				}
				catch(Exception ex){}

				TimeUnit.SECONDS.sleep(10);
			}
		}
		catch(Exception ex){}
		
	}

	private static Thread getThreadByName(String threadName) {
		for (Thread t : Thread.getAllStackTraces().keySet()) {
			if (t.getName().equals(threadName)) return t;
		}
		return null;
	}

}
