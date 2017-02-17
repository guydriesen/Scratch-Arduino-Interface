package sai.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class Sensor {

	@Getter private final String name;
	@Getter private final String value;

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(name).append(" ").append(value).append("\n");
		return sb.toString();
	}

}
