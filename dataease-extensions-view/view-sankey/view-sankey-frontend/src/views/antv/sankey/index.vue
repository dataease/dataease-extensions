<template>
  <div ref="chartContainer" style="padding: 0;width: 100%;height: 100%;overflow: hidden;" :style="bg_class">
    <view-track-bar ref="viewTrack" :track-menu="trackMenu" class="track-bar" :style="trackBarStyleTime"
                    @trackClick="trackClick"/>
    <div v-if="chart.type && antVRenderStatus" v-show="title_show" ref="title" :style="titleClass"
         style="cursor: default;display: block;">
      <p
        style="padding:6px 4px 0;margin: 0;overflow: hidden;white-space: pre;text-overflow: ellipsis;display: inline;">
        {{ chart.title }}
      </p>
      <title-remark v-if="remarkCfg.show" style="text-shadow: none!important;" :remark-cfg="remarkCfg"/>
    </div>
    <div :id="chartId" style="width: 100%;overflow: hidden;" :style="{height:chartHeight}"/>

  </div>
</template>

<script>
import {Sankey} from '@antv/g2plot'
import {uuid, hexColorToRGBA} from '@/utils/sankey'
import ViewTrackBar from '@/components/views/ViewTrackBar'
import {getRemark} from "@/components/views/utils";
import _ from 'lodash';

