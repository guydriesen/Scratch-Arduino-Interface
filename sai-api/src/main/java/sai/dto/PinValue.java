package sai.dto;

import lombok.Getter;
import lombok.Setter;

public class PinValue {

	@Getter private static final int FIRST_ANALOG_PIN = 14;

	@Getter @Setter private int pin;
	@Getter @Setter private PinMode mode;
	@Getter @Setter private long value;

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(mode).append("/");
		if (mode == PinMode.ANALOG)
			sb.append(pin % FIRST_ANALOG_PIN);
		else
			sb.append(pin);
		sb.append(" ");
		if (mode == PinMode.INPUT || mode == PinMode.OUTPUT)
			sb.append(value == 0 ? "false" : "true");
		else
			sb.append(value);
		sb.append("\n");
		return sb.toString();
	}

}
