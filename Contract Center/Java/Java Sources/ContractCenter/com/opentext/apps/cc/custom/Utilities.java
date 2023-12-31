/*
  This class has been generated by the Code Generator
 */

package com.opentext.apps.cc.custom;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.cordys.restclient.SAMLArtifactProvider;
import com.cordys.cpc.bsf.busobject.BSF;
import com.cordys.cpc.bsf.busobject.BusObject;
import com.cordys.cpc.bsf.busobject.BusObjectConfig;
import com.cordys.cpc.bsf.busobject.BusObjectIterator;
import com.cordys.util.Base64Encoding;
import com.eibus.security.identity.UserIdentityFactory;
import com.eibus.util.Util;
import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;
import com.eibus.util.system.EIBProperties;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.XMLException;
import com.eibus.xml.nom.internal.NOMDocumentPool;
import com.eibus.xml.xpath.XPath;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opentext.apps.cc.custom.exceptions.ContractCenterAlertMessages;
import com.opentext.apps.cc.custom.exceptions.ContractCenterApplicationException;

public class Utilities extends UtilitiesBase {
	public static final String DOCUMENT_STORE_NS_1_0 = "http://schemas.cordys.com/documentstore/default/1.0";
	public static final String DOC_STORE_CORDYS_RESOURCE_NAME = "/store/storeconfiguration/authentication/CordysResource/ResourceName";
	public static final String DOC_STORE_CORDYS_RESOURCE_SPACE = "/store/storeconfiguration/authentication/CordysResource/Space";
	public static final String DOC_STORE_CONTENT_SERVER_RESOURCE_NAME = "//store/storeconfiguration/authentication/ContentServerResource/ResourceName";
	public static final String DOC_STORE_CONTENT_SERVER_RESOURCE_SPACE = "//store/storeconfiguration/authentication/ContentServerResource/Space";
	public static final String DOC_STORE_CONTENT_SERVER_SUPPORT_PATH = "//store/storeconfiguration/xECMDetails/SupportPath";
	private static final CordysLogger LOGGER = CordysLogger.getCordysLogger(Utilities.class);

	public Utilities() {
		this((BusObjectConfig) null);
	}

	public Utilities(BusObjectConfig config) {
		super(config);
	}

	public static Date getDate() {
		return new Date();
	}

	public static boolean isStringEmpty(final String value) {
		return Util.isStringEmpty(value);
	}

	public static boolean isArrayEmpty(final Object[] value) {
		return value == null || value.length == 0;
	}

	public static String replace(String str, String regex, String replacement) {
		if (Objects.isNull(str) || str.isEmpty() || Objects.isNull(regex) || Objects.isNull(replacement))
			return str;
		return str.replaceAll(regex, replacement);
	}

	public static String appendLastIfNotThere(String str1, String str2) {
		if (Objects.isNull(str1) || str1.isEmpty())
			str1 = "";
		if (Objects.isNull(str2) || str2.isEmpty() || str1.contains(str2))
			return str1;
		return str1 + str2;
	}

	public static String removeAndAppendLast(String str1, String str2) {
		if (Objects.isNull(str1) || str1.isEmpty())
			str1 = "";
		if (Objects.isNull(str2) || str2.isEmpty())
			return str1;
		return str1.replaceAll(str2, "") + str2;
	}

	public static String parseDate(String inputDate) {
		return inputDate.replace("Z", "");
	}

	public static String parseDurationToMonths(String inputDuration) {
		int monthsInd = inputDuration.indexOf('M'), daysInd = inputDuration.indexOf('D');
		int startInd = 1;
		String months = "0M", days = "0D", duration = "P";

		if (monthsInd > 0) {
			months = inputDuration.substring(startInd, monthsInd).concat("M");
		}
		if (daysInd > 0) {
			days = inputDuration.substring(monthsInd > 0 ? monthsInd + 1 : startInd, daysInd).concat("D");
		}
		return duration.concat(months).concat(days).concat("T0H0M");
	}

	/*
	 * @param UserDN cn=prajendra,cn=organizational
	 * users,o=Personal,cn=cordys,cn=CordysAssure,o=vanenburg.com
	 * 
	 */
	public static String getUserFromdn(final String userDN) {
		return userDN.substring(userDN.indexOf('=') + 1, userDN.indexOf(','));
	}

