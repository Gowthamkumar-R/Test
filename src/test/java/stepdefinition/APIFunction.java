package stepdefinition;

import static io.restassured.RestAssured.given;

import static org.hamcrest.Matchers.equalTo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import javax.xml.crypto.Data;

import org.apache.poi.ddf.EscherColorRef.SysIndexSource;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.databind.deser.Deserializers.Base;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.utilities.AmeriSaveOrder_Data;
import com.utilities.FWUtils;
import com.utilities.GTXOrder_Data;


import io.cucumber.messages.internal.com.google.common.base.CharMatcher;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import junit.framework.Assert;

//This class defines an API function that interacts with a web service.
public class APIFunction extends com.utilities.Base {

	// Store the current directory path.
	public static String rootPath = System.getProperty("user.dir");

	// Initialize properties and configuration file variables.
	static Properties prop = new Properties();
	static File Propfile = new File(
			"C:\\Users\\Gowthamkumar.R\\git\\repositoryFinal\\Cucumberwithapi\\Config.prop\\config.properties");

	// Method to generate the payload for an API request.
	public String APIPayload(String FileNamePath) throws InterruptedException, IOException {

		String responseBody = null;

		// Load properties from the configuration file.
		FileInputStream propfile = new FileInputStream(Propfile);
		prop.load(propfile);

		// String FileNamePath = rootPath + "/Data/CreateRequestBody.json";
		try {
//			test = extent.createTest("ExecuteAPI - Payload");

			// Load the payload from the specified JSON file.
			File file = new File(FileNamePath);
			System.out.println("Read Request payload from  json file  - Success");

			// Send an API request with the loaded payload.
			Response res = given().baseUri(prop.getProperty("api.baseUri"))
					.header("Authorization", "Bearer  " + GenerateTokenAuthorization()).body(file).

					when().post("​/CreateQuoteExtended").

					then().statusCode(200).assertThat().statusCode(200).extract().response();
			System.out.println("Token" + res.body().prettyPrint());
			// Store the API response body.
			responseBody = res.body().prettyPrint(); // res.asString()

		} catch (AssertionError e) {
			System.out.println(e.getMessage());
			System.out.println("Assertion Error:  ----- check the API status code in response " + e.getMessage());
		}
		// Return the API response body.
		return responseBody;
	}

	// Suppress unchecked warnings for this method.
	@SuppressWarnings("unchecked")

	// Method to generate an authorization token for API access.
	public String GenerateTokenAuthorization() throws InterruptedException, IOException {

		// Create a JSON object to hold the request body data.
		JSONObject body = new JSONObject();

		// Load properties from the configuration file.
		FileInputStream propfile = new FileInputStream(Propfile);
		prop.load(propfile);

		// Populate the JSON object with user credentials and titleCompanyID.
		body.put("userName", prop.getProperty("api.username")); // A2
		body.put("password", prop.getProperty("api.password")); // B2
		body.put("titleCompanyID", prop.getProperty("api.titleCompanyID")); // c2

//          FWUtils.readXLPages(driver, "TestData", 0, 1);

		// Send a POST request to the /Token endpoint to obtain an access token.
		String response = given().baseUri(prop.getProperty("baseURI")).contentType(ContentType.JSON)

				// Validate a specific field in the response body.
				.body(body.toJSONString()).when().post("/Token").then().statusCode(200)
				.body("userName", equalTo("user3")).extract().path("accessToken");// Extract the value of the
																					// "accessToken" field from the
																					// response.

		// Print the obtained access token.
		System.out.println("accessToken from response:::::::: " + response);
		// Return the obtained access token.
		return response;
	}

	@SuppressWarnings("unchecked")
	public String GTXTokenGeneration() throws IOException {

		JSONObject body = new JSONObject();

		FileInputStream propfile = new FileInputStream(Propfile);
		prop.load(propfile);

		body.put("userName", prop.getProperty("GTX.api.username"));
		body.put("password", prop.getProperty("GTX.api.password"));
		body.put("titleCompanyID", prop.getProperty("GTX.api.titleCompanyID"));

		Response res = given().baseUri(prop.getProperty("GTX.baseURI")).header("Content-Type", "application/json")
				.contentType(ContentType.JSON).body(body.toJSONString()).when().post("/GtxToken").then().extract()
				.response();

		String resbody = res.asString();

		System.out.println("Token is " + resbody);

		return resbody;

	}

