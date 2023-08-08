<template>
  <div ref="chartContainer"
       style="padding: 0; width: 100%; height: 100%; overflow: hidden; display: flex; flex-direction: column;"
       :style="bg_class">
    <view-track-bar ref="viewTrack" :track-menu="trackMenu" class="track-bar" :style="trackBarStyleTime"
                    @trackClick="trackClick"/>

    <span
      v-if="chart.type && antVRenderStatus"
      v-show="title_show"
      ref="title"
      :style="title_class"
      style="cursor: default;display: block;"
    >
          <div style="padding:4px 4px 0;margin: 0;">
            <chart-title-update
              :title-class="title_class"
              :chart-info="chartInfo"
              :bus="bus"
              :axios-request="axiosRequest"
            />
            <title-remark
              v-if="remarkCfg.show"
              style="text-shadow: none!important;margin-left: 4px;"
              :remark-cfg="remarkCfg"
            />
          </div>
        </span>
    <div :id="chartId" style="width: 100%; overflow: hidden; flex: 1"/>

  </div>
</template>

<script>
import {Mix} from '@antv/g2plot'
import {uuid, hexColorToRGBA} from '@/utils/racebar'
import ViewTrackBar from '@/components/views/ViewTrackBar'
import {getRemark} from "@/components/views/utils";
import {DEFAULT_TITLE_STYLE} from '@/utils/map';
import ChartTitleUpdate from '@/components/views/ChartTitleUpdate';
import _ from 'lodash';
import {clear} from 'size-sensor'
import {valueFormatter} from '@/utils/formatter'

