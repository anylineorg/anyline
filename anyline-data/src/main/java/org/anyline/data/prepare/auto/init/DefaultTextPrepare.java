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



package org.anyline.data.prepare.auto.init;
 
import org.anyline.data.prepare.auto.TextPrepare;
import org.anyline.data.run.Run;
import org.anyline.data.run.TextRun;
import org.anyline.data.runtime.DataRuntime;

public class DefaultTextPrepare extends DefaultAutoPrepare implements TextPrepare {
	private String text; 
	public DefaultTextPrepare(String text) {
		super(); 
		this.text = text; 
		chain = new DefaultAutoConditionChain();
	} 
	public String getText() {
		return this.text; 
	}

	@Override
	public Run build(DataRuntime runtime) {
		TextRun run = new TextRun();
		run.setPrepare(this);
		run.setRuntime(runtime);
		return run;
	}
}
