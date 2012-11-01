package dungth.hpu;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public abstract class MapperBase
{
	private File licence;
	private boolean isTest;
	private boolean isVerbose;
	
	public abstract void setMappingConfig(File config) throws FileNotFoundException;
	public abstract void transform(File source, File destDir);

	public abstract boolean requiresConfig();
	
	public void setLicence(File licence)
	{
		this.licence = licence;
	}
	
	public void createPackage(File destPackageDir, List<MetadataValue> metadata)
	{
		createPackage(null, destPackageDir, metadata, new ArrayList<String>());
	}

	public void createPackage(File sourceDir, File destPackageDir, List<MetadataValue> metadata, List<String> bitstreamNames)
	{
		if (isTest)
		{
			System.out.print("[TEST MODE] ");
		}
		System.out.println("Creating item package: " + destPackageDir.getAbsolutePath());

		Document itemXml = createDSpaceXml(metadata);

		if (!isTest)
		{
			if (destPackageDir.exists() == false)
			{
				destPackageDir.mkdirs();
			}
			writeDSpaceXml(destPackageDir, itemXml);

			/*if (sourceDir != null)
			{
				copyBitstreams(sourceDir, destPackageDir, bitstreamNames);
			}*/
			createContentsFile(destPackageDir, bitstreamNames);
		}		
	}
	
	public Document createDSpaceXml(List<MetadataValue> mappedData)
	{
		Document doc = new Document();
		Element root = new Element("dublin_core");
		
		doc.setRootElement(root);
		
		for (MetadataValue metadataValue : mappedData)
		{
			String key = metadataValue.getKey();
			String value = metadataValue.getValue();
			
			String[] metadataName = key.split("\\.");
			if (metadataName.length > 1)
			{
				Element dcValue = new Element("dcvalue");
				
				dcValue.setAttribute("element", metadataName[1]);
				
				if (metadataName.length > 2)
				{
					dcValue.setAttribute("qualifier", metadataName[2]);
				}
				
				dcValue.setText(value);
				
				root.addContent(dcValue);
			}
			else
			{
				System.err.println("Metadata key '" + key + "' is invalid. Skipping.");
			}
			
		}
		
		return doc;
	}

	public void writeDSpaceXml(File dir, Document doc)
	{
		File dc = new File(dir, "dublin_core.xml");
		XMLOutputter dcOutput = new XMLOutputter(Format.getPrettyFormat());
		FileWriter out = null;
		try
		{
			out = new FileWriter(dc);
			dcOutput.output(doc, out);
		}
		catch (IOException e)
		{
			System.err.println("Error creating DSpace metadata file '" + dc.getAbsolutePath() + "': " + e.getLocalizedMessage());
		}
		
	}

	public void createContentsFile(File destDir, List<String> bitstreamNames)
	{
		File contents = new File(destDir, "contents");
		PrintWriter writer = null;
		try
		{
			writer = new PrintWriter(contents);
			
			for (String bitstream : bitstreamNames)
			{
				if (bitstream.contains(".jpg"))
					writer.println("-r -s 1 -f " + bitstream + "\tbundle:THUMBNAIL2");
				else if (bitstream.contains("preview"))
					writer.println("-r -s 2 -f " + bitstream + "\tbundle:PREVIEW");
				else
					writer.println("-r -s 0 -f " + bitstream + "\tbundle:ORIGINAL");
			}
			
			if (licence != null)
			{
				writer.println(licence.getName() + "\tbundle:LICENSE");
			}
		}
		catch (IOException e)
		{
			System.err.println("Error creating contents file '" + contents.getAbsolutePath() + "': " + e.getLocalizedMessage());
		}
		finally
		{
			if (writer != null)
			{
				writer.close();
			}
		}
		
	}
	
	public void copyBitstreams(File sourceDir, File destDir, List<String> bitstreamNames)
	{
		if (sourceDir.exists() == false)
		{
			System.err.println("Source directory '" + sourceDir.getAbsolutePath() + "' not found.");
			return;
		}
		
		if (destDir.exists() == false)
		{
			destDir.mkdirs();
		}
		
		for (String bitstream : bitstreamNames)
		{
			try
			{
				fileCopy(new File(sourceDir, bitstream), new File(destDir, bitstream));
			}
			catch (IOException e)
			{
				System.err.println("Failed to copy '" + bitstream + "' from '" + sourceDir.getAbsolutePath() + 
						"' to '" + destDir.getAbsolutePath() + "': " + e.getLocalizedMessage());
			}
		}
		
		// Copy licence bitstream
		if (licence != null)
		{
			try
			{
				fileCopy(licence, new File(destDir, licence.getName()));
			}
			catch (IOException e)
			{
				System.err.println("Failed to copy licence file '" + licence.getAbsolutePath() + 
						"' to '" + destDir.getAbsolutePath() + "': " + e.getLocalizedMessage());
			}
		}
		
	}
	
	public void fileCopy( File in, File out )
	throws IOException
	{
		FileChannel inChannel = new FileInputStream( in ).getChannel();
		FileChannel outChannel = new FileOutputStream( out ).getChannel();
		try
		{
			inChannel.transferTo(0, inChannel.size(), outChannel);      // original -- apparently has trouble copying large files on Windows
/*
			// magic number for Windows, 64Mb - 32Kb)
			int maxCount = (64 * 1024 * 1024) - (32 * 1024);
			long size = inChannel.size();
			long position = 0;
			while ( position < size )
			{
				position += inChannel.transferTo( position, maxCount, outChannel );
			}
*/
		}
		finally
		{
			if ( inChannel != null )
			{
				inChannel.close();
			}
			if ( outChannel != null )
			{
				outChannel.close();
			}
		}
	}
	public boolean isTest()
	{
		return isTest;
	}
	public void setTest(boolean isTest)
	{
		this.isTest = isTest;
	}
	public boolean isVerbose()
	{
		return isVerbose;
	}
	public void setVerbose(boolean isVerbose)
	{
		this.isVerbose = isVerbose;
	}

}
