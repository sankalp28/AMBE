package com.tvu.audiomixer.handler;

import java.io.BufferedReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.amazonaws.client.builder.AwsClientBuilder;
//import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApi;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApiClient;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApiClientBuilder;
import com.amazonaws.services.apigatewaymanagementapi.model.PostToConnectionRequest;
//import com.amazonaws.services.apigateway.AmazonApiGateway;
//import com.amazonaws.services.apigateway.AmazonApiGatewayClient;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.tvu.audiomixer.dao.AudioMixerDao;
import com.tvu.audiomixer.dao.AudioMixerDaoImpl;
import com.tvu.audiomixer.dao.model.AudioMixer;
import com.tvu.audiomixer.dao.model.AudioMixerClient;
import com.tvu.audiomixer.meeting.token.AgoraTokenGenerator;

public class AudioMixerHandler implements RequestHandler<Object, Object> {
	String TASK_IS_DISABLED = "Task is disabled.";
	String TASK_STATUS_UPDATED = "Task status is updated successfully.";
	String TASK_STATUS_NOTPROVIDED = "Task status not provided.Please provide 'status' as query param with value true/false";
	String TASK_STATUS_UPDATE_ERROR = "Unable to update the task status.";
	String TASK_ALREADY_DELETED = "Task is already deleted.";
	String TASK_NOTFOUND = "Audio mixer task not found.";
	String TASKID_NOT_PROVIDED = "Task Id is not provided.";
	String VM_ADDRESS_NOT_PROVIDED = "VM IP address is not provided.";
	String TASK_CREATION_ERROR = "Unable to create task, there is some internal error.";
	String TASK_DELETION_SUCCESS = "Task deleted successfully";

	String TASK_DELETION_ERROR = "Unable to delete the task, there is some internal error.";
	String INVALID_JSON = "Invalid JSON is passed in the request body.";
	String NO_SOURCES_FOR_TASK = "Sources not available for the task.";
	String UPDATED_GAINS_ERROR = "Unable to update the sources updated gains in producer";
	String GROUPS_CREATION_SUCCESS = "Groups saved successfully";
	String GROUPS_CREATION_ERROR = "Groups not saved, there is some internal error.";
	String GET_GROUPS_ERROR = "Unable to retrieve the groups, there is some internal error.";
	String GET_SOURCES_ERROR = "Unable to get the sources";
	String SOURCES_CREATION_SUCCESS = "Sources saved successfully";
	String SOURCES_CREATION_ERROR = "Sources not saved, there is some internal error.";
	String SCREENID_NOT_PROVIDED = "ScreenId is not provided.";
	String AFV_IS_ON = "AFV is set to true.";
	// int MUTE_GAIN = 0;
	// int INITIAL_GAIN = 239;

	String AGORA_APPID = "bbae89fd0cb14412883a3adf1ee57e4a";
	String AGORA_APP_CERT = "72d4d777778b4389ac234e1c58d21f45";
	int AGORA_TOKEN_EXPIRATION = 86400; // in seconds..24 hours

	@Override
	public Map<String, Object> handleRequest(Object input, Context context) {
		Map<String, Object> responseMap = new HashMap();
		Map<String, Object> inputParams = (Map<String, Object>) input;
		Map<String, String> requestContext = (Map<String, String>) inputParams.get("requestContext");
		Map<String, String> header = new HashMap<String, String>();

		// System.out.println("inputParams...\n" + inputParams);

		header.put("Content-Type", "application/json; charset=UTF-8");
		header.put("Access-Control-Allow-Origin", "*");
		header.put("Access-Control-Allow-Methods", "GET,POST,PUT,OPTIONS");

		String eventType = "";
		if (requestContext.get("eventType") != null) {
			eventType = (String) requestContext.get("eventType");
			if (eventType.equals("CONNECT")) {
				responseMap = saveClientConnection(inputParams);
				return responseMap;
			} else if (eventType.equals("DISCONNECT")) {
				responseMap = deleteClientConnection(inputParams);
				responseMap.put("headers", header);
				return responseMap;
			} else if (eventType.equals("MESSAGE")) {
				System.out.println("@@Entring the web socket request processing......");
				responseMap = processRequest(inputParams);
				responseMap.put("headers", header);
				return responseMap;
			} /*
				 * else if (eventType.equals("MESSAGE")) { responseMap =
				 * sendClientNotifications(inputParams); responseMap.put("headers", header);
				 * return responseMap; }
				 */
		}

		String resource = (String) inputParams.get("resource");
		String httpMethod = (String) inputParams.get("httpMethod");

		header.put("Content-Type", "application/json; charset=UTF-8");

		if (resource.equals("/tvu/audiomixer/v1/sources") && "GET".equalsIgnoreCase(httpMethod)) {
			responseMap = getSources(inputParams);
		} else if (resource.equals("/tvu/audiomixer/v1/groups") && "GET".equalsIgnoreCase(httpMethod)) {
			responseMap = getGroups(inputParams);
		} else if (resource.equals("/tvu/audiomixer/v1/groups") && "POST".equalsIgnoreCase(httpMethod)) {
			responseMap = saveGroups(inputParams);
		} else if (resource.equals("/tvu/audiomixer/v1/sources") && "POST".equalsIgnoreCase(httpMethod)) {
			responseMap = saveSources(inputParams);
		} else if (resource.equals("/tvu/audiomixer/v1/task") && "POST".equalsIgnoreCase(httpMethod)) {
			responseMap = createAudioMixerTask(inputParams);
		} else if (resource.equals("/tvu/audiomixer/v1/task/{taskId}") && "DELETE".equalsIgnoreCase(httpMethod)) {
			responseMap = deleteAudioMixerTask(inputParams);
		} else if (resource.equals("/tvu/audiomixer/v1/task/{taskId}") && "PUT".equalsIgnoreCase(httpMethod)) {
			responseMap = updateAudioMixerTaskStatus(inputParams);
		} else if (resource.equals("/tvu/audiomixer/v1/notifyupdatesources") && "POST".equalsIgnoreCase(httpMethod)) {
			responseMap = sentNotificationForTask(inputParams);
		} else if (resource.equals("/tvu/audiomixer/v1/sourcesinfo") && "PUT".equalsIgnoreCase(httpMethod)) {
			responseMap = updateSourcesRtilCodes(inputParams);
		} else if (resource.equals("/tvu/audiomixer/v1/rtilmeeting/token") && "GET".equalsIgnoreCase(httpMethod)) {
			responseMap = generateRtilMeetingToken(inputParams);
		} else if (resource.equals("/tvu/audiomixer/v2/sources") && "POST".equalsIgnoreCase(httpMethod)) {
			responseMap = saveSourcesV2(inputParams);
		} else if (resource.equals("/tvu/audiomixer/v2/sources") && "GET".equalsIgnoreCase(httpMethod)) {
			responseMap = getSourcesV2(inputParams);
		}

		header.put("Access-Control-Allow-Origin", "*");
		header.put("Access-Control-Allow-Methods", "GET,POST,PUT,OPTIONS");

		responseMap.put("headers", header);
		return responseMap;

	}

	private Map<String, Object> updateAudioMixerTaskStatus(Map<String, Object> inputParams) {
		System.out.println("Entering updateAudioMixerTaskStatus method...");
		String vmip = null;
		String status = null;
		AudioMixer audio = new AudioMixer();
		Map<String, Object> responseMap = new HashMap();
		try {
			Map pathParameters = new HashMap();
			String topicid = null;
			if (inputParams.get("pathParameters") != null) {
				pathParameters = (Map) inputParams.get("pathParameters");
			}
			Map queryStringParameters = new HashMap();
			if (inputParams.get("queryStringParameters") != null) {
				queryStringParameters = (Map) inputParams.get("queryStringParameters");
				if (queryStringParameters.get("status") != null) {
					status = (String) queryStringParameters.get("status");
				}
			}

			if (pathParameters.get("taskId") != null) {
				if (status != null) {
					if (status.equalsIgnoreCase("true") || status.equalsIgnoreCase("false")) {
						AudioMixerDao dao = new AudioMixerDaoImpl();
						audio = dao.retrieveAudioMixer((String) pathParameters.get("taskId"));
						if (audio != null) {
							if (!audio.isDeleted()) {
								if (status.equalsIgnoreCase("true")) {
									audio.setTaskstatus(1);
								} else {
									audio.setTaskstatus(0);
								}
								dao.saveAudioMixer(audio);

								// send the notifications
								if (status.equalsIgnoreCase("false")) {
									JSONObject message = new JSONObject();
									message.put("message", "The task has been disabled");
									sendNotificationsForTask((String) pathParameters.get("taskId"), message.toString());
								}
								// notifications ends

								JSONObject response = new JSONObject();
								response.put("message", TASK_STATUS_UPDATED);
								responseMap.put("statusCode", "200");
								responseMap.put("body", response.toString());
							} else {
								JSONObject response = new JSONObject();
								response.put("message", TASK_ALREADY_DELETED);
								responseMap.put("statusCode", "400");
								responseMap.put("body", response.toString());
							}

						} else {
							JSONObject response = new JSONObject();
							response.put("message", TASK_NOTFOUND);
							responseMap.put("statusCode", "404");
							responseMap.put("body", response.toString());
						}

					} else {
						JSONObject response = new JSONObject();
						response.put("message", TASK_STATUS_NOTPROVIDED);
						responseMap.put("statusCode", "400");
						responseMap.put("body", response.toString());
					}
				} else {
					JSONObject response = new JSONObject();
					response.put("message", TASK_STATUS_NOTPROVIDED);
					responseMap.put("statusCode", "400");
					responseMap.put("body", response.toString());
				}

			} else {
				JSONObject response = new JSONObject();
				response.put("message", TASKID_NOT_PROVIDED);
				responseMap.put("statusCode", "400");
				responseMap.put("body", response.toString());
			}
		} catch (Exception e) {
			responseMap.put("statusCode", "503");
			JSONObject response = new JSONObject();
			response.put("message", TASK_STATUS_UPDATE_ERROR);
			responseMap.put("body", response.toString());
			e.printStackTrace();
		}
		return responseMap;
	}

	private Map<String, Object> createAudioMixerTask(Map<String, Object> inputParams) {
		System.out.println("Entering createAudioMixerTask method...");
		String vmip = null;
		AudioMixer audio = new AudioMixer();
		Map<String, Object> responseMap = new HashMap();
		try {
			Map queryStringParameters = new HashMap();
			if (inputParams.get("queryStringParameters") != null) {
				queryStringParameters = (Map) inputParams.get("queryStringParameters");
				if (queryStringParameters.get("vmip") != null) {
					vmip = (String) queryStringParameters.get("vmip");
				}
			}
			if (vmip != null) {
				//// Add agora meeting info
				String rtilMeetingInfoJSON = "";
				if (inputParams.get("body") != null) {
					rtilMeetingInfoJSON = (String) inputParams.get("body");
					JSONObject json = new JSONObject(rtilMeetingInfoJSON);
					if (!validateRtilMeetingInfoJson(json)) {
						JSONObject response = new JSONObject();
						response.put("message", "Invalid JSON in the request");
						responseMap.put("statusCode", "400");
						responseMap.put("body", response.toString());
						return responseMap;
					}
					audio.setRtilMeetingInfoJSON(rtilMeetingInfoJSON);
				}
				/// Add agora meeting info END HERE

				AudioMixerDao dao = new AudioMixerDaoImpl();
				String taskId = UUID.randomUUID().toString();
				audio.setTaskId(taskId);
				audio.setVmip(vmip);
				audio.setTimestamp(System.currentTimeMillis());
				audio.setTaskstatus(1);
				dao.saveAudioMixer(audio);
				JSONObject response = new JSONObject();
				response.put("taskId", taskId);
				responseMap.put("statusCode", "200");
				responseMap.put("body", response.toString());

			} else {
				JSONObject response = new JSONObject();
				response.put("message", VM_ADDRESS_NOT_PROVIDED);
				responseMap.put("statusCode", "400");
				responseMap.put("body", response.toString());
			}
		} catch (Exception e) {
			responseMap.put("statusCode", "503");
			JSONObject response = new JSONObject();
			response.put("message", TASK_CREATION_ERROR);
			responseMap.put("body", response.toString());
			e.printStackTrace();
		}
		return responseMap;
	}

	private Map<String, Object> deleteAudioMixerTask(Map<String, Object> inputParams) {
		System.out.println("Entering deleteAudioMixerTask method...");
		String vmip = null;
		AudioMixer audio = new AudioMixer();
		Map<String, Object> responseMap = new HashMap();
		try {
			Map pathParameters = new HashMap();
			String topicid = null;
			if (inputParams.get("pathParameters") != null) {
				pathParameters = (Map) inputParams.get("pathParameters");
			}

			if (pathParameters.get("taskId") != null) {
				AudioMixerDao dao = new AudioMixerDaoImpl();
				audio = dao.retrieveAudioMixer((String) pathParameters.get("taskId"));
				if (audio != null) {
					if (!audio.isDeleted()) {
						audio.setDeleted(true);
						dao.saveAudioMixer(audio);
						JSONObject response = new JSONObject();
						response.put("message", TASK_DELETION_SUCCESS);
						responseMap.put("statusCode", "200");
						responseMap.put("body", response.toString());
					} else {
						JSONObject response = new JSONObject();
						response.put("message", TASK_ALREADY_DELETED);
						responseMap.put("statusCode", "200");
						responseMap.put("body", response.toString());
					}

				} else {
					JSONObject response = new JSONObject();
					response.put("message", TASK_NOTFOUND);
					responseMap.put("statusCode", "404");
					responseMap.put("body", response.toString());
				}
			} else {
				JSONObject response = new JSONObject();
				response.put("message", TASKID_NOT_PROVIDED);
				responseMap.put("statusCode", "400");
				responseMap.put("body", response.toString());
			}
		} catch (Exception e) {
			responseMap.put("statusCode", "503");
			JSONObject response = new JSONObject();
			response.put("message", TASK_DELETION_ERROR);
			responseMap.put("body", response.toString());
			e.printStackTrace();
		}
		return responseMap;
	}

