package com.opentext.apps.cc.custom;

import java.io.UnsupportedEncodingException;


import com.cordys.cpc.bsf.busobject.BSF;
import com.cordys.cpc.bsf.busobject.BusObject;
import com.eibus.util.logger.CordysLogger;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.XMLException;
import com.eibus.xml.xpath.XPath;

public class NomUtil 
{
	
	  private static final CordysLogger LOGGER = CordysLogger.getCordysLogger(NomUtil.class);
	
	  /*
		 * @param nodes: pass all the nodes to be appended
		 * 
		 */
		  public static int appendAll(String rootName,int ... nodes) throws XMLException
		  {
			  String rootTag = "<" + rootName + "/>"; 
			    int finalNode = BSF.getXMLDocument().load(rootTag.getBytes());
		    	for(int node :nodes)
		    	{
			        if(node != 0)
			        {
			        	Node.appendToChildren(node, finalNode);
			        	
			        }
		    	}
		    	
		    	return finalNode;
		  }
		  
		  public static int[] getNodeList(final String expression,final int node)
		  {
				return  XPath.getMatchingNodes(expression, null, node);
		  }

		  public static int getNode(final String expression,final int node)
		  {	
				return XPath.getFirstMatch(expression, null, node);
		  }
		  
		
		  public static String getData(final String expression,final int node)
		  {
			    int firstChild = getNode(expression,node);
				return getData(firstChild);
		  }
		  
		  public static String getData(final int node)
		  {
			  if (BusObject._isNull(node))
			  {
			      return null;
			  }
				return Node.getData(node);
		  }
		  
		  public static int unlinkNode(final String expression,final int node)
		  {
    		  	 int firstChild = getNode(expression,node);
    		  	 
    		  	 if(firstChild > 0)
    		  	 {
    		  		 	return  Node.unlink(firstChild);
    		  	 }
    		  	 
    		  	 return 0;
		  }
		  
		  public static int setData(final int node,String value)
		  {
		  	
			   return Node.setDataElement(node, "", value);	 
		  }
		  
		   public static int setCData(final int node,String value)
		  {
		  	
			   return Node.setCDataElement(node, "", value);	 
		  }
		  
		  public static int setData(int node,final String expression,String value)
		  {
			   int firstChild = getNode(expression,node);
  			   return  setData(firstChild, value);	 
		  }
		  
		  public static void setDataToAllMatchingNodes(int node,final String expression,String value)
		  {
			   int[] childs = getNodeList(expression,node);
			   
			   for(int child: childs)
			   {
			  	 setData(child,value);
			   }

		  }
		  
		  public static int parseXML(final String xml)
		  {
			  int node = 0;
			  try 
			  {
				  node = BSF.getXMLDocument().load(xml.getBytes("UTF-8"));
			  } 
			  catch (XMLException | UnsupportedEncodingException e) 
			  {
			  			    
			    throw new RuntimeException();
			  }
			  
			  return node;
		  }
		  
		  public static String writeToString(final int node)
		  {
			   return Node.writeToString(node, true);
			   
		  }
		  
		  public static int appendChild(final int child,final int parent)
		  {
			   return Node.appendToChildren(child,parent);
			   
		  }
		  
		  
		  public static void setAttribute(final int node, String attributeName, String attributeValue)
		  {
			  Node.setAttribute(node, attributeName, attributeValue);
		  }	 
		  
		  public static void setName(final int node, String name)
		  {
			  		Node.setName(node, name);
		  }	
		  
		  public static void cleanAllNodes(int node,final String expression)
		  {  		  	
				   int[] childs = getNodeList(expression,node);	
				  cleanAll(childs);			   	  	
		  }
		  
		  public static void cleanAll(final int ... nodes)
	    {
	    	for(final int node :nodes)
	    	{
		        if(node != 0)
		        {
		        	  if(Node.getParent(node) > 0)
		        	  Node.unlink(node);	        	
		        	  Node.delete(node);
		        }
	    	}
	    }


}
