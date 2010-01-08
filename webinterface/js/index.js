/**
 * 
 * @author Jernej Virag
 */
"use strict";

var jobTableHeader = '<tr><th>File name</th><th>Format</th><th>Status</th><th>Progress</th><th>&nbsp;</th></tr>';
var availableFormats = [];
var selectedFormats = [];

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
	// Attach button functions
	$("#add").click(addFormatClick);
	$("#remove").click(removeFormatClick);
	$("#encode-button").click(addJobset);
	
	// Modal window hide
	$("body").click(function ()
					{
						$.modal.close();
					});
	
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

/**
 * TODO: encoding format presets
 */
function loadFormatPresets()
{
	// TODO: presets
	loadFormatPresetsCB(null);
}

function loadFormatPresetsCB(response)
{
	// TODO: presets
	$("select#select-preset").html("<option>TODO</option>");
}

/**
 * Requests available formats from server
 * @return
 */
function loadFormats()
{
	$("select#allformatlist").html("<option>Loading...</option>");
	$.get("/api/formats", null, loadFormatsCB, "json");
}

function loadFormatsCB(response)
{
	selectedFormats = [];
	availableFormats = response;
	
	renderFormatTables();
}

/**
 * Renders tables with available/selected formats with values from selectedFormats/availableFormats arrays
 * @return
 */
function renderFormatTables()
{
	// Create available formats
	var formatsHTML = '';
	var i;
	
	if (availableFormats.length === 0)
	{
		formatsHTML += '<option> -- NO FORMATS -- </option>';
	}	
	else
	{	
		for (i = 0; i < availableFormats.length; i++)
		{
			formatsHTML += '<option value="' + availableFormats[i].id + '">' + availableFormats[i].name + '</option>';
		}
	}
	
	$("select#allformatlist").html(formatsHTML);
	
	// Create selected formats
	formatsHTML = '';
	
	if (selectedFormats.length === 0)
	{
		formatsHTML += '<option> -- NOTHING SELECTED -- </option>';
	}
	else
	{
		for (i = 0; i < selectedFormats.length; i++)
		{
			formatsHTML += '<option value="' + selectedFormats[i].id + '">' + selectedFormats[i].name + '</option>';
		}
	}
	
	$("select#selectedformatlist").html(formatsHTML);
}

/**
 * Requests list of jobs from server
 */
function loadJobTable()
{	
	// Send load request
	$.get("/api/jobs", null, loadJobTableCB, "json");
}

/**
 * Renders list of jobs returned from the server
 * This is a callback from loadJobTable()
 */
function loadJobTableCB(response)
{
	// Clear the table
	$("#jobs > tbody").empty();
	$("#jobs > tbody").append(jobTableHeader);
	
	if (response.length === 0)
	{
		$("#jobs > tbody").append('<tr><td colspan="5" align="center"> -- NO JOBS -- </td></tr>');
	}
	else
	{
		var tableHTML = '';
		
		for (var i = 0; i < response.length; i++)
		{
			var responseColor;
			
			// Color job status
			switch(response[i].status)
			{
				case 'RUNNING':
					responseColor = 'black';
					break;
				case 'FAILED':
					responseColor = 'red';
					break;
				case 'DONE':
					responseColor = 'green';
					break;
			}
			
			tableHTML += '<tr><td>' + response[i].filename + '</td><td>' + response[i].format + '</td><td style="color:' + responseColor + '">' + response[i].status + '</td>';
			
			if (response[i].progress !== null)
			{
				tableHTML += "<td>" + response[i].progress + "</td>";
			}
			else
			{
				// If job failed, add "WHY?" link instead of progress
				if (response[i].status === 'FAILED')
				{
					tableHTML += '<td><a href="javascript:showFailReason(' + response[i].id + ')">Why?</a></td>';
				}
				else
				{
					tableHTML += "<td>&nbsp;</td>";	
				}
			}
			
			
			if (response[i].status === 'RUNNING')
			{
				tableHTML += '<td><a href="javascript:stopJob(' + response[i].id + ')">Stop</a></td>';
			}
			else
			{
				tableHTML += '<td>&nbsp;</td>';
			}
			
			/* TODO
			if (response[i].eta != null)
			{
				tableHTML += "<td>" + response[i].eta + "</td>";
			}
			else
			{
				tableHTML += "<td>&nbsp;</td>";
			} */
			
			tableHTML += "</tr>";
		}
		
		$("#jobs > tbody").append(tableHTML);
	}
}

/**
 * Requests reason the job failed from server
 * @param jobid ID of the failed job
 */
function showFailReason(jobid)
{
	var data = { id : jobid};
	
	$.get("/api/getfailreason", data, showFailReasonCB, "json");
}

/**
 * Displays a messagebox with a reason the job failed
 * This is a callback from showFailReason()
 */
function showFailReasonCB(response)
{
	$.modal('<div class="messagebox">' + response.message + '</div>');
}

/**
 * Stops a job in progress
 * @param jobid ID of the job to stop
 */
function stopJob(jobid)
{
	var data = { id : jobid};
	
	// Reload job table as callback
	$.get("/api/stopjob", data, loadJobTable, "json");
}
