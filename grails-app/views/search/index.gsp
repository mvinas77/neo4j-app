<%@ page import="search.Facet" %>
<!DOCTYPE html>
<html>
	<head>
		<!--meta name="layout" content="test"/-->
		<title>I am a test page</title>
		<!--link rel="stylesheet" href="${resource(dir: 'css', file: 'main.css')}" type="text/css"-->
	</head>
	<body>
	<h2>Result Items</h2>
	  <ul>
		  <% result.items.each { %>
		  <li><%=it%></li>
             <% } %>

	  </ul>
		<p>Shows ${result.start} to ${result.end} from ${result.numFound}</p>

	<h2>Facets</h2>
	   <ul>
		   <% result.facets.each { facet -> %>
		   		  <li>${facet.name} (${facet.count})
		   			<ol>

		   		<% for (int pos=0; pos < facet.options.size; pos++) { %>

		   			 <li>${facet.options[pos].name} (${facet.options[pos].count})</li>
				   <% } %>
					</ol>
	              </li>
		      <% } %>
	   </ul>

	</body>
</html>