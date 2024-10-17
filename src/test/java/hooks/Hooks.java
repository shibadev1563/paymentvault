package hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import utils.DriverFactory;
import utils.ConfigReader;
import utils.EmailLib2;
import utils.ScreenshotUtil;

import java.io.IOException;

public class Hooks {

    @Before
    public void setUp(Scenario scenario) {
        DriverFactory.initializeDriver();
        String baseUrl = ConfigReader.getProperty("base.url");
        DriverFactory.getDriver().get(baseUrl);
    }

    @AfterStep
    public void afterStep(Scenario scenario) throws IOException {
        // Capture screenshot if the scenario failed
        if (scenario.isFailed()) {
            byte[] screenshotBytes = ((TakesScreenshot) DriverFactory.getDriver()).getScreenshotAs(OutputType.BYTES);
            scenario.attach(screenshotBytes, "image/png", "screenshot");

            // Save screenshot to file
            String fileName = "screenshot_" + System.currentTimeMillis() + ".png";
            String path = ScreenshotUtil.captureScreenshot(DriverFactory.getDriver(), fileName);

            scenario.attach("<a href='"+path+"' target='_blank'>"+fileName+"</a>","image/url","screenshot");
//            scenario.attach(screenshotBytes, "image/png", "screenshot");
            EmailLib2.sendEmail();
        }
    }

    @After
    public void tearDown(Scenario scenario) {
        DriverFactory.quitDriver();
    }
}
