package com.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.security.KeyStore.Entry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Node;
import org.jbpt.petri.Transition;
import org.jbpt.petri.io.PNMLSerializer;
import org.jbpt.petri.unfolding.AbstractCondition;
import org.jbpt.petri.unfolding.AbstractEvent;
import org.jbpt.petri.unfolding.ProperCompletePrefixUnfolding;
import org.omg.CORBA.SystemException;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.importing.pnml.PnmlImport;


public class FileUtil {
	FileNameSelector filenameFilter;
	HashMap<String, String> originalName = new HashMap<String, String>();
	ArrayList<String> silentTransitionNames = new ArrayList<String>();
	
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
			if(t.getLabel().contains("inv_"))
				silentTransitionNames.add(t.getId());
			
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