	@SuppressWarnings({ "unchecked", "unused" })
	public void AmerisaveOrder(String fileName) throws InterruptedException, IOException, ParseException {

		File file = new File(fileName);

		FileInputStream propfile = new FileInputStream(Propfile);
		prop.load(propfile);

		int rowCountTotal = FWUtils.getRowCount(driver, "AmeriSaveOrder");

		System.out.println("Row count from the Excel is " + rowCountTotal);

		for (int i = 1; i <rowCountTotal; i++) {

			DataFormatter format = new DataFormatter();
			JSONObject mainBody = new JSONObject();
			// format.formatCellValue(cell)
			String loanReason = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 0);
			String languagePreference = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 1);
			// Loans:
			String loanIdentifier = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 3);
			String lenderName = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 4);
			String loanNumber = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 5);
			
			Object loanAmount = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 6);
			double loanAmountDouble = Double.parseDouble((String) loanAmount);
			int loanAmountInt = (int) loanAmountDouble;
			String purchasePrice = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 7);
			double purchasePriceDouble = Double.parseDouble(purchasePrice);
			int purchasePriceInt = (int) purchasePriceDouble;
			String loanType = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 8);
			String lienPosition = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 9);
			double lienPositionDouble = Double.parseDouble(lienPosition);
			int lienPositionInt = (int) lienPositionDouble;

			// String rate = format.formatCellValue(sheet.getRow(1).getCell(10));
			// double rateInt = Integer.parseInt(rate);
			String rate = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 10);
			double rateDouble = Double.parseDouble(rate);
			int rateInt = (int) rateDouble;
			String term = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 11);
			double termDouble = Double.parseDouble(term);
			int termInt = (int) termDouble;
			String PriorPolicyDate = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 12);
			// Properties:
			String propertyIdString = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 14);
			double propertyIdDouble = Double.parseDouble(propertyIdString);
			int propertyId = (int) propertyIdDouble;
			
			System.out.println("Property ID from the excel is " + propertyId);
			// address:
			String address1Prop = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 16);
			String address2Prop = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 17);
			String cityProp = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 18);
			String stateProp = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 19);
			String fipsProp = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 20);
			String zipCodeProp = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 21);
			double zipCodePropDouble = Double.parseDouble(zipCodeProp);
			int zipCodePropInt = (int) zipCodePropDouble;
			String zipCodePropString = Integer.toString(zipCodePropInt);
			String countyProp = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 22);
			String propertyUsageTypeProp = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 23);
			String propertyTypeProp = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 24);
			// contacts:
			String contactTypeCont = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 26);
			String contactIdCont = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 27);
			String firstNameCont = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 28);
			String lastNameCont = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 29);
			String companyNameCont = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 30);
			String phoneCont = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 31);
			// address:
			String address1 = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 33);
			String address2 = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 34);
			String city = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 35);
			String state = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 36);
			String fips = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 37);
			String zipCode = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 38);
			String county = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 39);
			String email = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 40);
			String contacttype = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 41);
			String contactId = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 42);
			String firstName = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 43);
			String middleName = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 44);
			String lastName = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 45);
			// Object suffix = format.formatCellValue(sheet.getRow(1).getCell(46));

			String result = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 46);
			String suffix = CharMatcher.is('\"').trimFrom(result);

			String ssn = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 47);
			String companyname = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 48);
			// address:
			String address1IND = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 50);
			String address2IND = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 51);
			String cityIND = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 52);
			String stateIND = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 53);
			String fipsIND = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 54);
			String zipCodeIND = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 55);
			String countyIND = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 56);
			String phoneIND = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 57);
			String emailIND = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 58);

			/*********************************************************************************************************/
			mainBody.put("loanReason", loanReason);
			mainBody.put("languagePreference", languagePreference);

			JSONObject loansBody = new JSONObject();
			loansBody.put("loanIdentifier", loanIdentifier);
			loansBody.put("lenderName", lenderName);
			loansBody.put("loanNumber", loanNumber);
			loansBody.put("loanAmount", loanAmountInt);
			loansBody.put("purchasePrice", purchasePriceInt);
			loansBody.put("loanType", loanType);
			loansBody.put("lienPosition", lienPositionInt);
			loansBody.put("rate", rateInt);
			loansBody.put("term", termInt);
			loansBody.put("PriorPolicyDate", PriorPolicyDate);

			JSONArray loansArray = new JSONArray();
			loansArray.add(loansBody);

			/*********************************************************************************************************/
			JSONObject propertiesbody = new JSONObject();
			propertiesbody.put("propertyId", propertyId);
			propertiesbody.put("propertyUsageType", propertyUsageTypeProp);
			propertiesbody.put("propertyType", propertyTypeProp);

			JSONObject addressprop = new JSONObject();
			addressprop.put("address1", address1Prop);
			addressprop.put("address2", address2Prop);
			addressprop.put("city", cityProp);
			addressprop.put("state", stateProp);
			addressprop.put("fips", fipsProp);
			addressprop.put("zipcode", zipCodePropString);
			addressprop.put("county", countyProp);

			propertiesbody.put("address", addressprop);

			JSONArray propertiesArray = new JSONArray();
			propertiesArray.add(propertiesbody);

			/*********************************************************************************************************/
			JSONObject contactsbody = new JSONObject();
			contactsbody.put("contactType", contactTypeCont);
			contactsbody.put("contactId", contactIdCont);
			contactsbody.put("firstName", firstNameCont);
			contactsbody.put("lastName", lastNameCont);
			contactsbody.put("companyName", companyNameCont);
			contactsbody.put("phone", phoneCont);
			contactsbody.put("email", email);

			JSONObject addresscon = new JSONObject();
			addresscon.put("address1", address1);
			addresscon.put("address2", address2);
			addresscon.put("city", city);
			addresscon.put("state", state);
			addresscon.put("fips", fips);
			addresscon.put("zipcode", zipCodePropString);
			addresscon.put("county", county);

			contactsbody.put("address", addresscon);

			/*********************************************************************************************************/
			JSONObject lastbody = new JSONObject();
			lastbody.put("contacttype", contacttype);
			lastbody.put("contactId", contactId);
			lastbody.put("firstName", firstName);
			lastbody.put("middleName", middleName);
			lastbody.put("lastName", lastName);
			lastbody.put("suffix", suffix);
			lastbody.put("ssn", ssn);
			lastbody.put("companyname", companyname);
			lastbody.put("phone", phoneIND);
			lastbody.put("email", emailIND);

			JSONObject addresslastbody = new JSONObject();
			addresslastbody.put("address1", address1IND);
			addresslastbody.put("address2", address2IND);
			addresslastbody.put("city", cityIND);
			addresslastbody.put("state", stateIND);
			addresslastbody.put("fips", fipsIND);
			addresslastbody.put("zipcode", zipCodePropString);
			addresslastbody.put("county", countyIND);

			lastbody.put("address", addresslastbody);

			/*********************************************************************************************************/

			JSONArray contactsArray = new JSONArray();
			contactsArray.add(contactsbody);
			contactsArray.add(lastbody);

			/*********************************************************************************************************/
			mainBody.put("loans", loansArray);
			mainBody.put("properties", propertiesArray);
			mainBody.put("contacts", contactsArray);

			String jsonString = mainBody.toJSONString();

			Gson gson = new GsonBuilder().setPrettyPrinting().create();

			JsonElement jsonElement = JsonParser.parseString(jsonString);

			String prettyJson = gson.toJson(jsonElement);

			System.out.println(prettyJson);

			Response res = (Response) given().baseUri(prop.getProperty("GTX.api.AmeriSaveOrder"))
					.header("Authorization", "Bearer  " + GTXTokenGeneration())
					.header("Content-Type", "application/json").body(prettyJson).when().post("/CreatePlaceOrder").then()
					.extract().response();


			String ResponseFile = res.body().asPrettyString();
			
			
			System.out.println("Response from the API is " + ResponseFile);
			
			

			//Actual Status Code
