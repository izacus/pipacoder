/**
 * 
 */


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
			
			if (response[i].progress != null)
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
