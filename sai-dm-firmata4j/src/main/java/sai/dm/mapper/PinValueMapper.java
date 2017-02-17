package sai.dm.mapper;

import org.firmata4j.Pin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import sai.dto.PinValue;

@Component
public class PinValueMapper {

	@Autowired private PinModeMapper pinModeMapper;

	public PinValue map(Pin pin) {
		PinValue pinValue = new PinValue();
		pinValue.setMode(pinModeMapper.map(pin.getMode()));
		pinValue.setValue(pin.getValue());
		pinValue.setPin(pin.getIndex());
		return pinValue;
	}

}
