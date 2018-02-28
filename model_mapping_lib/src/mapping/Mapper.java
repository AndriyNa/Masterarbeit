package mapping;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import situationtemplate.model.ObjectFactory;
import situationtemplate.model.TContextNode;
import situationtemplate.model.TOperationNode;
import situationtemplate.model.TParent;
import situationtemplate.model.TSituation;
import situationtemplate.model.TSituationNode;
import situationtemplate.model.TSituationTemplate;
import utils.IOUtils;
import utils.NodeREDUtils;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

/**
 * This is the entry point of the rule-based mapping library. The pattern-based
 * model will be transformed into a Node-RED-processable model.
 *
 */
public class Mapper {

	/**
	 * The situation template to be mapped
	 */
	static TSituationTemplate situationTemplate;
	private static boolean adHocDistributable = false;

	/**
	 * Class constructor
	 * 
	 * @param situation
	 *            instance of the situation template
	 */
	@SuppressWarnings("static-access")
	public Mapper(TSituationTemplate situation) {
		this.situationTemplate = situation;
	}

	/**
	 * Main class of the mapping that receives the pattern-based model as XML
	 * and invokes methods to transform it into an executable model in JSON.
	 * 
	 * @param topic
	 *            the URL of the machine
	 * @param debug
	 */
	public void map(boolean doOverwrite, long timestamp, boolean debug, Integer port, boolean isInputTemplate) {
		JSONObject nodeREDModel = new JSONObject();
		JSONArray nodes = new JSONArray();
		ArrayList<String> thingsArrList = new ArrayList<>();
		ArrayList<String> topicsArrList = new ArrayList<>();
		ArrayList<String> IPsArrList = new ArrayList<>();
		ArrayList<TContextNode> ctxtNodeArrList = new ArrayList<>();

		TSituation nodeContainer = situationTemplate.getSituation();
		JSONObject brokerNode = NodeREDUtils.createBroker();

		for (TContextNode ctxt : nodeContainer.getContextNode()) {
			if (ctxt.getInputType().equals("sensor")) {
				ctxtNodeArrList.add(ctxt);
			}
		}

		selectSensorThings(nodeContainer, thingsArrList, topicsArrList);

		HashMap<String, String> thingAndIp = new HashMap<>();

		for (int j = 0; j < thingsArrList.size(); j++) {
			String subTopic = "";
			String selectedThing = thingsArrList.get(j);
			String[] splitThingFromIP = selectedThing.split(" ");
			thingAndIp.put(splitThingFromIP[0], splitThingFromIP[1]);
			IPsArrList.add(splitThingFromIP[1]);
			subTopic = splitThingFromIP[0] + "/" + topicsArrList.get(j);
			topicsArrList.set(thingsArrList.indexOf(selectedThing), subTopic);
		}
		
		
		//createNewGoalTemplate(situationTemplate, adHocAnalysis(IPsArrList, thingsArrList, ctxtNodeArrList));
		startMapping(nodeREDModel, nodes, nodeContainer, brokerNode, debug, doOverwrite, thingsArrList, topicsArrList,
				port, isInputTemplate, IPsArrList);
	}

	private void createNewGoalTemplate(TSituationTemplate situationTemplate2, ArrayList<String> adHocAnalysis) {
		
	}

	private void selectSensorThings(TSituation nodeContainer, ArrayList<String> thingsArrList,
			ArrayList<String> topicsArrList) {
		try {
			ArrayList<String> thingList = requestThingList();
			ArrayList<TContextNode> sensorInputList = new ArrayList<TContextNode>();
			for (int i = 0; i < nodeContainer.getContextNode().size(); i++) {
				if (nodeContainer.getContextNode().get(i).getInputType().equals("sensor")) {
					sensorInputList.add(nodeContainer.getContextNode().get(i));
				}
			}

			for (int j = 0; j < sensorInputList.size(); j++) {
				TContextNode sensorCtxt = sensorInputList.get(j);
				while (thingsArrList.size() != j) {
					Thread.sleep(500);
				}

				String nameSensorNode = sensorCtxt.getName();
				String thingTypeSensorNode = sensorCtxt.getThingType();
				String senType = sensorCtxt.getSensorType();
				topicsArrList.add(senType);
				HashMap<String, String> thingAndIp = selectApplicableThings(thingTypeSensorNode, thingList);
				selectThings(nameSensorNode, thingAndIp, thingsArrList, nodeContainer.getName());
			}
			// Warten bis die Things ausgewählt sind
			while (thingsArrList.size() != sensorInputList.size()) {
				Thread.sleep(500);
			}

		} catch (IOException | InterruptedException e) {
			
			e.printStackTrace();
		}

	}

