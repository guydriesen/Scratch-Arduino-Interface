package sai.dm.statemachine;

import java.io.IOException;
import java.util.Arrays;

import org.firmata4j.IODevice;
import org.firmata4j.firmata.FirmataDevice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.guard.Guard;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import sai.api.SerialPortManager;

@Slf4j
@Component
public class Guards {

	@Autowired private SerialPortManager serial;

	public Guard<States, Events> connected() {
		return new Guard<States, Events>() {

			@Override
			public boolean evaluate(StateContext<States, Events> context) {
				Object port = context.getExtendedState().getVariables().get(Variables.PORT);
				log.info("Connecting to " + port + "...");
				if (port != null) {
					IODevice device = new FirmataDevice((String) port);
					try {
						device.start();
						device.ensureInitializationIsDone();
					} catch (IOException e) {
						log.error("Cannot start device.", e);
						return false;
					} catch (InterruptedException e) {
						log.error("Timeout...", e);
						try {
							device.stop();
						} catch (IOException e2) {
							log.error("Cannot stop device.", e2);
						}
						return false;
					}
					context.getExtendedState().getVariables().put(Variables.DEVICE, device);
					return true;
				}
				return false;
			}
		};
	}

	public Guard<States, Events> portMissingChanged() {
		return new Guard<States, Events>() {

			@Override
			public boolean evaluate(StateContext<States, Events> context) {
				Object port = context.getExtendedState().getVariables().get(Variables.PORT);
				if (port == null) return false;
				return Arrays.asList(serial.getSerialPorts()).contains(port);
			}
		};
	}

	public Guard<States, Events> connectChanged() {
		return new Guard<States, Events>() {

			@Override
			public boolean evaluate(StateContext<States, Events> context) {
				Object port = context.getExtendedState().getVariables().get(Variables.PORT);
				if (port == null) return false;
				if (Arrays.asList(serial.getSerialPorts()).contains(port)) return false;
				Object device = context.getExtendedState().getVariables().get(Variables.DEVICE);
				if (device != null && device instanceof IODevice) {
					try {
						((IODevice) device).stop();
					} catch (IOException e) {
						
					}
				}
				context.getExtendedState().getVariables().remove(Variables.DEVICE);
				return true;
			}
		};
	}

}
