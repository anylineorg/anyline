package org.anyline.data.handler;

public interface StreamHandler {
    default int size(){return Integer.MIN_VALUE;}
    default void handler(ConnectionHandler handler){}
    /**
     * read(ResultSet result)之后 是否保存ResultSet连接状态，如果保持则需要在调用方关闭
     * @return boolean
     */
    default boolean keep(){
        return false;
    }
}
