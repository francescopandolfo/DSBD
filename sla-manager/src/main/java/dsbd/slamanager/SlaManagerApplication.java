package dsbd.slamanager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SlaManagerApplication {

	public static boolean debug = false;

	public static void main(String[] args) {
		SpringApplication.run(SlaManagerApplication.class, args);
	}

	public static String sendHTTPRequest(String type, String url, String params){
		try{
			URL _url = new URL(url);
			HttpURLConnection con = (HttpURLConnection) _url.openConnection();

			con.setDoOutput(true);
			con.setRequestMethod(type);
			
			if(params != null){
				OutputStream os = con.getOutputStream();
				os.write(params.getBytes());
				os.flush();
				os.close();
			}
			
			BufferedReader in;
			int responseCode = con.getResponseCode();
			if(responseCode == HttpURLConnection.HTTP_OK) in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			else in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
			
			String inputLine;
			StringBuffer response = new StringBuffer();
			while ((inputLine = in.readLine()) != null) response.append(inputLine);

			in.close();
			return response.toString();
		}
		catch(Exception ex){
			return "ERRORE: " + ex.getMessage();
		}
    }

	public static String getFloatFormatted(Float number, int number_decimal){
		DecimalFormat df = new DecimalFormat("#." + String.format("%0"+String.valueOf(number_decimal)+"d", 0).replace("0","#"));
		return df.format(number);
	} 

}
