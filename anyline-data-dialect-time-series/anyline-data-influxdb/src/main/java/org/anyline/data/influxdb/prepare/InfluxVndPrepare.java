package org.anyline.data.influxdb.prepare;

import org.anyline.data.influxdb.run.InfluxVndRun;
import org.anyline.data.run.Run;
import org.anyline.data.runtime.DataRuntime;

public class InfluxVndPrepare extends InfluxRunPrepare {
    public Run build(DataRuntime runtime) {
        InfluxVndRun run = new InfluxVndRun(runtime);
        run.api("/api/v2/query");
        run.body(this.text);
        return run;
    }

}
