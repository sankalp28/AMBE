package com.tvu.audiomixerTest;



import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class TestResource {
	InputStream inputStream;
	String result = "";

	public String getVMValues() throws IOException {

		try {
			Properties prop = new Properties();
			String propFileName = "testcase.properties";

			inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

			if (inputStream != null) {
				prop.load(inputStream);
			} else {
				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
			}
			result = prop.getProperty("vmip");
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		} finally {
			inputStream.close();
		}
		return result;
	}
	public String getSourceURL() throws IOException {

		try {
			Properties prop = new Properties();
			String propFileName = "testcase.properties";

			inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

			if (inputStream != null) {
				prop.load(inputStream);
			} else {
				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
			}
			result = prop.getProperty("sourceURL");
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		} finally {
			inputStream.close();
		}
		return result;
	}
	public String getGroupURL() throws IOException {

		try {
			Properties prop = new Properties();
			String propFileName = "testcase.properties";

			inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

			if (inputStream != null) {
				prop.load(inputStream);
			} else {
				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
			}
			result = prop.getProperty("groupURL");
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		} finally {
			inputStream.close();
		}
		return result;
	}
	public String getTaskURL() throws IOException {

		try {
			Properties prop = new Properties();
			String propFileName = "testcase.properties";

			inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

			if (inputStream != null) {
				prop.load(inputStream);
			} else {
				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
			}
			result = prop.getProperty("createTaskURL");
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		} finally {
			inputStream.close();
		}
		return result;
	}
	public String getTaskid() throws IOException {

		try {
			Properties prop = new Properties();
			String propFileName = "testcase.properties";

			inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

			if (inputStream != null) {
				prop.load(inputStream);
			} else {
				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
			}
			result = prop.getProperty("taskid");
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		} finally {
			inputStream.close();
		}
		return result;
	}
	public String genrateTaskIdforValidVM() throws IOException
	{
		TestResource testNeededDetail= new TestResource();
		// Given
		String vmip = testNeededDetail.getVMValues();
		String endpoint=testNeededDetail.getTaskURL()+"?vmip=" + vmip ;
		//API Body
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(endpoint);
		httpPost.addHeader("content-type", "application/json");
		CloseableHttpResponse postResponse = httpClient.execute(httpPost);
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				postResponse.getEntity().getContent()));

		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = reader.readLine()) != null) {
			response.append(inputLine);
		}
		reader.close();
		httpClient.close();
		return response.toString().split("\":\"")[1].split("\"")[0];
	}
}

