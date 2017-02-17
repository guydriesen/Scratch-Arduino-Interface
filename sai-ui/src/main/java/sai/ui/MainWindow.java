package sai.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import lombok.extern.slf4j.Slf4j;
import sai.api.DeviceManager;
import sai.api.SerialPortListener;
import sai.api.SerialPortManager;

@Slf4j
public class MainWindow extends JFrame {

	private static final long serialVersionUID = -1145787032871276612L;
	private static final ResourceBundle labels = ResourceBundle.getBundle("ui/labels");
	private static final String scratchExtensionFileName = "ScratchArduinoInterface.s2e";
	private static final String scratchExtensionResource = "scratch/ScratchArduinoInterface.s2e";

	private JButton connectButton = new JButton(labels.getString("connect.button.connect"));
	private JButton saveButton = new JButton(labels.getString("save.button"));
	private JComboBox<String> portsComboBox = new JComboBox<>();
	private Boolean connected = false;
	private MainWindow self = this;

	public MainWindow() {
		super(labels.getString("title"));

		// layout
		int gap = 5;
		setLayout(new BorderLayout(gap, gap));

		// input panel
		JPanel inputPanel = new JPanel(new BorderLayout(gap, gap));
		inputPanel.setBorder(BorderFactory.createEmptyBorder(gap, gap, 0, gap));
		add(inputPanel, BorderLayout.NORTH);
		// labels
		JPanel panel = new JPanel(new GridLayout(1, 1));
		panel.add(new JLabel(labels.getString("port.label"), JLabel.RIGHT));
		inputPanel.add(panel, BorderLayout.WEST);
		// fields
		panel = new JPanel(new GridLayout(1, 1));
		panel.add(portsComboBox);
		inputPanel.add(panel, BorderLayout.CENTER);
		// buttons
		panel = new JPanel(new GridLayout(1, 1));
		panel.add(connectButton);
		inputPanel.add(panel, BorderLayout.EAST);

		// log area
		JTextArea logArea = new JTextArea();
		logArea.setFont(new Font("monospaced", Font.PLAIN, 10));
		add(new JScrollPane(logArea), BorderLayout.CENTER);

		// save extension button
		panel = new JPanel(new FlowLayout(FlowLayout.CENTER, gap, gap));
		panel.add(saveButton);
		add(panel, BorderLayout.SOUTH);

		// disable controls during init
		portsComboBox.setEnabled(false);
		connectButton.setEnabled(false);
		saveButton.setEnabled(false);

		// redirect sysout to the textarea
		SysoutForwarder forwarder = new SysoutForwarder(logArea);
		System.setOut(new PrintStream(forwarder));
		System.setErr(new PrintStream(forwarder));

		// close action
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		// show the window
		getContentPane().setPreferredSize(new Dimension(800, 300));
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	public void configure(SerialPortManager serial, DeviceManager device) {
		// update close action
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (connected) device.disconnect();
				System.exit(0);
			}
		});

		// serial port list
		DefaultComboBoxModel<String> ports = new DefaultComboBoxModel<>(serial.getSerialPorts());
		portsComboBox.setModel(ports);
		portsComboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				if (event.getStateChange() == ItemEvent.SELECTED) {
					updateDeviceControls();
				}
			}
		});

		serial.addSerialPortListener(new SerialPortListener() {
			@Override
			public void portsChanged() {
				// don't update list when connected (user selection should not change)
				if (connected)
					return;
				updateSerialPorts(ports, serial);
			}
		});

		connectButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (connected) {
					disconnect(device);
					// refresh device list as it might have changed
					updateSerialPorts(ports, serial);;
				} else {
					connect(device, portsComboBox.getSelectedItem().toString());
				}
			}
		});

		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				writeScratchExtensionFile();
			}
		});
		saveButton.setEnabled(true);

		updateDeviceControls();
	}

	private void updateSerialPorts(DefaultComboBoxModel<String> ports, SerialPortManager serial) {
		ports.removeAllElements();
		for (String port : serial.getSerialPorts())
			ports.addElement(port);
		updateDeviceControls();
	}

	private void updateDeviceControls() {
		portsComboBox.setEnabled(portsComboBox.getItemCount() != 0 && !connected);
		connectButton.setEnabled(portsComboBox.getItemCount() != 0 && !portsComboBox.getSelectedItem().toString().isEmpty());
	}

	private void connect(DeviceManager device, String port) {
		log.info("Connecting to: " + port);
		device.connect(port);
		connected = true;
		connectButton.setText(labels.getString("connect.button.disconnect"));
		portsComboBox.setEnabled(false);
	}

	private void disconnect(DeviceManager device) {
		device.disconnect();
		connected = false;
		connectButton.setText(labels.getString("connect.button.connect"));
		portsComboBox.setEnabled(true);
	}

	private void writeScratchExtensionFile() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setSelectedFile(new File(scratchExtensionFileName));
		fileChooser.setFileFilter(new S2eFileFilter());
		fileChooser.setDialogTitle(labels.getString("save.dialog.title"));
		int response = fileChooser.showSaveDialog(self);
		if (JFileChooser.CANCEL_OPTION == response) {
			return;
		}

		File selectedFile = fileChooser.getSelectedFile();
		final File file;
		if (selectedFile.getName().endsWith(S2eFileFilter.FILE_EXTENSION)) {
			file = selectedFile;
		} else {
			file = new File(selectedFile + S2eFileFilter.FILE_EXTENSION);
		}

		if (file.exists()) {
			String message = MessageFormat.format(labels.getString("save.file_exist.message"), selectedFile.getName());
			String title = labels.getString("save.file_exist.title");
			response = JOptionPane.showConfirmDialog(self, message, title, JOptionPane.YES_NO_OPTION);
			if (JOptionPane.NO_OPTION == response) {
				return;
			}
		}

		try {
			InputStream is = null;
			OutputStream os = null;
			try {
				is = this.getClass().getClassLoader().getResourceAsStream(scratchExtensionResource);
				os = new FileOutputStream(file);
				byte[] buffer = new byte[1024];
				int length;
				while ((length = is.read(buffer)) > 0) {
					os.write(buffer, 0, length);
				}
			} finally {
				is.close();
				os.close();
			}
		} catch (IOException e) {
			log.error("Failed writing Scatch Extension to" + file.getAbsolutePath());
			log.debug("", e);
		}
	}

}