export default {
  name: 'ChartComponent',
  components: {ViewTrackBar},
  props: {
    chart: {
      type: Object,
      required: true
    },
    filter: {
      type: Object,
      required: false,
      default: function () {
        return {}
      }
    },
    trackMenu: {
      type: Array,
      required: false,
      default: function () {
        return ['drill']
      }
    },
    searchCount: {
      type: Number,
      required: false,
      default: 0
    },
    scale: {
      type: Number,
      required: false,
      default: 1
    },
    themeStyle: {
      type: Object,
      required: false,
      default: null
    }
  },
  data() {
    return {
      myChart: null,
      chartId: uuid(),
      showTrackBar: true,
      trackBarStyle: {
        position: 'absolute',
        left: '0px',
        top: '0px',
        zIndex: 99
      },
      pointParam: null,
      dynamicAreaCode: null,
      borderRadius: '0px',
      chartHeight: '100%',
      title_show: true,
      antVRenderStatus: false,
      remarkCfg: {
        show: false,
        content: ''
      },
      buttonTextColor: null,
      titleClass: {
        margin: '0 0',
        zIndex: 1,
        width: '100%',
        fontSize: '18px',
        color: '#303133',
        textAlign: 'left',
        fontStyle: 'normal',
        fontWeight: 'normal',
        background: hexColorToRGBA('#ffffff', 0)
      },
      title_class: {
        margin: '0 0',
        width: '100%',
        fontSize: '18px',
        color: '#303133',
        textAlign: 'left',
        fontStyle: 'normal',
        fontWeight: 'normal',
        background: ''
      },
    }
  },

  computed: {
    trackBarStyleTime() {
      return this.trackBarStyle
    },
    bg_class() {
      return {
        borderRadius: this.borderRadius
      }
    },
    chartInfo() {
      const {id, title} = this.chart
      return {id, title}
    }
  },
  watch: {
    chart: {
      handler(newVal, oldVla) {
        this.calcHeightDelay()
        new Promise((resolve) => {
          resolve()
        }).then(() => {
          this.updateViewData()
          this.initTitle();
        })
      },
      deep: true
    }
  },
  created() {
    !this.$sankey && (this.$sankey = Sankey)
  },
  mounted() {
    this.preDraw()
  },
  destroyed() {
    this.myChart.destroy()
  },
  methods: {
    preDraw() {
      this.calcHeightDelay()

      this.myChart = new this.$sankey(this.chartId, this.getParam())

      this.myChart.render();

      this.initTitle();

      const that = this;
      window.onresize = function () {
        that.calcHeightDelay()
      }

    },

    initTitle() {
      this.antVRenderStatus = true;

      if (this.chart.customStyle) {
        const customStyle = JSON.parse(this.chart.customStyle)

        if (customStyle.text) {
          this.title_show = customStyle.text.show
          this.titleClass.fontSize = customStyle.text.fontSize + 'px'
          this.titleClass.color = customStyle.text.color
          this.titleClass.textAlign = customStyle.text.hPosition
          this.titleClass.fontStyle = customStyle.text.isItalic ? 'italic' : 'normal'
          this.titleClass.fontWeight = customStyle.text.isBolder ? 'bold' : 'normal'
          this.titleClass.fontSize = customStyle.text.isBolder ? 'bold' : 'normal'

          this.titleClass.fontFamily = customStyle.text.fontFamily ? customStyle.text.fontFamily : 'Microsoft YaHei'
          this.titleClass.letterSpacing = (customStyle.text.letterSpace ? customStyle.text.letterSpace : '0') + 'px'
          this.titleClass.textShadow = customStyle.text.fontShadow ? '2px 2px 4px' : 'none'
        }
        if (customStyle.background) {
          this.titleClass.background = hexColorToRGBA(customStyle.background.color, customStyle.background.alpha)
          this.borderRadius = (customStyle.background.borderRadius || 0) + 'px'
        }
      }
      this.initRemark()
    },
    initRemark() {
      this.remarkCfg = getRemark(this.chart)
    },

    getParam() {
      let _data = this.chart.data ? _.cloneDeep(this.chart.data.tableRow) : undefined;

      let _source = null, _target = null, _value = null;

      if (_data === undefined || _data === null || this.chart.data.fields.length < 3) {
        _data = [];
      } else {
        _source = this.chart.data.fields[0].dataeaseName
        _target = this.chart.data.fields[1].dataeaseName
        _value = this.chart.data.fields[2].dataeaseName
      }

      const params = {
        data: _data,
        sourceField: _source,
        targetField: _target,
        weightField: _value,
      };

      if (this.chart.customAttr) {
        const customAttr = JSON.parse(this.chart.customAttr);
        console.log(customAttr)
        if (customAttr.color) {
          params.color = customAttr.color.colors;

          //透明度，设置了之后没有hover加深，看不出来选了啥
          /*const alpha = customAttr.color.alpha / 100;
          params.edgeStyle = {
            fillOpacity: alpha,
          }*/
        }

        if (customAttr.label) {
          params.label = {
            formatter: function (_a) {
              let name = _a.name;
              return name;
            },
            callback: function (x) {
              let isLast = x[1] === 1; // 最后一列靠边的节点
              return {
                style: {
                  fill: customAttr.label.show ? customAttr.label.color : 'transparent',
                  fontSize: parseInt(customAttr.label.fontSize),
                  textAlign: isLast ? 'end' : 'start',
                },
                offsetX: isLast ? -8 : 8,
              };
            },
            layout: [
              {
                type: 'hide-overlap',
              },
            ],
          }
        }

        if (customAttr.tooltip) {
          params.tooltip = customAttr.tooltip.show ? {
            showTitle: false,
            showMarkers: false,
            shared: false,
            // 内置：node 不显示 tooltip，edge 显示 tooltip
            // showContent: function (items) {
            //   return !Object(_antv_util__WEBPACK_IMPORTED_MODULE_1__["get"])(items, [0, 'data', 'isNode']);
            // },
            formatter: function (datum) {
              let source = datum.source, target = datum.target, value = datum.value;
              return {
                name: source + ' -> ' + target,
                value: value,
              };
            },
            showContent: customAttr.tooltip.show,
            domStyles: {
              'g2-tooltip': {
                fontSize: customAttr.tooltip.textStyle.fontSize + 'px',
                color: customAttr.tooltip.textStyle.color,
                backgroundColor: customAttr.tooltip.backgroundColor,
              }
            }
          } : false;
        }

      }

      return params;
    },

    updateViewData() {

      const param = this.getParam();

      this.myChart.update(param);

      console.log(this.myChart)

    },


    trackClick(trackAction) {
      const param = this.pointParam
      if (!param || !param.data || !param.data.dimensionList) {
        // 地图提示没有关联字段 其他没有维度信息的 直接返回
        if (this.chart.type === 'map') {
          this.$warning(this.$t('panel.no_drill_field'))
        }
        return
      }
      const linkageParam = {
        option: 'linkage',
        viewId: this.chart.id,
        dimensionList: this.pointParam.data.dimensionList,
        quotaList: this.pointParam.data.quotaList
      }
      const jumpParam = {
        option: 'jump',
        viewId: this.chart.id,
        dimensionList: this.pointParam.data.dimensionList,
        quotaList: this.pointParam.data.quotaList
      }
      switch (trackAction) {
        case 'drill':
          this.$emit('onChartClick', this.pointParam)
          break
        case 'linkage':
          this.$store.commit('addViewTrackFilter', linkageParam)
          break
        case 'jump':
          this.$emit('onJumpClick', jumpParam)
          break
        default:
          break
      }
    },

    calcHeightRightNow() {
      this.$nextTick(() => {
        if (this.$refs.chartContainer) {
          const currentHeight = this.$refs.chartContainer.offsetHeight
          if (this.$refs.title) {
            const titleHeight = this.$refs.title.offsetHeight
            this.chartHeight = (currentHeight - titleHeight) + 'px'
          }
        }
      })
    },
    calcHeightDelay() {
      setTimeout(() => {
        this.calcHeightRightNow()
      }, 100)
    },


  }
}
</script>

<style scoped lang="scss">

.track-bar > > > ul {
  width: 80px !important;
}
</style>
