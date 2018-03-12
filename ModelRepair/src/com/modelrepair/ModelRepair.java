package com.modelrepair;




import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Transition;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogEvents;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.petrinet.PetriNet;
import com.modelrepair.relation.BPPlusSimilarity;
import com.modelrepair.relation.alphaDollar.AlphaMixMine;
import com.modelrepair.repair.HighLevelOp;
import com.modelrepair.util.FileUtil;
import com.modelrepair.util.PetriNetConversion;



public class ModelRepair {
	ArrayList<String> common = new ArrayList<String>();
	HashMap<String, HashMap<String, Integer>> modelRelationMatrix;
	HashMap<String, HashMap<String, Integer>> logReationMatrix;
	HashMap<String, String> mappingNames;
	ArrayList<String> silentNames;
	
	public ModelRepair(){}
	public ModelRepair(HashMap<String, String> names, ArrayList<String> silenNames){
		mappingNames = names;
		this.silentNames = silenNames; 
	}
	
	public void getModelRelation(NetSystem net){	
		BPPlusSimilarity bpp = new BPPlusSimilarity(silentNames);
		modelRelationMatrix = bpp.getRelationMatrix(mappingNames, net);
		printMatrix(modelRelationMatrix);
	}
	
	public void getLogRelation(LogReader log){
		AlphaMixMine amm = new AlphaMixMine();
		logReationMatrix = amm.getRelationMatrix(log);
		
		printMatrix(logReationMatrix);
	}
	
	public void repair(LogReader log, NetSystem model) throws IOException{
		List<String> deleteOp = new ArrayList<String>();
		List<String> insertOp = new ArrayList<String>();
		List<String> moveOp = new ArrayList<String>();
		
		this.common = getCommon(log, model);
		
		//1.delete the task in the model not in the log
		deleteOp = delete(log, model);
		
		//2.move the model transition arrcoring to the log
	//	moveOp = move(log, model);
		
		//3.insert the transition not in the model but in the log
		//insertOp = insert(log, model);
		//1.1 get the model relation matrix based on BP+
	/*	getModelRelation(model);
		
		//1.2 get the log realtion matrix based on alpha#
		getLogRelation(log);
		
		//1.3 find the difference between modelMatrix and logMatrix
		float differentValue = findTheDifferent(modelRelationMatrix, logReationMatrix);
		
		if(differentValue == 1){
			System.out.println("Same, do not need repair....");
			return;
		}else if(differentValue == 0){
			System.out.println("Total different, please use process mining algorithm..");
			return;
		}*/
		
		//2. specific repair progress
	}
	
	//1.delete the task in model not in the log
	public  ArrayList<String> delete(LogReader log, NetSystem model) throws IOException
	{
		ArrayList<String> del = new ArrayList<String>();
		ArrayList dif = diff(log, model, false);
		
		if(dif == null)
			return del;
		
		for(Transition tt: (ArrayList<Transition>)dif){
			System.out.println(tt.getLabel());
		}
		//model = HighLevelOp.deleteOp(dif, model);
		model = HighLevelOp.deleteOp(dif, model);
		
		PetriNet petri = PetriNetConversion.convert(model);
		
		// cpu
		
//		ProvidedObject po2 = new ProvidedObject("petrinet", petri);
//		DotPngExport dpe2 = new DotPngExport();
//		OutputStream image2 = new FileOutputStream("test/test1.pnml");
//		dpe2.export(po2, image2);
//		
		System.out.println("export pnml");
		FileUtil.Export(petri, "test/test1.pnml");
		System.out.println("finish...");
		
		for(Transition st:(ArrayList<Transition>)dif){
			String t="";
			t=t+"delete(S,"+st+")";
			del.add(t);
		}
		//System.out.println(dif);
		return del;
	}
	
	//2.move the transition according to the log
	public ArrayList<String> move(LogReader log, NetSystem model){
		return null;
	}
	
