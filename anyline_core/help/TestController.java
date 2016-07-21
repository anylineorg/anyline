package com.chuangxingu.web.home.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.anyline.cache.CacheUtil;
import org.anyline.config.db.Procedure;
import org.anyline.config.db.impl.ProcedureImpl;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.util.WebUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller("web.home.controller.TestController")
@RequestMapping("/test")
public class TestController extends BasicController {
	protected String dir = "test";
	@RequestMapping("index")
	public ModelAndView index(HttpServletRequest request, HttpServletResponse response){
		ModelAndView mv = template("index.jsp");
		service.query("web.pc.test:GET_MEMBERS","TID.foo:10");
		DataSet set = service.query("members", parseConfig(true));
//		set = service.query("members", parseConfig(true, "+mmb_id:%id++:member++"));
		mv.addObject("set", set);
		List<Object> ps = getParams("id",false);
		service.deleteTable("members", "mmb_id", "1");
		return mv;
	}
	@RequestMapping("ajax")
	@ResponseBody
	public String ajax(HttpServletRequest request, HttpServletResponse response){
		DataSet set = service.query("members", parseConfig(true));
		String id = getParam("id");
		List<Object> ids = getParams("id", true);
		if("1".equals(id)){
			return fail("模拟fail");
		}
		return success(set);
	}
	@RequestMapping("tmp")
	@ResponseBody
	public String tmp(HttpServletRequest request, HttpServletResponse response){
		DataSet set = service.query("members", parseConfig(true));
		request.setAttribute("set", set);
		String html = "";
		try {
			html = WebUtil.parseJsp(request, response, "/WEB-INF/tmp.jsp");
		} catch (ServletException | IOException e) {
			e.printStackTrace();
		}
		return success(html);
	}
	@RequestMapping("members")
	@ResponseBody
	public String members(HttpServletRequest request, HttpServletResponse response){
		DataSet set = null;
		/**
		 * anyline只是在 js-jsp-controller-service-cache-jdbc 之间的一个辅助
		 * 只是为了提高开发效率 统一编写规则 降低开发难度 并不是替代
		 * 使用之前还是需要先熟悉js jsp springmvc或struts sql
		 * anyline_core.jar
		 * anyline_mysql.jar
		 * anyline_springmvc.jar
		 */
		
		//查询主要通过service.query() queryRow()实现
		//query返回DataSet 相当于一个表一个集合
		//queryRow返回DataRow 相当于一行 一个Map
		//queryRow的结果一定要判断是否为空再调用其方法
		//所有的查询数据,在DataRow中 key以大写形式保存
		
		
		//SELECT * FROM MEMBERS
		set = service.query("members");
		
		
		//查询条件通过parseConfig解析 列名:运行符key
		//值为"" null "null" 的条件默认不会拼接到SQL
		//运算符缺省为= parseConfig("ID:>id","ID:<>id","ID:id")
		
		//SELECT * FROM MEMBERS WHERE MMB_ID = ?(getParam("id")) AND MMB_AGE >= ? (getParam("age"))
		//如果id未传入或传入null "" 则不执行mmb_id = ?
		set = service.query("members", parseConfig(true, "mmb_id:id"));
		set = service.query("members", parseConfig(true, "mmb_age:>=age"));
		
		//必须参数
		//如果id未传入或传入null "" 则执行mmb_id IS NULL
		set = service.query("members", parseConfig(true, "+mmb_id:id"));

		//like 
		set = service.query("members", parseConfig(true, "mmb_name:%name"));
		set = service.query("members", parseConfig(true, "mmb_name:name%"));
		set = service.query("members", parseConfig(true, "mmb_name:%name%"));

		//in
		set = service.query("members", parseConfig(true, "mmb_id:[id]"));

		//加密解密
		//key之后跟两个标记位++ -+ +- 
		//第1个表示 key是否加密 第2个表示value是否加密
		//key value同时加密  test.do?密文	(一般通过<des:a/>标签生成)
		//key 不加密 value加密 test.do?id=密文
		//只有敏感数据才需要加密 类似新闻,公告之类的数据没有必要
		set = service.query("members", parseConfig(true, "mmb_id:id++"));
		set = service.query("members", parseConfig(true, "mmb_id:id+"));//与-+相同
		String id = getParam("id", false, true);
		System.out.println("id="+id);
		
		//默认值(仅支持一个) column:key:defkey   或 column:key:{defvalue} 
		//如果id未传入则执行mmb_id = getParam("member"); mmb_id = 0; mmb_name like '%ljs%'
		set = service.query("members", parseConfig(true, "+mmb_id:id:member"));
		set = service.query("members", parseConfig(true, "+mmb_id:id-+:member-+"));
		set = service.query("members", parseConfig(true, "+mmb_id:id:{0}"));
		set = service.query("members", parseConfig(true, "+mmb_id:id:{[1,2,3]}"));
		set = service.query("members", parseConfig(true, "+mmb_id:[id:member]"));
		set = service.query("members", parseConfig(true, "+mmb_name:%name:member%"));
		set = service.query("members", parseConfig(true, "+mmb_name:%name:{ljs}%"));
		set = service.query("members", parseConfig(true, "+mmb_id:%id++:member++"));
		
		
		
		//分页 通过parseConfig(true)或parseConfig(10
		//执行分页查询后,在jsp中通过{navi}或{set.navi}显示分页 
		//{navi}将生成html.form,parseConfig()解析出来的查询参数会隐藏于form中,
		//如果参数不是通过parseConfig()解析,可以通过parseConfig().addParam(key,value)设置隐藏 以保证点下一页时还保持同样的查询条件
		//分页引用的js css通过配置文件指定
		//<property key="NAVI_STYLE_FILE_PATH">/web/common/plugin/navi/skin.css</property>
		//<property key="NAVI_SCRIPT_FILE_PATH">/web/common/plugin/navi/navi.js</property>
		service.query("members", parseConfig(true));
		service.query("members", parseConfig(10));
		service.query("members", 0, 9);	//不分页只查0-9条
		
		//ajax分页
		//ajax分页通过<al:navi/>标签实现
		//<al:navi id="borrow" url="/web/hm/bor/l.do" param="fnGetParam" container="divList" naviContainer="divNavi" empty="没有更多内容" callback="fnCallback"></al:navi>
		//id:在同一个页面显示多个分页时,需要通过id区分,如果不写id将从下标0开始递增
		//param:参数或收集参数的函数最终需json格式 
		//container:分页数据显示的容器的id 
		//naviContainer:分页下标显示的容器的id 
		//callback:回调成功后执行的函数 指定callback后将默认不显示分页数据与下标 只有在callback逻辑比较复杂时才需要
		//after:分页数据显示或或callback执行完后执行after
		//intime:false 分页请求默认在页面加载完成后发出.
		//重新请求分页数据需要调用_navi_init(_anyline_navi_conf_0)
		//后台通过navi(request, response, set,"分页样式文件");返回数据
//		navi标签的属性
//		private String url				;	//数据来源
//		private String param			;	//参数收集函数
//		private String container		;	//返回内容容器id
//		private String type = "ajax"	;	//分页方式 ajax | jsp
//		
//		private String id				;	//一个页面内多个标签时需要id区分
//		private Boolean intime = false	;	//实时执行,否则放入jqery.ready
//		private String callback			;	//回调函数
//		private String before			;	//渲染之前调用
//		private String after			;	//渲染之后调用			
//		private String bodyContainer	;	//如果body与page分开
//		private String naviContainer	;	//如果body与page分开
//		private String empty			;	//空数据显示内容
		//后台参数
		//parseConfig只能接收http中的参数
		//对于非http参数直接通过通过 parseConfig().addCondition(key,value);addConditions(key,values)传入
		service.query("members", parseConfig().addCondition("id", "1"));
		List<String> ids = new ArrayList<String>();
		ids.add("1");
		ids.add("2");
		service.query("members", parseConfig().addConditions("id", ids));

		//或直接通过String传入query方法中
		//需要解析的条件以:分开 不需要解析的直接写原生SQL 
		//对于时间格式的条件 因为时间中有: 会造成解析器误解 所以需要以{}形式转入 {}参数解析原样拼接到SQL
		service.query("members", parseConfig(true), "mmb_id=1");
		service.query("members", parseConfig(true), "mmb_name like '%ljs%'");
		service.query("members", parseConfig(true), "mmb_id:%1%");
		service.query("members", parseConfig(true), "mmb_id:<>1");
		service.query("members", parseConfig(true), "{reg_tile > '12:00'}");
		
		
		//指定列名(系统实现基本功能后需要完善SQL,特别是对于列数较多的表或视图)
		//多个列名以,分隔
		
		service.query("members(mmb_id,mmb_name)", parseConfig(true));
		service.query("members(mmb_id as id, mmb_name as name)", parseConfig(true));
		service.query("members(distinct mmb_id as id, mmb_name as name)", parseConfig(true));
		service.queryRow("members(max(mmb_id) as max_id)");
		service.queryRow("members(avg(mmb_age) as avg_age)");
		//对于有,的函数为避免解析器误解需要以{}形式传入{}中的参数将作为一整列拼入SQL
		service.query("members(mmb_id, {convert(int,mmb_sex) as sex})", parseConfig(true));

		
		
		//插入
		DataRow member = new DataRow("members");
		member.addPrimaryKey("mmb_id");
		member.put("name","ljs");
		service.insert(member);
		//对于自增长的主键,在保存后需要取主键的情况,需要在插入前指明主键
		
		//更新 按形式上的主键执行更新 也可以指定多列
		// update members set mmb_name = ? where mmb_id = ?
		member = service.queryRow("members", "mmb_id:1");
		member.addPrimaryKey("mmb_id");
		member.put("mmb_name","ljs2");
		service.update(member, "name");
		//update执行时将不对值为""或null的列操作,如果需要强制设置成NULl需要以大写NULL或{null}传入
		
		
		//put同时指定主键 row.put(key,value,是否作为主键, 是否清之前设置的主键:默认true)
		member.put("mmb_id", "1", true);	//此时的主键为mmb_name
		member.put("mmb_name", "1", true);	//此时的主键为mmb_name
		member.put("mmb_code", "A01", true, false);//此时的主键为mmb_name mmb_code
		
		//save  根据主键是否为空来决定调用insert或update
		service.save(member);
		
		//执行insert save upate时,表名与query不一致的情况
		//如从视图中查询出数据,修改后保存到表中
		member = service.queryRow("uv_members", "mmb_id:1");
		member.addPrimaryKey("mmb_id");		//设置主键
		member.put("mmb_name", "ljs");
		member.put("update_time", "{now()}");
		service.update("members", member, "mmb_name","update_time");	//只更新指定的两列 数据由uv_members视图中查出,但更新到mebmers表中
		
		//执行更新操作时会根据指定的主键生成where条件
		//DataRow中批定主键
		member.setPrimaryKey("mmb_id","mmb_name");		//设置主键
		member.addPrimaryKey("mmb_age");				//在原主键基础上增加新主键, 执行后member共有3个主键
		
		member = new DataRow();
		member.put("mmb_id", "1", true);			//设置属性值,同时设置这一列为主键
		member.put("mmb_name", "zhang", true);		//mmb_name 代替mmb_id成为主键(mmb_id的值不受影响)
		member.put("mmb_age", "20", true, false);	//第一个true表示设置mmb_age为主键,最后一个false表示不代替mmb_name,而是在原主键基础上增加新主键,执行后member有两个主键mmb_name,mmb_age 
		
		
		
//		 * save insert区别
//		 * 操作单个对象时没有区别
//		 * 在操作集合时区别:
//		 * save会循环操作数据库每次都会判断insert|update
//		 * save 集合中的数据可以是不同的表不同的结构 
//		 * insert 集合中的数据必须保存到相同的表,结构必须相同
//		 * insert 将一次性插入多条数据整个过程有可能只操作一次数据库  并 不考虑update情况 对于大批量数据来说 性能是主要优势
		
		
		
		
		//删除
		service.delete(member);//根据主键删除
		service.delete(member, "mmb_id");	//根据mmb_id删除,忽略主键
		service.delete("member", member, "mmb_id");	//从member表中删除
		service.deleteTable("members", "mmb_id", getParams("id"));	//直接根据表与列删除
		service.deleteTable("members", "mmb_id", "1","2");	//直接根据表与列删除
		
		//原生 SQL 特殊情况下才需要执行原生SQL
		service.query("SELECT * FROM members WHERE mmb_id = 2",parseConfig(true),"mmb_name:ljs");
		service.execute("UPDATE members SET mmb_name = 'ljs' WHERE mmb_id = 2");
		
		
		
		//缓存 基于ehcache实现
		//在query,queryRow基础上 加1个参数 cacheKey(在ehcache.xml中定义)
		service.cache("cache_key", "members", parseConfig(true));
		service.cacheRow("cache_key", "members", parseConfig(true));
		//对于使用同一个cache key的查询可以通过cache_key:flag的形式区分以避免数据覆盖
		service.cache("cache_key:index", "members", parseConfig(true));
		
		//清空缓存
		service.removeCache("cache_key", "members", parseConfig(true),"name:ljs"); 
		service.clearCache("cache_key");
		//总行数查询慢时 设置总行数缓存时间
		parseConfig().setTotalLazy(1000*10);
		/*
		    ehcache.xml:标准的ehcache定义文件,定义缓存的生存时间
			anyline-config-reflush-cache.xml:定义强制刷新缓存的最高频率,以秒为单位,表示N秒内最多接收一次刷新调用
			刷新缓存的调用在CacheUtil中 与pay,msg项目中的util类似
			如果刷新及时,则在ehcache.xml配置中延长缓存生存时间
			主动刷新频率不需要太高(秒数不需要太低),根据缓存耗时设置,
			如:刷新一次耗时10秒,设置刷新频率5秒,在1,2,3,4,5,6,7,8秒时分别调用了8次,刚1,6秒的调用会生效.但1,6启动的线程不一定哪一个先执行完,所以会出现脏读幻读问题.
			CacheUtil会限制第1次没有执行完前不接受第2次请求,但只是在编写代码无误的情况下.
			所以刷新间隔一定远高于10秒,保证每次调用时,前一次已执行完.
			如果没有调用刷新,频率高也没有效果,反而在高峰期加重服务器压力.
			
			
			缓存中取出的数据不要再执行加密,否则会造成多重加密效果
			需要加密显示的在jsp中通过<al:des/>或<des:/>标签实现
		 */
		if(CacheUtil.start("CACHE_REFLUSH_PERIOD_OPTION")){
			//数据操作
			CacheUtil.stop("CACHE_REFLUSH_PERIOD_OPTION");
		}
		
		
		//是否存在
		service.exists("members", "mmb_id:1");
		//计数
		service.count("members", "mmb_name:%ljs%");
		
		
		//执行存储过程
		List<Object> ouputs = service.executeProcedure("proc_pay", "input_param","input_param");
		Procedure proc = new ProcedureImpl();
		proc.addInput("input_param");
		proc.addInput("input_param");
		proc.regOutput();
		proc.regOutput();
		service.executeProcedure(proc);

		//从存储过程中查询
		set = service.queryProcedure("proc_members", "input_param");
		set = service.queryProcedure(proc);
		
		
		service.query("web.pc.test:GET_MEMBERS", "MMB_ID:1","DATE.FR:2015-01-01","MGR_ID:[1,2]");
		service.query("web.pc.test:GET_MEMBERS", parseConfig("MMB_ID:id","DATE.FR:fr","MGR_ID:[mgr]"));
		//自定义SQL
		//web.pc包下的test.xml中定义的GET_MEMBERS
		/* <sqls>
				<sql id="GET_MEMBERS">
					<text>
						SELECT * FROM MEMBERS
					</text>
					<condition id="DATE">
					 mmb_RegisterTime >= :FR
					</condition>
					<!-- test=true时 当前条件生效-->
					<condition id="AGE" static="true" test="var>10">
						AGE > 18
					</condition>
					<!-- test=true 并且 MMB_ID  非空时 当前条件有效 -->
					<condition id="MMB_ID" test="var>10">
					 MMB_ID = ?
					</condition>
					<!--parseConfig("MGR_ID:[mgr]")-->
					<!--parseConfig("MGR_ID:[mgr:mgrId]")-->
					<!--parseConfig("MGR_ID:[mgr:{[1,2,3]}]")-->
					<condition id="MGR_ID">
					 	MGR_ID IN(:MGR_ID)
					</condition>
					
					<condition id="BCM_ID">
					 	BCM_ID IN(:BCM_ID)
					</condition>
				</sql>
			</sqls>
		 * */
		//自定义SQL in
		//BCM_ID IN(:BCM_ID)
		List<String> list  = new ArrayList<String>();
		list.add("1");
		list.add("2");
		service.query("web.pc.test:GET_MEMBERS","MGR_ID:[1,2]");
		service.query("web.pc.test:GET_MEMBERS","MGR_ID:[1,2]","AGE.var:11");
		service.query("web.pc.test:GET_MEMBERS", parseConfig("AGE.var:age"));
		service.query("web.pc.test:GET_MEMBERS", parseConfig("MGR_ID:[mgr]"));
		service.query("web.pc.test:GET_MEMBERS", parseConfig().addConditions("MGR_ID", "[11,22]"));
		service.query("web.pc.test:GET_MEMBERS", parseConfig().addConditions("MGR_ID", list));
		service.query("web.pc.test:GET_MEMBERS", parseConfig("MGR_ID:[mgr:{[1,2,3]}]"));
		service.query("web.pc.test:GET_MEMBERS", parseConfig("MGR_ID:[mgr0:{[1,2,3]}]"));
		//为什么 parseConfig中需要{}
		//因为默认情况parseConfig需要从request中取值:后表示http.key
		//注意condition中的static 与test
		//static:条件中没有需要解析的变量
		//test:一个OGNL表达式 结果为false时 当前条件无效 赋值与其他变更相同 "AGE.var:10"
		
		
		//异步
		//public int save(boolean sync, String dest, Object data, boolean checkPriamry, String ... columns);
		service.save(true, "members", member);
		//当svae或insert,update方法耗时过长时,将影响主线程影响速度
		//sync=true时save方法将开启新的子线程执行,以保证主线程的影响速度
		//注意开启过多的子线程占用资源的问题,
		//只是针对执行耗时过长的操作,并不适用于执行频繁的操作(考虑使用缓存或消息列表)
		
		//批量插入(异步)
		//public int batchInsert(String dest, Object data, boolean checkPriamry, String ... columns);
		//在需要大量插入操作,并不需要返回执行结果时,可以通过批量插入,批量操作并不实时执行,会先将数据放入池中,在了线程中执行统一操作
		
		 
		//set的几个方法
		set.getRow(0);
		set.getRows("mmb_name:ljs","age:1");				//符合条件的行
		set.getRows("mmb_name","ljs", "age","1");
		set.getRows(0, 5);
		set.sum("mmb_id");
		set.avg("mmb_age");
		set.contains(new DataRow());
		set.union(new DataSet(),"mmb_id");
		set.unionAll(new DataSet());
		set.difference(new DataSet(), "mmb_id");
		set.or(new DataSet());
		set.distinct("mmb_id","mmb_name");
		set.fetchValues("mmb_name");
		set.fetchDistinctValue("mmb_name");
		set.max("mmb_age");
		set.min("mmb_age");
		set.isExpire(1000*60*60);								//从创建到现在是否已超过ms毫秒 (DataRow有相同函数)
		set.toJSON();
		set.dispatchItems(new DataSet(), "mmb_id","mmb_name");
		set.group("mmb_id", "mmb_name");
		
		
		
		//BasicUtil的方法
		//DateUtil
		//FileUtil
		//WebUtil
		//HttpUtil
		//RegularUitl
		
		//模板
		ModelAndView mv = template("index.jsp","default");
		mv = template("index.jsp");
		//  /WEB-INF/web/home/template/layout/default.jsp
		//  /WEB-INF/web/home/page/test/index.jsp
		
		//include只在模板中出现,内容页中不出现
		//修改结构时可以统一修改,模板只有一个,而内容页有多个 模板页编写简单,内容页随业务变化
		//不是每个实现内容页的人都熟悉整体结构
		//内容页多处被引用
		
		//ajax
		/*
		 * JS;
		 * al.ajax({
		 * 		url:'/test/ajax.do',
		 * 		//data:{id:1},
		 * 		data:fn,
		 * 		callback:function(result,data,msg){
		 * 			
		 * 		}
		 * });
		 * controller:
		 * @RequestMapping("ajax")
		 * @ResponseBody
		 * return success(Object);
		 * return fail("提示信息");
		 * 
		 * 
		 * */
		//ajax加载JSP模板(模板内容默认缓存,强制刷新 cache:false)
		/*
		 * al.template({path:'<al:des>/WEB-INF/test.jsp</al:des>',param1:'参数1'},function(result,data,msg){
		 *		alert(data);
		 *	});
		 * 
		 */
		/**
		 * 加载服务器端文件
		 * path必须以密文提交 <al:des>/WEB-INF/template/a.jsp</al:des>
		 * 以WEB-INF为相对目录根目录
		 * al.template('/WEB-INF/template/a.jsp',function(result,data,msg){alert(data)});
		 * al.template({path:'template/a.jsp', id:'1'},function(result,data,msg){});
		 * 模板文件中以${param.id}的形式接收参数
		 * 
		 * 对于复杂模板(如解析前需要查询数据)需要自行实现解析方法js中 通过指定解析器{parser:'/al/tmp/load1.do'}形式实现
		 *controller中通过 WebUtil.parseJsp(request, response, file)解析JSP
		 *注意 parsejsp后需要对html编码(以避免双引号等字符在json中被转码) js接收到数据后解码
		 *escape unescape
		 */
		//微平台中的模板引用 相对复杂 
		//虽然会快一点 但只是毫秒级的差别 一般情况下不需要用
		
//		include子页面为父页面设置值
//		如:从子页面给框架页面设置title, keywores, description
//		关于jsp的两个阶段,这里我们需要把执行分成合并与输入两个阶段
//		合并指把所有include进来的jsp编译并合并成一个java文件(参考这个目录work\org\apache\jsp\WEB_002dINF下的文件)
//		输出是指往response写入的过程
//		为使用理解我们可以这样描述执行过程
//		父页面合并  子页面合并 输出
//		所以虽然父页面先执行了合并,但此时还没有执行输出,所以子页面还有机会设置一个全局变量,在父页面输出时引用
//
//		如:
//		父页面<meta name="keywords" content="${seo_keywords}"/>
//		子页面<c:set var="seo_head_keywords" value="关键字1,关键字2" scope="request"/>
//
//
//		实际应用中
//		子页面有时不指定变更值如title,我们需要一个默认值 
//		<meta name="title" content="<al:evl>
//		<al:param>${seo_head_title }</al:param>
//		<al:param>互联网信息咨询服务平台提供抵押贷款转介绍服务</al:param>
//		</al:evl>"/>
//
//		或者直接使用<seo>标签
		
		
		//配置文件
		/*
		    RELOAD					配置文件热加载间隔(0-只在启动时加载一次)
			DEBUG					是否显示更多日志信息
			SQL_STORE_DIR			自定义SQL存放path
			DES_KEY_FILE			密钥文件path
			SHOW_SQL				是否显示执行过程的中SQL
			SQL_DEBUG				是否显示更多的SQL执行状态
		 */
		//对于多个环境配置不同的情况 如生产环境与测试环境与开发环境
		//开发环境多变,生产与测试相对固定
		//为避免开发环境提交的配置误覆盖生产环境.需要在生产环境中创建一份以anyline-config-*.xml配置文件
		//系统加载配置文件时会用anyline-config-*.xml中的内容覆盖anyline-config.xml中key相同是内容
		
		
		
		//{}表示的原生值(不需要解析)的情况:查询列,查询条件,默认值,更新值
		//数组用[]表示[1,2,3] CD:[cd]
		//查询条件拼接顺序先拼接query参数再拼接parseConfig()参数 注意拼接顺序与执行顺序是相反的(根据数据库不同)
		
//		nvl与evl的区别
//		n:null 
//		e:empty
//		nvl:取第一个 != null 的值 nvl认为""符合条件
//		evl:取第一个 != null 并 != ""的值  evl认为""不符合条件
//		nvl有可能取出"" evl不会取出"" 并且evl内在判断value之前执行的了trim操作
		

		return success(set);
	}
//	public DataSet query(String src, ConfigStore configs, String ... conditions);
//	public DataSet query(String src, String ... conditions);
//	public DataSet query(String src, int fr, int to, String ... conditions);
//	/**
//	 * 
//	 * @param ds
//	 * @param cache	对应ehcache缓存配置文件 中的cache.name
//	 * @param src
//	 * @param configs
//	 * @param conditions
//	 * @return
//	 */
//	public DataSet cache(String cache, String src, ConfigStore configs, String ... conditions);
//	public DataSet cache(String cache, String src, String ... conditions);
//	public DataSet cache(String cache, String src, int fr, int to, String ... conditions);
//
//	
//	public <T> List<T> query(Class<T> clazz, int fr, int to, String ... conditions);
//	public <T> List<T> query(Class<T> clazz, ConfigStore configs, String ... conditions);
//	public <T> List<T> query(Class<T> clazz, String ... conditions);
//
//	public DataRow queryRow(String src, ConfigStore configs, String ... conditions);
//	public DataRow queryRow(String src, String ... conditions);
//	
//	public DataRow cacheRow(String cache, String src, ConfigStore configs, String ... conditions);
//	public DataRow cacheRow(String cache, String src, String ... conditions);
//
//	/**
//	 * 删除缓存 参数保持与查询参数完全一致
//	 * @param cache
//	 * @param src
//	 * @param configs
//	 * @param conditions
//	 * @return
//	 */
//	public boolean removeCache(String cache, String src, ConfigStore configs, String ... conditions);
//	public boolean removeCache(String cache, String src, String ... conditions);
//	public boolean removeCache(String cache, String src, int fr, int to, String ... conditions);
//	/**
//	 * 清空缓存
//	 * @param cache
//	 * @return
//	 */
//	public boolean clearCache(String cache);
//	
//	public <T> T queryEntity(Class<T> clazz, ConfigStore configs, String ... conditions);
//	public <T> T queryEntity(Class<T> clazz, String ... conditions);
//	/**
//	 * 是否存在
//	 * @param src
//	 * @param configs
//	 * @param conditions
//	 * @return
//	 */
//	public boolean exists(String src, ConfigStore configs, String ... conditions);
//	public boolean exists(String src, String ... conditions);
//	public boolean exists(String src, DataRow row);
//	public boolean exists(DataRow row);
//	
//	public int count(String src, ConfigStore configs, String ... conditions);
//	public int count(String src, String ... conditions);
//	
//	
//	
//	/**
//	 * 更新记录
//	 * @param row		
//	 * 			需要更新的数据
//	 * @param columns	
//	 * 			需要更新的列
//	 * @param dest	
//	 * 			表
//	 * @return
//	 */
//	public int update(String dest, Object data, String ... columns);
//	public int update(Object data, String ... columns);
//	
//	public int update(boolean sync, String dest, Object data, String ... columns);
//	public int update(boolean sync, Object data, String ... columns);
//	/**
//	 * 保存(insert|update)
//	 * @param data
//	 * @param checkPriamry
//	 * @param columns
//	 * @param dest 表
//	 * @return
//	 */
//	public int save(String dest, Object data, boolean checkPriamry, String ... columns);
//	public int save(Object data, boolean checkPriamry, String ... columns);
//	public int save(String dest, Object data, String ... columns);
//	public int save(Object data, String ... columns);
////
//	public int save(boolean sync, String dest, Object data, boolean checkPriamry, String ... columns);
//	public int save(boolean sync, Object data, boolean checkPriamry, String ... columns);
//	public int save(boolean sync, String dest, Object data, String ... columns);
//	public int save(boolean sync, Object data, String ... columns);
//
//
//	public int insert(String dest, Object data, boolean checkPriamry, String ... columns);
//	public int insert(Object data, boolean checkPriamry, String ... columns);
//	public int insert(String dest, Object data, String ... columns);
//	public int insert(Object data, String ... columns);
//
//
//	public int batchInsert(String dest, Object data, boolean checkPriamry, String ... columns);
//	public int batchInsert(Object data, boolean checkPriamry, String ... columns);
//	public int batchInsert(String dest, Object data, String ... columns);
//	public int batchInsert(Object data, String ... columns);
//	/**
//	 * save insert区别
//	 * 操作单个对象时没有区别
//	 * 在操作集合时区别:
//	 * save会循环操作数据库每次都会判断insert|update
//	 * save 集合中的数据可以是不同的表不同的结构 
//	 * insert 集合中的数据必须保存到相同的表,结构必须相同
//	 * insert 将一次性插入多条数据整个过程有可能只操作一次数据库  并 不考虑update情况 对于大批量数据来说 性能是主要优势
//	 * 
//	 */
//	
//	/**
//	 * 执行
//	 * @param src
//	 * @param configs
//	 * @param conditions
//	 * @return
//	 */
//	public int execute(String src, ConfigStore configs, String ... conditions);
//	public int execute(String src, String ... conditions);
//	/**
//	 * 执行存储过程
//	 * @param procedure
//	 * @param inputs
//	 * @param outputs
//	 * @return
//	 */
//	public List<Object> executeProcedure(String procedure, String... inputs);
//	public List<Object> executeProcedure(Procedure procedure);
//	/**
//	 * 根据存储过程查询
//	 * @param procedure
//	 * @param inputs
//	 * @return
//	 */
//	public DataSet queryProcedure(String procedure, String ... inputs);
//	public DataSet queryProcedure(Procedure procedure);
//	
//
//	public int delete(String dest, Object data, String ... columns);
//	public int delete(Object data,  String ... columns);
}