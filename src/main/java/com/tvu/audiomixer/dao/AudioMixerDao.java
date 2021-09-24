package com.tvu.audiomixer.dao;

import java.util.List;

import com.tvu.audiomixer.dao.model.AudioMixer;
import com.tvu.audiomixer.dao.model.AudioMixerClient;

public interface AudioMixerDao {
	void saveAudioMixer(AudioMixer audioMixer);
	AudioMixer retrieveAudioMixer(String id);
	void saveClientConnection(AudioMixerClient client);
	void deleteClientConnection(AudioMixerClient client);
	List <AudioMixerClient> getClientConnections(String taskid);
}
