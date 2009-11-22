/**
 * @author Jernej
 */

jobTableHeader = '<tr><th>Job description</th><th>Status</th><th>Progress</th><th>ETA</th></tr>';

/**
 * Load page values after document is ready
 */
$(document).ready(
function()
{
	loadInputFiles();
	loadFormatPresets();
	loadFormats();
	
	loadJobTable();
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
	
	for (var i = 0; i < response.length; i++)
	{
		fileListHTML += '<option>' + response[i] + '</option>';
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

function loadFormats()
{
	$("select#allformatlist").html("<option>Loading...</option>");
	$.get("/api/formats", null, loadFormatsCB, "json");
}

function loadFormatsCB(response)
{
	var formatsHTML = '';
	
	if (response.length == 0)
	{
		formatsHTML += '<option> -- NO FORMATS -- </option>';
	}	
	else
	{	
		for (var i = 0; i < response.length; i++)
		{
			formatsHTML += '<option>' + response[i].name + '</option>';
		}	
	}
	
	$("select#allformatlist").html(formatsHTML);
}

function loadJobTable()
{
	// Show loading text for elements
	var tableHTML = jobTableHeader + '<tr><td colspan="4" align="center">Loading...</td></tr>';
	$("table#jobs").html(tableHTML);
	
	// Send load request
	$.get("/api/jobs", null, loadJobTableCB, "json");
}

function loadJobTableCB(response)
{
	var tableHTML = jobTableHeader;
	
	if (response.length == 0)
	{
		tableHTML += '<tr><td colspan="4" align="center"> -- NO JOBS -- </td></tr>';
	}
	else
	{
		for (var i = 0; i < response.length; i++)
		{
			tableHTML += '<tr><td>' + response[i].filename + '</td><td>' + response[i].status + '</td>';
			
			if (response[i].progress != null)
			{
				tableHTML += "<td>" + reponse[i].progress + "</td>";
			}
			else
			{
				tableHTML += "<td>&nbsp;</td>";
			}
			
			if (response[i].eta != null)
			{
				tableHTML += "<td>" + response[i].eta + "</td>";
			}
			else
			{
				tableHTML += "<td></td>";
			}
			
			tableHTML += "</tr>";
		}
	}
	
	$("table#jobs").html(tableHTML);	 
}