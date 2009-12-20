/**
 * @author Jernej
 */

$(document).ready(function()
{	
	loadFormats();
	// Add handler for Add/Update button
	$("#addupdatebtn").click(AddRemoveClickStart);
	$("#removebtn").click(RemoveClickStart);
	$("#formatedit").change(FormatClick);
});

function loadFormats(selectid)
{
	$("select#formatedit").html("<option>Loading...</option>");
	$.get("/api/formats", null, function(data)
								{
									loadFormatsCB(data, selectid);
								}, "json");
}

function loadFormatsCB(response, selectid)
{		
	if (response.length == 0)
	{
		$("#formatedit").html('<option value="-1">No formats available.</option>');
	}
	else
	{
		var formatsHTML = '<option value="-1"> - Select one - </option>';
		
		for (var i = 0; i < response.length; i++)
		{
			formatsHTML += '<option value="' + response[i].id + '">' + response[i].name + '</option>';
		}
		
		$("#formatedit").html(formatsHTML);
		
		// Select value with passed ID
		if (selectid !== undefined)
		{
			$("#formatedit").val(selectid);
		}		
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
	if ($("input#id").val() == -1)
	{
		$.get("/api/addformat", formData, AddUpdateFormatCB, "json");	
	}
	else
	{
		$.get("/api/updateformat", formData, AddUpdateFormatCB, "json");
	}
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
	
	loadFormats(response.id);
}

function RemoveClickStart()
{
	if (statusShown) 
	{
		$("#response").fadeTo("normal", 0.01, RemoveClickCall);
	}
	else
	{
		RemoveClickCall();
	}	
}

function RemoveClickCall()
{
	$("#response").removeClass("success-response");
	$("#response").removeClass("error-response");
	$("#response").html("");
	
	// Serialize form for GET
	var formData = $("#edit-format").serialize();
	
	$.get("/api/removeformat", formData, RemoveClickCB, "json");
}

function RemoveClickCB(response)
{
	if (response.status == 'OK')
	{
		clearFormatForm();
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
	
	loadFormats();
}

function FormatClick()
{
	// Clear response
	$("#response").removeClass("success-response");
	$("#response").removeClass("error-response");
	$("#response").html("");
	$("#response").hide();
	statusShown = false;
	
	if (parseInt($("#formatedit").val()) == -1)
	{
		clearFormatForm();
	}
	else
	{
		$.get("/api/getformatinfo", {id:parseInt($("#formatedit").val())}, FormatInfoReceived, "json");
	}
}

function FormatInfoReceived(response)
{	
	// Fill form data
	var $fields = $(":input");
	
	for (var i = 0; i < $fields.length; i++)
	{
		$fields[i].value = response[$fields[i].name];
	}
}

function clearFormatForm()
{
	var $fields = $(":input");
	
	for (var i = 0; i < $fields.length; i++)
	{
		$fields[i].value = '';
	}
	
	// Set current ID to -1
	$("input#id").val(-1);
}
