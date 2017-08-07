package org.sonar.Jlin.java.ApiCompatibility;

import java.util.Properties;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * this handler is the callback for parsing the XML file from Eclipse API tools.
 * It reads a eclipse message, converts it into something that is ready for writing
 * and triggers writing of one message.
 * DONE: As workaround for Eclipse internal error, only parts of APIs are compared at a time. This results in lots of "missing class"
 * warnings. Add switch to suppress generation of "missing type" warnings. 
 *
 * @author d034003
 *
 */
public class HandlerEclipse2JLin extends DefaultHandler { 
	private boolean debug = false;
	private int messageCounter;
	private MessageDataLocal oneMessage=null;
	//private IMessageWriter writer;
	private MessageFlags converter;
	private Properties knownErrors;
	private boolean verbose = false;
	
	/**
	 * 
	 * @param debug: if true, parsing is aborted after 100 messages, speeding up debug cycles.
	 * @param writer: the instance used to write each message after it is parsed.
	 */
	public HandlerEclipse2JLin(boolean debug,  MessageFlags converter, Properties knownErrors, boolean verbose) {
		this.debug = debug;
		oneMessage = null;
		//this.writer = writer;
		this.converter = converter;
		this.knownErrors = knownErrors;
		this.verbose = verbose;
		messageCounter = 0;
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attr)throws SAXException{
		super.startElement(uri, localName, qName, attr);
		if(debug){
			if(messageCounter>=100){
				return;
			}
		}
		if("delta".equals(qName)){
			messageCounter++;
			if (oneMessage!=null){
				throw new RuntimeException("something strange, nested message? ");
			}
			oneMessage=new MessageDataLocal();
			oneMessage.init(attr);
		}
		else{
			if ("message_arguments".equals(qName)){
				oneMessage.setArguments(attr);
			}
			else{
				if ("message_argument".equals(qName)){
					oneMessage.setArgument(attr);
				}
				else{
					if ("deltas".equals(qName)){
						// ignore outer tag.
					}
					else{
						System.out.println("unhandled tag: "+qName);
					}
				}
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException{
		super.endElement(uri, localName, qName);
		if(debug){
			if(messageCounter>=100){
				return;
			}
		}
		if("delta".equals(qName)){
			if (oneMessage==null){
				throw new RuntimeException("something strange ");
			}
			MessageFlags.Data tmp = converter.getData(oneMessage);
			if(tmp!=null){
				String signature = tmp.getCompleteSignature();
				String code = tmp._flagsString;
				String knownCode = knownErrors.getProperty(signature+"_"+code);
				boolean suppress;
				if(knownCode!=null){
					// the error is known and should be ignored.
					suppress = true;
					if(verbose){
						System.out.println("suppressing message, as it is on suppress list: "+signature+"_"+code);
					}
				}
				else{
					suppress = false;
				}
				tmp.setSuppress(suppress);
				//writer.writeData(tmp);
			}
			oneMessage = null;
		}
		else{
			if ("message_arguments".equals(qName)){
				oneMessage.endArguments();
			}
			else{
				if ("message_argument".equals(qName)){
					oneMessage.endArgument();
				}
				else{
					if ("deltas".equals(qName)){
						// ignore outer tag.
					}
					else{
						System.out.println("unhandled tag: "+qName);
					}
				}
			}
		}
	}

	public int getMessageCount() {
		return messageCounter;
	}
			
}
