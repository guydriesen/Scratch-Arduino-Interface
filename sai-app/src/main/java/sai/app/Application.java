package sai.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import sai.api.DeviceManager;
import sai.api.SerialPortManager;
import sai.ui.MainWindow;

@SpringBootApplication(scanBasePackages = "sai")
public class Application implements CommandLineRunner {

	private static MainWindow main;
	private static ConfigurableApplicationContext context;

	@Autowired private SerialPortManager serial;
	@Autowired private DeviceManager device;

	public static void main(String[] args) {
		main = new MainWindow();
		context = new SpringApplicationBuilder(Application.class).headless(false).run(args);
		context.registerShutdownHook();
	}

	@Override
	public void run(String... arg0) throws Exception {
		main.configure(serial, device);
	}

}
