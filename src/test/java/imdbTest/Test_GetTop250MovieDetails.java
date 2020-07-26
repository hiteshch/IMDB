//Runner

package imdbTest;

import java.io.BufferedWriter;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import variables.GlobalVariables;
import database.DBConnect;
import io.github.bonigarcia.wdm.WebDriverManager;
import Pages.IMDBHomePage;
import Pages.IMDBTop250Page;

@Test
public class Test_GetTop250MovieDetails {

	public WebDriver driver;
	
	IMDBHomePage objHomePage = new IMDBHomePage(driver);
	IMDBTop250Page objTop250 = new IMDBTop250Page(driver);
	DBConnect objDBConnect = new DBConnect();
	GlobalVariables objGlobal = new GlobalVariables();
	
	@BeforeClass
	public void setUp() {
		WebDriverManager.chromedriver().setup();
		driver = new ChromeDriver();
		 
	}

	@BeforeTest
	public void droptable() throws SQLException {
		objGlobal.strQuery = "DELETE FROM IMDB_Top250";
		objDBConnect.Connect();
		objDBConnect.exeQuery(objGlobal.strQuery);
		System.out.println("Records Deleted");
	}

	// Class Variables


	// Opens the IMDB Web site and checks for the title of the page
	@Test
	public void GetURL() {

		driver.manage().window().maximize();
		driver.get(objHomePage.strWebURL);
		System.out.println(driver.getTitle());
		Assert.assertEquals(driver.getTitle(), objHomePage.strHomePageTitle);
	}

	// Navigate to Top 250 Page
	@Test(dependsOnMethods = { "GetURL" })
	public void NavigateInIMDB() throws InterruptedException {

		WebElement btnChartDropDown = driver.findElement(By.xpath("/html/body/div[1]/nav/div[2]/label[2]"));
		btnChartDropDown.click();
		Thread.sleep(3000L);
		WebElement lnkTop250 = driver.findElement(
				By.xpath("//*[@id=\"imdbHeader\"]/div[2]/aside/div/div[2]/div/div[1]/span/div/div/ul/a[3]"));
		lnkTop250.click();
		// objHomePage.lnkTop250.click();
		Assert.assertEquals(driver.getTitle(), objTop250.strTop250PageTitle);
	}

	// Connect to the database and fetch the data from the Web Page
	@Test(dependsOnMethods = { "GetURL", "NavigateInIMDB" })
	public void FetchData() throws SQLException {
		objDBConnect.Connect();
		objTop250.moviecount = 1;
		int rating = 0;
		System.out.println("Creating database ..... ");
		WebElement tblMovieList = driver.findElement(By.xpath("//*[@id=\"main\"]/div/span/div/div/div[3]/table/tbody"));
		// Find required the child objects
		for (objTop250.yearcount = 0; objTop250.yearcount < 250; objTop250.yearcount++) {

			// Find Movie Name and handle the ' sign in the movie name for SQLite Query
			objTop250.movieName = tblMovieList.findElements(By.tagName("tr")).get(objTop250.yearcount).findElements(By.tagName("td"))
					.get(objTop250.moviecount).findElements(By.tagName("a")).get(rating).getText();
			System.out.println("Movie Name " + objTop250.movieName);
			If(objTop250.movieName.contains("'"));
			{
				objTop250.movieName = objTop250.movieName.replaceAll("'", "''");
			}

			// Find Movie Year and remove the brackets
			objTop250.movieYear = tblMovieList.findElements(By.tagName("tr")).get(objTop250.yearcount).findElements(By.tagName("td"))
					.get(objTop250.moviecount).findElement(By.tagName("span")).getText();
			objTop250.movieYear = objTop250.movieYear.substring(1, 5);
			System.out.println("Movie year " + objTop250.movieYear);

			// Find the Movie Ratings
			objTop250.movieRating = tblMovieList.findElements(By.tagName("tr")).get(objTop250.yearcount)
					.findElements(By.tagName("td")).get(objTop250.moviecount + 1).findElement(By.tagName("strong")).getText();
			System.out.println("Movie rating " + objTop250.movieRating);

			// Update the SQLite database with the all the results

			objGlobal.strQuery = "INSERT INTO IMDB_Top250 (Sr_No,Movie_Name,Movie_Year,Movie_Rating) " + "VALUES ("
					+ (objTop250.yearcount + 1) + ",'" + objTop250.movieName + "','" + objTop250.movieYear + "','" + objTop250.movieRating + "')";
			objDBConnect.exeQuery(objGlobal.strQuery);

		}
	}

	private void If(boolean contains) {
		// TODO Auto-generated method stub

	}

	// Test to Create the file with all Movie_List
	@Test(dependsOnMethods = { "GetURL", "NavigateInIMDB", "FetchData" })
	public void CreateFile() throws SQLException {
		try {
			System.out.println("Database Created Successfully");
			FileWriter objwriter = new FileWriter(objGlobal.strFilePath);
			BufferedWriter objbw = new BufferedWriter(objwriter);
			objDBConnect.Connect();
			objGlobal.conn = DriverManager.getConnection(objGlobal.strDBPath);
			objGlobal.stmt = objGlobal.conn.createStatement();
			objGlobal.strQuery = "select * from IMDB_Top250";
			objGlobal.rs = objGlobal.stmt.executeQuery(objGlobal.strQuery);
			ResultSetMetaData col = objGlobal.rs.getMetaData();
			objbw.write(col.getColumnName(1) + "," + col.getColumnName(2) + "," + col.getColumnName(3) + ","
					+ col.getColumnName(4));
			objbw.newLine();
			while (objGlobal.rs.next()) {
				objbw.write(objGlobal.rs.getInt("Sr_No") + "," + objGlobal.rs.getString("Movie_Name") + ","
						+ objGlobal.rs.getString("Movie_Year") + "," + objGlobal.rs.getString("Movie_Rating"));
				objbw.newLine();
			}
			objbw.close();
			objGlobal.rs.close();
			objGlobal.stmt.close();
			objGlobal.conn.close();
			System.out.println("File is created successfully and placed at " + objGlobal.strFilePath);
		} catch (IOException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
	}
}
