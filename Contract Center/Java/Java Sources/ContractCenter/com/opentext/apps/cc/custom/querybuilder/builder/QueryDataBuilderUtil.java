package com.opentext.apps.cc.custom.querybuilder.builder;

import java.io.IOException;
import java.io.StringReader;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.opentext.apps.cc.custom.querybuilder.builder.data.Connector;
import com.opentext.apps.cc.custom.querybuilder.builder.data.ConnectorNode;
import com.opentext.apps.cc.custom.querybuilder.builder.data.ContainerNode;
import com.opentext.apps.cc.custom.querybuilder.builder.data.ExpressionNode;
import com.opentext.apps.cc.custom.querybuilder.builder.data.ExpressionOperandDataType;
import com.opentext.apps.cc.custom.querybuilder.builder.data.ExpressionOperator;
import com.opentext.apps.cc.custom.querybuilder.builder.data.IDataNode;
import com.opentext.apps.cc.custom.querybuilder.builder.data.SearchQueryData;

public class QueryDataBuilderUtil {

	public final static SearchQueryData buildSearchQueryData(String xmlData)
			throws SAXException, IOException, ParserConfigurationException {
		SearchQueryData queryData = null;
		if (!Objects.isNull(xmlData)) {
			Document document = readDocument(xmlData);
			queryData = new SearchQueryData();
			NodeList nodeList = document.getElementsByTagName("QueryElement");
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
//				System.out.println(node.getTextContent());
				Element ele = (Element) node;
				String parent = ele.getElementsByTagName("ParentElement").item(0).getTextContent();
				String type = ele.getElementsByTagName("Type").item(0).getTextContent();

				if (node.getNodeType() == Node.ELEMENT_NODE && type.equals("CONTAINER")
						&& (Objects.isNull(parent) || parent.isEmpty())) {
					IDataNode dataNode = addNode(i, nodeList, queryData);
					queryData.addContainerNode(dataNode);
				}
			}
		}
		return queryData;
	}

	private static IDataNode addNode(int index, NodeList rootQueryList, SearchQueryData queryData) {
		Node node = rootQueryList.item(index);
		Element elementNode = ((Element) node);
		String nodeType = elementNode.getElementsByTagName("Type").item(0).getTextContent();
		String attrType = !Objects.isNull(elementNode.getElementsByTagName("AttrType"))
				&& !Objects.isNull(elementNode.getElementsByTagName("AttrType").item(0))
						? elementNode.getElementsByTagName("AttrType").item(0).getTextContent()
						: "GENERAL";
		IDataNode dataNode = null;
		if (null != node && (nodeType.equalsIgnoreCase("CONTAINER"))) {
			dataNode = addContainerNode(index, false, rootQueryList, queryData);
		} else if (null != node && (nodeType.equalsIgnoreCase("CONNECTOR"))) {
			Connector connector = Connector.AND;
			String connecXmlStr = elementNode.getElementsByTagName("Connector").item(0).getTextContent();
			if ("OR".equalsIgnoreCase(connecXmlStr)) {
				connector = Connector.OR;
			}
			dataNode = addConnector(index, connector, rootQueryList);
		} else if (null != node && (nodeType.equalsIgnoreCase("EXPRESSION"))) {
			dataNode = addExpressionNode(index, elementNode.getElementsByTagName("Expression").item(0), rootQueryList,
					attrType, queryData);
		}
		return dataNode;
	}

	private static IDataNode addExpressionNode(int index, Node node, NodeList rootQueryList, String attrType,
			SearchQueryData queryData) {
		String[] expressionList = new String[4];
		Element elementNode = ((Element) node);
		expressionList[0] = elementNode.getElementsByTagName("OperandName").item(0).getTextContent();
		expressionList[1] = elementNode.getElementsByTagName("Operand").item(0).getTextContent();
		expressionList[2] = elementNode.getElementsByTagName("OperandValue").item(0).getTextContent();
		expressionList[3] = elementNode.getElementsByTagName("OperandDataType").item(0).getTextContent();
		ExpressionOperator operator = ExpressionOperator.EQUALS;
		if ("!=".equalsIgnoreCase(expressionList[1])) {
			operator = ExpressionOperator.NOTEQUALS;
		} else if ("like".equalsIgnoreCase(expressionList[1])) {
			operator = ExpressionOperator.CONTAINS;
		} else if ("is null".equalsIgnoreCase(expressionList[1])) {
			operator = ExpressionOperator.EMPTY;
		} else if ("is not null".equalsIgnoreCase(expressionList[1])) {
			operator = ExpressionOperator.NOTEMPTY;
		} else if (">=".equalsIgnoreCase(expressionList[1])) {
			operator = ExpressionOperator.GREATERTHANEQUALTO;
		} else if ("<=".equalsIgnoreCase(expressionList[1])) {
			operator = ExpressionOperator.LESSTHANEQUALTO;
		}
		ExpressionOperandDataType exprOperandDataType = resolveDataType(expressionList[3]);
		boolean isComplianceStatusFlag = isComplianceStatus(expressionList[0]);
		queryData.setHasComplainceStatus(isComplianceStatusFlag);
		return new ExpressionNode(operator, expressionList[0], expressionList[2], attrType, exprOperandDataType);
	}

	private static boolean isComplianceStatus(String expressionName) {
		return SearchQueryData.COMPLIANCE_STATUS.equalsIgnoreCase(expressionName);
	}

	private static ExpressionOperandDataType resolveDataType(String operandDataType) {
		ExpressionOperandDataType exprOperandDataType = ExpressionOperandDataType.TEXT;
		if ("BOOLEAN".equalsIgnoreCase(operandDataType)) {
			exprOperandDataType = ExpressionOperandDataType.BOOLEAN;
		} else if ("LOOKUP".equalsIgnoreCase(operandDataType)) {
			exprOperandDataType = ExpressionOperandDataType.TEXT;
		} else if ("LONGTEXT".equalsIgnoreCase(operandDataType)) {
			exprOperandDataType = ExpressionOperandDataType.LONGTEXT;
		} else if ("ENUMERATEDTEXT".equalsIgnoreCase(operandDataType)) {
			exprOperandDataType = ExpressionOperandDataType.ENUMERATEDTEXT;
		} else if ("DATE".equalsIgnoreCase(operandDataType)) {
			exprOperandDataType = ExpressionOperandDataType.DATE;
		} else if ("DURATION".equalsIgnoreCase(operandDataType)) {
			exprOperandDataType = ExpressionOperandDataType.DURATION;
		} else if ("DECIMAL".equalsIgnoreCase(operandDataType)) {
			exprOperandDataType = ExpressionOperandDataType.DECIMAL;
		} else if ("INTEGER".equalsIgnoreCase(operandDataType)) {
			exprOperandDataType = ExpressionOperandDataType.INTEGER;
		}

		return exprOperandDataType;
	}

	private static IDataNode addConnector(int index, Connector connector, NodeList rootQueryList) {
		ConnectorNode containerNode = new ConnectorNode(connector);
		return containerNode;
	}

	private static final Document readDocument(String xmlData)
			throws SAXException, IOException, ParserConfigurationException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		dbFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(new InputSource(new StringReader(xmlData)));
		return doc;
	}

	private static final IDataNode addContainerNode(int index, boolean isFirstNode, NodeList rootQueryList,
			SearchQueryData queryData) {
		Node xmlConnectorNode = rootQueryList.item(index);
		String xmlConnectorNodeId = ((Element) xmlConnectorNode).getElementsByTagName("Id").item(0).getTextContent();
		ContainerNode connectorNode = new ContainerNode();
		IDataNode childNode = null;
		for (int i = index + 1; i < rootQueryList.getLength(); i++) {
			Node node = rootQueryList.item(i);
			Element ele = (Element) node;
			String parent = ele.getElementsByTagName("ParentElement").item(0).getTextContent();
			if (node.getNodeType() == Node.ELEMENT_NODE
					&& (!Objects.isNull(parent) && xmlConnectorNodeId.equalsIgnoreCase(parent))) {
				childNode = addNode(i, rootQueryList, queryData);
				connectorNode.addNode(childNode);
			}
		}
		return connectorNode;
	}

}
