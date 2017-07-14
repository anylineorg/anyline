<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.anyline.org/core" prefix="al"%>
<%@ taglib uri="http://www.anyline.org/des" prefix="des"%>
<div style="line-height:30px;">
cnt:${set.size }
<hr/>	
checkbox:<al:checkbox name="mbr_chk" data="${set}" textKey="MMB_NAME" valueKey="MMB_ID"></al:checkbox>
cehckbox 是否:<al:checkbox data="{1:}" name="scan" value="${row.IS_NEED_SCAN_CODE }"></al:checkbox>
<hr/>
radio:
<al:radio name="mbr_rdo" id="rdo" data="${set}" textKey="MMB_NAME" valueKey="MMB_ID"></al:radio>
<hr/>
checkbox 与radio的id通过name_value生成 ,同一个页面中name不可重复
<hr/>
select:
<al:select data="${set }" textKey="MMB_NAME" valueKey="MMB_ID"></al:select>
<al:select data="{1:男,2:女}" value="1"></al:select>
<al:select data="${set }" head="" textKey="MMB_NAME" valueKey="MMB_ID"></al:select>
<hr/>
select text:
0=<al:selecttext data="${set }" textKey="MMB_NAME" valueKey="MMB_ID" value="0"></al:selecttext>
1=<al:selecttext data="{1:男,2:女}" value="1"></al:selecttext>
<hr/>
date:nvl=true(value为空时取当前时间否则不输出)<hr/>
<al:date nvl="true" format="yyyy-MM-dd hh:mm:ss"></al:date>
<hr/>
contains:
<al:contains value="1" data="{1,2}">1,2中包含,所以输出此body</al:contains>
<hr/>
加密:
0=<al:des>0</al:des>
<hr/>
ellipsis:两个英文和数字算一个长度
<al:ellipsis length="10">01234567890123456789ABCDEFG</al:ellipsis>
<hr/>
evl:第一个不为""和null的值
<al:evl>
<al:param value=""></al:param>
<al:param value="1"></al:param>
</al:evl>
<hr/>
nvl:第一个不为null的值 ""将被输出
<al:nvl>
<al:param value=""></al:param>
<al:param value="1"></al:param>
</al:nvl>
<hr/>
if:
<al:if test="${true}">true</al:if>
<al:if test="${false}">
true
<al:else>false</al:else>
</al:if>
<hr/>
money:
<al:money value="123"></al:money>
<hr/>
number:
<al:number value="123124" format="#.0"></al:number>
<hr/>
omit:0123456789=
<al:omit left="1" right="2">0123456789</al:omit>
<hr/>
random:
<al:random length="5"></al:random>
<hr/>
strip:清空html标签
<al:strip><b><u>加粗下划线</u></b></al:strip>
<hr/>
substring:0123456789(1,3)=
<al:substring begin="1" end="3">0123456789</al:substring>
<hr/>
sum:
<al:sum data="${ set}" key="MMB_ID"></al:sum>
<hr/>
加密:jsp中加密需要用到des:下的几个标签
<des:a href="/test?cd=1">test?cd=1</des:a>
<hr/>

<script>
//
function fnAjax(id){
	al.ajax({
		url:'/test/ajax.do',
		cache:false,	//是否启用缓存,默认否
		data:{id:id},	//java中通过request.getParamater(String)
		callback:function(result,data,msg){
			var html = "";
			if(!result){
				html += "<div style='color:red;'>" + msg + "</div>";
			}else{
				var size = data.length;
				for(var i=0; i<size ;i++){
					var item = data[i];
					html += "<div> ID:" + item['MMB_ID'] + "用户名:"+item['MMB_NAME'];
				}
			}
			$('#divAjaxCon').html(html);
		}
	});
}
</script>
AJAX:
<a href="javascript:void(0);" onclick="fnAjax(0);">ajax success</a>
|
<a href="javascript:void(0);" onclick="fnAjax(1);">ajax fail</a>
<div id="divAjaxCon" style="background-color:#ccc;"></div>
<hr/>

<script>
function fnLoadTmp(){
	var path = "<al:des>/WEB-INF/tmp.jsp</al:des>";
	al.template(path,function(result,data,msg){
		$('#divAjaxCon').html(data);
		alert(data);
	});
}
//al.template实际是调用了TemplateController.load(/al/tmp/load.do)方法,该方法中通过parseJsp解析了tmp.jsp文件



function fnLoadTmp1(){
	al.template({parser:'/test/tmp.do'},function(result,data,msg){
		$('#divAjaxCon').html(data);
		alert(data);
	});
}
//  /test/tmp.do中需要将返回的内容编码后返回 BasiUtil.escape(String)
//模板加载默认启用ajax缓存,同一个页面同一次请求(页面不刷新)内的相同模板只连接服务器一次
//关闭缓存: {cache:false,path:''}
</script>
模板加载:<a href="javascript:void(0);" onclick="fnLoadTmp();">template load</a>
|<a href="javascript:void(0);" onclick="fnLoadTmp1();"> 自定义</a>
<hr/>
<script>
function fnCallback(result,data,msg){
	console.log(data.length);
}
function fnParam(){
	var data = {};
	data['nm'] ='zhang';
	return data;
}
</script>
<al:ajax url="/test/ajax.do" param="fnParam" callback="fnCallback"></al:ajax>
<des:a href="/test/index.do?id=1">加密URL(id=1)</des:a>
<des:a href="/test/index.do?member=1">加密URL(member=1)</des:a>
</div>
<al:text data="${set}" textKey="MMB_NAME" valueKey="MMB_ID" value="0"/>
