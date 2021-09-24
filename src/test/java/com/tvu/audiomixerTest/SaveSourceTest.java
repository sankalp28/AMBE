/*
 * package com.tvu.audiomixerTest;
 * 
 * //import static org.junit.Assert.Assert.assertEquals;
 * 
 * import java.io.BufferedReader; import java.io.IOException; import
 * java.io.InputStreamReader; import java.lang.reflect.Method;
 * 
 * import org.apache.http.HttpEntity; import org.apache.http.HttpResponse;
 * import org.apache.http.client.ClientProtocolException; import
 * org.apache.http.client.methods.CloseableHttpResponse; import
 * org.apache.http.client.methods.HttpGet; import
 * org.apache.http.client.methods.HttpPost; import
 * org.apache.http.client.methods.HttpUriRequest; import
 * org.apache.http.entity.StringEntity; import
 * org.apache.http.impl.client.CloseableHttpClient; import
 * org.apache.http.impl.client.HttpClientBuilder; import
 * org.apache.http.impl.client.HttpClients; import
 * org.apache.http.util.EntityUtils; //import org.junit.Test; import
 * org.testng.Assert; import org.testng.annotations.Test;
 * 
 * public class SaveSourceTest extends BaseTest{
 * 
 * TestResource testNeededDetail=new TestResource(); public String getPostURL()
 * throws ClientProtocolException, IOException { // Given String taskid =
 * testNeededDetail.getTaskid(); String
 * endpoint=testNeededDetail.getSourceURL()+"?taskid=" + taskid ; //String
 * endpointPost=testNeededDetail.getSourceURL()+"?taskid="; HttpUriRequest
 * request = new HttpGet(endpoint);
 * 
 * // When HttpResponse httpResponse =
 * HttpClientBuilder.create().build().execute( request ); HttpEntity entity =
 * httpResponse.getEntity(); String apiBody = EntityUtils.toString(entity,
 * "UTF-8"); return apiBody; }
 * 
 * @Test public void validtaskidForSaveSourceStatusCodeAndMessage(Method method)
 * throws Exception { test=extent.createTest(method.getName(),
 * "validtaskidForSaveSourceStatusCodeAndMessage");
 * test.assignAuthor("Ankit Kumar Tripathi"); test.assignCategory("Unit Test");
 * // Given TestResource testNeededDetail=new TestResource(); String taskid =
 * testNeededDetail.getTaskid(); String
 * endpoint=testNeededDetail.getSourceURL()+"?taskid=" + taskid ;
 * //System.out.println("Endpoint : :"+endpoint); //API Body String apiBody =
 * new SaveSourceTest().getPostURL();
 * //System.out.println("Api Body : :"+apiBody); CloseableHttpClient httpClient
 * = HttpClients.createDefault(); HttpPost httpPost = new HttpPost(endpoint);
 * httpPost.addHeader("content-type", "application/json"); StringEntity params
 * =new StringEntity(apiBody); httpPost.setEntity(params); CloseableHttpResponse
 * postResponse = httpClient.execute(httpPost); HttpEntity entity =
 * postResponse.getEntity(); String responseString =
 * EntityUtils.toString(entity, "UTF-8"); //System.out.println(responseString);
 * Assert.assertEquals(responseString.toString().split(":")[1].split("\"")[1]
 * ,"Sources saved successfully"); Assert.assertEquals(
 * postResponse.getStatusLine().getStatusCode(), 200); httpClient.close(); }
 * 
 * @Test public void blanktaskidForSaveSourceStatusCodeAndMessage(Method method)
 * throws Exception { test=extent.createTest(method.getName(),
 * "blanktaskidForSaveSourceStatusCodeAndMessage");
 * test.assignAuthor("Ankit Kumar Tripathi"); test.assignCategory("Unit Test");
 * // Given String taskid = ""; String
 * endpoint=testNeededDetail.getSourceURL()+"?taskid=" + taskid ; //API Body
 * String apiBody = new SaveSourceTest().getPostURL(); CloseableHttpClient
 * httpClient = HttpClients.createDefault(); HttpPost httpPost = new
 * HttpPost(endpoint); httpPost.addHeader("content-type", "application/json");
 * StringEntity params =new StringEntity(apiBody); httpPost.setEntity(params);
 * CloseableHttpResponse postResponse = httpClient.execute(httpPost);
 * BufferedReader reader = new BufferedReader(new InputStreamReader(
 * postResponse.getEntity().getContent()));
 * 
 * String inputLine; StringBuffer response = new StringBuffer();
 * 
 * while ((inputLine = reader.readLine()) != null) { response.append(inputLine);
 * } reader.close();
 * Assert.assertEquals(response.toString().split(":")[1].split("\"")[1]
 * ,"Sources not saved, there is some internal error."); Assert.assertEquals(
 * postResponse.getStatusLine().getStatusCode(), 503); httpClient.close(); }
 * 
 * @Test public void withouttaskidForSaveSourceStatusCodeAndMessage(Method
 * method) throws Exception { test=extent.createTest(method.getName(),
 * "withouttaskidForSaveSourceStatusCodeAndMessage");
 * test.assignAuthor("Ankit Kumar Tripathi"); test.assignCategory("Unit Test");
 * // Given String endpoint=testNeededDetail.getSourceURL(); //API Body String
 * apiBody = new SaveSourceTest().getPostURL(); CloseableHttpClient httpClient =
 * HttpClients.createDefault(); HttpPost httpPost = new HttpPost(endpoint);
 * httpPost.addHeader("content-type", "application/json"); StringEntity params
 * =new StringEntity(apiBody); httpPost.setEntity(params); CloseableHttpResponse
 * postResponse = httpClient.execute(httpPost); BufferedReader reader = new
 * BufferedReader(new InputStreamReader(
 * postResponse.getEntity().getContent()));
 * 
 * String inputLine; StringBuffer response = new StringBuffer();
 * 
 * while ((inputLine = reader.readLine()) != null) { response.append(inputLine);
 * } reader.close();
 * Assert.assertEquals(response.toString().split(":")[1].split("\"")[1]
 * ,"Task Id is not provided."); Assert.assertEquals(
 * postResponse.getStatusLine().getStatusCode(), 400); httpClient.close(); }
 * 
 * @Test public void inValidtaskidForSaveSourceStatusCodeAndMessage(Method
 * method) throws Exception { test=extent.createTest(method.getName(),
 * "inValidtaskidForSaveSourceStatusCodeAndMessage");
 * test.assignAuthor("Ankit Kumar Tripathi"); test.assignCategory("Unit Test");
 * // Given String taskid = "35.172.181.19"; String
 * endpoint=testNeededDetail.getSourceURL()+"?taskid=" + taskid ; //API Body
 * String apiBody = new SaveSourceTest().getPostURL(); CloseableHttpClient
 * httpClient = HttpClients.createDefault(); HttpPost httpPost = new
 * HttpPost(endpoint); httpPost.addHeader("content-type", "application/json");
 * StringEntity params =new StringEntity(apiBody); httpPost.setEntity(params);
 * CloseableHttpResponse postResponse = httpClient.execute(httpPost);
 * BufferedReader reader = new BufferedReader(new InputStreamReader(
 * postResponse.getEntity().getContent()));
 * 
 * String inputLine; StringBuffer response = new StringBuffer();
 * 
 * while ((inputLine = reader.readLine()) != null) { response.append(inputLine);
 * } reader.close();
 * Assert.assertEquals(response.toString().split(":")[1].split("\"")[1]
 * ,"Audio mixer task not found."); Assert.assertEquals(
 * postResponse.getStatusLine().getStatusCode(), 404); // print result
 * httpClient.close(); } }
 */