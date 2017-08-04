package replica2.utilities;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class EventLogger {
	private Logger clientLogger;

	private String queuedMessage = "";
	private static EventLogger fileHandlerObject = null;
	String fileName = "";

	public EventLogger(String FileName) {
		this.fileName = FileName;
		clientLogger = Logger.getLogger(FileName);

	}

	public synchronized void setMessage(String Message) {
		this.queuedMessage = Message;
		FileHandler eventFile;

		try {

			// This block configure the logger with handler and formatter
			eventFile = new FileHandler(fileName + ".log", true);
			clientLogger.addHandler(eventFile);
			SimpleFormatter formatter = new SimpleFormatter();
			eventFile.setFormatter(formatter);

			clientLogger.setUseParentHandlers(false);// To remove the console
														// handler, use
			synchronized (this) {
				// the following statement is used to log any messages
				clientLogger.info(this.getMessage());// All methods on Logger
														// are
														// multi-thread safe.
			}
			eventFile.close();

		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public String getMessage() {
		return queuedMessage;
	}

}
