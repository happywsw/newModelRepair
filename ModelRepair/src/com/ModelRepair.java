package com;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Node;
import org.jbpt.petri.Transition;

import com.bpplus.BPPlusSimilarity;
import com.util.FileUtil;


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
		modelRelationMatrix = bpp.getRelationMatrix(net);
        
		bpp.printMatrix(mappingNames, modelRelationMatrix);
	}
	
	public void getLogRelation(){
		
	}
	
	public void repair(NetSystem model){
		//1.1 get the model relation matrix based on BP+
		getModelRelation(model);
		
		//1.2 get the log realtion matrix based on alpha#
		getLogRelation();
		
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
	
	public float findTheDifferent(HashMap<String, HashMap<String, Integer>> modelRelationMatrix, 
			HashMap<String, HashMap<String, Integer>> logRelationMatrix){
		float diffValue = 1.0f;
		
		return diffValue;
	}
	
	public static void main(String[] args){
		String modelPath = "models/InvisibleTask_O_APBPM.pnml";
		
		FileUtil util = new FileUtil();
		NetSystem model = util.importModel(modelPath);
		
		ModelRepair repair = new ModelRepair(util.getOriginalName(), util.getSilentName());
		repair.repair(model);
	}
}
