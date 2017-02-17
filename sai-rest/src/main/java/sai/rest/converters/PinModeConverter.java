package sai.rest.converters;

import java.beans.PropertyEditorSupport;

import sai.dto.PinMode;

public class PinModeConverter extends PropertyEditorSupport {

	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		setValue(PinMode.valueOf(text.toUpperCase()));
	}

}
