const webpack = require('webpack')
const path = require('path')
const utils = require('./utils')

function resolve (dir) {
  return path.join(__dirname, '..', dir)
}

module.exports = {
  entry: {

    // PluginDemo: resolve('/src/views/PluginDemo.vue')
    BuddleView: resolve('/src/views/echarts/map/buddle/index.vue'),
    BuddleData: resolve('/src/views/echarts/map/buddle/data.vue'),
    BuddleType: resolve('/src/views/echarts/map/buddle/type.vue')

  },
  output: {
    path: resolve('/static/'),
    filename: '[name].js'
  },
  resolve: {
    extensions: ['.js', '.vue', '.json', '.svg'],
    alias: {
      'vue$': 'vue/dist/vue.esm.js',
      '@': resolve('src')
    }
  },
  externals: {
    vue: 'vue'
  },
  module: {
    rules: [
      {
        test: /\.vue$/,
        loader: 'vue-loader',
        options: {
          esModule: false, // vue-loader v13 更新 默认值为 true v12及之前版本为 false, 此项配置影响 vue 自身异步组件写法以及 webpack 打包结果
          loaders: utils.cssLoaders({
            sourceMap: true,
            extract: false // css 不做提取
          }),
          transformToRequire: {
            video: 'src',
            source: 'src',
            img: 'src',
            image: 'xlink:href'
          }
        }
      },
      {
        test: /\.js$/,
        loader: 'babel-loader',
        include: [resolve('src'), resolve('test')]
      },
      {
        test: /\.(png|jpe?g|gif|svg)(\?.*)?$/,
        loader: 'url-loader',
        options: {
          limit: 10000,
          name: utils.assetsPath('img/[name].[hash:7].[ext]')
        }
      },
      {
        test: /\.(mp4|webm|ogg|mp3|wav|flac|aac)(\?.*)?$/,
        loader: 'url-loader',
        options: {
          limit: 10000,
          name: utils.assetsPath('media/[name].[hash:7].[ext]')
        }
      },
      {
        test: /\.(woff2?|eot|ttf|otf)(\?.*)?$/,
        loader: 'url-loader',
        options: {
          limit: 10000,
          name: utils.assetsPath('fonts/[name].[hash:7].[ext]')
        }
      }
    ]
  },
  plugins: [
    new webpack.DefinePlugin({
      'process.env.NODE_ENV': '"production"'
    })

  ]/* ,
  chainWebpack: config => {
    config.module.rules.delete('svg') // 删除默认配置中处理svg,
    
    config.module
      .rule('svg-sprite-loader')
      .test(/\.svg$/)
      .include
      .add(resolve('src/icons')) // 处理svg目录
      .end()
      .use('svg-sprite-loader')
      .loader('svg-sprite-loader')
      .options({
        symbolId: 'icon-[name]'
      })
    if (process.env.NODE_ENV === 'production') {
     
    }
  } */
}