//			JSONObject jsonObject = new JSONObject(re23);
//			JSONObject getSth = ((Object) jsonObject).getJSONObject("LanguageLevels");
//		    Object level = getSth.get("2");
//			
//			JsonPath jsonPathEvaluator = res.jsonPath();
//			System.out.println("Response from the Body is " + jsonPathEvaluator.get("status"));
			
			  JSONParser parser = new JSONParser();
			 JSONObject jsonObject = (JSONObject) parser.parse(ResponseFile);
			  JSONObject status = (JSONObject) jsonObject.get("status");
			  long Actual = (long) status.get("statusCode");
//			  System.out.println("Status code from the response is" +StatusCode);
			 
//			int Actual = res.getStatusCode(); //200 from server
			String ActualStatusCode = String.valueOf(Actual);
			FWUtils.writeXLPages(driver, "AmeriSaveOrder", i, 61, ActualStatusCode);
			System.out.println("Actul Status code form the server " + Actual);
			
			//Expected Status code
			String expect = FWUtils.readXLPages(driver, "AmeriSaveOrder", i, 59);
			double expectstring = Double.parseDouble(expect);
			int ExpectedStatusCode = (int) expectstring;
            System.out.println("Expected status code in excel is " + ExpectedStatusCode);
            

            int TestStatus = 60;	
			
			if (Actual == ExpectedStatusCode) {
				
				//FWUtils.writeXLPages(driver, "APICreatePlaceOrder", i, Scenario, "Case" + i);
				FWUtils.writeXLPages(driver, "AmeriSaveOrder", i, TestStatus, "PASS");
				FWUtils.writeXLPages(driver, "AmeriSaveOrder", i, 62, ResponseFile);

				System.out.println("Scenario : Case " + i);
				System.out.println("Test Status : " + "PASS");
				
			}
			
            else {

				//FWUtils.writeXLPages(driver, "APICreatePlaceOrder", i, Scenario, "Case" + i);
				FWUtils.writeXLPages(driver, "AmeriSaveOrder", i, TestStatus, "FAIL");
				FWUtils.writeXLPages(driver, "AmeriSaveOrder", i, 62, ResponseFile);

				System.out.println("Scenario : Case " + i);
				System.out.println("Test Status : " + "FAIL");
				
				}
			
			} 

	}

	@SuppressWarnings({ "unchecked", "unused" })
	public String GTXAPI(String fileName) throws InterruptedException, IOException, ParseException  {

		
		File file = new File(fileName);

		FileInputStream propfile = new FileInputStream(Propfile);
		prop.load(propfile);

		int rowCountTotal = FWUtils.getRowCount(driver, "APICreatePlaceOrder");

		System.out.println("Row count from the Excel is " + rowCountTotal);

       for (int i = 1; i <= rowCountTotal; i++) {
			
			// format.formatCellValue(cell)
			 
			
			 Object	loanIdentifier = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 0);
			
			String loanNumber = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 1);
			String previousLoanNumber = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 2);

//			String clientIDString = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 3);
//			double clientIDDouble = Double.parseDouble(clientIDString);
//			int clientID = (int) clientIDDouble;

