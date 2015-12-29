<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<div id="main">
	<div class="padding">
		<div class="right_side">
			<div class="sponsors">
				<a href="#" /></a> <a href="#" /></a> <a href="#" /></a>
			</div>
			<h3>版本说明</h3>
			<ul>
				<li><a href="#">V6.0(最新)</a></li>
				<li><a href="#">V5.X(极速&不兼容,官方推荐,重点研发, Why?)</a></li>
				<li><a href="#">V4.0(主要bug修复)</a></li>
				<li><a href="#">V3.0(兼容实体Bean,加密解密,SEO,基于spring jdbc)</a></li>
				<li><a href="#">V2.0(整合strut2, spring mvc)</a></li>
				<li><a href="#">anyline_mysql.jar anyline_mssql.jar 其他数据库支持</a></li>
				<li><a href="#">低于V2.0的版本请联系以前的QQ群技术支持(辅助升级到5.X)</a></li>
				<li><a href="#">IN-MVC版本请联系公司架构师或联系QQ群技术支持</a></li>
				<li><a href="#">公司订制商业版本请联系offical@anyline.org或手机</a></li>
			</ul>
			<div class="extra">
				<div class="padding">Place your extra content here like ads,
					news, tags,...</div>
			</div>
			<div class="ls">
				<h3>Demo</h3>
				<ul>
					<li><a href="http://www.cssmoban.com/">offical</a></li>
					<li><a
						href="http://www.cssmoban.com/free_css_xhtml_templates/">Templates
							(14)</a></li>
					<li><a href="http://www.cssmoban.com/webdesign/">Webdesign
							(11)</a></li>
					<li><a
						href="http://www.cssmoban.com/photoshop-webdesign-tutorials/">Tutorials
							(21)</a></li>
					<li><a href="http://www.cssmoban.com/photoshop-stuffs/">Photoshop
							(14)</a></li>
					<li><a href="http://www.cssmoban.com/css-techniques/">CSS
							Techniques (15)</a></li>

				</ul>
			</div>
			<div class="rs">
				<h3>Links</h3>
				<ul>
					<li><a href="http://www.cssmoban.com/code2css.php">Code2Css</a></li>
					<li><a href="http://www.cssmoban.com">Free Css Templates</a></li>
					<li><a href="http://www.cssmoban.com/website_templates.html">Website
							Templates</a></li>
					<li><a href="http://www.snewscms.com">sNews CMS</a></li>
					<li><a href="http://www.cssheaven.com/">Css Heaven</a></li>
					<li><a href="http://www.csscreme.com/">Css Creme</a></li>
					<li><a href="http://www.links4se.com/">Link4se.com</a></li>
				</ul>

			</div>
		</div>

		<div id="left_side">

			<div class="intro">
				<div class="pad">
					<h3>设计初衷</h3>
					
					为保证开发速度,产品质量,代码一致性,最终控制开发与维护成本.我们创建了<b>Anyline</b>. <br /> <b>Anyline</b>以数据库操作为核心,以WEB开发为背景，追求极致的开发效率。<br />
					从门户网站到OA/ERP,从制造业到金融业,从单人开发到几十人的团队开发,<br /> 经历了数年的实战验证,<b>Anyline</b>始终不负所托.<br />
					<b class="alert">速度与一致性</b>是<b>Anyline</b>各版本无止境的追求. <br /> <a
						href="/download" title="sNews cms">下载 </a> &nbsp;|&nbsp; <a
						href="/history" title="sNews cms">成长历程</a> &nbsp;|&nbsp; <a
						href="/atp">适用于场景</a>
				</div>
			</div>
			<div class="mpart">
				<h2>一行代码 自我介绍</h2>
				<ol class="code">
					<li class="t0">service.query("MEMBER",parseConfig(true,"SORT_CD:sort"));</li>
				</ol>
				<div>以上方法最终执行的SQL:SELEC * FROM MEMBER WHERE SORT_CD=?</div>
				<br />
				<div>MEMBER:需要查询的表</div>
				<div>parseConfig:收集http传入的参数</div>
				<div>true:是否需要分页(默认false)</div>
				<div>SORT_CD:sort</div>
				<div style="padding-left:25px;">
					SORT_CD:对应表中的列<br /> sort:对应http参数名<br /> 默认当(null !=
					sort值)时,最终会拼成SQL查询条件 WHERE SORT_CD= ?
				</div>
				<div class="date">
					<a href="/api/index">更多重载(28)</a>
				</div>
				<div>
					<a href="#">标签</a> | <a href="#">SEO</a> | <a href="#">加密解密</a> | <a
						href="#">日期</a> | <a href="#">Action</a> | <a href="#">Controller</a>
					| <a href="#">Service</a> | <a href="#">AJAX</a> |
				</div>
			</div>
		</div>
	</div>
	<div class="clear"></div>
</div>
