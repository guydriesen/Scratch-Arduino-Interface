package sai.rest.mapper;

import org.springframework.stereotype.Component;

import sai.dto.PinMode;
import sai.dto.PinValue;
import sai.rest.dto.Sensor;

@Component
public class SensorMapper {

	public Sensor map(PinValue pinValue) {
		StringBuilder name = new StringBuilder();
		name.append(pinValue.getMode()).append("/");
		if (pinValue.getMode() == PinMode.ANALOG)
			name.append(pinValue.getPin() % PinValue.getFIRST_ANALOG_PIN());
		else
			name.append(pinValue.getPin());

		String value;
		if (pinValue.getMode() == PinMode.INPUT || pinValue.getMode() == PinMode.OUTPUT)
			value = pinValue.getValue() == 0 ? "false" : "true";
		else
			value = "" + pinValue.getValue();

		return new Sensor(name.toString(), value);
	}

}
