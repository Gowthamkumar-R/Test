package com.techarchive.Cucumberwithapi;

import org.junit.runner.RunWith;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

/**
 * This class serves as the Cucumber test runner for executing feature files and
 * step definitions.
 */
@RunWith(Cucumber.class)
@CucumberOptions(
		//features = "src/test/resources/Test/SettlementStatementFIPS.feature",
		
        // features = "src/test/resources/GTX/GTXOrder.feature",
		
		//features = "src/test/resources/GTX/GTXApi.feature",
        
        //features = "src\\test\\resources\\GTX\\AmeriSavePlaceOrder.feature",
		
	    features = "src/test/resources/GTX/AmeriSaveDocument.feature",
		
	   // features = "src/test/resources/GTX/AmeriSaveSendNotes.feature",
	    		
        
		// Specifies the package containing step definitions
		glue = { "stepdefinition", "com.techarchive.Cucumberwithapi" },
		
		// Specifies the plugins for generating different types of reports
		plugin = { "pretty", "html:target/cucumber.html", "json:target/cucumber.json" },
		
		// Publishes the generated reports
		publish = true,
		
		// Enables or disables colorful console output (true: colorful, false: black and
		// white)
		monochrome = true,
		
		// Whether to run the scenarios to check if steps have matching step definitions
		dryRun = false)

//@CucumberOptions(
//		features = "src/test/resources/Test",
//		glue = {"stepdefinition"},
//		plugin ={"pretty", 
//				"html:target/cucumber.html",
//				"json:target/cucumber.json"
//				},
//		/* can generate the repors as  Cucumber.html(html:target/cucumber-reports), cucumber.json(json:target/cucumber-reports/Cucumber.json), Cucumber.xml(junit:target/cucumber-reports/Cucumber.xml)*/
//		monochrome = false)  /* the monochrome option is set to false, then the console output is not as readable. if the monochrome is not defined in Cucumber Options, it takes it as false by default*/

public class CukeRunner {

}