	public JSONArray nestedMap(TSituationTemplate nestedModule) {
		JSONArray nestedNodes = new JSONArray();
		return nestedNodes;
	}

	/**
	 * Get the nested situation-contextnode from db, unmarshall it with JAXB.
	 * 
	 * @param thingsArrList
	 * @param topicsArrList
	 * @param port
	 * @param thingAndIp
	 * 
	 * @param name
	 *            - name of the nested situation-contextnode
	 * @throws MalformedURLException
	 * @throws ProtocolException
	 */

	@SuppressWarnings("unchecked")
	public static void startMapping(JSONObject nodeREDModel, JSONArray nodes, TSituation nodeContainer,
			JSONObject brokerNode, boolean debug, boolean doOverwrite, ArrayList<String> thingsArrList,
			ArrayList<String> topicsArrList, Integer port, boolean isInputeTemplate, ArrayList<String> IPsArrList) {

		String[] things = new String[thingsArrList.size()];
		things = thingsArrList.toArray(things);

		String[] topics = new String[topicsArrList.size()];
		topics = topicsArrList.toArray(topics);

		String inputSits = "";

		if (isInputeTemplate) {
			ContextNodeMapper.inputSituationThings.put(situationTemplate.getName(), thingsArrList);
		}

		// first, map all the operation nodes, then map the other nodes
		OperationNodeMapper lnm = new OperationNodeMapper();
		lnm.mapOperationNodes(nodeContainer, nodes, situationTemplate.getId(), brokerNode, things);

		ContextNodeMapper snm = new ContextNodeMapper();
		snm.mapContextNodes(situationTemplate, nodes, debug, brokerNode, things, topics);

		java.util.Date beginDate = new java.util.Date();

		long begin = beginDate.getTime();
		// deploy the flow to NodeRED

		for (TContextNode ctxtNode : nodeContainer.getContextNode()) {
			if (ctxtNode.getInputType().equals("situation")) {
				inputSits += "&" + ctxtNode.getName();
			}
		}

		nodeREDModel.put("nodes", nodes);
		String deployId = IOUtils.deployToNodeRED(nodeREDModel, situationTemplate, things, inputSits, doOverwrite,
				port);
		// }

		Date endDate = new Date();
		System.out.println("Deploy Time: " + (begin - endDate.getTime()));
		System.out.println(deployId);
	}

	private static ArrayList<String> adHocAnalysis(ArrayList<String> iPsArrList, ArrayList<String> thingsArrList,
			ArrayList<TContextNode> ctxtNodeArrList) {
		HashMap<String, ArrayList<TContextNode>> IpGroups = new HashMap<>();

		for (int i = 0; i < iPsArrList.size(); i++) {
			String IPkey = "";
			ArrayList<TContextNode> innerGroup = new ArrayList<>();
			for (TParent parent : ctxtNodeArrList.get(i).getParent()) {
				IPkey = ((TOperationNode) parent.getParentID()).getId() + iPsArrList.get(i);
				innerGroup.add(ctxtNodeArrList.get(i));
				for (int j = i + 1; j < iPsArrList.size(); j++) {
					if ((iPsArrList.get(i).equals(iPsArrList.get(j))) && (i != j)) {
						for (TParent parent2 : ctxtNodeArrList.get(j).getParent()) {
							if (((TOperationNode) parent.getParentID()).getId()
									.equals(((TOperationNode) parent2.getParentID()).getId())) {
								innerGroup.add(ctxtNodeArrList.get(j));

								// System.out.println("Elements with same IPs
								// are : " + ctxtNodeArrList.get(j).getId()
								// + " " + thingsArrList.get(j) + " and " +
								// ctxtNodeArrList.get(i).getId() + " "
								// + thingsArrList.get(i));
							}
						}
					}
				}
			}
			if (!IpGroups.containsKey(IPkey)) {
				if (innerGroup.size() > 1)
					;
				IpGroups.put(IPkey, innerGroup);
			}

		}
		ArrayList<String> namesOfSubs = produceSubtemplateXML(IpGroups, situationTemplate);
		return namesOfSubs;

	}

