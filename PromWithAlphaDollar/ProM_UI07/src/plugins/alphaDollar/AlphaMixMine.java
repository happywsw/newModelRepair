package alphaDollar;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.processmining.exporting.petrinet.PnmlExport;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.heuristics.HeuristicsNet;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.mining.petrinetmining.AlphaProcessMiner;
import org.processmining.mining.petrinetmining.PetriNetResult;




public class AlphaMixMine {

	public PetriNet mine(LogReader logReader) {
		if (logReader != null) {
			// Mine the log for a Petri net.
			AlphaMMiner miningPlugin = new AlphaMMiner();
			PetriNetResult result = (PetriNetResult) miningPlugin.mine(logReader);
			return result.getPetriNet();
		} else {
			System.err.println("No log reader could be constructed.");
			return null;
		}		
	}

	public String getName() {		
		return "Alpha Mix Miner";
	}

	public String getDesription() {
		return "New Under Test Miner";
	}
	
	public static void exportPetriNet(String filePath, PetriNet petriNet) {
		try {
			OutputStream out = new FileOutputStream(new File(filePath));
			if (out == null) {
				System.out.println("file Path is error.....");
			}
			PnmlExport export = new PnmlExport();
			ProvidedObject object = new ProvidedObject("temp", petriNet);
			//BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
			export.export(object, out);
			//PnmlWriter.write(false, true, petriNet, bw);
			//bw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
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
	
	public static void main(String[] args) throws Exception{

		String modelPath = "models/test_invisible_3.pnml";
		String logPath = "logs/invisible_test3.mxml";
				
		FileUtil util = new FileUtil();
		//NetSystem model = util.importModel(modelPath);
		LogReader log = util.readLog(logPath); 
				
		AlphaMixMine repair = new AlphaMixMine();
		PetriNet result = repair.mine(log);
	
		exportPetriNet(modelPath, result);
	}
}
