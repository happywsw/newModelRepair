package com.modelrepair.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Transition;
import org.jbpt.petri.io.PNMLSerializer;
import org.jbpt.petri.unfolding.ProperCompletePrefixUnfolding;
import org.processmining.framework.log.LogFile;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogReaderFactory;
import org.processmining.framework.log.filter.DefaultLogFilter;

/**
 * @author wyj
 * */

public class FileUtil {
	FileNameSelector filenameFilter;
	HashMap<String, String> originalName = new HashMap<String, String>();
	ArrayList<String> silentTransitionNames = new ArrayList<String>();
	
	//input: import an event log 
	public LogReader importLog(String filePath) throws Exception {
		File file = new File(filePath);
		if (!file.exists()) 
			return null;
		
		LogFile logFile = LogFile.getInstance(file.getAbsolutePath());
		LogReader logReader = LogReaderFactory.createInstance(new DefaultLogFilter(DefaultLogFilter.INCLUDE), logFile);
		
		return logReader;
	}
	
	//input: import a petri net
	public NetSystem importModel(String filePath) {
		PNMLSerializer pnmlSerializer = new PNMLSerializer();
		
		if(!filePath.contains(".pnml")){
			System.out.print("The file is not petri net...");
			return null;
		}
		
		NetSystem net = pnmlSerializer.parse(filePath);
		
        //prepare initial marking
		net.getSourcePlaces().stream().forEach(p -> net.getMarking().put(p, 1));
		
		Set<Transition> transitions = net.getTransitions();
		for(Transition t: transitions){
			//System.out.println(t.getLabel());
			if(t.getLabel().contains("inv_")){
				silentTransitionNames.add(t.getId());
			}
				originalName.put(t.getId(), t.getLabel());
			
		}
		
        ProperCompletePrefixUnfolding cpuP = new ProperCompletePrefixUnfolding(net);
        NetSystem netPCPU = PetriNetConversion.convertCPU2NS(cpuP);
		
        return netPCPU;
	}
	
	
	public FileNameSelector getFileter(String fileter){
		filenameFilter = new FileNameSelector(fileter);
		return filenameFilter;
	}
	
	
	class FileNameSelector  implements FilenameFilter
	{
	    String extension = ".";

	    public FileNameSelector(String fileExtensionNoDot)
	    {
	        extension += fileExtensionNoDot;
	    }

	    @Override
	    public boolean accept(File dir, String name)
	    {
	        return name.endsWith(extension);
	    }
	}

	public ArrayList<String> getSilentName(){
		return silentTransitionNames;
	}
	
	public HashMap<String, String> getOriginalName() {
		// TODO Auto-generated method stub
		return originalName;
	}

}

