package sai.api;

import java.util.List;

import sai.dto.PinMode;
import sai.dto.PinValue;

public interface DeviceManager {

	public void connect(String port);

	public void disconnect();

	public void reset();

	public String getError();

	public List<PinValue> getPinValues();

	public void configurePin(int pinNumber, PinMode mode);

	public void setDigitalOutput(int pinNumber, boolean value);

	public void setPwmOutput(int pinNumber, int value);

	public void setServoOutput(int pinNumber, int value);

}
