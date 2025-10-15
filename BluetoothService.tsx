import { NativeModules, PermissionsAndroid, Platform, Alert } from 'react-native';


const { BluetoothModule, ManageExternalStorage } = NativeModules;

if (!BluetoothModule) {
  console.warn('[BluetoothService] ⚠️ BluetoothModule is not linked! Check native code.');
}

if (!ManageExternalStorage) {
  console.warn('[BluetoothService] ⚠️ ManageExternalStorage module is not linked! Check native code.');
}

export const requestBluetoothPermissions = async (): Promise<boolean> => {
  if (Platform.OS === 'android') {
    try {
      console.log('[BluetoothService] 🔄 Requesting Bluetooth and Storage permissions...');

      const permissions = [
        PermissionsAndroid.PERMISSIONS.BLUETOOTH_SCAN,
        PermissionsAndroid.PERMISSIONS.BLUETOOTH_CONNECT,
        PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
      ];

      // Add storage permissions based on Android version
      if (Platform.Version < 30) {
        permissions.push(
          PermissionsAndroid.PERMISSIONS.READ_EXTERNAL_STORAGE,
          PermissionsAndroid.PERMISSIONS.WRITE_EXTERNAL_STORAGE
        );
      }

      const granted = await PermissionsAndroid.requestMultiple(permissions);

      console.log('[BluetoothService] 🟢 Permissions Response:', granted);

      const allGranted = Object.values(granted).every(value => value === PermissionsAndroid.RESULTS.GRANTED);

      if (!allGranted) {
        console.warn('[BluetoothService] ❌ Some permissions were denied.');
        Alert.alert('Permission Denied', 'Please enable all permissions in settings.');
        return false;
      }

      // For Android 11+ (API level 30 and above), use ManageExternalStorage module to handle MANAGE_EXTERNAL_STORAGE
      if (Platform.Version >= 30) {
        const hasStoragePermission = await ManageExternalStorage.hasPermission();
        
        if (!hasStoragePermission) {
          console.warn('[BluetoothService] ❌ Storage permission not granted.');
          Alert.alert('Permission Denied', 'Please enable storage permission in settings.');
          return false;
        }
      }

      console.log('[BluetoothService] ✅ All permissions granted.');
      return true;
    } catch (error) {
      console.error(`[BluetoothService] ❌ Permissions Request Failed: ${JSON.stringify(error)}`);
      return false;
    }
  }
  return true; // iOS doesn’t need explicit permission handling
};

const BluetoothService = {
  startBluetooth: async (): Promise<string> => {
    try {
      console.log('[BluetoothService] 🔄 Starting Bluetooth connection...');

      // ✅ Check permissions first
      const hasPermission = await requestBluetoothPermissions();
      if (!hasPermission) return '[BluetoothService] ❌ Bluetooth Permissions Denied';

      if (!BluetoothModule) {
        return '[BluetoothService] ❌ BluetoothModule is missing. Check native linking.';
      }

      const result = await BluetoothModule.startBluetooth();
      console.log('[BluetoothService] ✅ Bluetooth Connected:', result);

      // ✅ Save file in Download directory


      // ✅ Check if file exists before writing
    

      return result;
    } catch (error: any) {
      const errorMessage = `[BluetoothService] ❌ Bluetooth Connection Failed: ${
        error.message || JSON.stringify(error)
      }`;
      console.error(errorMessage);
      return errorMessage;
    }
  },

  stopBluetooth: async (): Promise<string> => {
    try {
      console.log('[BluetoothService] 🔄 Stopping Bluetooth connection...');

      if (!BluetoothModule) {
        return '[BluetoothService] ❌ BluetoothModule is missing. Check native linking.';
      }

      const result = await BluetoothModule.stopBluetooth();
      console.log('[BluetoothService] ✅ Bluetooth Disconnected:', result);
      return result;
    } catch (error: any) {
      const errorMessage = `[BluetoothService] ❌ Bluetooth Disconnection Failed: ${
        error.message || JSON.stringify(error)
      }`;
      console.error(errorMessage);
      return errorMessage;
    }
  },
};

export default BluetoothService;
