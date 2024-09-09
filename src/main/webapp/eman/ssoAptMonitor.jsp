<%@ page contentType="text/html;charset=UTF-8"%>
<%@ page import="java.net.*"%>
<%@ page import="java.io.IOException" %>
<%
	response.addHeader("Cache-Control", "no-cache");//HTTP/1.1
	response.addHeader("Expires", "-1");
	response.addHeader("Pragma", "no-cache");//HTTP/1.0
%>
<%
	String responseString = "FAILURE";
	try{
		URL url = new URL("https://logcso.cloudapps.cisco.com/logcso/rest/obs/clickCSAMLink");
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		if(con.getResponseCode() == 200)
			responseString = "SUCCESS";
		
	}catch(IOException ioex){
		responseString = "FAILURE";
	}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>SSO Apt Eman Monitor</title>
</head>
<body>
	<div><%=responseString%></div>
</body>
</html>