	private Map<String, Object> saveGroups(Map<String, Object> inputParams) {
		System.out.println("Entering saveGroups method...");
		String taskid = null;
		AudioMixer audio = new AudioMixer();
		// audio.setTimestamp(System.currentTimeMillis());
		// audio.setVmip(vmip);

		Map<String, Object> responseMap = new HashMap();

		JSONObject jsonBody;
		try {
			String groupsJSON = (String) inputParams.get("body");

			jsonBody = new JSONObject(groupsJSON);

			Map queryStringParameters = new HashMap();
			if (inputParams.get("queryStringParameters") != null) {
				queryStringParameters = (Map) inputParams.get("queryStringParameters");
				if (queryStringParameters.get("taskid") != null) {
					taskid = (String) queryStringParameters.get("taskid");
				}
			}

			if (taskid != null) {
				System.out.println("taskid:::::::::: " + taskid);
				if (!validateRequestGroupsJson(jsonBody)) {
					JSONObject response = new JSONObject();
					response.put("message", INVALID_JSON);
					responseMap.put("statusCode", "400");
					responseMap.put("body", response.toString());
					return responseMap;
				}

				AudioMixerDao dao = new AudioMixerDaoImpl();
				audio = dao.retrieveAudioMixer(taskid);
				if (audio != null) {
					if (audio.isDeleted()) {
						responseMap.put("statusCode", "503");
						JSONObject response = new JSONObject();
						response.put("message", TASK_ALREADY_DELETED);
						responseMap.put("body", response.toString());
						return responseMap;
					}
					if (audio.getTaskstatus() != 1) {
						responseMap.put("statusCode", "503");
						JSONObject response = new JSONObject();
						response.put("message", TASK_IS_DISABLED);
						responseMap.put("body", response.toString());
						return responseMap;
					}
					/*
					 * String srcJson = null; if (audio.getSourcesJSON() != null) { srcJson =
					 * updateSourcesGainForGroups(groupsJSON, audio.getSourcesJSON());
					 * System.out.println(
					 * "updated sources::\n***************************************************\n" +
					 * srcJson);
					 * System.out.println("*************************************************");
					 * String postSourceURL = System.getenv("POST_SOURCES_URL"); postSourceURL =
					 * postSourceURL.replaceFirst("vmip", audio.getVmip());
					 * System.out.println("vmip:::::::::: " + audio.getVmip());
					 * 
					 * String returnCode = saveProdcerSources(postSourceURL, srcJson); if
					 * ("0x0".equals(returnCode)) { audio.setGroupsJSON(groupsJSON);
					 * dao.saveAudioMixer(audio); JSONObject response = new JSONObject();
					 * response.put("message", GROUPS_CREATION_SUCCESS);
					 * response.put("updatedSourcesGains", new JSONObject(srcJson));
					 * responseMap.put("statusCode", "200"); responseMap.put("body",
					 * response.toString()); } else if ("0x1".equals(returnCode)) { JSONObject
					 * response = new JSONObject(); response.put("message",
					 * "AFV is Enable,all operation for audio mixing will been refused!");
					 * response.put("updatedSourcesGains", new JSONObject(srcJson));
					 * responseMap.put("statusCode", "200"); responseMap.put("body",
					 * response.toString()); } else { responseMap.put("statusCode", "503");
					 * JSONObject response = new JSONObject(); response.put("message",
					 * UPDATED_GAINS_ERROR); responseMap.put("body", response.toString()); }
					 * 
					 * } else { audio.setGroupsJSON(groupsJSON); dao.saveAudioMixer(audio);
					 * JSONObject response = new JSONObject(); response.put("message",
					 * GROUPS_CREATION_SUCCESS); responseMap.put("statusCode", "200");
					 * responseMap.put("body", response.toString()); }
					 */
					audio.setGroupsJSON(groupsJSON);
					dao.saveAudioMixer(audio);
					JSONObject response = new JSONObject();
					response.put("message", GROUPS_CREATION_SUCCESS);
					responseMap.put("statusCode", "200");
					responseMap.put("body", response.toString());

				} else {
					responseMap.put("statusCode", "404");
					JSONObject response = new JSONObject();
					response.put("message", TASK_NOTFOUND);
					responseMap.put("body", response.toString());
				}
			} else {
				JSONObject response = new JSONObject();
				response.put("message", TASKID_NOT_PROVIDED);
				responseMap.put("statusCode", "400");
				responseMap.put("body", response.toString());
			}
		} catch (Exception e) {
			responseMap.put("statusCode", "503");
			JSONObject response = new JSONObject();
			response.put("message", GROUPS_CREATION_ERROR);
			responseMap.put("body", response.toString());
			e.printStackTrace();
		}
		return responseMap;
	}

	private Map<String, Object> getGroups(Map<String, Object> inputParams) {
		System.out.println("Entering getGroups method...");
		String taskid = null;
		AudioMixer audio = new AudioMixer();
		Map<String, Object> responseMap = new HashMap();

		JSONObject jsonBody;
		try {
			Map queryStringParameters = new HashMap();
			if (inputParams.get("queryStringParameters") != null) {
				queryStringParameters = (Map) inputParams.get("queryStringParameters");
				if (queryStringParameters.get("taskid") != null) {
					taskid = (String) queryStringParameters.get("taskid");
				}
			}

			if (taskid != null) {
				System.out.println("taskid:::::::::: " + taskid);
				AudioMixerDao dao = new AudioMixerDaoImpl();
				audio = dao.retrieveAudioMixer(taskid);
				if (audio != null) {
					if (audio.isDeleted()) {
						responseMap.put("statusCode", "503");
						JSONObject response = new JSONObject();
						response.put("message", TASK_ALREADY_DELETED);
						responseMap.put("body", response.toString());
						return responseMap;
					}
					if (audio.getTaskstatus() != 1) {
						responseMap.put("statusCode", "503");
						JSONObject response = new JSONObject();
						response.put("message", TASK_IS_DISABLED);
						responseMap.put("body", response.toString());
						return responseMap;
					}
					String groupsJSON = audio.getGroupsJSON();
					JSONObject response = new JSONObject();
					if (groupsJSON != null) {
						response = new JSONObject(groupsJSON);
					}
					responseMap.put("statusCode", "200");
					responseMap.put("body", response.toString());
				} else {
					responseMap.put("statusCode", "404");
					JSONObject response = new JSONObject();
					response.put("message", TASK_NOTFOUND);
					responseMap.put("body", response.toString());
				}
			} else {
				JSONObject response = new JSONObject();
				response.put("message", TASKID_NOT_PROVIDED);
				responseMap.put("statusCode", "400");
				responseMap.put("body", response.toString());
			}
		} catch (Exception e) {
			responseMap.put("statusCode", "503");
			JSONObject response = new JSONObject();
			response.put("message", GET_GROUPS_ERROR);
			responseMap.put("body", response.toString());
			e.printStackTrace();
		}
		return responseMap;
	}

	private Map<String, Object> getSourcesV2(Map<String, Object> inputParams) {
		long timestamp = System.currentTimeMillis();
		System.out.println("@@Entering getSourcesV2 method...." + timestamp);

		String taskid = null;
		String screenId = null;
		Map<String, Object> responseMap = new HashMap();

		JSONObject responseBody;
		try {
			JSONObject returnJson = new JSONObject();

			Map queryStringParameters = new HashMap();
			if (inputParams.get("queryStringParameters") != null) {
				queryStringParameters = (Map) inputParams.get("queryStringParameters");
				if (queryStringParameters.get("taskid") != null) {
					taskid = (String) queryStringParameters.get("taskid");
				}
				if (queryStringParameters.get("screenid") != null) {
					screenId = (String) queryStringParameters.get("screenid");
				}
			}
			if (taskid != null) {
				System.out.println("@@ getSources:: taskid:::::::::: " + taskid);
				AudioMixerDao dao = new AudioMixerDaoImpl();

				System.out.println();
				timestamp = System.currentTimeMillis();
				AudioMixer audiomixer = dao.retrieveAudioMixer(taskid);
				System.out.println(
						"@@ getSources:: time taken in DB operation " + (System.currentTimeMillis() - timestamp));

				if (audiomixer != null) {
					if (audiomixer.isDeleted()) {
						responseMap.put("statusCode", "503");
						JSONObject response = new JSONObject();
						response.put("message", TASK_ALREADY_DELETED);
						responseMap.put("body", response.toString());
						return responseMap;
					}
					if (audiomixer.getTaskstatus() != 1) {
						responseMap.put("statusCode", "503");
						JSONObject response = new JSONObject();
						response.put("message", TASK_IS_DISABLED);
						responseMap.put("body", response.toString());
						return responseMap;
					}
					if (screenId == null || screenId.equals("")) {
						responseMap.put("statusCode", "400");
						JSONObject response = new JSONObject();
						response.put("message", SCREENID_NOT_PROVIDED);
						responseMap.put("body", response.toString());
						return responseMap;
					}
					String rtilCodes = audiomixer.getSourcesRtilJSON();

					String getSourceURL = System.getenv("GET_SOURCES_URL");
					getSourceURL = getSourceURL.replaceFirst("vmip", audiomixer.getVmip());
					System.out.println("vmip:::::::::: " + audiomixer.getVmip());
					JSONObject dbSources = null;
					JSONObject rtilCodesJson = null;
					if (audiomixer.getSourcesJSON() != null) {
						dbSources = new JSONObject(audiomixer.getSourcesJSON());
					}
					if (rtilCodes != null) {
						rtilCodesJson = new JSONObject(rtilCodes);
					}
					timestamp = System.currentTimeMillis();
					returnJson = getProducerSourcesV2(getSourceURL, rtilCodesJson, dbSources);

					// save in db if nothing is there in the DB
					/*
					 * if (dbSources == null) { audiomixer.setSourcesJSON(returnJson.toString());
					 * dao.saveAudioMixer(audiomixer); }
					 */

					String dbScreenSettings = audiomixer.getScreenSettings();
					if (dbScreenSettings != null) {
						JSONObject userScreenSettings = getUserScreenSettings(dbScreenSettings, screenId);
						if (userScreenSettings != null) {
							returnJson.put("userScreenSettings", userScreenSettings);
						}
					} else {
						returnJson.put("userScreenSettings", new JSONObject());
					}

				} else {
					responseMap.put("statusCode", "404");
					// responseBody = new JSONObject();
					JSONObject response = new JSONObject();
					response.put("message", TASK_NOTFOUND);
					responseMap.put("body", response.toString());
					return responseMap;
				}
				responseMap.put("statusCode", "200");

				System.out.println("The data send to UI::\n");
				System.out.println(returnJson.toString());

				responseMap.put("body", returnJson.toString());
			} else {
				responseMap.put("statusCode", "400");
				// responseBody = new JSONObject();
				JSONObject response = new JSONObject();
				response.put("message", TASKID_NOT_PROVIDED);
				responseMap.put("body", response.toString());
			}
		} catch (Exception e) {
			responseMap.put("statusCode", "503");
			JSONObject response = new JSONObject();
			response.put("message", GET_SOURCES_ERROR);
			responseMap.put("body", response.toString());
			e.printStackTrace();
		}
		return responseMap;
	}

