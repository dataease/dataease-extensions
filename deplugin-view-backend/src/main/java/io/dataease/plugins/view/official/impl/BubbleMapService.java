package io.dataease.plugins.view.official.impl;

import io.dataease.plugins.view.entity.PluginViewType;
import io.dataease.plugins.view.service.ViewPluginService;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class BubbleMapService extends ViewPluginService {
    @Override
    public PluginViewType viewType() {
        PluginViewType pluginViewType = new PluginViewType();
        pluginViewType.setRender("echarts");
        pluginViewType.setCategory("chart.chart_type_space");
        pluginViewType.setValue("buddle-map");
        return pluginViewType;
    }

    @Override
    public Object format(Object o) {
        return null;
    }

    @Override
    public List<String> components() {
        List<String> results = new ArrayList<>();
        results.add("buddle-map-view");
        results.add("buddle-map-data");
        results.add("buddle-map-type");
        results.add("buddle-map-style");
        return results;
    }

    @Override
    protected InputStream readContent(String s) {
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("static/" + s);
        return resourceAsStream;
    }
}
