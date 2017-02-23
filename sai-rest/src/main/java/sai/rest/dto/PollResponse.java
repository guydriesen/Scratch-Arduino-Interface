package sai.rest.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

public class PollResponse {

	@Getter List<Sensor> sensors = new ArrayList<Sensor>();
	@Getter List<Integer> busyIds = new ArrayList<Integer>();
	@Getter List<String> errors = new ArrayList<String>();

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Sensor sensor : sensors)
			sb.append(sensor);
		if (!busyIds.isEmpty()) {
			sb.append("_busy");
			for (int busy : busyIds)
				sb.append(" ").append(busy);
			sb.append("\n");
		}
		if (!errors.isEmpty()) {
			sb.append("_problem");
			for (String error : errors)
				sb.append(" ").append(error);
			sb.append("\n");
		}
		return sb.toString();
	}

}
