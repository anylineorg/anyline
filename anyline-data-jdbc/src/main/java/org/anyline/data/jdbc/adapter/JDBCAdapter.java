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



package org.anyline.data.jdbc.adapter;

import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.metadata.Metadata;

import javax.sql.DataSource;
import java.sql.Connection;

public interface JDBCAdapter extends DriverAdapter {

    <T extends Metadata> void checkSchema(DataRuntime runtime, DataSource datasource, T meta);
    <T extends Metadata> void checkSchema(DataRuntime runtime, Connection con, T meta);
}
