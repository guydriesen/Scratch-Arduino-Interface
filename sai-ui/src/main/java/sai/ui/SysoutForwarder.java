package sai.ui;

import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

public class SysoutForwarder extends OutputStream {

	private byte [] buffer = new byte [4096];
	private int pos = 0;
	private JTextArea textArea = null;

	public SysoutForwarder(JTextArea textArea) {
		super();
		this.textArea = textArea;
	}

	@Override
	public void write(int b) throws IOException {
		if (textArea != null) {
			// only handle bytes if the text area is available
			buffer[pos++] = (byte) b;
			if (pos == buffer.length || b == '\n') {
				// flush if buffer is full or if a line ending is fed
				String output = new String(buffer, 0, pos);
				textArea.append(output);
				pos = 0;
				try {
					textArea.setCaretPosition(textArea.getLineStartOffset(textArea.getLineCount() - 1));
				} catch (BadLocationException e) {}	// ignore
			}
		}
	}

}
