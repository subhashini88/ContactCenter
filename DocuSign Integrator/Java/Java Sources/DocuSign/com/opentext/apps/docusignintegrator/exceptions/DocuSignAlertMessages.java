package com.opentext.apps.docusignintegrator.exceptions;

import com.eibus.localization.message.Message;
import com.eibus.localization.message.MessageSet;

public class DocuSignAlertMessages {
	public static final MessageSet DOCU_SIGN_MESSAGE_SET = MessageSet.getMessageSet("com.opentext.apps.commoncomponents.docusign.Messages");

	public static final Message ERROR_PROCESSING_RESPONSE_FOR_ACCESS_TOKEN = DOCU_SIGN_MESSAGE_SET.getMessage("processingResponseForAccessToken");
	public static final Message ERROR_UNSUPPORTED_ENCODING = DOCU_SIGN_MESSAGE_SET.getMessage("unsupportedEncoding");
	public static final Message ERROR_ACCESS_TOKEN_CONNECTION = DOCU_SIGN_MESSAGE_SET.getMessage("accessTokenConnection");
	public static final Message ERROR_ACCESS_TOKEN_PROTOCOL = DOCU_SIGN_MESSAGE_SET.getMessage("accessTokenProtocolError");
	public static final Message ERROR_OUTPUT_STREAM = DOCU_SIGN_MESSAGE_SET.getMessage("outputStreamError");
	public static final Message ERROR_RELEASING_RESOURCES = DOCU_SIGN_MESSAGE_SET.getMessage("releasingResources");
	
	public static final Message ERROR_LOGIN_INFO_CONNECTION = DOCU_SIGN_MESSAGE_SET.getMessage("loginInfoConnection");
	public static final Message ERROR_LOGIN_INFO_PROTOCOL = DOCU_SIGN_MESSAGE_SET.getMessage("loginInfoProtocolError");
	
}
