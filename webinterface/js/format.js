/**
 * 
 */

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


function loadFormatPresets()
{
	$("select#select-preset").html("<option>Loading...</option>");	
	$.get("/api/presets", null, loadFormatPresetsCB, "json");
}

function loadFormatPresetsCB(response)
{
	var presetListHTML = '<option> ---- </option>';
	
	for (var i = 0; i < response.length; i++)
	{
		presetListHTML += '<option value="' + response[i].id + '" label="' + response[i].name + '">' + response[i].name + '</option>';
		availablePresets[response[i].id] = response[i].formatIds;
	}
	
	$("select#select-preset").html(presetListHTML);
}

