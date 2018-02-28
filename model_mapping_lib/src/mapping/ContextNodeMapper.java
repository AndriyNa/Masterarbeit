package mapping;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import constants.Nodes;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import constants.Properties;
import situationtemplate.model.*;
import utils.NodeREDUtils;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactoryConfigurationError;

/**
 * This class maps context nodes to HTTP nodes in NodeRED
 */
public class ContextNodeMapper {

	/**
	 * constants
	 */
	public static final String TYPE = "http request";
	public static final String METHOD = "GET";
	public static final HashMap<String, ArrayList<String>> inputSituationThings = new HashMap<>();

	/**
	 * Maps the context nodes to corresponding NodeRED nodes
	 *
	 * @param situationTemplate
	 *            the situation template JAXB node
	 * @param nodeREDModel
	 *            the Node-RED flow as JSON
	 * @param thing
	 * @param brokerNode
	 * @param topics
	 *
	 * @return the mapped JSON model
	 */
	@SuppressWarnings({ "unchecked" })
	public JSONArray mapContextNodes(TSituationTemplate situationTemplate, JSONArray nodeREDModel, boolean debug,
			JSONObject brokerNode, String[] things, String[] topics) {

		int xCoordinate = 300;
		int yCoordinate = 50;

		int counter = 0;
		for (TContextNode ctxtNode : situationTemplate.getSituation().getContextNode()) {
			if (ctxtNode.getInputType().equals("sensor")) {
				String opType = ctxtNode.getOpType();
				String value = ctxtNode.getValue();
			
				
				String NRId_part = NodeREDUtils.generateNodeREDId();

				JSONObject nodeREDContextNode = NodeREDUtils.createNodeREDNode(NRId_part + "_context",
						ctxtNode.getName() + "/" + things[counter], "mqtt in", Integer.toString(xCoordinate),
						Integer.toString(yCoordinate));
				nodeREDContextNode.put("qos", "0");
				nodeREDContextNode.put("broker", brokerNode.get("id"));
				nodeREDContextNode.put("topic", topics[counter]);

				JSONObject condition = NodeREDUtils.createNodeREDNode(NRId_part + "_condition",
						ctxtNode.getName() + opType + value, "function", Integer.toString(xCoordinate),
						Integer.toString(yCoordinate));
				condition.put("outputs", 1);

				Object methodReturn = null;

				try {
					methodReturn = Nodes.class
							.getMethod("get" + opType.toUpperCase() + "Node", String.class, String.class, String.class,
									String.class, String.class, String.class)
							.invoke(null, value, null, situationTemplate.getId(), ctxtNode.getId(), "0", "0");

				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
						| NoSuchMethodException | SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				JSONArray MqqtConnections = new JSONArray();
				JSONArray CondConnections = new JSONArray();

				for (TParent parent : ctxtNode.getParent()) {
					if (parent.getParentID() instanceof TOperationNode) {
						CondConnections.add(((TOperationNode) parent.getParentID()).getId() + "."
								+ ((TOperationNode) parent.getParentID()).getName());
					}
				}

				MqqtConnections.add(condition.get("id"));
				condition.put("func", methodReturn);
				condition.put("wires", CondConnections);
				nodeREDModel.add(condition);
				nodeREDModel.add(brokerNode);
				nodeREDContextNode.put("wires", MqqtConnections);
				nodeREDModel.add(nodeREDContextNode);
				counter++;
			} else if (ctxtNode.getInputType().equals("situation")) {
				
				try {
					getSituationFromDB(ctxtNode.getName());
				
				JSONObject situationContextNode = NodeREDUtils.createNodeREDNode(
						ctxtNode.getSituationInput() + "_situationInput", ctxtNode.getName(), "mqtt in",
						Integer.toString(xCoordinate), Integer.toString(yCoordinate));
				situationContextNode.put("qos", "0");
				situationContextNode.put("broker", brokerNode.get("id"));
				// situationContextNode.put("topic", "situations");
				
				String inputTemplateName = "";
				for (String thing : inputSituationThings.get(ctxtNode.getSituationInput())){
					inputTemplateName += "&" + thing;
				}
				
				if (inputTemplateName.startsWith("&")){
					inputTemplateName = inputTemplateName.substring(1);
				}
				
				situationContextNode.put("topic", "situations/" + inputTemplateName + "/" + ctxtNode.getSituationInput());

				JSONObject fetchBoolVal = NodeREDUtils.createNodeREDNode(ctxtNode.getSituationInput() + "_occurrence",
						ctxtNode.getName() + " occured?", "function", Integer.toString(xCoordinate),
						Integer.toString(yCoordinate));
				fetchBoolVal.put("outputs", 1);

				Object methodReturn = null;
				try {
					methodReturn = Nodes.class.getMethod("fetchBoolVal").invoke(null);

				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
						| NoSuchMethodException | SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				JSONArray SitConnections = new JSONArray();
				JSONArray fetchConnections = new JSONArray();

				for (TParent parent : ctxtNode.getParent()) {
					if (parent.getParentID() instanceof TOperationNode) {
						SitConnections.add(((TOperationNode) parent.getParentID()).getId() + "."
								+ ((TOperationNode) parent.getParentID()).getName());
					}
				}
				fetchConnections.add(fetchBoolVal.get("id"));
				fetchBoolVal.put("func", methodReturn);
				fetchBoolVal.put("wires", SitConnections);
				nodeREDModel.add(fetchBoolVal);
				situationContextNode.put("wires", fetchConnections);
				nodeREDModel.add(situationContextNode);
				nodeREDModel.add(brokerNode);

				} catch (MalformedURLException | ProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		Mapper.situationTemplate = situationTemplate;
		return nodeREDModel;
	}

	// TODO: Eigene klasse hierfür
	private void getSituationFromDB(String name) throws MalformedURLException, ProtocolException {
		String url = "http://localhost:5984/situationtemplates/" + name + "/attachment";
		URL obj;

		try {
			obj = new URL(url);

			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Accept", "application/xml");

			InputStream xml = con.getInputStream();

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db;

			db = dbf.newDocumentBuilder();
			Document doc = db.parse(xml);

			JAXBContext jc = JAXBContext.newInstance(TSituationTemplate.class);
			Unmarshaller u = jc.createUnmarshaller();
			JAXBElement<TSituationTemplate> root;
			root = u.unmarshal(doc, TSituationTemplate.class);

			TSituationTemplate nestedSituationTemplate = root.getValue();
			nestedSituationTemplate.setId(nestedSituationTemplate.getId());
			TSituation nestedNodeContainer = nestedSituationTemplate.getSituation();

			java.util.Date date = new java.util.Date();
			long timestamp = date.getTime();
			Integer port = 1880;
			boolean debug = false;

			Mapper mapper = new Mapper(nestedSituationTemplate);
			mapper.map(false, timestamp, debug, port,true);

		} catch (IOException | SAXException | ParserConfigurationException | TransformerFactoryConfigurationError
				| JAXBException e) {
			e.printStackTrace();
		}

	}

}
