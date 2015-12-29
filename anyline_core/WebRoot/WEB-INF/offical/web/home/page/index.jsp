<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<jsp:include page="/WEB-INF/offical/web/home/inc/head.jsp"></jsp:include>
		
		<style>
		#divHello{
			text-align:left;
			margin: 0 auto;
			position:absolute;
			z-index:100;
			background-color:#000;
			color:#BECFCD;
			width:100%;
			padding:10px 10px 10px 200px;
			opacity:0;
		}
		.code_key{color:#857C6B}
		.code_str{color:#E3B144}
		.code_var{color:#DFDBBB}
		.code_des{color:#44544D}
		.code_tag{color:#DDC164}
		.code_num{color:#FFFF00}
		.code_class{color:#3084D8}
		.code_sql{color:green}
		</style>
	</head>
	<body>
		<jsp:include page="/WEB-INF/offical/web/home/inc/top.jsp"></jsp:include>
			
		<article class="container index">
			<section>
				<div class="container hero">
					<div class="jumbotron col-md-12">
						<h1><b>AnyLine Core</b></h1>
						<p style="line-height:50px;">
							提高开发速度 
							保证产品质量 
							保持代码一致性
							最终控制开发与维护成本<br/>
							以数据库操作为核心
							以WEB开发为背景 
							追求极致的开发效率<br/>
						</p>
						<a href="/download.cgi#struts23241" class="btn btn-primary btn-large"> 
							<img src="/offical/web/home/img/download-icon.svg"/> 下    载 ( V6.0 )
						</a> 
						<a href="/api/" class="btn btn-info btn-large"> 
							<img src="/offical/web/home/img/primer-icon.svg"/> 接口说明 ( V6.0 )
						</a>
						<a href="javascript:void(0)" onclick="fnHello();" class="btn btn-info" style="display:inline-block;width:100%">
							<b>一行代码自我介绍</b> 
						</a>
		<pre id="divHello">
		<span class="code_class">DataSet</span> set = <span class="code_var">service</span>.query(<span class="code_str">"member"</span>, parseConfig(true,<span class="code_str">"AGE:age","NAME:name%"</span>));<span style="float:right;"><a href="/api/"></>更多方法</a></span>
		
		方法说明:以分页方式 查询 年龄=20 并且 姓名以'张'开头的用户
		对应的URL参数: <a href="javascript:void(0);"></>http://localhost/test?age=20&name=张</a> 
		最终执行的SQL:<span class="code_sql">SELEC * FROM MEMBER WHERE AGE=20 AND NAME LIKE '张%' limit 0, 10</span>
		
		MEMBER:需要查询的表
		parseConfig:收集http传入的参数
		true:是否需要分页(默认false)
		AGE:age
			AGE:对应表中的列 
			age:对应url参数名 
			默认当(null != age值)时,最终会拼成SQL查询条件 WHERE AGE= ?
			
		类似的查询:
		<span class="code_des">/**
		 * 按条件查询
		 * @param src           数据源(表或自定义SQL或SELECT语句)
		 * @param configs       封装来自于http的查询条件
		 * @param conditions    固定查询条件
		 * @return
		 */</span>
		<span class="code_key">public</span> <span class="code_class">DataSet</span> query(<span class="code_class">DataSource</span> ds, <span class="code_class">String</span> src, <span class="code_class">ConfigStore</span> configs, <span class="code_class">String</span> ... conditions);
		<span class="code_key">public</span> <span class="code_class">DataSet</span> query(<span class="code_class">String</span> src, <span class="code_class">int</span> fr, <span class="code_class">int</span> to, <span class="code_class">String</span> ... conditions);
		<span class="code_key">public</span> &lt;T&gt; <span class="code_class">List</span>&lt;T&gt; query(<span class="code_class">Class</span><T> clazz, <span class="code_class">ConfigStore</span> configs, <span class="code_class">String</span> ... conditions);<span style="float:right;"><a href="/api/"></>更多方法</a></span>
		</pre>
					</div>
				</div>
				<script type="text/javascript">
					var helloFlag = 1;
					var i=0;
					var s=0.01;
					function fnHello(){
						var o = document.getElementById("divHello");
						var t = setInterval(function(){
							i+=s*helloFlag;
							if(i > 1){
								i = 1;
								helloFlag = -1;
								clearInterval(t);
							}
							if(i < 0){
								i = 0;
								helloFlag = 1;
								clearInterval(t);
							}
							if(o.filters){
								o.filters[0].opacity=i*100;
							}else {
								o.style.opacity=i;
							}
						},10);
					 }
				</script>
				<div class="container important-notes">
					<div class="col-md-12">
						<div class="row">
							<div class="column col-md-4">
								<h2>设计来源</h2>
								<p>
									Apache Struts 2.3.24.1 GA has been released<br />on 24 september
									2015.
								</p>
								Read more in <a href="announce.html#a20150924">Announcement</a> or
								in <a href="/docs/version-notes-23241.html">Version notes</a>
							</div>
							<div class="column col-md-4">
								<h2>适用场景</h2>
								<p>
									During <a href="http://www.meetup.com/sfhtml5/">SFHTML5</a>
									Google announced that they extend their program to cover the
									Apache Struts project as well. Now you can earn money preparing
									patches for us! <a href="submitting-patches.html#patch-reward">read more</a>
								</p>
							</div>
							<div class="column col-md-4">
								<h2>版本说明</h2>
								<ul style="font-size:10pt;">
									<li><a href="#">V6.0(最新)</a></li>
									<li><a href="#">V5.X(极速&不兼容)</a></li>
									<li><a href="#">V4.0(主要bug修复)</a></li>
									<li><a href="#">V3.0(兼容实体Bean,加密解密,SEO,基于spring jdbc)</a></li>
									<li><a href="#">V2.0(整合strut2, spring mvc)</a></li>
									<li><a href="#">anyline_mysql.jar anyline_mssql.jar 其他数据库支持</a></li>
									<li><a href="#">低于V2.0的版本请联系以前的QQ群技术支持</a></li>
									<li><a href="#">IN-MVC版本请联系公司架构师或联系QQ群技术支持</a></li>
									<li><a href="#">公司订制商业版本请联系offical@anyline.org或手机</a></li>
								</ul>
							</div>
						</div>
					</div>
				</div>
			</section>
		</article>
		<jsp:include page="/WEB-INF/offical/web/home/inc/bottom.jsp"></jsp:include>
	</body>
</html>
