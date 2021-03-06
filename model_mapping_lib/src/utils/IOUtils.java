package utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import mapping.ObjectIdSensorIdMapping;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import org.json.simple.parser.ParseException;
import situationtemplate.model.TSituationTemplate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Utility class for input and output
 */
public class IOUtils {

	/**
	 * This methods writes the JSON string to a local file.
	 * 
	 * @param nodeREDModel
	 *            the nodeRED JSON flow model
	 * @param situationTemplate
	 *            the situation template of the model
	 */
	@SuppressWarnings("unchecked")
	public static void writeJSONFile(JSONArray nodeREDModel, TSituationTemplate situationTemplate) {
		try {
			JSONArray flow = new JSONArray();
			JSONObject sheet = new JSONObject();

			// TODO create constant for tab
			sheet.put("type", "tab");
			sheet.put("id", situationTemplate.getId());
			sheet.put("label", situationTemplate.getId() + ": " + situationTemplate.getName());

			// add the sheet definition and all other components to the node red
			// flow
			flow.add(sheet);

			for (int i = 0; i < nodeREDModel.size(); i++) {
				flow.add(nodeREDModel.get(i));
			}

			// pretty print json
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			String json = gson.toJson(flow);
			System.out.println(json);

			// specify path here
			String path = "mappingOutput.json";

			Files.delete(Paths.get(path));
			Files.write(Paths.get(path), json.getBytes(), StandardOpenOption.CREATE);
		} catch (IOException e) {
			System.err.println("Could not write the json file, an error occurred.");
			e.printStackTrace();
		}
	}

	/**
	 * Clears all flows currently deployed in Node-RED
	 */
	public static void clearNodeRED() {
		try {

			JSONArray flow = new JSONArray();

			String body = flow.toJSONString();

			URL url;

			String server = constants.Properties.getServer();
			server = server == null ? "localhost" : server;
			String protocol = constants.Properties.getProtocol();
			protocol = protocol == null ? "http" : protocol;
			String port = constants.Properties.getPort();
			port = port == null ? "1880" : port;

			url = new URL(String.format("%s://%s:%s/flows", protocol, server, port));

			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("charset", "UTF-8");

			OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());

			writer.write(body);

			writer.flush();

			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			writer.close();
			reader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * This method deploys the model to NodeRED
	 * 
	 * @param nodeREDModel
	 *            the model to be deployed
	 * @param situationTemplate
	 *            the situation template as XML
	 * @param doOverwrite
	 *            boolean determining whether the flow shall be overwritten or
	 *            not
	 * @param setPort 
	 */
	@SuppressWarnings("unchecked")
	public static String deployToNodeRED(JSONObject nodeREDModel, TSituationTemplate situationTemplate, String[] things, String inputSituations,
			boolean doOverwrite, Integer setPort) {
		try {
			Properties prop = new Properties();

			String server = "localhost";
			String protocol = "http";
			String port = setPort.toString();
			System.out.println(port);

			try (InputStream input = new FileInputStream("settings.properties")) {
				prop.load(input);
				server = prop.getProperty("server");
				protocol = prop.getProperty("protocol");
				port = prop.getProperty("port");
			} catch (Exception e) {
				e.printStackTrace();
			}

			String concatThingNames = "";
			for (int i = 0; i < things.length; i++) {
				if (i == 0) {
					concatThingNames += things[i];
				} else {
					concatThingNames += "&" + things[i];
				}
			}
			String id = concatThingNames + "." + situationTemplate.getName();
			
			String allContexts = concatThingNames + inputSituations;

			String put = hasFlow(String.format("%s://%s:%s/flows", protocol, server, port), concatThingNames,
					situationTemplate.getName());
			if (put != null && doOverwrite) {
				put = "";
				delete(String.format("%s://%s:%s/flow/%s", protocol, server, port, id));
			}
			
			nodeREDModel.put("type", "tab");
			nodeREDModel.put("id", id);
			nodeREDModel.put("label", allContexts + "/" + situationTemplate.getName());
			nodeREDModel.put("configs", new JSONArray());

			// pretty print json
			// Gson gson = new GsonBuilder().setPrettyPrinting().create();
			// String json = gson.toJson(flow);
			// System.out.println(json);

			// we use this POST call to deploy the JSON
			// $.ajax({
			// url:"flows",
			// type: "POST",
			// data: JSON.,
			// contentType: "application/json; charset=utf-8"
			// });

			String body = nodeREDModel.toJSONString();
			System.out.println(nodeREDModel.toJSONString());
			// String altPut = NodeREDUtils.generateNodeREDId();
			URL url;
			if (put != null) {
				url = new URL(String.format("%s://%s:%s/flow/%s", protocol, server, port, put));
			} else {
				url = new URL(String.format("%s://%s:%s/flow/", protocol, server, port));

			}
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod(put != null ? "PUT" : "POST");
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("charset", "UTF-8");

			OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
//			System.out.println(connection.getRequestProperties());
			writer.write(body);
			writer.flush();

			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			JSONObject obj = (JSONObject) new JSONParser().parse(reader);

			writer.close();
			reader.close();
			connection.disconnect();
			return (String) obj.get("id");

		} catch (IOException | ParseException e1) {
			e1.printStackTrace();
		}
		return "";
	}

	private static void delete(String urlToRead) {
		URL url;
		HttpURLConnection conn;
		try {
			url = new URL(urlToRead);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("DELETE");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.connect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This methods gets the currently deployed flows in Node-RED
	 * 
	 * @param urlToRead
	 *            the url of Node-RED
	 * @return the current flow
	 */
	public static String hasFlow(String urlToRead, String objects, String templateName) {
		URL url;
		HttpURLConnection conn;
		try {
			url = new URL(urlToRead);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Content-Type", "application/json");
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
				JSONArray array = (JSONArray) new JSONParser().parse(reader);
				for (Object o : array) {
					JSONObject obj = (JSONObject) o;
					if (obj.get("label") != null && obj.get("label").equals(objects + "/" + templateName)) {
						return (String) obj.get("id");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
