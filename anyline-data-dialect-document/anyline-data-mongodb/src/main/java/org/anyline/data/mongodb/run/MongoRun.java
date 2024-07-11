package org.anyline.data.mongodb.run;

import org.anyline.data.run.Run;
import org.anyline.data.run.TableRun;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.metadata.Table;
import org.bson.conversions.Bson;

public class MongoRun extends TableRun implements Run {
    private Bson filter;
    private Bson update;
    public MongoRun(DataRuntime runtime, String table) {
        super(runtime, table);
    }
    public MongoRun(DataRuntime runtime, Table table) {
        super(runtime, table);
    }

    public Bson getFilter() {
        return filter;
    }

    public void setFilter(Bson filter) {
        this.filter = filter;
    }

    public Bson getUpdate() {
        return update;
    }

    public void setUpdate(Bson update) {
        this.update = update;
    }
}
