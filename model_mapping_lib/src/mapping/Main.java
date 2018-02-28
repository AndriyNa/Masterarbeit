package mapping;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import situationtemplate.model.TSituationTemplate;

public class Main {

	private static boolean templateSelected = false;
	private static String nameOftemplate = "";

	/**
	 * main method for debug reasons
	 * 
	 * @param args
	 *            0 Path to objectID<->SensorID mapping 1 path to a valid
	 *            situation template defined in XML 2 debug flag
	 */
	public static void main(String[] args) {
		try {
			selectTemplateFromDB();
			while (!templateSelected) {
				Thread.sleep(500);
			}

			String url = "http://localhost:5984/situationtemplates/" + nameOftemplate + "/attachment";
			URL obj;

			obj = new URL(url);

			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Accept", "application/xml");

			InputStream xml = con.getInputStream();

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db;

			db = dbf.newDocumentBuilder();
			Document doc = db.parse(xml);
			executeMapping(doc);

		} catch (IOException | InterruptedException | ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void selectTemplateFromDB() throws IOException {
		ArrayList<String> availableTemplates = new ArrayList<String>();
		String url = "http://localhost:5984/situationtemplates/_all_docs";
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			String[] parts = inputLine.split(",");
			String fetchedName = "";
			for (int k = 0; k < parts.length; k++) {
				if (parts[k].contains("id")) {
					fetchedName = parts[k].substring(parts[k].indexOf(":") + 2, parts[k].length() - 1);
					availableTemplates.add(fetchedName);
				}
			}
		}
		JFrame frame = new JFrame("Select the Situationstemplate");
		JButton buttonSelect = new JButton("Select");
		frame.setVisible(true);
		frame.setLayout(new FlowLayout(FlowLayout.LEFT, 100, 100));
		ArrayList<String> tempList = new ArrayList<String>();
		for (String str : availableTemplates) {
			tempList.add(str);
		}
		// Einfach parts[] - nehmen ? einfacher und besser....
		String[] SituationTitles = new String[tempList.size()];
		SituationTitles = tempList.toArray(SituationTitles);

		JComboBox comboBox1 = new JComboBox(SituationTitles);
		comboBox1.setForeground(Color.BLUE);
		comboBox1.setFont(new Font("Arial", Font.BOLD, 14));

		buttonSelect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				String selectedItem = (String) comboBox1.getSelectedItem();
				System.out.println(selectedItem);
				setNameOftemplate(selectedItem);
				setTemplateSelected(true);
				frame.dispose();
			}
		});
		frame.add(comboBox1);
		frame.add(buttonSelect);

		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);

	}

	public static void executeMapping(Document doc) {

		try {

			// connects each node to a corresponding debug node
			// deactivate this flag for measurements
			boolean debug;
			debug = false;

			java.util.Date date = new java.util.Date();

			// input is defined, parse the XML model

			JAXBContext jc = JAXBContext.newInstance(TSituationTemplate.class);
			Unmarshaller u = jc.createUnmarshaller();
			JAXBElement<TSituationTemplate> root;
			root = u.unmarshal(doc, TSituationTemplate.class);

			TSituationTemplate situationTemplate = root.getValue();

			situationTemplate.setId(situationTemplate.getId());

			long timestamp = date.getTime();
			Integer port = 1880;

			Mapper mapper = new Mapper(situationTemplate);
			mapper.map(true, timestamp, debug, port,false);

		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}

	public boolean isTemplateSelected() {
		return templateSelected;
	}

	public static void setTemplateSelected(boolean templateSel) {
		templateSelected = templateSel;
	}

	public static String getNameOftemplate() {
		return nameOftemplate;
	}

	public static void setNameOftemplate(String nameOftemplate) {
		Main.nameOftemplate = nameOftemplate;
	}

}
