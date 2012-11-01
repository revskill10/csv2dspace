package dungth.hpu;

import java.io.File;
import java.io.FileNotFoundException;

public interface Mapper
{
	public void setLicence(File licence);
	public void setVerbose(boolean verbose);
	public void setTest(boolean test);
	public boolean requiresConfig();
	public void setMappingConfig(File config) throws FileNotFoundException;
	public void transform(File source, File destDir);
	
}
