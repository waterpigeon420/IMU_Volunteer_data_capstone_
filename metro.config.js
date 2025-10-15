const { getDefaultConfig, mergeConfig } = require("@react-native/metro-config");
const { getDefaultConfig: getDefaultExpoConfig } = require("expo/metro-config");

const defaultConfig = getDefaultConfig(__dirname);
const expoConfig = getDefaultExpoConfig(__dirname);

const config = {
  resolver: {
    sourceExts: [...new Set([...defaultConfig.resolver.sourceExts, "jsx", "ts", "tsx"])],
    assetExts: [...new Set([...defaultConfig.resolver.assetExts, ...(expoConfig.resolver?.assetExts || [])])],
  },
  transformer: {
    ...defaultConfig.transformer,
    getTransformOptions: async () => ({
      transform: {
        experimentalImportSupport: false,
        inlineRequires: true,
      },
    }),
  },
};

module.exports = mergeConfig(defaultConfig, config);
