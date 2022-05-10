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


        <el-collapse-item name="tooltip" :title="$t('chart.tooltip')" >
          <tooltip-selector-ant-v
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

        <el-collapse-item  name="background" :title="$t('chart.background')">
          <base-map-style-selector
            :param="param"
            class="attr-selector"
            :chart="chart"
            @onChangeBaseMapForm="onChangeBaseMapForm"
          />
        </el-collapse-item>
      </el-collapse>
    </el-row>
  </div>
</template>

<script>
  import ColorSelector from '@/components/selector/ColorSelector'
  import TitleSelector from '@/components/selector/TitleSelector'
  import TooltipSelectorAntV from '@/components/selector/TooltipSelectorAntV'
  import BaseMapStyleSelector from '@/components/selector/BaseMapStyleSelector'
  export default {
    components: {
      ColorSelector,
      TitleSelector,
      TooltipSelectorAntV,
      BaseMapStyleSelector
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
        debugger
        this.view.customAttr.tooltip = val
        this.calcStyle()
      },
      onTextChange(val) {
        this.view.customStyle.text = val
        this.view.title = val.title
        this.calcStyle()
      },
      onChangeBaseMapForm(val) {
        this.view.customStyle.baseMapStyle = val
        this.calcStyle()
      },
      onChangeBackgroundForm(val) {
        this.view.customStyle.background = val
        this.calcStyle()
      },
      onLegendChange(val) {
        this.view.customStyle.legend = val
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
  .padding-lr {
    padding: 0 6px;
  }
  span {
    font-size: 12px;
  }
  .el-radio {
    margin: 5px;
  }
  .radio-span > > > .el-radio__label {
    margin-left: 2px;
  }
</style>
