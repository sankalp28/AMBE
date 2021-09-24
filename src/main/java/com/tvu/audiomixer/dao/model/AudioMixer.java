package com.tvu.audiomixer.dao.model;

import java.util.List;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import lombok.Data;

@Data
@DynamoDBTable(tableName = "audiomixertask")
public class AudioMixer {


	@DynamoDBHashKey
	private String taskId;
	private String vmip;
	private String sourcesJSON;
	private String groupsJSON;
	private boolean isDeleted;
	private int taskstatus=1;
	
	private String sourcesRtilJSON;
	private String rtilMeetingInfoJSON;
	private String screenSettings;





	public String getScreenSettings() {
		return screenSettings;
	}

	public void setScreenSettings(String screenSettings) {
		this.screenSettings = screenSettings;
	}

	public String getRtilMeetingInfoJSON() {
		return rtilMeetingInfoJSON;
	}

	public void setRtilMeetingInfoJSON(String rtilMeetingInfoJSON) {
		this.rtilMeetingInfoJSON = rtilMeetingInfoJSON;
	}

	public String getSourcesRtilJSON() {
		return sourcesRtilJSON;
	}

	public void setSourcesRtilJSON(String sourcesRtilJSON) {
		this.sourcesRtilJSON = sourcesRtilJSON;
	}

	public int getTaskstatus() {
		return taskstatus;
	}

	public void setTaskstatus(int taskstatus) {
		this.taskstatus = taskstatus;
	}

	public String getGroupsJSON() {
		return groupsJSON;
	}

	public void setGroupsJSON(String groupsJSON) {
		this.groupsJSON = groupsJSON;
	}

	public String getSourcesJSON() {
		return sourcesJSON;
	}

	public void setSourcesJSON(String sourcesJSON) {
		this.sourcesJSON = sourcesJSON;
	}

	// private List<Source> Sources;
	private Long timestamp;

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public String getVmip() {
		return vmip;
	}

	public void setVmip(String vmip) {
		this.vmip = vmip;
	}
	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}
}
