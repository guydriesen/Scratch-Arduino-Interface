package sai.api;

public interface SerialPortManager {

	public String[] getSerialPorts();

	public void addSerialPortListener(SerialPortListener listener);

}