export default {
  name: 'ChartComponent',
  components: {ViewTrackBar, ChartTitleUpdate},
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
    },
    bus: {
      type: Object,
      required: false,
      default: null
    },
    axiosRequest: {
      type: Function | Object,
      required: false,
      default: null
    },
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
      linkageActiveParam: null,
      linkageActiveHistory: false,
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
    },
    titleHeight() {
      if (this.$refs.title) {
        return this.$refs.title.offsetHeight + 'px';
      } else {
        return '0px';
      }
    },
    chartStyle() {
      return {height: `calc(100% - ${this.titleHeight})`};
    }
  },
  watch: {
    chart: {
      handler(newVal, oldVla) {
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
    !this.$racebar && (this.$racebar = Mix)
  },
  mounted() {
    this.preDraw()
  },
  destroyed() {
    this.myChart.destroy()
  },
  methods: {
    preDraw() {
      const _params = this.getParam();

      this.myChart = new this.$racebar(this.chartId, _params)

      this.myChart.off('edge:click')
      this.myChart.on('edge:click', this.antVAction)

      this.myChart.render();

      this.initTitle();

      this.myChart.update(_params);

    },

    antVAction(param) {
      switch (this.chart.type) {
        case 'treemap':
          this.pointParam = param.data.data
          break
        case 'word-cloud':
          this.pointParam = {
            data: param.data.data.datum
          }
          break
        default:
          this.pointParam = param.data
          break
      }
      this.linkageActiveParam = {
        category: this.pointParam.data.category ? this.pointParam.data.category : 'NO_DATA',
        name: this.pointParam.data.name ? this.pointParam.data.name : 'NO_DATA'
      }
      if (this.trackMenu.length < 2) { // 只有一个事件直接调用
        this.trackClick(this.trackMenu[0])
      } else { // 视图关联多个事件
        this.trackBarStyle.left = param.x + 'px'
        this.trackBarStyle.top = (param.y + 10) + 'px'
        this.$refs.viewTrack.trackButtonClick()
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

        if (customStyle.text) {
          this.title_show = customStyle.text.show
          this.title_class.fontSize = customStyle.text.fontSize + 'px'
          this.title_class.color = customStyle.text.color
          this.title_class.textAlign = customStyle.text.hPosition
          this.title_class.fontStyle = customStyle.text.isItalic ? 'italic' : 'normal'
          this.title_class.fontWeight = customStyle.text.isBolder ? 'bold' : 'normal'

          this.title_class.fontFamily = customStyle.text.fontFamily ? customStyle.text.fontFamily : DEFAULT_TITLE_STYLE.fontFamily
          this.title_class.letterSpacing = (customStyle.text.letterSpace ? customStyle.text.letterSpace : DEFAULT_TITLE_STYLE.letterSpace) + 'px'
          this.title_class.textShadow = customStyle.text.fontShadow ? '2px 2px 4px' : 'none'
        }
        if (customStyle.background) {
          this.title_class.background = hexColorToRGBA(customStyle.background.color, customStyle.background.alpha)
          this.borderRadius = (customStyle.background.borderRadius || 0) + 'px'
        }

      }
      this.initRemark()
    },
    initRemark() {
      this.remarkCfg = getRemark(this.chart)
    },

    getChartType(type) {
      if (type === 'line') {
        return 'line';
      } else if (type === 'scatter') {
        return 'scatter';
      } else {
        return 'column';
      }
    },

    getParam() {
      let yaxisList = this.chart.yaxis ? JSON.parse(this.chart.yaxis) : [];
      let yaxisExtList = this.chart.yaxisExt ? JSON.parse(this.chart.yaxisExt) : [];

      let yaxisCount = yaxisList.length;

      let customAttr = undefined;
      let colors = undefined;
      let labelSetting = undefined;
      if (this.chart.customAttr) {
        customAttr = JSON.parse(this.chart.customAttr);
        if (customAttr) {
          if (customAttr.color) {
            colors = customAttr.color.colors;
          }
          if (customAttr.label) {
            labelSetting = customAttr.label.show ? {
              callback: function (x) {
                return {
                  style: {
                    fill: customAttr.label.color,
                    fontSize: parseInt(customAttr.label.fontSize),
                  },
                };
              },
            } : false
          }
        }
      }

      let _data = this.chart.data && this.chart.data.data && this.chart.data.data.length > 0 ? _.map(_.filter(this.chart.data.data, (c, _index) => {
        return _index < yaxisCount;
      }), (t, _index) => {

        const _labelSetting = _.cloneDeep(labelSetting);
        if (_labelSetting && yaxisList[_index].formatterCfg) {
          _labelSetting.formatter = function (x) {
            return valueFormatter(x.value, yaxisList[_index].formatterCfg);
          }
        }

        return {
          type: this.getChartType(yaxisList[_index].chartType),
          name: t.name,
          options: {
            data: _.map(t.data, (v) => {
              return {
                key: _.join(_.map(v.dimensionList, (d) => d.value), "\n"),
                value: v.value,
                i: _index,
                t: 'yaxis'
              }
            }),
            xField: 'key',
            yField: 'value',
            meta: {
              key: {
                sync: true,
              },
              value: {
                alias: t.name,
              },
            },
            yAxis: {
              position: 'left',
            },
            color: colors && _index < colors.length ? colors[_index] : undefined,
            label: _labelSetting,
          }
        }
      }) : [];

      let _dataExt = this.chart.data && this.chart.data.data && this.chart.data.data.length > 0 ? _.map(_.filter(this.chart.data.data, (c, _index) => {
        return _index >= yaxisCount;
      }), (t, _index) => {

        const _labelSetting = _.cloneDeep(labelSetting);
        if (_labelSetting && yaxisExtList[_index].formatterCfg) {
          _labelSetting.formatter = function (x) {
            return valueFormatter(x.value, yaxisExtList[_index].formatterCfg);
          }
        }

        return {
          type: this.getChartType(yaxisExtList[_index].chartType),
          name: t.name,
          options: {
            data: _.map(t.data, (v) => {
              return {
                key: _.join(_.map(v.dimensionList, (d) => d.value), "\n"),
                value: v.value,
                i: _index,
                t: 'yaxisExt'
              }
            }),
            xField: 'key',
            yField: 'value',
            meta: {
              key: {
                sync: true,
              },
              value: {
                alias: t.name,
              },
            },
            yAxis: {
              position: 'right',
            },
            color: colors && (yaxisCount + _index) < colors.length ? colors[yaxisCount + _index] : undefined,
            label: _labelSetting,
          }
        }
      }) : [];


      const params = {
        tooltip: false,
        syncViewPadding: true,
        plots: [
          ..._data,
          ..._dataExt
        ]
      };


      if (customAttr) {

        if (customAttr.tooltip) {
          params.tooltip = customAttr.tooltip.show ? {
            showTitle: false,
            showMarkers: false,
            shared: true,
            // 内置：node 不显示 tooltip，edge 显示 tooltip
            showContent: function (items) {
              //return !Object(_antv_util__WEBPACK_IMPORTED_MODULE_1__["get"])(items, [0, 'data', 'isNode']);
              return customAttr.tooltip.show && items.length > 0 && items[0].value !== undefined;
            },
            domStyles: {
              'g2-tooltip': {
                fontSize: customAttr.tooltip.textStyle.fontSize + 'px',
                color: customAttr.tooltip.textStyle.color,
                backgroundColor: customAttr.tooltip.backgroundColor,
              }
            },
            customItems: (originalItems) => {
              // process originalItems,
              originalItems.forEach(item => {
                if (item.data.t === 'yaxis' && item.data.i !== undefined && item.data.i < yaxisList.length && yaxisList[item.data.i].formatterCfg) {
                  item.value = valueFormatter(item.data.value, yaxisList[item.data.i].formatterCfg)
                } else if (item.data.t === 'yaxisExt' && item.data.i !== undefined && item.data.i < yaxisExtList.length && yaxisExtList[item.data.i].formatterCfg) {
                  item.value = valueFormatter(item.data.value, yaxisExtList[item.data.i].formatterCfg)
                }
              })

              return originalItems;
            }
          } : false;
        }

      }

      return params;
    },

    updateViewData() {

      const param = this.getParam();

      this.myChart.update(param);

    },

    reDrawView() {
      this.linkageActiveHistory = false
      this.myChart.render()
    },
    linkageActivePre() {
      if (this.linkageActiveHistory) {
        this.reDrawView()
      }
      this.$nextTick(() => {
        this.linkageActive()
      })
    },
    linkageActive() {
      this.linkageActiveHistory = true
      this.myChart.setState('active', (param) => {
        if (Array.isArray(param)) {
          return false
        } else {
          if (this.checkSelected(param)) {
            return true
          }
        }
      })
      this.myChart.setState('inactive', (param) => {
        if (Array.isArray(param)) {
          return false
        } else {
          if (!this.checkSelected(param)) {
            return true
          }
        }
      })
    },
    checkSelected(param) {
      return (this.linkageActiveParam.name === param.name || (this.linkageActiveParam.name === 'NO_DATA' && !param.name)) &&
        (this.linkageActiveParam.category === param.category)
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
      const quotaList = this.pointParam.data.quotaList
      quotaList[0]['value'] = this.pointParam.data.value
      const linkageParam = {
        option: 'linkage',
        name: this.pointParam.data.name,
        viewId: this.chart.id,
        dimensionList: this.pointParam.data.dimensionList,
        quotaList: quotaList
      }
      const jumpParam = {
        option: 'jump',
        name: this.pointParam.data.name,
        viewId: this.chart.id,
        dimensionList: this.pointParam.data.dimensionList,
        quotaList: quotaList
      }
      switch (trackAction) {
        case 'drill':
          this.$emit('onChartClick', this.pointParam)
          break
        case 'linkage':
          this.linkageActivePre()
          this.$store.commit('addViewTrackFilter', linkageParam)
          break
        case 'jump':
          this.$emit('onJumpClick', jumpParam)
          break
        default:
          break
      }
    },

    beforeDestroy() {
      if (this.myChart.container) {
        if (typeof this.myChart.container.getAttribute === 'function') {
          clear(this.myChart.container)
        }
      }
      if (this.myChart) {
        if (this.myChart.clear) {
          this.myChart.clear()
        }
        if (this.myChart.unbindSizeSensor) {
          this.myChart.unbindSizeSensor()
        }
        if (this.myChart.unbind) {
          this.myChart.unbind()
        }
        if (this.myChart.destroy) {
          this.myChart.destroy()
        }
      }
      if (this.myChart) {
        for (const key in this.myChart.chart) {
          this.myChart.chart[key] = null
          this.$delete(this.myChart.chart, key)
        }
        for (const key in this.myChart) {
          this.myChart[key] = null
          this.$delete(this.myChart, key)
        }
      }
      for (const key in this.pointParam) {
        this.$delete(this.pointParam, key)
      }
      this.myChart = null
    },


  }
}
</script>

<style scoped lang="scss">

.track-bar > > > ul {
  width: 80px !important;
}
</style>
