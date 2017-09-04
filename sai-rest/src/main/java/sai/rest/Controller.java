package sai.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import sai.api.DeviceManager;
import sai.dto.PinMode;
import sai.dto.PinValue;
import sai.rest.converters.PinModeConverter;
import sai.rest.dto.PollResponse;
import sai.rest.mapper.SensorMapper;

@Slf4j
@RestController
public class Controller {

	@Autowired private DeviceManager device;
	@Autowired private SensorMapper SensorMapper;

	@InitBinder
	public void initBinder(WebDataBinder dataBinder) {
		dataBinder.registerCustomEditor(PinMode.class, new PinModeConverter());
	}

	@RequestMapping(value = "crossdomain.xml")
	public String policy() {
		// Send the Flash null-teriminated cross-domain policy.
		String policy = "<cross-domain-policy>\n" + "  <allow-access-from domain=\"*\" to-ports=\"" + 8080
				+ "\"/>\n" + "</cross-domain-policy>\n\0";
		return policy;
	}

	@RequestMapping(value = "/reset_all")
	public void resetAll() {
		device.reset();
	}

	@RequestMapping(value = "/poll", produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<PollResponse> poll() {
		PollResponse resp = new PollResponse();
		List<PinValue> pinValues = device.getPinValues();

		if (pinValues == null) // Board not connected
			return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(null);

		for (PinValue pinValue : pinValues)
			resp.getSensors().add(SensorMapper.map(pinValue));

		resp.getBusyIds().addAll(device.getBusyIds());
		resp.getErrors().addAll(device.getErrors());

		return ResponseEntity.ok(resp);
	}

	@RequestMapping(value = "/waitConnected/{1}")
	public void waitConnected(@PathVariable(value = "1") int busyId) {
		log.debug("Wait until connected, id: " + busyId);
		/* Nothing to do.
		 * As long as the arduino board is not connected an error is
		 * returned on /poll. When the arduino is ready, /poll response
		 * changes to 200 OK (with sensor values) and Scratch can 
		 * continue.
		 */
	}

	@RequestMapping(value = "/pinMode/{1}/{2}")
	public void pinMode(@PathVariable(value = "1") int pinNumber, @PathVariable(value = "2") PinMode mode) {
		log.debug("Configure pin " + pinNumber + " as " + mode);
		device.configurePin(pinNumber, mode);
	}

	@RequestMapping(value = "/pinModeInput/{1}")
	public void pinModeInput(@PathVariable(value = "1") int pinNumber) {
		log.debug("Configure pin " + pinNumber + " as digital input");
		device.configurePin(pinNumber, PinMode.INPUT);
	}

	@RequestMapping(value = "/pinModeOutput/{1}")
	public void pinModeOutput(@PathVariable(value = "1") int pinNumber) {
		log.debug("Configure pin " + pinNumber + " as digital output");
		device.configurePin(pinNumber, PinMode.OUTPUT);
	}

	@RequestMapping(value = "/pinModePwm/{1}")
	public void pinModePwm(@PathVariable(value = "1") int pinNumber) {
		log.debug("Configure pin " + pinNumber + " as pwm");
		device.configurePin(pinNumber, PinMode.PWM);
	}

	@RequestMapping(value = "/pinModeServo/{1}/{2}/{3}")
	public void pinModeServo(@PathVariable(value = "1") int pinNumber, @PathVariable(value = "2") int minPulse, @PathVariable(value = "3") int maxPulse) {
		log.debug("Configure pin " + pinNumber + " as servo with minPulse " + minPulse + " & maxPulse " + maxPulse);
		device.configureServoPin(pinNumber, minPulse, maxPulse);
	}

	@RequestMapping(value = "/setOutputPin/{1}/{2}")
	public void setOutputPin(@PathVariable(value = "1") int pinNumber, @PathVariable(value = "2") boolean value) {
		log.debug("Set output pin " + pinNumber + " to " + value);
		device.setDigitalOutput(pinNumber, value);
	}

	@RequestMapping(value = "/setPwmPin/{1}/{2}")
	public void setPwmPin(@PathVariable(value = "1") int pinNumber, @PathVariable(value = "2") int value) {
		log.debug("Set pwm pin " + pinNumber + " to " + value);
		device.setPwmOutput(pinNumber, value);
	}

	@RequestMapping(value = "/setServoPin/{1}/{2}")
	public void setServoPin(@PathVariable(value = "1") int pinNumber, @PathVariable(value = "2") int value) {
		log.debug("Set servo pin " + pinNumber + " to " + value);
		device.setServoOutput(pinNumber, value);
	}

}
