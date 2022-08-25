# RX5 communication example for Android

This project demonstrate how we are able to create an Android project to communicate with Koso RX5 device. We use bluetooth classic to communicate between Android and Rx5 (which is difference in iOS).



## Communication flow

We uses 3 commands for RX5 communication, including:

- When connected with RX5, two commands will be received by App:
    - RuntimeInfo1Command (contains speed, rpm, battery voltage, fuel consumption, gear and fuel level data.)
    - RuntimeInfo2Command (contains odo, trip1, trip2 and some other statistics info.)
- App takes responsibility to calculate real-time navigation info and send to RX5
    - NaviInfoCommand (used to send road name, turn direction and distance info to RX5)

```mermaid
sequenceDiagram
    participant App
    participant RX5
    Note left of App: connect to RX5
    loop connected
    RX5->>App: RuntimeInfo1Command
    RX5->>App: RuntimeInfo2Command
    end
    Note left of App: start navigation
	loop navigating
	App->>RX5: NaviInfoCommand 
	end
	
```



## Installing

- Clone git repository to local storage

    ```
    git clone https://github.com/koso-app/KosoRx5-Android.git
    ```

- Add core module into your project

    - Open your project
    - In the Android Studio, click File/New/import module and selects core module in the project just clone into "core" folder in the project

- In the settings.gradle, include "core" module

    ```
    include ':sample', ':core'
    ```

- In the module level build.gradle, add "core" module as dependency

    ```
    dependencies {
        implementation project(path: ':core')
        ...
    }
    ```

    

## Connect to a RX5

