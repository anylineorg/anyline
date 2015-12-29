<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.anyline.org/des" prefix="des"%>

<link rel="stylesheet" href="/common/plugin/prism/prism.css">
<script src="/common/plugin/prism/prism.js"></script>
<c:forEach var="item" items="${set }">
<des:a href="/api/?cd=${item.CD }">${item.TITLE }</des:a>
|
</c:forEach>
<pre><code class="language-java">
${row.CONTENT }
</code></pre>
