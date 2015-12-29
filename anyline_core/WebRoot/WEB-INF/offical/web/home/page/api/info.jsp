<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<form action="s" method="post">
	<input name="cd" value="${row.CD }"><br/>
	BASE:<input name="base" value="AI0001"/>
	标题:<input name="title" value="${row.TITLE }"/>
	排序:<input name="idx" value="${row.IDX }"/><br/>
	<input type="submit" value="SAVE"/>
	CONTENT:
	<textarea style="width:100%;height:500px;" name="content">${row.CONTENT }</textarea>
</form>