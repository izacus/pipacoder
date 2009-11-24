/**
 * @author Jernej
 */

$(document).ready(function()
{	
	loadFormats();
	// Add handler for Add/Update button
	$("#addupdatebtn").click(AddRemoveClickStart);
});


function loadFormats()
{
	$("select#formatedit").html("<option>Loading...</option>");
	$.get("/api/formats", null, loadFormatsCB, "json");
}

function loadFormatsCB(response)
{		
	if (response.length == 0)
	{
		$("#formatedit").html("<option>No formats available.</option>");
	}
	else
	{
		var formatsHTML = '<option> - Select one - </option>';
		
		for (var i = 0; i < response.length; i++)
		{
			formatsHTML += '<option>' + response[i].name + '</option>';
		}
		
		$("#formatedit").html(formatsHTML);		
	}
}

var statusShown = false;

function AddRemoveClickStart()
{	
	if (statusShown) 
	{
		$("#response").fadeTo("normal", 0.01, AddRemoveClickCall);
	}
	else
	{
		AddRemoveClickCall();
	}	
};

function AddRemoveClickCall()
{
	$("#response").removeClass("success-response");
	$("#response").removeClass("error-response");
	$("#response").html("");
	// TODO: validate form

	// Serialize form for GET
	var formData = $("#edit-format").serialize();

	// Create GET call to upload new format
	$.get("/api/addformat", formData, AddUpdateFormatCB, "json");
}

function AddUpdateFormatCB(response)
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
}
