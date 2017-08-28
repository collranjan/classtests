package com.example.ranjankumar.class_test;


import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

public class HumidityActivity extends Activity implements SensorEventListener {

        private SensorManager mSensorManager;
        private Sensor mHumiditySensor;
        private Sensor mTemperatureSensor;
        private boolean isHumiditySensorPresent;
        private boolean isTemperatureSensorPresent;
        private TextView mRelativeHumidityValue;
        private TextView mAbsoluteHumidityValue;
        private float mLastKnownRelativeHumidity = 0;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            setContentView(R.layout.humidity_layout);
            mRelativeHumidityValue = (TextView)findViewById(R.id.relativehumiditytext);
            mAbsoluteHumidityValue = (TextView)findViewById(R.id.absolutehumiditytext);
            mSensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);

            if(mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY) != null) {
                mHumiditySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
                isHumiditySensorPresent = true;
            }
            else {
                mRelativeHumidityValue.setText("Relative Humidity Sensor is not available!");
                mAbsoluteHumidityValue.setText("Cannot calculate Absolute Humidity, as relative humidity sensor is not available!");
                isHumiditySensorPresent = false;
            }

            if(mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE) != null) {
                mTemperatureSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
                isTemperatureSensorPresent = true;
            } else {
                mAbsoluteHumidityValue.setText("Cannot calculate Absolute Humidity, as temperature sensor is not available!");
                isTemperatureSensorPresent = false;
            }
        }

        @Override
        protected void onResume() {
            super.onResume();
            if(isHumiditySensorPresent) {
                mSensorManager.registerListener(this, mHumiditySensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
            if(isTemperatureSensorPresent) {
                mSensorManager.registerListener(this, mTemperatureSensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }

        @Override
        protected void onPause() {
            super.onPause();
            if(isHumiditySensorPresent || isTemperatureSensorPresent) {
                mSensorManager.unregisterListener(this);
            }
        }


        @Override
        public void onSensorChanged(SensorEvent event) {
            if(event.sensor.getType()==Sensor.TYPE_RELATIVE_HUMIDITY) {
                mRelativeHumidityValue.setText("Relative Humidity in % is " + event.values[0]);
                mLastKnownRelativeHumidity = event.values[0];
            } else if(event.sensor.getType()==Sensor.TYPE_AMBIENT_TEMPERATURE) {
                if(mLastKnownRelativeHumidity !=0) {
                    float temperature = event.values[0];
                    float absoluteHumidity = calculateAbsoluteHumidity(temperature, mLastKnownRelativeHumidity);
                    mAbsoluteHumidityValue.setText("The absolute humidity at temperature: " + temperature + " is: " + absoluteHumidity);
                    float dewPoint = calculateDewPoint(temperature, mLastKnownRelativeHumidity);
                }
            }
        }


        public float calculateAbsoluteHumidity(float temperature, float relativeHumidity)
        {
            float Dv = 0;
            float m = 17.62f;
            float Tn = 243.12f;
            float Ta = 216.7f;
            float Rh = relativeHumidity;
            float Tc = temperature;
            float A = 6.112f;
            float K = 273.15f;

            Dv =   (float) (Ta * (Rh/100) * A * Math.exp(m*Tc/(Tn+Tc)) / (K + Tc));

            return Dv;
        }
        public float calculateDewPoint(float temperature, float relativeHumidity)
        {
            float Td = 0;
            float m = 17.62f;
            float Tn = 243.12f;
            float Rh = relativeHumidity;
            float Tc = temperature;

            Td = (float) (Tn * ((Math.log(Rh/100) + m*Tc/(Tn+Tc))/(m - (Math.log(Rh/100) + m*Tc/(Tn+Tc)))));

            return Td;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        @Override
        protected void onDestroy() {
            super.onDestroy();
            mSensorManager = null;
            mHumiditySensor = null;
            mTemperatureSensor = null;
        }

    }