module.exports = {
  root: true,
  parser: '@babel/eslint-parser', // ✅ Ensures ESLint uses Babel
  extends: ['@react-native', 'plugin:prettier/recommended'],
  parserOptions: {
    // ✅ Prevents ESLint from requiring `.babelrc`
    babelOptions: {
      configFile:
        'C:/Users/vidha/OneDrive/Documents/lmaolol/BluetoothApp/babel.config.js', // ✅ Ensures ESLint finds the Babel config
    },
  },
  rules: {
    'prettier/prettier': ['error', {semi: false}],
    semi: 'on',
  },
}
