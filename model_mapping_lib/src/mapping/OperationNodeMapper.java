package mapping;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import constants.Nodes;
import constants.Properties;
import situationtemplate.model.TContextNode;
import situationtemplate.model.TOperationNode;
import situationtemplate.model.TParent;
import situationtemplate.model.TSituation;
import situationtemplate.model.TSituationNode;
import situationtemplate.model.TSituationTemplate;
import utils.NodeREDUtils;

/**
 * This class maps the operation nodes of the situation template to
 * corresponding NodeRED implementations
 */
public class OperationNodeMapper {

	/**
	 * This method processes the mapping of operation nodes
	 * 
	 * @param nodeContainer
	 *            the situation template to be mapped
	 * @param nodeREDModel
	 *            the existing flow defined in NodeRed JSON
	 * @param idOfTemplate
	 * @param brokerNode
	 * 
	 * @return the mapped model
	 */
	@SuppressWarnings("unchecked")
	public JSONArray mapOperationNodes(TSituation nodeContainer, JSONArray nodeREDModel, String idOfTemplate,
			JSONObject brokerNode, String[] things) {

		// TODO those are just random values, write style function!
		int xCoordinate = 900;
		int yCoordinate = 50;

		// get the number of children of the operation node
		int children = 0;

		for (TOperationNode logicNode : nodeContainer.getOperationNode()) {
			for (TContextNode contextChild : nodeContainer.getContextNode()) {
				for (TParent parent : contextChild.getParent()) {

					if (((TOperationNode) parent.getParentID()).getId().equals(logicNode.getId())) {
						children++;
					}
				}
			}

			// create the comparison node in NodeRED
			JSONObject nodeREDNode = NodeREDUtils.createNodeREDNode(logicNode.getId() + "." + logicNode.getName(),
					logicNode.getName(), "function", Integer.toString(xCoordinate), Integer.toString(yCoordinate));
			Method m;
			ArrayList<String> parentIds = new ArrayList<>();
			parentIds.add(logicNode.getId());
			ArrayList<TContextNode> sensors = new ArrayList<>();

			for (TOperationNode node : nodeContainer.getOperationNode()) {
				for (TParent parent : node.getParent()) {
					if (parent.getParentID() instanceof TSituationNode) {
						if (parentIds.contains(((TSituationNode) parent.getParentID()).getId())) {
							parentIds.add(node.getId());
						}
					} else {
						if (parentIds.contains(((TOperationNode) parent.getParentID()).getId())) {
							parentIds.add(node.getId());
						}
					}
				}
			}


			for (TContextNode node : nodeContainer.getContextNode()) {
				for (TParent parent : node.getParent()) {
					if (parentIds.contains(((TOperationNode) parent.getParentID()).getId())) {
						parentIds.add(node.getId());
					}
				}
			}

			for (TContextNode node : nodeContainer.getContextNode()) {
				for (TParent parent : node.getParent()) {
					if (parent.getParentID() instanceof TOperationNode
							&& parentIds.contains(((TOperationNode) parent.getParentID()).getId())) {
						sensors.add(node);
					}
				}
			}

			// String sensorIdMapping = sensorMapping.map(sensors);

			try {
				m = Nodes.class.getMethod("get" + logicNode.getType().toUpperCase() + "Node", String.class,
						String.class, String.class, String.class);
			} catch (NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}

			String concatThingNames = "";
			for (int i = 0; i < things.length; i++) {
				if (i == 0) {
					concatThingNames += things[i];
				} else {
					concatThingNames += "&" + things[i];
				}
			}

			for (TContextNode ctxtNode : nodeContainer.getContextNode()) {
				if (ctxtNode.getInputType().equals("situation")) {
					concatThingNames += "&" + ctxtNode.getName();
				}
			}

			try {
				nodeREDNode.put("func",
						m.invoke(null, Integer.toString(children), concatThingNames, idOfTemplate, concatThingNames));
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}

			// connect it to the parent(s)
			JSONArray connections = new JSONArray();

			if (!logicNode.getParent().isEmpty()) {
				for (TParent parent : logicNode.getParent()) {
					if (parent.getParentID() instanceof TOperationNode) {
						String parentName = ((TOperationNode) parent.getParentID()).getName();
						String parentID = ((TOperationNode) parent.getParentID()).getId();
						connections.add(parentID + "." + parentName);
						// Creating a mqtt-out in case if the operation is
						// connected directly to the situationNode
					} else if (parent.getParentID() instanceof TSituationNode) {
						JSONObject mqttOutput = NodeREDUtils.createNodeREDNode(NodeREDUtils.generateNodeREDId(),
								((TSituationNode) parent.getParentID()).getName(), "mqtt out",
								Integer.toString(xCoordinate), Integer.toString(yCoordinate));
						mqttOutput.put("broker", brokerNode.get("id"));
						mqttOutput.put("topic",
								"situations/" + concatThingNames + "/" + nodeContainer.getSituationNode().getName());
						// mqttOutput.put("topic", "situations");
						mqttOutput.put("qos", "");
						mqttOutput.put("retain", "");
						nodeREDModel.add(mqttOutput);
						connections.add(mqttOutput.get("id"));
					}
				}
			} else {
				// JSONObject debugNode = NodeREDUtils.generateDebugNode("600",
				// "500");
				// debugNode.put("name", nodeContainer.getName());
				// nodeREDModel.add(debugNode);
				// connections.add(debugNode.get("id"));
			}

			nodeREDNode.put("outputs", String.valueOf(1));
			nodeREDNode.put("wires", connections);
			nodeREDModel.add(nodeREDNode);

			yCoordinate += 100;
		}

		return nodeREDModel;
	}
}