//			String titleCompanyIDString = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 4);
//			double titleCompanyIDDouble = Double.parseDouble(titleCompanyIDString);
//			int titleCompanyID = (int) titleCompanyIDDouble;

			// attributes:
			String key = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 6);
			String value = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 7);
			// subjectPropertyAddress:
			String streetAddress1 = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 9);

			String streetAddress2 = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 10);
			String city = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 11);
			String county = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 12);

			String fipScodeString = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 13);
			double fipScodeDouble = Double.parseDouble(fipScodeString);
			int fipScode = (int) fipScodeDouble;

			String state = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 14);

			String unitString = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 15);
			double unitDouble = Double.parseDouble(unitString);
			int unit = (int) unitDouble;

			String zipCode = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 16);
			// borrowerSellers:
			String actionType = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 18);
			String borrowerSellerID = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 19);
			String borrowerSellerEntityType = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 20);
			String borrowerSellerType = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 21);
			String entityTypeName = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 22);
			String entityTypeTaxID = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 23);
			String firstName = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 24);
			String middleName = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 25);
			String lastName = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 26);
			String maritalStatus = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 27);
			String spouse = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 28);
			String ssn = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 29);
			String email = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 30);
			String phone = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 31);
			String address_StreetAddress1 = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 32);
			String address_StreetAddress2 = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 33);
			String address_City = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 34);
			String address_County = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 35);
			String address_State = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 36);

			String address_UnitString = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 37);
			double address_UnitDouble = Double.parseDouble(address_UnitString);
			int address_Unit = (int) address_UnitDouble;

			String address_ZipCode = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 38);
			// lenders:
			String actionTypelenders = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 40);
			String lenderID = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 41);
			String lendingName = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 42);
			String lendingCompanyName = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 43);
			String contactFirstName = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 44);
			String contactLastName = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 45);
			String contactPhone = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 46);
			String contactEmail = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 47);
			// String contactEmail = CharMatcher.is('\"').trimFrom(result);
			String lenderCity = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 48);
			String lenderCounty = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 49);
			String lenderState = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 50);
			String lenderStreetAddress = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 51);

			String lenderUnitString = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 52);
			double lenderUnitDouble = Double.parseDouble(lenderUnitString);
			int lenderUnit = (int) lenderUnitDouble;

			String lenderZipCode = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 53);
			// lenderUsers:
			String lenderGuid = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 55);
			String userType = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 56);
			String firstNamelend = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 57);
			String lastNamelend = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 58);
			String emaillend = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 59);
			String phonelend = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 60);
			// titleOrder:
			String amortizationType = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 62);
			String propertyType = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 63);
			String lienPosition = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 64);

			String loanAmountString = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 65);
			double loanAmountDouble = Double.parseDouble(loanAmountString);
			int loanAmount = (int) loanAmountDouble;

			String loanPurpose = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 66);
			String loanType = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 67);

			String originalLoanAmountString = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 68);
			double originalLoanAmountDouble = Double.parseDouble(originalLoanAmountString);
			int originalLoanAmount = (int) originalLoanAmountDouble;

			String priorPolicyDate = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 69);
			String secondMortgageType = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 70);
			String propertyUse = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 71);

			String salePriceString = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 72);
			double salePriceDouble = Double.parseDouble(salePriceString);
			int salePrice = (int) salePriceDouble;

			String unpaidBalanceString = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 73);
			double unpaidBalanceDouble = Double.parseDouble(unpaidBalanceString);
			int unpaidBalance = (int) unpaidBalanceDouble;

			// noteCocs
			String id = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 75);
			String noteSubject = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 76);
			String noteBody = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 77);
			// documents

			String documentTypeID = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 79);
//			double documentTypeIDDouble = Double.parseDoudocumentTypeIDble(documentTypeIDString);
//			int documentTypeID = (int) documentTypeIDDouble;

			String documentDescription = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 80);
			String documentFileName = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 81);
			String documentBytes = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 82);
			String documentAZPath = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 83);

			JSONObject mainBody = new JSONObject();
			mainBody.put("loanIdentifier", loanIdentifier);
			mainBody.put("loanNumber", loanNumber);
			mainBody.put("previousLoanNumber", previousLoanNumber);
