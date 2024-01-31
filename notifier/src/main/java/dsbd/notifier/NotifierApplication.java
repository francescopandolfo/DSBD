package dsbd.notifier;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import dsbd.notifier.services.ConsumerKafka;
import dsbd.notifier.services.ConsumerService;

@SpringBootApplication
public class NotifierApplication {

	public static boolean debug = false;

	public static ApplicationContext applicationContext;

	private static String GROUP_ID = "defo-group";
	private static ArrayList<String> oldThreads; //usata per verificare se vengono creati nuovi topic o eliminati di esistenti

	public static void main(String[] args) {
		applicationContext = SpringApplication.run(NotifierApplication.class, args);

		oldThreads = new ArrayList<String>();
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
				
				String[] args = new String[6];
				args[0] = GROUP_ID;
				args[1] = (String)sub.get("username");
				args[2] = (String)sub.get("station");
				args[3] = sub.get("threshold").toString();
				args[4] = sub.get("mintime").toString();
				args[5] = sub.get("id").toString();

				String nomeThread = String.format("%s-%s-%s-%s",args[1],args[2],args[3],args[4]);

				//iscrizione al topic che notifica il supero soglia
				new Thread(() -> {
					ConsumerKafka.main(args);
				}, nomeThread).start();	

				//iscrizione al topic che notifica la regolarità dei dati (per chiudere le notifiche aperte)
				new Thread(() -> {
					ConsumerKafka.topicCheck(args);
				}, nomeThread).start();	
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
						String nomeThread = String.format( "%s-%s-%s-%s", sub.get("username"), sub.get("station"), sub.get("threshold"), sub.get("mintime") );
						if( !oldThreads.contains(nomeThread) ){
							//nuova sottoscrizione
							String[] args = new String[6];
							args[0] = GROUP_ID;
							args[1] = (String)sub.get("username");
							args[2] = (String)sub.get("station");
							args[3] = sub.get("threshold").toString();
							args[4] = sub.get("mintime").toString();
							args[5] = sub.get("id").toString();

							oldThreads.add(nomeThread);

							Thread thNuovo = getThreadByName(nomeThread);
							if(thNuovo == null){
								new Thread(() -> {
									ConsumerKafka.main(args);
								}, nomeThread).start();
							}
							else if (thNuovo.isInterrupted()) thNuovo.start(); //esisteva in passato ma era stato fermato 					
						}
					}

					//verifica sottoscrizioni cancellate
					ArrayList<String> daCancellare = new ArrayList<>();
					for(String x : oldThreads){
						Boolean ancoraAttivo = false; 
						for( int i=0; i<subs.length(); i++ ){
							JSONObject sub = (JSONObject)subs.get(i);
							String nomeThread = String.format( "%s-%s-%s-%s", sub.get("username"), sub.get("station"), sub.get("threshold"), sub.get("mintime") );
							if(nomeThread.equals(x)){
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
					
					for(String x : daCancellare) oldThreads.remove(x);
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
