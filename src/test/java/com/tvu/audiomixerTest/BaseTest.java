package com.tvu.audiomixerTest;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.configuration.ChartLocation;
import com.aventstack.extentreports.reporter.configuration.Theme;

public class BaseTest
{
	public static ExtentHtmlReporter htmlReporter;
	public static ExtentReports extent;
	public static ExtentTest test;

	@BeforeSuite
	public void setUp() throws UnknownHostException
	{
		htmlReporter = new ExtentHtmlReporter(System.getProperty("user.dir") +"/test-output/Dashboard_Report.html");
		extent = new ExtentReports();
		extent.attachReporter(htmlReporter);

		extent.setSystemInfo("OS", System.getProperty("os.name"));
		extent.setSystemInfo("Host Name", InetAddress.getLocalHost().getHostName());
		extent.setSystemInfo("Environment", "Dev");
		extent.setSystemInfo("User Name", "AudioMixer");

		htmlReporter.config().setChartVisibilityOnOpen(true);
		htmlReporter.config().setDocumentTitle("Unit Testing APIGateway Report");
		htmlReporter.config().setReportName("Testing Report");
		htmlReporter.config().setTestViewChartLocation(ChartLocation.TOP);
		htmlReporter.config().setTheme(Theme.DARK);
	}

	@AfterMethod
	public void getResult(ITestResult result)
	{
		if(result.getStatus() == ITestResult.FAILURE)
		{
			test.log(Status.FAIL, MarkupHelper.createLabel(result.getName()+" Test case FAILED due to below issues:", ExtentColor.RED));
			test.fail(result.getThrowable());
		}
		else if(result.getStatus() == ITestResult.SUCCESS)
		{
			test.log(Status.PASS, MarkupHelper.createLabel(result.getName()+" Test Case PASSED", ExtentColor.GREEN));
		}
		else
		{
			test.log(Status.SKIP, MarkupHelper.createLabel(result.getName()+" Test Case SKIPPED", ExtentColor.ORANGE));
			test.skip(result.getThrowable());
		}
	}
	@AfterSuite
	public void tearDown()
	{
		extent.flush();
	}
}