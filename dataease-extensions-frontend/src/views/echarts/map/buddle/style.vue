<template>
    <div
        style="overflow:auto;border-right: 1px solid #e6e6e6;height: 100%;width: 100%;"
        class="attr-style theme-border-class"
    >
        <el-row class="padding-lr">
            <span class="title-text">{{ $t('chart.style_priority') }}</span>
            <el-row>
                <el-radio-group
                v-model="view.stylePriority"
                class="radio-span"                
                size="mini"
                @change="calcStyle"
                >
                <el-radio label="view"><span>{{ $t('chart.chart') }}</span></el-radio>
                <el-radio label="panel"><span>{{ $t('chart.dashboard') }}</span></el-radio>
                </el-radio-group>
            </el-row>
        </el-row>

        <el-row>
            <span class="padding-lr">{{ $t('chart.shape_attr') }}</span>
            <el-collapse v-model="attrActiveNames" class="style-collapse">
                <el-collapse-item name="color" :title="$t('chart.color')">
                    <color-selector :param="param" class="attr-selector" :chart="chart" @onColorChange="onColorChange" />
                </el-collapse-item>
            
            
                <el-collapse-item name="label" :title="$t('chart.label')">
                    <label-selector
                        :param="param"
                        class="attr-selector"
                        :chart="chart"
                        @onLabelChange="onLabelChange"
                    />
                </el-collapse-item>


                <el-collapse-item name="tooltip" :title="$t('chart.tooltip')" >
                    <tooltip-selector
                        :param="param"
                        class="attr-selector"
                        :chart="chart"
                        @onTooltipChange="onTooltipChange"
                    />
                </el-collapse-item>
            </el-collapse>
        </el-row>

        <el-row>
            <span class="padding-lr">{{ $t('chart.module_style') }}</span>
            <el-collapse v-model="styleActiveNames" class="style-collapse">
            
                <el-collapse-item v-show="view.type" name="title" :title="$t('chart.title')">
                    <title-selector
                        
                        :param="param"
                        class="attr-selector"
                        :chart="chart"
                        @onTextChange="onTextChange"
                    />
                </el-collapse-item>
                
                <el-collapse-item name="background" :title="$t('chart.background')">
                <background-color-selector
                    :param="param"
                    class="attr-selector"
                    :chart="chart"
                    @onChangeBackgroundForm="onChangeBackgroundForm"
                />
                </el-collapse-item>
            </el-collapse>
        </el-row>
    </div>
</template>

<script>
import ColorSelector from '@/components/selector/ColorSelector'
import LabelSelector from '@/components/selector/LabelSelector'
import TitleSelector from '@/components/selector/TitleSelector'
import TooltipSelector from '@/components/selector/TooltipSelector'
import BackgroundColorSelector from '@/components/selector/BackgroundColorSelector'
export default {
    components: {
        ColorSelector,
        LabelSelector,
        TitleSelector,
        TooltipSelector,
        BackgroundColorSelector
    },
    data() {
        return {
            attrActiveNames: [],
            styleActiveNames: [],
        }
    },
    props: {
       
        obj: {
            type: Object,
            default: () => {}
        }
    },

    computed: {
        param() {
            return this.obj.param
        },
        view() {
            return this.obj.view
        },
        chart() {
            return this.obj.chart
        }
    },
    methods: {
        onColorChange(val) {
            this.view.customAttr.color = val
            this.calcStyle()
        },
        onLabelChange(val) {
            this.view.customAttr.label = val
            this.calcStyle()
        },

        onTooltipChange(val) {
            this.view.customAttr.tooltip = val
            this.calcStyle()
        },
        onTextChange(val) {
            this.view.customStyle.text = val
            this.view.title = val.title
            this.calcStyle()
        },
        onChangeBackgroundForm(val) {
            this.view.customStyle.background = val
            this.calcStyle()
        },
        calcStyle() {
            this.$emit('plugin-call-back', {
                eventName: 'plugins-calc-style',
                eventParam: this.view
            })
        },
    }
}
</script>

<style lang="scss" scoped>
span {
  font-size: 12px;
}
</style>