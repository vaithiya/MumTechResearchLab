package postman;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONArray;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class JsonFileCreter extends JsonTestCaseFormate {

	private static final String FILE_PATH = "/Users/vaithy/Documents/postman/DataDesignForPostman.xlsx";
	
	public JSONObject jobj = null;

	public static void main(String args[]) throws IOException, JSONException {
		String rqstValidation = "requestValidation";
		String requestValidation[] = new String[7];
		requestValidation[0] = rqstValidation + " Required";
		requestValidation[1] = rqstValidation + " MaxLength";
		requestValidation[2] = rqstValidation + " Float";
		requestValidation[3] = rqstValidation + " Date";
		requestValidation[4] = rqstValidation + " String";
		requestValidation[5] = rqstValidation + " Integer";
		requestValidation[6] = rqstValidation + " ArrayFormat";
		JsonFileCreter jf = new JsonFileCreter();
		JSONObject overalJson = jf.readClientProperty(requestValidation);

		try (FileWriter file = new FileWriter("/Users/vaithy/Documents/postman/TestScript_Result/wholeTestcases/post_clientProperty_postman_collection.json")) {
			file.write(overalJson.toString());
			System.out.println("Successfully Copied JSON Object to File...");
			System.out.println("\nJSON Object: " + overalJson);
		}
	}

	@SuppressWarnings({ "unchecked", "resource", "rawtypes" })
	public JSONObject readClientProperty(String requestValidation[]) throws JSONException {
		FileInputStream fis = null;

		try {
			fis = new FileInputStream(FILE_PATH);

			// Using XSSF for xlsx format, for xls use HSSF
			Workbook workbook = new XSSFWorkbook(fis);

			Map<String, String> environmentSetup = new LinkedHashMap<String, String>();
			environmentSetup.put("Local_Env", "{{LCLROOT}}clientProperty");
			environmentSetup.put("Test_Env", "{{TSTROOT}}clientProperty");

			JSONArray envItemArray = new JSONArray();
			int caseId = 0;
			
			int env = 0;
			
			for (Map.Entry<String, String> entry : environmentSetup.entrySet()) {
				
				// looping over each workbook sheet
				JSONArray overAllItemArray = new JSONArray();

				Sheet sheet = workbook.getSheetAt(0);

				for (int j = 0; j < 7; j++) {

					JSONArray eachCaseItemArray = new JSONArray();
					int itemNum = 0;
					// iterating over each row
					Iterator rowIterator = sheet.iterator();

					while (rowIterator.hasNext()) {

						Row row = (Row) rowIterator.next();

						Cell _json_key = row.getCell(0);
						Cell _format_type = row.getCell(1);
						Cell _key_size = row.getCell(2);
						Cell _key_mandatory = row.getCell(3);
						
						int caseType = 0;
						
						String keyName = _json_key.getStringCellValue().trim();
						String keyType = _format_type.getStringCellValue().trim();
						String keySize = _key_size.toString().trim();
						int keyLength = 0;
						if (!keySize.equals("N/A") && !keySize.equals("")) {
							keyLength = Integer.parseInt(keySize);
						}
						Boolean keyMandate = _key_mandatory.getStringCellValue().trim().equals("yes");
						
						
						if (keyType.equals("number")) {
							caseType = 1;
						} else if (keyType.equals("string")) {
							caseType = 2;
						} else if (keyType.equals("date")) {
							caseType = 3;
						} else if (keyType.equals("array")) {
							caseType = 4;
						} else if (keyType.equals("float")) {
							caseType = 5;
						}
						
						
						JSONObject caseItemJson = new JSONObject();
						switch (j) {
						case 0:
							if (keyMandate) {
								if (caseType == 1 || caseType == 5 || caseType == 4) {
									caseItemJson = super.createTestCaseForRequired(jobj, caseId, requestValidation[j] + "(null)", 0,
											keyName, null, "POST", entry.getValue());
									eachCaseItemArray.add(itemNum, caseItemJson);
									itemNum++;
								} else {
									caseItemJson = super.createTestCaseForRequired(jobj, caseId, requestValidation[j] + "(null)", 0,
											keyName, null, "POST", entry.getValue());
									eachCaseItemArray.add(itemNum, caseItemJson);
									itemNum++;
									caseItemJson = super.createTestCaseForRequired(jobj, caseId, requestValidation[j], 0,
											keyName, "", "POST", entry.getValue());
									eachCaseItemArray.add(itemNum, caseItemJson);
									itemNum++;
								}
								
							}
							break;
						case 1:
							if (keyLength != 0) {
								if (caseType == 1) {
									String checkValue = String.format("%0" + (keyLength + 2) + "d", 3);
									caseItemJson = super.createTestCaseForRequired(jobj, caseId, requestValidation[j], caseType,
											keyName, "1" + checkValue, "POST", entry.getValue());
									eachCaseItemArray.add(itemNum, caseItemJson);
									itemNum++;
								} else if (caseType == 2 || caseType == 3) {
									String checkValue = String.format("%0" + (keyLength + 2) + "d", 1);
									caseItemJson = super.createTestCaseForRequired(jobj, caseId, requestValidation[j], 0,
											keyName, checkValue, "POST", entry.getValue());
									eachCaseItemArray.add(itemNum, caseItemJson);
									itemNum++;
								} else if (caseType == 4) {
									caseItemJson = super.createTestCaseForRequired(jobj, caseId, requestValidation[j], caseType,
											keyName, "integerArray", "POST", entry.getValue());
									eachCaseItemArray.add(itemNum, caseItemJson);
									itemNum++;
								} else if (caseType == 5) {
									String checkValue = String.format("%0" + keyLength + "d", 3);
									caseItemJson = super.createTestCaseForRequired(jobj, caseId, requestValidation[j], caseType,
											keyName, "1" + checkValue + ".004", "POST", entry.getValue());
									eachCaseItemArray.add(itemNum, caseItemJson);
									itemNum++;
								}
							}
							break;
						case 2:
							if (caseType == 5) {
								caseItemJson = super.createTestCaseForRequired(jobj, caseId, requestValidation[j], 1,
										keyName, "10", "POST", entry.getValue());
								eachCaseItemArray.add(itemNum, caseItemJson);
								itemNum++;
							}
							break;
						case 3:
							if (caseType == 3) {
								caseItemJson = super.createTestCaseForRequired(jobj, caseId, requestValidation[j], 0,
										keyName, "2019-06-27-17", "POST", entry.getValue());
								eachCaseItemArray.add(itemNum, caseItemJson);
								itemNum++;
							}
							break;
						case 4:
							if (caseType == 2) {
								caseItemJson = super.createTestCaseForRequired(jobj, caseId, requestValidation[j], 5,
										keyName, "10.0000", "POST", entry.getValue());
								eachCaseItemArray.add(itemNum, caseItemJson);
								itemNum++;
							}
							break;
						case 5:
							if (caseType == 1) {
								caseItemJson = super.createTestCaseForRequired(jobj, caseId, requestValidation[j], 0,
										keyName, "Check", "POST", entry.getValue());
								eachCaseItemArray.add(itemNum, caseItemJson);
								itemNum++;
							}
							break;
						case 6:
							if (caseType == 4) {
								caseItemJson = super.createTestCaseForRequired(jobj, caseId, requestValidation[j], 0,
										keyName, "[s,w,21]", "POST", entry.getValue());
								eachCaseItemArray.add(itemNum, caseItemJson);
								itemNum++;
							}
							break;
						}
					}
					JSONObject eachCaseItemJson = new JSONObject();
					eachCaseItemJson.put("item", eachCaseItemArray);
					eachCaseItemJson.put("name", requestValidation[j]);
					overAllItemArray.add(j, eachCaseItemJson);
				}
				
				overAllItemArray.add(7, super.createTestCaseForRequired(jobj, caseId, null, 8, null, null, "POST", entry.getValue()));
				JSONObject envObj = new JSONObject();
				envObj.put("item", overAllItemArray);
				envObj.put("name", entry.getKey());
				envItemArray.add(env, envObj);
				env++;
			}

			JSONObject infoObj = new JSONObject();
			infoObj.put("schema", "https://schema.getpostman.com/json/collection/v2.1.0/collection.json");
			infoObj.put("name", "clientProperty");
			infoObj.put("_postman_id", "ae8a6e55-6f00-41f1-bac9-36592026dc9a");

			JSONObject overalJson = new JSONObject();
			overalJson.put("item", envItemArray);
			overalJson.put("info", infoObj);
			fis.close();
			return overalJson;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private JsonFileCreter() {
		String projectWorkDirectory = System.getProperty("user.dir");
		File currentFile = new File(projectWorkDirectory + "/JsonRequestBody/post_clientproperty.json");
		JSONParser parser = new JSONParser();
			try {
				Object obj = parser.parse(new FileReader(currentFile));
				jobj = (JSONObject)obj;
			} catch (Exception e) {
				
			}
	}
}
