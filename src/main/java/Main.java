import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.DriverManagerType;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {
        WebDriverManager.getInstance(DriverManagerType.CHROME).setup();
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.setHeadless(true);
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
        rows.stream()
                .map(row -> row.findElements(By.tagName("td")))
                //.map(td -> td.get(TITEL))
                .flatMap(Collection::stream)
                .map(WebElement::getText)
                .forEach(System.out::println)
        ;

    }

}
