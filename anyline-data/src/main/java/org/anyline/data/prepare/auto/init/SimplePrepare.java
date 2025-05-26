/*
 * Copyright 2006-2025 www.anyline.org
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

package org.anyline.data.prepare.auto.init;

import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.run.Run;
import org.anyline.data.run.SimpleRun;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.DataRow;
import org.anyline.entity.OriginRow;

public class SimplePrepare extends DefaultAutoPrepare implements RunPrepare {
	@Override
	public Run build(DataRuntime runtime) {
		SimpleRun run = new SimpleRun(runtime);
		run.setPrepare(this);
		return run;
	}


	@Override
	public DataRow map(boolean empty, boolean join) {
		DataRow row = new OriginRow();
		row.put("text", text);
		return row;
	}
}
