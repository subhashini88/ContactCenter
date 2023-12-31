/*
  This class has been generated by the Code Generator
*/

package com.opentext.apps.docusignintegrator;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.cordys.cpc.bsf.busobject.BusObjectConfig;
import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;
import com.opentext.apps.docusignintegrator.exceptions.DocuSignAlertMessages;
import com.opentext.apps.docusignintegrator.exceptions.DocuSignApplicationException;

public class DocuSignServices extends DocuSignServicesBase {
	private static final CordysLogger logger = CordysLogger.getCordysLogger(DocuSignServices.class);

	private static final String DOCUSIGN_REST_ENDPOINT = "https://demo.docusign.net/restapi/v2/";
	private static final String REQUEST_URL_ACCESS_TOKEN = DOCUSIGN_REST_ENDPOINT + "oauth2/token";
	private static final String REQUEST_URL_LOGIN_INFORMATION = DOCUSIGN_REST_ENDPOINT + "login_information";
	private static final String ACCESS_TOKEN_URL_PART = "oauth2/token";
	private static final String LOGIN_INFORMATION_PART = "login_information";

	public DocuSignServices() {
		this((BusObjectConfig) null);
	}

	public DocuSignServices(BusObjectConfig config) {
		super(config);
	}

	public static String GenerateDocuSignAccessToken(String username, String password, String key, String docuSignURL) {
		HttpURLConnection conn = sendAccessTokenRequest(username, password, key, docuSignURL);
		Document parser = getDomXmlfromResponse(conn);
		NodeList nodes = parser.getElementsByTagName("access_token");
		return nodes.item(0).getTextContent();
	}

	private static HttpURLConnection sendAccessTokenRequest(String username, String password, String key,
			String docuSignURL) {
		HttpURLConnection conn = null;
		OutputStream os = null;
		BufferedWriter writer = null;

		// Request body
		HashMap<String, String> authMap = new HashMap<String, String>();
		authMap.put("grant_type", "password");
		authMap.put("client_id", key);
		authMap.put("scope", "api");
		authMap.put("username", username);
		authMap.put("password", password);

		String accessTokenURL = null;
		if (Objects.nonNull(docuSignURL) && !docuSignURL.isBlank()) {
			accessTokenURL = docuSignURL.endsWith("/") ? docuSignURL + ACCESS_TOKEN_URL_PART
					: docuSignURL + "/" + ACCESS_TOKEN_URL_PART;
		} else {
			accessTokenURL = REQUEST_URL_ACCESS_TOKEN;
		}

		try {
			conn = (HttpURLConnection) new URL(accessTokenURL).openConnection();
		} catch (IOException e) {
			logger._log("com.opentext.apps.docusignintegrator", Severity.ERROR, e,
					DocuSignAlertMessages.ERROR_ACCESS_TOKEN_CONNECTION);
			throw new DocuSignApplicationException(DocuSignAlertMessages.ERROR_ACCESS_TOKEN_CONNECTION);
		}

		try {
			conn.setRequestMethod("POST");
		} catch (ProtocolException e) {
			logger._log("com.opentext.apps.docusignintegrator", Severity.ERROR, e,
					DocuSignAlertMessages.ERROR_ACCESS_TOKEN_PROTOCOL);
			throw new DocuSignApplicationException(DocuSignAlertMessages.ERROR_ACCESS_TOKEN_PROTOCOL);
		}

		// Request header
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setRequestProperty("Accept", "application/xml");

		conn.setDoInput(true);
		conn.setDoOutput(true);

		try {
			os = conn.getOutputStream();
			writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
			writer.write(getPostDataString(authMap, "auth"));
			writer.flush();
		} catch (UnsupportedEncodingException e) {
			logger._log("com.opentext.apps.docusignintegrator", Severity.ERROR, e,
					DocuSignAlertMessages.ERROR_UNSUPPORTED_ENCODING);
			throw new DocuSignApplicationException(DocuSignAlertMessages.ERROR_UNSUPPORTED_ENCODING);
		} catch (IOException e) {
			logger._log("com.opentext.apps.docusignintegrator", Severity.ERROR, e,
					DocuSignAlertMessages.ERROR_OUTPUT_STREAM);
			throw new DocuSignApplicationException(DocuSignAlertMessages.ERROR_OUTPUT_STREAM);
		} finally {
			try {
				if(null!=writer){
					writer.close();
				}
				if(null!=os){
					os.close();
				}
				
			} catch (IOException e) {
				logger._log("com.opentext.apps.docusignintegrator", Severity.ERROR, e,
						DocuSignAlertMessages.ERROR_RELEASING_RESOURCES);
				throw new DocuSignApplicationException(DocuSignAlertMessages.ERROR_RELEASING_RESOURCES);
			}
		}

		return conn;
	}

