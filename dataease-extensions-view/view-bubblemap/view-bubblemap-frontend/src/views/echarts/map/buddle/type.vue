<template>
    <el-radio value="buddle-map" label="buddle-map" border class="radio-style">
        <span title="气泡地图">
        <svg-icon icon-class="buddle-map" class="chart-icon" />
        </span>
        <p class="radio-label">气泡地图</p>
    </el-radio>
</template>

<script>
export default {
    
}
</script>

<style lang="scss" scoped>
.radio-style {
  width: 80px;
  height: 60px;
  padding: 0;
}
.chart-icon{
  width: 80px;
  height: 40px;
}
.radio-label{
  display: block;
  position: absolute;
  bottom: 0;
  width: 100%;
  margin: 0;
  text-align: center;
  height: 20px;
  line-height: 20px;
  text-overflow: ellipsis;
  overflow: hidden;
  white-space: pre;
  font-size: 12px;
}
</style>