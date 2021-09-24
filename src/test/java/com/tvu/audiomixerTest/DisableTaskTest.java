
package com.tvu.audiomixerTest;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;


public class DisableTaskTest {

	TestResource testNeededDetail=new TestResource();
	private String createTaskId() throws Exception {
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
	public void validTaskIdDisableStatusCode() throws Exception {

		String deleteEndpoint = testNeededDetail.getTaskURL()+"/"+ new DisableTaskTest().createTaskId()+"?status=false";
		CloseableHttpClient httpclient = HttpClients.createDefault();

		HttpDelete httpDelete = new HttpDelete(deleteEndpoint);
		HttpResponse response = httpclient.execute(httpDelete);
		HttpEntity entity = response.getEntity();
		String responseString = EntityUtils.toString(entity, "UTF-8");
		BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
		assertEquals(
				response.getStatusLine().getStatusCode(),
				200);
	} 


	@Test 
	public void inValidTaskIdDisableStatusCode() throws Exception {

		String deleteEndpoint = testNeededDetail.getTaskURL()+"/"+ "testd8f6f19-b2b2-4a88-b683?status=false";
		CloseableHttpClient httpclient = HttpClients.createDefault();

		HttpDelete httpDelete = new HttpDelete(deleteEndpoint);
		HttpResponse response = httpclient.execute(httpDelete);
		HttpEntity entity = response.getEntity();
		String responseString = EntityUtils.toString(entity, "UTF-8");
		assertEquals(
				response.getStatusLine().getStatusCode(),
				404);
	} 
} 
