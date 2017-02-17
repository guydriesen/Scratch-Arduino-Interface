package sai.dm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.firmata4j.IODevice;
import org.firmata4j.Pin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.annotation.WithStateMachine;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import sai.api.DeviceManager;
import sai.api.SerialPortListener;
import sai.api.SerialPortManager;
import sai.dm.mapper.PinModeMapper;
import sai.dm.mapper.PinValueMapper;
import sai.dm.statemachine.Events;
import sai.dm.statemachine.States;
import sai.dm.statemachine.Variables;
import sai.dto.PinMode;
import sai.dto.PinValue;

@Slf4j
@Component
@WithStateMachine
public class DeviceManagerImpl implements DeviceManager {

	@Autowired private StateMachine<States, Events> stateMachine;
	@Autowired private SerialPortManager serial;
	@Autowired private PinValueMapper pinValueMapper;
	@Autowired private PinModeMapper pinModeMapper;

	@Getter private String error;

	private void appendError(final String error) {
		this.error = this.error == null ? error : this.error + "; " + error;
	}

	@PostConstruct
	public void init() {
		serial.addSerialPortListener(new SerialPortListener() {

			@Override
			public void portsChanged() {
				stateMachine.sendEvent(Events.PORTS_CHANGED);
			}
		});
	}

	@Override
	public void connect(String port) {
		stateMachine.sendEvent(MessageBuilder
				.withPayload(Events.CONNECT)
				.setHeader(Variables.PORT.toString(), port)
				.build());
	}

	@Override
	public void disconnect() {
		stateMachine.sendEvent(Events.DISCONNECT);
	}

	@Override
	public void reset() {
		log.info("Reseting...");
		error = null;
		stateMachine.sendEvent(Events.RESET);
	}

	private boolean notConnected() {
		return !stateMachine.sendEvent(Events.CONNECTED);
	}

	@Override
	public List<PinValue> getPinValues() {
		IODevice device = (IODevice) stateMachine.getExtendedState().getVariables().get(Variables.DEVICE);
		if (notConnected()) return null;
		List<PinValue> pins = new ArrayList<>();
		for (Pin pin : device.getPins()) {
			pins.add(pinValueMapper.map(pin));
		}
		return pins;
	}

	@Override
	public void configurePin(int pinNumber, PinMode pinMode) {
		IODevice device = (IODevice) stateMachine.getExtendedState().getVariables().get(Variables.DEVICE);
		if (notConnected()) return;
		Pin.Mode mode = pinModeMapper.map(pinMode);
		try {
			switch (mode) {
			case INPUT:
			case OUTPUT:
			case PWM:
			case SERVO:
				device.getPin(pinNumber).setMode(mode);
				break;
			default:
				String error = "Unsupported pin mode: " + pinMode;
				appendError(error);
				log.error(error);
				break;
			}
		} catch (IllegalArgumentException | IOException e) {
			String error = "Failed configuring pin " + pinNumber + " to " + pinMode;
			appendError(error);
			log.error(error, e);
		}
	}

	@Override
	public void setDigitalOutput(int pinNumber, boolean value) {
		IODevice device = (IODevice) stateMachine.getExtendedState().getVariables().get(Variables.DEVICE);
		if (notConnected()) return;
		Pin pin = device.getPin(pinNumber);
		try {
			pin.setValue(value ? 1 : 0);
		} catch (IllegalStateException | IOException e) {
			error = "Failed setting pin " + pinNumber + " to " + value;
			log.error(error, e);
		}
	}

	@Override
	public void setPwmOutput(int pinNumber, int value) {
		IODevice device = (IODevice) stateMachine.getExtendedState().getVariables().get(Variables.DEVICE);
		if (notConnected()) return;
		Pin pin = device.getPin(pinNumber);
		try {
			pin.setValue(value);
		} catch (IllegalStateException | IOException e) {
			error = "Failed setting pin " + pinNumber + " to " + value;
			log.error(error, e);
		}
	}

	@Override
	public void setServoOutput(int pinNumber, int value) {
		IODevice device = (IODevice) stateMachine.getExtendedState().getVariables().get(Variables.DEVICE);
		if (notConnected()) return;
		Pin pin = device.getPin(pinNumber);
		try {
			pin.setValue(value);
		} catch (IllegalStateException | IOException e) {
			error = "Failed setting pin " + pinNumber + " to " + value;
			log.error(error, e);
		}
	}

}
