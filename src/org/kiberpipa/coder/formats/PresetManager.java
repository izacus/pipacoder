package org.kiberpipa.coder.formats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.kiberpipa.coder.Database;

public class PresetManager
{
	// This is a singleton class
	private static PresetManager instance = null;
	
	/**
	 * 
	 * @return singleton instance of the preset manager
	 */
	public static PresetManager getInstance()
	{
		if (PresetManager.instance == null)
		{
			PresetManager manager = new PresetManager();
			PresetManager.instance = manager;
		}
		
		return PresetManager.instance;
	}
	
	private HashMap<Integer, FormatPreset> presets = null;
	
	public PresetManager()
	{
		presets = new HashMap<Integer, FormatPreset>();
		
		// Get presets from database
		List<FormatPreset> dbPresets = Database.getPresets(FormatManager.getInstance());
		
		for (FormatPreset preset : dbPresets)
		{
			presets.put(preset.getId(), preset);
		}
	}
	
	
	public void putPreset(Integer id, String name, ArrayList<Integer> formatIds) throws Exception
	{
		FormatPreset preset = new FormatPreset(id, name);
		
		// Set format instances to preset
		for (Integer fmtId : formatIds)
		{
			OutputFormat format = FormatManager.getInstance().getFormatWithId(fmtId);
			
			if (format == null)
				continue;
			
			preset.addFormat(format);
		}
		
		// Update database entry
		Database.putPreset(preset);
		
		// Update map entry
		presets.put(preset.getId(), preset);
	}
	
	public List<FormatPreset> getPresets()
	{
		return new ArrayList<FormatPreset>(presets.values());
	}
	
}
