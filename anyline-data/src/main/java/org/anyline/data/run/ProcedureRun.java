/*
 * Copyright 2006-2023 www.anyline.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



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
