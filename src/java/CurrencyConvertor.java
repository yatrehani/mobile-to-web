import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;

/* 
* This web service fires a request to fixer API and gets the USD to INR rate in the market
* for today and calculates the INR value and sends it as JSON
* Author - Yatin Rehani
* Date - 11-11-16
*/

@WebServlet(name = "CurrencyConvertor", urlPatterns = {"/CurrencyConvertor/*"})
public class CurrencyConvertor extends HttpServlet {

    
    // GET returns a value
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        System.out.println("Console: doGET visited");
        boolean isParseableToFloat = false;
        String returnJSONString = null;
    
        // The dollar value is on the path /dollar so skip over the '/'
        String dollar = (request.getPathInfo()).substring(1);
        System.out.println(dollar);
        Float dollarValue = 0F;
        try
        {
          dollarValue = Float.parseFloat(dollar);
          isParseableToFloat = true;
          System.out.println("Input String is parseable to Float!!");
          System.out.println("Float value of dollar = "+dollarValue);
        }
        catch(NumberFormatException e)
        {
          System.out.println("Input String not parseable to Float!!");
        }
        
        // return 200 if name not provided
        if(dollar.equals("") || !isParseableToFloat) {
            response.setStatus(200);
            returnJSONString = formOutputJSON("Please enter a numeric value", 0);
            PrintWriter out = response.getWriter();
            out.println(returnJSONString);      
            out.close();
            return;      
        }
         /* Fixer API to fetch the USD to INR rate in the international market for today*/
        //////////////////////////////////
        String fixerURL =
                "http://api.fixer.io/latest?base=USD&symbols=USD,INR";
        
        // Fetch the page
        String fixerResponse = fetch(fixerURL);
        /////////////////////////////////
        
        // return 401 if name not in map
        if(fixerResponse == null || fixerResponse.equals("")) {
            // no variable name found in map
            response.setStatus(401);
            returnJSONString = formOutputJSON("Rate returned by URL is null or blank", 0);
            // return the value from a GET request
            return;    
        }
        
        String dollarRate = parseJSONSring(fixerResponse);
        System.out.println("Dollar Rate = "+dollarRate);
        Float dollarRateValue = Float.parseFloat(dollarRate);
        System.out.println("Float Dollar Rate  = "+dollarRateValue);
        
        // Calculate the currency equivalent
        float convertedValue = dollarRateValue * dollarValue;
        System.out.println("Converted Value  = "+convertedValue);
        
        returnJSONString = formOutputJSON("Successfully converted to rupee equivalent", convertedValue);
        System.out.println(returnJSONString);
        
        // Things went well so set the HTTP response code to 200 OK
        response.setStatus(200);
        // tell the client the type of the response
        //response.setContentType("text/plain;charset=UTF-8");
        response.setContentType("application/json");

        // return the value from a GET request
        PrintWriter out = response.getWriter();
        out.println(returnJSONString);      
        out.close();
    }
    
       private String fetch(String urlString) {
        String response = "";
        try {
            URL url = new URL(urlString);
            /*
             * Create an HttpURLConnection.  This is useful for setting headers
             * and for getting the path of the resource that is returned (which 
             * may be different than the URL above if redirected).
             * HttpsURLConnection (with an "s") can be used if required by the site.
             */
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            // Read all the text returned by the server
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            String str;
            // Read each line of "in" until done, adding each to "response"
            while ((str = in.readLine()) != null) {
                // str is one line of text readLine() strips newline characters
                response += str;
            }
            in.close();
        } catch (IOException e) {
            System.out.println("Eeek, an exception");
        }
        return response;
    }
    /*
     * This method parses the JSON String according to the parameters
     * base currency, date, rates and converted currency 
     *
     */
    private static String parseJSONSring(String jsonString)
    {   
        JSONParser parser = new JSONParser();
        String dollatToInrRate = null;

	try {

		Object obj = parser.parse(jsonString);

		JSONObject jsonObject = (JSONObject) obj;

		String base = (String) jsonObject.get("base");
		//System.out.println(base);

		String date = (String) jsonObject.get("date");
		//System.out.println(date);
                
                JSONObject rates = (JSONObject) jsonObject.get("rates");
                //System.out.println("Into job structure, name: " + rates.get("INR"));
                dollatToInrRate = rates.get("INR").toString();

	} catch (Exception e) {
		e.printStackTrace();
	}
        return dollatToInrRate;
    }
    /*
     * This method formats the output of JSON String 
     * in form of message and value
     *
     */
    private static String formOutputJSON(String message, float convertedValue)
    {   
        JSONObject obj = null;

	try {
		obj = new JSONObject();
                obj.put("message", message);
                obj.put("value", new Float(convertedValue));

	} catch (Exception e) {
		e.printStackTrace();
	}
        
        return obj.toJSONString();
    }

    
}