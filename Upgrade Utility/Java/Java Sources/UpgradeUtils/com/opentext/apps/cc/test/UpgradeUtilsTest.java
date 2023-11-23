//package com.opentext.apps.cc.test;
//
//import java.io.IOException;
//import java.io.StringReader;
//
//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.parsers.ParserConfigurationException;
//
//import org.apache.commons.lang3.StringUtils;
//import org.w3c.dom.Document;
//import org.w3c.dom.Node;
//import org.w3c.dom.NodeList;
//import org.xml.sax.InputSource;
//import org.xml.sax.SAXException;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.core.Version;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.node.ObjectNode;
//
//public class UpgradeUtilsTest {
//
//	public static void main(String[] args) {
////		ReadXmlFile readXmlFile = new ReadXmlFile();
////		readXmlFile.readXmlFile();
////		appendContractIds(
////				"<wstxns1:FindZ_INT_ContractTotalCountResponse xmlns:wstxns1=\"http://schemas/OpenTextContractCenter/Contract/operations\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" total=\"117\"><wstxns2:Contract xmlns:wstxns2=\"http://schemas/OpenTextContractCenter/Contract\" xmlns=\"http://schemas/OpenTextContractCenter/Contract\"><Contract-id><Id>983042</Id></Contract-id></wstxns2:Contract><wstxns3:Contract xmlns:wstxns3=\"http://schemas/OpenTextContractCenter/Contract\" xmlns=\"http://schemas/OpenTextContractCenter/Contract\"><Contract-id><Id>1310722</Id></Contract-id></wstxns3:Contract><wstxns4:Contract xmlns:wstxns4=\"http://schemas/OpenTextContractCenter/Contract\" xmlns=\"http://schemas/OpenTextContractCenter/Contract\"><Contract-id><Id>1310723</Id></Contract-id></wstxns4:Contract><wstxns5:Contract xmlns:wstxns5=\"http://schemas/OpenTextContractCenter/Contract\" xmlns=\"http://schemas/OpenTextContractCenter/Contract\"><Contract-id><Id>1310724</Id></Contract-id></wstxns5:Contract><wstxns6:Cursor xmlns:wstxns6=\"http://schemas.opentext.com/bps/entity/core\" offset=\"4\" limit=\"4\"/></wstxns1:FindZ_INT_ContractTotalCountResponse>");
//
////		String error = "";
////		String error1 = "{}";
////		String error2 = "{\"151\":{\"Error\":{\"IPO\":{\"error\":\"some text\",\"version\":\"22.4\"}}}}";
////		String error3 = "{\"152\":{\"Error\":{\"IPO\":{\"error\":\"some text\",\"version\":\"22.4\"}}}}";
////		String str = AddErrorToErrorJson(error2, "152", "IPO1", "22.4", "some text");
////		System.out.println(str);
//
////		System.out.println(compareVersion("20.2", "21.2"));
////		System.out.println(compareVersion("21.2", "20.2"));
////		System.out.println(compareVersion("20", "21.2"));
////		System.out.println(compareVersion("20.1", "21"));
////		System.out.println(compareVersion("20.1.1", "21.0.1"));
////		System.out.println(compareVersion("20.1.2", "20.1.1"));
////		System.out.println(compareVersion("21.2", "21.1.1"));
////		System.out.println(compareVersion("21.1", "21.1.1"));
//
////		System.out.println("----------------true----------------------");
////		System.out.println(compareVersion1("20.2", "21.2"));
////		System.out.println(compareVersion1("20", "21.2"));
////		System.out.println(compareVersion1("20.1", "21"));
////		System.out.println(compareVersion1("20.1.1", "21.0.1"));
////		System.out.println(compareVersion1("21.1", "21.1.1"));
////
////		System.out.println("----------------false----------------------");
////		System.out.println(compareVersion1("21.2", "21.1.1"));
////		System.out.println(compareVersion1("21.2", "20.2"));
////		System.out.println(compareVersion1("20.1.2", "20.1.1"));
////		System.out.println(compareVersion1("21.1.1", "21.1.0"));
////		
//
//		System.out.println("----------------true----------------------");
//		System.out.println(compareVersion2("20.2", "21.2"));
//		System.out.println(compareVersion2("20", "21.2"));
//		System.out.println(compareVersion2("20.1", "21"));
//		System.out.println(compareVersion2("20.1.1", "21.0.1"));
//		System.out.println(compareVersion2("21.1", "21.1.1"));
//
//		System.out.println("----------------false----------------------");
//		System.out.println(compareVersion2("21.2", "21.1.1"));
//		System.out.println(compareVersion2("21.2", "20.2"));
//		System.out.println(compareVersion2("20.1.2", "20.1.1"));
//		System.out.println(compareVersion2("21.1.1", "21.1.0"));
//		System.out.println(compareVersion2("21.2", "21.1.0"));
//	}
//
//	public static String appendContractIds(String contracts) {
//		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//		DocumentBuilder db;
//		StringBuffer buffer = new StringBuffer();
//		try {
//			db = dbf.newDocumentBuilder();
//			Document doc = db.parse(new InputSource(new StringReader(contracts)));
//			doc.getDocumentElement().normalize();
//			System.out.println(doc.toString());
//			System.out.println(doc.getNodeValue());
//			NodeList nodeList = doc.getChildNodes().item(0).getChildNodes();
//			for (int itr = 0; itr < nodeList.getLength(); itr++) {
//				Node node = nodeList.item(itr);
//				if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().contains("Contract")) {
//					String nodeId = nodeList.item(itr).getFirstChild().getFirstChild().getTextContent().trim();
//					buffer.append(nodeId + ";");
//				}
//
//			}
//		} catch (ParserConfigurationException | SAXException | IOException e) {
//			e.printStackTrace();
//		}
//
//		return buffer.toString();
//	}
//
//	public static String AddErrorToErrorJson(String errorJson, String instanceId, String feature, String version,
//			String error) {
//		if (null == errorJson || errorJson.trim().isEmpty()) {
//			errorJson = "{}";
//		}
//		ObjectMapper objectMapper = new ObjectMapper();
//		JsonNode jsonNode = null, jsonInstanceNode = null, jsonErrorNode = null, errorFeatureNode = null,
//				errorInfoNode = null;
//		String resultJson = null;
//		try {
//			jsonNode = objectMapper.readTree(errorJson);
//			jsonInstanceNode = jsonNode.get(instanceId);
//			if (null != jsonInstanceNode && !jsonInstanceNode.isEmpty()) {
//				errorFeatureNode = jsonInstanceNode.get("Error");
//				errorInfoNode = objectMapper.createObjectNode();
//				((ObjectNode) errorInfoNode).put("error", error);
//				((ObjectNode) errorInfoNode).put("version", version);
//				((ObjectNode) errorFeatureNode).set(feature, errorInfoNode);
//			} else {
//				jsonErrorNode = objectMapper.createObjectNode();
//				errorFeatureNode = objectMapper.createObjectNode();
//				errorInfoNode = objectMapper.createObjectNode();
//				((ObjectNode) errorInfoNode).put("error", error);
//				((ObjectNode) errorInfoNode).put("version", version);
//				((ObjectNode) errorFeatureNode).set(feature, errorInfoNode);
//				((ObjectNode) jsonErrorNode).set("Error", errorFeatureNode);
//				((ObjectNode) jsonNode).set(instanceId, jsonErrorNode);
//			}
//			resultJson = jsonNode.toString();
//		} catch (JsonProcessingException e) {
//			e.printStackTrace();
//		}
//		return resultJson;
//	}
//
//	/***
//	 * if version1 < version2 = true else false
//	 **/
//	private static boolean compareVersion(String version1, String version2) {
////		Version version1 = new Version(major, minor, patchLevel, snapshotInfo)  
//		boolean isLess = true;
//		String[] indVersions1 = StringUtils.split(version1, ".");
//		String[] indVersions2 = StringUtils.split(version2, ".");
//		for (int i = 0; i < indVersions2.length; i++) {
//			if (i < indVersions1.length && indVersions1[i].compareTo(indVersions2[i]) == 0) {
//				continue;
//			} else if (i < indVersions1.length && indVersions1[i].compareTo(indVersions2[i]) > 0) {
//				return false;
//			} else if (i < indVersions1.length && indVersions1[i].compareTo(indVersions2[i]) < 0) {
//				return true;
//			}
//		}
//		return isLess;
//	}
//
//	/***
//	 * if version1 < version2 = true else false
//	 **/
//	private static boolean compareVersion1(String version1, String version2) {
//		boolean isLess = true;
//		String[] indVersions1 = StringUtils.split(version1, ".");
//		String[] indVersions2 = StringUtils.split(version2, ".");
//		int major = 0, minor = 0, patch = 0;
//
//		Version versionO1 = new Version(major, minor, patch, null);
//		Version versionO2 = new Version(major, minor, patch, null);
//		for (int i = 0; i < indVersions1.length; i++) {
//			if (i == 0) {
//				major = Integer.parseInt(indVersions1[0]);
//			} else if (i == 1) {
//				minor = Integer.parseInt(indVersions1[1]);
//			} else if (i == 2) {
//				patch = Integer.parseInt(indVersions1[2]);
//			}
//		}
//		versionO1 = new Version(major, minor, patch, null);
//		major = 0;
//		minor = 0;
//		patch = 0;
//		for (int i = 0; i < indVersions2.length; i++) {
//			if (i == 0) {
//				major = Integer.parseInt(indVersions2[0]);
//			} else if (i == 1) {
//				minor = Integer.parseInt(indVersions2[0]);
//			} else if (i == 2) {
//				patch = Integer.parseInt(indVersions2[0]);
//			}
//		}
//		versionO2 = new Version(major, minor, patch, null);
//
//		return versionO1.compareTo(versionO2) > 0 ? false : true;
//	}
//
//	static boolean compareVersion2(String v1, String v2) {
//		// vnum stores each numeric part of version
//		int vnum1 = 0, vnum2 = 0;
//
//		// loop until both String are processed
//		for (int i = 0, j = 0; (i < v1.length() || j < v2.length());) {
//			// Storing numeric part of
//			// version 1 in vnum1
//			while (i < v1.length() && v1.charAt(i) != '.') {
//				vnum1 = vnum1 * 10 + (v1.charAt(i) - '0');
//				i++;
//			}
//
//			// storing numeric part
//			// of version 2 in vnum2
//			while (j < v2.length() && v2.charAt(j) != '.') {
//				vnum2 = vnum2 * 10 + (v2.charAt(j) - '0');
//				j++;
//			}
//
//			if (vnum1 > vnum2)
//				return false;
//			if (vnum2 > vnum1)
//				return true;
//
//			// if equal, reset variables and
//			// go for next numeric part
//			vnum1 = vnum2 = 0;
//			i++;
//			j++;
//		}
//		return false;
//	}
//
//}
