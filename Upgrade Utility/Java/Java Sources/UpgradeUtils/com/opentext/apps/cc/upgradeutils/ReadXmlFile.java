package com.opentext.apps.cc.upgradeutils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.cordys.cpc.bsf.busobject.BSF;
import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;
import com.eibus.util.system.EIBProperties;
import com.opentext.apps.cc.upgradeutils.util.NomUpgradeUtil;
import com.opentext.apps.cc.upgradeutils.util.NomUtil;
//import com.opentext.apps.cc.custom.Utilities;
//import com.opentext.apps.cc.importcontent.ContractManagementMessages;
//import com.opentext.apps.cc.importcontent.ContractMgmtRuntimeException;
import com.opentext.apps.cc.upgradeutils.util.ReadConfigFileUtil;

public final class ReadXmlFile {

	public static final String BPM_CONTRACT = "contract";
	public static final String VERSION = "version";
	public static final String UPGRADECONFIG = "upgradeConfig";
	public static final String CONTENT = "content";
	private static final CordysLogger logger = CordysLogger.getCordysLogger(ReadXmlFile.class);

	private static Object docLock = new Object();

	private static volatile Document doc = null;

	public static void readXmlFile() {
		if (null != doc) {
			return;
		}
		synchronized (docLock) {
			if (null != doc) {
				return;
			}
			String fileFullPath = (getConfigFilePath());
			File file = new File(fileFullPath);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db;
			try {
				db = dbf.newDocumentBuilder();
				doc = db.parse(file);
				doc.getDocumentElement().normalize();
			} catch (ParserConfigurationException | SAXException | IOException e) {
				logger._log("com.opentext.apps.cc.upgradeutils.ReadXmlFile", Severity.ERROR, e,
						"Error while executing readXmlFile");
			}
		}
	}

	public static String getConfigFilePath() {
		String orgDN = BSF.getOrganization();
		String orgName = orgDN.substring(orgDN.indexOf('=') + 1, orgDN.indexOf(','));
		String fileRootPath = File.separator + orgName + File.separator + "com" + File.separator + "opentext"
				+ File.separator + "apps" + File.separator + "cc" + File.separator + "resources" + File.separator
				+ "CCUpgradeMetaData.xml";
		String OrgPath = Paths.get(EIBProperties.getInstallDir())
				.resolve("webroot" + File.separator + "organization" + fileRootPath).toString();
		String SharedPath = Paths.get(EIBProperties.getInstallDir())
				.resolve("webroot" + File.separator + "shared" + fileRootPath).toString();
		String OrgSharedPath = Paths.get(EIBProperties.getInstallDir())
				.resolve("webroot" + File.separator + "organization" + File.separator + "system" + fileRootPath)
				.toString();
		if (new File(OrgPath).exists()) {
			return OrgPath;
		} else if (new File(SharedPath).exists()) {
			return SharedPath;
		} else if (new File(OrgSharedPath).exists()) {
			return OrgSharedPath;
		} else {
			return "";
		}
	}

	public static Map<String, List<String>> getBPMsList(String entity, String toVersion) {
		return getBPMsList(entity, null, toVersion);
	}

	public static Map<String, List<String>> getBPMsList(String entity, String fromVersion, String toVersion) {
		if (null == doc) {
			return null;
		}
		List<String> Bpms = new ArrayList<>();
		Map<String, List<String>> bpmVersionMap = new LinkedHashMap<>();
		String bpmName = null;
		NodeList nodeContent = doc.getElementsByTagName(CONTENT);
		NodeList nodeList = nodeContent.item(0).getChildNodes();
		for (int itr = 0; itr < nodeList.getLength(); itr++) {
			Node node = nodeList.item(itr);
			if (node.getNodeType() == Node.ELEMENT_NODE && UPGRADECONFIG == node.getNodeName()) {
				Element eElement = (Element) node;
				String l_Version = eElement.getElementsByTagName(VERSION).item(0).getTextContent();
				if (null == fromVersion || ReadConfigFileUtil.compareVersion(fromVersion, l_Version)) {
					if (null != eElement.getElementsByTagName(entity) && null!=eElement.getElementsByTagName(entity).item(0)) {
						Bpms = new ArrayList<>();
						NodeList bpmsNode = eElement.getElementsByTagName(entity).item(0).getChildNodes();
						for (int i = 0; i < bpmsNode.getLength(); i++) {
							if (node.getNodeType() == Node.ELEMENT_NODE
									&& (!StringUtils.isEmpty(bpmName = bpmsNode.item(i).getTextContent().trim()))) {
								Bpms.add(bpmName);
							}
						}
						bpmVersionMap.put(l_Version, Bpms);
					}
				}
			}
		}
		return bpmVersionMap;
	}

	public static Node getLastNodeList() {
		if (null == doc) {
			return null;
		}
		NodeList nodeContent = doc.getElementsByTagName(CONTENT);
		NodeList nodeContent1 = doc.getElementsByTagName("version");
		String str = null;
		for (int i = 0; i < nodeContent1.getLength(); i++) {
			str = nodeContent1.item(i).getTextContent();
//			String str = NomUtil.getData(nodeContent1.item(i));
		}
		NodeList nodeList = nodeContent.item(0).getChildNodes();
		Node nodeReturn = null;
		for (int itr = 0; itr < nodeList.getLength(); itr++) {
			Node node = nodeList.item(itr);
			if (node.getNodeType() == Node.ELEMENT_NODE && UPGRADECONFIG == node.getNodeName()) {
				nodeReturn = node;
			}
		}
		return nodeReturn;
	}

}
