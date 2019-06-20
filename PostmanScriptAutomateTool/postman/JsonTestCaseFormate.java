package postman;

import org.json.simple.JSONArray;

import java.math.BigDecimal;

import org.json.JSONException;
import org.json.simple.JSONObject;

public class JsonTestCaseFormate {
	
	
	
	@SuppressWarnings("unchecked")
	public JSONObject createTestCaseForRequired(JSONObject jobj, int caseId, String caseTypeAndkey, int caseType, String keyName,
			String value, String methodType, String urlEnvironment) throws JSONException {

		// Event body
		JSONArray eventArray = new JSONArray();
		JSONObject eventJson = new JSONObject();
		eventJson.put("script", this.scriptJson(caseType, keyName, value, caseId));
		eventJson.put("listen", "test");
		eventArray.add(0, eventJson);

		// request body
		JSONObject requestJson = new JSONObject();
		requestJson.put("url", this.urlSettingParam(urlEnvironment));
		requestJson.put("body", this.requestSettingParam(jobj, methodType, caseType,  keyName, value));
		requestJson.put("header", this.headerArray());
		requestJson.put("method", methodType.toString());
		
		JSONObject caseJson = new JSONObject();
		// response body
		caseJson.put("response", new JSONArray());
		
		caseJson.put("request", requestJson);
		
		caseJson.put("event", eventArray);
		// case Name
		if (caseType == 8) {
			caseJson.put("name", "SuccessCase");
		} else {
			caseJson.put("name", "Failure:" + caseTypeAndkey +": "+ keyName);
		}
		return caseJson;

	}

	@SuppressWarnings("unchecked")
	private JSONArray headerArray() {
		JSONObject headerJson = new JSONObject();
		headerJson.put("type", "text");
		headerJson.put("value", "application/json");
		headerJson.put("name", "Content-Type");
		headerJson.put("key", "Content-Type");
		JSONArray headerArray = new JSONArray();
		headerArray.add(0, headerArray);
		return headerArray;
	}

	@SuppressWarnings("unchecked")
	private JSONObject scriptJson(int caseType, String keyName, String value, int caseId) throws JSONException {
		JSONObject scriptJson = new JSONObject();
		
		scriptJson.put("type", "text/javascript");
		
		if (caseType == 8) {
			scriptJson.put("exec", this.getJsonForSuccessCase());
		} else {
			scriptJson.put("exec", this.getJsonCase(caseType, keyName, value));
		}
		scriptJson.put("id", caseId);
		return scriptJson;
	}

	@SuppressWarnings("unchecked")
	private JSONObject urlSettingParam(String urlEnvironment) throws JSONException {
		JSONObject urlJsonBody = new JSONObject();
		JSONArray urlArray = new JSONArray();
		urlArray.add(0, urlEnvironment);
		urlJsonBody.put("host", urlArray);
		urlJsonBody.put("raw", urlEnvironment);
		return urlJsonBody;
	}

	@SuppressWarnings("unchecked")
	private JSONObject requestSettingParam(JSONObject jobj, String methodType, int caseType, String key, String value) throws JSONException {
		JSONObject requestJsonBody = new JSONObject();
		if ( caseType == 0) {
			jobj.replace(key, value);
			//as it is
		} else if (caseType == 1) {
			Integer valueInt = Integer.parseInt(value);
			jobj.replace(key, valueInt); 
		} else if (caseType == 4) {
			JSONArray ar = new JSONArray();
			ar.add(0,10);
			ar.add(0,30);
			ar.add(0,4);
			ar.add(0,5);
			ar.add(0,7);
			jobj.replace(key, ar);
		} else if (caseType == 5) {
			BigDecimal ft = new BigDecimal(value);
			jobj.replace(key, ft);
		}
		switch (methodType) {
		case "POST":
			requestJsonBody.put("raw", jobj);
			requestJsonBody.put("mode", "raw");
			case "GET":
			break;
		case "PUT":
			break;
		}
		return requestJsonBody;
	}

	@SuppressWarnings("unchecked")
	private JSONArray getJsonCase(int caseType, String keyName, String value) throws JSONException {
		JSONArray caseArray = new JSONArray();
		int i = 0;
		caseArray.add(i, "pm.test(\"Failure POST request\", function() {");
		i++;
		if ( value == null) {
			caseArray.add(i, "    pm.environment.set(\"" + keyName + "\", " + value + ");");
			i++;
		} else {
			if (caseType == 6 || caseType == 4) {
				caseArray.add(i, "    pm.environment.set(\"" + keyName + "\", " + value + ");");
				i++;
			} else {
				caseArray.add(i, "    pm.environment.set(\"" + keyName + "\", \"" + value + "\");");
				i++;
			}
		}
		caseArray.add(i, "    pm.expect(pm.response.code).to.be.oneOf([400]);");
		i++;
		caseArray.add(i, "});");
		i++;
		return caseArray;
	}
	
	@SuppressWarnings("unchecked")
	private JSONArray getJsonForSuccessCase() throws JSONException {
		JSONArray caseArray = new JSONArray();
		caseArray.add(0, "pm.test(\"Success POST request\", function() {");
		caseArray.add(1, "    pm.expect(pm.response.code).to.be.oneOf([200, 201, 202]);");
		caseArray.add(2, "});");
		return caseArray;
	}
}
