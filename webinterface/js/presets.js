/**
 * 
 */

var availablePresets = {};

var selectedPreset = null;

var statusShown = false;

$(document).ready(

function()
{
	$("select#select-preset").change(presetSelected);
	loadFormatPresets();
	loadFormats();
	
	$("#add").click(addFormatClick);
	$("#remove").click(removeFormatClick);
	$("#save_preset").click(savePreset);
}		
);

/**
 * Adds new format to selected formats
 *
 */
function addFormatClick()
{
	var selected = $("select#allformatlist").val();
	
	availableFormats = jQuery.grep(availableFormats, function(value)
	{
		if (jQuery.inArray(value.id + '', selected))
		{
			return true;
		} 
		else
		{
			selectedFormats.push(value);
			return false;
		}
	});
	
	renderFormatTables();
}

/**
 * Removes a format from selected format box and puts it back into unselected
 *
 */
function removeFormatClick()
{
	var selected = $("select#selectedformatlist").val();
	
	selectedFormats = jQuery.grep(selectedFormats, function(value)
	{
		if (jQuery.inArray(value.id + '', selected))
		{
			return true;
		} 
		else
		{
			availableFormats.push(value);
			return false;
		}
	});
	
	renderFormatTables();
}

function presetSelected()
{	
	availableFormats = availableFormats.concat(selectedFormats);
	selectedFormats = [];
	
	selectedPreset = availablePresets[$("select#select-preset").val()];
	
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

function savePreset()
{
	$("#response").removeClass("success-response");
	$("#response").removeClass("error-response");
	$("#response").html("");
	
	var formatIdList = $.map(selectedFormats, function(format) { return format.id })
	console.info(formatIdList);
	
	var data = { "presetId" : selectedPreset[0], "presetName" : $("input#preset_name").val(), "formats" : formatIdList };
	$.get("/api/savepreset", data, presetSavedCB, "json");
}

function presetSavedCB(response)
{
	if (response.status == 'OK')
	{
		$("#response").addClass("success-response");
	}
	else
	{
		$("#response").addClass("error-response");
	}
	
	$("#response").html(response.message);

	if (!statusShown) 
	{
		$("#response").slideDown("slow");
	}
	else
	{
		$("#response").fadeTo("normal", 1, null);
	}
	
	statusShown = true;
	
	loadFormatPresets();
}