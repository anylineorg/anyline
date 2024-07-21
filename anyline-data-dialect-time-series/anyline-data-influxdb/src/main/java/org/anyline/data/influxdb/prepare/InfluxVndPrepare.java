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
