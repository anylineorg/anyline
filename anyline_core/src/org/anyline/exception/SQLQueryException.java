package org.anyline.exception;

public class SQLQueryException extends SQLException{
	private static final long serialVersionUID = 1L;
	public SQLQueryException(){
		super();
	}
	public SQLQueryException(String title){
		super(title);
	}
}
