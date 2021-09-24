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
 * org.apache.http.util.EntityUtils; import org.json.JSONObject; import
 * org.testng.Assert; import org.testng.annotations.Test;
 * 
 * import com.tvu.audiomixer.handler.AudioMixerHandler;
 * 
 * public class GetGroupTest extends BaseTest { TestResource
 * testNeededDetail=new TestResource();
 * 
 * @Test public void validtaskidForGetGroupStatusCode(Method method) throws
 * Exception { test=extent.createTest(method.getName(),
 * "validtaskidForGetGroupStatusCode");
 * test.assignAuthor("Ankit Kumar Tripathi"); test.assignCategory("Unit Test");
 * // Given String taskid = testNeededDetail.getTaskid(); String
 * endpoint=testNeededDetail.getGroupURL()+"?taskid=" + taskid ; HttpUriRequest
 * request = new HttpGet(endpoint);
 * 
 * // When HttpResponse httpResponse =
 * HttpClientBuilder.create().build().execute( request ); Assert.assertEquals(
 * httpResponse.getStatusLine().getStatusCode(), 200); }
 * 
 * @Test public void checkGroupDataJson(Method method) throws
 * ClientProtocolException, IOException {
 * test=extent.createTest(method.getName(), "checkGroupDataJson");
 * test.assignAuthor("Ankit Kumar Tripathi"); test.assignCategory("Unit Test");
 * // Given
 * 
 * String taskid = testNeededDetail.getTaskid(); String
 * endpoint=testNeededDetail.getGroupURL()+"?taskid=" + taskid ; HttpUriRequest
 * request = new HttpGet(endpoint);
 * 
 * // When HttpResponse httpResponse =
 * HttpClientBuilder.create().build().execute( request ); HttpEntity entity =
 * httpResponse.getEntity(); String responseString =
 * EntityUtils.toString(entity, "UTF-8");
 * Assert.assertEquals((responseString.contains("Groups") &&
 * responseString.contains("GroupName") &&
 * responseString.contains("ChannelIds")),true); }
 * 
 * @Test public void checkGroupJsonFormat(Method method) throws
 * ClientProtocolException, IOException {
 * test=extent.createTest(method.getName(), "checkGroupJsonFormat");
 * test.assignAuthor("Ankit Kumar Tripathi"); test.assignCategory("Unit Test");
 * // Given TestResource testNeededDetail=new TestResource(); String taskid =
 * testNeededDetail.getTaskid(); String endpoint=
 * testNeededDetail.getGroupURL()+"?taskid="+ taskid ; HttpUriRequest request =
 * new HttpGet(endpoint);
 * 
 * // When HttpResponse httpResponse =
 * HttpClientBuilder.create().build().execute( request ); HttpEntity entity =
 * httpResponse.getEntity(); String responseString =
 * EntityUtils.toString(entity, "UTF-8"); AudioMixerHandler audiomixer =new
 * AudioMixerHandler(); JSONObject jsonBody= new JSONObject(responseString);
 * Assert.assertEquals(audiomixer.validateRequestGroupsJson(jsonBody),true); }
 * 
 * @Test public void inValidtaskidForGetGroupStatusCodeAndMessage(Method method)
 * throws ClientProtocolException, IOException {
 * test=extent.createTest(method.getName(),
 * "inValidtaskidForGetGroupStatusCodeAndMessage"); // Given String taskid =
 * "ankittripathi"; String endpoint=testNeededDetail.getGroupURL()+"?taskid=" +
 * taskid ; HttpUriRequest request = new HttpGet(endpoint);
 * 
 * // When HttpResponse httpResponse =
 * HttpClientBuilder.create().build().execute( request ); String responseString
 * = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
 * Assert.assertEquals(responseString.split(":")[1].split("\"")[1]
 * ,"Audio mixer task not found."); Assert.assertEquals(
 * httpResponse.getStatusLine().getStatusCode(), 404); }
 * 
 * @Test public void blanktaskidForGetGroupStatusCodeAndMessage(Method method)
 * throws ClientProtocolException, IOException {
 * test=extent.createTest(method.getName(),
 * "blanktaskidForGetGroupStatusCodeAndMessage");
 * test.assignAuthor("Ankit Kumar Tripathi"); test.assignCategory("Unit Test");
 * // Given String taskid = ""; String
 * endpoint=testNeededDetail.getGroupURL()+"?taskid=" + taskid ; HttpUriRequest
 * request = new HttpGet(endpoint);
 * 
 * // When HttpResponse httpResponse =
 * HttpClientBuilder.create().build().execute( request ); HttpEntity entity =
 * httpResponse.getEntity(); String responseString =
 * EntityUtils.toString(entity, "UTF-8");
 * Assert.assertEquals(responseString.split(":")[1].split("\"")[1]
 * ,"Unable to retrieve the groups, there is some internal error.");
 * Assert.assertEquals( httpResponse.getStatusLine().getStatusCode(), 503); }
 * 
 * @Test public void withouttaskidForGetGroupStatusCodeAndMessage(Method method)
 * throws ClientProtocolException, IOException {
 * test=extent.createTest(method.getName(),
 * "blanktaskidForGetGroupStatusCodeAndMessage");
 * test.assignAuthor("Ankit Kumar Tripathi"); test.assignCategory("Unit Test");
 * // Given String endpoint=testNeededDetail.getGroupURL(); HttpUriRequest
 * request = new HttpGet(endpoint);
 * 
 * // When HttpResponse httpResponse =
 * HttpClientBuilder.create().build().execute( request ); String responseString
 * = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
 * Assert.assertEquals(responseString.split(":")[1].split("\"")[1]
 * ,"Task Id is not provided."); Assert.assertEquals(
 * httpResponse.getStatusLine().getStatusCode(), 400); } }
 */