	private static Document getDomXmlfromResponse(HttpURLConnection conn) {
		DocumentBuilderFactory newInstance = DocumentBuilderFactory.newInstance();
		newInstance.setNamespaceAware(true);
		InputStream is = null;
		try {
			newInstance.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			is = conn.getInputStream();
			return newInstance.newDocumentBuilder().parse(is);
		} catch (SAXException | IOException | ParserConfigurationException e) {
			logger._log("com.opentext.apps.docusignintegrator", Severity.ERROR, e,
					DocuSignAlertMessages.ERROR_PROCESSING_RESPONSE_FOR_ACCESS_TOKEN);
			throw new DocuSignApplicationException(DocuSignAlertMessages.ERROR_PROCESSING_RESPONSE_FOR_ACCESS_TOKEN);
		} finally {
			try {
				if(null!=is){
					is.close();
				}
				
			} catch (IOException e) {
				logger._log("com.opentext.apps.docusignintegrator", Severity.ERROR, e,
						DocuSignAlertMessages.ERROR_RELEASING_RESOURCES);
				throw new DocuSignApplicationException(DocuSignAlertMessages.ERROR_RELEASING_RESOURCES);
			}
		}
	}

	private static String getPostDataString(HashMap<String, String> params, String type) {
		StringBuilder result = new StringBuilder();
		boolean first = true;
		for (Map.Entry<String, String> entry : params.entrySet()) {
			if ("auth".equalsIgnoreCase(type)) {
				if (first)
					first = false;
				else
					result.append("&");
				try {
					result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
					result.append("=");
					result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					logger._log("com.opentext.apps.docusignintegrator", Severity.ERROR, e,
							DocuSignAlertMessages.ERROR_UNSUPPORTED_ENCODING);
					throw new DocuSignApplicationException(DocuSignAlertMessages.ERROR_UNSUPPORTED_ENCODING);
				}
			}
		}
		return result.toString();
	}

	public static String ValidateAccessToken(String key, String docuSignURL) throws IOException {
		HttpURLConnection conn = sendLoginInfoRequest(key, docuSignURL);
		Document parser = getDomXmlfromResponse(conn);
		NodeList nodes = parser.getElementsByTagName("accountId");

		return nodes.item(0).getTextContent();
	}

	private static HttpURLConnection sendLoginInfoRequest(String key, String docuSignURL) throws IOException {
		HttpURLConnection conn = null;
		
		String docusignLoginURL = null;
		if (Objects.nonNull(docuSignURL) && !docuSignURL.isBlank()) {
			docusignLoginURL = docuSignURL.endsWith("/") ? docuSignURL + LOGIN_INFORMATION_PART
					: docuSignURL + "/" + LOGIN_INFORMATION_PART;
		} else {
			docusignLoginURL = REQUEST_URL_LOGIN_INFORMATION;
		}

		try {
			conn = (HttpURLConnection) new URL(docusignLoginURL).openConnection();
		} catch (IOException e) {
			logger._log("com.opentext.apps.docusignintegrator", Severity.ERROR, e,
					DocuSignAlertMessages.ERROR_LOGIN_INFO_CONNECTION);
			throw new DocuSignApplicationException(DocuSignAlertMessages.ERROR_LOGIN_INFO_CONNECTION);
		}

		try {
			conn.setRequestMethod("GET");
		} catch (ProtocolException e) {
			logger._log("com.opentext.apps.docusignintegrator", Severity.ERROR, e,
					DocuSignAlertMessages.ERROR_LOGIN_INFO_PROTOCOL);
			throw new DocuSignApplicationException(DocuSignAlertMessages.ERROR_LOGIN_INFO_PROTOCOL);
		}

		// Request header
		conn.setRequestProperty("authorization", "Bearer " + key);
		conn.setRequestProperty("content-type", "application/xml");
		conn.setDoInput(true);

		return conn;
	}

	public void onInsert() {
	}

	public void onUpdate() {
	}

	public void onDelete() {
	}

}
