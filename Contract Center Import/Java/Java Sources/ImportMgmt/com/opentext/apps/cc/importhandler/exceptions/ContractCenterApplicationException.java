package com.opentext.apps.cc.importhandler.exceptions;

import javax.xml.namespace.QName;

import com.cordys.cpc.bsf.busobject.exception.WSAppServerRunTimeException;
import com.eibus.localization.IStringResource;

public class ContractCenterApplicationException extends WSAppServerRunTimeException{
	private static final long serialVersionUID = -3441178035952063739L;
	private static final QName qName = new QName("ContractCenterJavaException");

	/**
	 * Creates an instance of the BsfRuntimeException
	 */
	public ContractCenterApplicationException(String message)
	{
		super(message, null);
		setFaultCode(qName);
	}

	/**
	 * Creates an instance of AssureException with the nested exception and the
	 * message.
	 * 
	 * @param message
	 *            The message string of the exception.
	 * @param nestedException
	 *            The nested Throwable object.
	 */
	public ContractCenterApplicationException(String message, Throwable nestedException)
	{
		super(message, nestedException);
		setFaultCode(qName);
	}

	public ContractCenterApplicationException(IStringResource localizableString, Object... insertions)
	{
		super(localizableString, insertions);
		setFaultCode(qName);
	}

	public ContractCenterApplicationException(IStringResource localizableString, Throwable cause, Object... insertions)
	{
		super(cause, localizableString, insertions);
		setFaultCode(qName);
	}

}
