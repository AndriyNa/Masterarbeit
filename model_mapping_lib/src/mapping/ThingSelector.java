//package mapping;
//
//import java.awt.Color;
//import java.awt.FlowLayout;
//import java.awt.Font;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.util.ArrayList;
//import java.util.HashMap;
//
//import javax.swing.DefaultComboBoxModel;
//import javax.swing.JButton;
//import javax.swing.JComboBox;
//import javax.swing.JFrame;
//import javax.swing.JOptionPane;
//import javax.swing.SwingUtilities;
//import javax.swing.UIManager;
//
//import org.json.simple.JSONArray;
//import org.json.simple.JSONObject;
//
//import situationtemplate.model.TSituation;
//
///**
// * A Swing program that demonstrates how to create and use JComboBox component.
// * 
// * @author www.codejava.net
// * 
// */
//@SuppressWarnings("serial")
//public class ThingSelector extends JFrame {
//	
//	String selectedThing;
//
//	private JButton buttonSelect = new JButton("Select");
//
//	public ThingSelector(String nameOfNode, HashMap<String,String> thingWithIp, JSONObject nodeREDModel, JSONArray nodes, TSituation nodeContainer, JSONObject brokerNode, boolean debug, boolean doOverwrite) {
//		super("Select Thing for the ContextNode" + nameOfNode);
//
//		setLayout(new FlowLayout(FlowLayout.LEFT, 100, 100));
//		ArrayList<String> tempList = new ArrayList<String>();
//		for (String str: thingWithIp.keySet() ){
//			tempList.add(str);
//		}
//		
//		String[] thingTitles = new String[tempList.size()];
//		thingTitles = tempList.toArray(thingTitles);
//
//		final JComboBox<String> thingList = new JComboBox<String>(thingTitles);
//
//		thingList.setForeground(Color.BLUE);
//		thingList.setFont(new Font("Arial", Font.BOLD, 14));
//		thingList.setMaximumRowCount(10);
//		
//		// make the combo box editable so we can add new item when needed
//		thingList.setEditable(true);
//
//		// add an event listener for the combo box
//		thingList.addActionListener(new ActionListener() {
//
//			@Override
//			public void actionPerformed(ActionEvent event) {
//				JComboBox<String> combo = (JComboBox<String>) event.getSource();
//				String selectedItem = (String) combo.getSelectedItem();
//
//				DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) combo
//						.getModel();
//
//				int selectedIndex = model.getIndexOf(selectedItem);
//				if (selectedIndex < 0) {
//					// if the selected book does not exist before, 
//					// add it into this combo box
//					model.addElement(selectedItem);
//				}
//
//			}
//		});
//
//		buttonSelect.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent event) {
//				String selectedItem = (String) thingList.getSelectedItem();
//				selectedThing = selectedItem;
//				Mapper.thingsList.add(selectedThing);
//				ThingSelector.this.dispose();
//				if (Mapper.thingsList.size() == thingWithIp.size()){
//				Mapper.startMapping(nodeREDModel, nodes, nodeContainer, brokerNode, debug, doOverwrite);}
////				JOptionPane.showMessageDialog(ThingSelector.this,
////						"You selected the thing: " + selectedItem);
//				
//			}
//		});
//
//		add(thingList);
//		add(buttonSelect);
//
//		pack();
//		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		setLocationRelativeTo(null);
//	}
//
//	public static void selectThing(String nameOfNode, HashMap<String,String> thingWithIp, JSONObject nodeREDModel, JSONArray nodes, TSituation nodeContainer, JSONObject brokerNode, boolean debug, boolean doOverwrite) {
//		try {
//			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//
//		SwingUtilities.invokeLater(new Runnable() {
//
//			@Override
//			public void run() {
//				new ThingSelector(nameOfNode, thingWithIp, nodeREDModel, nodes,  nodeContainer,  brokerNode,  debug,  doOverwrite).setVisible(true);
//			}
//		});
//	}
//}