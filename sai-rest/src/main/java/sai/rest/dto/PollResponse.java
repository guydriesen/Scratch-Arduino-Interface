package sai.rest.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class PollResponse {

	@Getter List<Sensor> sensors = new ArrayList<Sensor>();
	@Getter @Setter String error;
	@Getter List<Integer> busyLines = new ArrayList<Integer>();

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Sensor sensor : sensors)
			sb.append(sensor);
		if (error != null && !error.isEmpty())
			sb.append("_problem ").append(error).append("\n");
		if (!busyLines.isEmpty()) {
			sb.append("_busy");
			for (int busyLine : busyLines)
				sb.append(" ").append(busyLine);
			sb.append("\n");
		}
		return sb.toString();
	}

}
