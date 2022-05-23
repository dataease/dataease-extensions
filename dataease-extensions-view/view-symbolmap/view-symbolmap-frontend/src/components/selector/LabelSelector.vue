<template>
  <div style="width: 100%;">
    <el-col>
      <el-form ref="labelForm"  :model="labelForm" label-width="80px" size="mini">
        <el-form-item :label="$t('chart.show')" class="form-item">
          <el-checkbox v-model="labelForm.show" @change="changeLabelAttr">{{ $t('chart.show') }}</el-checkbox>
        </el-form-item>
        <div v-show="labelForm.show">
          
          <el-form-item :label="$t('chart.text_fontsize')" class="form-item">
            <el-select v-model="labelForm.fontSize" :placeholder="$t('chart.text_fontsize')" size="mini" @change="changeLabelAttr">
              <el-option v-for="option in fontSize" :key="option.value" :label="option.name" :value="option.value" />
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('chart.text_color')" class="form-item">
            <el-color-picker v-model="labelForm.color" class="color-picker-style" :predefine="predefineColors" @change="changeLabelAttr" />
          </el-form-item>
          <!-- <el-form-item :label="$t('chart.label_position')" class="form-item">
            <el-select v-model="labelForm.position" :placeholder="$t('chart.label_position')" @change="changeLabelAttr">
              <el-option v-for="option in labelPosition" :key="option.value" :label="option.name" :value="option.value" />
            </el-select>
          </el-form-item> -->

          <el-form-item :label="$t('chart.label')" class="form-item">
            <el-select v-model="values" placeholder="请选择" multiple collapse-tags @change="changeFields">
    
                <el-option-group
                    v-for="group in fieldOptions"
                    :key="group.label"
                    :label="group.label"
                >
                    <el-option
                        v-for="item in group.options"
                        :key="item.id"
                        :label="item.name"
                        :value="item.id">
                    </el-option>
                </el-option-group>
            </el-select>
          </el-form-item>
          
          <el-form-item class="form-item">
            <span slot="label">
              <span class="span-box">
                <span>{{ $t('chart.content_formatter') }}</span>
                <el-tooltip class="item" effect="dark" placement="bottom">
                  <div slot="content">
                    可以{fieldName}形式读字段值，标签和提示种字段互相通用
                  </div>
                  <i class="el-icon-info" style="cursor: pointer;" />
                </el-tooltip>
              </span>
            </span>
            <el-input v-model="labelForm.labelTemplate" type="textarea" :autosize="{ minRows: 4, maxRows: 4}" @blur="changeLabelAttr" />
          </el-form-item>
        </div>
      </el-form>
      
    </el-col>
  </div>
</template>

<script>
import { COLOR_PANEL, DEFAULT_LABEL } from '@/utils/map'

export default {
  name: 'LabelSelector',
  props: {
    param: {
      type: Object,
      required: true
    },
    chart: {
      type: Object,
      required: true
    },
    dimensionData: {
      type: Array,
      required: true
    },
    quotaData: {
      type: Array,
      required: true
    },
    view: {
      type: Object,
      required: true
    }
  },
  computed: {
      fieldOptions() {
          return [
            {
              label: this.$t('chart.dimension'),
              options: this.dimensionData
            },
            {
              label: this.$t('chart.quota'),
              options: this.quotaData
            }]            
      },
      labelFields() {
          return this.view.viewFields && this.view.viewFields.filter(field => field.busiType === this.busiType)
      }
  },
  data() {
    return {
      labelForm: JSON.parse(JSON.stringify(DEFAULT_LABEL)),
      fontSize: [],
      isSetting: false,
      labelPosition: [],
      labelPositionPie: [
        { name: this.$t('chart.inside'), value: 'inside' },
        { name: this.$t('chart.outside'), value: 'outside' }
      ],
      predefineColors: COLOR_PANEL,
      values: null,
      busiType: 'labelAxis'
    }
  },
  watch: {
    'chart': {
      handler: function() {
        this.initOptions()
        this.initData()
      }
    }
  },
  mounted() {
    this.init()
    this.initOptions()
    this.initData()
  },
  methods: {
    initData() {
        debugger
      const chart = JSON.parse(JSON.stringify(this.chart))
      if (chart.customAttr) {
        let customAttr = null
        if (Object.prototype.toString.call(chart.customAttr) === '[object Object]') {
          customAttr = JSON.parse(JSON.stringify(chart.customAttr))
        } else {
          customAttr = JSON.parse(chart.customAttr)
        }
        if (customAttr.label) {
          this.labelForm = customAttr.label
          if (this.labelForm.show) {
              const labes = JSON.parse(JSON.stringify(this.labelFields))
              this.values = labes.map(item => item.id)
          }
          if (!this.labelForm.labelLine) {
            this.labelForm.labelLine = JSON.parse(JSON.stringify(DEFAULT_LABEL.labelLine))
          }
        }
      }
    },
    init() {
      const arr = []
      for (let i = 10; i <= 40; i = i + 2) {
        arr.push({
          name: i + '',
          value: i + ''
        })
      }
      this.fontSize = arr
    },
    changeLabelAttr() {
      if (!this.labelForm.show) {
        this.isSetting = false
      }
      this.$emit('onLabelChange',this.labelForm)
    },

    clearBusiTypeFields() {
        this.view.viewFields = this.view.viewFields.filter(field => field.busiType !== this.busiType)
    },
    changeFields(vals) {
        this.clearBusiTypeFields()
        const allFields = [...JSON.parse(JSON.stringify(this.dimensionData)), ... JSON.parse(JSON.stringify(this.quotaData))]
        allFields.forEach(field => {
            if (vals.includes(field.id)) {
                const item = Object.assign(JSON.parse(JSON.stringify(field)), {busiType: this.busiType})
                item.summary = 'group_concat'
                this.view.viewFields.push(item)
            }
        })
               
        this.$emit('onRefreshViewFields',this.view.viewFields)
        
    },
    initOptions() {
      const type = this.chart.type
      if (type) {
        this.labelPosition = this.labelPositionPie
      }
    }
  }
}
</script>

<style scoped>
  .shape-item{
    padding: 6px;
    border: none;
    width: 100%;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
.form-item-slider>>>.el-form-item__label{
  font-size: 12px;
  line-height: 38px;
}
.form-item>>>.el-form-item__label{
  font-size: 12px;
}
.el-select-dropdown__item{
  padding: 0 20px;
}
  span{
    font-size: 12px
  }
  .el-form-item{
    margin-bottom: 6px;
  }

  .switch-style{
    position: absolute;
    right: 10px;
    margin-top: -4px;
  }
  .color-picker-style{
    cursor: pointer;
    z-index: 1003;
  }
</style>