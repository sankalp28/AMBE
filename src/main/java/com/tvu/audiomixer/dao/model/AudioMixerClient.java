package com.tvu.audiomixer.dao.model;

import java.util.List;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import lombok.Data;

@Data
@DynamoDBTable(tableName = "audiomixerclient")
public class AudioMixerClient {


	@DynamoDBHashKey
	private String connectionid;
	
	private String taskid;

	public String getTaskid() {
		return taskid;
	}

	public void setTaskid(String taskid) {
		this.taskid = taskid;
	}

	public String getConnectionid() {
		return connectionid;
	}

	public void setConnectionid(String connectionid) {
		this.connectionid = connectionid;
	}

}
