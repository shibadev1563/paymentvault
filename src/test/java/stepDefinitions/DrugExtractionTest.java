package stepDefinitions;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;
import org.testng.annotations.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.*;

public class DrugExtractionTest {

    private WebDriver driver;
    private WebDriverWait wait;

    // Directory to store individual Excel files and the master Excel file
    private final String DIRECTORY = "DrugData/";

    // Map to hold drug names per letter for the master Excel file
    private Map<String, List<String>> letterDrugListMap = new LinkedHashMap<>();

    @BeforeClass
    public void setUp() {
        // Setup WebDriver using WebDriverManager
        WebDriverManager.chromedriver().setup();

        // Optional: Run Chrome in headless mode for faster execution and to run on machines without a GUI
        // ChromeOptions options = new ChromeOptions();
        // options.addArguments("--headless");
        // driver = new ChromeDriver(options);

        driver = new ChromeDriver();

        // Maximize browser and set implicit wait
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        // Initialize WebDriverWait for explicit waits
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        // Create the directory if it doesn't exist
        createDirectory(DIRECTORY);
    }

    /**
     * Creates a directory if it does not already exist.
     *
     * @param path The path of the directory to create.
     */
    private void createDirectory(String path) {
        java.io.File dir = new java.io.File(path);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                System.out.println("Directory created: " + path);
            } else {
                System.out.println("Failed to create directory: " + path);
            }
        }
    }

    @DataProvider(name = "alphabetProvider", parallel = false)
    public Object[][] alphabetProvider() {
        // Providing letters A-Z
        String[] letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".split("");
        Object[][] data = new Object[letters.length][1];
        for (int i = 0; i < letters.length; i++) {
            data[i][0] = letters[i];
        }
        return data;
    }

    @Test(dataProvider = "alphabetProvider")
    public void extractDrugData(String letter) {
        // Initialize data structure for current letter
        Map<String, Map<String, String>> drugsData = new LinkedHashMap<>();

        // Navigate to the target URL
        driver.get("https://www.medicare.gov/plan-compare/#/manage-prescriptions?fips=09003&plan_type=&year=2025&lang=en");

        try {
            // Click on "Browse A-Z" link
            WebElement browseAZLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("div.AtoZDrugList button")));
            browseAZLink.click();

            Thread.sleep(500);
            // Wait for the A-Z pop-up to appear
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[normalize-space(.)='Browse drugs A-Z']")));

            // Locate the letter filter or select dropdown
            WebElement letterFilter = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("select[name='A to Z Drug List Select']")));

            Select select = new Select(letterFilter);
            select.selectByVisibleText(letter);

            Thread.sleep(500);
            // Wait for the list to update based on the letter
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("ul.AtoZDrugList__results li")));

            // Get the list of drugs for the current letter and capture their text
            List<WebElement> drugElements = driver.findElements(By.cssSelector("ul.AtoZDrugList__results li"));
            List<String> drugNames = new ArrayList<>();
            for (WebElement drugElement : drugElements) {
                drugNames.add(drugElement.getText().trim());
            }

            // Print the list of drugs for the current letter
            System.out.println("Drugs for letter " + letter + ":");
            for (String drugName : drugNames) {
                System.out.println(drugName);
            }

            // Store the drug names for the master Excel file
            letterDrugListMap.put(letter, new ArrayList<>(drugNames));

            // If no drugs are found, exit early
            if (drugNames.isEmpty()) {
                System.out.println("No drugs found for letter: " + letter);
                return;
            }

            // Iterate through the drug names and process each drug