	private static ArrayList<String> produceSubtemplateXML(HashMap<String, ArrayList<TContextNode>> ipGroups,
			TSituationTemplate situationTemplate) {
		ArrayList<String> NamesOfSubs = new ArrayList<>();
		for (Map.Entry<String, ArrayList<TContextNode>> entry : ipGroups.entrySet()) {
			System.out.println(entry.getKey());
			String composedKey = entry.getKey().toString();
			try {
				String formatEntry = composedKey.substring(0, composedKey.length() - 1);
				String[] entryParts = formatEntry.split("\\(");

				ObjectFactory factory = new ObjectFactory();

				File file = new File("C:\\Users\\Andriy\\Desktop\\xml\\created\\" + "Sub" + entryParts[1] + "_"
						+ situationTemplate.getName() + ".xml");

				JAXBContext jaxbContext = JAXBContext.newInstance(TSituationTemplate.class);
				Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
				jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

				TSituation situation = factory.createTSituation();
				situation.setName("Sub" + entry.getKey() + "_" + situationTemplate.getName());

				TSituationTemplate subTemplate = new TSituationTemplate();
				subTemplate.setName("Sub_for_" + entryParts[1] + "_in_" + situationTemplate.getName());
				subTemplate.setId("Sub_for_" + entryParts[1] + "_in_" + situationTemplate.getName());

				TSituationNode sitNOde = factory.createTSituationNode();
				sitNOde.setId(entry.getKey());
				sitNOde.setName(entry.getKey());
				situation.setSituationNode(sitNOde);

				TOperationNode opNode = factory.createTOperationNode();
				opNode.setId(entryParts[0]);
				opNode.setName(entryParts[0]);
				TParent typeOfThisEntryOp = entry.getValue().get(0).getParent().get(0);
				opNode.setType(((TOperationNode) typeOfThisEntryOp.getParentID()).getType());
				situation.getOperationNode().add(opNode);

				TContextNode contextNode = factory.createTContextNode();
				for (int k = 0; k < entry.getValue().size(); k++) {
					contextNode = entry.getValue().get(k);
					situation.getContextNode().add(contextNode);
				}
				subTemplate.setSituation(situation);
				JAXBElement<TSituationTemplate> sitTemplate = factory.createSituationTemplate(subTemplate);

				jaxbMarshaller.marshal(sitTemplate, file);
				String NameOfSub = subTemplate.getName();
				NamesOfSubs.add(entryParts[0] + "_" + NameOfSub);
				uploadSubtemplate(file, subTemplate.getName());

			} catch (JAXBException | IOException | ParseException e) {
				e.printStackTrace();
			}
		}
		return NamesOfSubs;
	}

	@SuppressWarnings("unchecked")
	private static void uploadSubtemplate(File file, String name) throws IOException, ParseException {

		JSONObject newSubTemp = new JSONObject();
		newSubTemp.put("_id", name);
		String body = newSubTemp.toJSONString();

		System.out.println(body);

		String url = "http://localhost:5984/situationtemplates/";
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		con.setRequestMethod("POST");
		con.setDoOutput(true);
		con.setDoInput(true);
		con.setUseCaches(false);
		con.setRequestProperty("Content-Type", "application/json");

		OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());

		BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

		JSONObject obj2 = (JSONObject) new JSONParser().parse(reader);

		writer.write(body);
		writer.flush();
		writer.close();

		reader.close();
		con.disconnect();

