package br.com.connectbeneficios.test;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
public class EnviarRemessa {
	
	public static void main(String[] args) throws InterruptedException {

		try{
		WebDriver driver;
		String baseUrl;
		StringBuffer verificationErrors = new StringBuffer();
		System.out.println("==== Iniciando driver ====");
		
		System.setProperty("webdriver.chrome.driver", "driverExplorer/chromedriver.exe");
		DesiredCapabilities capabilities = DesiredCapabilities.chrome();
		capabilities.setCapability("requireWindowFocus", true);
		driver = new ChromeDriver(capabilities);

		baseUrl = "https://www.itau.com.br/";
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);

		System.out.println("==== Acessando Itaú ====");
		driver.get(baseUrl);
		driver.findElement(By.id("linkfirst")).click();
		driver.findElement(By.id("codop")).click();
		driver.findElement(By.id("campo_cod_operador")).clear();
		driver.findElement(By.id("campo_cod_operador")).sendKeys("847548088");
		driver.findElement(By.linkText("Acessar")).click();
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
		
		//TODO Alguém esta acessando a conta
		
		System.out.println("==== Inserindo senha ====");
		int[] numeroPasse = { 2, 0, 5, 5, 2, 0 };
		WebElement div = driver.findElement(By.xpath("//div[@class='teclas clearfix']"));
		List<WebElement> Todos = div.findElements(By.xpath("//a[@class='tecla left']"));
		Actions actions = new Actions(driver);

		Thread.sleep(5000);

		for (int i : numeroPasse) {
			for (WebElement row : Todos) {
				if (row.getText().contains(String.valueOf(i))) {
					driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
					WebElement botao = driver.findElement(By.linkText(row.getText()));
					actions.moveToElement(botao).click().perform();
				}
			}
		}
		
		WebElement senha = driver.findElement(By.id("senha"));
		if(senha.getClass().toString() == "invalido"){
			driver.findElement(By.id("btnApagar")).click();
			for (int i : numeroPasse) {
				for (WebElement row : Todos) {
					if (row.getText().contains(String.valueOf(i))) {
						driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
						WebElement botao = driver.findElement(By.linkText(row.getText()));
						actions.moveToElement(botao).click().perform();
					}
				}
			}
		}

		driver.findElement(By.linkText("acessar")).click();

		Actions builder = new Actions(driver);
		WebDriverWait wait = new WebDriverWait(driver, 1000);

		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("rdBasico")));

		Thread.sleep(5000);

		driver.findElement(By.id("rdBasico")).click();
		driver.findElement(By.id("btn-continuar")).click();

		driver.findElement(By.id("fecharTourElements")).click();
		driver.findElement(By.cssSelector("button.mfp-close")).click();
		System.out.println("==== Acessando Transmissão de arquivos ====");
		WebElement element = driver.findElement(By.linkText("menu"));
		builder.moveToElement(element).build().perform();

		driver.findElement(By.linkText("Transmissão de arquivos")).click();
		Thread.sleep(2000);

		driver.findElement(By.linkText("Enviar")).click();

		Thread.sleep(1000);
		driver.switchTo().frame("output_frame");

		File file = new File("C:/Users/suport/Desktop/Remessas/Remessa itau/Remessa/");
		File[] files = file.listFiles();
		File lastModified = files[0];
		System.out.println("==== Identificando ultima remessa ====");
		for (int i = 0; i < files.length; i++) {
			if (lastModified.lastModified() < files[i].lastModified()) {
				lastModified = files[i];
			}
		}

		driver.findElement(By.xpath("//input[@type='file']")).sendKeys(lastModified.getAbsolutePath());
		driver.findElement(By.xpath(".//*[@id='TRNcontainer01']/div/table[2]/tbody/tr/td/input")).click();
		Thread.sleep(5000);

		System.out.println("==== Arquivo enviado com sucesso ! ====");

		driver.quit();
		String verificationErrorString = verificationErrors.toString();
		if (!"".equals(verificationErrorString)) {
			fail(verificationErrorString);
		}
		}catch (Exception e) {
			System.out.println(e.getMessage());
			
		}
	}

}