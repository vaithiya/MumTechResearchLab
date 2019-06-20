import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonTestCaseFormate {
	public JSONObject createTestCaseForRequired () throws JSONException {
		JSONObject obj = new JSONObject();
		obj.put("Name", "crunchify.com");
		obj.put("Author", "App Shah");
 
		JSONArray company = new JSONArray();
		company.put(0, "Compnay: Oracle");
		company.put(1,"Compnay: Paypal");
		company.put(2,"Compnay: Google");
		obj.put("Company List", company);
		
		return obj;
 
		
	}
}