	private Map<String, Object> getSources(Map<String, Object> inputParams) {
		long timestamp = System.currentTimeMillis();
		System.out.println("@@Entering getSources method...." + timestamp);

		String taskid = null;
		Map<String, Object> responseMap = new HashMap();

		JSONObject responseBody;
		try {
			String returnJson = "";

			Map queryStringParameters = new HashMap();
			if (inputParams.get("queryStringParameters") != null) {
				queryStringParameters = (Map) inputParams.get("queryStringParameters");
				if (queryStringParameters.get("taskid") != null) {
					taskid = (String) queryStringParameters.get("taskid");
				}
			}
			if (taskid != null) {
				System.out.println("@@ getSources:: taskid:::::::::: " + taskid);
				AudioMixerDao dao = new AudioMixerDaoImpl();

				System.out.println();
				timestamp = System.currentTimeMillis();
				AudioMixer audiomixer = dao.retrieveAudioMixer(taskid);
				System.out.println(
						"@@ getSources:: time taken in DB operation " + (System.currentTimeMillis() - timestamp));

				if (audiomixer != null) {
					if (audiomixer.isDeleted()) {
						responseMap.put("statusCode", "503");
						JSONObject response = new JSONObject();
						response.put("message", TASK_ALREADY_DELETED);
						responseMap.put("body", response.toString());
						return responseMap;
					}
					if (audiomixer.getTaskstatus() != 1) {
						responseMap.put("statusCode", "503");
						JSONObject response = new JSONObject();
						response.put("message", TASK_IS_DISABLED);
						responseMap.put("body", response.toString());
						return responseMap;
					}
					String rtilCodes = audiomixer.getSourcesRtilJSON();

					String getSourceURL = System.getenv("GET_SOURCES_URL");
					getSourceURL = getSourceURL.replaceFirst("vmip", audiomixer.getVmip());
					System.out.println("vmip:::::::::: " + audiomixer.getVmip());
					JSONObject dbSources = null;
					JSONObject rtilCodesJson = null;
					if (audiomixer.getSourcesJSON() != null) {
						dbSources = new JSONObject(audiomixer.getSourcesJSON());
					}
					if (rtilCodes != null) {
						rtilCodesJson = new JSONObject(rtilCodes);
					}
					timestamp = System.currentTimeMillis();
					returnJson = getProducerSources(getSourceURL, rtilCodesJson, dbSources);
					System.out.println("@@ getSources:: total time taken in core api response processing "
							+ (System.currentTimeMillis() - timestamp));
					/*
					 * else { returnJson = getProdcerSources(getSourceURL, dbSources); }
					 */
					/*
					 * JSONArray sources = (JSONArray) srcJson.get("Sources"); int size =
					 * sources.length(); for (int i = 0; i < size; i++) { JSONObject source =
					 * sources.getJSONObject(i); source.put("SourceName", "Source " + (i + 1)); }
					 */
					// returnJson = srcJson.toString();

				} else {
					responseMap.put("statusCode", "404");
					// responseBody = new JSONObject();
					JSONObject response = new JSONObject();
					response.put("message", TASK_NOTFOUND);
					responseMap.put("body", response.toString());
					return responseMap;
				}
				responseMap.put("statusCode", "200");

				System.out.println("The data send to UI::\n");
				System.out.println(returnJson.toString());

				responseMap.put("body", returnJson);
			} else {
				responseMap.put("statusCode", "400");
				// responseBody = new JSONObject();
				JSONObject response = new JSONObject();
				response.put("message", TASKID_NOT_PROVIDED);
				responseMap.put("body", response.toString());
			}
		} catch (Exception e) {
			responseMap.put("statusCode", "503");
			JSONObject response = new JSONObject();
			response.put("message", GET_SOURCES_ERROR);
			responseMap.put("body", response.toString());
			e.printStackTrace();
		}
		return responseMap;
	}

	private Map<String, Object> saveSources(Map<String, Object> inputParams) {
		System.out.println("@@ Entering saveSources method...");
		long timestamp = System.currentTimeMillis();
		String taskid = null;
		AudioMixer audio = new AudioMixer();
		// audio.setTimestamp(System.currentTimeMillis());
		// audio.setVmip(vmip);

		Map<String, Object> responseMap = new HashMap();
		AudioMixerDao dao = new AudioMixerDaoImpl();
		JSONObject jsonBody;
		try {
			String sourcesJSON = (String) inputParams.get("body");

			System.out.println("@@ saveSources :: Request data from UI:::::::::::::::\n");
			System.out.println(sourcesJSON);

			jsonBody = new JSONObject(sourcesJSON);
			// jsonBody.put("Timestamp", System.currentTimeMillis());

			Map queryStringParameters = new HashMap();
			if (inputParams.get("queryStringParameters") != null) {
				queryStringParameters = (Map) inputParams.get("queryStringParameters");
				if (queryStringParameters.get("taskid") != null) {
					taskid = (String) queryStringParameters.get("taskid");
				}
			}

			if (taskid != null) {
				System.out.println("taskid:::::::::: " + taskid);
				if (!validateRequestSourcesJson(jsonBody)) {
					JSONObject response = new JSONObject();
					response.put("message", INVALID_JSON);
					responseMap.put("statusCode", "400");
					responseMap.put("body", response.toString());
					return responseMap;
				}

				audio = dao.retrieveAudioMixer(taskid);
				System.out.println("@@ saveSources:: Time taken in taking audiomixer task from DB "
						+ (System.currentTimeMillis() - timestamp));
				if (audio == null) {
					JSONObject response = new JSONObject();
					response.put("message", TASK_NOTFOUND);
					responseMap.put("statusCode", "404");
					responseMap.put("body", response.toString());
					return responseMap;
				}
				if (audio.isDeleted()) {
					responseMap.put("statusCode", "503");
					JSONObject response = new JSONObject();
					response.put("message", TASK_ALREADY_DELETED);
					responseMap.put("body", response.toString());
					return responseMap;
				}
				if (audio.getTaskstatus() != 1) {
					responseMap.put("statusCode", "503");
					JSONObject response = new JSONObject();
					response.put("message", TASK_IS_DISABLED);
					responseMap.put("body", response.toString());
					return responseMap;
				}
				/*
				 * boolean afv = false; try { afv = jsonBody.getBoolean("AFV"); } catch
				 * (Exception e) {
				 * 
				 * } if (afv) { JSONObject response = new JSONObject(); response.put("message",
				 * AFV_IS_ON); responseMap.put("statusCode", "404"); responseMap.put("body",
				 * response.toString()); return responseMap; }
				 */
				String postSourceURL = System.getenv("POST_SOURCES_URL");
				postSourceURL = postSourceURL.replaceFirst("vmip", audio.getVmip());
				System.out.println("vmip:::::::::: " + audio.getVmip());
				/*
				 * if (isSoloChannelAvailable(jsonBody)) { // update for Solo channels
				 * updateForSoloChannels(jsonBody); } else { // update the gains for the muted
				 * channels updateMutedChannels(jsonBody); }
				 */
				// update the gains for the muted channels and convert the gains for other
				// channel as per formula
				timestamp = System.currentTimeMillis();
				updateSourcesForProducer(jsonBody);
				System.out.println("@@ saveSources:: Time taken to updateSourcesForProducer "
						+ (System.currentTimeMillis() - timestamp));
				System.out.println("json sent to the producer:::::\n" + jsonBody);
				String returnCode = saveProducerSources(postSourceURL, jsonBody.toString());
				if ("0x0".equals(returnCode)) {
					audio.setSourcesJSON(sourcesJSON);

					timestamp = System.currentTimeMillis();
					System.out.println("@@ saveSources::Saving data in DB...." + timestamp);
					dao.saveAudioMixer(audio);
					System.out.println("@@ saveSources:: Time taken to save sources in the DB:: "
							+ (System.currentTimeMillis() - timestamp));
					JSONObject response = new JSONObject();
					response.put("message", SOURCES_CREATION_SUCCESS);
					responseMap.put("statusCode", "200");
					responseMap.put("body", response.toString());

				} else if ("0x1".equals(returnCode)) {
					JSONObject response = new JSONObject();
					response.put("message", "AFV is Enable,all operation for audio mixing will been refused!");
					responseMap.put("statusCode", "200");
					responseMap.put("body", response.toString());

				} else {
					throw new Exception();
				}

			} else {
				JSONObject response = new JSONObject();
				response.put("message", TASKID_NOT_PROVIDED);
				responseMap.put("statusCode", "400");
				responseMap.put("body", response.toString());
			}
		} catch (Exception e) {
			responseMap.put("statusCode", "503");
			JSONObject response = new JSONObject();
			response.put("message", SOURCES_CREATION_ERROR);
			responseMap.put("body", response.toString());
			e.printStackTrace();
		}
		return responseMap;
	}

	private Map<String, Object> saveSourcesV2(Map<String, Object> inputParams) {
		System.out.println("@@ Entering saveSourcesV2 method...");
		long timestamp = System.currentTimeMillis();
		String taskid = null;
		AudioMixer audio = new AudioMixer();
		// audio.setTimestamp(System.currentTimeMillis());
		// audio.setVmip(vmip);

		Map<String, Object> responseMap = new HashMap();
		AudioMixerDao dao = new AudioMixerDaoImpl();
		JSONObject jsonBody;
		try {
			String sourcesJSON = (String) inputParams.get("body");

			System.out.println("@@ saveSourcesV2 :: Request data from UI:::::::::::::::\n");
			System.out.println(sourcesJSON);

			jsonBody = new JSONObject(sourcesJSON);
			String screenId = null;
			Map queryStringParameters = new HashMap();
			if (inputParams.get("queryStringParameters") != null) {
				queryStringParameters = (Map) inputParams.get("queryStringParameters");
				if (queryStringParameters.get("taskid") != null) {
					taskid = (String) queryStringParameters.get("taskid");
				}
				if (queryStringParameters.get("screenid") != null) {
					screenId = (String) queryStringParameters.get("screenid");
				}
			}

			if (taskid != null) {
				System.out.println("taskid:::::::::: " + taskid);
				/*
				 * if (!validateRequestSourcesJson(jsonBody)) { JSONObject response = new
				 * JSONObject(); response.put("message", INVALID_JSON);
				 * responseMap.put("statusCode", "400"); responseMap.put("body",
				 * response.toString()); return responseMap; }
				 */

				audio = dao.retrieveAudioMixer(taskid);
				System.out.println("@@ saveSourcesV2:: Time taken in taking audiomixer task from DB "
						+ (System.currentTimeMillis() - timestamp));
				if (audio == null) {
					JSONObject response = new JSONObject();
					response.put("message", TASK_NOTFOUND);
					responseMap.put("statusCode", "404");
					responseMap.put("body", response.toString());
					return responseMap;
				}
				if (audio.isDeleted()) {
					responseMap.put("statusCode", "503");
					JSONObject response = new JSONObject();
					response.put("message", TASK_ALREADY_DELETED);
					responseMap.put("body", response.toString());
					return responseMap;
				}
				if (audio.getTaskstatus() != 1) {
					responseMap.put("statusCode", "503");
					JSONObject response = new JSONObject();
					response.put("message", TASK_IS_DISABLED);
					responseMap.put("body", response.toString());
					return responseMap;
				}

				if (screenId == null || screenId.equals("")) {
					responseMap.put("statusCode", "400");
					JSONObject response = new JSONObject();
					response.put("message", SCREENID_NOT_PROVIDED);
					responseMap.put("body", response.toString());
					return responseMap;
				}
				if (!jsonBody.isNull("userScreenSettings")) {
					JSONObject jsonReqUserScreenSettings = jsonBody.getJSONObject("userScreenSettings");
					jsonReqUserScreenSettings.put("screenId", screenId);
					JSONObject jsonUpdatedScreenSettings = updateScreenSettings(audio.getScreenSettings(),
							jsonReqUserScreenSettings);
					audio.setScreenSettings(jsonUpdatedScreenSettings.toString());
					// dao.saveAudioMixer(audio);
				}

				String postSourceURL = System.getenv("POST_SOURCES_URL");
				postSourceURL = postSourceURL.replaceFirst("vmip", audio.getVmip());
				System.out.println("vmip:::::::::: " + audio.getVmip());

				timestamp = System.currentTimeMillis();
				if (!jsonBody.isNull("Sources") || !jsonBody.isNull("Master")) {
					updateSourcesForProducerV2(jsonBody);
				}
				if (!jsonBody.isNull("Sources") || !jsonBody.isNull("AFV") || !jsonBody.isNull("Master")) {
					String returnCode = saveProducerSources(postSourceURL, jsonBody.toString());
					if ("0x0".equals(returnCode)) {
						JSONObject response = new JSONObject();
						response.put("message", SOURCES_CREATION_SUCCESS);
						responseMap.put("statusCode", "200");
						responseMap.put("body", response.toString());

					} else if ("0x1".equals(returnCode)) {
						JSONObject response = new JSONObject();
						response.put("message", "AFV is Enable,all operation for audio mixing will been refused!");
						responseMap.put("statusCode", "200");
						responseMap.put("body", response.toString());

					} else {
						throw new Exception();
					}
				} else {
					JSONObject response = new JSONObject();
					response.put("message", "User settings saved successfully");
					responseMap.put("statusCode", "200");
					responseMap.put("body", response.toString());
				}

				JSONObject dbSources = null;
				String strDBSrc = audio.getSourcesJSON();
				if (strDBSrc != null) {
					dbSources = new JSONObject(strDBSrc);

				}
				dbSources = updateDBSources(dbSources, jsonBody);
				audio.setSourcesJSON(dbSources.toString());
				dao.saveAudioMixer(audio);

			} else {
				JSONObject response = new JSONObject();
				response.put("message", TASKID_NOT_PROVIDED);
				responseMap.put("statusCode", "400");
				responseMap.put("body", response.toString());
			}
		} catch (Exception e) {
			responseMap.put("statusCode", "503");
			JSONObject response = new JSONObject();
			response.put("message", SOURCES_CREATION_ERROR);
			responseMap.put("body", response.toString());
			e.printStackTrace();
		}
		return responseMap;
	}

