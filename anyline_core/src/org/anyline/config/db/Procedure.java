

package org.anyline.config.db;
/**
 * V3.0
 */
import java.util.List;



public interface Procedure{
	/**
	 * 添加输入参数
	 * @param value	值
	 * @param type	类型
	 * @return
	 */
	public Procedure addInput(String value, Integer type);
	public Procedure addInput(String value);
	
	public List<String> getInputValues();
	public List<Integer> getInputTypes() ;
	
	/**
	 * 注册输出参数
	 * @param type	类型
	 * @return
	 */
	public Procedure regOutput(Integer type);
	public Procedure regOutput();
	public String getName() ;
	public void setName(String name) ;
	public List<Integer> getOutputTypes() ;

}