		System.out.println((String) obj2.get("id"));
	}

	private HashMap<String, String> selectApplicableThings(String thingTypeSensorNode, ArrayList<String> thingList)
			throws IOException {
		HashMap<String, String> thingWithIP = new HashMap<String, String>();
		for (int i = 0; i < thingList.size(); i++) {
			String url = "http://localhost:5984/things/" + thingList.get(i);
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				String[] parts = inputLine.split(",");
				for (int k = 0; k < parts.length; k++) {
					int beginIndexType = 0;
					int beginIndexIP = 0;
					if (parts[k].contains("thingType")) {
						beginIndexType = parts[k].indexOf("thingType") + 12;
						String thingType = parts[k].substring(beginIndexType, parts[k].length() - 1);

						if (thingType.equals(thingTypeSensorNode)) {

							beginIndexIP = parts[k + 1].indexOf("edgeNodeIP") + 14;
							parts[k + 1].substring(beginIndexIP, parts[k + 1].length() - 1);
							String edgeNodeIP = parts[k + 1].substring(beginIndexType, parts[k + 1].length() - 1);
							thingWithIP.put(thingList.get(i), edgeNodeIP);

						}
					}
					// int indNum = inputLine.indexOf("thingType") + 12;

				}
			}
		}
		return thingWithIP;
	}

	/**
	 * Accesss to the central database to retrieve the list with all of things.
	 * 
	 * @return list of things from database
	 * @throws IOException
	 */
	private ArrayList<String> requestThingList() throws IOException {
		ArrayList<String> ThingNames = new ArrayList<String>();
		String url = "http://localhost:5984/things/_all_docs";
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		// StringBuffer response = new StringBuffer();
		// TODO Better parsing
		while ((inputLine = in.readLine()) != null) {
			// response.append(inputLine);
			if (inputLine.contains("id")) {
				int indNum = inputLine.indexOf("id") + 5;
				if (indNum > 0) {
					int delimNum = inputLine.indexOf(",") - 1;
					String ThingName = inputLine.substring(indNum, delimNum);
					ThingNames.add(ThingName);
				}
			}
		}
		in.close();
		// for (int i = 0; i < ThingNames.size(); i++) {
		// System.out.println(ThingNames.get(i));
		// }

		return ThingNames;
	}

	private synchronized void selectThings(String nameSensorNode, HashMap<String, String> thingAndIp,
			ArrayList<String> thingsArrList, String templateName) {
		JFrame frame = new JFrame("Select thing for " + nameSensorNode + "(" + situationTemplate.getName() + ")");
		// JTextField templatName = new JTextField(situationTemplate.getName());
		JButton buttonSelect = new JButton("Select");
		frame.setPreferredSize(new Dimension(700, 300));
		frame.setVisible(true);
		frame.setLayout(new FlowLayout(FlowLayout.LEFT, 100, 100));
		ArrayList<String> tempList_thing_ip = new ArrayList<String>();
		for (String str : thingAndIp.keySet()) {

			if (thingAndIp.get(str).replace("\"", "").equals("")) {
				tempList_thing_ip.add(str + " (None)");
			} else

				tempList_thing_ip.add(str + " (" + thingAndIp.get(str).replace("\"", "") + ")");

		}

		String[] thingTitles = new String[tempList_thing_ip.size()];
		thingTitles = tempList_thing_ip.toArray(thingTitles);

		@SuppressWarnings({ "rawtypes", "unchecked" })
		JComboBox comboBox1 = new JComboBox(thingTitles);
		comboBox1.setForeground(Color.BLUE);
		comboBox1.setFont(new Font("Arial", Font.BOLD, 14));

		buttonSelect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				String selectedItem = (String) comboBox1.getSelectedItem();
				thingsArrList.add(selectedItem);
				frame.dispose();
			}
		});

		// frame.add(templatName);
		frame.add(comboBox1);
		frame.add(buttonSelect);

		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
	}

	public static boolean isAdHocDistributable() {
		return adHocDistributable;
	}

	public static void setAdHocDistributable(boolean adHocDistributable) {
		Mapper.adHocDistributable = adHocDistributable;
	}
}
