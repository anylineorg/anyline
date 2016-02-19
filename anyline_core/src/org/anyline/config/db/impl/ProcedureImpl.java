

package org.anyline.config.db.impl;
import java.util.ArrayList;
import java.util.List;


/**
 * V3.0
 */
import org.anyline.config.db.Procedure;



public class ProcedureImpl  implements Procedure{

	private String name;
	private List<Integer> outputTypes;					//输出参数
	private List<String> inputValues;
	private List<Integer> inputTypes;
	
	public ProcedureImpl(String name){
		this();
		this.name = name;
	}
	public ProcedureImpl(){
		inputValues = new ArrayList<String>();
		inputTypes = new ArrayList<Integer>();
		outputTypes = new ArrayList<Integer>();
	}
	/**
	 * 添加输入参数
	 * @param value	值
	 * @param type	类型
	 * @return
	 */
	public Procedure addInput(String value, Integer type){
		inputValues.add(value);
		inputTypes.add(type);
		return this;
	}
	public Procedure addInput(String value){
		return addInput(value, java.sql.Types.VARCHAR);
	}
	
	public List<String> getInputValues(){
		return inputValues;
	}
	public List<Integer> getInputTypes() {
		return inputTypes;
	}
	
	/**
	 * 注册输出参数
	 * @param type	类型
	 * @return
	 */
	public Procedure regOutput(Integer type){
		outputTypes.add(type);
		return this;
	}
	public Procedure regOutput(){
		return regOutput(java.sql.Types.VARCHAR);
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Integer> getOutputTypes() {
		return outputTypes;
	}

}
