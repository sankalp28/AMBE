/*
 * package com.tvu.audiomixerTest;
 * 
 * //import static org.junit.Assert.Assert.assertEquals;
 * 
 * import java.io.BufferedReader; import java.io.IOException; import
 * java.io.InputStreamReader; import java.lang.reflect.Method;
 * 
 * import org.apache.http.HttpEntity; import org.apache.http.HttpResponse;
 * import org.apache.http.ParseException; import
 * org.apache.http.client.ClientProtocolException; import
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
 * public class SaveGroupTest extends BaseTest {
 * 
 * TestResource testNeededDetail=new TestResource(); public String getPostURL()
 * throws ClientProtocolException, IOException { // Given String taskid =
 * testNeededDetail.getTaskid(); String
 * endpoint=testNeededDetail.getGroupURL()+"?taskid=" + taskid ; //String
 * endpointPost=testNeededDetail.getGroupURL()+"?taskid="; HttpUriRequest
 * request = new HttpGet(endpoint);
 * 
 * // When HttpResponse httpResponse =
 * HttpClientBuilder.create().build().execute( request ); HttpEntity entity =
 * httpResponse.getEntity(); String apiBody=null; try { apiBody =
 * EntityUtils.toString(entity, "UTF-8"); } catch (ParseException | IOException
 * e) { // TODO Auto-generated catch block e.printStackTrace(); } //API Body
 * return apiBody; }
 * 
 * @Test public void validtaskidForSaveGroupStatusCodeAndMessage(Method method)
 * throws Exception { test=extent.createTest(method.getName(),
 * "validtaskidForSaveGroupStatusCodeAndMessage");
 * test.assignAuthor("Ankit Kumar Tripathi"); test.assignCategory("Unit Test");
 * // Given String taskid = testNeededDetail.getTaskid();
 * 
 * String endpoint=testNeededDetail.getGroupURL()+"?taskid=" + taskid ; //API
 * Body String apiBody = new SaveGroupTest().getPostURL(); CloseableHttpClient
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
 * Assert.assertEquals(response.toString().split("\"message\":\"")[1].split("\""
 * )[0],"Groups saved successfully"); Assert.assertEquals(
 * postResponse.getStatusLine().getStatusCode(), 200); // print result
 * httpClient.close(); }
 * 
 * @Test public void blanktaskidForSaveGroupStatusCodeAndMessage(Method method)
 * throws Exception { test=extent.createTest(method.getName(),
 * "blanktaskidForSaveGroupStatusCodeAndMessage");
 * test.assignAuthor("Ankit Kumar Tripathi"); test.assignCategory("Unit Test");
 * // Given String taskid = ""; String
 * endpoint=testNeededDetail.getGroupURL()+"?taskid=" + taskid ; //API Body
 * String apiBody = new SaveGroupTest().getPostURL(); CloseableHttpClient
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
 * ,"Groups not saved, there is some internal error."); Assert.assertEquals(
 * postResponse.getStatusLine().getStatusCode(), 503); httpClient.close(); }
 * 
 * @Test public void withouttaskidForSaveGroupStatusCodeAndMessage(Method
 * method) throws Exception { test=extent.createTest(method.getName(),
 * "withouttaskidForSaveGroupStatusCodeAndMessage");
 * test.assignAuthor("Ankit Kumar Tripathi"); test.assignCategory("Unit Test");
 * // Given String endpoint=testNeededDetail.getGroupURL(); //API Body String
 * apiBody = new SaveGroupTest().getPostURL(); CloseableHttpClient httpClient =
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
 * @Test public void inValidtaskidForSaveGroupStatusCodeAndMessage(Method
 * method) throws Exception { test=extent.createTest(method.getName(),
 * "inValidtaskidForSaveGroupStatusCodeAndMessage");
 * test.assignAuthor("Ankit Kumar Tripathi"); test.assignCategory("Unit Test");
 * 
 * // Given String taskid = "35.172.181.19"; String
 * endpoint=testNeededDetail.getGroupURL()+"?taskid=" + taskid ; //API Body
 * String apiBody = new SaveGroupTest().getPostURL(); CloseableHttpClient
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