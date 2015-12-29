<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<jsp:include page="/WEB-INF/offical/web/home/inc/head.jsp"></jsp:include>
</head>
<body>
		<jsp:include page="/WEB-INF/offical/web/home/inc/top.jsp"></jsp:include>
		<article class="container">
		<div class="col-md-12">
		<jsp:include page="${content_page }"></jsp:include>
		</div>
		</article>
		<jsp:include page="/WEB-INF/offical/web/home/inc/bottom.jsp"></jsp:include>
</body>
</html>
