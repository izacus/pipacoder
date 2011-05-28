/**
 * 
 * @author Jernej Virag
 */
"use strict";

var jobTableHeader = '<tr><th>File name</th><th>Format</th><th>Status</th><th>Progress</th><th>&nbsp;</th></tr>';
var availableFormats = [];
var selectedFormats = [];
var availablePresets = {};

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

/**
 * Callback from jobs added 
 */
function jobsAddedCB(response)
{
	loadJobTable();
}

/**
 * Calls API to enqueue selected file for transcoding
 */
function addJobset()
{
	var formatIDs = [];
	
	for (var i = 0; i < selectedFormats.length; i++)
	{
		formatIDs.push(selectedFormats[i].id);
	}
	
	var request = { filename : $("select#select-filename").val(), formats : formatIDs};
	
	$.post("/api/addjobs", request, jobsAddedCB, "json");
}

/**
 * Load page values after document is ready
 */
$(document).ready(
function()
{
	$(document).ajaxError(function(event, request, settings, error) { console.info(error) });
	
	// Attach button functions
	$("#add").click(addFormatClick);
	$("#remove").click(removeFormatClick);
	$("#encode-button").click(addJobset);
	
	// Modal window hide
	$("body").click(function ()
					{
						$.modal.close();
					});
	
	$("select#select-preset").change(presetSelected);
	
	loadInputFiles();
	loadFormatPresets();
	loadFormats();
	loadJobTable();
	
	setInterval(loadJobTable, 1500);
}
);

/**
 * Load input file list for listbox
 */
function loadInputFiles()
{
	// Output "loading" text before request
	$("select#select-filename").html("<option>Loading...</option>");
	$.get("/api/inputfiles", null, loadInputFilesCB, "json");
}

/**
 * Populate listbox with receives input file list
 * @param {Object} response
 */
function loadInputFilesCB(response)
{
	var fileListHTML = '';
	
	// Add "All files" option
	fileListHTML += '<option value="ALLFILES"> -- All input files -- </option>';
	
	for (var i = 0; i < response.length; i++)
	{
		fileListHTML += '<option value="' + response[i] + '">' + response[i] + '</option>';
	}
	
	$("select#select-filename").html(fileListHTML);
};

function presetSelected()
{
	availableFormats = availableFormats.concat(selectedFormats);
	selectedFormats = [];
	
	var selectedPreset = availablePresets[$("select#select-preset").val()];
	
	if (typeof(selectedPreset) == 'undefined')
	{
		renderFormatTables();
		return;
	}
	
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