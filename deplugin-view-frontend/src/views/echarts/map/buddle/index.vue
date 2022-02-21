<template>
  <div style="display: flex;position:relative" class="chart-class">
    <view-track-bar
      ref="viewTrack"
      :track-menu="trackMenu"
      class="track-bar"
      :style="trackBarStyleTime"
      @trackClick="trackClick"
    />
    <div :id="chartId" style="width: 100%;height: 100%;overflow: hidden;" :style="{ borderRadius: borderRadius}" />
    <div class="map-zoom-box">
      <div style="margin-bottom: 0.5em;">
        <el-button size="mini" icon="el-icon-plus" circle @click="roamMap(true)" />
      </div>

      <div style="margin-bottom: 0.5em;">
        <el-button size="mini" icon="el-icon-refresh" circle @click="resetZoom()" />
      </div>

      <div>
        <el-button size="mini" icon="el-icon-minus" circle @click="roamMap(false)" />
      </div>

    </div>
  </div>
</template>

<script>



import {BASE_MAP, baseMapOption, uuid} from '@/utils/map'
import ViewTrackBar from '@/components/views/ViewTrackBar'
export default {
  name: 'ChartComponent',
  components: {
    ViewTrackBar
  },
  props: {
    obj: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      myChart: {},
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
      mapCenter: null
    }
  },

  computed: {
    trackBarStyleTime() {
      return this.trackBarStyle
    },
    chart() {
        return this.obj.chart
    },
    filter() {
        return this.obj.filter || {}
    },
    trackMenu() {
        return this.obj.trackMenu || ['drill']
    },
    searchCount() {
        return this.obj.searchCount || 0
    },
    terminalType() {
        return this.obj.terminalType || 'pc'
    }
  },
  watch: {
    chart: {
      handler(newVal, oldVla) {
        this.preDraw()
      },
      deep: true
    },
    resize() {
      this.drawEcharts()
    }
  },
  mounted() {
    this.preDraw()
  },
  destroyed() {
    this.myChart.dispose()
  },
  methods: {
    executeAxios (url, type, data, callBack) {
        const param = {
            url: url,
            type: type,
            data: data,
            callBack: callBack
        }
        this.$emit('execute-axios', param)
        if (process.env.NODE_ENV === 'development') {
            execute(param).then(res => {
                if (param.callBack) {
                    param.callBack(res)
                }
                }).catch(e => {
                if (param.error) {
                    param.error(e)
                }
            })
        }
    },
    preDraw() {     
      const that = this
      new Promise((resolve) => {
        resolve()
      }).then(() => {
        //	此dom为echarts图标展示dom
        this.myChart = this.$echarts.getInstanceByDom(document.getElementById(this.chartId))
        if (!this.myChart) {
          this.myChart = this.$echarts.init(document.getElementById(this.chartId))
        }
        this.drawEcharts()

        this.myChart.off('click')
        this.myChart.on('click', function(param) {
          that.pointParam = param
          if (that.trackMenu.length < 2) { // 只有一个事件直接调用
            that.trackClick(that.trackMenu[0])
          } else { // 视图关联多个事件
            that.trackBarStyle.left = param.event.offsetX + 'px'
            that.trackBarStyle.top = (param.event.offsetY - 15) + 'px'
            that.$refs.viewTrack.trackButtonClick()
          }
        })
      })
    },
    drawEcharts() {
      const chart = this.chart
      let chart_option = {}
     
      if (this.myChart && this.searchCount > 0) {
        chart_option.animation = false
      }

      const customAttr = JSON.parse(chart.customAttr)
        if (!customAttr.areaCode) {
          this.myChart.clear()
          return
        }
        const cCode = this.dynamicAreaCode || customAttr.areaCode
       
        const geoMap = !!localStorage.getItem('geoMap') ? JSON.parse(localStorage.getItem('geoMap')) : {}
        if (geoMap[cCode]) {
            this.initMapChart(geoMap[cCode], chart)
            return
        }
        const url = '/geo/' + cCode + '_full.json'
        this.executeAxios(url, 'get', null, res => {
            if (res && Object.keys(res).length > 0) {
                geoMap[cCode] = res
                localStorage.setItem("geoMap", JSON.stringify(geoMap))
                this.initMapChart(res, chart)
            }
            
        })

        
    },

    registerDynamicMap(areaCode) {
      this.dynamicAreaCode = areaCode
    },
    

    initMapChart(geoJson, chart) {
      this.$echarts.registerMap('BUDDLE_MAP', geoJson)
      const base_json = JSON.parse(JSON.stringify(BASE_MAP))
      let mapData = {}
      if ( !geoJson || !geoJson.features ||  !geoJson.features.length === 0) {
          return
      }
      geoJson.features.map(function(item){
          mapData[item.properties.name] = item.properties.centroid || item.properties.center           
      })
      const chart_option = baseMapOption(base_json, chart, mapData, this.terminalType)
      this.myEcharts(chart_option)
      const opt = this.myChart.getOption()
      if (opt && opt.series) {
        const center = opt.series[0].center
        this.mapCenter = center
      }
    },
    myEcharts(option) {
      // 指定图表的配置项和数据
      const chart = this.myChart
      this.setBackGroundBorder()
      setTimeout(chart.setOption(option, true), 500)
      window.onresize = function() {
        chart.resize()
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
      // 指定图表的配置项和数据
      const chart = this.myChart
      chart.resize()
      this.reDrawMap()
    },
    reDrawMap() {
      const chart = this.chart
      this.preDraw()
    },
    trackClick(trackAction) {
     this.pointParam.viewId = this.chart.id
      const param = this.pointParam
      if (!param || !param.data || !param.data.dimensionList) {
        // 地图提示没有关联字段 其他没有维度信息的 直接返回
         this.$warning(this.$t('panel.no_drill_field'))
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
            this.$emit('plugin-call-back', {
                eventName: 'plugin-chart-click',
                eventParam: this.pointParam
            })
          
          break
        case 'linkage':
          // this.$store.commit('addViewTrackFilter', linkageParam)
          this.$emit('plugin-call-back', {
                eventName: 'plugin-add-view-track-filter',
                eventParam: linkageParam
            })
          break
        case 'jump':
            this.$emit('plugin-call-back', {
                eventName: 'plugin-jump-click',
                eventParam: jumpParam
            })
          break
        default:
          break
      }
    },
    roamMap(flag) {
      let targetZoom = 1
      const zoom = this.myChart.getOption().geo[0].zoom
      if (flag) {
        targetZoom = zoom * 1.2
      } else {
        targetZoom = zoom / 1.2
      }
      const options = JSON.parse(JSON.stringify(this.myChart.getOption()))
      options.geo[0].zoom = targetZoom
      this.myChart.setOption(options)
    },
    resetZoom() {
      const options = JSON.parse(JSON.stringify(this.myChart.getOption()))
      options.geo[0].zoom = 1
      options.geo[0].center = this.mapCenter
      this.myChart.setOption(options)
    }
  }
}

</script>

<style scoped>
  .map-zoom-box {
    position: absolute;
    z-index: 999;
    left: 2%;
    bottom: 30px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    text-align: center;
    padding: 2px;
    border-radius: 5px
  }
  .chart-class {
    height: 100%;
    padding: 10px;
  }

</style>