	/*
	 * @param orgDN o=Personal,cn=cordys,cn=CordysAssure,o=vanenburg.com
	 * 
	 */
	public static String getOrganizationFromdn(final String orgDN) {
		return orgDN.substring(orgDN.indexOf('=') + 1, orgDN.indexOf(','));
	}

	/*
	 * @param UserDN cn=prajendra,cn=organizational
	 * users,o=Personal,cn=cordys,cn=CordysAssure,o=vanenburg.com
	 * 
	 */
	public static String getBSFUser() {
		final String userDN = BSF.getUser();
		return userDN.substring(userDN.indexOf('=') + 1, userDN.indexOf(','));
	}

	/*
	 * returns the current organization
	 */
	public static String getOrganization() {
		final String orgDN = BSF.getOrganization();
		return getOrganizationFromdn(orgDN);
	}

	public static String getOrganization(boolean returnDN) {

		if (returnDN) {
			return BSF.getOrganization();
		} else {
			return getOrganizationFromdn(BSF.getOrganization());
		}
	}

	/*
	 * 
	 * @param noOfDays: pass the number of days to get the future Date
	 * 
	 */

	public static Date getDateAfter(final int noOfDays) {
		Date date = new Date();
		final Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DATE, noOfDays); // add 10 days
		date = cal.getTime();
		return date;
	}

	public static String encode(final String string) {
		final Base64Encoding decoder = new Base64Encoding();
		try {
			return new String(decoder.encode(string.getBytes("UTF-8")));
		} catch (final UnsupportedEncodingException ex) {
			// throw new
			// RequestProcessingFailedException(AssureAlertMessages.ENCODING_FAILED, ex);
		}
		return null;
	}

	public static String decode(final String string) {
		final Base64Encoding decoder = new Base64Encoding();
		try {
			return new String(decoder.decode(string.getBytes("UTF-8")));
		} catch (final UnsupportedEncodingException ex) {
			// throw new
			// RequestProcessingFailedException(AssureAlertMessages.ENCODING_FAILED, ex);
		}
		return null;
	}

	/*
	 * @param nodes: pass all the nodes to be appended
	 * 
	 */
	public static int appendAll(String rootName, int... nodes) throws XMLException {
		String rootTag = "<" + rootName + "/>";
		int finalNode = BSF.getXMLDocument().load(rootTag.getBytes());
		for (int node : nodes) {
			if (node != 0) {
				Node.appendToChildren(node, finalNode);

			}
		}

		return finalNode;
	}

	public static int[] getNodeList(final String expression, final int node) {
		return XPath.getMatchingNodes(expression, null, node);
	}

	public static int getNode(final String expression, final int node) {
		return XPath.getFirstMatch(expression, null, node);
	}

	public static String getData(final String expression, final int node) {
		int firstChild = getNode(expression, node);
		return getData(firstChild);
	}

	public static String getData(final int node) {
		if (BusObject._isNull(node)) {
			return null;
		}
		return Node.getData(node);
	}

	public static String getDocumentURL(String orgName, String fileUrl) {
		String nodeUrl = EIBProperties.getProperty("com.cordys.node.url");
		String documentUrl = null;
		if (nodeUrl != null && !nodeUrl.isEmpty()) {
			documentUrl = nodeUrl + "/home/" + orgName + "/" + fileUrl;
			return documentUrl;
		}
		return null;
	}

	private static String getSamlArtifact(String userDn) {
		return SAMLArtifactProvider.getSAMLArtifact(UserIdentityFactory.getCordysIdentity(userDn));
	}

	public static int getGHSProperties() {
		String orgDN = BSF.getOrganization();
		String orgName = orgDN.substring(orgDN.indexOf('=') + 1, orgDN.indexOf(','));
		Properties properties = new Properties();
		int mappingsNode = NomUtil.parseXML("<Mappings></Mappings>");
		if (properties.isEmpty()) {
			String fileName = "DocumentConfigurations.properties";
			String filePath = EIBProperties.getInstallDir() + File.separator + "webroot" + File.separator
					+ "organization" + File.separator + orgName + File.separator + fileName;
			Path path = Paths.get(filePath);
			URI mappingFile = path.toUri();
			try {
				properties.load(mappingFile.toURL().openStream());
			} catch (IOException e) {
				throw new RuntimeException(fileName + " file not found." + e);
			}
		}
		for (Entry<?, ?> p : properties.entrySet()) {
			int childNode = NomUtil.parseXML("<" + p.getKey().toString() + ">" + "</" + p.getKey().toString() + ">");
			NomUtil.setData(childNode, p.getValue().toString());
			NomUtil.appendChild(childNode, mappingsNode);
		}
		return mappingsNode;
	}

	public static int unlinkNode(final String expression, final int node) {
		int firstChild = getNode(expression, node);

		if (firstChild > 0) {
			return Node.unlink(firstChild);
		}

		return 0;
	}

	public static int setData(final int node, String value) {

		return Node.setDataElement(node, "", value);
	}

	public static int setData(int node, final String expression, String value) {
		int firstChild = getNode(expression, node);
		return setData(firstChild, value);
	}

	public static void setDataToAllMatchingNodes(int node, final String expression, String value) {
		int[] childs = getNodeList(expression, node);

		for (int child : childs) {
			setData(child, value);
		}

	}

	public static int parseXML(final String xml) {
		int node = 0;
		try {
			node = BSF.getXMLDocument().load(xml.getBytes("UTF-8"));
		} catch (XMLException | UnsupportedEncodingException e) {
			// RequestProcessingFailedException reqException = new
			// RequestProcessingFailedException(AssureAlertMessages.XML_LOADING_FAILED,xml,e);
			LOGGER.error(e.getMessage());
			// throw reqException;
		}

		return node;
	}

	public static String writeToString(final int node) {
		return Node.writeToString(node, true);

	}

	public static int appendChild(final int child, final int parent) {
		return Node.appendToChildren(child, parent);

	}

	public static void setAttribute(final int node, String attributeName, String attributeValue) {
		Node.setAttribute(node, attributeName, attributeValue);
	}

	public static void setName(final int node, String name) {
		Node.setName(node, name);
	}

	public static void cleanAllNodes(int node, final String expression) {
		int[] childs = getNodeList(expression, node);
		cleanAll(childs);
	}

	/*
	 * @param nodes: pass all the nodes to be deleted
	 * 
	 * 
	 */
	public static void cleanAll(final int... nodes) {
		for (final int node : nodes) {
			if (node != 0 && Node.isValidNode(node)) {
				if (Node.getParent(node) > 0)
					Node.unlink(node);
				Node.delete(node);
			}
		}
	}

	public static Double roundToTwoDecimal(String ccValue) {
		DecimalFormat roundingTo2Decimal = new DecimalFormat("##.00");
		DecimalFormatSymbols dotSymbol = new DecimalFormatSymbols();
		dotSymbol.setDecimalSeparator('.');
		roundingTo2Decimal.setDecimalFormatSymbols(dotSymbol);
		return Double.valueOf(roundingTo2Decimal.format(Double.parseDouble(ccValue)));
	}

	public static BusObjectIterator<com.opentext.apps.cc.custom.Utilities> getUtilitiesObjects(
			com.cordys.cpc.bsf.query.Cursor cursor) {
		// TODO implement body
		return null;
	}

	public void onInsert() {
	}

	public void onUpdate() {
	}

	public void onDelete() {
	}

	public static String getAllEmailNodes(String Emails) {
		String allEmails[] = Emails.split(";");
		String allEmailsXML = "";
		if (allEmails.length > 0) {
			for (int i = 0; i < allEmails.length; i++) {
				if (allEmails[i].length() > 0)
					allEmailsXML += "<Email>" + allEmails[i] + "</Email>";
			}
		}
		return allEmailsXML;
	}

	public static String removeDuplicates(String emailList) {
		StringBuilder list = new StringBuilder();
		Set<String> emailSet = new HashSet<String>();
		for (String email : emailList.split(";")) {
			emailSet.add(email);
		}
		Iterator<String> emailIterator = emailSet.iterator();
		while (emailIterator.hasNext()) {
			String email = emailIterator.next();
			if (!email.isEmpty()) {
				list.append("<address>");
				list.append(email);
				list.append("</address>");
			}
		}
		return list.toString();
	}

	public static int getDefaultCTRValInput(String attributes) {
		Map<String, String> attributesMap = Pattern.compile("\\s*;\\s*").splitAsStream(attributes.trim())
				.map(s -> s.split(":", 2)).collect(Collectors.toMap(a -> a[0], a -> a.length > 1 ? a[1] : ""));
		return addContractPropertiestoXML(attributesMap);
	}

	private static int addContractPropertiestoXML(Map<String, String> attributesMap) {
		int ContractInputXML = 0;
		ContractInputXML = NOMDocumentPool.getInstance().createElement("ContractInputXML");
		String tags[] = { GenerateSectionClauseConstants.CONTRACT_ORGANIZATION_NAME,
				GenerateSectionClauseConstants.CONTRACT_CONTRACTTYPE_NAME,
				GenerateSectionClauseConstants.CONTRACT_ISEXECUTED, GenerateSectionClauseConstants.CONTRACT_ISEXTERNAL,
				GenerateSectionClauseConstants.CONTRACT_TEMPLATETYPE,
				GenerateSectionClauseConstants.CONTRACT_TEMPLATE_NAME,
				GenerateSectionClauseConstants.CONTRACT_COUNTRY_NAME, GenerateSectionClauseConstants.CONTRACT_PERPETUAL,
				GenerateSectionClauseConstants.CONTRACT_CONTRACTTERM,
				GenerateSectionClauseConstants.CONTRACT_CURRENCY_NAME };

		for (String tag : tags)
			switch (tag) {
			case GenerateSectionClauseConstants.CONTRACT_ISEXECUTED:
				if ((attributesMap.get(tag.toUpperCase()) != null)
						&& attributesMap.get(tag.toUpperCase()).toLowerCase().equals("true")) {
					int attr = 0;
					attr = NOMDocumentPool.getInstance().createElement(tag.toUpperCase());
					NomUtil.setData(attr, "true");
					NomUtil.appendChild(attr, ContractInputXML);
				}
				break;
			case GenerateSectionClauseConstants.CONTRACT_ISEXTERNAL:
				if ((attributesMap.get(tag.toUpperCase()) != null)
						&& attributesMap.get(tag.toUpperCase()).toLowerCase().equals("true")) {
					int attr = 0;
					attr = NOMDocumentPool.getInstance().createElement(tag.toUpperCase());
					NomUtil.setData(attr, "EXTERNALDOCUMENT");
					NomUtil.appendChild(attr, ContractInputXML);
				}
				break;
			case GenerateSectionClauseConstants.CONTRACT_TEMPLATETYPE:
				if (!isStringEmpty(attributesMap.get(tag.toUpperCase()))) {
					String TemplateTypes[] = { "Internal template", "Internal party document",
							"External party document" };
					boolean contains = Arrays.stream(TemplateTypes)
							.anyMatch(attributesMap.get(tag.toUpperCase())::equals);
					if (contains) {
						int attr = 0;
						attr = NOMDocumentPool.getInstance().createElement(tag.toUpperCase());
						NomUtil.setData(attr, attributesMap.get(tag.toUpperCase()));
						NomUtil.appendChild(attr, ContractInputXML);
					}
				}
				break;
			case GenerateSectionClauseConstants.CONTRACT_PERPETUAL:
				if ((attributesMap.get(tag.toUpperCase()) != null)
						&& attributesMap.get(tag.toUpperCase()).toLowerCase().equals("true")) {
					int attr = 0;
					attr = NOMDocumentPool.getInstance().createElement(tag.toUpperCase());
					NomUtil.setData(attr, "true");
					NomUtil.appendChild(attr, ContractInputXML);
				}
				break;
			case GenerateSectionClauseConstants.CONTRACT_CONTRACTTERM:
				if ((attributesMap.get(tag.toUpperCase()) != null)
						&& ((attributesMap.get(GenerateSectionClauseConstants.CONTRACT_PERPETUAL.toUpperCase()) == null)
								|| !attributesMap.get(GenerateSectionClauseConstants.CONTRACT_PERPETUAL.toUpperCase())
										.toLowerCase().equals("true"))) {
					int attr = 0;
					attr = NOMDocumentPool.getInstance().createElement(tag.toUpperCase());
					NomUtil.setData(attr, attributesMap.get(tag.toUpperCase()));
					NomUtil.appendChild(attr, ContractInputXML);
				}
				break;
			default:
				int attr = 0;
				attr = NOMDocumentPool.getInstance().createElement(tag.toUpperCase());
				try {

					ObjectMapper mapper = new ObjectMapper();
					JsonNode jsonNode = mapper.readTree(attributesMap.get(tag.toUpperCase()));
					JsonNode itemIDNode = jsonNode.get("ItemId");
					NomUtil.setData(attr,
							itemIDNode.toString().startsWith("\"") && itemIDNode.toString().endsWith("\"")
									? itemIDNode.toString().substring(1, itemIDNode.toString().lastIndexOf("\""))
									: itemIDNode.toString());

//					JsonReader jsonReader = Json.createReader(new StringReader(attributesMap.get(tag.toUpperCase())));
//					JsonObject jsonObject = jsonReader.readObject();
				} catch (Exception e) {
				}
				NomUtil.appendChild(attr, ContractInputXML);
				break;
			}
		return ContractInputXML;
	}

	private static String getTextNode(String tag, String value) {
		return ("<" + tag + ">" + (value != null ? value : "") + "</" + tag + ">");
	}

	public static String getTableName(String solutionname, String entityname) {
		HttpURLConnection conn = null;
		String nodeUrl = EIBProperties.getProperty("com.cordys.node.url");
		// nodeUrl = "http://ccdevint.lab.opentext.com:5050";
		String orgDN = BSF.getOrganization();
		String orgName = orgDN.substring(orgDN.indexOf('=') + 1, orgDN.indexOf(','));

		if (nodeUrl != null && !nodeUrl.isEmpty()) {
			int responseCode = 0;
			URL url;
			String ctrAddPropsTableName;
			String documentUrl = nodeUrl + "/home/" + orgName + "/app/entityRestService/api/" + solutionname
					+ "/entities/Contract/tableName";

			try {
				url = new URL(documentUrl);
				conn = (HttpURLConnection) url.openConnection();
				conn.setDoOutput(true);
				conn.setRequestMethod("GET");
				conn.setRequestProperty("SAMLart", getSamlArtifact(BSF.getUser()));

				InputStreamReader inputStreamReader = new InputStreamReader(conn.getInputStream());
				String reponse = new BufferedReader(inputStreamReader).lines().collect(Collectors.joining("\n"));

				responseCode = ((HttpURLConnection) conn).getResponseCode();
				if (responseCode == 200) {
					ObjectMapper mapper = new ObjectMapper();
					JsonNode jsonNode = mapper.readTree(reponse);
					JsonNode tableNameTextNode = jsonNode.get("tableName");

					if (Objects.nonNull(tableNameTextNode)) {
						String contractTableName = tableNameTextNode.asText();
						ctrAddPropsTableName = contractTableName.substring(0, contractTableName.indexOf(solutionname));
						return (ctrAddPropsTableName.concat(solutionname).concat(entityname).toLowerCase());
					}
				}
			} catch (MalformedURLException e) {
				LOGGER._log("com.opentext.apps.cc.custom.Utilities", Severity.ERROR, e,
						ContractCenterAlertMessages.URL_IS_NOT_VALIED);
				throw new ContractCenterApplicationException(ContractCenterAlertMessages.URL_IS_NOT_VALIED);
			} catch (IOException e) {
				LOGGER._log("com.opentext.apps.cc.custom.Utilities", Severity.ERROR, e,
						ContractCenterAlertMessages.INPUT_SHOULD_NOT_BE_NULL);
				throw new ContractCenterApplicationException(ContractCenterAlertMessages.INPUT_SHOULD_NOT_BE_NULL);
			} finally {
				conn.disconnect();
			}
		}
		return null;
	}
}
