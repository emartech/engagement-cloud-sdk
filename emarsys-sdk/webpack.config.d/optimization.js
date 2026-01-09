// Webpack configuration for aggressive minification
const TerserPlugin = require('terser-webpack-plugin');

config.devtool = false;
config.optimization = config.optimization || {};
config.optimization.minimize = true;
config.optimization.minimizer = [
    new TerserPlugin({
        terserOptions: {
            compress: {
                unused: true,
                dead_code: true,
                drop_debugger: true,
                conditionals: true,
                evaluate: true,
                sequences: true,
                booleans: true,
                passes: 3, // Run compression multiple times
            },
            output: {
                comments: false, // Remove all comments
                beautify: false,
            },
            mangle: true,
        },
    }),
];
