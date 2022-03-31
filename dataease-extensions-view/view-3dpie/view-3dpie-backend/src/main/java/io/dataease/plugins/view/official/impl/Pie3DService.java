package io.dataease.plugins.view.official.impl;

import io.dataease.plugins.common.dto.StaticResource;
import io.dataease.plugins.view.entity.PluginViewType;
import io.dataease.plugins.view.service.ViewPluginService;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class Pie3DService extends ViewPluginService {
    @Override
    public PluginViewType viewType() {
        PluginViewType pluginViewType = new PluginViewType();
        pluginViewType.setRender("highcharts");
        pluginViewType.setCategory("chart.chart_type_space");
        pluginViewType.setValue("3d-pie");
        return pluginViewType;
    }

    @Override
    public Object format(Object o) {
        return null;
    }

    @Override
    public List<String> components() {
        List<String> results = new ArrayList<>();
        results.add("3d-pie-view");
        results.add("3d-pie-data");
        results.add("3d-pie-type");
        results.add("3d-pie-style");
        return results;
    }

    @Override
    public List<StaticResource> staticResources() {
        List<StaticResource> results = new ArrayList<>();
        StaticResource staticResource = new StaticResource();
        staticResource.setName("3d-pie");
        staticResource.setSuffix("svg");
        results.add(staticResource);
        return results;
    }

    @Override
    protected InputStream readContent(String s) {
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("static/" + s);
        return resourceAsStream;
    }
}
