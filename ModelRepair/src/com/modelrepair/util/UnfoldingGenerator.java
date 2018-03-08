package com.modelrepair.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;


import org.jbpt.petri.NetSystem;
import org.jbpt.petri.unfolding.AbstractCondition;
import org.jbpt.petri.unfolding.ProperCompletePrefixUnfolding;
import org.processmining.exporting.DotPngExport;
import org.processmining.exporting.petrinet.PnmlExport;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.importing.pnml.PnmlImport;
import org.jbpt.petri.unfolding.AbstractEvent;

public class UnfoldingGenerator
{
	//export petri net 
	public static void Export(PetriNet petriNet, String fileNet) {
		if (petriNet != null) {
			// Export the Petri net as PNML.
			PnmlExport exportPlugin = new PnmlExport();
			Object[] objects = new Object[] {petriNet};
			ProvidedObject object = new ProvidedObject("temp", objects);
			FileOutputStream outputStream = null;
			try {
				if (fileNet != null) {
					outputStream = new FileOutputStream(fileNet);
				}
				// If no output file specified, write to System.out
				// However, some other thing smay get written to System.out as well :-(.
				exportPlugin.export(object, (outputStream != null ? outputStream : System.out));
				//System.exit(0);
			} catch (Exception e) {
				System.err.println("Unable to write to file: " + e.toString());
			}
		} else {
			System.err.println("No Petri net could be constructed.");
		}
	}

	
	public static void jbptTest() throws Exception
	{
		File folder = new File("models/others");
	
		File[] arModels = folder.listFiles(new FileUtil().getFileter("pnml"));
		for(File file: arModels)
		{
			//print the file name
			System.out.println("========" + file.getName() + "========");

			//initialize the counter for conditions and events
			AbstractEvent.count = 0;
			AbstractCondition.count = 0;

			//get the file path
			String filePrefix = file.getPath();
			filePrefix = filePrefix.substring(0, filePrefix.lastIndexOf('.'));
			String filePNG = filePrefix + ".png";
			String fileCPU = filePrefix + "-cpu.png";
			String fileNet = filePrefix + "-cpu.pnml";

			PnmlImport pnmlImport = new PnmlImport();
			PetriNet p1 = pnmlImport.read(new FileInputStream(file));

			// ori
			ProvidedObject po1 = new ProvidedObject("petrinet", p1);
			
			DotPngExport dpe1 = new DotPngExport();
			OutputStream image1 = new FileOutputStream(filePNG);
			
			dpe1.export(po1, image1);

			NetSystem ns = PetriNetConversion.convert(p1);
			ProperCompletePrefixUnfolding cpu = new ProperCompletePrefixUnfolding(ns);

			// cpu
			PetriNet p2 = PetriNetConversion.convert(cpu);
			
			ProvidedObject po2 = new ProvidedObject("petrinet", p2);
			DotPngExport dpe2 = new DotPngExport();
			OutputStream image2 = new FileOutputStream(fileCPU);
			dpe2.export(po2, image2);
//			
			Export(p2, fileNet);
//			// net
//			NetSystem nsCPU = PetriNetConversion.convertCPU2NS(cpu);
//			PetriNet pnCPU = PetriNetConversion.convertNS2PN(nsCPU);
//			ProvidedObject po3 = new ProvidedObject("petrinet", pnCPU);
//			DotPngExport dpe3 = new DotPngExport();
//			OutputStream image3 = new FileOutputStream(fileNet);
//			dpe3.export(po3, image3);
		}
			System.exit(0);
	}

	public static void main(String[] args) throws Exception {
			jbptTest2();
	}


	private static void jbptTest2() throws FileNotFoundException, Exception {
		// TODO Auto-generated method stub
		File folder = new File("models");
		
		File[] arModels = folder.listFiles(new FileUtil().getFileter("pnml"));
		for(File file: arModels)
		{
			//print the file name
			System.out.println("========" + file.getName() + "========");

			//initialize the counter for conditions and events
			AbstractEvent.count = 0;
			AbstractCondition.count = 0;

			//get the file path
			String filePrefix = file.getPath();
			filePrefix = filePrefix.substring(0, filePrefix.lastIndexOf('.'));
			String filePNG = filePrefix + ".png";
			String fileCPU = filePrefix + "-cpu.png";
			String fileNet = filePrefix + "-cpu.pnml";

			PnmlImport pnmlImport = new PnmlImport();
			PetriNet p1 = pnmlImport.read(new FileInputStream(file));

			// ori
			/*ProvidedObject po1 = new ProvidedObject("petrinet", p1);
			
			DotPngExport dpe1 = new DotPngExport();
			OutputStream image1 = new FileOutputStream(filePNG);
			
			dpe1.export(po1, image1);*/

			NetSystem ns = PetriNetConversion.convert(p1);
			ProperCompletePrefixUnfolding cpu = new ProperCompletePrefixUnfolding(ns);
			NetSystem netPCPU = PetriNetConversion.convertCPU2NS(cpu);
			// cpu
			PetriNet p2 = PetriNetConversion.convert(netPCPU);
			
			ProvidedObject po2 = new ProvidedObject("petrinet", p2);
			DotPngExport dpe2 = new DotPngExport();
			OutputStream image2 = new FileOutputStream(fileCPU);
			dpe2.export(po2, image2);
//			
			Export(p2, fileNet);
//			// net
//			NetSystem nsCPU = PetriNetConversion.convertCPU2NS(cpu);
//			PetriNet pnCPU = PetriNetConversion.convertNS2PN(nsCPU);
//			ProvidedObject po3 = new ProvidedObject("petrinet", pnCPU);
//			DotPngExport dpe3 = new DotPngExport();
//			OutputStream image3 = new FileOutputStream(fileNet);
//			dpe3.export(po3, image3);
		}
			System.exit(0);
	}
}
