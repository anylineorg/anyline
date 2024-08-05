package org.anyline.data.chroma;

import org.anyline.annotation.Component;
import org.anyline.data.adapter.init.AbstractDriverAdapter;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.metadata.Column;
import org.anyline.metadata.Metadata;
import org.anyline.metadata.Type;
import org.anyline.metadata.type.DatabaseType;

import java.util.LinkedHashMap;

@Component("anyline.data.adapter.chroma")
public class ChromaAdapter extends AbstractDriverAdapter {
    @Override
    public DatabaseType type() {
        return DatabaseType.Chroma;
    }

    @Override
    public boolean supportCatalog() {
        return false;
    }

    @Override
    public boolean supportSchema() {
        return false;
    }

    @Override
    public String name(Type type) {
        return null;
    }

    @Override
    public <T extends Metadata> void checkSchema(DataRuntime runtime, T meta) {

    }

    @Override
    public LinkedHashMap<String, Column> metadata(DataRuntime runtime, RunPrepare prepare, boolean comment) {
        return null;
    }

    @Override
    public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, boolean create, LinkedHashMap<String, T> columns, Column query) throws Exception {
        return null;
    }

    @Override
    public String concat(DataRuntime runtime, String... args) {
        return null;
    }
}
