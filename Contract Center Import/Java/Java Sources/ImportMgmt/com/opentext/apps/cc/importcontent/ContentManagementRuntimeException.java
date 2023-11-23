package com.opentext.apps.cc.importcontent;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.eibus.localization.IStringResource;
import com.eibus.localization.exception.LocalizableRuntimeException;

public class ContentManagementRuntimeException extends LocalizableRuntimeException
{

	/**  */
	private static final long serialVersionUID = -3186067345937944037L;

	public ContentManagementRuntimeException( final IStringResource stMessage, final Object... insertions )
	{
		super(stMessage, insertions);
	}

	public ContentManagementRuntimeException( final Throwable oThrowable, final IStringResource stMessage,
	        final Object... insertions )
	{
		super(oThrowable, stMessage, insertions);
	}

	public ContentManagementRuntimeException(final Throwable oThrowable)
	{
		super(oThrowable);
	}
	
	@Override
	public String toString()
	{
		String stDetails = this.getMessage();
		Throwable oThrowable = null;
		if( (oThrowable = this.getCause()) != null )
		{
			StringWriter oStringWriter = new StringWriter();
			PrintWriter oPrintWriter = new PrintWriter(oStringWriter);
			oThrowable.printStackTrace(oPrintWriter);
			if (stDetails == null)
			{
				stDetails = oStringWriter.toString();
			}
			else
			{
				stDetails = new StringBuffer(stDetails).append("\nCaused By :\n").append(oStringWriter.toString()).toString();
			}
		}
		return stDetails;
	}
}
