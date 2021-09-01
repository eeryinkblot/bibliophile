import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.DriverManagerType;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) {
        WebDriverManager.getInstance(DriverManagerType.CHROME).setup();
        ChromeOptions chromeOptions = new ChromeOptions();
        // chromeOptions.setHeadless(true);
        WebDriver driver = new ChromeDriver(chromeOptions);

        driver.get("https://stadtbibliothek.magdeburg.de/Mein-Konto");
        WebElement user = driver.findElement(By.name("dnn$ctr362$Login$Login_COP$txtUsername"));
        user.sendKeys("%USER%");

        WebElement pass = driver.findElement(By.name("dnn$ctr362$Login$Login_COP$txtPassword"));
        pass.sendKeys("%PASSWORD%");

        WebElement login = driver.findElement(By.name("dnn$ctr362$Login$Login_COP$cmdLogin"));
        login.click();

        // Warten bis Login durch ist
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);

        // Hole die Tabelle
        WebElement table = driver.findElement(By.className("oclc-patronaccount-checkboxtable"));
        List<WebElement> rows = table.findElements(By.tagName("tr"));
        rows.remove(0); // Entferne "Alle ausgewählt"

        final int TITEL = 2;
        final int MEDIUM = 4;
        final int ABGABEDATUM = 6;
        List<TableRow> tableRows = rows.stream()
                .map(row -> row.findElements(By.tagName("td")))
                .map(row -> new TableRow(
                        row.get(TITEL).getText(),
                        row.get(MEDIUM).getText(),
                        row.get(ABGABEDATUM).getText()))
                .collect(Collectors.toList());

        driver.close();

        List<TableRow> heuteAbgeben = tableRows.stream().filter(row -> {
            String[] dateParts = row.getAbgabedatum().split("\\.");

            LocalDate date = LocalDate.of(Integer.valueOf(dateParts[2]), Integer.valueOf(dateParts[1]), Integer.valueOf(dateParts[0]));
            return date.isEqual(LocalDate.now());
        }).collect(Collectors.toList());

        heuteAbgeben.forEach(System.out::println);

    }

}
