package org.sonar.Jlin.java.ApiCompatibility;

import java.util.Properties;


/**
 * central interface to write messages into different output formats. 
 * @see com.sap.netweaver.compatibility.eclipse2jlin.MessageWriterFactory or a derived 
 * class for creating instances of this interface. 
 * @author d034003
 *
 */
public interface IMessageWriter {
	/**
	 * Create an output stream and write a header (important for XML files)
	 */
	public void writeHeader();	
	
	/**
	 * write one message into the stream. From API Compatibility
	 * @param oneMessage
	 */
	public void writeData(MessageFlags.Data oneMessage);	
	
	/**
	 * write one message from "DoubledClasses" into the stream
	 */
	public void writeData(String className,String message, String messageKey, int prio, String csn,Properties values);
	
	/**
	 * Write a footer and close the output stream.
	 */
	public void writeFooter();

	public int getMessageCount();
	
	public static abstract class DefaultImpl implements IMessageWriter{
		private int msgCount;
		public DefaultImpl(){
			msgCount = 0;
		}
		public int getMessageCount(){
			return msgCount;
		}

		public abstract void writeData1(MessageFlags.Data oneMessage);
		public final void writeData(MessageFlags.Data oneMessage){
			msgCount++;
			writeData1(oneMessage);
		}

		public abstract void writeData1(String className,String message, String messageKey, int prio, String csn,Properties values);
		public final void writeData(String className,String message, String messageKey, int prio, String csn,Properties values){
			msgCount++;
			writeData1(className, message, messageKey, prio, csn, values);
		}

	}
	
}
