package com.tvu.audiomixer.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.tvu.audiomixer.dao.model.AudioMixer;
import com.tvu.audiomixer.dao.model.AudioMixerClient;

public class AudioMixerDaoImpl extends BasicDao implements AudioMixerDao{
	@Override
	public void saveAudioMixer(AudioMixer audioMixer) {
		getDynamoDBMapper().save(audioMixer);
	}
	@Override
	public AudioMixer retrieveAudioMixer(String id) {
		return getDynamoDBMapper().load(AudioMixer.class, id);
	}
	@Override
	public void saveClientConnection(AudioMixerClient client) {
		getDynamoDBMapper().save(client);
	}
	
	@Override
	public void deleteClientConnection(AudioMixerClient client) {
		getDynamoDBMapper().delete(client);	
	}
	@Override
	public List<AudioMixerClient> getClientConnections(String taskid){
		
		Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
        eav.put(":val1", new AttributeValue().withS(taskid));
        
		DynamoDBScanExpression scanExpression = new DynamoDBScanExpression().withFilterExpression("taskid=:val1").withExpressionAttributeValues(eav);
		return getDynamoDBMapper().scan(AudioMixerClient.class, scanExpression);
	}
}
