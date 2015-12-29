<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.anyline.org/des" prefix="des"%>

<c:forEach var="item" items="${set }">
<des:a href="/api/u?cd=${item.CD }">${item.TITLE }</des:a><br/>
</c:forEach>
