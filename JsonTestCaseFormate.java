package postman;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonTestCaseFormate {

	public JSONObject createTestCaseForRequired(int caseId, String caseTypeAndkey, int caseType, String keyName,
			String value, String methodType, String urlEnvironment) throws JSONException {

		// Event body
		JSONArray eventArray = new JSONArray();
		JSONObject eventJson = new JSONObject();
		eventJson.put("script", this.scriptJson(caseType, keyName, value, caseId));
		eventJson.put("listen", "test");
		eventArray.put(0, eventJson);

		// request body
		JSONObject requestJson = new JSONObject();
		requestJson.put("url", this.urlSettingParam(urlEnvironment));
		requestJson.put("body", this.requestSettingParam(methodType));
		requestJson.put("header", new JSONArray());
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

	private JSONObject scriptJson(int caseType, String keyName, String value, int caseId) {
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

	private JSONObject urlSettingParam(String urlEnvironment) {
		JSONObject urlJsonBody = new JSONObject();
		urlJsonBody.put("host", new JSONArray().put(0, urlEnvironment));
		urlJsonBody.put("raw", urlEnvironment);
		return urlJsonBody;
	}

	private JSONObject requestSettingParam(String methodType) {
		JSONObject requestJsonBody = new JSONObject();
		switch (methodType) {
		case "POST":
			requestJsonBody.put("raw",
					"{\n        \"type\":1,\n        \"sale_status\":2,\n        \"ad_comment\":\"※価格・販売戸数は未定です。※販売開始まで契約または予約の申込および申込順位の確保につながる行為は一切できません。※★月販売予定。※第★期の販売住戸が未確定のため、物件データは第★期以降の全販売対象住戸のものを表記しています。確定情報は新規分譲広告にて明示します。\",\n        \"name\":\"野村不動産　プラウドシーズン西落合\",\n        \"sale_housiki\":0,\n        \"sale_start\":2,\n        \"sale_start_year\":2019,\n        \"sale_start_month\":1,\n        \"sale_start_timing\":3,\n        \"sentyaku_year_month_day\":\"2019-04-15\",\n        \"sale_schedule_comment\":\"来月金額公開予定！\",\n        \"postal_code_first\":\"141\",\n        \"postal_code_last\":\"0033\",\n        \"prefectures_list\":1,\n        \"city_list\":1101,\n        \"town_list\":11010000,\n        \"jityou_list\":\"1丁目\",\n        \"tiban\":\"1番地\",\n        \"gouban\":\"7号\",\n        \"address_last\":\"青山外苑ビル9F\",\n        \"latitude\":123.456,\n        \"longitude\":456.123,\n        \"main_access\":1,\n        \"ensen_name\":1002,\n        \"station_name\":100201,\n        \"access_type\":1,\n        \"walk_time\":10,\n        \"bus_time\":20,\n        \"getoff_busstop\":\"六本木駅前\",\n        \"getoff_busstop_walk_time\":15,\n        \"running_distance_1\":10,\n        \"running_distance_2\":2,\n        \"bus_company\":\"東急バス\",\n        \"busstop\":\"新宿駅前\",\n        \"others_access_1\":1,\n        \"ensen_name_1\":1002,\n        \"station_name_1\":100201,\n        \"access_type_1\":1,\n        \"walk_time_1\":10,\n        \"bus_time_1\":20,\n        \"getoff_busstop_1\":\"六本木駅前\",\n        \"getoff_busstop_walk_time_1\":15,\n        \"running_distance_1_1\":10,\n        \"running_distance_1_2\":2,\n        \"bus_company_1\":\"東急バス\",\n        \"busstop_1\":\"新宿駅前\",\n        \"others_access_2\":1,\n        \"ensen_name_2\":1002,\n        \"station_name_2\":100201,\n        \"access_type_2\":1,\n        \"walk_time_2\":10,\n        \"bus_time_2\":20,\n        \"getoff_busstop_2\":\"六本木駅前\",\n        \"getoff_busstop_walk_time_2\":15,\n        \"running_distance_2_1\":10,\n        \"running_distance_2_2\":2,\n        \"bus_company_2\":\"東急バス\",\n        \"busstop_2\":\"新宿駅前\",\n        \"price_setting\":1,\n        \"price_1\":2000,\n        \"price_handle\":2,\n        \"price_2\":3000,\n        \"site_right_type\":1,\n        \"land_area_type\":1,\n        \"land_area_1\":100,\n        \"land_area_under_decimal_1\":15,\n        \"land_area_handle\":1,\n        \"land_area_2\":200,\n        \"land_area_under_decimal_2\":30,\n        \"shidoufutan_type\":0,\n        \"shidoumenseki\":100,\n        \"shidoumenseki_decimal\":11,\n        \"share_ratio\":51,\n        \"share_ratio_under_decimal\":2,\n        \"overall_ratio\":90,\n        \"overall_ratio_under_decimal\":21,\n        \"building_area_type\":1,\n        \"building_area_1\":200,\n        \"building_area_under_decimal_1\":30,\n        \"building_area_handle\":2,\n        \"building_area_2\":300,\n        \"building_area_under_decimal_2\":20,\n        \"sale_house_num_type\":1,\n        \"sale_house_num\":3,\n        \"total_house_num\":10,\n        \"room_num_1\":2,\n        \"layout_type_1\":12,\n        \"service_rule_type_1\":1,\n        \"layout_handle\":1,\n        \"room_num_2\":3,\n        \"layout_type_2\":5,\n        \"service_rule_type_2\":1,\n        \"complete_time_type\":1,\n        \"complete_time_select\":2,\n        \"complete_year\":2019,\n        \"complete_month\":4,\n        \"complete_decade\":4,\n        \"comlete_year_month_day\":\"2019-04-15\",\n        \"complete_after_contract\":2,\n        \"building_situation\":2,\n        \"parking_type\":2,\n        \"advertiser_company_trade_aspect_type\":17,\n        \"trade_aspect_1\":\"\",\n        \"advertiser_company_trade_aspect_type_2\":6,\n        \"company_postalcode_2\":\"141\",\n        \"company_area_num_2\":\"0033\",\n        \"company_address_2\":\"\",\n        \"position_group_name_2\":\"経団連\",\n        \"license_num_2\":\"第HPA-17-10449-1号\",\n        \"company_name_2\":\"\",\n        \"advertiser_company_trade_aspect_type_3\":1,\n        \"company_postalcode_3\":\"141\",\n        \"company_area_num_3\":\"0033\",\n        \"company_address_3\":\"東京都品川区西品川1-2-3-444\",\n        \"position_group_name_3\":\"経団連\",\n        \"license_num_3\":\"第HPA-17-10449-1号\",\n        \"company_name_3\":\"\",\n        \"station_conveniene_selecct\":[1,3],\n        \"dwelling_unit_floors_num_select\":[1],\n        \"lighting_ventication_select\":[1,6,10],\n        \"character_madori_selecet\":[6,7],\n        \"kitchen_concerned_facilities_select\":[1,2,3],\n        \"parking_select\":[1,6],\n        \"reform_renovation_select\":[1,5,6],\n        \"main_inner_image_1\":\"\",\n        \"main_inner_image_category_1\":10,\n        \"main_inner_image_caption_1\":\"\",\n        \"sub_inner_image_2\":\"\",\n        \"sub_inner_image_category_2\":19,\n        \"sub_inner_image_caption_2\":\"\",\n        \"main_outer_image_1\":\"\",\n        \"main_outer_image_category_1\":11,\n        \"main_outer_image_caption_1\":\"\",\n        \"sub_outer_image_2\":\"\",\n        \"sub_outer_image_category_2\":1,\n        \"sub_outer_image_caption_2\":\"\",\n        \"internal_memo\":\"社内のメモです\",\n        \"store_id\":\"IM00001\",\n        \"posting_priority_value\":10,\n        \"jimukyoku_memo\":\"事務局のメモです\"\n}");
			requestJsonBody.put("mode", "raw");
			case "GET":
			break;
		case "PUT":
			break;
		}
		return requestJsonBody;
	}

	private JSONArray getJsonCase(int caseType, String keyName, String value) {
		JSONArray caseArray = new JSONArray();
		int i = 0;
		caseArray.put(i, "pm.test(\"Failure POST request\", function() {");
		i++;
		if ( value == null) {
			caseArray.put(i, "    pm.environment.set(\"" + keyName + "\", " + value + ");");
			i++;
		} else {
			if (caseType == 6 || caseType == 4) {
				caseArray.put(i, "    pm.environment.set(\"" + keyName + "\", " + value + ");");
				i++;
			} else {
				caseArray.put(i, "    pm.environment.set(\"" + keyName + "\", \"" + value + "\");");
				i++;
			}
		}
		caseArray.put(i, "    pm.expect(pm.response.code).to.be.oneOf([400]);");
		i++;
		caseArray.put(i, "});");
		i++;
		return caseArray;
	}
	
	private JSONArray getJsonForSuccessCase() {
		JSONArray caseArray = new JSONArray();
		caseArray.put(0, "pm.test(\"Success POST request\", function() {");
		caseArray.put(1, "    pm.expect(pm.response.code).to.be.oneOf([200, 201, 202]);");
		caseArray.put(2, "});");
		return caseArray;
	}
}
