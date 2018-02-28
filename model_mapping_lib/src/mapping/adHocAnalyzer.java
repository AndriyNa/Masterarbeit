package mapping;

import java.util.ArrayList;

import situationtemplate.model.TSituation;
import situationtemplate.model.TSituationTemplate;

public class adHocAnalyzer {

	static TSituation analyzedSituation;
	static ArrayList<String> selectedThings;
	static ArrayList<String> ipAdresses;
	
	
	public adHocAnalyzer(TSituation analyzedSituation, ArrayList<String> selectedThings,
			ArrayList<String> ipAdresses) {
		super();
		this.analyzedSituation = analyzedSituation;
		this.selectedThings = selectedThings;
		this.ipAdresses = ipAdresses;
	}


	public static ArrayList findGroups() {
		ArrayList<ArrayList<String>> groups = new ArrayList<>();
		ArrayList<String> innerGroup = new ArrayList<>();
		System.out.println("Analyzer output: ");
		for(int i = 0; i < analyzedSituation.getContextNode().size()-1; i++){
			
			if (analyzedSituation.getContextNode().get(i).getInputType().equals("sensor")) {
//				System.out.println(analyzedSituation.getContextNode().get(i).getId() + " " + selectedThings.get(counter) + " " + ipAdresses.get(counter));
				for (int j = i+1; j < analyzedSituation.getContextNode().size(); j++){
					innerGroup.add(selectedThings.get(i));
					if( (ipAdresses.get(i).equals(ipAdresses.get(j))) && (i != j) )
					{
						innerGroup.add(selectedThings.get(j));
						System.out.println("Elements with same IPs is : "+ selectedThings.get(j) + " and " + selectedThings.get(i));
					}
				}			
			}
			groups.add(innerGroup);
		}
		
		return groups;
	}
	
	
	
}