//			mainBody.put("clientID", clientID);
//			mainBody.put("titleCompanyID", titleCompanyID);

			JSONObject attributes = new JSONObject();
			attributes.put("key", key);
			attributes.put("value", value);

			JSONObject subjectPropertyAddress = new JSONObject();
			subjectPropertyAddress.put("streetAddress1", streetAddress1);
			subjectPropertyAddress.put("streetAddress2", streetAddress2);
			subjectPropertyAddress.put("city", city);
			subjectPropertyAddress.put("county", county);
			subjectPropertyAddress.put("fipScode", fipScode);
			subjectPropertyAddress.put("state", state);
			subjectPropertyAddress.put("unit", unit);
			subjectPropertyAddress.put("zipCode", zipCode);

			JSONObject borrowerSellers = new JSONObject();
			borrowerSellers.put("actionType", actionType);
			borrowerSellers.put("borrowerSellerID", borrowerSellerID);
			borrowerSellers.put("borrowerSellerEntityType", borrowerSellerEntityType);
			borrowerSellers.put("borrowerSellerType", borrowerSellerType);
			borrowerSellers.put("entityTypeName", entityTypeName);
			borrowerSellers.put("entityTypeTaxID", entityTypeTaxID);
			borrowerSellers.put("firstName", firstName);
			borrowerSellers.put("middleName", middleName);
			borrowerSellers.put("lastName", lastName);
			borrowerSellers.put("maritalStatus", maritalStatus);
			borrowerSellers.put("spouse", spouse);
			borrowerSellers.put("ssn", ssn);
			borrowerSellers.put("email", email);
			borrowerSellers.put("phone", phone);
			borrowerSellers.put("address_StreetAddress1", address_StreetAddress1);
			borrowerSellers.put("address_StreetAddress2", address_StreetAddress2);
			borrowerSellers.put("address_City", address_City);
			borrowerSellers.put("address_County", address_County);
			borrowerSellers.put("address_State", address_State);
			borrowerSellers.put("address_Unit", address_Unit);
			borrowerSellers.put("address_ZipCode", address_ZipCode);

			JSONArray borrowerSellersArray = new JSONArray();
			borrowerSellersArray.add(borrowerSellers);

			JSONObject lendersbody = new JSONObject();
			lendersbody.put("actionType", actionTypelenders);
			lendersbody.put("lenderID", lenderID);
			lendersbody.put("lendingName", lendingName);
			lendersbody.put("lendingCompanyName", lendingCompanyName);
			lendersbody.put("contactFirstName", contactFirstName);
			lendersbody.put("contactLastName", contactLastName);
			lendersbody.put("contactPhone", contactPhone);
			lendersbody.put("contactEmail", contactEmail);
			lendersbody.put("lenderCity", lenderCity);
			lendersbody.put("lenderCounty", lenderCounty);
			lendersbody.put("lenderState", lenderState);
			lendersbody.put("lenderStreetAddress", lenderStreetAddress);
			lendersbody.put("lenderUnit", lenderUnit);
			lendersbody.put("lenderZipCode", lenderZipCode);

			JSONObject lenderUsers = new JSONObject();
			lenderUsers.put("lenderGuid", lenderGuid);
			lenderUsers.put("userType", userType);
			lenderUsers.put("firstName", firstNamelend);
			lenderUsers.put("lastName", lastNamelend);
			lenderUsers.put("email", emaillend);
			lenderUsers.put("phone", phonelend);

			JSONArray lenderUsersArray = new JSONArray();
			lenderUsersArray.add(lenderUsers);

			lendersbody.put("lenderUsers", lenderUsersArray);

			JSONArray lendersArray = new JSONArray();
			lendersArray.add(lendersbody);

			JSONObject titleOrder = new JSONObject();
			titleOrder.put("amortizationType", amortizationType);
			titleOrder.put("propertyType", propertyType);
			titleOrder.put("lienPosition", lienPosition);

			titleOrder.put("loanAmount", loanAmount);
			titleOrder.put("loanPurpose", loanPurpose);
			titleOrder.put("loanType", loanType);
			titleOrder.put("originalLoanAmount", originalLoanAmount);
			titleOrder.put("priorPolicyDate", priorPolicyDate);
			titleOrder.put("secondMortgageType", secondMortgageType);
			titleOrder.put("propertyUse", propertyUse);
			titleOrder.put("salePrice", salePrice);
			titleOrder.put("unpaidBalance", unpaidBalance);

			JSONObject notesDocsbody = new JSONObject();
			notesDocsbody.put("id", id);
			notesDocsbody.put("noteSubject", noteSubject);
			notesDocsbody.put("noteBody", noteBody);

			JSONObject documents = new JSONObject();
			documents.put("documentTypeID", documentTypeID);
			documents.put("documentDescription", documentDescription);
			documents.put("documentFileName", documentFileName);
			documents.put("documentBytes", documentBytes);
			documents.put("documentAZPath", documentAZPath);

			JSONArray documentsArray = new JSONArray();
			documentsArray.add(documents);

			notesDocsbody.put("documents", documentsArray);
			titleOrder.put("notesDocs", notesDocsbody);

			mainBody.put("attributes", attributes);
			mainBody.put("subjectPropertyAddress", subjectPropertyAddress);
			mainBody.put("borrowerSellers", borrowerSellersArray);
			mainBody.put("lenders", lendersArray);
			mainBody.put("titleOrder", titleOrder);

			String jsonString = mainBody.toJSONString();

			Gson gson = new GsonBuilder().setPrettyPrinting().create();

			JsonElement jsonElement = JsonParser.parseString(jsonString);

			String prettyJson = gson.toJson(jsonElement);

			System.out.println(prettyJson);

			Response res = (Response) given()
					.header("Authorization", "Bearer " + GTXTokenGeneration())
					.header("Content-Type", "application/json").body(prettyJson).when().post("https://qagtx.greenlight-connect.com/GtxAPI/CreatePlaceOrder").then()
					.extract().response();

			String ResponseFile = res.body().asPrettyString();
			
			System.out.println("Response from the API is " + ResponseFile);
			
			long Actual;
			
			try {
				
			JSONParser parser = new JSONParser();
			 JSONObject jsonObject = (JSONObject) parser.parse(ResponseFile);
			  JSONObject status = (JSONObject) jsonObject.get("status");
			   Actual = (long) status.get("statusCode");
			}
			
			catch(ClassCastException status) {
				
				 Actual = res.getStatusCode();
			}
			  
			//Actual Status Code
			
			
