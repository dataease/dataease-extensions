package io.dataease.plugins.datasource.maxcompute.provider;

import io.dataease.plugins.datasource.entity.JdbcConfiguration;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class PrestoConfig extends JdbcConfiguration {

    private String driver = "io.prestosql.jdbc.PrestoDriver";
    private String extraParams;


    public String getJdbc() {
        return "jdbc:presto://HOST:PORT/DATABASE/SCHEMA"
                .replace("HOST", getHost().trim())
                .replace("PORT", getPort().toString())
                .replace("DATABASE", getDataBase().trim())
                .replace("SCHEMA", getSchema().trim());
    }
}
