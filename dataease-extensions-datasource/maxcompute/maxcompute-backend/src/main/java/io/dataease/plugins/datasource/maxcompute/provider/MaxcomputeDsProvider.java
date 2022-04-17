package io.dataease.plugins.datasource.maxcompute.provider;

import com.google.gson.Gson;
import io.dataease.plugins.common.dto.datasource.TableDesc;
import io.dataease.plugins.common.dto.datasource.TableField;
import io.dataease.plugins.common.exception.DataEaseException;
import io.dataease.plugins.common.request.datasource.DatasourceRequest;
import io.dataease.plugins.datasource.provider.DefaultJdbcProvider;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


@Component()
public class MaxcomputeDsProvider extends DefaultJdbcProvider {

    @Override
    public String getType() {
        return "maxcompute";
    }

    @Override
    public boolean isUseDatasourcePool() {
        return false;
    }

    @Override
    public Connection getConnection(DatasourceRequest datasourceRequest) throws Exception {
        Properties props = new Properties();
        MaxcomputeConfig maxcomputeConfig = new Gson().fromJson(datasourceRequest.getDatasource().getConfiguration(), MaxcomputeConfig.class);
        String username = maxcomputeConfig.getAccess_id();
        String password = maxcomputeConfig.getAccess_key();
        String driver = maxcomputeConfig.getDriver();
        String url = maxcomputeConfig.getJdbc();
        Driver driverClass = (Driver) extendedJdbcClassLoader.loadClass(driver).newInstance();

        if (StringUtils.isNotBlank(username)) {
            props.setProperty("user", username);
            if (StringUtils.isNotBlank(password)) {
                props.setProperty("password", password);
            }
        }
        Connection conn = driverClass.connect(url, props);
        return conn;
    }

    @Override
    public List<TableDesc> getTables(DatasourceRequest datasourceRequest) throws Exception {
        List<TableDesc> tables = new ArrayList<>();
        String queryStr = getTablesSql(datasourceRequest);
        try (Connection con = getConnectionFromPool(datasourceRequest); Statement statement = con.createStatement(); ResultSet resultSet = statement.executeQuery(queryStr)) {
            while (resultSet.next()) {
                tables.add(getTableDesc(datasourceRequest, resultSet));
            }
        } catch (Exception e) {
            DataEaseException.throwException(e);
        }

        return tables;
    }

    private TableDesc getTableDesc(DatasourceRequest datasourceRequest, ResultSet resultSet) throws SQLException {
        TableDesc tableDesc = new TableDesc();
        tableDesc.setName(resultSet.getString(1));
        return tableDesc;
    }

    @Override
    public List<TableField> getTableFileds(DatasourceRequest datasourceRequest) throws Exception {
        datasourceRequest.setQuery("select * from " + datasourceRequest.getTable() + " limit 0");
        return fetchResultField(datasourceRequest);
    }

}
