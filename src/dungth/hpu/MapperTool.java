package dungth.hpu;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jdom.JDOMException;

public class MapperTool
{
	
	public MapperTool()
	{
		// Not much happens here
	}
	
	
	public static void main(String[] args) throws ParseException, JDOMException, IOException 
	{
		
		CommandLine line;
		line = parseCommandLine(args);
		if (line == null)
		{
			return;
		}

		String sourceMetadata = line.getOptionValue("s");
		String destDir = line.getOptionValue("d");
		String configFile = line.getOptionValue("c");
		String licenceFile = line.getOptionValue("l");
		
		
		File source = new File(sourceMetadata);
		
		File dest = new File(destDir);
		File config = null;
		
		if (configFile != null)
		{
			config = new File(configFile);
		}
		
		File licence = new File(licenceFile);
		
		int separator = sourceMetadata.lastIndexOf(".");
		if (separator == -1)
		{
			System.err.println("Cannot determine source file type from extension - exiting.");
			return;
		}

		String extension = sourceMetadata.substring(separator + 1);

		String prefix = extension.substring(0, 1).toUpperCase() + extension.substring(1).toLowerCase();

		Mapper mapper;
		
		try
		{
			Class c = Class.forName("dungth.hpu." + prefix + "Mapper");
			mapper = (Mapper) c.newInstance();
		}
		catch (ClassNotFoundException e)
		{
			System.out.println("Could not load metadata mapping class for '" + extension + "' files. Exiting.");
			return;
		}
		catch (InstantiationException e)
		{
			System.out.println("Could not load metadata mapping class for '" + extension + "' files. Exiting.");
			return;
		}
		catch (IllegalAccessException e)
		{
			System.out.println("Could not load metadata mapping class for '" + extension + "' files. Exiting.");
			return;
		}
		
		mapper.setMappingConfig(config);
		mapper.setLicence(licence);
		mapper.setTest(line.hasOption('t'));
		mapper.setVerbose(line.hasOption('v'));
		
		mapper.transform(source, dest);

	}
	
	public static CommandLine parseCommandLine(String[] args)
	throws ParseException 
	{
		Options options;
		CommandLineParser parser = new GnuParser();
		CommandLine line = null;
		options = new Options();
		Option source = OptionBuilder.isRequired().
		withArgName("sourcemetadata").
		withLongOpt("source").
		hasArg().
		withDescription("Source metadata file to read").
		create("s");

		Option config = OptionBuilder.
		withArgName("config").
		withLongOpt("config").
		hasArg().
		withDescription("Config file to use in transformation (if needed)").
		create("c");

		Option destination = OptionBuilder.isRequired().
		withArgName("destinationdir").
		withLongOpt("dest").
		hasArg().
		withDescription("Destination directory for output").
		create("d");

		Option licence = OptionBuilder.isRequired().
		withArgName("licence").
		withLongOpt("licence").
		hasArg().
		withDescription("Licence file to include in batch").
		create("l");

		
		options.addOption(source);
		options.addOption(config);
		options.addOption(destination);
		options.addOption(licence);

		options.addOption("h", "help", false, "Show help text");
		options.addOption("t", "test", false, "Test mode (do not create files)");
//		options.addOption("v", "verbose", false, "Verbose mode");

		try
		{
			line = parser.parse(options, args);
			if (line.hasOption('h'))
			{	
				printHelp(options);
				line = null;
			}

		} catch (MissingOptionException moe)
		{
			printHelp(options);
			line = null;
		}


		return line;
	}

	public static void printHelp(Options options)
	{
		String footer = "This tool expects any referenced bitstream files to be in the same directory as " + 
			"the source metadata file, or defined as a path relative to its location.";
		HelpFormatter myhelp = new HelpFormatter();
		myhelp.printHelp("MapperTool", "", options, footer, true);		
	}

}
