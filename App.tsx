import React, {useState} from 'react';
import {
  View,
  Button,
  Text,
  StyleSheet,
  ScrollView,
  Platform,
} from 'react-native';
import BluetoothService from './BluetoothService'

export default function App() {
  const [log, setLog] = useState<string>('Log:\n');

  const addLog = (message: string) => {
    setLog(prevLog => `${prevLog}\n${message}`);
  };

  const startBluetooth = async () => {
    if (Platform.OS === 'android') {
      const response: string = await BluetoothService.startBluetooth();
      addLog(response);
    } else {
      addLog('Bluetooth only works on Android');
    }
  };

  const stopBluetooth = async () => {
    if (Platform.OS === 'android') {
      const response: string = await BluetoothService.stopBluetooth();
      addLog(response);
    } else {
      addLog('Bluetooth only works on Android');
    }
  };

  return (
    <View style={styles.container}>
      <Button title="Start Bluetooth" color="green" onPress={startBluetooth} />
      <Button title="Stop Bluetooth" color="red" onPress={stopBluetooth} />
      <ScrollView style={styles.logBox}>
        <Text style={styles.logText}>{log}</Text>
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  logBox: {
    width: '100%',
    height: 150,
    borderWidth: 1,
    borderColor: 'black',
    marginTop: 20,
    padding: 10,
  },
  logText: {
    fontSize: 14,
    color: 'black',
  },
});