	//3.insert the transition in the model
	public ArrayList<String> insert(LogReader log, NetSystem model){
		ArrayList<String> insert = new ArrayList<String>();
		ArrayList<String> dif = diff(log, model, true);
		if(dif == null)
			return insert;
		model = HighLevelOp.insertOp(dif, model);
		
		return insert;
	}
	private static ArrayList<String> getCommon(LogReader log, NetSystem model){
		ArrayList<String> com = new ArrayList<String>();
		 LogEvents events = log.getLogSummary().getLogEvents();
		 Set<Transition> transList = model.getTransitions();
		 
		 for(LogEvent e: events){
			 for(Transition tt:transList){
				 if(e.getModelElementName().equals(tt.getLabel())){
					 com.add(e.getModelElementName());
					 continue;
				 }
			 }
		 }
	    return com;
	}
	
	public ArrayList diff(LogReader log, NetSystem model, boolean flag){
		ArrayList diffActivities = new ArrayList();
		//flag == false, count the activity in the model not in the log
		if(!flag){
			 Set<Transition> transList = model.getTransitions();
			 for(Transition tt: transList){
				 
				// if(!common.contains(tt.getLabel()) && !tt.getLabel().contains(Relation.SLIENTTRANSITION))
				if(tt.getLabel().contains("Appeal to Judge"))
					 diffActivities.add(tt);
			 }
		}else{
			 LogEvents events = log.getLogSummary().getLogEvents();
			 for(LogEvent event : events){
				 if(!common.contains(event.getModelElementName()))
					 diffActivities.add(event);
			 	}
		}
		
		return diffActivities;
	}

	 public static void printMatrix(HashMap<String, HashMap<String, Integer>> map){
	    	Iterator iterator = map.entrySet().iterator();
	    	while(iterator.hasNext()){
	    		Map.Entry entry = (Map.Entry)iterator.next();
	    		String key = (String)entry.getKey();
	    		System.out.print(String.format("%7s", "["+ key+"]" + "\t"));
	    		
	    		HashMap<String, Integer> values = (HashMap<String, Integer>)entry.getValue();
	    		
	    		
	    		Iterator iterator2 = values.entrySet().iterator();
	    		while(iterator2.hasNext()){
	    			Map.Entry entry2 = (Map.Entry)iterator2.next();
	    			String key2 = (String)entry2.getKey();
	    			int value2 = (int)entry2.getValue();
	    			//System.out.println(value2);
	    			System.out.print(key2 + ":"+ value2 + "\t");
	    		}
	    		
	    		
	    		System.out.println();
	    		
	    	}
	    }
	public float findTheDifferent(HashMap<String, HashMap<String, Integer>> logRelationMatrix, 
			HashMap<String, HashMap<String, Integer>> modelRelationMatrix){
		int diffNum = 0;
		int totalNum = (int) Math.pow(logRelationMatrix.size(),2);
		
		Iterator iterator = logRelationMatrix.entrySet().iterator();
    	while(iterator.hasNext()){
    		Map.Entry entry = (Map.Entry)iterator.next();
    		String key = (String)entry.getKey();
    		
    		HashMap<String, Integer> values = (HashMap<String, Integer>)entry.getValue();
    		
    		
    		Iterator iterator2 = values.entrySet().iterator();
    		while(iterator2.hasNext()){
    			Map.Entry entry2 = (Map.Entry)iterator2.next();
    			String key2 = (String)entry2.getKey();
    			int value2 = (int)entry2.getValue();
    			//System.out.println(value2);
    			if(modelRelationMatrix.containsKey(key) && modelRelationMatrix.get(key).get(key2) != value2){
    				diffNum++;
    				System.out.println(key+":diff:"+key2);
    			}
    		}	
    	}
    	System.out.println("diff:"+diffNum);
    	return (float)diffNum/(float)totalNum;
	}
	
	public static void main(String[] args) throws Exception{
		String modelPath = "models/testAA.pnml";
		String logPath = "logs/invisible_test3.mxml";
		
		FileUtil util = new FileUtil();
		NetSystem model = util.importModel(modelPath);
		LogReader log = util.importLog(logPath);
		
		ModelRepair repair = new ModelRepair(util.getOriginalName(), util.getSilentName());
		repair.repair(log, model);
	}
}
