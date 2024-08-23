package org.anyline.data.prepare.auto.init;

import org.anyline.data.prepare.RunPrepare;

public class VirtualTablePrepare extends DefaultTablePrepare{
    protected RunPrepare prepare;
    public VirtualTablePrepare(RunPrepare prepare){
        this.prepare = prepare;
    }

    public RunPrepare getPrepare() {
        return prepare;
    }

    public void setPrepare(RunPrepare prepare) {
        this.prepare = prepare;
    }
}
