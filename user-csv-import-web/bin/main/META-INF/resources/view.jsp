<%@ include file="/init.jsp" %>

<portlet:actionURL var="userCSVDataUploadURL" name="userCSVDataUpload"></portlet:actionURL>

<p>
	<b>Add user to Liferay DB from CSV</b>
</p>

<form action="${userCSVDataUploadURL}" enctype="multipart/form-data" method="post" id="csvDataFileForm">
	<div>
		<label>Upload User Data CSV :</label>
		<input type="file" name='<portlet:namespace/>csvDataFile' id="csvDataFile"></input>
	</div>
	<div>
		<input type="submit"></input>
	</div>
</form>