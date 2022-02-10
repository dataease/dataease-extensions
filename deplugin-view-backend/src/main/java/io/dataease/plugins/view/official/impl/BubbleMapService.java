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
        pluginViewType.setName("气泡地图");
        pluginViewType.setRender("echarts");
        return pluginViewType;
    }

    @Override
    public Object format(Object o) {
        return null;
    }

    @Override
    public List<String> components() {
        List<String> results = new ArrayList<>();
        results.add("BuddleView");
        results.add("BuddleData");
        results.add("BuddleType");
        return results;
    }

    @Override
    protected InputStream readContent(String s) {
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("static/" + s);
        return resourceAsStream;
    }
}