	private String saveProducerSources(String saveSourcesURL, String postBody) throws Exception {

		// String status = "xxx";

		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpPost postRequest = null;
		HttpResponse response = null;
		InputStream inputSource = null;
		String postResponse = "";
		JSONObject jsonResponse = null;
		JSONObject jsonReturnValue = null;
		String returnCode = "xxx";

		try {
			postRequest = new HttpPost(saveSourcesURL);
			StringEntity entity = new StringEntity(postBody, ContentType.APPLICATION_JSON);
			postRequest.setEntity(entity);
			long timestamp = System.currentTimeMillis();
			System.out.println("@@ saveSources::Request sent to Core APIs:::" + timestamp);
			System.out.println("@@ Data sent to Core APIs:::\n" + postBody);
			long start = System.currentTimeMillis();
			response = httpClient.execute(postRequest);
			long end = System.currentTimeMillis();
			System.out.println("@@ saveSources::Time taken by Producer API to save the data::" + (end - start)
					+ " :::: " + timestamp);

			inputSource = response.getEntity().getContent();

			postResponse = getServerResponse(inputSource);

			System.out.println("@@ saveSources::Returned response:::\n" + postResponse);
			jsonResponse = new JSONObject(postResponse);
			returnCode = jsonResponse.getString("ErrorCode");

			/*
			 * if (!"0x0".equals(returnCode) || !"0x1".equals(returnCode)) { status = false;
			 * }
			 */

		} catch (Exception e) {
			e.printStackTrace();
			// status = false;
		}

		return returnCode;
	}