//            for (String drugName : drugNames) {
//                try {
//                    // Find the drug element by text and click to open its details
//                    WebElement drug = wait.until(ExpectedConditions.elementToBeClickable(
//                            By.xpath("//ul[@class='AtoZDrugList__results']//li//label[normalize-space(text())=" + escapeXPath(drugName) + "]")));
//                    drug.click();
//
//                    // Wait for the 'Add Drug' button to be visible
//                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[contains(text(),'Add Drug')]")));
//
//                    // Click on 'Add Drug' button
//                    WebElement addDrugButton = driver.findElement(By.cssSelector("button#aToZAddDrug"));
//                    addDrugButton.click();
//
//                    // Wait for the 'Add to My Drug List' button to be clickable
//                    WebElement addToMyDrugList = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[normalize-space(.)='Add to My Drug List']")));
//                    addToMyDrugList.click();
//
//                    // Wait for the prescription cards to load
//                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("ol[class*='ViewPrescriptions__listCards'] li")));
//
//                    // Locate all prescription cards
//                    List<WebElement> prescriptionCards = driver.findElements(By.cssSelector("ol[class*='ViewPrescriptions__listCards'] li"));
//
//                    // Map to hold key-value pairs for the current drug
//                    Map<String, String> currentDrugInfo = new LinkedHashMap<>();
//
//                    for (WebElement card : prescriptionCards) {
//                        try {
//                            // Extract all h3 and p elements within the card
//                            List<WebElement> keys = card.findElements(By.tagName("h3"));
//                            List<WebElement> values = card.findElements(By.tagName("p"));
//
//                            for (int i = 0; i < keys.size(); i++) {
//                                String key = keys.get(i).getText().trim();
//                                String value = (i < values.size()) ? values.get(i).getText().trim() : "N/A";
//
//                                currentDrugInfo.put(key, value);
//                            }
//                        } catch (NoSuchElementException e) {
//                            System.out.println("Error extracting key-value pairs from a card: " + e.getMessage());
//                            continue;
//                        }
//                    }
//
//                    // Add the current drug's information to the main map
//                    drugsData.put(drugName, currentDrugInfo);
//
//                    // Close the drug details pop-up
//                    WebElement closeButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//button[contains(text(),'Add Another Drug')])[1]")));
//                    closeButton.click();
//
//                    // Wait briefly before next iteration
//                    Thread.sleep(500);
//
//                    // Reopen the Browse A-Z pop-up for the next drug
//                    browseAZLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("div.AtoZDrugList button")));
//                    browseAZLink.click();
//
//                    Thread.sleep(500);
//                    // Wait for the A-Z pop-up to appear
//                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[normalize-space(.)='Browse drugs A-Z']")));
//
//                    // Locate the letter filter or select dropdown
//                     letterFilter = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("select[name='A to Z Drug List Select']")));
//
//                     select = new Select(letterFilter);
//                    select.selectByVisibleText(letter);
//                    Thread.sleep(500);
//                } catch (NoSuchElementException | TimeoutException e) {
//                    System.out.println("Error processing drug '" + drugName + "' under letter " + letter + ": " + e.getMessage());
//                    // Attempt to close any open pop-up to continue
//                    try {
//                        WebElement closeButton = driver.findElement(By.xpath("//button[contains(text(),'Close')]"));
//                        closeButton.click();
//                    } catch (Exception ex) {
//                        // Ignore if close button is not found
//                    }
//                    continue;
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                    System.out.println("Thread interrupted: " + e.getMessage());
//                    return;
//                }
//            }
//
//            // After collecting data for the current letter, write to Excel
//            writeDataToExcel(letter, drugsData);

        } catch (NoSuchElementException | TimeoutException e) {
            System.out.println("Error processing letter " + letter + ": " + e.getMessage());
        } catch (InterruptedException e) {
           e.printStackTrace();
        }
    }

    /**
     * Writes the collected drug data to an Excel file named based on the letter.
     *
     * @param letter    The current letter being processed.
     * @param drugsData A map containing drug names and their corresponding key-value pairs.
     */
    private void writeDataToExcel(String letter, Map<String, Map<String, String>> drugsData) {
        if (drugsData.isEmpty()) {
            System.out.println("No data to write for letter: " + letter);
            return;
        }

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Drug Information - " + letter);

        // Collect all unique keys to form Excel headers
        Set<String> allKeys = new LinkedHashSet<>();
        for (Map<String, String> drugInfo : drugsData.values()) {
            allKeys.addAll(drugInfo.keySet());
        }

        // Sort the keys alphabetically (optional)
        List<String> sortedKeys = new ArrayList<>(allKeys);
        Collections.sort(sortedKeys);

        // Create header row
        Row headerRow = sheet.createRow(0);
        int headerCellNum = 0;

        // First column is 'Drug Name'
        Cell headerCell0 = headerRow.createCell(headerCellNum++);
        headerCell0.setCellValue("Drug Name");

        // Add other headers based on unique keys
        for (String key : sortedKeys) {
            Cell headerCell = headerRow.createCell(headerCellNum++);
            headerCell.setCellValue(key);
        }

        // Populate rows with drug data
        int rowNum = 1;
        for (Map.Entry<String, Map<String, String>> drugEntry : drugsData.entrySet()) {
            Row row = sheet.createRow(rowNum++);
            int cellNum = 0;

            // Drug Name
            Cell drugNameCell = row.createCell(cellNum++);
            drugNameCell.setCellValue(drugEntry.getKey());

            // Other key-value pairs
            Map<String, String> drugInfo = drugEntry.getValue();
            for (String key : sortedKeys) {
                Cell dataCell = row.createCell(cellNum++);
                String value = drugInfo.getOrDefault(key, "N/A");
                dataCell.setCellValue(value);
            }
        }

        // Autosize columns for better readability
        for (int i = 0; i < sortedKeys.size() + 1; i++) {
            sheet.autoSizeColumn(i);
        }

        // Define the file name based on the letter
        String fileName = DIRECTORY + "DrugInformation_" + letter + ".xlsx";

        // Save the Excel file
        try (FileOutputStream fileOut = new FileOutputStream(fileName)) {
            workbook.write(fileOut);
            System.out.println("Excel file '" + fileName + "' written successfully.");
        } catch (IOException e) {
            System.out.println("Error writing Excel file for letter " + letter + ": " + e.getMessage());
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                System.out.println("Error closing workbook for letter " + letter + ": " + e.getMessage());
            }
        }
    }

    /**
     * Escapes special characters in XPath expressions.
     *
     * @param text The text to escape.
     * @return The escaped text.
     */
    private String escapeXPath(String text) {
        if (text.contains("'") && text.contains("\"")) {
            String[] parts = text.split("'");
            StringBuilder xpathBuilder = new StringBuilder("concat(");
            for (int i = 0; i < parts.length; i++) {
                xpathBuilder.append("'").append(parts[i]).append("'");
                if (i != parts.length - 1) {
                    xpathBuilder.append(", \"'\", ");
                }
            }
            xpathBuilder.append(")");
            return xpathBuilder.toString();
        } else if (text.contains("'")) {
            return "\"" + text + "\"";
        } else {
            return "'" + text + "'";
        }
    }

    /**
     * Writes a master Excel file containing all letters with their respective drug lists.
     */
    private void writeMasterExcel() {
        if (letterDrugListMap.isEmpty()) {
            System.out.println("No data to write to the master Excel file.");
            return;
        }

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet;

        for (Map.Entry<String, List<String>> entry : letterDrugListMap.entrySet()) {
            String letter = entry.getKey();
            List<String> drugNames = entry.getValue();

            sheet = workbook.createSheet("Letter " + letter);

            // Create header row
            Row headerRow = sheet.createRow(0);
            Cell headerCell = headerRow.createCell(0);
            headerCell.setCellValue("Drug Name");

            // Populate drug names
            int rowNum = 1;
            for (String drugName : drugNames) {
                Row row = sheet.createRow(rowNum++);
                Cell cell = row.createCell(0);
                cell.setCellValue(drugName);
            }

            // Add total count at the end
            Row totalRow = sheet.createRow(rowNum + 1);
            Cell totalLabelCell = totalRow.createCell(0);
            totalLabelCell.setCellValue("Total Drugs:");

            Cell totalCountCell = totalRow.createCell(1);
            totalCountCell.setCellValue(drugNames.size());

            // Autosize columns
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
        }

        // Define the master Excel file name
        String masterFileName = DIRECTORY + "AllDrugInformation.xlsx";

        // Save the master Excel file
        try (FileOutputStream fileOut = new FileOutputStream(masterFileName)) {
            workbook.write(fileOut);
            System.out.println("Master Excel file '" + masterFileName + "' written successfully.");
        } catch (IOException e) {
            System.out.println("Error writing master Excel file: " + e.getMessage());
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                System.out.println("Error closing master workbook: " + e.getMessage());
            }
        }
    }

    @AfterClass
    public void tearDown() {
        // Create master Excel file after all letters have been processed
        writeMasterExcel();

        // Quit the browser after all tests are done
        if (driver != null) {
            driver.quit();
        }
    }
}
