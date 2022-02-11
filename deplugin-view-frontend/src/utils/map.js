export const BASE_MAP = {
    title: {
      text: '',
      textStyle: {
        fontWeight: 'normal'
      }
    },
  
    tooltip: {},
    geo: {
        show: true,
        map: 'BUDDLE_MAP',
        label: {
            normal: {
                show: false
            },
            emphasis: {
                show: true
            }
        },
        roam: true
    },
    series: [
      {
        name: '',
        type: 'scatter',
        coordinateSystem: 'geo',        
        data: []
      }
    ]
}
const convertData = (mapData, chart) => {
    let maxVal = 0
    const k = terminalType === 'pc' ? 30 : 15
    const names = chart.data.x
    const results = []
    for (let index = 0; index < names.length; index++) {
        const name = names[index];
        results.push({name, value: mapData[name].concat(chart.data.series[0].data[index].value)}) 
        maxVal = Math.max(maxVal, chart.data.series[0].data[index].value)       
    }
    const rate = k / maxVal

    return {
        value: results,
        rate: rate
    }
}

let terminalType = 'pc'
export function baseMapOption(chart_option, chart, mapData, terminal = 'pc') {
    terminalType = terminal
    // 处理shape attr
    let customAttr = {}
    if (chart.customAttr) {
      customAttr = JSON.parse(chart.customAttr)
      if (customAttr.color) {
        chart_option.color = customAttr.color.colors
      }
      // tooltip
      if (customAttr.tooltip) {
        const tooltip = JSON.parse(JSON.stringify(customAttr.tooltip))
        const reg = new RegExp('\n', 'g')
        const text = tooltip.formatter.replace(reg, '<br/>')
        tooltip.formatter = function(params) {
          const a = params.seriesName
          const b = params.name
          const c = params.value ? params.value[2] : ''
          return text.replace(new RegExp('{a}', 'g'), a).replace(new RegExp('{b}', 'g'), b).replace(new RegExp('{c}', 'g'), c)
        }
        chart_option.tooltip = tooltip
      }
    }
    // 处理data
    if (chart.data) {
      chart_option.title.text = chart.title
      if (chart.data.series && chart.data.series.length > 0) {
        chart_option.series[0].name = chart.data.series[0].name
        // label
        if (customAttr.label) {
          const text = customAttr.label.formatter
          chart_option.series[0].label = customAttr.label
          chart_option.series[0].label.formatter = function(params) {
            const a = params.seriesName
            const b = params.name
            const c = params.value ? params.value : ''
            return text.replace(new RegExp('{a}', 'g'), a).replace(new RegExp('{b}', 'g'), b).replace(new RegExp('{c}', 'g'), c)
          }
          chart_option.series[0].labelLine = customAttr.label.labelLine
        }
        
        const convert = convertData(mapData, chart)
        chart_option.series[0].data = convert.value
        chart_option.series[0].symbolSize = val => val[2] * convert.rate
        
      }
    }
    // console.log(chart_option);
    componentStyle(chart_option, chart)
    return chart_option
}
export function componentStyle(chart_option, chart) {
    const padding = '8px'
    if (chart.customStyle) {
      const customStyle = JSON.parse(chart.customStyle)
      if (customStyle.text) {
        chart_option.title.show = customStyle.text.show
        // 水平方向
        if (customStyle.text.hPosition === 'left') {
          chart_option.title.left = padding
        } else if (customStyle.text.hPosition === 'right') {
          chart_option.title.right = padding
        } else {
          chart_option.title.left = customStyle.text.hPosition
        }
        // 垂直方向
        if (customStyle.text.vPosition === 'top') {
          chart_option.title.top = padding
        } else if (customStyle.text.vPosition === 'bottom') {
          chart_option.title.bottom = padding
        } else {
          chart_option.title.top = customStyle.text.vPosition
        }
        const style = chart_option.title.textStyle ? chart_option.title.textStyle : {}
        style.fontSize = customStyle.text.fontSize
        style.color = customStyle.text.color
        customStyle.text.isItalic ? style.fontStyle = 'italic' : style.fontStyle = 'normal'
        customStyle.text.isBolder ? style.fontWeight = 'bold' : style.fontWeight = 'normal'
        chart_option.title.textStyle = style
      }
      if (customStyle.legend && chart_option.legend) {
        chart_option.legend.show = customStyle.legend.show
        // 水平方向
        if (customStyle.legend.hPosition === 'left') {
          chart_option.legend.left = padding
        } else if (customStyle.legend.hPosition === 'right') {
          chart_option.legend.right = padding
        } else {
          chart_option.legend.left = customStyle.legend.hPosition
        }
        // 垂直方向
        if (customStyle.legend.vPosition === 'top') {
          chart_option.legend.top = padding
        } else if (customStyle.legend.vPosition === 'bottom') {
          chart_option.legend.bottom = padding
        } else {
          chart_option.legend.top = customStyle.legend.vPosition
        }
        chart_option.legend.orient = customStyle.legend.orient
        chart_option.legend.icon = customStyle.legend.icon
        chart_option.legend.textStyle = customStyle.legend.textStyle
        if (chart.type === 'treemap' || chart.type === 'gauge') {
          chart_option.legend.show = false
        }
      }
      if (customStyle.xAxis && (chart.type.includes('bar') || chart.type.includes('line') || chart.type.includes('scatter') || chart.type === 'chart-mix')) {
        chart_option.xAxis.show = customStyle.xAxis.show
        chart_option.xAxis.position = customStyle.xAxis.position
        chart_option.xAxis.name = customStyle.xAxis.name
        chart_option.xAxis.axisLabel = customStyle.xAxis.axisLabel
        chart_option.xAxis.splitLine = customStyle.xAxis.splitLine
        chart_option.xAxis.nameTextStyle = customStyle.xAxis.nameTextStyle
  
        chart_option.xAxis.axisLabel.showMaxLabel = true
        chart_option.xAxis.axisLabel.showMinLabel = true
  
        if (!customStyle.xAxis.show) {
          chart_option.xAxis.axisLabel.show = false
        }
  
        // 轴值设置
        delete chart_option.xAxis.min
        delete chart_option.xAxis.max
        delete chart_option.xAxis.split
        if (chart.type.includes('horizontal')) {
          if (customStyle.xAxis.axisValue && !customStyle.xAxis.axisValue.auto) {
            customStyle.xAxis.axisValue.min && (chart_option.xAxis.min = parseFloat(customStyle.xAxis.axisValue.min))
            customStyle.xAxis.axisValue.max && (chart_option.xAxis.max = parseFloat(customStyle.xAxis.axisValue.max))
            customStyle.xAxis.axisValue.split && (chart_option.xAxis.interval = parseFloat(customStyle.xAxis.axisValue.split))
          }
        }
      }
      if (customStyle.yAxis && (chart.type.includes('bar') || chart.type.includes('line') || chart.type.includes('scatter'))) {
        chart_option.yAxis.show = customStyle.yAxis.show
        chart_option.yAxis.position = customStyle.yAxis.position
        chart_option.yAxis.name = customStyle.yAxis.name
        chart_option.yAxis.axisLabel = customStyle.yAxis.axisLabel
        chart_option.yAxis.splitLine = customStyle.yAxis.splitLine
        chart_option.yAxis.nameTextStyle = customStyle.yAxis.nameTextStyle
  
        chart_option.yAxis.axisLabel.showMaxLabel = true
        chart_option.yAxis.axisLabel.showMinLabel = true
  
        if (!customStyle.yAxis.show) {
          chart_option.yAxis.axisLabel.show = false
        }
  
        // 轴值设置
        delete chart_option.yAxis.min
        delete chart_option.yAxis.max
        delete chart_option.yAxis.split
        if (!chart.type.includes('horizontal')) {
          if (customStyle.yAxis.axisValue && !customStyle.yAxis.axisValue.auto) {
            customStyle.yAxis.axisValue.min && (chart_option.yAxis.min = parseFloat(customStyle.yAxis.axisValue.min))
            customStyle.yAxis.axisValue.max && (chart_option.yAxis.max = parseFloat(customStyle.yAxis.axisValue.max))
            customStyle.yAxis.axisValue.split && (chart_option.yAxis.interval = parseFloat(customStyle.yAxis.axisValue.split))
          }
        }
      }
      if (customStyle.yAxis && chart.type === 'chart-mix') {
        chart_option.yAxis[0].show = customStyle.yAxis.show
        chart_option.yAxis[0].position = customStyle.yAxis.position
        chart_option.yAxis[0].name = customStyle.yAxis.name
        chart_option.yAxis[0].axisLabel = customStyle.yAxis.axisLabel
        chart_option.yAxis[0].splitLine = customStyle.yAxis.splitLine
        chart_option.yAxis[0].nameTextStyle = customStyle.yAxis.nameTextStyle
  
        chart_option.yAxis[0].axisLabel.showMaxLabel = true
        chart_option.yAxis[0].axisLabel.showMinLabel = true
  
        if (!customStyle.yAxis.show) {
          chart_option.yAxis[0].axisLabel.show = false
        }
  
        // 轴值设置
        delete chart_option.yAxis[0].min
        delete chart_option.yAxis[0].max
        delete chart_option.yAxis[0].split
        if (!chart.type.includes('horizontal')) {
          if (customStyle.yAxis.axisValue && !customStyle.yAxis.axisValue.auto) {
            customStyle.yAxis.axisValue.min && (chart_option.yAxis[0].min = parseFloat(customStyle.yAxis.axisValue.min))
            customStyle.yAxis.axisValue.max && (chart_option.yAxis[0].max = parseFloat(customStyle.yAxis.axisValue.max))
            customStyle.yAxis.axisValue.split && (chart_option.yAxis[0].interval = parseFloat(customStyle.yAxis.axisValue.split))
          }
        }
  
        // axis ext
        !customStyle.yAxisExt && (customStyle.yAxisExt = JSON.parse(JSON.stringify(DEFAULT_YAXIS_EXT_STYLE)))
        chart_option.yAxis[1].show = customStyle.yAxisExt.show
        chart_option.yAxis[1].position = customStyle.yAxisExt.position
        chart_option.yAxis[1].name = customStyle.yAxisExt.name
        chart_option.yAxis[1].axisLabel = customStyle.yAxisExt.axisLabel
        chart_option.yAxis[1].splitLine = customStyle.yAxisExt.splitLine
        chart_option.yAxis[1].nameTextStyle = customStyle.yAxisExt.nameTextStyle
  
        chart_option.yAxis[1].axisLabel.showMaxLabel = true
        chart_option.yAxis[1].axisLabel.showMinLabel = true
  
        if (!customStyle.yAxisExt.show) {
          chart_option.yAxis[1].axisLabel.show = false
        }
  
        // 轴值设置
        delete chart_option.yAxis[1].min
        delete chart_option.yAxis[1].max
        delete chart_option.yAxis[1].split
        if (!chart.type.includes('horizontal')) {
          if (customStyle.yAxisExt.axisValue && !customStyle.yAxisExt.axisValue.auto) {
            customStyle.yAxisExt.axisValue.min && (chart_option.yAxis[1].min = parseFloat(customStyle.yAxisExt.axisValue.min))
            customStyle.yAxisExt.axisValue.max && (chart_option.yAxis[1].max = parseFloat(customStyle.yAxisExt.axisValue.max))
            customStyle.yAxisExt.axisValue.split && (chart_option.yAxis[1].interval = parseFloat(customStyle.yAxisExt.axisValue.split))
          }
        }
      }
      if (customStyle.split && chart.type.includes('radar')) {
        chart_option.radar.name = customStyle.split.name
        chart_option.radar.splitNumber = customStyle.split.splitNumber
        chart_option.radar.axisLine = customStyle.split.axisLine
        chart_option.radar.axisTick = customStyle.split.axisTick
        chart_option.radar.axisLabel = customStyle.split.axisLabel
        chart_option.radar.splitLine = customStyle.split.splitLine
        chart_option.radar.splitArea = customStyle.split.splitArea
      }
      if (customStyle.background) {
        chart_option.backgroundColor = hexColorToRGBA(customStyle.background.color, customStyle.background.alpha)
      }
    }
}
export function hexColorToRGBA(hex, alpha) {
    const rgb = [] // 定义rgb数组
    if (/^\#[0-9A-F]{3}$/i.test(hex)) { // 判断传入是否为#三位十六进制数
      let sixHex = '#'
      hex.replace(/[0-9A-F]/ig, function(kw) {
        sixHex += kw + kw // 把三位16进制数转化为六位
      })
      hex = sixHex // 保存回hex
    }
    if (/^#[0-9A-F]{6}$/i.test(hex)) { // 判断传入是否为#六位十六进制数
      hex.replace(/[0-9A-F]{2}/ig, function(kw) {
        // eslint-disable-next-line no-eval
        rgb.push(eval('0x' + kw)) // 十六进制转化为十进制并存如数组
      })
      return `rgba(${rgb.join(',')},${alpha / 100})` // 输出RGB格式颜色
    } else {
      return 'rgb(0,0,0)'
    }
}
  


export const DEFAULT_YAXIS_EXT_STYLE = {
    show: true,
    position: 'right',
    name: '',
    nameTextStyle: {
      color: '#333333',
      fontSize: 12
    },
    axisLabel: {
      show: true,
      color: '#333333',
      fontSize: '12',
      rotate: 0,
      formatter: '{value}'
    },
    splitLine: {
      show: true,
      lineStyle: {
        color: '#cccccc',
        width: 1,
        style: 'solid'
      }
    },
    axisValue: {
      auto: true,
      min: null,
      max: null,
      split: null,
      splitCount: null
    }
}

export function uuid() {
    return (((1+Math.random())*0x10000)|0).toString(16).substring(1);
}
  
