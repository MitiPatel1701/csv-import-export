<%@ include file="./init.jsp"%>

<portlet:resourceURL var="exportCSVURL">
	<portlet:param name="export" value="exportCSV" />
</portlet:resourceURL>

<h2>Data Export as CSV in Liferay Portlet</h2>
<div>
	<a href="${exportCSVURL}">Export</a>
</div>

<br />

<table border="1">
	<tbody>
	<tr>
		<th>UserID</th>
		<th>Email Address</th>
		<th>First Name</th>
		<th>Last Name</th>
		<th>Birth Date</th>
	</tr>

	<c:forEach items="${usersList}" var="user" varStatus="loop">
	<c:set var="birthDay" value="${user.getBirthday()}" />
	<tr>
		<td>${user.getUserId()}</td>
		<td>${user.getEmailAddress()}</td>
		<td>${user.getFirstName()}</td>
		<td>${user.getLastName()}</td>
		<td><fmt:formatDate pattern="dd-MM-yyyy" value="${birthDay}" /></td>
	</tr>
	</c:forEach>
	</tbody>
</table>





