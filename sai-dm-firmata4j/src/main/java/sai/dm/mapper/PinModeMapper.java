package sai.dm.mapper;

import org.firmata4j.Pin.Mode;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import sai.dto.PinMode;

@Slf4j
@Component
public class PinModeMapper {

	public PinMode map(Mode mode) {
		if (mode == null) return PinMode.UNSUPPORTED;
		switch (mode) {
		case ANALOG:
			return PinMode.ANALOG;
		case INPUT:
			return PinMode.INPUT;
		case OUTPUT:
			return PinMode.OUTPUT;
		case PWM:
			return PinMode.PWM;
		case SERVO:
			return PinMode.SERVO;
		default:
			log.error("Pin mode \"" + mode + "\" not supported!");
			return PinMode.UNSUPPORTED;
		}
	}

	public Mode map(PinMode pinMode) {
		if (pinMode == null) return Mode.UNSUPPORTED;
		switch (pinMode) {
		case ANALOG:
			return Mode.ANALOG;
		case INPUT:
			return Mode.INPUT;
		case OUTPUT:
			return Mode.OUTPUT;
		case PWM:
			return Mode.PWM;
		case SERVO:
			return Mode.SERVO;
		default:
			log.error("Pin mode \"" + pinMode + "\" not supported!");
			return Mode.UNSUPPORTED;
		}
	}

}
