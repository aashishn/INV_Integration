<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<%@include file="includes.jsp"%>
<title></title>
<style type="text/css">
.header {
}
.footer {
    position: fixed;
    height: 80px;
    bottom: 0;
    width: 100%;
    background-color: #F5F5F5;
}

.navbar-fixed-top {
	/*min-height: 120px;*/
}
</style>
</head>
<body>
	<div class="header">
		<tiles:insertAttribute name="header" ignore="true" />
	</div>
	<div class="container">
		<tiles:insertAttribute name="body" />
	</div>
	<div class="footer">
		<tiles:insertAttribute name="footer" ignore="true" />
	</div>
</body>
</html>
