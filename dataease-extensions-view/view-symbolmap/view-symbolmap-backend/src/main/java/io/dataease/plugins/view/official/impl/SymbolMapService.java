package io.dataease.plugins.view.official.impl;

import io.dataease.plugins.common.dto.StaticResource;

import io.dataease.plugins.view.entity.*;
import io.dataease.plugins.view.official.handler.SymbolMapRSHandler;
import io.dataease.plugins.view.official.handler.SymbolMapStatHandler;
import io.dataease.plugins.view.service.ViewPluginService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.InputStream;
import java.util.*;

@Service
public class SymbolMapService extends ViewPluginService {

    @Resource
    private SymbolMapStatHandler symbolMapStatHandler;

    @Resource
    private SymbolMapRSHandler symbolMapRSHandler;

    private static final String VIEW_TYPE_VALUE = "symbol-map";
    /* 下版这些常量移到sdk */
    private static final String TYPE = "-type";
    private static final String DATA = "-data";
    private static final String STYLE = "-style";
    private static final String VIEW = "-view";
    private static final String SUFFIX = "svg";
    /* 下版这些常量移到sdk */
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

    private static List<String> trans2Ykeys = new ArrayList<String>();

    @PostConstruct
    public void init() {
        trans2Ykeys.add("labelAxis");
        trans2Ykeys.add("tooltipAxis");
    }

    @Override
    public String generateSQL(PluginViewParam pluginViewParam) {
        pluginViewParam.getPluginViewFields().forEach(field -> {
            if (trans2Ykeys.contains(field.getTypeField())) {
                field.setTypeField("yAxis");
            }
        });
        List<PluginViewField> xAxis = pluginViewParam.getFieldsByType("xAxis");
        List<PluginViewField> yAxis = pluginViewParam.getFieldsByType("yAxis");
        if (CollectionUtils.isEmpty(xAxis) || xAxis.size() < 2) {
            return null;
        }

        /*
         * List<PluginViewField> labelAxis =
         * pluginViewParam.getFieldsByType("labelAxis");
         * List<PluginViewField> tooltipAxis =
         * pluginViewParam.getFieldsByType("tooltipAxis");
         * 
         * 
         * yAxis = (null == yAxis) ? new ArrayList<PluginViewField>() : yAxis;
         * 
         * Boolean yAxisChange = false;
         * if (CollectionUtils.isNotEmpty(labelAxis)) {
         * yAxis.addAll(labelAxis);
         * yAxisChange = true;
         * }
         * if (CollectionUtils.isNotEmpty(tooltipAxis)) {
         * yAxis.addAll(tooltipAxis);
         * yAxisChange = true;
         * }
         * if (yAxisChange) {
         * yAxis.forEach(item -> {
         * item.setTypeField("yAxis");
         * });
         * addFields2Param(pluginViewParam, yAxis, "yAxis");
         * }
         */

        if (CollectionUtils.isNotEmpty(yAxis))
            return super.generateSQL(pluginViewParam);
        // 下面考虑符号大小为空的情况
        return symbolMapStatHandler.build(pluginViewParam, this);

    }

    /*
     * private void addFields2Param(PluginViewParam pluginViewParam,
     * List<PluginViewField> axis, String type) {
     * List<PluginViewField> pluginViewFields =
     * pluginViewParam.getPluginViewFields();
     * 
     * List<PluginViewField> resultFields = pluginViewFields.stream()
     * .filter(item -> StringUtils.equals(item.getTypeField(),
     * type)).collect(Collectors.toList());
     * resultFields.addAll(axis);
     * pluginViewParam.setPluginViewFields(resultFields);
     * }
     */

    @Override
    public Map<String, Object> formatResult(PluginViewParam pluginViewParam, List<String[]> data, Boolean isDrill) {
        return symbolMapRSHandler.format(pluginViewParam, data, isDrill);
    }
}
