package com.opentext.apps.cc.importcontent;

import javax.xml.namespace.QName;

import com.cordys.cpc.bsf.busobject.exception.WSAppsSOAPFault;
import com.eibus.localization.IStringResource;
import com.eibus.soap.fault.Fault;

public class ContractMgmtRuntimeException extends WSAppsSOAPFault{
	
	private static final long serialVersionUID = 8538717879872771514L;

	public ContractMgmtRuntimeException(QName faultCode, String actor, IStringResource faultString, Object... messageParams) {
		super(faultCode, actor, null, faultString, messageParams);
	}
	
	public ContractMgmtRuntimeException(Throwable t, IStringResource message, Object...insertions )
	{
		super(t, Fault.Codes.SERVER, message, insertions);
	}
}
