package sai.dm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.firmata4j.IODevice;
import org.firmata4j.Pin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.annotation.WithStateMachine;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Synchronized;
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

	@Getter(onMethod=@__({@Override, @Synchronized})) private volatile Set<Integer> busyIds = new HashSet<>();
	@Getter(onMethod=@__({@Override})) private volatile List<String> errors = new ArrayList<>();

	@PostConstruct
	public void init() {
		serial.addSerialPortListener(new SerialPortListener() {

			@Override
			public void portsChanged() {
				stateMachine.sendEvent(Events.PORTS_CHANGED);
			}
		});
	}

	private void clear() {
		busyIds.clear();
		errors.clear();
	}

	@Override
	public void connect(String port) {
		log.info("Connect device...");
		clear();
		stateMachine.sendEvent(MessageBuilder
				.withPayload(Events.CONNECT)
				.setHeader(Variables.PORT.toString(), port)
				.build());
	}

	@Override
	public void disconnect() {
		log.info("Disconnect device...");
		clear();
		stateMachine.sendEvent(Events.DISCONNECT);
	}

	@Override
	public void reset() {
		log.info("Reset device...");
		clear();
		stateMachine.sendEvent(Events.RESET);
	}

	private boolean isConnected() {
		return stateMachine.sendEvent(Events.CONNECTED);
	}

	private boolean notConnected() {
		return !isConnected();
	}

	@Override
	public List<PinValue> getPinValues() {
		if (notConnected()) return null;
		IODevice device = (IODevice) stateMachine.getExtendedState().getVariables().get(Variables.DEVICE);
		if (device == null) return null;
		List<PinValue> pins = new ArrayList<>();
		for (Pin pin : device.getPins()) {
			pins.add(pinValueMapper.map(pin));
		}
		return pins;
	}

	@Override
	public void configurePin(int pinNumber, PinMode pinMode) {
		if (notConnected()) return;
		IODevice device = (IODevice) stateMachine.getExtendedState().getVariables().get(Variables.DEVICE);
		if (device == null) return;
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
				errors.add(0, error);
				log.error(error);
				break;
			}
		} catch (IllegalArgumentException | IOException e) {
			String error = "Failed configuring pin " + pinNumber + " to " + pinMode;
			errors.add(0, error);
			log.error(error, e);
		}
	}

	@Override
	public void configureServoPin(int pinNumber, int minPulse, int maxPulse) {
		if (notConnected()) return;
		IODevice device = (IODevice) stateMachine.getExtendedState().getVariables().get(Variables.DEVICE);
		if (device == null) return;
		try {
			device.getPin(pinNumber).setServoMode(minPulse, maxPulse);
		} catch (IllegalArgumentException | IOException e) {
			String error = "Failed configuring pin " + pinNumber + " to " + PinMode.SERVO
					+ " with minPulse " + minPulse + " & maxPulse " + maxPulse;
			errors.add(0, error);
			log.error(error, e);
		}
	}

	@Override
	public void setDigitalOutput(int pinNumber, boolean value) {
		if (notConnected()) return;
		IODevice device = (IODevice) stateMachine.getExtendedState().getVariables().get(Variables.DEVICE);
		if (device == null) return;
		Pin pin = device.getPin(pinNumber);
		try {
			pin.setValue(value ? 1 : 0);
		} catch (IllegalStateException | IOException e) {
			String error = "Failed setting pin " + pinNumber + " to " + value;
			errors.add(0, error);
			log.error(error, e);
		}
	}

	@Override
	public void setPwmOutput(int pinNumber, int value) {
		if (notConnected()) return;
		IODevice device = (IODevice) stateMachine.getExtendedState().getVariables().get(Variables.DEVICE);
		if (device == null) return;
		Pin pin = device.getPin(pinNumber);
		try {
			pin.setValue(value);
		} catch (IllegalStateException | IOException e) {
			String error = "Failed setting pin " + pinNumber + " to " + value;
			errors.add(0, error);
			log.error(error, e);
		}
	}

	@Override
	public void setServoOutput(int pinNumber, int value) {
		if (notConnected()) return;
		IODevice device = (IODevice) stateMachine.getExtendedState().getVariables().get(Variables.DEVICE);
		if (device == null) return;
		Pin pin = device.getPin(pinNumber);
		try {
			pin.setValue(value);
		} catch (IllegalStateException | IOException e) {
			String error = "Failed setting pin " + pinNumber + " to " + value;
			errors.add(0, error);
			log.error(error, e);
		}
	}

}
