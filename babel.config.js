module.exports = {
  presets: ['module:metro-react-native-babel-preset'],
  plugins: [
     // If using Reanimated
    '@babel/plugin-transform-runtime', // Helps with polyfills
  ],
};