package com.tvu.audiomixer.meeting.token;

public class AgoraTokenGenerator {

	public String getMeetingToken(int uid, String appId,String channelId, String appCertificate, int expirationTimeInSeconds) throws Exception {
		String result;
		//String appId = "970CA35de60c44645bbae8a215061b33";
		//String appCertificate = "5CFd2fd1755d40ecb72977518be15d3b";
		//String channelName = "7d72365eb983485397e3e3f9d460bdda";
		// String userAccount = "2082341273";
		// uid = 2082341273;
		//int expirationTimeInSeconds = 86400;  //24 hours
		RtcTokenBuilder token = new RtcTokenBuilder();
		int timestamp = (int) (System.currentTimeMillis() / 1000 + expirationTimeInSeconds);
		// String result = token.buildTokenWithUserAccount(appId, appCertificate,
		// channelName, userAccount,
		// Role.Role_Publisher, timestamp);
		// System.out.println(result);

		result = token.buildTokenWithUid(appId, appCertificate, channelId, uid, Role.Role_Admin, timestamp);
		//System.out.println(result);
		return result;
	}

}
