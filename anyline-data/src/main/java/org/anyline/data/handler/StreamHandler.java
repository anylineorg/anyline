package org.anyline.data.handler;

public interface StreamHandler extends DataHandler {
    /**
     * 每次从ResultSet中读取的行数
     * @return int
     */
    default int size(){return Integer.MIN_VALUE;}

    /**
     * 用于项目中释放连接 keep()返回true时需要在项目中释放连接
     * @param handler ConnectionHandler
     */
    default void handler(ConnectionHandler handler){}
    /**
     * read(ResultSet result)之后 是否保存ResultSet连接状态，如果保持则需要在项目中调用ConnectionHandler释放连接
     * @return boolean
     */
    default boolean keep(){
        return false;
    }
}
