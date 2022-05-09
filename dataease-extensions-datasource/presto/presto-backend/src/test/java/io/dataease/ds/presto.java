package io.dataease.ds;

import com.google.gson.Gson;
import io.dataease.plugins.common.base.domain.Datasource;
import io.dataease.plugins.common.dto.datasource.TableDesc;
import io.dataease.plugins.common.request.datasource.DatasourceRequest;
import io.dataease.plugins.datasource.maxcompute.provider.PrestoConfig;
import io.dataease.plugins.datasource.maxcompute.provider.PrestoDsProvider;
import org.junit.Test;

import java.sql.Driver;

public class presto {



    @Test
    public void testPresto()throws Exception{
        PrestoDsProvider prestoDsProvider = new PrestoDsProvider();
        prestoDsProvider.init();
        PrestoConfig prestoConfig = new PrestoConfig();
        prestoConfig.setHost("123.56.8.132");
        prestoConfig.setPort(8080);
        prestoConfig.setUsername("root");
        prestoConfig.setDataBase("mysql");
        prestoConfig.setSchema("dataease");

        Datasource datasource = new Datasource();
        datasource.setType("presto");
        datasource.setName("presto");
        datasource.setId("presto");
        datasource.setConfiguration(new Gson().toJson(prestoConfig));

        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(datasource);

        prestoDsProvider.checkStatus(datasourceRequest);
        for (TableDesc table : prestoDsProvider.getTables(datasourceRequest)) {
            System.out.println(new Gson().toJson(table));
        }
    }
}
