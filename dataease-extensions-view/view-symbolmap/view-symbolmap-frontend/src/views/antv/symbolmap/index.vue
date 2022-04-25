<template>
  <div ref="chartContainer" style="padding: 0;width: 100%;height: 100%;overflow: hidden;" :style="bg_class">
    <view-track-bar ref="viewTrack" :track-menu="trackMenu" class="track-bar" :style="trackBarStyleTime" @trackClick="trackClick" />
    <span v-if="chart.type && antVRenderStatus" v-show="title_show" ref="title" :style="titleClass" style="cursor: default;display: block;">
      <p style="padding:6px 10px 0 10px;margin: 0;overflow: hidden;white-space: pre;text-overflow: ellipsis;">{{ chart.title }}</p>
    </span>
    <div :id="chartId" style="width: 100%;overflow: hidden;" :style="{height:chartHeight}" />
  </div>
</template>

<script>
  import { Scene, PointLayer, Popup } from '@antv/l7';
  import { Mapbox } from '@antv/l7-maps';


  import { uuid, hexColorToRGBA} from '@/utils/symbolmap'
  import ViewTrackBar from '@/components/views/ViewTrackBar'


  export default {
    name: 'ChartComponentG2',
    components: { ViewTrackBar },
    props: {
      chart: {
        type: Object,
        required: true
      },
      filter: {
        type: Object,
        required: false,
        default: function() {
          return {}
        }
      },
      trackMenu: {
        type: Array,
        required: false,
        default: function() {
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
          top: '0px'
        },
        pointParam: null,
        dynamicAreaCode: null,
        borderRadius: '0px',
        chartHeight: '100%',
        titleClass: {
          margin: '0 0',
          width: '100%',
          fontSize: '18px',
          color: '#303133',
          textAlign: 'left',
          fontStyle: 'normal',
          fontWeight: 'normal',
          background: hexColorToRGBA('#ffffff', 0)
        },
        title_show: true,
        antVRenderStatus: false
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
      }
    },
    watch: {
      chart: {
        handler(newVal, oldVla) {
          this.initTitle()
          this.calcHeightDelay()
          new Promise((resolve) => { resolve() }).then(() => {
            this.drawView()
          })
        },
        deep: true
      }
    },
    created() {
      debugger
      if (!this.$scene) {
        this.$scene = Scene
        this.$pointLayer = PointLayer
        this.$mapbox = Mapbox
        this.$popup = Popup
      }
    },
    mounted() {
      this.preDraw()
    },
    destroyed() {
      this.myChart.destroy()
    },
    methods: {
      preDraw() {
        this.initMap(() => {
          this.initTitle()
          this.calcHeightDelay()
          new Promise((resolve) => { resolve() }).then(() => {
            this.drawView()
          })
          const that = this
          window.onresize = function() {
            that.calcHeightDelay()
          }
        })

      },
      initMap(callBack) {

        if (!this.myChart) {
          this.myChart = new this.$scene({
            id: this.chartId,
            map: new this.$mapbox({
              pitch: 0,
              style: 'light',
              center: [ 116.276227, 35.256776 ],
              logoVisible: false,
              zoom: 6
            })

          })
          callBack && callBack()
        }
      },
      addGlobalImage() {
        this.myChart.addImage('marker','/api/pluginCommon/staticInfo/map-marker/svg')
      },
      setLayerAttr (layer, chart) {
        const colors = []
        let customAttr = {}
        if (chart.customAttr) {
          customAttr = JSON.parse(chart.customAttr)
          if (customAttr.color) {
            const c = JSON.parse(JSON.stringify(customAttr.color))
            c.colors.forEach(ele => {
              colors.push(hexColorToRGBA(ele, c.alpha))
            })
            if (customAttr.singeColor) {
              layer.color(colors[0])
            }else {
              layer.color('value', colors)
            }

          }
          if (customAttr.tooltip) {
            const t = JSON.parse(JSON.stringify(customAttr.tooltip))
            layer.on('mousemove', event => {
              const popup = new this.$popup({
                offsets: [ 0, 0 ]
              }).setText('hello');
              popup.setLnglat(event.lngLat)
              this.myChart.addPopup(popup)
              // popup.open()
            })

            //tooltipColor = t.textStyle.color
            //tooltipFontsize = t.textStyle.fontSize

          }

          layer.size('value', [10, 25])
        }
        return layer


      },
      drawView() {
        const chart = this.chart

        this.antVRenderStatus = true
        if (!chart.data || (!chart.data.datas && !chart.data.series)) {
          chart.data = {
            datas: [{}],
            series: [
              {
                data: [0]
              }
            ]
          }
        }
        debugger

        this.myChart.on('loaded', () => {
          debugger
          this.addGlobalImage()


          const data = chart.data.datas
          const pointLayer = new this.$pointLayer({autoFit: true})
          pointLayer.source(data, {
            parser: {
              type: 'json',
              x: 'longitude',
              y: 'latitude'
            }
          }).shape('marker')
            .active(true)
          this.setLayerAttr(pointLayer, chart )

          this.myChart.addLayer(pointLayer);
        })


        if (this.myChart && this.searchCount > 0) {
          this.myChart.options.animation = false
        }

        if (this.antVRenderStatus) {
          this.myChart.render()
        }
        this.setBackGroundBorder()
      },

      antVAction(param) {
        this.pointParam = param.data
        if (this.trackMenu.length < 2) { // 只有一个事件直接调用
          this.trackClick(this.trackMenu[0])
        } else { // 视图关联多个事件
          this.trackBarStyle.left = param.x + 'px'
          this.trackBarStyle.top = (param.y + 10) + 'px'
          this.$refs.viewTrack.trackButtonClick()
        }
      },
      setBackGroundBorder() {
        if (this.chart.customStyle) {
          const customStyle = JSON.parse(this.chart.customStyle)
          if (customStyle.background) {
            this.borderRadius = (customStyle.background.borderRadius || 0) + 'px'
          }
        }
      },
      chartResize() {
        this.calcHeightDelay()
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

      initTitle() {
        if (this.chart.customStyle) {
          const customStyle = JSON.parse(this.chart.customStyle)
          if (customStyle.text) {
            this.title_show = customStyle.text.show
            this.titleClass.fontSize = customStyle.text.fontSize + 'px'
            this.titleClass.color = customStyle.text.color
            this.titleClass.textAlign = customStyle.text.hPosition
            this.titleClass.fontStyle = customStyle.text.isItalic ? 'italic' : 'normal'
            this.titleClass.fontWeight = customStyle.text.isBolder ? 'bold' : 'normal'
          }
          if (customStyle.background) {
            this.titleClass.background = hexColorToRGBA(customStyle.background.color, customStyle.background.alpha)
            this.borderRadius = (customStyle.background.borderRadius || 0) + 'px'
          }
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
      }
    }
  }
</script>

<style scoped lang="scss">

</style>
