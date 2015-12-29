
package org.anyline.tag;

import java.util.Collection;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.apache.log4j.Logger;
import org.anyline.config.db.PageNavi;
import org.anyline.entity.DataSet;
import org.anyline.util.BasicUtil;


public class FillTable extends BaseBodyTag{
	private static final long serialVersionUID = 1L;
	private static Logger log = Logger.getLogger(FillTable.class);
	private int column;
	private int count = -1; 		//总行数
	private Object data;


	public int getCount() {
		return count;
	}


	public void setCount(int count) {
		this.count = count;
	}


	public Object getData() {
		return data;
	}


	public void setData(Object data) {
		this.data = data;
	}


	public int getColumn() {
		return column;
	}


	public void setColumn(int column) {
		this.column = column;
	}


	public int doEndTag() throws JspException {
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		
		try{
			Collection<Map> items = (Collection<Map>)data;
			int line_size = 0;
			if(null != items) line_size = items.size();		//现有行数
			if(count == -1 && data instanceof DataSet){
				PageNavi navi = ((DataSet)data).getNavi();
				if(null != navi){
					count = navi.getPageRows();
				}
			}
			int fill_size = count - line_size;  //补充行数
			String html = "";
			for(int i=0; i<fill_size; i++){
				if(BasicUtil.isEmpty(body)){
					html += "<tr>";
					for(int j=0; j<column; j++){
						html += "<td>&nbsp;</td>";
					}
					html += "</tr>\n";
				}else{
					html += body;
				}
			}
			JspWriter out = pageContext.getOut();
			out.print(html);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			release();
		}
        return EVAL_PAGE;   
	}


	@Override
	public void release() {
		super.release();
		column = 0;
		count = -1;
		data = null;
	}

}