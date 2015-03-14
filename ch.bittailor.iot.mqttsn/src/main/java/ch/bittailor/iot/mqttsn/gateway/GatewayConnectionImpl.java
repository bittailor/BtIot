package ch.bittailor.iot.mqttsn.gateway;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.bittailor.iot.core.wsn.RfSocketAddress;
import ch.bittailor.iot.mqttsn.messages.Connack;
import ch.bittailor.iot.mqttsn.messages.Connect;
import ch.bittailor.iot.mqttsn.messages.Disconnect;
import ch.bittailor.iot.mqttsn.messages.Message;
import ch.bittailor.iot.mqttsn.messages.MessageVisitor;
import ch.bittailor.iot.mqttsn.messages.Publish;
import ch.bittailor.iot.mqttsn.messages.Regack;
import ch.bittailor.iot.mqttsn.messages.Register;
import ch.bittailor.iot.mqttsn.messages.Suback;
import ch.bittailor.iot.mqttsn.messages.Subscribe;

public class GatewayConnectionImpl implements GatewayConnection, MessageVisitor {
	private static final Logger LOG = LoggerFactory.getLogger(GatewayConnectionImpl.class);

	private final ExecutorService mExecutorService;
	private final RfSocketAddress mAddress;
	
	public GatewayConnectionImpl(ExecutorService executorService, RfSocketAddress address) {
		super();
		mExecutorService = executorService;
		mAddress = address;
	}
	
	@Override
	public void close() throws IOException {
		mExecutorService.shutdown();
	}

	@Override
	public void handle(final Message message) {
		mExecutorService.execute(new Runnable() {		
			@Override
			public void run() {
				message.accept(GatewayConnectionImpl.this);
			}
		});
	}

	@Override
	public void visit(Connect connect) {
		LOG.debug("handle Connect " + mAddress);	
	}

	@Override
	public void visit(Connack connack) {
		LOG.debug("handle Connack " + mAddress);	
		
	}

	@Override
	public void visit(Register register) {
		LOG.debug("handle Register " + mAddress);
		
	}

	@Override
	public void visit(Regack regack) {
		LOG.debug("handle Regack " + mAddress);
		
	}

	@Override
	public void visit(Publish publish) {
		LOG.debug("handle Publish " + mAddress);
		
	}

	@Override
	public void visit(Disconnect disconnect) {
		LOG.debug("handle Disconnect " + mAddress);
		
	}

	@Override
	public void visit(Subscribe subscribe) {
		LOG.debug("handle Subscribe " + mAddress);
		
	}

	@Override
	public void visit(Suback suback) {
		LOG.debug("handle Suback " + mAddress);
		
	}

}
