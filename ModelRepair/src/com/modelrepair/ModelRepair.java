package com.modelrepair;




import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.jbpt.petri.NetSystem;
import org.processmining.framework.log.LogReader;

import com.modelrepair.relation.BPPlusSimilarity;
import com.modelrepair.relation.alphaDollar.AlphaMixMine;
import com.modelrepair.util.FileUtil;



public class ModelRepair {
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
	
	public void repair(LogReader log, NetSystem model){
		//1.1 get the model relation matrix based on BP+
		getModelRelation(model);
		
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
		}
		
		//2. specific repair progress
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
		String modelPath = "models/L3.pnml";
		String logPath = "logs/L3.mxml";
		
		FileUtil util = new FileUtil();
		NetSystem model = util.importModel(modelPath);
		LogReader log = util.importLog(logPath);
		
		ModelRepair repair = new ModelRepair(util.getOriginalName(), util.getSilentName());
		repair.repair(log, model);
	}
}
