# CS305-MQTT-Client
The MQTTClient is an MQTT client written in Java for Android phone, based on MQTT 3.1.


## Features

|                        |                    |      |                     |                    |
| ---------------------- | ------------------ | ---- | ------------------- | ------------------ |
| MQTT 3.1               | :heavy_check_mark: |      | View Sensors' Data  | :heavy_check_mark: |
| Publish Sensor Data    | :heavy_check_mark: |      | Get Sensor's Data   | :heavy_check_mark: |
| Use Graph to View Data | :heavy_check_mark: |      | Subscribe Manually  | :heavy_check_mark: |
| Subscribe Conveniently | :heavy_check_mark: |      | Change Publish Rate | :heavy_check_mark: |

But not support Andriod v8 currently.


## Project description:

The MQTTClient project has been created to let a phone to update its sensors' data to MQTT server and view sensors' data in other phone.
Here we use Apollo 1.7.1 server and the client library "Eclipse Paho Android Service" to implement this app.


## Using the MQTT Client

### Downloading APK

Download MQTTClient.apk, install in your phone.

### Downloading apache-apollo-1.7.1-windows-distro(The MQTT Server)

Download apache-apollo-1.7.1 from http://activemq.apache.org/apollo/download.html.

### Run the server in your computer

Details of how to run a server can be viewd from https://www.cnblogs.com/xiaojitui/p/7874654.html.
After run the server successfully you need to record the IP address of your computer and the port that server use.

### Run the client in your phone

Install it in your phone, open it, and input the tcp address of your server, click connection switch, then you can start publish or view other phone's sensor data.

## Reference

### Links

- MQTT Official Website： [https://mqtt.org)
- MQTT.fx：[https://mqttfx.jensd.de)
- Playing with MQTT by Android 1/2 ==publish messages==：[https://www.youtube.com/watch?v=BAkGm02WBc0)
- Playing with MQTT by Android 2/2 ==subscribe messages==：[https://www.youtube.com/watch?v=6AE4D8INs_U)
- MQTT Client Library Encyclopedia – Paho Android Service：[https://www.hivemq.com/blog/mqtt-client-library-enyclopedia-paho-android-service/)
- MQTT + apache-apollo服务器初学使用：[https://www.cnblogs.com/xiaojitui/p/7874654.html)
- Android获取Mac地址-适配所有版本：[https://blog.csdn.net/chaozhung_no_l/article/details/78329371)
- Android组件系列----当前Activity跳转到另一个Activity的详细过程：[https://www.cnblogs.com/smyhvae/p/3863720.html)
- Android屏幕控制一：强制竖屏横屏：[https://blog.csdn.net/lixpjita39/article/details/72899629)
- Android图表控件MPAndroidChart——曲线图LineChart（多条曲线）动态添加数据：[https://blog.csdn.net/ww897532167/article/details/74139843)
- Java中Date与String的相互转换：[https://www.cnblogs.com/huangminwen/p/5994846.html)
- MQTT——服务器搭建（一）： [https://www.cnblogs.com/chenrunlin/p/5090916.html?utm_source=tuicool&utm_medium=referral)
- Android NumberPicker的基本用法及常见问题汇总：[https://www.jianshu.com/p/1042995703ad)
- SwipeRefreshLayout的使用详解：[https://blog.csdn.net/fightingxia/article/details/75303600)
- Android 更新UI的两种方法——handler和runOnUiThread()：[https://www.cnblogs.com/H-BolinBlog/p/5518720.html)
- EditText限制输入换行：[https://blog.csdn.net/xiaoyi_tdcq/article/details/78890455)
- 安卓侧滑删除功能：[https://blog.csdn.net/sinat_40387150/article/details/80909097)
- android 带EditView（编辑框）的AlertDialog（对话框）及获取输入内容：[https://blog.csdn.net/feiqinbushizheng/article/details/78837711)
- Java读取文件操作、实现读一行删一行：[https://blog.csdn.net/QiaoTongCSDN/article/details/80743674)
- Android之使用传感器获取相应数据：[https://www.cnblogs.com/zhaoyanhaoBlog/p/9139563.html)
- 如何获取Android设备所支持的传感器种类：[https://blog.csdn.net/redoq/article/details/52515123)
- Android Studio 打包、生成jks密钥、签名Apk、多渠道打包：[https://blog.csdn.net/xiaoyangsavvy/article/details/71107252)
- MQTT--topic（主题）设计：[https://blog.csdn.net/qq_28877125/article/details/78360376)
- Android 文件的读取和写入：[https://blog.csdn.net/zadarrien_china/article/details/55226068)