	/*
	 * private String getProdcerSources(String getSourcesURL, JSONObject dbSources)
	 * throws Exception {
	 * 
	 * HttpClient httpClient = HttpClientBuilder.create().build(); HttpGet
	 * getRequest = null; HttpResponse response = null; InputStream inputSource =
	 * null; String getResourcesResponse = ""; JSONObject jsonResponse = null;
	 * JSONObject jsonReturnValue = null;
	 * 
	 * try {
	 * 
	 * getRequest = new HttpGet(getSourcesURL); long start =
	 * System.currentTimeMillis(); response = httpClient.execute(getRequest); long
	 * end = System.currentTimeMillis();
	 * System.out.println("Time taken to get the data from Producer API:" + (end -
	 * start)); inputSource = response.getEntity().getContent();
	 * 
	 * getResourcesResponse = getServerResponse(inputSource); jsonResponse = new
	 * JSONObject(getResourcesResponse); jsonReturnValue = new JSONObject((String)
	 * jsonResponse.get("ReturnValue"));
	 * 
	 * } catch (Exception e) { e.printStackTrace(); throw e; }
	 * System.out.println("The data fetched from Producer APIs::\n");
	 * System.out.println(jsonReturnValue); // prepare the gains for Master
	 * JSONObject master = (JSONObject) jsonReturnValue.get("Master"); JSONArray
	 * masterChannels = (JSONArray) master.get("Channels"); int mSize =
	 * masterChannels.length(); // JSONArray newArrMasterChannels = new JSONArray();
	 * 
	 * for (int j = 0; j < mSize; j++) { JSONObject newChannel =
	 * masterChannels.getJSONObject(j); int gain = newChannel.getInt("Gain");
	 * newChannel.put("Gain", (int) convertGainToSliderPosition(gain));
	 * newChannel.put("isMute", newChannel.getBoolean("Mute"));
	 * newChannel.remove("Mute"); //// add saved headphone value if (dbSources !=
	 * null) { JSONObject savedChannel = getMasterSavedChannel(dbSources,
	 * newChannel.getInt("ID")); if (savedChannel != null) {
	 * newChannel.put("HeadPhone", savedChannel.getBoolean("HeadPhone")); } } ////
	 * add saved headphone value ENDS } //////////////////////////////
	 * 
	 * JSONArray sources = (JSONArray) jsonReturnValue.get("Sources");
	 * 
	 * int size = sources.length(); for (int i = 0; i < size; i++) { JSONObject
	 * source = sources.getJSONObject(i); JSONArray channels = (JSONArray)
	 * source.get("Channels"); int csize = channels.length();
	 * 
	 * for (int j = 0; j < csize; j++) { JSONObject newChannel =
	 * channels.getJSONObject(j); int gain = newChannel.getInt("Gain");
	 * newChannel.put("Gain", (int) convertGainToSliderPosition(gain));
	 * newChannel.put("isMute", newChannel.getBoolean("Mute"));
	 * newChannel.remove("Mute"); //// add saved headphone value if (dbSources !=
	 * null) { JSONObject savedChannel = getSavedChannel(dbSources,
	 * source.getString("ID"), newChannel.getInt("ID")); if (savedChannel != null) {
	 * newChannel.put("HeadPhone", savedChannel.getBoolean("HeadPhone")); } } ////
	 * add saved headphone value ENDS } source.put("SourceName", "Source " + (i +
	 * 1)); } return jsonReturnValue.toString(); }
	 */
	private String getServerResponse(InputStream inputSource) {
		StringBuffer buffer = new StringBuffer();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputSource));
			String line = null;

			while ((line = reader.readLine()) != null) {
				buffer.append(line);
			}
		} catch (Exception e) {
			System.out.println("Exception in getServerResponse():" + e.toString());
		}
		return buffer.toString();

	}

	private String getProducerSources(String getSourcesURL, JSONObject rtilCodesJson, JSONObject dbSources)
			throws Exception {
		// Sample feed url
		/// https://api.ap.org/media/v/content/feed?q=type:text&apikey={apikey}
		long timestamp = System.currentTimeMillis();
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpGet getRequest = null;
		HttpResponse response = null;
		InputStream inputSource = null;
		String getResourcesResponse = "";
		JSONObject jsonResponse = null;
		JSONObject jsonReturnValue = null;

		try {
			getRequest = new HttpGet(getSourcesURL);
			System.out.println("@@ getSources:: request sent to Core APIs::" + timestamp);
			response = httpClient.execute(getRequest);
			inputSource = response.getEntity().getContent();
			System.out.println("@@ getSources:: Time taken by core api::" + (timestamp - System.currentTimeMillis())
					+ " ::::: " + timestamp);
			getResourcesResponse = getServerResponse(inputSource);

			jsonResponse = new JSONObject(getResourcesResponse);
			jsonReturnValue = new JSONObject((String) jsonResponse.get("ReturnValue"));
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

		System.out.println("The data fetched from Producer APIs::\n");
		System.out.println(jsonReturnValue);
		///// prepare master source /////
		JSONObject master = (JSONObject) jsonReturnValue.get("Master");

		// code for handling SourceMasterGain
		if (!master.isNull("SourceMasterGain")) {
			master.put("SourceMasterGain", convertGainToSliderPosition(master.getInt("SourceMasterGain")));
		}
		// code for handling SourceMasterGain ENDS HERE

		JSONArray masterChannels = (JSONArray) master.get("Channels");
		int mSize = masterChannels.length();

		for (int j = 0; j < mSize; j++) {
			JSONObject masterChannel = masterChannels.getJSONObject(j);
			int gain = masterChannel.getInt("Gain");
			masterChannel.put("Gain", (int) convertGainToSliderPosition(gain));
			masterChannel.put("isMute", masterChannel.getBoolean("Mute"));
			masterChannel.remove("Mute");
			// handling for AdvanceGain parameter
			try {
				if (!masterChannel.isNull("AdvanceGain")) {
					JSONArray advanceGainValues = masterChannel.getJSONArray("AdvanceGain");
					// System.out.println("FROM PRODUCER-->Master AdvanceGain value from producer
					// API::" + advanceGainValues.toString());
					JSONArray updatedAdvanceGainValues = new JSONArray();
					int agSize = advanceGainValues.length();
					for (int x = 0; x < agSize; x++) {
						updatedAdvanceGainValues.put((int) convertGainToSliderPosition(advanceGainValues.getInt(x)));
					}
					// System.out.println("FROM PRODUCER-->Master AdvanceGain value after
					// conversion::" + updatedAdvanceGainValues.toString());
					masterChannel.put("AdvanceGain", updatedAdvanceGainValues);
				}
			} catch (Exception e) {
				System.out.println("Unable to process the AdvanceGain values while getting the data.");
			}
			// handling for AdvanceGain value ENDS HERE

			//// add saved headphone value
			if (dbSources != null) {
				JSONObject savedChannel = getMasterSavedChannel(dbSources, masterChannel.getInt("ID"));
				if (savedChannel != null) {
					if (savedChannel.has("HeadPhone")) {
						masterChannel.put("HeadPhone", savedChannel.getBoolean("HeadPhone"));
					}
				}
			}
			//// add saved headphone value ENDS
		}
		/////////

		JSONArray sources = (JSONArray) jsonReturnValue.get("Sources");

		int size = sources.length();
		for (int i = 0; i < size; i++) {
			JSONObject source = sources.getJSONObject(i);

			// Include rtilCode for the source if available in the DB
			if (rtilCodesJson != null) {
				JSONObject rtilinfo = getSourceRTILInfo(rtilCodesJson, source.getString("ID"));
				if (rtilinfo != null) {
					if (!rtilinfo.isNull("rtilCode")) {
						source.put("rtilCode", rtilinfo.getString("rtilCode"));
					}
					if (!rtilinfo.isNull("partyCode")) {
						source.put("partyCode", rtilinfo.getString("partyCode"));
					}
					if (!rtilinfo.isNull("sourceName")) {
						source.put("sourceName", rtilinfo.getString("sourceName"));
					}
					if (!rtilinfo.isNull("sourceType")) {
						source.put("sourceType", rtilinfo.getInt("sourceType"));
					}
				}
				/*
				 * String rtilCode = getSourceRTILCode(rtilCodesJson, source.getString("ID"));
				 * if (rtilCode != null) { source.put("rtilCode", rtilCode); }
				 */
			}
			// Inclusion of rtilCode for the source ENDS HERE

			// Code to handle "SourceMasterGain" coming in the source
			if (!source.isNull("SourceMasterGain")) {
				source.put("SourceMasterGain", convertGainToSliderPosition(source.getInt("SourceMasterGain")));
			}
			// Code to handle "SourceMasterGain" coming in the source ENDs HERE

			// Code to add selected pair
			if (dbSources != null) {
				JSONObject savedSource = getSavedSource(dbSources, source.getString("ID"));
				if (savedSource != null) {
					if (!savedSource.isNull("PairSelected")) {
						source.put("PairSelected", savedSource.getInt("PairSelected"));
					}
				}
			}
			// Code to add selected pair ENDs HERE

			JSONArray channels = (JSONArray) source.get("Channels");
			int csize = channels.length();

			JSONArray newArrChannels = new JSONArray();

			for (int j = 0; j < csize; j++) {
				JSONObject channel = channels.getJSONObject(j);
				int gain = channel.getInt("Gain");
				channel.put("Gain", (int) convertGainToSliderPosition(gain));
				channel.put("isMute", channel.getBoolean("Mute"));
				channel.remove("Mute");
				// handling for AdvanceGain parameter
				try {
					if (!channel.isNull("AdvanceGain")) {
						JSONArray advanceGainValues = channel.getJSONArray("AdvanceGain");
						// System.out.println("FROM PRODUCER-->AdvanceGain value from producer api::" +
						// advanceGainValues.toString());

						JSONArray updatedAdvanceGainValues = new JSONArray();
						int agSize = advanceGainValues.length();
						for (int x = 0; x < agSize; x++) {
							updatedAdvanceGainValues
									.put((int) convertGainToSliderPosition(advanceGainValues.getInt(x)));
						}
						// System.out.println("FROM PRODUCER-->AdvanceGain value after conversion::" +
						// updatedAdvanceGainValues.toString());
						channel.put("AdvanceGain", updatedAdvanceGainValues);
					}
				} catch (Exception e) {
					System.out.println("Unable to process the AdvanceGain values while getting the data.");
				}
				// handling for AdvanceGain value ENDS HERE

				//// add saved headphone values

				if (dbSources != null) {
					JSONObject savedChannel = getSavedChannel(dbSources, source.getString("ID"), channel.getInt("ID"));
					if (savedChannel != null) {
						if (savedChannel.has("HeadPhone")) {
							channel.put("HeadPhone", savedChannel.getBoolean("HeadPhone"));
						}

						if (!savedChannel.isNull("advanceGainSource")) {
							channel.put("advanceGainSource", savedChannel.getJSONArray("advanceGainSource"));
						}
						if (!savedChannel.isNull("faderTypeSource")) {
							channel.put("faderTypeSource", savedChannel.getJSONArray("faderTypeSource"));
						}
						if (!savedChannel.isNull("webRTCGain")) {
							channel.put("webRTCGain", savedChannel.getFloat("webRTCGain"));
						}
					}
				} /*
					 * else { channel.put("FaderType", new JSONArray()); }
					 */
				//// add saved headphone and FaderType values ENDS
			}
			source.put("SourceName", "Source " + (i + 1));
		}
		try {
			// boolean rps = rtilCodesJson.getBoolean("rps");
			// jsonReturnValue.put("rps", rps);

			if (!rtilCodesJson.isNull("rps")) {
				jsonReturnValue.put("rps", rtilCodesJson.getBoolean("rps"));
			}

			if (!rtilCodesJson.isNull("productionId")) {
				jsonReturnValue.put("productionId", rtilCodesJson.getLong("productionId"));
			}

			if (!rtilCodesJson.isNull("productionName")) {
				jsonReturnValue.put("productionName", rtilCodesJson.getString("productionName"));
			}
			if (!rtilCodesJson.isNull("partyCodes")) {
				jsonReturnValue.put("partyCodes", rtilCodesJson.getJSONArray("partyCodes"));
			}
			/// added selected output pair
			if (dbSources != null) {
				if (dbSources.has("SelectedOutputPair")) {
					jsonReturnValue.put("SelectedOutputPair", dbSources.getInt("SelectedOutputPair"));
				}
			}

		} catch (Exception e) {
			System.out.println(e.toString());
		}
		return jsonReturnValue.toString();
	}

	private JSONObject getProducerSourcesV2(String getSourcesURL, JSONObject rtilCodesJson, JSONObject dbSources)
			throws Exception {
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpGet getRequest = null;
		HttpResponse response = null;
		InputStream inputSource = null;
		String getResourcesResponse = "";
		JSONObject jsonResponse = null;
		JSONObject jsonReturnValue = new JSONObject();

		try {
			getRequest = new HttpGet(getSourcesURL);
			response = httpClient.execute(getRequest);
			inputSource = response.getEntity().getContent();
			getResourcesResponse = getServerResponse(inputSource);

			jsonResponse = new JSONObject(getResourcesResponse);
			jsonReturnValue = new JSONObject((String) jsonResponse.get("ReturnValue"));
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

		System.out.println("The data fetched from Producer APIs::\n");
		System.out.println(jsonReturnValue);
		int AdvanceMIXOutputCount = jsonReturnValue.getInt("AdvanceMIXOutputCount");
		boolean afv = jsonReturnValue.getBoolean("AFV");
		if (!jsonReturnValue.isNull("AdvanceAFV")) {
			afv = jsonReturnValue.getJSONArray("AdvanceAFV").getBoolean(0);
		}

		///// prepare master source /////
		JSONObject master = (JSONObject) jsonReturnValue.get("Master");

		String pgmSourceId = "";
		if (!master.isNull("PGMSourceID")) {
			pgmSourceId = master.getString("PGMSourceID");
		}
		System.out.println("pgmSourceId::::::::::::::::: " + pgmSourceId);

		System.out.println("afv::::::::::::::::: " + afv);

		// code for handling SourceMasterGain
		if (!master.isNull("SourceMasterGain")) {
			master.put("SourceMasterGain", convertGainToSliderPosition(master.getInt("SourceMasterGain")));
		}
		// code for handling SourceMasterGain ENDS HERE

		JSONArray masterChannels = (JSONArray) master.get("Channels");
		int mSize = masterChannels.length();

		for (int j = 0; j < mSize; j++) {
			JSONObject masterChannel = masterChannels.getJSONObject(j);
			int gain = masterChannel.getInt("Gain");
			masterChannel.put("Gain", (int) convertGainToSliderPosition(gain));
			masterChannel.put("isMute", masterChannel.getBoolean("Mute"));
			masterChannel.remove("Mute");

			if (dbSources != null && !dbSources.isNull("Master")) {
				JSONObject savedChannel = getMasterSavedChannel(dbSources, masterChannel.getInt("ID"));
				if (savedChannel != null) {
					if (!savedChannel.isNull("AdvanceHeadPhone")) {
						masterChannel.put("AdvanceHeadPhone", savedChannel.getJSONArray("AdvanceHeadPhone"));
					}
					if (!savedChannel.isNull("AdvanceGainUI")) {
						masterChannel.put("AdvanceGain", savedChannel.getJSONArray("AdvanceGainUI"));
					}
					if (!savedChannel.isNull("AdvanceMute")) {
						masterChannel.put("AdvanceMute", savedChannel.getJSONArray("AdvanceMute"));
					}
				}
			} else {
				/// Initialize the AdvanceHeadPhone
				JSONArray updatedAdvanceHeadPhone = new JSONArray();
				for (int x = 0; x < AdvanceMIXOutputCount; x++) {
					updatedAdvanceHeadPhone.put(true);
				}
				masterChannel.put("AdvanceHeadPhone", updatedAdvanceHeadPhone);

				/// Initialize the AdvanceMute
				JSONArray updatedAdvanceMute = new JSONArray();
				for (int x = 0; x < AdvanceMIXOutputCount; x++) {
					updatedAdvanceMute.put(false);
				}
				masterChannel.put("AdvanceMute", updatedAdvanceMute);

				// Initialize AdvanceGain
				JSONArray updatedAdvanceGainValues = new JSONArray();
				int initialGainValue = (int) convertGainToSliderPosition(gain);
				for (int x = 0; x < AdvanceMIXOutputCount; x++) {
					updatedAdvanceGainValues.put(initialGainValue);
				}
				masterChannel.put("AdvanceGain", updatedAdvanceGainValues);
			}
		}
		/////////

		JSONArray sources = (JSONArray) jsonReturnValue.get("Sources");

		int size = sources.length();
		for (int i = 0; i < size; i++) {
			JSONObject source = sources.getJSONObject(i);
			boolean sourceExists = sourceExistInDB(dbSources, source);

			// Include rtilCode for the source if available in the DB
			if (rtilCodesJson != null) {
				JSONObject rtilinfo = getSourceRTILInfo(rtilCodesJson, source.getString("ID"));
				if (rtilinfo != null) {
					if (!rtilinfo.isNull("rtilCode")) {
						source.put("rtilCode", rtilinfo.getString("rtilCode"));
					}
					if (!rtilinfo.isNull("partyCode")) {
						source.put("partyCode", rtilinfo.getString("partyCode"));
					}
					if (!rtilinfo.isNull("sourceName")) {
						source.put("sourceName", rtilinfo.getString("sourceName"));
					}
					if (!rtilinfo.isNull("sourceType")) {
						source.put("sourceType", rtilinfo.getInt("sourceType"));
					}
					if (!rtilinfo.isNull("living")) {
						source.put("living", rtilinfo.getBoolean("living"));
					}
					if (!rtilinfo.isNull("slotNo")) {
						source.put("slotNo", rtilinfo.getString("slotNo"));
					}
				}
			}
			// Inclusion of rtilCode for the source ENDS HERE

			// Code to handle "SourceMasterGain" coming in the source
			if (!source.isNull("SourceMasterGain")) {
				source.put("SourceMasterGain", convertGainToSliderPosition(source.getInt("SourceMasterGain")));
			}
			// Code to handle "SourceMasterGain" coming in the source ENDs HERE
			if (sourceExists) {
				JSONObject savedSource = getSavedSource(dbSources, source.getString("ID"));
				if (savedSource != null) {
					if (!savedSource.isNull("BalanceUI")) {
						source.put("Balance", savedSource.getInt("BalanceUI"));
					}
				}
			}

			JSONArray channels = (JSONArray) source.get("Channels");
			int csize = channels.length();

			JSONArray newArrChannels = new JSONArray();

			for (int j = 0; j < csize; j++) {
				JSONObject channel = channels.getJSONObject(j);
				int gain = channel.getInt("Gain");
				channel.put("Gain", (int) convertGainToSliderPosition(gain));
				channel.put("isMute", channel.getBoolean("Mute"));
				channel.remove("Mute");

				if (sourceExists) {
					// if (dbSources != null) {
					JSONObject savedChannel = getSavedChannel(dbSources, source.getString("ID"), channel.getInt("ID"));
					if (savedChannel != null) {
						if (!savedChannel.isNull("AdvanceHeadPhone")) {
							channel.put("AdvanceHeadPhone", savedChannel.getJSONArray("AdvanceHeadPhone"));
						}
						if (!savedChannel.isNull("AdvanceBalance")) {
							channel.put("AdvanceBalance", savedChannel.getJSONArray("AdvanceBalance"));
						}
						if (afv) {
							if (pgmSourceId.equals(source.getString("ID"))) {
								JSONArray updatedAdvanceOutputs = new JSONArray();
								if (channel.getInt("ID") == 0 || channel.getInt("ID") == 1) {
									for (int x = 0; x < AdvanceMIXOutputCount; x++) {
										if (x == 0 || x == 1) {
											updatedAdvanceOutputs.put(true);
										} else {
											updatedAdvanceOutputs.put(false);
										}
									}
								} else {
									for (int x = 0; x < AdvanceMIXOutputCount; x++) {
										updatedAdvanceOutputs.put(false);
									}
								}
								channel.put("AdvanceOutput", updatedAdvanceOutputs);
							} else {
								JSONArray updatedAdvanceOutputs = new JSONArray();
								for (int x = 0; x < AdvanceMIXOutputCount; x++) {
									updatedAdvanceOutputs.put(false);
								}
								channel.put("AdvanceOutput", updatedAdvanceOutputs);
							}
							/*
							 * if (!channel.getBoolean("Output")) { JSONArray updatedAdvanceOutputs = new
							 * JSONArray(); for (int x = 0; x < AdvanceMIXOutputCount; x++) {
							 * updatedAdvanceOutputs.put(false); } channel.put("AdvanceOutput",
							 * updatedAdvanceOutputs); } else { JSONArray updatedAdvanceOutputs = new
							 * JSONArray(); for (int x = 0; x < AdvanceMIXOutputCount; x++) { if (x == 0 ||
							 * x == 1) { updatedAdvanceOutputs.put(true); } else {
							 * updatedAdvanceOutputs.put(false); } } channel.put("AdvanceOutput",
							 * updatedAdvanceOutputs); }
							 */
						} else {
							if (!savedChannel.isNull("AdvanceOutput")) {
								channel.put("AdvanceOutput", savedChannel.getJSONArray("AdvanceOutput"));
							}
						}

						if (!savedChannel.isNull("AdvanceMute")) {
							channel.put("AdvanceMute", savedChannel.getJSONArray("AdvanceMute"));
						}
						if (!savedChannel.isNull("AdvanceGainUI")) {
							channel.put("AdvanceGain", savedChannel.getJSONArray("AdvanceGainUI"));
						}
						if (!savedChannel.isNull("AdvanceDirection")) {
							channel.put("AdvanceDirection", savedChannel.getJSONArray("AdvanceDirection"));
						}
					}
				} else {
					//// Initialize the values for AdvancePan, Balance, AdvanceHeadPhone
					// Balance
					JSONArray updatedAdvanceBalances = new JSONArray();
					for (int x = 0; x < AdvanceMIXOutputCount; x++) {
						updatedAdvanceBalances.put(50);
					}
					channel.put("AdvanceBalance", updatedAdvanceBalances);

					// AdvancePan
					JSONArray updatedAdvancePan = new JSONArray();
					int channelId = channel.getInt("ID");
					if (channelId % 2 == 0) {
						for (int x = 0; x < AdvanceMIXOutputCount; x++) {
							if (x % 2 == 0) {
								updatedAdvancePan.put(100);
							} else if (x % 2 == 1) {
								updatedAdvancePan.put(0);
							}
						}
						channel.put("AdvancePan", updatedAdvancePan);
					} else if (channelId % 2 == 1) {
						for (int x = 0; x < AdvanceMIXOutputCount; x++) {
							if (x % 2 == 0) {
								updatedAdvancePan.put(0);
							} else if (x % 2 == 1) {
								updatedAdvancePan.put(100);
							}
						}
						channel.put("AdvancePan", updatedAdvancePan);
					}

					// AdvanceOutput
					if (afv) {
						if (pgmSourceId.equals(source.getString("ID"))) {
							JSONArray updatedAdvanceOutputs = new JSONArray();
							if (channel.getInt("ID") == 0 || channel.getInt("ID") == 1) {
								for (int x = 0; x < AdvanceMIXOutputCount; x++) {
									if (x == 0 || x == 1) {
										updatedAdvanceOutputs.put(true);
									} else {
										updatedAdvanceOutputs.put(false);
									}
								}
							} else {
								for (int x = 0; x < AdvanceMIXOutputCount; x++) {
									updatedAdvanceOutputs.put(false);
								}
							}
							channel.put("AdvanceOutput", updatedAdvanceOutputs);
						} else {
							JSONArray updatedAdvanceOutputs = new JSONArray();
							for (int x = 0; x < AdvanceMIXOutputCount; x++) {
								updatedAdvanceOutputs.put(false);
							}
							channel.put("AdvanceOutput", updatedAdvanceOutputs);
						}
					} else {
						if (!channel.getBoolean("Output")) {
							JSONArray updatedAdvanceOutputs = new JSONArray();
							for (int x = 0; x < AdvanceMIXOutputCount; x++) {
								updatedAdvanceOutputs.put(false);
							}
							channel.put("AdvanceOutput", updatedAdvanceOutputs);
						} else {
							JSONArray updatedAdvanceOutputs = new JSONArray();
							for (int x = 0; x < AdvanceMIXOutputCount; x++) {
								if (x == 0 || x == 1) {
									updatedAdvanceOutputs.put(true);
								} else {
									updatedAdvanceOutputs.put(false);
								}
							}
							channel.put("AdvanceOutput", updatedAdvanceOutputs);
						}
					}
					// AdvanceHeadPhone
					/// Initialize the AdvanceHeadPhone
					JSONArray updatedAdvanceHeadPhone = new JSONArray();
					for (int x = 0; x < AdvanceMIXOutputCount; x++) {
						updatedAdvanceHeadPhone.put(false);
					}
					channel.put("AdvanceHeadPhone", updatedAdvanceHeadPhone);

					// AdvanceMute
					/// Initialize the AdvanceMute
					JSONArray updatedAdvanceMute = new JSONArray();
					for (int x = 0; x < AdvanceMIXOutputCount; x++) {
						updatedAdvanceMute.put(false);
					}
					channel.put("AdvanceMute", updatedAdvanceMute);

					// Initialize AdvanceGain
					JSONArray updatedAdvanceGainValues = new JSONArray();
					int initialGainValue = (int) convertGainToSliderPosition(gain);
					for (int x = 0; x < AdvanceMIXOutputCount; x++) {
						updatedAdvanceGainValues.put(initialGainValue);
					}
					channel.put("AdvanceGain", updatedAdvanceGainValues);

					// Initialize AdvanceDirection
					JSONArray updatedAdvanceDirectionValues = new JSONArray();
					for (int x = 0; x < AdvanceMIXOutputCount; x++) {
						updatedAdvanceDirectionValues.put(-1);
					}
					channel.put("AdvanceDirection", updatedAdvanceDirectionValues);
				}
			}
			source.put("SourceName", "Source " + (i + 1));
		}
		try {
			// boolean rps = rtilCodesJson.getBoolean("rps");
			// jsonReturnValue.put("rps", rps);
			if (rtilCodesJson != null) {
				if (!rtilCodesJson.isNull("rps")) {
					jsonReturnValue.put("rps", rtilCodesJson.getBoolean("rps"));
				}

				if (!rtilCodesJson.isNull("productionId")) {
					jsonReturnValue.put("productionId", rtilCodesJson.getLong("productionId"));
				}

				if (!rtilCodesJson.isNull("productionName")) {
					jsonReturnValue.put("productionName", rtilCodesJson.getString("productionName"));
				}
				if (!rtilCodesJson.isNull("partyCodes")) {
					jsonReturnValue.put("partyCodes", rtilCodesJson.getJSONArray("partyCodes"));
				}
				if (!rtilCodesJson.isNull("sources")) {
					JSONArray srcArray = rtilCodesJson.getJSONArray("sources");
					int ssize = srcArray.length();
					for (int i = 0; i < ssize; i++) {
						JSONObject source = srcArray.getJSONObject(i);
						if (source.isNull("SourceID")) {
							jsonReturnValue.getJSONArray("Sources").put(source);
						}
					}
				}
			}
			if (dbSources != null) {
				if (!dbSources.isNull("MonitorLevel")) {
					jsonReturnValue.put("MonitorLevel", dbSources.getJSONArray("MonitorLevel"));
				}
			} else {
				JSONArray updatedMonitorLevelValues = new JSONArray();
				for (int x = 0; x < (AdvanceMIXOutputCount / 2); x++) {
					updatedMonitorLevelValues.put(0.5);
				}
				jsonReturnValue.put("MonitorLevel", updatedMonitorLevelValues);
			}

		} catch (Exception e) {
			System.out.println(e.toString());
		}
		return jsonReturnValue;
	}

	private JSONObject getSavedChannel(JSONObject dbSources, String sourceId, Integer channelId) {
		// Integer gain = 1000;
		JSONObject savedChannel = null;
		JSONArray sources = (JSONArray) dbSources.get("Sources");
		int size = sources.length();
		for (int i = 0; i < size; i++) {
			JSONObject source = sources.getJSONObject(i);
			String srcId = source.getString("ID");

			if (srcId.equals(sourceId)) {

				JSONArray channels = (JSONArray) source.get("Channels");
				int csize = channels.length();

				// JSONArray newArrChannels = new JSONArray();

				for (int j = 0; j < csize; j++) {
					savedChannel = channels.getJSONObject(j);
					Integer cId = savedChannel.getInt("ID");
					if (cId == channelId) {
						break;
					}
				}
				break;
			}
		}
		return savedChannel;

	}

	private JSONObject getSavedSource(JSONObject dbSources, String sourceId) {
		JSONObject source = null;
		JSONArray sources = (JSONArray) dbSources.get("Sources");
		int size = sources.length();
		for (int i = 0; i < size; i++) {
			source = sources.getJSONObject(i);
			String srcId = source.getString("ID");

			if (srcId.equals(sourceId)) {
				break;
			}
		}
		return source;

	}

	private JSONObject getMasterSavedChannel(JSONObject dbSources, Integer channelId) {
		JSONObject savedMasterChannel = null;
		try {
			JSONObject master = (JSONObject) dbSources.get("Master");
			JSONArray channels = (JSONArray) master.get("Channels");
			int csize = channels.length();

			for (int j = 0; j < csize; j++) {
				savedMasterChannel = channels.getJSONObject(j);

				Integer cId = savedMasterChannel.getInt("ID");
				if (cId == channelId) {
					break;
				}
			}
		} catch (Exception e) {

		}
		return savedMasterChannel;
	}

	public boolean validateRequestGroupsJson(JSONObject groupsJSON) {
		boolean isValid = true;
		JSONObject jsonSchema = new JSONObject(
				new JSONTokener(this.getClass().getResourceAsStream("/groupsSchema.json")));

		Schema schema = SchemaLoader.load(jsonSchema);
		try {
			schema.validate(groupsJSON);
		} catch (Exception e) {
			isValid = false;
		}
		return isValid;
	}

	public boolean validateRtilMeetingInfoJson(JSONObject agoraMeetingInfoJSON) {
		boolean isValid = true;
		JSONObject jsonSchema = new JSONObject(
				new JSONTokener(this.getClass().getResourceAsStream("/rtilMeetingInfo.json")));

		Schema schema = SchemaLoader.load(jsonSchema);
		try {
			schema.validate(agoraMeetingInfoJSON);
		} catch (Exception e) {
			isValid = false;
		}
		return isValid;
	}

	public boolean validateRequestSourcesJson(JSONObject sourcesJSON) {
		boolean isValid = true;
		JSONObject jsonSchema = new JSONObject(
				new JSONTokener(this.getClass().getResourceAsStream("/SaveSourcesReqSchema.json")));

		Schema schema = SchemaLoader.load(jsonSchema);
		try {
			schema.validate(sourcesJSON);
		} catch (Exception e) {
			isValid = false;
		}
		return isValid;
	}

	public boolean validateSourcesRtilCodesJson(JSONObject sourcesJSON) {
		boolean isValid = true;
		JSONObject jsonSchema = new JSONObject(
				new JSONTokener(this.getClass().getResourceAsStream("/SaveSourcesRtilInfoSchema.json")));

		Schema schema = SchemaLoader.load(jsonSchema);
		try {
			schema.validate(sourcesJSON);
		} catch (Exception e) {
			isValid = false;
		}
		return isValid;
	}

	/*
	 * private String updateSourcesGainForGroups(String grp, String src) {
	 * JSONObject grpJson = new JSONObject(grp); JSONObject srcJson = null;
	 * JSONArray grpArray = grpJson.getJSONArray("Groups"); int gsize =
	 * grpArray.length(); for (int i = 0; i < gsize; i++) { JSONObject group =
	 * grpArray.getJSONObject(i); int groupGain = group.getInt("Gain"); boolean
	 * isGroupMute = group.getBoolean("isMute");
	 * 
	 * JSONArray chArray = group.getJSONArray("ChannelIds"); List<String> lstCh =
	 * new ArrayList<String>();
	 * 
	 * int csize = chArray.length(); for (int j = 0; j < csize; j++) { String
	 * srcAndChannel = chArray.getString(j); lstCh.add(srcAndChannel); // String
	 * sourceId = srcAndChannel.substring(0, srcAndChannel.indexOf("-")); // String
	 * channelId = srcAndChannel.substring(srcAndChannel.indexOf("-") + 1);
	 * 
	 * } if (!isGroupMute && !lstCh.isEmpty()) { srcJson = new JSONObject(src);
	 * updateChannelGain(srcJson, lstCh, groupGain);
	 * 
	 * ///////// Update the master to mute /////// JSONObject master =
	 * srcJson.getJSONObject("Master"); JSONArray masterChArray =
	 * master.getJSONArray("Channels"); boolean isMasterMute = false; int mcSize =
	 * masterChArray.length(); for (int j = 0; j < mcSize; j++) { JSONObject channel
	 * = masterChArray.getJSONObject(j); // channel.put("Gain", MUTE_GAIN);
	 * channel.put("Output", false); } ////////////////////////////////////////// }
	 * } if (srcJson == null) { srcJson = new JSONObject(src); } return
	 * srcJson.toString(); }
	 */
	/*
	 * private void updateChannelGain(JSONObject srcJson, List chAndSource, int
	 * groupGain) {
	 * 
	 * // System.out.println("chAndSourcechAndSourcechAndSource:::::: " +
	 * chAndSource); // Integer cId = Integer.parseInt(channelId); JSONArray
	 * srcArray = srcJson.getJSONArray("Sources"); int ssize = srcArray.length();
	 * for (int i = 0; i < ssize; i++) { JSONObject source =
	 * srcArray.getJSONObject(i); String sourceId = source.getString("ID");
	 * JSONArray chArray = source.getJSONArray("Channels"); int csize =
	 * chArray.length(); for (int j = 0; j < csize; j++) { JSONObject channel =
	 * chArray.getJSONObject(j); int chGain = channel.getInt("Gain");
	 * channel.put("Gain", getConvertedGain(groupGain + chGain)); int channelId =
	 * channel.getInt("ID"); boolean isChannelMute = channel.getBoolean("isMute");
	 * if (chAndSource.contains(sourceId + "-" + channelId)) { //
	 * System.out.println(" KKKKKK " + sourceId + "-" + channelId); if
	 * (isChannelMute) { // channel.put("Gain", MUTE_GAIN); channel.put("Output",
	 * false); } else { // int chGain = channel.getInt("Gain"); //
	 * channel.put("Gain", (groupGain + chGain)); // channel.put("Gain",
	 * getConvertedGain(groupGain + chGain)); channel.put("Output", true); }
	 * 
	 * } else { // System.out.println(" NOTKKKKKK " + sourceId + "-" + channelId);
	 * // channel.put("Gain", MUTE_GAIN); channel.put("Output", false); }
	 * channel.put("Mute", channel.getBoolean("isMute")); channel.remove("isMute");
	 * } } }
	 */
	private void updateSourcesForProducerV2(JSONObject srcJson) {

		if (!srcJson.isNull("Master")) {
			JSONObject master = srcJson.getJSONObject("Master");

			// Code to handle "SourceMasterGain" coming in the request
			if (!master.isNull("SourceMasterGain")) {
				master.put("SourceMasterGain", getConvertedGain(master.getInt("SourceMasterGain")));
			}
			// Code to handle "SourceMasterGain" coming in the request ENDs HERE

			JSONArray masterChArray = master.getJSONArray("Channels");
			boolean isMasterMute = false;
			int mcSize = masterChArray.length();
			for (int j = 0; j < mcSize; j++) {
				JSONObject channel = masterChArray.getJSONObject(j);
				channel.put("Gain", getConvertedGain(channel.getInt("Gain")));
				channel.put("Output", true);
				channel.put("Mute", channel.getBoolean("isMute"));
				channel.remove("isMute");

				// handling for AdvanceGain parameter
				try {
					if (!channel.isNull("AdvanceGain")) {
						JSONArray advanceGainValues = channel.getJSONArray("AdvanceGain");
						JSONArray updatedAdvanceGainValues = new JSONArray();
						int agSize = advanceGainValues.length();
						for (int x = 0; x < agSize; x++) {
							updatedAdvanceGainValues.put(getConvertedGain(advanceGainValues.getInt(x)));
						}
						// channel.put("AdvanceGain", updatedAdvanceGainValues);
						channel.put("AdvanceGainUI", advanceGainValues);
						channel.put("AdvanceGain",
								convertGainValues(channel.getJSONArray("AdvanceMute"), null, updatedAdvanceGainValues));
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Unable to process the AdvanceGain values while saving the data.");
				}
				// handling for AdvanceGain value ENDS HERE
			}
		}
		if (!srcJson.isNull("Sources")) {
			JSONArray srcArray = srcJson.getJSONArray("Sources");
			int ssize = srcArray.length();
			for (int i = 0; i < ssize; i++) {
				JSONObject source = srcArray.getJSONObject(i);

				// Code to handle "SourceMasterGain" coming in the request
				if (!source.isNull("SourceMasterGain")) {
					source.put("SourceMasterGain", getConvertedGain(source.getInt("SourceMasterGain")));
				}
				// Code to handle "SourceMasterGain" coming in the request ENDs HERE
				// Add Balance as -1
				if (!source.isNull("Balance")) {
					source.put("BalanceUI", source.getInt("Balance"));
				}
				source.put("Balance", -1);

				JSONArray chArray = source.getJSONArray("Channels");
				int csize = chArray.length();
				for (int j = 0; j < csize; j++) {
					JSONObject channel = chArray.getJSONObject(j);
					channel.put("Gain", getConvertedGain(channel.getInt("Gain")));
					channel.put("Mute", channel.getBoolean("isMute"));
					channel.remove("isMute");

					// handling for AdvanceGain parameter
					try {
						if (!channel.isNull("AdvanceGain")) {
							JSONArray advanceGainValues = channel.getJSONArray("AdvanceGain");
							// System.out.println("FROM UI-->AdvanceGain value::" +
							// advanceGainValues.toString());

							JSONArray updatedAdvanceGainValues = new JSONArray();
							int agSize = advanceGainValues.length();
							for (int x = 0; x < agSize; x++) {
								updatedAdvanceGainValues.put(getConvertedGain(advanceGainValues.getInt(x)));
							}
							// System.out.println("FROM UI-->AdvanceGain value after conversion::" +
							// advanceGainValues.toString());
							channel.put("AdvanceGainUI", advanceGainValues);
							channel.put("AdvanceGain", convertGainValues(channel.getJSONArray("AdvanceMute"),
									channel.getJSONArray("AdvanceOutput"), updatedAdvanceGainValues));

						}
						/// Setting up Output parameter for the channel
						channel.put("Output", false);
						JSONArray advoutputArr = channel.getJSONArray("AdvanceOutput");
						for (int z = 0; z < advoutputArr.length(); z++) {
							if (advoutputArr.getBoolean(z)) {
								channel.put("Output", true);
								break;
							}
						}
						/// Setting up Output parameter for the channel ENDs HERE

					} catch (Exception e) {
						e.printStackTrace();
						System.out.println("Unable to process the AdvanceGain values while saving the data.");
					}
					// handling for AdvanceGain value ENDS HERE

				}
			}
		}
	}

///////////////////// Added for supporting RC
	private void updateSourcesForProducer(JSONObject srcJson) {

		JSONObject master = srcJson.getJSONObject("Master");

		// Code to handle "SourceMasterGain" coming in the request
		if (!master.isNull("SourceMasterGain")) {
			master.put("SourceMasterGain", getConvertedGain(master.getInt("SourceMasterGain")));
		}
		// Code to handle "SourceMasterGain" coming in the request ENDs HERE

		JSONArray masterChArray = master.getJSONArray("Channels");
		boolean isMasterMute = false;
		int mcSize = masterChArray.length();
		for (int j = 0; j < mcSize; j++) {
			JSONObject channel = masterChArray.getJSONObject(j);
			channel.put("Gain", getConvertedGain(channel.getInt("Gain")));
			channel.put("Output", true);
			channel.put("Mute", channel.getBoolean("isMute"));
			channel.remove("isMute");

			// handling for AdvanceGain parameter
			try {
				if (!channel.isNull("AdvanceGain")) {
					JSONArray advanceGainValues = channel.getJSONArray("AdvanceGain");
					JSONArray updatedAdvanceGainValues = new JSONArray();
					int agSize = advanceGainValues.length();
					for (int x = 0; x < agSize; x++) {
						updatedAdvanceGainValues.put(getConvertedGain(advanceGainValues.getInt(x)));
					}
					channel.put("AdvanceGain", updatedAdvanceGainValues);
				}
			} catch (Exception e) {
				System.out.println("Unable to process the AdvanceGain values while saving the data.");
			}
			// handling for AdvanceGain value ENDS HERE
		}

		JSONArray srcArray = srcJson.getJSONArray("Sources");
		int ssize = srcArray.length();
		for (int i = 0; i < ssize; i++) {
			JSONObject source = srcArray.getJSONObject(i);

			// Code to handle "SourceMasterGain" coming in the request
			if (!source.isNull("SourceMasterGain")) {
				source.put("SourceMasterGain", getConvertedGain(source.getInt("SourceMasterGain")));
			}
			// Code to handle "SourceMasterGain" coming in the request ENDs HERE

			JSONArray chArray = source.getJSONArray("Channels");
			int csize = chArray.length();
			for (int j = 0; j < csize; j++) {
				JSONObject channel = chArray.getJSONObject(j);
				channel.put("Gain", getConvertedGain(channel.getInt("Gain")));
				channel.put("Mute", channel.getBoolean("isMute"));
				channel.remove("isMute");
				// handling for AdvanceGain parameter
				try {
					if (!channel.isNull("AdvanceGain")) {
						JSONArray advanceGainValues = channel.getJSONArray("AdvanceGain");
						// System.out.println("FROM UI-->AdvanceGain value::" +
						// advanceGainValues.toString());

						JSONArray updatedAdvanceGainValues = new JSONArray();
						int agSize = advanceGainValues.length();
						for (int x = 0; x < agSize; x++) {
							updatedAdvanceGainValues.put(getConvertedGain(advanceGainValues.getInt(x)));
						}
						// System.out.println("FROM UI-->AdvanceGain value after conversion::" +
						// advanceGainValues.toString());
						channel.put("AdvanceGain", updatedAdvanceGainValues);
					}
				} catch (Exception e) {
					System.out.println("Unable to process the AdvanceGain values while saving the data.");
				}
				// handling for AdvanceGain value ENDS HERE

			}
		}
	}

////////////////////////	

	private long getConvertedGain(int slider_position) {
		double updatedGain = (Math.pow(10, (double) (slider_position + 100) / 100) * 0.04093171969);
		return Math.round(updatedGain);
	}

	private long convertGainToSliderPosition(int gain) {
		long slider_position = (long) (100 * Math.log10(gain * 2.443093053));
		return slider_position;
	}

	private Map<String, Object> sentNotificationForTask(Map<String, Object> inputParams) {
		System.out.println("Entering sentNotificationForTask(Map<String, Object> inputParams) method...");
		String taskid = null;
		Map<String, Object> responseMap = new HashMap();

		try {
			String message = "";
			try {
				message = (String) inputParams.get("body");
			} catch (Exception e) {

			}
			if (message == null)
				message = "";
			Map queryStringParameters = new HashMap();
			if (inputParams.get("queryStringParameters") != null) {
				queryStringParameters = (Map) inputParams.get("queryStringParameters");
				if (queryStringParameters.get("taskid") != null) {
					taskid = (String) queryStringParameters.get("taskid");
				}
			}
			if (taskid != null) {
				sendNotificationsForTask(taskid, message);
				JSONObject response = new JSONObject();
				response.put("message", "Message for updating sources sent to all UI clients");
				responseMap.put("statusCode", "200");
				responseMap.put("body", response.toString());

			} else {
				JSONObject response = new JSONObject();
				response.put("message", TASKID_NOT_PROVIDED);
				responseMap.put("statusCode", "400");
				responseMap.put("body", response.toString());
			}

		} catch (Exception e) {
			responseMap.put("statusCode", "503");
			JSONObject response = new JSONObject();
			response.put("message", "Error in sending the notifications");
			responseMap.put("body", response.toString());
			e.printStackTrace();
		}
		return responseMap;
	}

	private void sendNotificationsForTask(String taskid, String message) {
		try {
			AudioMixerDao dao = new AudioMixerDaoImpl();
			long s3 = System.currentTimeMillis();
					
			List<AudioMixerClient> clients = dao.getClientConnections(taskid);
			System.out.println("@@updateSourcesRtilCodes::sendNotificationsForTask:: Time taken in retreiving client WS connections from DB:"+(System.currentTimeMillis()-s3));

			
			AwsClientBuilder.EndpointConfiguration config = new AwsClientBuilder.EndpointConfiguration(
					System.getenv("NOTIFICATION_URL"), null);

			AmazonApiGatewayManagementApiClient api = (AmazonApiGatewayManagementApiClient) AmazonApiGatewayManagementApiClient
					.builder().standard().withEndpointConfiguration(config).build();
			long s4 = System.currentTimeMillis();
			int totalClients = clients.size();
			for (int i = 0; i < totalClients; i++) {
				AudioMixerClient client = clients.get(i);
				try {
					PostToConnectionRequest request = new PostToConnectionRequest();
					request.setConnectionId(client.getConnectionid());

					Charset charset = Charset.forName("UTF-8");
					CharsetEncoder encoder = charset.newEncoder();
					ByteBuffer buff = null;

					buff = encoder.encode(CharBuffer.wrap(message));
					request.setData(buff);
					api.postToConnection(request);
				} catch (Exception e) {

				}
			}
			System.out.println("@@updateSourcesRtilCodes::sendNotificationsForTask:: Time taken in sending the notification:"+(System.currentTimeMillis()-s4));

		} catch (Exception e) {
			System.out.println("Exception in notification method::::::" + e.toString());
			e.printStackTrace();
		}

	}

	private Map<String, Object> saveClientConnection(Map<String, Object> inputParams) {
		Map<String, String> requestContext = (Map<String, String>) inputParams.get("requestContext");
		Map<String, Object> responseMap = new HashMap();
		try {
			AudioMixerDao dao = new AudioMixerDaoImpl();
			Map queryMap = (Map) inputParams.get("queryStringParameters");
			String taskid = (String) queryMap.get("taskid");

			String connectionid = (String) requestContext.get("connectionId");

			AudioMixerClient client = new AudioMixerClient();
			client.setConnectionid(connectionid);
			client.setTaskid(taskid);

			dao.saveClientConnection(client);
			JSONObject response = new JSONObject();
			response.put("message", "Client connection saved successfully");
			responseMap.put("statusCode", "200");
			responseMap.put("body", response.toString());
		} catch (Exception e) {
			responseMap.put("statusCode", "503");
			JSONObject response = new JSONObject();
			response.put("message", "Unable to save connection id");
			responseMap.put("body", response.toString());
			e.printStackTrace();
		}
		return responseMap;
	}

	private Map<String, Object> deleteClientConnection(Map<String, Object> inputParams) {
		Map<String, String> requestContext = (Map<String, String>) inputParams.get("requestContext");
		Map<String, Object> responseMap = new HashMap();
		try {
			AudioMixerDao dao = new AudioMixerDaoImpl();
			AudioMixerClient client = new AudioMixerClient();
			client.setConnectionid(requestContext.get("connectionId"));
			dao.deleteClientConnection(client);
			JSONObject response = new JSONObject();
			response.put("message", "Client connection deleted successfully");
			responseMap.put("statusCode", "200");
			responseMap.put("body", response.toString());
		} catch (Exception e) {
			responseMap.put("statusCode", "503");
			JSONObject response = new JSONObject();
			response.put("message", "Unable to delete connection id");
			responseMap.put("body", response.toString());
			e.printStackTrace();
		}
		return responseMap;
	}

	private Map<String, Object> sendClientNotifications(Map<String, Object> inputParams) {
		Map<String, Object> responseMap = new HashMap();
		try {
			Map<String, String> contextParams = (Map<String, String>) inputParams.get("requestContext");
			JSONObject json = new JSONObject((String) inputParams.get("body"));
			String taskid = json.getString("taskid");
			JSONObject message = new JSONObject();
			message.put("message", json.getString("message"));
			AudioMixerDao dao = new AudioMixerDaoImpl();
			List<AudioMixerClient> clients = dao.getClientConnections(taskid);

			AwsClientBuilder.EndpointConfiguration config = new AwsClientBuilder.EndpointConfiguration(
					System.getenv("NOTIFICATION_URL"), null);

			AmazonApiGatewayManagementApiClient api = (AmazonApiGatewayManagementApiClient) AmazonApiGatewayManagementApiClient
					.builder().standard().withEndpointConfiguration(config).build();

			int totalClients = clients.size();
			for (int i = 0; i < totalClients; i++) {
				AudioMixerClient client = clients.get(i);
				if (contextParams.get("connectionId").equals(client.getConnectionid())) {
					continue;
				}
				try {
					PostToConnectionRequest request = new PostToConnectionRequest();
					request.setConnectionId(client.getConnectionid());

					Charset charset = Charset.forName("UTF-8");
					CharsetEncoder encoder = charset.newEncoder();
					ByteBuffer buff = null;

					buff = encoder.encode(CharBuffer.wrap(message.toString()));
					request.setData(buff);
					api.postToConnection(request);
				} catch (Exception e) {

				}
			}
			JSONObject response = new JSONObject();
			response.put("message", "Message for updating sources sent to all UI clients");
			responseMap.put("statusCode", "200");
			responseMap.put("body", response.toString());

		} catch (Exception e) {
			JSONObject response = new JSONObject();
			response.put("message", "Error in sending notification messages");
			responseMap.put("statusCode", "503");
			responseMap.put("body", response.toString());
		}
		return responseMap;
	}

	private Map<String, Object> updateSourcesRtilCodes(Map<String, Object> inputParams) {
		System.out.println("Entering updateSourcesRtilCodes method...");
		String taskid = null;
		Map<String, Object> responseMap = new HashMap();

		try {
			String rtilJSON = null;
			try {
				rtilJSON = (String) inputParams.get("body");
			} catch (Exception e) {

			}
			if (rtilJSON == null) {
				JSONObject response = new JSONObject();
				response.put("message", "Please provide JSON for sources RTIL");
				responseMap.put("statusCode", "400");
				responseMap.put("body", response.toString());
				return responseMap;
			}
			JSONObject jsonBody = new JSONObject(rtilJSON);
			if (!validateSourcesRtilCodesJson(jsonBody)) {
				JSONObject response = new JSONObject();
				response.put("message", INVALID_JSON);
				responseMap.put("statusCode", "400");
				responseMap.put("body", response.toString());
				return responseMap;
			}

			Map queryStringParameters = new HashMap();
			if (inputParams.get("queryStringParameters") != null) {
				queryStringParameters = (Map) inputParams.get("queryStringParameters");
				if (queryStringParameters.get("taskid") != null) {
					taskid = (String) queryStringParameters.get("taskid");
				}
			}
			if (taskid == null) {
				JSONObject response = new JSONObject();
				response.put("message", TASKID_NOT_PROVIDED);
				responseMap.put("statusCode", "400");
				responseMap.put("body", response.toString());
				return responseMap;
			}
			System.out.println("@@updateSourcesRtilCodes::"+taskid);

			AudioMixerDao dao = new AudioMixerDaoImpl();
			long s1 = System.currentTimeMillis();
			AudioMixer audiomixer = dao.retrieveAudioMixer(taskid);
			System.out.println("@@updateSourcesRtilCodes:: Time taken in retreiving the data from DB:"+(System.currentTimeMillis()-s1));
			if (audiomixer == null) {
				responseMap.put("statusCode", "404");
				// responseBody = new JSONObject();
				JSONObject response = new JSONObject();
				response.put("message", TASK_NOTFOUND);
				responseMap.put("body", response.toString());
				return responseMap;
			}
			if (audiomixer.isDeleted()) {
				responseMap.put("statusCode", "400");
				JSONObject response = new JSONObject();
				response.put("message", TASK_ALREADY_DELETED);
				responseMap.put("body", response.toString());
				return responseMap;
			}
			if (audiomixer.getTaskstatus() != 1) {
				responseMap.put("statusCode", "400");
				JSONObject response = new JSONObject();
				response.put("message", TASK_IS_DISABLED);
				responseMap.put("body", response.toString());
				return responseMap;
			}
			audiomixer.setSourcesRtilJSON(rtilJSON);
			long s2 = System.currentTimeMillis();
			dao.saveAudioMixer(audiomixer);
			System.out.println("@@updateSourcesRtilCodes:: Time taken in saving the data in DB:"+(System.currentTimeMillis()-s2));

			// send notifications
			JSONObject message = new JSONObject();
			message.put("message", "UpdateSources");
			long s5 = System.currentTimeMillis();
			sendNotificationsForTask(taskid, message.toString());
			System.out.println("@@updateSourcesRtilCodes:: TOTAL Time taken in sending the notification:"+(System.currentTimeMillis()-s5));

			JSONObject response = new JSONObject();
			response.put("message", "Sources RTIL codes are saved successfully.");
			responseMap.put("statusCode", "200");
			responseMap.put("body", response.toString());

		} catch (Exception e) {
			responseMap.put("statusCode", "503");
			JSONObject response = new JSONObject();
			response.put("message", "Error in saving the RTIL codes or error in sending the notifications");
			responseMap.put("body", response.toString());
			e.printStackTrace();
		}
		return responseMap;
	}

	private int getMeetingUid() {
		long min = 1;
		long max = 4294967295l;
		return (int) ((Math.random() * (max - min)) + min);
	}

	private Map<String, Object> generateRtilMeetingToken(Map<String, Object> inputParams) {
		System.out.println("Entering generateAgoraMeetingToken method...");
		String taskid = null;
		AudioMixer audio = new AudioMixer();

		Map<String, Object> responseMap = new HashMap();

		JSONObject jsonBody;
		try {
			Map queryStringParameters = new HashMap();
			if (inputParams.get("queryStringParameters") != null) {
				queryStringParameters = (Map) inputParams.get("queryStringParameters");
				if (queryStringParameters.get("taskid") != null) {
					taskid = (String) queryStringParameters.get("taskid");
				}
			}

			if (taskid != null) {
				System.out.println("taskid:::::::::: " + taskid);

				AudioMixerDao dao = new AudioMixerDaoImpl();
				audio = dao.retrieveAudioMixer(taskid);
				if (audio != null) {
					System.out.println("vmip:::::::::: " + audio.getVmip());
					if (audio.isDeleted()) {
						responseMap.put("statusCode", "400");
						JSONObject response = new JSONObject();
						response.put("message", TASK_ALREADY_DELETED);
						responseMap.put("body", response.toString());
						return responseMap;
					}
					if (audio.getTaskstatus() != 1) {
						responseMap.put("statusCode", "400");
						JSONObject response = new JSONObject();
						response.put("message", TASK_IS_DISABLED);
						responseMap.put("body", response.toString());
						return responseMap;
					}
					String rtilMeetingInfo = audio.getRtilMeetingInfoJSON();
					if (rtilMeetingInfo == null) {
						responseMap.put("statusCode", "400");
						JSONObject response = new JSONObject();
						response.put("message", "RTIL channel Id not found for the task");
						responseMap.put("body", response.toString());
						return responseMap;
					}
					int uid = getMeetingUid();
					JSONObject json = new JSONObject(rtilMeetingInfo);
					String channelId = json.getString("rtilChannelId");
					AgoraTokenGenerator tokenGenerator = new AgoraTokenGenerator();
					String token = tokenGenerator.getMeetingToken(uid, AGORA_APPID, channelId, AGORA_APP_CERT,
							AGORA_TOKEN_EXPIRATION);

					responseMap.put("statusCode", "200");
					JSONObject response = new JSONObject();
					response.put("appId", AGORA_APPID);
					response.put("meetingToken", token);
					response.put("uid", uid + "");
					response.put("rtilChannelId", channelId);
					responseMap.put("body", response.toString());

				} else {
					responseMap.put("statusCode", "404");
					JSONObject response = new JSONObject();
					response.put("message", TASK_NOTFOUND);
					responseMap.put("body", response.toString());
				}
			} else {
				JSONObject response = new JSONObject();
				response.put("message", TASKID_NOT_PROVIDED);
				responseMap.put("statusCode", "400");
				responseMap.put("body", response.toString());
			}
		} catch (Exception e) {
			responseMap.put("statusCode", "503");
			JSONObject response = new JSONObject();
			response.put("message", "Error in meeting token generation");
			responseMap.put("body", response.toString());
			e.printStackTrace();
		}
		return responseMap;
	}

	private JSONObject getSourceRTILInfo(JSONObject rtilCodesJson, String sourceId) {
		String rtilCode = null;
		JSONObject source = null;
		JSONArray srcArray = rtilCodesJson.getJSONArray("sources");
		int ssize = srcArray.length();
		for (int i = 0; i < ssize; i++) {
			source = srcArray.getJSONObject(i);
			if(source.isNull("SourceID")) {
				continue;
			}
			String id = source.getString("SourceID");
			if (id.equals(sourceId)) {
				return source;
			}

		}
		return source;

	}

	private Map<String, Object> processRequest(Map<String, Object> inputParams) {
		Map<String, Object> inputData = new HashMap();
		Map<String, Object> responseMap = new HashMap();
		Map<String, String> qstring = new HashMap();
		String wsConnectionId = "";
		try {
			Map<String, String> contextParams = (Map<String, String>) inputParams.get("requestContext");
			wsConnectionId = contextParams.get("connectionId");
			JSONObject json = new JSONObject((String) inputParams.get("body"));

			String message = json.getString("message");
			System.out.println("Processing request for " + message);

			if (!json.isNull("taskid")) {
				System.out.println("taskid:::: " + json.getString("taskid"));
				qstring.put("taskid", json.getString("taskid"));
			}
			if (!json.isNull("screenid")) {
				System.out.println("screenid:::: " + json.getString("screenid"));
				qstring.put("screenid", json.getString("screenid"));
			}

			inputData.put("body", json.getJSONObject("data").toString());
			inputData.put("queryStringParameters", qstring);
			if (message.equalsIgnoreCase("saveSources")) {
				responseMap = saveSourcesV2(inputData);
			} else if (message.equalsIgnoreCase("saveGroups")) {
				responseMap = saveGroups(inputData);
			} else {
				responseMap = sendClientNotifications(inputParams);
			}

			sendRequestProccessingResponse(wsConnectionId, (String) responseMap.get("body"));
		} catch (Exception e) {
			JSONObject response = new JSONObject();
			response.put("message", "Unable to process the request on the web socket");
			sendRequestProccessingResponse(wsConnectionId, response.toString());
			e.printStackTrace();
		}

		return responseMap;
	}

	private void sendRequestProccessingResponse(String wsConnectionId, String message) {
		try {

			AwsClientBuilder.EndpointConfiguration config = new AwsClientBuilder.EndpointConfiguration(
					System.getenv("NOTIFICATION_URL"), null);

			AmazonApiGatewayManagementApiClient api = (AmazonApiGatewayManagementApiClient) AmazonApiGatewayManagementApiClient
					.builder().standard().withEndpointConfiguration(config).build();

			PostToConnectionRequest request = new PostToConnectionRequest();
			request.setConnectionId(wsConnectionId);

			Charset charset = Charset.forName("UTF-8");
			CharsetEncoder encoder = charset.newEncoder();
			ByteBuffer buff = null;

			buff = encoder.encode(CharBuffer.wrap(message));
			request.setData(buff);
			api.postToConnection(request);

		} catch (Exception e) {
			System.out.println("Unable to send the request processing response to " + wsConnectionId);
			e.printStackTrace();
		}

	}

	private JSONObject updateScreenSettings(String dbScreenSettings, JSONObject reqUserScreenSettings) {
		boolean settingFound = false;
		JSONObject screenSettings = new JSONObject();
		JSONArray arrUserSettings = new JSONArray();
		if (dbScreenSettings != null) {
			screenSettings = new JSONObject(dbScreenSettings);
			arrUserSettings = screenSettings.getJSONArray("screenSettings");
			int size = arrUserSettings.length();
			for (int i = 0; i < size; i++) {
				JSONObject obj = arrUserSettings.getJSONObject(i);
				String sId = obj.getString("screenId");
				if (sId.equalsIgnoreCase(reqUserScreenSettings.getString("screenId"))) {
					arrUserSettings.put(i, reqUserScreenSettings);
					settingFound = true;
					break;
				}
			}
			if (!settingFound) {
				arrUserSettings.put(reqUserScreenSettings);
			}

		} else {
			screenSettings = new JSONObject();
			arrUserSettings = new JSONArray();
			arrUserSettings.put(reqUserScreenSettings);
			screenSettings.put("screenSettings", arrUserSettings);
		}
		return screenSettings;
	}

	private JSONObject getUserScreenSettings(String dbScreenSettings, String screenId) {
		JSONObject userScreenSettings = null;
		JSONObject jsonDbScreenSettings = new JSONObject(dbScreenSettings);
		JSONArray arrUserSettings = jsonDbScreenSettings.getJSONArray("screenSettings");
		int size = arrUserSettings.length();
		for (int i = 0; i < size; i++) {
			userScreenSettings = arrUserSettings.getJSONObject(i);
			String sId = userScreenSettings.getString("screenId");
			if (sId.equalsIgnoreCase(screenId)) {
				break;
			}
		}
		return userScreenSettings;
	}

	private JSONObject updateDBSources(JSONObject dbSources, JSONObject jsonBody) {
		JSONObject source = null;
		if (dbSources == null) {
			dbSources = new JSONObject(jsonBody.toString());
		} else {
			if (!jsonBody.isNull("AFV")) {
				dbSources.put("AFV", jsonBody.getBoolean("AFV"));
			}
			if (!jsonBody.isNull("MonitorLevel")) {
				dbSources.put("MonitorLevel", jsonBody.getJSONArray("MonitorLevel"));
			}
			if (!jsonBody.isNull("Master")) {
				dbSources.put("Master", jsonBody.getJSONObject("Master"));
			}
			if (!jsonBody.isNull("Sources")) {
				source = jsonBody.getJSONArray("Sources").getJSONObject(0);
				if (!dbSources.isNull("Sources")) {
					boolean found = false;
					JSONArray srcArray = dbSources.getJSONArray("Sources");
					int size = srcArray.length();
					for (int i = 0; i < size; i++) {
						JSONObject src = srcArray.getJSONObject(i);
						if (src.getString("ID").equalsIgnoreCase(source.getString("ID"))) {
							srcArray.put(i, source);
							found = true;
							break;
						}
					}
					if (!found) {
						srcArray.put(source);
					}
				} else {
					JSONArray arr = new JSONArray();
					arr.put(source);
					dbSources.put("Sources", arr);
				}
			}
		}
		return dbSources;
	}

	private JSONArray convertGainValues(JSONArray advanceMute, JSONArray advanceOutput, JSONArray advanceGain) {

		int size = advanceGain.length();
		if (advanceOutput != null) {
			for (int i = 0; i < size; i++) {
				boolean output = advanceOutput.getBoolean(i);
				if (!output) {
					advanceGain.put(i, 0);
				}
			}
		}
		for (int i = 0; i < size; i++) {
			boolean mute = advanceMute.getBoolean(i);
			if (mute) {
				advanceGain.put(i, 0);
			}
		}
		return advanceGain;

	}

	private boolean sourceExistInDB(JSONObject dbSource, JSONObject source) {
		boolean exist = false;
		if (dbSource == null) {
			return false;
		}

		if (!dbSource.isNull("Sources")) {
			JSONArray arrSrc = dbSource.getJSONArray("Sources");
			int size = arrSrc.length();
			for (int i = 0; i < size; i++) {
				JSONObject src = arrSrc.getJSONObject(i);
				if (src.getString("ID").equalsIgnoreCase(source.getString("ID"))) {
					exist = true;
					break;
				}
			}
		}

		return exist;
	}

}