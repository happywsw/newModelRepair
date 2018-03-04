package alphaDollar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.security.KeyStore.Entry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import org.omg.CORBA.SystemException;
import org.processmining.framework.log.LogFile;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogReaderFactory;
import org.processmining.framework.log.filter.DefaultLogFilter;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.importing.pnml.PnmlImport;

/**
 * @author wyj
 * */

public class FileUtil {
	FileNameSelector filenameFilter;
	HashMap<String, String> originalName = new HashMap<String, String>();
	ArrayList<String> silentTransitionNames = new ArrayList<String>();
	
	//input: import an event log 
	public LogReader readLog(String filePath) throws Exception {
		File file = new File(filePath);
		if (!file.exists()) 
			return null;
		
		LogFile logFile = LogFile.getInstance(file.getAbsolutePath());
		LogReader logReader = LogReaderFactory.createInstance(new DefaultLogFilter(DefaultLogFilter.INCLUDE), logFile);
		
		return logReader;
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

