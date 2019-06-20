package postman;

import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


public class JsonChecker {

	private JSONObject getRequestBody() {
		String projectWorkDirectory = System.getProperty("user.dir");
		File currentFile = new File(projectWorkDirectory + "/JsonRequestBody/post_clientproperty.json");
		JSONParser parser = new JSONParser();
		JSONObject jobj = null;
			try {
				Object obj = parser.parse(new FileReader(currentFile));
				jobj = (JSONObject)obj;
				JSONArray ar = new JSONArray();
				ar.add(0,10);
				ar.add(0,30);
				//Integer ar[] = new Integer[] {20,30};
				BigDecimal ft = new BigDecimal("10000000000.14");
				jobj.replace("type", ft); 
				
				System.out.println(jobj);
			} catch (Exception e) {
				
			}
		return jobj;
	}
	
	public static void main(String args[]) {
		JsonChecker jc = new JsonChecker();
		jc.getRequestBody();
	}
}
