package org.anyline.demo.springmvc.home.controller;

import org.anyline.entity.DataSet;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller("home.defaultController")
@RequestMapping("/")
public class DefaultController extends BasicController{
	@RequestMapping("index")
	public ModelAndView index(){
		ModelAndView mv = new ModelAndView("home/page/index.jsp");
		DataSet set = service.query("members", parseConfig(true));
		mv.addObject("set",set);
		return mv;
	}
}
