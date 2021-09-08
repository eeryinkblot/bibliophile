import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.DriverManagerType;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    public static final String LOGIN_BUTTON = "dnn$ctr362$Login$Login_COP$cmdLogin";
    public static final String PASSWORD_TEXTFIELD = "dnn$ctr362$Login$Login_COP$txtPassword";
    public static final String USERNAME_TEXTFIELD = "dnn$ctr362$Login$Login_COP$txtUsername";
    public static final String CHECKBOX_TABLE = "oclc-patronaccount-checkboxtable";

    public static void main(String[] args) {
        WebDriverManager.getInstance(DriverManagerType.CHROME).setup();
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.setHeadless(true);
        WebDriver driver = new ChromeDriver(chromeOptions);

        driver.get("https://stadtbibliothek.magdeburg.de/Mein-Konto");
        fillUsername(driver, "%USERNAME%");
        fillPassword(driver, "%PASSWORD%");
        login(driver);

        final List<TableRow> heuteAbgeben = getHeuteAbgeben(driver);

        Stream<String> nicht_verlängerbar = heuteAbgeben.stream()
                .filter(abgeben -> abgeben.getVerlängerbar().contains("Nicht verlängerbar"))
                .map(abgeben -> "Nicht verlängerbar: " + abgeben.getName());

        Stream<String> verlängerbar = heuteAbgeben.stream()
                .filter(abgeben -> abgeben.getVerlängerbar().contains("Verlängerbar"))
                .map(abgeben -> "Verlängerbar: " + abgeben.getName());

        nicht_verlängerbar.forEach(System.out::println);
        verlängerbar.forEach(System.out::println);
    }

    @NotNull
    private static List<TableRow> getHeuteAbgeben(WebDriver driver) {
        final int TITEL = 2;
        final int MEDIUM = 4;
        final int ABGABEDATUM = 6;
        final int VERLÄNGERBAR = 7;
        // Warten bis Login durch ist
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        List<WebElement> rows = getRows(driver);
        List<TableRow> tableRows = rows.stream()
                .map(row -> row.findElements(By.tagName("td")))
                .map(row -> new TableRow(
                        row.get(TITEL).getText(),
                        row.get(MEDIUM).getText(),
                        row.get(ABGABEDATUM).getText().replace("Aktuelle Frist: ", ""),
                        row.get(VERLÄNGERBAR).getText()))
                .collect(Collectors.toList());

        driver.close();

        return tableRows.stream().filter(row -> {
            String[] dateParts = row.getAbgabedatum().split("\\.");
            LocalDate date = LocalDate.of(Integer.valueOf(dateParts[2]), Integer.valueOf(dateParts[1]), Integer.valueOf(dateParts[0]));
            return date.isEqual(LocalDate.now());
        }).collect(Collectors.toList());
    }

    private static void fillUsername(WebDriver driver, String username) {
        getElementByName(driver, USERNAME_TEXTFIELD).sendKeys(username);
    }

    private static void fillPassword(WebDriver driver, String password) {
        getElementByName(driver, PASSWORD_TEXTFIELD).sendKeys(password);
    }

    private static void login(WebDriver driver) {
        getElementByName(driver, LOGIN_BUTTON).click();
    }

    private static WebElement getElementByName(WebDriver driver, String txtUsername) {
        return driver.findElement(By.name(txtUsername));
    }

    @NotNull
    private static List<WebElement> getRows(WebDriver driver) {
        WebElement table = driver.findElement(By.className(CHECKBOX_TABLE));
        List<WebElement> rows = table.findElements(By.tagName("tr"));
        rows.remove(0); // Entferne "Alle ausgewählt"
        return rows;
    }

}