To connect to a RX5, you are able to find the Mac address of the RX5 by [Find bluetooth devices](https://developer.android.com/guide/topics/connectivity/bluetooth/find-bluetooth-devices). Then you are able to connect to this RX5 via static method Rx5Handler.startConnectService(). Following are the sample code to connect to a RX5.

```kotlin
import com.koso.rx5.core.Rx5Handler

// connect to a RX5 device
Rx5Handler.startConnectService(
    context,
    targetMacAddress,
    100 //foreground service notification ID
)
```

Rx5Handler.startConnectService() will start a foreground service with a notification to make sure the connection will not terminate by Android.



## Interrupt a connection

You're able to disconnect programmatically by following snippets.

```kotlin
// to stop a connection to RX5
Rx5Handler.stopConnectService(requireActivity())
```



## Listen to the RX5 connection state

Rx5Handler.STATE_LIVE is a [LiveData](https://developer.android.com/topic/libraries/architecture/livedata) observable object, provides runtime change of the Rx5Device.State which Including Disconnected, Discovering, Connected and Connecting

```kotlin
Rx5Handler.STATE_LIVE.observe(lifecycleOwner) {
    updateStateUi(it)
    if (it == Rx5Device.State.Connected) {
        //do something when connected to RX5
    } else if(it == Rx5Device.State.Disconnected){
        //do something when disconnected
    } else if(it == Rx5Device.State.Discovering){
        //do something when scanning device
    } else{
        //do something when connecting device
    }
}
```



## Listen to incoming commands

After connected to a RX5 device, you're able to subscribe the preodically incoming commands including Runtime-info1 (once per second) and Runtime-info2 (once per 10 seconds)

```kotlin
Rx5Handler.incomingCommandLive().observe(lifecyclerOwner){ it ->
    if(it is RuntimeInfo1Command){
        // do something about RuntimeInfo1Command
        val speed = it.speed
    } else if (it is RuntimeInfo2Command){
        // do something about RuntimeInfo2Command
        val odo = it.odo
    }
}
```



## Commands detail - Read from RX5

### Runtime-info 1 command

Send from RX5 to App once per second.
It contains runtime speed, rpm, battery voltage, fuel consumption, gear and fuel level data (please refer to the RuntimeInfo1Command data model below). App side will receive once per second automatically when app connected to RX5.

*RuntimeInfo1Command data model*

```kotlin
 class RuntimeInfo1Command{
     var speed: Int  //km/h
     var rpm: Int  // r/min
     var batt_vc: Int  // Battery voltage (by 0.1V)
     var consume: Int  // Fuel consumption L/H 
     var gear: Int  // instant gear
     var fuel: Int    // Fuel Level (bit0~6 , warming bit7)
     ...
 }
```



### Runtime-info 2 command

Send from RX5 to App once per 10 seconds.
It contains odo, trip1, trip2 and some other statistics info (please refer to the RuntimeInfo2Command data model below). App side will receive once per 10 seconds automatically when app connected to RX5

*RuntimeInfo2Command data model*

```kotlin
class RuntimeInfo2Command{
    var odo: Int // 32bit
    var odo_total: Int // 32bits
    var average_speed: Int //kmh, 32bits
    var rd_time: Int //Total ride sec, 16bit
    var average_consume: Int //平均油耗KM/L, 16 bits
    var trip_1: Int //trip1 meter, 32bits
    var trip_1_time: Int //trip1 time sec, 32bits
    var trip_1_average_speed: Int //16bits
    var trip_1_average_consume: Int //16bits

    var trip_2: Int //trip2 meter, 32bits
    var trip_2_time: Int //trip2 time sec, 32bits
    var trip_2_average_speed: Int //16bits
    var trip_2_average_consume: Int //16bits

    var trip_a: Int //trip auto meter, 32bits
    var al_time: Int //total time, 32bits
    var fuel_range: Int //km, 32bits
    var service_DST: Int //maintain alarm distance, 32bits
    ...
}
```



## Commands detail - Write to RX5

### Navigation info command

Navigation info, Send from App to Rx5, at least one transaction per 10 seconds.

In the navigation function, App should take responsibility of calculating the navigation info, then send the direction result to the RX5 at every location updated. You are able to write a NaviInfoCommand using Rx5Handler.rx5!!.write() method.

*Navigation info data model*

```kotlin
 class NaviInfoCommand{
     val navimode: Int,          //navimode  =0導航模式
     val ctname: String,      //char         ctname[24]; city name 
     val roadname: String,    //char         nowroadname[64];  //road name
     val doornum: String,     //char         doornum[24]; // house number
     val limitsp: Int,           //int          limitsp;  //limit speed of current street by km/h  
     val nextroadname: String,//char         nextroadname[64];  // next turn street name 
     val nextdist: Int,          //int          nextdist;  //next turn distance by meter   
     val nextturn: Int,          //int          nextturn; // next turn direction
     val camera: Int,            //int          camera; //speed camera alert :  0 for none, 1 exist one alert
     val navidist: Int,          //int          navidist; // total distance for navigation by meter        
     val navitime: Int,          //int          navitime; // total time for navigation by minutes   
     val gpsnum: Int,            //int          gpsnum; //GPS fixed count 
     val gpsdir: Int						// int gps heading
 }
```

To send a NaviInfoCommand to RX5

```kotlin
// create a NaviInfoCommand
val cmd = NaviInfoCommand(
    mode,
    cityname,
    roadname,
    doornum,
    limitsp,
    nextroad,
    nextdist,
    nextturn,
    camera,
    navidist,
    navitime,
    gpsnum,
    gpsdir
)

// Check if remote device available
if(Rx5Handler.rx5 != null) {
    // Write cmd to RX5
    val ok = Rx5Handler.rx5!!.write(cmd)
}else{
    // Failed, connection is not available
}
```



## RX5 nextturn type

RX5 turn type mapping to [Google direction API](https://developers.google.com/maps/documentation/directions/overview)'s Maneuver

| Google map maneuver | Turn type |                         Sample image                         |
| ------------------- | :-------: | :----------------------------------------------------------: |
| fork-left           |    36     | <img src="./readme-img/img_direction_folkleft.png" style="background-color:black;" width="40" height="40"/> |
| folk-right          |    37     | <img src="./readme-img/img_direction_folkright.png" style="background-color:black;" width="40" height="40"/> |
| merge               |    42     | <img src="./readme-img/img_direction_merge.png" style="background-color:black;" width="40" height="40"/> |
| ramp-left           |    34     | <img src="./readme-img/img_direction_rampleft.png" style="background-color:black;" width="40" height="40"/> |
| ramp-right          |    35     | <img src="./readme-img/img_direction_rampright.png" style="background-color:black;" width="40" height="40"/> |
| roundabout-left     |    40     | <img src="./readme-img/img_direction_roundaboutleft.png" style="background-color:black;" width="40" height="40"/> |
| roundabout-right    |    41     | <img src="./readme-img/img_direction_roundaboutright.png" style="background-color:black;" width="40" height="40"/> |
| straight            |     0     | <img src="./readme-img/img_direction_straight.png" style="background-color:black;" width="40" height="40"/> |
| turn-left           |     1     | <img src="./readme-img/img_direction_turnleft.png" style="background-color:black;" width="40" height="40"/> |
| turn-right          |     2     | <img src="./readme-img/img_direction_folkright.png" style="background-color:black;" width="40" height="40"/> |
| turn-sharp-left     |    30     | <img src="./readme-img/img_direction_turnsharpleft.png" style="background-color:black;" width="40" height="40"/> |
| turn-sharp-right    |    31     | <img src="./readme-img/img_direction_turnsharpright.png" style="background-color:black;" width="40" height="40"/> |
| uturn-left          |    38     | <img src="./readme-img/img_direction_uturnleft.png" style="background-color:black;" width="40" height="40"/> |
| uturn-right         |    39     | <img src="./readme-img/img_direction_uturnright.png" style="background-color:black;" width="40" height="40"/> |
| turn-slight-left    |    32     | <img src="./readme-img/img_direction_turnslightleft.png" style="background-color:black;" width="40" height="40"/> |
| turn-slight-right   |    33     | <img src="./readme-img/img_direction_turnslightright.png" style="background-color:black;" width="40" height="40"/> |
| arrival             |    25     |                                                              |
|                     |           |                                                              |
|                     |           |                                                              |

