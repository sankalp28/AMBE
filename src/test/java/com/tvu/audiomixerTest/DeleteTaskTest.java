package com.tvu.audiomixerTest;

//import static org.junit.Assert.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.Test;


public class DeleteTaskTest extends BaseTest{

	TestResource testNeededDetail=new TestResource();
	private String createTaskId() throws Exception{
		// Given
		String vmip ="10.191.74.80";
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
	@Test 
	public void validTaskIddeleteStatusCodeAndMessage(Method method) throws Exception{
		test=extent.createTest(method.getName(), "validTaskIddeleteStatusCodeAndMessage");
		test.assignAuthor("Ankit Kumar Tripathi");
		test.assignCategory("Unit Test");
		String deleteEndpoint = testNeededDetail.getTaskURL()+"/"+ new DeleteTaskTest().createTaskId();
		CloseableHttpClient httpclient = HttpClients.createDefault();

		HttpDelete httpDelete = new HttpDelete(deleteEndpoint);
		HttpResponse response = httpclient.execute(httpDelete);
		HttpEntity entity = response.getEntity();
		String responseString = EntityUtils.toString(entity, "UTF-8");
		BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
		Assert.assertEquals(
				responseString.toString().split("\":\"")[1].split("\"")[0],
				"Task deleted successfully");
		Assert.assertEquals(
				response.getStatusLine().getStatusCode(),
				200);
	} 
	@Test
	public void validTaskIdTwicedeleteStatusAndMessage(Method method) throws Exception{
		test=extent.createTest(method.getName(), "validTaskIdTwicedeleteStatusAndMessage");
		test.assignAuthor("Ankit Kumar Tripathi");
		test.assignCategory("Unit Test");
		String deleteEndpoint = testNeededDetail.getTaskURL()+"/"+ new DeleteTaskTest().createTaskId();
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpDelete httpDelete = new HttpDelete(deleteEndpoint);
		HttpResponse response = httpclient.execute(httpDelete);
		HttpResponse response1 = httpclient.execute(httpDelete);
		HttpEntity entity = response1.getEntity();
		String responseString = EntityUtils.toString(entity, "UTF-8");
		Assert.assertEquals(
				responseString.toString().split("\":\"")[1].split("\"")[0],
				"Task is already deleted.");
		Assert.assertEquals(
				response1.getStatusLine().getStatusCode(),
				200);
	} 

	@Test 
	public void inValidTaskIddeleteStatusCodeAndMessage(Method method) throws Exception{
		test=extent.createTest(method.getName(), "inValidTaskIddeleteStatusCodeAndMessage");
		test.assignAuthor("Ankit Kumar Tripathi");
		test.assignCategory("Unit Test");
		String deleteEndpoint = testNeededDetail.getTaskURL()+"/"+ "testd8f6f19-b2b2-4a88-b683";
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpDelete httpDelete = new HttpDelete(deleteEndpoint);
		HttpResponse response = httpclient.execute(httpDelete);
		HttpEntity entity = response.getEntity();
		String responseString = EntityUtils.toString(entity, "UTF-8");
		Assert.assertEquals(
				responseString.toString().split("\":\"")[1].split("\"")[0],
				"Audio mixer task not found.");
		Assert.assertEquals(
				response.getStatusLine().getStatusCode(),
				404);
	} 
} 

