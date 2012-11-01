package dungth.hpu;

public class MetadataValue
{
	private String key;
	private String value;
	
	public MetadataValue(String k, String v)
	{
		key = k;
		value = v;
	}

	public String getKey()
	{
		return key;
	}

	public void setKey(String key)
	{
		this.key = key;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}
}
