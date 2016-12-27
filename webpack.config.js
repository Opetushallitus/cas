const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const validate = require('webpack-validator');
var ExtractTextPlugin = require('extract-text-webpack-plugin');
var CleanWebpackPlugin = require('clean-webpack-plugin');
const LodashModuleReplacementPlugin = require('lodash-webpack-plugin');

const webpack = require('webpack');

const PATHS = {
  webapp: path.join(__dirname, 'src/main/webapp/app/'),
  app: path.join(__dirname, 'app'),
  build: path.join(__dirname, 'build'),
  style: path.join(__dirname, 'app', 'main.scss'),
  images: path.join(__dirname, 'app/resources/img')

};

var config = {
  entry: {
    app: PATHS.app
  },
  output: {
    path: PATHS.webapp,
    filename: '[name].js',
    chunkFilename: '[id].js'
  },
  resolve: {
    extensions: ['', '.js', '.jsx']
  },
  devtool: 'source-map',
  module: {
    loaders: [
      {
        test: /\.jsx?$/,
        exclude: /(node_modules|bower_components)/,
        loader: 'babel',
        query: {
          presets:['es2015', 'react'],
          plugins: ["transform-object-rest-spread", "lodash"]
        }
      },
      {
        test: /\.json$/,
        loader: "json-loader"
      },
      {
        test: /\.woff2?$|\.ttf$|\.svg$|\.eot$/,
        loader: "url?limit=200000"
      },
      {
        test: /\.(jpg|png|gif)$/,
        loader: 'url?limit=200000',
        include: PATHS.images
      },
      {
        test: /\.css$/,
        loader: ExtractTextPlugin.extract("style-loader", "css-loader"),
        include: PATHS.app
      },
      {
        test: /\.scss/,
        loader: ExtractTextPlugin.extract("style-loader", "css-loader!sass-loader")
      }
    ]
  },
  devServer: {
    historyApiFallback: true,
    hot: true,
    inline: true,
    stats: 'errors-only',
  },

  plugins: [
    new LodashModuleReplacementPlugin,
    new webpack.DefinePlugin({
      'process.env': {
        'NODE_ENV': JSON.stringify('production')
      }
    }),
    new ExtractTextPlugin("[name].css"),
    new webpack.optimize.UglifyJsPlugin({
      mangle: true,
      compress: {
        warnings: false,
      }
    }),
    new CleanWebpackPlugin([PATHS.webapp], {
      root: process.cwd()
    }),
    new HtmlWebpackPlugin({
      template: 'app/index.html',
      title: 'Login',
      appMountId: 'app',
      inject: false
    }),
    new webpack.HotModuleReplacementPlugin({
      multiStep: true
    })
  ]
};

module.exports = validate(config);
