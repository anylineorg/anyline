package org.anyline.data.handler;

import org.anyline.data.adapter.DriverWorker;

public interface ConnectionHandler {
    boolean close() throws Exception;
    void setWorker(DriverWorker worker);
}
