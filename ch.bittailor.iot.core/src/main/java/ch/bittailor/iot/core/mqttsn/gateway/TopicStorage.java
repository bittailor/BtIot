package ch.bittailor.iot.core.mqttsn.gateway;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TopicStorage {

	public static final int NO_TOPIC_ID = 0x0000;

	private final List<String> mIdToName;
	private final Map<String, Integer> mNameToId;

	public TopicStorage() {
		mIdToName = new ArrayList<String>();
		mNameToId = new HashMap<String, Integer>();
		mIdToName.add("0x0000 == RESERVED");
	}

	public synchronized int getOrCreateTopicId(String topicName) {
		Integer topicId = mNameToId.get(topicName);
		if(topicId != null) {
			return topicId;
		}

		Integer newId = mIdToName.size();
		mIdToName.add(topicName);
		mNameToId.put(topicName, newId);
		return newId;
	}

	public synchronized int getTopicId(String topicName) {
		Integer topicId = mNameToId.get(topicName);   
		if(topicId != null) {
			return topicId;
		}
		return NO_TOPIC_ID;
	}

	public synchronized String getTopicName(int topicId) {
		if (topicId >= mIdToName.size()) {
			return null;
		}
		return mIdToName.get(topicId);
	}
	
	public void clear() {
	   mIdToName.clear();
	   mIdToName.add("0x0000 == RESERVED");
	   mNameToId.clear();
	}

}
