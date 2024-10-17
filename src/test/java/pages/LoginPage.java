package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class LoginPage {

    private WebDriver driver;

    // Locators for the login page
    private By usernameField = By.id("username"); // Adjust as necessary
    private By passwordField = By.id("password"); // Adjust as necessary
    private By loginButton = By.id("loginButton"); // Adjust as necessary

    public LoginPage(WebDriver driver) {
        this.driver = driver;
    }

    public void enterUsername(String username) {
        WebElement element = driver.findElement(usernameField);
        element.clear();
        element.sendKeys(username);
    }

    public void enterPassword(String password) {
        WebElement element = driver.findElement(passwordField);
        element.clear();
        element.sendKeys(password);
    }

    public void clickLoginButton() {
        WebElement element = driver.findElement(loginButton);
        element.click();
    }
}
