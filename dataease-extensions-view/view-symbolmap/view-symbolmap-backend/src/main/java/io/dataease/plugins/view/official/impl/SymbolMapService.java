package io.dataease.plugins.view.official.impl;

import io.dataease.plugins.common.constants.SQLConstants;
import io.dataease.plugins.common.dto.StaticResource;
import io.dataease.plugins.common.dto.chart.AxisChartDataAntVDTO;
import io.dataease.plugins.common.dto.chart.ChartDimensionDTO;
import io.dataease.plugins.common.dto.chart.ChartQuotaDTO;
import io.dataease.plugins.common.util.ConstantsUtil;
import io.dataease.plugins.view.entity.*;
import io.dataease.plugins.view.official.dto.SymbolMapResultDTO;
import io.dataease.plugins.view.service.ViewPluginBaseService;
import io.dataease.plugins.view.service.ViewPluginService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SymbolMapService extends ViewPluginService {

    private static final String VIEW_TYPE_VALUE = "symbol-map";
    /*下版这些常量移到sdk*/
    private static final String TYPE = "-type";
    private static final String DATA = "-data";
    private static final String STYLE = "-style";
    private static final String VIEW = "-view";
    private static final String SUFFIX = "svg";
    /*下版这些常量移到sdk*/
    private static final String VIEW_TYPE = VIEW_TYPE_VALUE + TYPE;
    private static final String VIEW_DATA = VIEW_TYPE_VALUE + DATA;
    private static final String VIEW_STYLE = VIEW_TYPE_VALUE + STYLE;
    private static final String VIEW_VIEW = VIEW_TYPE_VALUE + VIEW;

    @Override
    public PluginViewType viewType() {
        PluginViewType pluginViewType = new PluginViewType();
        pluginViewType.setRender("antv");
        pluginViewType.setCategory("chart.chart_type_space");
        pluginViewType.setValue(VIEW_TYPE_VALUE);
        return pluginViewType;
    }

    @Override
    public Object format(Object o) {
        return null;
    }

    @Override
    public List<String> components() {
        List<String> results = new ArrayList<>();
        results.add(VIEW_VIEW);
        results.add(VIEW_DATA);
        results.add(VIEW_TYPE);
        results.add(VIEW_STYLE);
        return results;
    }

    @Override
    public List<StaticResource> staticResources() {
        List<StaticResource> results = new ArrayList<>();
        StaticResource staticResource = new StaticResource();
        staticResource.setName(VIEW_TYPE_VALUE);
        staticResource.setSuffix(SUFFIX);
        results.add(staticResource);
        addImage(results);
        return results;
    }

    private void addImage(List<StaticResource> results) {
        StaticResource staticResource = new StaticResource();
        staticResource.setName("map-marker");
        staticResource.setSuffix(SUFFIX);
        results.add(staticResource);
    }

    @Override
    protected InputStream readContent(String s) {
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("static/" + s);
        return resourceAsStream;
    }

    @Override
    public String generateSQL(PluginViewParam pluginViewParam) {
        List<PluginViewField> xAxis = pluginViewParam.getFieldsByType("xAxis");
        List<PluginViewField> yAxis = pluginViewParam.getFieldsByType("yAxis");
        List<PluginViewField> xAxisExt = pluginViewParam.getFieldsByType("xAxisExt");
        List<PluginViewField> bubble = pluginViewParam.getFieldsByType("extBubble");



        if (CollectionUtils.isEmpty(xAxis) || CollectionUtils.isEmpty(yAxis) || CollectionUtils.isEmpty(bubble)){
            return null;
        }

        yAxis.stream().forEach(field -> {
            field.setSummary(null);
        });



        ViewPluginBaseService baseService = this.getBaseService();
        PluginViewSet pluginViewSet = pluginViewParam.getPluginViewSet();
        String dsType = pluginViewSet.getDsType();
        PluginViewSQL tableObj = baseService.getTableObj(pluginViewSet);

        Map<String, List<PluginSingleField>> fieldSQLMap = new HashMap<>();



        for (int i = 0; i < pluginViewParam.getPluginViewFields().size(); i++) {
            PluginViewField pluginViewField = pluginViewParam.getPluginViewFields().get(i);
            String typeKey = pluginViewField.getTypeField();
            if (StringUtils.equals(typeKey, "yAxis")) {
                pluginViewField.setTypeField("xAxis");
            }

            PluginSingleField pluginSingleField = baseService.buildField(dsType, pluginViewField, tableObj, i);
            List<PluginSingleField> lists = fieldSQLMap.getOrDefault(typeKey, new ArrayList<>());
            lists.add(pluginSingleField);
            fieldSQLMap.put(typeKey, lists);
        }


        List<PluginViewSQL> xFieldsExt = new ArrayList<>();
        List<PluginViewSQL> xFields = fieldSQLMap.get("xAxis").stream().filter(singleField -> ObjectUtils.isNotEmpty(singleField.getField())).map(singleField -> singleField.getField()).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(xAxisExt)) {
            xFieldsExt= fieldSQLMap.get("xAxisExt").stream().filter(singleField -> ObjectUtils.isNotEmpty(singleField.getField())).map(singleField -> singleField.getField()).collect(Collectors.toList());
        }

        List<PluginViewSQL> yFields = fieldSQLMap.get("yAxis").stream().filter(singleField -> ObjectUtils.isNotEmpty(singleField.getField())).map(singleField -> singleField.getField()).collect(Collectors.toList());
        List<PluginViewSQL> extBubble = fieldSQLMap.get("extBubble").stream().filter(singleField -> ObjectUtils.isNotEmpty(singleField.getField())).map(singleField -> singleField.getField()).collect(Collectors.toList());


        // 处理视图中字段过滤
        String customWheres = baseService.customWhere(dsType, pluginViewParam.getPluginChartFieldCustomFilters(), tableObj);
        // 处理仪表板字段过滤
        String panelWheres = baseService.panelWhere(dsType, pluginViewParam.getPluginChartExtFilters(), tableObj);
        // 构建sql所有参数

        List<String> wheres = new ArrayList<>();
        if (customWheres != null) wheres.add(customWheres);
        if (panelWheres != null) wheres.add(panelWheres);
        List<PluginViewSQL> groups = new ArrayList<>();
        groups.addAll(xFieldsExt);
        // 外层再次套sql


        STGroup stg = new STGroupFile(SQLConstants.SQL_TEMPLATE);
        ST st_sql = stg.getInstanceOf("querySql");
        List<PluginViewSQL> fields = new ArrayList<>();
        fields.addAll(xFields);
        fields.addAll(yFields);

        //if (CollectionUtils.isNotEmpty(extBubble)) fields.addAll(extBubble);
        //if (CollectionUtils.isNotEmpty(xFieldsExt)) fields.addAll(xFieldsExt);
        if (CollectionUtils.isNotEmpty(xFieldsExt)) fields.addAll(xFieldsExt);
        st_sql.add("groups", fields);
        /*if (CollectionUtils.isNotEmpty(xFieldsExt)) st_sql.add("groups", xFieldsExt);*/
        if (CollectionUtils.isNotEmpty(extBubble)) st_sql.add("aggregators", extBubble);
        if (CollectionUtils.isNotEmpty(wheres)) st_sql.add("filters", wheres);
        if (ObjectUtils.isNotEmpty(tableObj)) st_sql.add("table", tableObj);
        String sql = st_sql.render();

        String brackets = ConstantsUtil.constantsValue(dsType, "BRACKETS");
        String table_alias_prefix = ConstantsUtil.constantsValue(dsType, "TABLE_ALIAS_PREFIX");

        ST st = stg.getInstanceOf("querySql");
        PluginViewSQL tableSQL = PluginViewSQL.builder()
                .tableName(String.format(brackets, sql))
                .tableAlias(String.format(table_alias_prefix, 1))
                .build();
        //if (CollectionUtils.isNotEmpty(aggWheres)) st.add("filters", aggWheres);
        //if (CollectionUtils.isNotEmpty(orders)) st.add("orders", orders);
        if (ObjectUtils.isNotEmpty(tableSQL)) st.add("table", tableSQL);
        return sqlLimit(st.render(), pluginViewParam.getPluginViewLimit());
    }

    private String sqlLimit(String sql, PluginViewLimit view) {
        return StringUtils.equalsIgnoreCase(view.getResultMode(), "custom") ? sql + " LIMIT 0," + view.getResultCount() : sql;
    }

    @Override
    public Map<String, Object> formatResult(PluginViewParam pluginViewParam, List<String[]> data, Boolean isDrill) {

            List<PluginViewField> xAxis = new ArrayList();
            List<PluginViewField> yAxis = new ArrayList();
            pluginViewParam.getPluginViewFields().forEach((pluginViewField) -> {
                if (StringUtils.equals(pluginViewField.getTypeField(), "xAxis") || StringUtils.equals(pluginViewField.getTypeField(), "yAxis")) {
                    xAxis.add(pluginViewField);
                }

                if (StringUtils.equals(pluginViewField.getTypeField(), "extBubble")) {
                    yAxis.add(pluginViewField);
                }

            });
        Map<String, Object> map = new HashMap<>();

        List<AxisChartDataAntVDTO> datas = new ArrayList<>();
        for (int i1 = 0; i1 < data.size(); i1++) {
            String[] row = data.get(i1);

            StringBuilder a = new StringBuilder();
            if (isDrill) {
                a.append(row[xAxis.size() - 1]);
            } else {
                for (int i = 0; i < xAxis.size(); i++) {
                    if (i == xAxis.size() - 1) {
                        a.append(row[i]);
                    } else {
                        a.append(row[i]).append("\n");
                    }
                }
            }

            for (int i = xAxis.size(); i < xAxis.size() + yAxis.size(); i++) {
                SymbolMapResultDTO axisChartDataDTO = new SymbolMapResultDTO();
                axisChartDataDTO.setField(a.toString());
                axisChartDataDTO.setName(a.toString());

                List<ChartDimensionDTO> dimensionList = new ArrayList<>();
                List<ChartQuotaDTO> quotaList = new ArrayList<>();

                for (int j = 0; j < xAxis.size(); j++) {
                    ChartDimensionDTO chartDimensionDTO = new ChartDimensionDTO();
                    chartDimensionDTO.setId(xAxis.get(j).getId());
                    chartDimensionDTO.setValue(row[j]);
                    dimensionList.add(chartDimensionDTO);
                }
                axisChartDataDTO.setDimensionList(dimensionList);

                int j = i - xAxis.size();
                ChartQuotaDTO chartQuotaDTO = new ChartQuotaDTO();
                chartQuotaDTO.setId(yAxis.get(j).getId());
                quotaList.add(chartQuotaDTO);
                axisChartDataDTO.setQuotaList(quotaList);
                try {
                    axisChartDataDTO.setValue(StringUtils.isEmpty(row[i]) ? null : new BigDecimal(row[i]));
                } catch (Exception e) {
                    axisChartDataDTO.setValue(new BigDecimal(0));
                }
                axisChartDataDTO.setCategory(yAxis.get(j).getName());
                axisChartDataDTO.setLongitude(dimensionList.get(0).getValue());
                axisChartDataDTO.setLatitude(dimensionList.get(1).getValue());
                datas.add(axisChartDataDTO);
            }
        }
        map.put("datas", datas);
        return map;

    }
}
