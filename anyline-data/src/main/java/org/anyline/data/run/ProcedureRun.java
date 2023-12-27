package org.anyline.data.run;

import org.anyline.data.runtime.DataRuntime;
import org.anyline.metadata.Parameter;
import org.anyline.metadata.Procedure;

import java.sql.Types;

public class ProcedureRun extends BasicRun implements Run{
    private Procedure  procedure = null;
    public ProcedureRun(Procedure procedure){
        this.procedure = procedure;
    }
    public ProcedureRun(DataRuntime runtime){
        this.runtime = runtime;
    }
    public ProcedureRun(){
    }
    public ProcedureRun(Procedure procedure, Object ... inputs){
        this(null, procedure, inputs);
    }
    public ProcedureRun(DataRuntime runtime, Procedure procedure, Object ... inputs){
        this.runtime = runtime;
        if(null != inputs){
            for(Object input:inputs){
                Parameter param = new Parameter();
                param.setType(Types.JAVA_OBJECT);
                param.setValue(input);
                procedure.addInput(param);
            }
        }
        this.procedure = procedure;
    }
    @Override
    public boolean checkValid() {
        return true;
    }

    public Procedure getProcedure() {
        return procedure;
    }

    public void setProcedure(Procedure procedure) {
        this.procedure = procedure;
    }
}
