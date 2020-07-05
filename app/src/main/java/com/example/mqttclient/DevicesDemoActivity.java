package com.example.mqttclient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.example.mqttclient.mqtt.MqttService;
import com.example.mqttclient.protocol.AirConditioningMessage;
import com.example.mqttclient.protocol.BoolMessage;
import com.example.mqttclient.protocol.FloatMessage;
import com.example.mqttclient.protocol.IntMessage;
import com.google.gson.Gson;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class DevicesDemoActivity extends AppCompatActivity implements MqttService.MqttEventCallBack, CompoundButton.OnCheckedChangeListener {

    private TextView connectState, temperatureValue, humidityValue, pmValue, carbonDioxideValue,
            gasValue, waterTowerValue, illuminanceValue , doorStatus, peopleStatus, windowStatus;
    private EditText airCconditioningValue;
    private MqttService.MqttBinder mqttBinder;
    private String TAG = "MainActivity";
    private Switch parlourLightSwitch, curtain_switch, fan_socket_switch, air_conditioning_switch;
    private ImageView parlour_light_switch_image,door_status_image,people_status_image,fan_socket_switch_image,
            window_status_image;
    private Map<String, Integer> subscribeTopics = new HashMap<>();

    private Button arrow_down,arrow_up;

    private Button lightDelete,curtainDelete,fanDelete,
            addLight,addCurtain,addFan;

    private LinearLayout mainLinearLayout;
    private CardView lightCardView,curtainCardView,fanCardView;


    //风扇图标动态显示
    private final Timer timer = new Timer();
    int count=0;
    int temperature=25;
    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            count++;
            fan_socket_switch_image.animate().rotation(count%18*20);
        }
    };

    Handler handler=new Handler();
    Runnable runnable=new Runnable() {
        @Override
        public void run() {
            count++;
            fan_socket_switch_image.animate().rotation(count*20);
            handler.postDelayed(this, 100);
        }
    };


    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mqttBinder = (MqttService.MqttBinder) iBinder;
            mqttBinder.setMqttEventCallback(DevicesDemoActivity.this);
            if (mqttBinder.isConnected()) {
                connectState.setText("已连接");
                subscribeTopics();
            } else {
                connectState.setText("未连接");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices_demo);

        connectState = findViewById(R.id.dev_connect_state);

        Intent mqttServiceIntent = new Intent(this, MqttService.class);
        bindService(mqttServiceIntent, connection, Context.BIND_AUTO_CREATE);

        temperatureValue = findViewById(R.id.temperature_value);

        humidityValue = findViewById(R.id.humidity_value);
        pmValue = findViewById(R.id.pm_value);
        gasValue = findViewById(R.id.gas_value);
        carbonDioxideValue=findViewById(R.id.carbon_dioxide_value);
        waterTowerValue=findViewById(R.id.waterTower_value);
        illuminanceValue=findViewById(R.id.illuminance_value);
        doorStatus = findViewById(R.id.door_status);
        windowStatus=findViewById(R.id.window_status);
        peopleStatus=findViewById(R.id.people_status);


        airCconditioningValue = findViewById(R.id.air_conditioning_value);
        parlourLightSwitch = findViewById(R.id.parlour_light_switch);
        parlourLightSwitch.setOnCheckedChangeListener(this);
        curtain_switch = findViewById(R.id.curtain_switch);
        curtain_switch.setOnCheckedChangeListener(this);
        fan_socket_switch = findViewById(R.id.fan_socket_switch);
        fan_socket_switch.setOnCheckedChangeListener(this);
        air_conditioning_switch = findViewById(R.id.air_conditioning_switch);
        air_conditioning_switch.setOnCheckedChangeListener(this);


        parlour_light_switch_image=findViewById(R.id.parlour_light_switch_image);
        door_status_image=findViewById(R.id.door_status_image);
        people_status_image=findViewById(R.id.people_status_image);
        fan_socket_switch_image=findViewById(R.id.fan_socket_switch_image);
        window_status_image=findViewById(R.id.window_status_image);
        arrow_down=findViewById(R.id.arrow_down);
        arrow_up=findViewById(R.id.arrow_up);


        mainLinearLayout=findViewById(R.id.mainLinearLayout);
        lightDelete=findViewById(R.id.lightDelete);
        lightCardView=findViewById(R.id.lightCardView);
        curtainDelete=findViewById(R.id.curtainDelete);
        curtainCardView=findViewById(R.id.curtainCardView);
        fanDelete=findViewById(R.id.fanDelete);
        fanCardView=findViewById(R.id.fanCardView);

        addCurtain=findViewById(R.id.addCurtain);
        addFan=findViewById(R.id.addFan);
        addLight=findViewById(R.id.addLight);

        temperature = Integer.parseInt(airCconditioningValue.getText().toString());

        //空调按钮调节温度
        arrow_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                temperature++;
                airCconditioningValue.setText(String.valueOf(temperature));
                if (air_conditioning_switch.isChecked()){
                    try {
                        String json = new Gson().toJson(new AirConditioningMessage(true,
                                Float.parseFloat(airCconditioningValue.getText().toString())));
                        Log.d("json",json);
                        mqttBinder.publishMessage("/test/airConditioning",json);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        arrow_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                temperature--;
                airCconditioningValue.setText(String.valueOf(temperature));
                if (air_conditioning_switch.isChecked()){
                    try {
                        String json = new Gson().toJson(new AirConditioningMessage(true,
                                Float.parseFloat(airCconditioningValue.getText().toString())));
                        Log.d("json",json);
                        mqttBinder.publishMessage("/test/airConditioning",json);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        //添加按钮模块
        addLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lightCardView.setVisibility(View.VISIBLE);
                addLight.setVisibility(View.GONE);
            }
        });
        addCurtain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                curtainCardView.setVisibility(View.VISIBLE);
                addCurtain.setVisibility(View.GONE);
            }
        });
        addFan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fanCardView.setVisibility(View.VISIBLE);
                addFan.setVisibility(View.GONE);
            }
        });


        //删除按钮模块
        lightDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lightCardView.setVisibility(View.GONE);
                addLight.setVisibility(View.VISIBLE);
            }
        });
        curtainDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                curtainCardView.setVisibility(View.GONE);
                addCurtain.setVisibility(View.VISIBLE);
            }
        });
        fanDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fanCardView.setVisibility(View.GONE);
                addFan.setVisibility(View.VISIBLE);
            }
        });


    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.parlour_light_switch:
                try {
                    if (compoundButton.isChecked()) {
                        mqttBinder.publishMessage("/test/light1",
                                new Gson().toJson(new BoolMessage(true)));
                        parlour_light_switch_image.setImageResource(R.drawable.light);

                    } else {
                        mqttBinder.publishMessage("/test/light1",
                                new Gson().toJson(new BoolMessage(false)));
                        parlour_light_switch_image.setImageResource(R.drawable.dark);
                    }
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.curtain_switch:
                try {
                    if (compoundButton.isChecked()) {
                        mqttBinder.publishMessage("/test/curtain1",
                                new Gson().toJson(new BoolMessage(true)));
                    } else {
                        mqttBinder.publishMessage("/test/curtain1",
                                new Gson().toJson(new BoolMessage(false)));
                    }
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.fan_socket_switch:
                try {
                    if (compoundButton.isChecked()) {
                        mqttBinder.publishMessage("/test/fan1",
                                new Gson().toJson(new BoolMessage(true)));

                        handler.postDelayed(runnable, 100);//每两秒执行一次runnable.


                    } else {
                        mqttBinder.publishMessage("/test/fan1",
                                new Gson().toJson(new BoolMessage(false)));

                        handler.removeCallbacks(runnable);
                    }
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.air_conditioning_switch:
                try {
                    if (compoundButton.isChecked()) {
                        String json = new Gson().toJson(new AirConditioningMessage(true,
                                Float.parseFloat(airCconditioningValue.getText().toString())));
                        Log.d("json",json);
                        mqttBinder.publishMessage("/test/airConditioning",json);
                    } else {
                        String json = new Gson().toJson(new AirConditioningMessage(false,
                                Float.parseFloat(airCconditioningValue.getText().toString())));
                        Log.d("json",json);
                        mqttBinder.publishMessage("/test/airConditioning",json);
                    }
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    void subscribeTopics() {
        try {
            subscribeTopics.put("/test/temp",1);
            subscribeTopics.put("/test/hum", 2);
            subscribeTopics.put("/test/pm",3);
            subscribeTopics.put("/test/gas",4);
            subscribeTopics.put("/test/door",5);
            subscribeTopics.put("/test/co2",6);
            subscribeTopics.put("/test/waterTower",7);
            subscribeTopics.put("/test/illuminance",8);
            subscribeTopics.put("/test/human",9);
            subscribeTopics.put("/test/window",10);

            for(Map.Entry<String, Integer> entry : subscribeTopics.entrySet()){
                mqttBinder.subscribe(entry.getKey());
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    void unSubscribeTopics() {
        try {
            for(Map.Entry<String, Integer> entry : subscribeTopics.entrySet()){
                mqttBinder.unSubscribe(entry.getKey());
            }
            subscribeTopics.clear();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectSuccess() {
        subscribeTopics();
        connectState.setText("已连接");
    }

    @Override
    public void onConnectError(String error) {
        Log.d(TAG, "onConnectError: " + error);
        connectState.setText("未连接");
        subscribeTopics.clear();
    }

    @Override
    public void onDeliveryComplete() {
        Log.d(TAG, "publish ok");
    }

    @Override
    public void onMqttMessage(String topic, String message) {
        Log.d("onMqttMessage", "topic:"+topic+ "message length:"+ message.length() + ", message:"+message);
        Gson gson = new Gson();
        switch (subscribeTopics.get(topic)){
            case 1:
                temperatureValue.setText(String.valueOf(gson.fromJson(message.trim(), FloatMessage.class).value));
                break;

            case 2:
                humidityValue.setText(String.valueOf(gson.fromJson(message.trim(), IntMessage.class).value));
                break;

            case 3:
                pmValue.setText(String.valueOf(gson.fromJson(message.trim(), IntMessage.class).value));
                break;

            case 4:
                gasValue.setText(String.valueOf(gson.fromJson(message.trim(), IntMessage.class).value));
                break;

            case 5:
                String status = gson.fromJson(message.trim(), BoolMessage.class).value ?"开":"关";
                doorStatus.setText(status);
                if (status.equals("开")){
                    door_status_image.setImageResource(R.drawable.open);
                }else {
                    door_status_image.setImageResource(R.drawable.close);
                }
                break;
                //新增检测CO2浓度等功能
            case 6:
                carbonDioxideValue.setText(String.valueOf(gson.fromJson(message.trim(), IntMessage.class).value));
                break;

            case 7:
                waterTowerValue.setText(String.valueOf(gson.fromJson(message.trim(), IntMessage.class).value));
                break;
            case 8:
                illuminanceValue.setText(String.valueOf(gson.fromJson(message.trim(), IntMessage.class).value));
                break;

            case 9:
                String people_Status = gson.fromJson(message.trim(), BoolMessage.class).value ?"有":"无";
                peopleStatus.setText(people_Status);
                if (people_Status.equals("有")){
                    people_status_image.setImageResource(R.drawable.people);
                }else {
                    people_status_image.setImageResource(R.drawable.nopeople);
                }
                break;
            case 10:
                String window_Status = gson.fromJson(message.trim(), BoolMessage.class).value ?"开":"关";
                windowStatus.setText(window_Status);
                if (window_Status.equals("开")){
                    window_status_image.setImageResource(R.drawable.open_window);
                }else {
                    window_status_image.setImageResource(R.drawable.close_window);
                }
                break;
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (mqttBinder.isConnected()) {
            connectState.setText("已连接");
            subscribeTopics();
        } else {
            connectState.setText("未连接");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unSubscribeTopics();
    }

    @Override
    protected void onDestroy() {
        unbindService(connection);
        super.onDestroy();
    }

}
