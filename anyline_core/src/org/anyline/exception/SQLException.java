package org.anyline.exception;

public class SQLException extends RuntimeException{
	private static final long serialVersionUID = 1L;
	public SQLException(){
		super();
	}
	public SQLException(String title){
		super(title);
	}
}
