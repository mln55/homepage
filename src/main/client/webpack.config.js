// https://webpack.js.org/configuration/#options
module.exports = (env, argv) => {

  const mode = env.production ? "production" : "development";

  return {
    mode: mode,

    entry: "./src/index.tsx",

    output: {
      filename: "main.js",
      path: __dirname + "../../resources/static/admin/js",
    },

    devtool: mode === "development" ? "inline-source-map" : "cheap-module-source-map",

    resolve: {
      extensions: [".ts", ".tsx", ".js"],
    },

    module: {
      rules: [
        { test: /\.tsx?$/, loader: "ts-loader" },
        { test: /\.css$/, use: ["style-loader", "css-loader"] }
      ]
    },

    devServer: {
      port: 3030,
      // contentBase: './',
      // publicPath: '/dist',
      proxy: {
        "/api/*": "http://127.0.0.1:8080",
        "/static/*": {
          target: "http://127.0.0.1:8080",
          rewrite: function(path, req) { return path.replace(/\/(.*?)/g, '')}
        }
      },
      historyApiFallback: true
    }
  }
}
