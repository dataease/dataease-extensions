package io.dataease.dm;

import com.google.gson.Gson;
import io.dataease.plugins.common.base.domain.Datasource;
import io.dataease.plugins.common.dto.datasource.TableDesc;
import io.dataease.plugins.common.dto.datasource.TableField;
import io.dataease.plugins.common.request.datasource.DatasourceRequest;
import io.dataease.plugins.datasource.dm.provider.DmConfig;
import io.dataease.plugins.datasource.dm.provider.DmDsProvider;

public class Test {

    @org.junit.Test
    public void testOracle() throws Exception {
        DmDsProvider jdbcProvider = new DmDsProvider();
        jdbcProvider.init();
        DmConfig oracleConfiguration = new DmConfig();
        oracleConfiguration.setUsername("SYSDBA");
        oracleConfiguration.setPassword("Calong2015");
        oracleConfiguration.setHost("123.56.8.132");
        oracleConfiguration.setPort(5236);
        oracleConfiguration.setDataBase("employee");
        oracleConfiguration.setSchema("WEI");
        Datasource datasource = new Datasource();

        datasource.setType("dm");
        datasource.setName("dm");
        datasource.setId("dm");
        datasource.setConfiguration(new Gson().toJson(oracleConfiguration));
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(datasource);
        for (String s : jdbcProvider.getSchema(datasourceRequest)) {
            System.out.println(s);
        }
        for (TableDesc s : jdbcProvider.getTables(datasourceRequest)) {
            System.out.println(new Gson().toJson(s));
        }
//
        datasourceRequest.setTable("employee");
        for (TableField tableFiled : jdbcProvider.getTableFileds(datasourceRequest)) {
            System.out.println(new Gson().toJson(tableFiled));
        }
    }
}
