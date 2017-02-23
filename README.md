GyroscopeExplorer
=================

![Alt text](http://www.kircherelectronics.com/resources/images/gyroscopeExplorer/gyroscope_explorer_home.png "Android Acceleration Explorer Screenshot")

# Introduction

Gyroscope Explorer provides a working open source code example and Android application that demonstrates how to use the gyroscope sensor for measuring the rotation of an Android device. While this example is implemented with Android/Java, the jist of the algorithm can be applied to almost any hardware/language combination to determine linear acceleration.

## Overiew of Features

Gyroscope Explorer contains Android classes that demonstrate how to use the Sensor.TYPE_GYROSCOPE and Sensor.TYPE_GYROSCOPE_UNCALIBRATED. This includes integrating the sensor outputs over time to describe the devices change in angles, initializing the rotation matrix, concatination of the new rotation matrix with the initial rotation matrix and providing an orientation for the concatenated rotation matrix. The Android developer documentation covers some of this information, but it is an incomplete example. Gyroscope Explorer provides an example that is fully implemented. Gyroscope Explorer provides the Earth frame orientation with the azimuth, pitch and roll and described in a clean graphical view.

###Caveat Emptor

Note that the gyroscope is subject to drift despite the fact that Sensor.TYPE_GYROSCOPE is supposed to compensate for drift. The gyroscope is also very drift sensitive to rapid rotations and external vibrations. 

Gyroscope Explorer also provides implementations of gyroscope sensor fusions that offer much more robust and reliable estimations of the device's roation. The sensor fusions use the acceleration sensor, magnetic sensor and gyroscope sensor to calculate rotation measurements that are not affected by rapid rotations or external vibrations.

Gyroscope Explorer Features:

* View the output of all of the sensors axes in real-time
* Log the output of all of the sensors axes to a .CSV file
* Mean filter for data smoothing
* Sensor fusions include three complimentary (Euler angle, rotation matrix and quaternion) and one Kalman (quaternion) filter.
* Visualize the tilt of the device
* Compare the performance of multiple devices

## Smoothing filters

Gyroscope Explorer implements the most common smoothing filter, a mean filters. The mean filter is designed to smooth the data points based on a time constant in units of seconds. The mean filter will average the samples that occur over a period defined by the time constant... the number of samples that are averaged is known as the filter window. The approach allows the filter window to be defined over a period of time, instead of a fixed number of samples. The mean filter is user configurable based on the time constant in units of seconds. The larger the time constant, the smoother the signal. However, latency also increases with the time constant. Because the filter coefficient is in the time domain, differences in sensor output frequencies have little effect on the performance of the filter. The smoothing filter should perform about the same across all devices regardless of the sensor frequency.

### Sensor Fusion Complimentary Filter

Gyroscope Explorer offers a number of different estimations of rotation using sensor fusion complimentary filters. The complementary filter is a frequency domain filter. In its strictest sense, the definition of a complementary filter refers to the use of two or more transfer functions, which are mathematical complements of one another. Thus, if the data from one sensor is operated on by G(s), then the data from the other sensor is operated on by I-G(s), and the sum of the transfer functions is I, the identity matrix. In practice, it looks nearly identical to a low-pass filter, but uses two different sets of sensor measurements to produce what can be thought of as a weighted estimation. 

In most cases, the gyroscope is used to measure the devices orientation. However, the gyrocope tends to drift due to round off errors and other factors. Most gyroscopes work by measuring very small vibrations in the earth's rotation, which means they really do not like external vibrations. Because of drift and external vibrations, the gyroscope has to be compensated with a second estimation of the devices orientation, which comes from the acceleration sensor and magnetic sensor. The acceleration sensor provides the pitch and roll estimations while the magnetic sensor provides the azimuth. A complimentary filter is used to fuse the two orienations together. It takes the form of gyro[0] = alpha * gyro[0] + (1 - alpha) * accel/magnetic[0]. Alpha is defined as alpha = timeConstant / (timeConstant + dt) where the time constant is the length of signals the filter should act on and dt is the sample period (1/frequency) of the sensor.

### Orientation Euler Angles Complimentary Filter (ImuOCfOrientation)

You can obtain an orientation comprised of Euler Angles (azimuth, pitch and roll) two ways in Android. The first method involves integrating the gyroscope measurements. The second is obtained by using the acceleration and magnetic sensors. These two measurements are ideal for a complimentary filter fusion. Paul Lawitzki wrote such an algorithm (you can find his writeup [here](http://www.thousand-thoughts.com/2012/03/android-sensor-fusion-tutorial/). I have modified it slightly and included it in Acceleration Explorer. While it is not what I consider an elegant, or particularly fast algorithm, it is highly intutive and easier to grasp than most approaches. Most importantly, it works well.

### Rotation Matrix Complimentary Filter (ImuOCfRotationMatrix)

Rotation matrices for the gyroscope and acceleration/magnetic sensor can be obtained in much the same way as the orientation Euler angles... in fact you need them to obtain the orientation. Rotation matrices can be scaled with a scalar matrix just like any other matrix, which allows them to be used in a complementary filter. Instead of using the orientation, the complementary filter is applied to the rotation matrices which is slightly more efficient and significantly more elegant than using the orientations. Rotation matrices suffer from many singularites including gimbal lock, so they are not ideal. However, many people are familiar with the concept of rotation matrices so this approach may be more simple to understand.

### Quaternions Complimentary Filter (ImuOCfQuaternion)

Quaternions offer an angle-axis solution to rotations which do not suffer from many of the singularies, including gimbal lock, that you will find with rotation matrices. Quaternions can also be scaled and applied to a complimentary filter. The quaternion complimentary filter is probably the most elegant, robust and accurate of the filters, although it can also be the most difficult to implement.

### Quaternion Kalman Filter (ImuOKfQuaternion)

Kalman filtering, also known as linear quadratic estimation (LQE), is an algorithm that uses a series of measurements observed over time, containing noise (random variations) and other inaccuracies, and produces estimates of unknown variables that tend to be more precise than those based on a single measurement alone. More formally, the Kalman filter operates recursively on streams of noisy input data to produce a statistically optimal estimate of the underlying system state. Much like complimentary filters, Kalman filters require two sets of estimations, which we have from the gyroscope and acceleration/magnetic senor. The Acceleration Explorer implementation of the Kalman filter relies on quaternions.

For more information on integrating the gyroscope to obtain a quaternion, rotation matrix or orientation, see [here](http://www.kircherelectronics.com/blog/index.php/11-android/sensors/15-android-gyroscope-basics).

Useful Links:

* [Gyroscope Explorer Homepage](http://www.kircherelectronics.com/gyroscopeexplorer/gyroscopeexplorer)
* [Gyroscope Explorer Community](http://kircherelectronics.com/forum/viewforum.php?f=12)
* [Gyroscope Explorer Blog Article](http://www.kircherelectronics.com/blog/index.php/11-android/sensors/15-android-gyroscope-basics)
* [Download Gyroscope Explorer from Google Play](https://play.google.com/store/apps/details?id=com.kircherelectronics.com.gyroscopeexplorer)

Written by [Kircher Electronics](https://www.kircherelectronics.com)

