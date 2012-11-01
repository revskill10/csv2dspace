package dungth.hpu;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import au.com.bytecode.opencsv.CSVReader;

public class CsvMapper extends MapperBase implements Mapper
{

	@Override
	public boolean requiresConfig()
	{
		return false;
	}

	@Override
	public void setMappingConfig(File config) throws FileNotFoundException
	{
		// CSV mapping takes no configuration
	}

	@Override
	public void transform(File source, File destDir)
	{
		System.out.println("CSV transform: " + source.getAbsolutePath() + " -> " + destDir.getAbsolutePath());
		System.out.println("Enforcing lower case metadata field names.");
		ArrayList<String> metadataFields = new ArrayList<String>();
	    try
		{
			CSVReader reader = new CSVReader(new FileReader(source));
		    String [] nextLine;

		    // First three rows are metadata field definitions

		    // Row 1 is schema
		    nextLine = reader.readNext();
		    if (nextLine == null)
		    {
		    	System.err.println("Source metadata file is empty.");
		    	return;
		    }
		    
		    for (String cell : nextLine)
		    {
		    	// First empty cell signifies last metadata column - subsequent columns contain bitstream names.
		    	if (cell.trim().equals(""))
		    	{
		    		break;
		    	}
		    	
		    	metadataFields.add(cell.toLowerCase().trim());
		    }
		    
		    // Row 2 is element. Not to have an element defined is an error.
		    nextLine = reader.readNext();
		    if (nextLine == null)
		    {
		    	System.err.println("Source metadata file contains no actual metadata.");
		    	return;
		    }
		    
		    for (int i = 0; i < metadataFields.size(); i ++)
		    {
		    	if (nextLine[i].trim().equals(""))
		    	{
		    		System.err.println("Column " + i + " has no element specifier. Aborting transform.");
		    		return;
		    	}
		    	
		    	metadataFields.set(i, metadataFields.get(i) + "." + nextLine[i].toLowerCase().trim());
		    }
		    
		    // Row 3 is qualifier. Qualifiers may be empty.
		    nextLine = reader.readNext();
		    if (nextLine == null)
		    {
		    	System.err.println("Source metadata file contains no actual metadata.");
		    	return;
		    }
		    
		    for (int i = 0; i < metadataFields.size(); i ++)
		    {
		    	if (nextLine[i].trim().equals("") == false)
		    	{
			    	metadataFields.set(i, metadataFields.get(i) + "." + nextLine[i].toLowerCase().trim());
		    	}		    	
		    }
		    
		    System.out.println("Found " + metadataFields.size() + " metadata fields in CSV file:");
		    for (String fieldName : metadataFields)
		    {
		    	System.out.println(fieldName);
		    }
		    
		    System.out.println("Processing metadata and bitstream entries.");
		    
		    // Rest of the file is metadata and bitstreams
		    ArrayList<MetadataValue> metadata;
		    ArrayList<String> bitstreams;
		    int itemNumber = 1;
		    File destPackageDir;
		    while ((nextLine = reader.readNext()) != null) 
		    {
		    	metadata = new ArrayList<MetadataValue>();
		    	bitstreams = new ArrayList<String>();

		    	//First part of row is metadata
			    for (int i = 0; i < metadataFields.size(); i ++)
			    {
			    	if (nextLine[i].trim().equals("") == false)
			    	{
			    		metadata.add(new MetadataValue(metadataFields.get(i), nextLine[i]));
			    	}		    	
			    }
			    
			    // Remainder of row is bitstream filenames
			    for (int i = metadataFields.size(); i < nextLine.length; i ++)
			    {
			    	if (nextLine[i].trim().equals("") == false)
			    	{
			    		bitstreams.add(nextLine[i]);
			    	}		    				    	
			    }
			    destPackageDir = new File(destDir, "item" + itemNumber);
			    
			    createPackage(source.getParentFile(), destPackageDir, metadata, bitstreams);
			    itemNumber ++;
			}
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
