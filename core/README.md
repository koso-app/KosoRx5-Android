## Permissions

Declare the Bluetooth permission(s) in your application manifest file. For example:
      <uses-permission android:name="android.permission.BLUETOOTH" />
      <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
    
      <manifest ... >
          <uses-permission android:name="android.permission.BLUETOOTH" />
          <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
        
          <!-- If your app targets Android 9 or lower, you can declare
               ACCESS_COARSE_LOCATION instead. -->
          <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
          ...
        </manifest>


## Dependencies

    implementation 'io.reactivex.rxjava2:rxandroid:2.1.1'
    implementation 'io.reactivex.rxjava2:rxjava:2.2.15'
    implementation 'com.github.ivbaranov:rxbluetooth2:2.1.1'