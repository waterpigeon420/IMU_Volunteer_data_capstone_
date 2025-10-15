/**
 * @format
 */
//import 'react-native-url-polyfill/auto'; // For fetch, Headers, Request, Response
//import 'react-native-polyfill-globals';


import {AppRegistry} from 'react-native';
import App from './App';
import {name as appName} from './app.json';

AppRegistry.registerComponent(appName, () => App);
