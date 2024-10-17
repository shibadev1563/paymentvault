package stepDefinitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import pages.LoginPage;
import utils.DriverFactory;
import utils.ConfigReader;

public class LoginSteps {

    WebDriver driver = DriverFactory.getDriver();
    LoginPage loginPage = new LoginPage(driver);

    private String validUsername = ConfigReader.getProperty("valid.username");
    private String validPassword = ConfigReader.getProperty("valid.password");
    private String invalidUsername = ConfigReader.getProperty("invalid.username");
    private String invalidPassword = ConfigReader.getProperty("invalid.password");

    @Given("I am on the login page")
    public void i_am_on_the_login_page() {
        String baseUrl = ConfigReader.getProperty("base.url") + "/login";
        driver.get(baseUrl);
    }

    @And("I enter valid credentials")
    public void i_enter_valid_credentials() {
        loginPage.enterUsername(validUsername);
        loginPage.enterPassword(validPassword);
    }

    @And("I enter invalid credentials")
    public void i_enter_invalid_credentials() {
        loginPage.enterUsername(invalidUsername);
        loginPage.enterPassword(invalidPassword);
    }

    @And("I click on the login button")
    public void i_click_on_the_login_button() {
        loginPage.clickLoginButton();
    }

    @Then("I should be redirected to the dashboard")
    public void i_should_be_redirected_to_the_dashboard() {
        String expectedUrl = ConfigReader.getProperty("base.url") + "/dashboard";
        String actualUrl = driver.getCurrentUrl();
        if (!actualUrl.equals(expectedUrl)) {
            // m. ReportManager.addScreenshot(((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES));
            throw new AssertionError("Login failed or incorrect redirect. Expected URL: " + expectedUrl + ", but found: " + actualUrl);
        }
    }

    @Then("I should see an error message")
    public void i_should_see_an_error_message() {
        // Implement this step based on how you handle error messages in your application
        // For example, you could check for an error message element on the page
    }
}
