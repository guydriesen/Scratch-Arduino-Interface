package sai.dm.statemachine;

import java.io.IOException;

import org.firmata4j.IODevice;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class Actions {

	public Action<States, Events> idleConnect() {
		return new Action<States, Events>() {

			@Override
			public void execute(StateContext<States, Events> context) {
				Object port = context.getMessageHeader(Variables.PORT);
				if (port != null) {
					context.getExtendedState().getVariables().put(Variables.PORT, port);
					log.info("Using port: " + port);
				}
			}
		};
	}

	public Action<States, Events> disconnect() {
		return new Action<States, Events>() {

			@Override
			public void execute(StateContext<States, Events> context) {
				Object device = context.getExtendedState().getVariables().get(Variables.DEVICE);
				if (device != null && device instanceof IODevice) {
					try {
						((IODevice) device).stop();
					} catch (IOException e) {
						
					}
				}
				context.getExtendedState().getVariables().remove(Variables.DEVICE);
				context.getExtendedState().getVariables().remove(Variables.PORT);
			}
		};
	}

	public Action<States, Events> reset() {
		return new Action<States, Events>() {

			@Override
			public void execute(StateContext<States, Events> context) {
				Object device = context.getExtendedState().getVariables().get(Variables.DEVICE);
				if (device != null && device instanceof IODevice) {
					try {
						((IODevice) device).stop();
					} catch (IOException e) {
						
					}
				}
			}
		};
	}

}
