package sai.serial;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.stereotype.Component;

import jssc.SerialPortList;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import sai.api.SerialPortListener;
import sai.api.SerialPortManager;

@Slf4j
@Component
public class SerialPortManagerImpl extends Thread implements SerialPortManager {

	private volatile boolean running;
	@Getter private String[] serialPorts = new String[] {};

	@PostConstruct
	public void init() {
		setName("spm");
		running = true;
		this.start();
	}

	@PreDestroy
	public void destroy() {
		running = false;
	}

	private List<SerialPortListener> listeners = new ArrayList<>();

	@Override
	public void addSerialPortListener(SerialPortListener listener) {
		listeners.add(listener);
	}

	private void notifyListeners() {
		for (SerialPortListener listener : listeners) {
			listener.portsChanged();
		}
	}

	@Override
	public void run() {
		log.info("SerialPortManager: starting...");
		while (running) { // never stop processing
			List<String> ports = new ArrayList<>();
			boolean[] portInList = new boolean[this.serialPorts.length];
			StringBuilder availablePorts = new StringBuilder().append("Available ports:");
			for (String portName : SerialPortList.getPortNames()) {
					availablePorts.append(portName);
					ports.add(portName);
					int index = Arrays.asList(serialPorts).indexOf(portName);
					if (index != -1 )
						portInList[index] = true;
			}
			log.debug(availablePorts.toString());

			if (ports.size() != this.serialPorts.length || Arrays.asList(portInList).contains(false)) {
				Collections.sort(ports);
				serialPorts = ports.toArray(new String[] {});
				notifyListeners();
			}

			// wait some seconds
			int sleepTime = 5;
			try {
				Thread.sleep(sleepTime * 1000);
			} catch (InterruptedException e) {
			} // don't care if I get interrupted
		}
		log.info("SerialPortManager: exiting...");
	}

}
