import org.anyline.util.BasicUtil;


public class SQLLog{
	public SQLLog(){}
	private String key;
	private String txt;
	private String params;
	private int exeTime;
	private int packTime;
	private int rows;
	public String getTxt() {
		return txt;
	}
	public void setTxt(String txt) {
		this.txt = txt;
	}
	public String getParams() {
		return params;
	}
	public void setParams(String params) {
		this.params = params;
	}
	public int getExeTime() {
		return exeTime;
	}
	public void setExeTime(String exeTime) {
		this.exeTime = BasicUtil.parseInt(exeTime,-1);
	}
	public int getPackTime() {
		return packTime;
	}
	public void setPackTime(String packTime) {
		this.packTime = BasicUtil.parseInt(packTime,-1);
	}
	public int getRows() {
		return rows;
	}
	public void setRows(String rows) {
		this.rows = BasicUtil.parseInt(rows, -1);
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	
}