package com.modelrepair.relation.alphaDollar;

import java.util.HashMap;

import org.processmining.framework.log.LogReader;
import com.modelrepair.util.FileUtil;




public class AlphaMixMine {
	public HashMap<String, HashMap<String, Integer>> getRelationMatrix(LogReader logReader){
		HashMap<String, HashMap<String, Integer>> relationMatrix = new HashMap<String, HashMap<String,Integer>>();
		AlphaMMiner alphaMMiner = new AlphaMMiner();
		
		relationMatrix = alphaMMiner.getLogRelations(logReader);
		return relationMatrix;
	}
	
	
	
	
	public static void main(String[] args) throws Exception{

		String modelPath = "models/test_invisible_3.pnml";
		String logPath = "logs/invisible_test3.mxml";
				
		FileUtil util = new FileUtil();
		//NetSystem model = util.importModel(modelPath);
		LogReader log = util.importLog(logPath); 
				
		AlphaMixMine repair = new AlphaMixMine();
		//PetriNet result = repair.mine(log);
	
		//exportPetriNet(modelPath, result);
	}
}