//			int StatusCodeGeneral = res.getStatusCode(); //200 from server
			  
			String ActualStatusCode = String.valueOf(Actual);
			FWUtils.writeXLPages(driver, "APICreatePlaceOrder", i, 86, ActualStatusCode);
			System.out.println("Actul Status code form the server " + Actual);
			
			//Expected Status code
			String expect = FWUtils.readXLPages(driver, "APICreatePlaceOrder", i, 84);
			double expectstring = Double.parseDouble(expect);
			int ExpectedStatusCode = (int) expectstring;
            System.out.println("Expected status code in excel is " + ExpectedStatusCode);
            
			
			
			int TestStatus = 85;	
			
			if (Actual == ExpectedStatusCode) {
				
				//FWUtils.writeXLPages(driver, "APICreatePlaceOrder", i, Scenario, "Case" + i);
				FWUtils.writeXLPages(driver, "APICreatePlaceOrder", i, TestStatus, "PASS");
				FWUtils.writeXLPages(driver, "APICreatePlaceOrder", i, 87, ResponseFile);

				System.out.println("Scenario : Case " + i);
				System.out.println("Test Status : " + "PASS");
				
			}
			
            else {

				//FWUtils.writeXLPages(driver, "APICreatePlaceOrder", i, Scenario, "Case" + i);
				FWUtils.writeXLPages(driver, "APICreatePlaceOrder", i, TestStatus, "FAIL");
				FWUtils.writeXLPages(driver, "APICreatePlaceOrder", i, 87, ResponseFile);
				
				System.out.println("Scenario : Case " + i);
				System.out.println("Test Status : " + "FAIL");
				}
			
		} 	
		
		return excelPath;
		
		
	}
		
	/******************************************************AmeriSaveDocument - GTX 
	 * @throws InterruptedException 
	 * @throws IOException *************************************************************************/
	
	@SuppressWarnings("unchecked")
	public void AmeriSaveSendNotes() throws InterruptedException, IOException {
		
	FileInputStream propfile = new FileInputStream(Propfile);
	prop.load(propfile);
	
	int totaRowcount = FWUtils.getRowCount(driver, "AmeriSaveSendNotes");
    System.out.println("Total Row count from the AmeriSaveSendNotes " + totaRowcount);
	
	for(int i=1; i<totaRowcount ; i++)
	{
	String orderNumber = FWUtils.readXLPages(driver, "AmeriSaveSendNotes", i, 0);
	String loanNumber = FWUtils.readXLPages(driver, "AmeriSaveSendNotes", i, 1);
	String event = FWUtils.readXLPages(driver, "AmeriSaveSendNotes", i, 2);
	String subject = FWUtils.readXLPages(driver, "AmeriSaveSendNotes", i, 3);
	String body = FWUtils.readXLPages(driver, "AmeriSaveSendNotes", i, 4);
	String noteDate = FWUtils.readXLPages(driver, "AmeriSaveSendNotes", i, 6);
	String sentBy = FWUtils.readXLPages(driver, "AmeriSaveSendNotes", i, 7);
	
	
	
	JSONObject AmeriSaveSendNotes = new JSONObject();
	AmeriSaveSendNotes.put("orderNumber", orderNumber);
	AmeriSaveSendNotes.put("loanNumber", loanNumber);
	AmeriSaveSendNotes.put("event", event);
	AmeriSaveSendNotes.put("subject", subject);
	AmeriSaveSendNotes.put("body", body);
	
	JSONObject metadata = new JSONObject();
	metadata.put("noteDate", noteDate);
	metadata.put("sentBy", sentBy);
	
	AmeriSaveSendNotes.put("metadata", metadata);
	
	String jsonString = AmeriSaveSendNotes.toJSONString();
	JsonElement jsonele = JsonParser.parseString(jsonString);
	Gson gson = new GsonBuilder().setPrettyPrinting().create();
	
	String prettyJson = gson.toJson(jsonele);
	
	System.out.println("Request Body of the AmeriSave Document is " + prettyJson);
	
	
	Response res = (Response) given()
			.header("Authorization", "Bearer " + GTXTokenGeneration())
			.header("Content-Type", "application/json").body(prettyJson).when().post("https://qagtx.greenlight-connect.com/api/ams/AmeriSaveSendNotes").then()
			.extract().response();
	
	String OutputResponse = res.getBody().asPrettyString();
	System.out.println("Response of the AmeriSave SendNotes API " + OutputResponse);
	
	//Actual Status Code from Server
	
	int Actual = res.getStatusCode();
	String ActualStatus = String.valueOf(Actual);
	FWUtils.writeXLPages(driver, "AmeriSaveSendNotes", i, 10, ActualStatus);
	System.out.println("Actual Status Code " + Actual);
	
	//Expected Status Code From Excel
	String ExpectedStatus = FWUtils.readXLPages(driver, "AmeriSaveSendNotes", i, 8);
	double expectstring = Double.parseDouble(ExpectedStatus);
	int Expected = (int) expectstring;
	System.out.println("Expected Status code " + Expected);
	
	if(Actual == Expected) {
		
		FWUtils.writeXLPages(driver, "AmeriSaveSendNotes", i, 9, "PASS");
		FWUtils.writeXLPages(driver, "AmeriSaveSendNotes", i, 11, OutputResponse);
		System.out.println("Case" +i + ": PASS");
	}
	
	else {
		FWUtils.writeXLPages(driver, "AmeriSaveSendNotes", i, 9, "FAIL");
		FWUtils.writeXLPages(driver, "AmeriSaveSendNotes", i, 11, OutputResponse);
		System.out.println("Case" +i + ": FAIL");
	}
	}
	
	
	}
	

	

	/******************************************************AmeriSaveDocument - GTX *************************************************************************/

	
	@SuppressWarnings("unchecked")
	public void AmeriSaveDocument() throws IOException, InterruptedException {
		
		FileInputStream propfile = new FileInputStream(Propfile);
		prop.load(propfile);
		
		int totaRowcount = FWUtils.getRowCount(driver, "AmeriSaveDocuments");
	    System.out.println("Total Row count from the AmeriSaveDocuments " + totaRowcount);
		
		for(int i=1; i<totaRowcount ; i++)
		{
		String orderNumber = FWUtils.readXLPages(driver, "AmeriSaveDocuments", i, 0);
		String loanNumber = FWUtils.readXLPages(driver, "AmeriSaveDocuments", i, 1);
		String documentType = FWUtils.readXLPages(driver, "AmeriSaveDocuments", i, 2);
		String description = FWUtils.readXLPages(driver, "AmeriSaveDocuments", i, 3);
		String documentContent = FWUtils.readXLPages(driver, "AmeriSaveDocuments", i, 4);
		String fileName = FWUtils.readXLPages(driver, "AmeriSaveDocuments", i, 5);
		
		
		
		JSONObject AmeriSaveDocuments = new JSONObject();
		AmeriSaveDocuments.put("orderNumber", orderNumber);
		AmeriSaveDocuments.put("loanNumber", loanNumber);
		AmeriSaveDocuments.put("documentType", documentType);
		AmeriSaveDocuments.put("description", description);
		AmeriSaveDocuments.put("documentContent", documentContent);
		AmeriSaveDocuments.put("fileName", fileName);
		
		
		String jsonString = AmeriSaveDocuments.toJSONString();
		JsonElement jsonele = JsonParser.parseString(jsonString);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		String prettyJson = gson.toJson(jsonele);
		
		System.out.println("Request Body of the AmeriSave Document is " + prettyJson);
		
		
		Response res = (Response) given().baseUri(prop.getProperty("GTX.AmeriSaveDocuments"))
				.header("Authorization", "Bearer " + GTXTokenGeneration())
				.header("Content-Type", "application/json").body(prettyJson).when().post("/AmeriSaveDocuments").then()
				.extract().response();
		
		String OutputResponse = res.getBody().asPrettyString();
		System.out.println("Response of the AmeriSave Documets API " + OutputResponse);
		
		//Actual Status Code from Server
		
		int Actual = res.getStatusCode();
		String ActualStatus = String.valueOf(Actual);
		FWUtils.writeXLPages(driver, "AmeriSaveDocuments", i, 8, ActualStatus);
		System.out.println("Actual Status Code " + Actual);
		
		//Expected Status Code From Excel
		String ExpectedStatus = FWUtils.readXLPages(driver, "AmeriSaveDocuments", i, 6);
		double expectstring = Double.parseDouble(ExpectedStatus);
		int Expected = (int) expectstring;
		System.out.println("Expected Status code " + Expected);
		
		Date date1 = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");
		String date = formatter.format(date1);
		System.out.println("Time os the running is " + date);
		FWUtils.writeXLPages(driver, "AmeriSaveDocuments", i, 10, date);
		
		if(Actual == Expected) {
			
			FWUtils.writeXLPages(driver, "AmeriSaveDocuments", i, 7, "PASS");
			FWUtils.writeXLPages(driver, "AmeriSaveDocuments", i, 9, OutputResponse);
			System.out.println("Case" +i + ": PASS");
		}
		
		else {
			FWUtils.writeXLPages(driver, "AmeriSaveDocuments", i, 7, "FAIL");
			FWUtils.writeXLPages(driver, "AmeriSaveDocuments", i, 9, OutputResponse);
			System.out.println("Case" +i + ": FAIL");
		}
		
		
		
		}
		
	
	}
	
	
	/*******************************************************************************************************************************/
	
	public void GTXOrder(String filePath) throws IOException {

		// File file = new File(fileName);

		FileInputStream propfile = new FileInputStream(Propfile);
		prop.load(propfile);

		GTXOrder_Data payload = new GTXOrder_Data(filePath);

		String resbody = payload.exceldata();

		// System.out.println("value of the body is " +resbody);

		given().baseUri(prop.getProperty("GTX.api.GTXOrder")).header("Authorization", "Bearer " + GTXTokenGeneration())
				.header("Content-Type", "application/json").body(resbody).when().post("/PlaceOrderToReware").then()
				.statusCode(200).log().all();

	}
	
	/*******************************************************************************************************************************/

	// Method to create an extended quote using an API endpoint.
	public void CreateQuoteExtended(String fileName) throws InterruptedException, IOException {
		// Create a File object from the provided file name.
		File file = new File(fileName);

		// Load properties from the configuration file.
		FileInputStream propfile = new FileInputStream(Propfile);
		prop.load(propfile);

		// Send a POST request to the /CreateQuoteExtended endpoint to create an
		// extended quote.
		given().baseUri(prop.getProperty("api.baseUri"))// Set the base URI for the API.

				// Include authorization token.
				.header("Authorization", "Bearer  " + GenerateTokenAuthorization())
				// Set content type to JSON.
				.header("Content-Type", "application/json").body(file).// Include the contents of the file in the
																		// request body.
				// Perform the POST request.
				when().post("/CreateQuoteExtended").
				// Log all response details
				then().log().all().statusCode(200);// Ensure that the response status code is 200 (OK).

	}

	/******************************************************************************************************************************************/
	// Method to create an extended quote using a specific API endpoint.
	public void CreateQuoteExtendedVal(String fileName) throws InterruptedException, IOException {
		// Create a File object from the provided file name.
		File file = new File(fileName);
		FileInputStream propfile = new FileInputStream(Propfile);
		prop.load(propfile);
		System.out.println("endpoint" + prop.getProperty("api.baseUri"));
		// Send a POST request to the /CreateQuoteExtendedVal endpoint to create an
		// extended quote.
//		given().baseUri(prop.getProperty("api.baseUri"))// Set the base URI for the API.
		given().baseUri("https://qaapi.greenlight-connect.com/Direct")
				// Include authorization token.
				.header("Authorization", "Bearer  " + GenerateTokenAuthorization())
				// Set content type to JSON.
				.header("Content-Type", "application/json").body(file).// Include the contents of the file in the
																		// request body.
				// Perform the POST request.
				when().post("/CreateQuoteExtendedVal").
				// Log all response details.
				then().log().all().statusCode(200);// Ensure that the response status code is 200 (OK).
	}

	/***************************************************************************************************************************************************/
	// Method to create a quote using the CreateQuoteFIPS API endpoint.
	public void CreateQuoteFIPS(String fileName) throws InterruptedException, IOException {
		// Create a File object from the provided file name.
		
		String file1 ="C://Users//Gowthamkumar.R//git//repositoryFinal//Cucumberwithapi//Data//CreateQuoteFIPS.json";
		File file = new File(file1);

		FileInputStream propfile = new FileInputStream(Propfile);
		prop.load(propfile);

		// Send a POST request to the /CreateQuoteFIPS endpoint to create a quote using
		// FIPS data.
		given().baseUri(prop.getProperty("api.baseUri"))// Set the base URI for the API.
				// Include authorization token.
				.header("Authorization", "Bearer  " + GenerateTokenAuthorization())
				// Set content type to JSON.
				.header("Content-Type", "application/json").body(file).// Include the contents of the file in the
																		// request body.
				// Perform the POST request.
				when().post("/CreateQuoteFIPS").
				// Log all response details.
				then().log().all().statusCode(200);// Ensure that the response status code is 200 (OK).
	}

	/******************************************************************************************************************************************************/
	// Method to create an order using the CreateOrder API endpoint.
	public void CreateOrder(String fileName) throws InterruptedException, IOException {
		// Create a File object from the provided file name.
		File file = new File(fileName);

		// Send a POST request to the /CreateOrder endpoint to create an order.
		given().baseUri(prop.getProperty("api.baseUri"))// Set the base URI for the API.

				// Include authorization token.
				.header("Authorization", "Bearer  " + GenerateTokenAuthorization())
				// Set content type to JSON.
				.header("Content-Type", "application/json").body(file).// Include the contents of the file in the
																		// request body.
				// Perform the POST request to create the order.
				when().post("/CreateOrder").
				// Log all response details.
				then().log().all().statusCode(200);// Ensure that the response status code is 200 (OK).
	}

	/*******************************************************************************************************************************************************/
	// Suppress unchecked warnings for this method.
	@SuppressWarnings("unchecked")

	// Method to write a JSON object to a file and beautify the content.
	public static void writeJsonSimpleDemo(String filename) throws Exception {

		// Create a JSON object to hold the data.
		JSONObject sampleObject = new JSONObject();

		// Load properties from the configuration file.
		FileInputStream propfile = new FileInputStream(Propfile);
		prop.load(propfile);

		// Populate the JSON object with user credentials and titleCompanyID.
		sampleObject.put("userName", prop.getProperty("api.username"));
		sampleObject.put("password", prop.getProperty("api.password"));
		sampleObject.put("titleCompanyID", prop.getProperty("api.titleCompanyID"));
//		FWUtils.readXLPages(driver, "TestData", 0, 1);

		// Write the JSON object to the specified file.
		Files.write(Paths.get(filename), sampleObject.toJSONString().getBytes());
//	    BeautifyJSON();

		// Beautify the JSON content and write it to another file.
		WriteJSONStringToFile(rootPath + "/Data/CreateRequestBody.json",
				BeautifyJSON(rootPath + "/Data/CreateRequestBody.json"));

	}

	/********************************************************************************************************************************************************/
	/*
	 * Creating Request Body using random parameters
	 */
	// Method to create a request body JSON file.
	public void CreateRequestBody() throws Exception {
		// Call the method to write a JSON object to a file and beautify the content.
		writeJsonSimpleDemo(rootPath + "/Data/CreateRequestBody.json");
	}

	/*
	 * Beautify JSON used to Generate Pretty JSON from Existing JSON file
	 */
	// Method to beautify JSON content and return the prettified JSON as a string.
	public static String BeautifyJSON(String FileNamepath) throws FileNotFoundException, IOException {
		// Initialize a variable to hold the parsed JSON object.
		Object simpleObj = null;
		// Create a JSONParser instance.
		JSONParser parser = new JSONParser();
		try {
			// Parse the JSON content from the specified file.
			simpleObj = parser.parse(new FileReader(FileNamepath));
		} catch (ParseException e) {
			// Print the parse exception stack trace.
			e.printStackTrace();
		}
		// Check if the JSON object was successfully parsed.
		if (simpleObj != null) {
			// Print the original JSON content.
			System.out.println("Simple JSON Result:\n" + simpleObj.toString());
		}
		// Initialize a variable to hold the prettified JSON.
		String prettyJson = null;
		// Check if the JSON object was successfully parsed.
		if (simpleObj != null) {
			// Use the crunchifyPrettyJSONUtility to prettify the JSON content.
			prettyJson = crunchifyPrettyJSONUtility(simpleObj.toString());
		}
		// Print the prettified JSON content.
		System.out.println("\n======== Pretty JSON Result: ==========\n" + prettyJson);
//	        WriteJSONStringToFile("/src/main/resources/resources/RequestBodyJSONFormat.json",prettyJson);

		// Return the prettified JSON string.
		return prettyJson;

	}

	/*
	 * Write Back the formatted JSON in pretty format into specific file.
	 */
	// Method to write a prettified JSON string to a file.
	public static void WriteJSONStringToFile(String Filename, String PrettyJsonContent) {
		// Write JSON string to a file
		try {
			// Create a FileWriter instance for the specified filename.
			FileWriter file = new FileWriter(Filename);
			// Write the prettified JSON content to the file.
			file.write(PrettyJsonContent); // json is a JSON string , the content which we are going to write here
			file.flush();// Flush the content to ensure it's written to the file.
		} catch (IOException e) {
			// Print the exception stack trace if an error occurs while writing the file.
			e.printStackTrace();
		}
	}

	/*
	 * Prettify JSON Utility
	 */

	// Utility method to convert a compact JSON string to a prettified JSON string.
	public static String crunchifyPrettyJSONUtility(String simpleJSON) {
		// parseString: Parses the specified JSON string into a parse tree

		// Parse the provided compact JSON string into a JsonObject.
		JsonObject crunchifyJSON = JsonParser.parseString(simpleJSON).getAsJsonObject();

		// JsonParser crunhifyParser = new JsonParser();
		// JsonObject json = crunhifyParser.parse(simpleJSON).getAsJsonObject();
		// Gson: This is the main class for using Gson. Gson is typically used by first
		// constructing
		// a Gson instance and then invoking toJson(Object) or fromJson(String, Class)
		// methods on it.
		// Gson instances are Thread-safe so you can reuse them freely across multiple
		// threads.

		// Create a Gson instance with pretty printing enabled.
		Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
		// setPrettyPrinting(): Configures Gson to output Json that fits in a page for
		// pretty printing.
		// This option only affects Json serialization.

		// Convert the JsonObject to a prettified JSON string using the created Gson
		// instance.
		String prettyJson = prettyGson.toJson(crunchifyJSON);
		// toJson: Converts a tree of JsonElements into its equivalent JSON
		// representation.

		// Return the prettified JSON string.
		return prettyJson;
	}

}
