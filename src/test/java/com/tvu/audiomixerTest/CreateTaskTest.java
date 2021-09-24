package com.tvu.audiomixerTest;

//import static org.junit.Assert.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.testng.Assert;
import org.testng.annotations.Test;

/*
 * 
 * This is a test class for task creation
 * CreateTaskIdStatusCodeAndMessage
 */
public class CreateTaskTest extends BaseTest{
	TestResource testNeededDetail=new TestResource();
	
	//@Test
	public void CreateTaskIdStatusCodeAndMessage(Method method) throws Exception {
		
		test=extent.createTest(method.getName(), "CreateTaskIdStatusCodeAndMessage");
		test.assignAuthor("Ankit Kumar Tripathi");
		test.assignCategory("Unit Test");
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
		// print result
		Assert.assertEquals(response.toString().split("\":\"")[1].isEmpty(),false);
		Assert.assertEquals(
				postResponse.getStatusLine().getStatusCode(),
				200);
		httpClient.close();
	}
	@Test
	public void withoutVMCreateTaskStatusCodeAndMessage(Method method) throws Exception {

		test=extent.createTest(method.getName(), "withoutVMCreateTaskStatusCodeAndMessage");
		// Given
		String endpoint=testNeededDetail.getTaskURL();
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
		// print result
		test.assignAuthor("Ankit Kumar Tripathi");
		test.assignCategory("Unit Test");
		Assert.assertEquals(response.toString().split(":")[1].split("\"")[1],"VM IP address is not provided.");
		Assert.assertEquals(
				postResponse.getStatusLine().getStatusCode(),
				400);
		httpClient.close();
	}
	
}
