/**
 * 
 */

var availablePresets = {};

$(document).ready(

function()
{
	$("select#select-preset").change(presetSelected);
	loadFormatPresets();
	loadFormats();
}		
);

function presetSelected()
{
	availableFormats = availableFormats.concat(selectedFormats);
	selectedFormats = [];
	
	var selectedPreset = availablePresets[$("select#select-preset").val()];
	
	if (typeof(selectedPreset) == 'undefined')
	{
		$("input#preset_name").prop('disabled', false);
		$("input#preset_name").val("");
		renderFormatTables();
		return;
	}
	
	$("input#preset_name").prop('disabled', true);
	$("input#preset_name").val($("select#select-preset option:selected").text());
	
	availableFormats = jQuery.grep(availableFormats, function(value)
	{
		if (jQuery.inArray(value.id, selectedPreset) > -1)
		{
			selectedFormats.push(value);
			return false;
		} 
		else
		{
			return true;
		}
	});
	
	renderFormatTables();
}