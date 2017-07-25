package br.com.connectbeneficios.test;

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ExportarRetorno {

	private static String dataRemessa = "";

	Calendar c = Calendar.getInstance();
	Date data = c.getTime();
	DateFormat f = DateFormat.getDateInstance(DateFormat.SHORT);

	TimerTask task = new TimerTask() {
		public void run() {
			if (dataRemessa.equals("")) {
				System.out.println("...@...");
				dataRemessa = f.format(data);
				try {
					AcessarSistema();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	};

	public void getInputData() throws Exception {
		Timer timer = new Timer();
		timer.schedule(task, 10 * 1000);

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("Digite a data de vencimento ou aperte ENTER para seguir com a data atual: ");
		dataRemessa = in.readLine();

		if (!dataRemessa.isEmpty()) {
			try {
				Date dataValida = sdf.parse(dataRemessa);
				try {
					AcessarSistema();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} catch (ParseException e) {
				System.err.println("Data inválida");
				return;
			}
		}

		timer.cancel();
	}

	public static void main(String[] args) throws InterruptedException, IOException {

		try {
			(new ExportarRetorno()).getInputData();
		} catch (Exception e) {
			System.out.println(e);
		}

	}

	public void AcessarSistema() throws InterruptedException {

		try {

			WebDriver driver;
			String baseUrl;
			StringBuffer verificationErrors = new StringBuffer();

			System.out.println("==== Remessa data: " + dataRemessa + " ====");
			System.out.println("==== Iniciando driver ====");

			String downloadFilepath = "C:/Users/suport/Desktop/Remessas/Remessa itau/Retorno";
			HashMap<String, Object> chromePrefs = new HashMap<String, Object>();

			chromePrefs.put("profile.default_content_settings.popups", 0);
			chromePrefs.put("download.default_directory", downloadFilepath);

			ChromeOptions options = new ChromeOptions();
			options.setExperimentalOption("prefs", chromePrefs);

			DesiredCapabilities cap = DesiredCapabilities.chrome();
			cap.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
			cap.setCapability(ChromeOptions.CAPABILITY, options);

			System.setProperty("webdriver.chrome.driver", "C:\\Users\\suport\\workspace\\chromedriver.exe");
			cap.setCapability("requireWindowFocus", true);
			driver = new ChromeDriver(cap);

			baseUrl = "https://www.itau.com.br/";
			driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);

			System.out.println("==== Acessando Itaú ====");

			driver.get(baseUrl);
			driver.findElement(By.id("linkfirst")).click();
			driver.findElement(By.id("codop")).click();
			driver.findElement(By.id("campo_cod_operador")).clear();
			driver.findElement(By.id("campo_cod_operador")).sendKeys(lerRegistro("HKCU\\Software\\Microsoft\\RoboRemessa", "ContaItau"));
//			driver.findElement(By.linkText("Acessar")).click();
			driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);

			// TODO Alguém esta acessando a conta

			System.out.println("==== Inserindo Senha ====");
			String passe = lerRegistro("HKCU\\Software\\Microsoft\\RoboRemessa", "ContaItauPasse");
			int[] numeroPasse = new int[6];
			for (int i = 0; i < 6; i++) {
				if (!Character.isDigit(passe.charAt(i))) {
					System.out.println("==== Dígito Inválido ====");
					return;
				}
				numeroPasse[i] = Integer.parseInt(String.valueOf(passe.charAt(i)));
			}
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
			if (senha.getClass().toString() == "invalido") {
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
//			driver.findElement(By.cssSelector("button.mfp-close")).click();
			System.out.println("==== Acessando Transfêrencia de arquivos ====");

			WebElement element = driver.findElement(By.linkText("menu"));
			builder.moveToElement(element).build().perform();

			driver.findElement(By.linkText("Transmissão de arquivos")).click();

			Thread.sleep(2000);

			driver.findElement(By.linkText("Recepcionar")).click();

			driver.switchTo().frame("output_frame_recepcao");

			Thread.sleep(1000);
			driver.findElement(By.xpath("//div[@id='divTabelas']/table/tbody/tr")).click();

			WebElement tabela = driver.findElement(By.id("ctl01_tabelaArquivos"));
			List<WebElement> tr = tabela.findElements(By.xpath("id('ctl01_tabelaArquivos')/tbody/tr"));

			System.out.println("==== Exportando arquivo ====");

			// System.out.println("linhas = "+tr.size());

			int numero_linha, numero_coluna;
			boolean linhaAtiva = false;
			numero_linha = 1;
			for (WebElement trE : tr) {
				linhaAtiva = false;
				if (numero_linha >= 2) {
					List<WebElement> td = trE.findElements(By.xpath("td"));
					numero_coluna = 1;

					for (WebElement tdE : td) {
						if (numero_coluna == 1 && tdE.getText().contains(dataRemessa)) {
							System.out.println("linha " + numero_linha + " | coluna " + numero_coluna + " | Texto "
									+ tdE.getText() + "\n");
							linhaAtiva = true;
						}
						if (numero_coluna == 5 && linhaAtiva == true) {
							System.out.println("linha " + numero_linha + " | coluna " + numero_coluna + " | Texto "
									+ tdE.getText() + "\n");
							tdE.click();
							System.out.println("==== Arquivo salvo em " + downloadFilepath + " ====");
							Thread.sleep(5000);
							return;
						}
						numero_coluna++;
					}
				}
				numero_linha++;
			}


			driver.quit();
			String verificationErrorString = verificationErrors.toString();
			if (!"".equals(verificationErrorString)) {
				fail(verificationErrorString);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());

		}
	}
	public static final String lerRegistro(String strLocalizacao, String strChave){
        StringWriter stringWriter = new StringWriter();
        InputStream inputStream;
        String strResultadoBusca;
        int i;
 
        try {
            //Roda reg query, depois lê a saída com StreamReader (classe interna)
            Process process = Runtime.getRuntime().exec("reg query " + strLocalizacao + " /v " + strChave);
 
            inputStream = process.getInputStream();
            while ((i = inputStream.read()) != -1)
                stringWriter.write(i);
 
            //strResultadoBusca contém o resultado completo da busca
            //(localização, chave, tipo do registro e valor).
            strResultadoBusca = stringWriter.toString();
 
            //Imprime o resultado completo da busca. Caso necessite de todos os
            //campos basta retornar essa variável (strResultadoBusca).
            System.out.println(strResultadoBusca);
 
            //Verifica qual o caracter utilizado para separação dos campos.
            //Win 7 = " " e Win XP = "t"
 
            if (System.getProperty("os.name").equals("Windows 10")) {
                if(strResultadoBusca.contains(" ")){
                    //Separa os campos em um vetor de strings.
                    String[] camposRegistro = strResultadoBusca.split(" ");
                    strResultadoBusca = camposRegistro[camposRegistro.length-1];
                } else {
                    strResultadoBusca = null;
                }
            } else {
                if(strResultadoBusca.contains("t")){
                    //Separa os campos em um vetor de strings.
                    String[] camposRegistro = strResultadoBusca.split("t");
                    strResultadoBusca = camposRegistro[camposRegistro.length-1];
                } else {
                    strResultadoBusca = null;
                }
            }
            return strResultadoBusca;
        } catch (Exception e) {
            return null;
        }
    }

}