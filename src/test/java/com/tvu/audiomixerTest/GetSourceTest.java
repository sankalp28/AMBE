/*
 * package com.tvu.audiomixerTest;
 * 
 * //import static org.junit.Assert.Assert.assertEquals;
 * 
 * import java.io.IOException; import java.lang.reflect.Method;
 * 
 * import org.apache.http.HttpEntity; import org.apache.http.HttpResponse;
 * import org.apache.http.client.ClientProtocolException; import
 * org.apache.http.client.methods.HttpGet; import
 * org.apache.http.client.methods.HttpUriRequest; import
 * org.apache.http.impl.client.HttpClientBuilder; import
 * org.apache.http.util.EntityUtils; import org.testng.Assert; import
 * org.testng.annotations.Test; public class GetSourceTest extends BaseTest {
 * TestResource testNeededDetail=new TestResource();
 * 
 * @Test public void validtaskidForGetSourceStatusCode(Method method) throws
 * ClientProtocolException, IOException {
 * test=extent.createTest(method.getName(),
 * "validtaskidForGetSourceStatusCode");
 * test.assignAuthor("Ankit Kumar Tripathi"); test.assignCategory("Unit Test");
 * // Given String taskid = testNeededDetail.getTaskid(); String
 * endpoint=testNeededDetail.getSourceURL()+"?taskid=" + taskid ; HttpUriRequest
 * request = new HttpGet(endpoint);
 * 
 * // When HttpResponse httpResponse =
 * HttpClientBuilder.create().build().execute( request ); Assert.assertEquals(
 * httpResponse.getStatusLine().getStatusCode(), 200); }
 * 
 * @Test public void inValidtaskidForGetSourceStatusCodeAndMessage(Method
 * method) throws ClientProtocolException, IOException {
 * test=extent.createTest(method.getName(),
 * "inValidtaskidForGetSourceStatusCodeAndMessage");
 * test.assignAuthor("Ankit Kumar Tripathi"); test.assignCategory("Unit Test");
 * // Given String taskid = "10.191.74.65"; String
 * endpoint=testNeededDetail.getSourceURL()+"?taskid=" + taskid ; HttpUriRequest
 * request = new HttpGet(endpoint);
 * 
 * // When HttpResponse httpResponse =
 * HttpClientBuilder.create().build().execute( request ); HttpEntity entity =
 * httpResponse.getEntity(); String responseString =
 * EntityUtils.toString(entity, "UTF-8");
 * Assert.assertEquals(responseString.split(":")[1].split("\"")[1]
 * ,"Audio mixer task not found."); Assert.assertEquals(
 * httpResponse.getStatusLine().getStatusCode(), 404); }
 * 
 * @Test public void blanktaskidForGetSourceStatusCodeAndMessage(Method method)
 * throws ClientProtocolException, IOException {
 * test=extent.createTest(method.getName(),
 * "blanktaskidForGetSourceStatusCodeAndMessage");
 * test.assignAuthor("Ankit Kumar Tripathi"); test.assignCategory("Unit Test");
 * // Given String taskid = ""; String
 * endpoint=testNeededDetail.getSourceURL()+"?taskid=" + taskid ; HttpUriRequest
 * request = new HttpGet(endpoint);
 * 
 * // When HttpResponse httpResponse =
 * HttpClientBuilder.create().build().execute( request ); HttpEntity entity =
 * httpResponse.getEntity(); String responseString =
 * EntityUtils.toString(entity, "UTF-8");
 * Assert.assertEquals(responseString.split(":")[1].split("\"")[1]
 * ,"Unable to get the sources"); Assert.assertEquals(
 * httpResponse.getStatusLine().getStatusCode(), 503); }
 * 
 * @Test public void withouttaskidForGetSourceStatusCodeAndMessage(Method
 * method) throws ClientProtocolException, IOException {
 * test=extent.createTest(method.getName(),
 * "withouttaskidForGetSourceStatusCodeAndMessage");
 * test.assignAuthor("Ankit Kumar Tripathi"); test.assignCategory("Unit Test");
 * // Given String endpoint=testNeededDetail.getSourceURL(); HttpUriRequest
 * request = new HttpGet(endpoint);
 * 
 * // When HttpResponse httpResponse =
 * HttpClientBuilder.create().build().execute( request ); HttpEntity entity =
 * httpResponse.getEntity(); String responseString =
 * EntityUtils.toString(entity, "UTF-8");
 * Assert.assertEquals(responseString.split(":")[1].split("\"")[1]
 * ,"Task Id is not provided."); Assert.assertEquals(
 * httpResponse.getStatusLine().getStatusCode(), 400); } }
 */