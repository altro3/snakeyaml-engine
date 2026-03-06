package org.snakeyaml.engine.util;

import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.api.lowlevel.Compose;
import org.snakeyaml.engine.v2.representer.StandardRepresenter;

public class TestUtil {

    public static final LoadSettings DEFAULT_LOAD_SETTINGS = LoadSettings.builder().build();
    public static final Load DEFAULT_LOAD = new Load(DEFAULT_LOAD_SETTINGS);

    public static final DumpSettings DEFAULT_DUMP_SETTINGS = DumpSettings.builder().build();
    public static final Dump DEFAULT_DUMP = new Dump(DEFAULT_DUMP_SETTINGS);

    public static final Compose DEFAULT_COMPOSE = new Compose(DEFAULT_LOAD_SETTINGS);

    public static final StandardRepresenter DEFAULT_REPRESENTER = new StandardRepresenter(DEFAULT_DUMP_SETTINGS);
}
