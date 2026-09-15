const path = require("path");
const rspack = require("@rspack/core");

const isProd = process.env.NODE_ENV === "production";

/** Dependencias compartidas como singleton entre el shell y el microfrontend remoto. */
const shared = {
  react: { singleton: true, requiredVersion: "^19.0.0" },
  "react-dom": { singleton: true, requiredVersion: "^19.0.0" },
  "@mui/material": { singleton: true, requiredVersion: "^7.0.0" },
  "@emotion/react": { singleton: true, requiredVersion: "^11.0.0" },
  "@emotion/styled": { singleton: true, requiredVersion: "^11.0.0" },
  "@reduxjs/toolkit": { singleton: true, requiredVersion: "^2.0.0" },
  "react-redux": { singleton: true, requiredVersion: "^9.0.0" },
};

module.exports = {
  entry: "./src/index.ts",
  mode: isProd ? "production" : "development",
  devtool: isProd ? false : "source-map",
  resolve: { extensions: [".ts", ".tsx", ".js", ".jsx"] },
  output: {
    path: path.resolve(__dirname, "dist"),
    publicPath: "auto",
    clean: true,
    uniqueName: "shell",
  },
  devServer: {
    port: 3001,
    historyApiFallback: true,
    hot: true,
  },
  module: {
    rules: [
      {
        test: /\.(ts|tsx)$/,
        exclude: /node_modules/,
        use: {
          loader: "builtin:swc-loader",
          options: {
            jsc: {
              parser: { syntax: "typescript", tsx: true },
              transform: { react: { runtime: "automatic" } },
            },
          },
        },
        type: "javascript/auto",
      },
    ],
  },
  plugins: [
    new rspack.HtmlRspackPlugin({ template: "./index.html" }),
    new rspack.container.ModuleFederationPlugin({
      name: "shell",
      remotes: {
        // El remoto se carga en runtime; su ausencia no rompe el build del host.
        mfRemoto: "mfRemoto@http://localhost:3002/remoteEntry.js",
      },
      shared,
    }),
  ],
};
