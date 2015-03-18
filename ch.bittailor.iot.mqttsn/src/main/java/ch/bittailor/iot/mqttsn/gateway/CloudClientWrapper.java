package ch.bittailor.iot.mqttsn.gateway;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.kura.KuraException;
import org.eclipse.kura.cloud.CloudClient;
import org.eclipse.kura.cloud.CloudClientListener;
import org.eclipse.kura.message.KuraPayload;

import ch.bittailor.iot.core.mqttsn.gateway.MqttClient;

public class CloudClientWrapper implements MqttClient {
	private static final int PRIO = 5;
	
	private final CloudClient mCloudClient;
	private final List<String> mSubscriptions;
	private CloudClientListenerWrapper mCurrentCloudClientListener;

	public CloudClientWrapper(CloudClient cloudClient) {
		mCloudClient = cloudClient;
		mSubscriptions = new LinkedList<String>();
	}

	@Override
	public void setCallback(Callback callback) {
		if(mCurrentCloudClientListener != null) {
			mCloudClient.removeCloudClientListener(mCurrentCloudClientListener);
			mCurrentCloudClientListener = null;
		}
		
		if(callback == null) {
			return;
		}
		
		mCurrentCloudClientListener = new CloudClientListenerWrapper(callback);
		mCloudClient.addCloudClientListener(mCurrentCloudClientListener);		
	}

	@Override
	public void close() throws Exception {
		for (String subscription : mSubscriptions) {
			mCloudClient.unsubscribe(subscription);
		}
		mCloudClient.release();
	}

	@Override
	public void publish(String appTopic, byte[] payload, int qos, boolean retain) {
		try {
			mCloudClient.publish(appTopic, payload, qos, retain, PRIO);
		} catch (KuraException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void subscribe(String topic, int qos) {
		try {
			mCloudClient.subscribe(topic, qos);
		} catch (KuraException e) {
			throw new RuntimeException(e);
		}
		mSubscriptions.add(topic);
	}
	
	private static class CloudClientListenerWrapper implements CloudClientListener {
		private final Callback mCallback;

		public CloudClientListenerWrapper(Callback callback) {
			mCallback = callback;
		}

		@Override
		public void onConnectionEstablished() {
		}

		@Override
		public void onConnectionLost() {
		}

		@Override
		public void onControlMessageArrived(String deviceId, String appTopic, KuraPayload msg, int qos, boolean retain) {
			mCallback.messageArrived(appTopic, msg.getBody(), qos, retain);
		}

		@Override
		public void onMessageArrived(String deviceId, String appTopic, KuraPayload msg, int qos, boolean retain) {
			mCallback.messageArrived(appTopic, msg.getBody(), qos, retain);
		}

		@Override
		public void onMessageConfirmed(int messageId, String appTopic) {
		}

		@Override
		public void onMessagePublished(int messageId, String appTopic) {
		}
		
	}
	
	

}
