package com.dji.uilibrarydemo;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import dji.common.LightbridgePIPPosition;
import dji.common.LightbridgeSecondaryVideoFormat;
import dji.common.airlink.LightbridgeSecondaryVideoDisplayMode;
import dji.common.airlink.LightbridgeSecondaryVideoOutputPort;
import dji.common.battery.BatteryState;
import dji.common.camera.ResolutionAndFrameRate;
import dji.common.camera.SettingsDefinitions;
import dji.common.camera.SystemState;
import dji.common.error.DJIError;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.gimbal.GimbalState;
import dji.common.product.Model;
import dji.common.realname.AircraftBindingState;
import dji.common.realname.AppActivationState;
import dji.common.remotecontroller.AircraftMappingStyle;
import dji.common.remotecontroller.HardwareState;
import dji.common.useraccount.UserAccountState;
import dji.common.util.CommonCallbacks;
import dji.internal.version.DJIModelUpgradePackList;
import dji.sdk.airlink.LightbridgeLink;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.gimbal.Gimbal;
import dji.sdk.products.Aircraft;
import dji.sdk.remotecontroller.RemoteController;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.sdk.useraccount.UserAccountManager;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    private BaseProduct product;
    private CommonCallbacks.CompletionCallback sendDataCallback;
    private Button pwmOutput;
    private double longitude,latitude,altitude,flightpitch,flightroll,flightyaw,gimbalpitch,gimbalyaw,gimbalroll;
    private NotificationManager mNotificationManager;
    private String timeStamp="00:00";
    private Boolean isVideoDataRecord=false;
    private DJISDKManager djisdkManager;
    private TextView batteryVol,SqlState;
    private Timer timer;
    private TimerTask timerTask;


//    private Runnable r=new Runnable() {
//        @Override
//        public void run() {
//            while(true) {
//                while (isVideoDataRecord) {
//                    addTimeStamp(timeStamp);
//                    try {
//                        Thread.sleep(5);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
//    };

    private class RecordData extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... v) {
            //showToast("正在记录视频数据");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    SqlState.setText("正在记录视频数据…");
                }
            });

            if(timer==null) timer=new Timer();
            if(timerTask==null) timerTask=new TimerTask() {
                @Override
                public void run() {
                    addTimeStamp(timeStamp);
                }
            };

            timer.schedule(timerTask,0,20);
            return null;
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            product=DemoApplication.getProductInstance();
            if (product != null && product.isConnected()&&!product.getModel().equals(Model.UNKNOWN_AIRCRAFT)){
                FlightController flightController = ((Aircraft) product).getFlightController();
                if (null != flightController) {
                    if (flightController.isOnboardSDKDeviceAvailable()) {
                        switch (msg.what) {
                            case 1:
                                Log.d(TAG, "C1,C2,ShutterButton");
                                //给Onboard SDK发送4个字节
                                flightController.sendDataToOnboardSDKDevice(new byte[]{0x6F, 0x11, 0x11, 0x0D}, sendDataCallback);
                                break;

                            case 2:
                                Log.d(TAG, "C1,C2,RecordButton");
                                //给Onboard SDK发送4个字节
                                flightController.sendDataToOnboardSDKDevice(new byte[]{0x6F, 0x11, 0x11, 0x0F}, sendDataCallback);
                                break;

                            case 3:
                                Log.d(TAG, "C1,C2,Shutter,RecordButton");
                                //给Onboard SDK发送4个字节
                                flightController.sendDataToOnboardSDKDevice(new byte[]{0x6F, 0x11, 0x11, 0x0E}, sendDataCallback);
                                break;

                            case 4:
                                Log.d(TAG, "C1,ShutterButton");
                                //给Onboard SDK发送4个字节
                                flightController.sendDataToOnboardSDKDevice(new byte[]{0x6F, 0x11, 0x11, 0x09}, sendDataCallback);
                                break;
                            case 5:
                                Log.d(TAG, "C1,RecordButton");
                                //给Onboard SDK发送4个字节
                                flightController.sendDataToOnboardSDKDevice(new byte[]{0x6F, 0x11, 0x11, 0x0A}, sendDataCallback);
                                break;
                            case 6:
                                Log.d(TAG, "ShutterButton");
                                //showToast("拍照成功！");
                                //给Onboard SDK发送4个字节

                                //拍照
                                flightController.sendDataToOnboardSDKDevice(new byte[]{0x6F, 0x11, 0x11, 0x01}, sendDataCallback);
                                writetext("拍照！");
                                break;
                            case 7:
                                Log.d(TAG, "Shutter,RecordButton");
                                //给Onboard SDK发送4个字节
                                flightController.sendDataToOnboardSDKDevice(new byte[]{0x6F, 0x11, 0x11, 0x03}, sendDataCallback);
                                break;
                            case 8:
                                Log.d(TAG, "RecordButton");
//                            if (mRecordBtn.isChecked()) {
//                                mRecordBtn.setChecked(false);
//                            } else {
//                                mRecordBtn.setChecked(true);
//                            }
                                //给Onboard SDK发送4个字节
                                //录像
                                flightController.sendDataToOnboardSDKDevice(new byte[]{0x6F, 0x11, 0x11, 0x02}, sendDataCallback);
                                break;
                            case 9:
                                Log.d(TAG, "Shutter,C2Button");
                                //给Onboard SDK发送4个字节
                                flightController.sendDataToOnboardSDKDevice(new byte[]{0x6F, 0x11, 0x11, 0x05}, sendDataCallback);
                                break;
                            case 10:
                                Log.d(TAG, "C2,RecordButton");
                                //给Onboard SDK发送4个字节
                                flightController.sendDataToOnboardSDKDevice(new byte[]{0x6F, 0x11, 0x11, 0x07}, sendDataCallback);
                                break;
                            case 11:
                                Log.d(TAG, "C2,Shutter,RecordButton");
                                //给Onboard SDK发送4个字节
                                flightController.sendDataToOnboardSDKDevice(new byte[]{0x6F, 0x11, 0x11, 0x06}, sendDataCallback);
                                break;
                            default:

                                break;
                        }
                    } else {
                        showToast("No OnboardSDK Device!");
                    }
                } else {
                    showToast("No FlightController!");
                }
            }else showToast("No product!");
        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // When the compile and target version is higher than 22, please request the
        // following permissions at runtime to ensure the
        // SDK work well.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.VIBRATE,
                            Manifest.permission.INTERNET, Manifest.permission.ACCESS_WIFI_STATE,
                            Manifest.permission.WAKE_LOCK, Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.SYSTEM_ALERT_WINDOW,
                            Manifest.permission.READ_PHONE_STATE,
                    }
                    , 1);
        }
        setContentView(R.layout.activity_main);
        loginAccount();

        initUI();
        sendDataCallback=new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if(djiError!=null) {
                    showToast(djiError.getDescription());
                }
            }
        };

        mNotificationManager=(NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Register the broadcast receiver for receiving the device connection's changes.
        IntentFilter filter = new IntentFilter();
        filter.addAction(DemoApplication.FLAG_CONNECTION_CHANGE);
        registerReceiver(mReceiver, filter);

        djisdkManager=DJISDKManager.getInstance();

        //new Thread(r).start();
    }

    private void initUI(){
        final Button setting;

        setting=(Button)findViewById(R.id.settings);
        pwmOutput=(Button)findViewById(R.id.PWM);
        batteryVol=(TextView)findViewById(R.id.batteryVol);
        SqlState=(TextView)findViewById(R.id.SqlState);


        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenuDialog();
            }
        });

        pwmOutput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OutputPwm();
            }
        });

        initFlightController();
    }

    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            initFlightController();
        }
    };

    private void loginAccount(){

        UserAccountManager.getInstance().logIntoDJIUserAccount(this,
                new CommonCallbacks.CompletionCallbackWith<UserAccountState>() {
                    @Override
                    public void onSuccess(final UserAccountState userAccountState) {
                        Log.e(TAG, "Login Success");
                    }
                    @Override
                    public void onFailure(DJIError error) {
                        Log.e(TAG, "Login Error:" + error.getDescription());
                    }
                });
    }

    public void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }


    /**
     * 设置栏 AlertDialog形式 add in 2017.10.20
     * */

    public void showMenuDialog() {
        product = DemoApplication.getProductInstance();

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setIcon(R.drawable.iconofsetting);
        builder.setTitle("设置选项");
        //    通过LayoutInflater来加载一个xml的布局文件作为一个View对象
        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.popup_menu, null);
        //    设置我们自己定义的布局文件作为弹出框的Content
        builder.setView(view);


        final SeekBar seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        final TextView textView=(TextView)view.findViewById(R.id.BandWidth);
        final Switch setEXTPort = (Switch) view.findViewById(R.id.EnableEXTPort);
        final Switch setHDMIPort = (Switch) view.findViewById(R.id.EnableSecondaryOutput);

//        if (product != null) {
//            final LightbridgeLink lightbridgeLink = product.getAirLink().getLightbridgeLink();
//
//            lightbridgeLink.getEXTVideoInputPortEnabled(new CommonCallbacks.CompletionCallbackWith<Boolean>() {
//                @Override
//                public void onSuccess(Boolean aBoolean) {
//                    if(aBoolean) setEXTPort.setChecked(true);
//                    else setEXTPort.setChecked(false);
//                }
//
//                @Override
//                public void onFailure(DJIError djiError) {
//                    //showToast(djiError.getDescription());
//                }
//            });
//
//            if(lightbridgeLink.isSecondaryVideoOutputSupported()){
//                lightbridgeLink.getSecondaryVideoOutputEnabled(new CommonCallbacks.CompletionCallbackWith<Boolean>() {
//                    @Override
//                    public void onSuccess(Boolean aBoolean) {
//                        if(aBoolean) setHDMIPort.setChecked(true);
//                        else setHDMIPort.setChecked(false);
//                    }
//
//                    @Override
//                    public void onFailure(DJIError djiError) {
//                        showToast("HDMI not sup"+djiError.getDescription());
//                    }
//                });
//            }
//
//            if(!setEXTPort.isChecked()) {
//                lightbridgeLink.getBandwidthAllocationForHDMIVideoInputPort(new CommonCallbacks.CompletionCallbackWith<Float>() {
//                    @Override
//                    public void onSuccess(Float aFloat) {
//                        seekBar.setProgress((int) (100 * aFloat));
//                    }
//
//                    @Override
//                    public void onFailure(DJIError djiError) {
//                        showToast(djiError.getDescription());
//                    }
//                });
//            } else{
//                lightbridgeLink.getBandwidthAllocationForLBVideoInputPort(new CommonCallbacks.CompletionCallbackWith<Float>() {
//                    @Override
//                    public void onSuccess(Float aFloat) {
//                        seekBar.setProgress((int) (100 * aFloat));
//                    }
//
//                    @Override
//                    public void onFailure(DJIError djiError) {
//                        showToast(djiError.getDescription());
//                    }
//                });
//            }
//        }

        setEXTPort.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    if(product!=null) {
                        final LightbridgeLink lightbridgeLink = product.getAirLink().getLightbridgeLink();
                        lightbridgeLink.setEXTVideoInputPortEnabled(true, new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
                                lightbridgeLink.getBandwidthAllocationForLBVideoInputPort(new CommonCallbacks.CompletionCallbackWith<Float>() {
                                    @Override
                                    public void onSuccess(Float aFloat) {
                                        seekBar.setProgress((int) (100 * aFloat));
                                    }

                                    @Override
                                    public void onFailure(DJIError djiError) {
                                        showToast(djiError.getDescription());
                                    }
                                });
                            }
                        });
                    }
                    CharSequence str = "图传带宽  HDMI " + seekBar.getProgress() + "% : " + "EXT " + (100 - seekBar.getProgress()) + "%";
                    textView.setText(str);
                }else{
                    if(product!=null) {
                        final LightbridgeLink lightbridgeLink = product.getAirLink().getLightbridgeLink();
                        lightbridgeLink.setEXTVideoInputPortEnabled(false, new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
                                lightbridgeLink.getBandwidthAllocationForHDMIVideoInputPort(new CommonCallbacks.CompletionCallbackWith<Float>() {
                                    @Override
                                    public void onSuccess(Float aFloat) {
                                        seekBar.setProgress((int) (100 * aFloat));
                                    }

                                    @Override
                                    public void onFailure(DJIError djiError) {
                                        showToast(djiError.getDescription());
                                    }
                                });
                            }
                        });
                    }
                    CharSequence str = "图传带宽  HDMI " + seekBar.getProgress() + "% : " + "AV " + (100 - seekBar.getProgress()) + "%";
                    textView.setText(str);
                }
            }
        });


        setHDMIPort.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(product!=null) {
                    final LightbridgeLink lightbridgeLink=product.getAirLink().getLightbridgeLink();
                    if (isChecked) {
                        lightbridgeLink.setSecondaryVideoOutputPort(LightbridgeSecondaryVideoOutputPort.HDMI, new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
                                if (djiError != null)
                                    showToast(djiError.getDescription());
                            }
                        });

                        lightbridgeLink.setSecondaryVideoDisplay(LightbridgeSecondaryVideoDisplayMode.SOURCE_1_ONLY, new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
                                if (djiError != null)
                                    showToast(djiError.getDescription());
                            }
                        });

                        lightbridgeLink.setSecondaryVideoPIPPosition(LightbridgePIPPosition.BOTTOM_RIGHT, new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
                                if (djiError != null)
                                    showToast(djiError.getDescription());
                            }
                        });

                        lightbridgeLink.setSecondaryVideoOutputFormat(LightbridgeSecondaryVideoFormat.RESOLUTION_1080P_50FPS, LightbridgeSecondaryVideoOutputPort.HDMI, new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
                                if (djiError != null)
                                    showToast(djiError.getDescription());
                            }
                        });

                        lightbridgeLink.setSecondaryVideoOSDEnabled(false, new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
                                if(djiError!=null)
                                    showToast(djiError.getDescription());
//                                lightbridgeLink.setSecondaryVideoOSDTopMargin(0, new CommonCallbacks.CompletionCallback() {
//                                    @Override
//                                    public void onResult(DJIError djiError) {
//                                        if (djiError != null)
//                                            showToast(djiError.getDescription());
//                                    }
//                                });
                            }
                        });

                        lightbridgeLink.setSecondaryVideoOutputEnabled(true, new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
                                if(djiError!=null) {
                                    showToast(djiError.getDescription());
                                }
                            }
                        });

                    }else{
                        lightbridgeLink.setSecondaryVideoOutputEnabled(false, new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {

                            }
                        });
                    }
                }
            }
        });


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int Progress;
                if(progress%10>=0 && progress%10<=4) Progress=progress/10*10;
                else Progress=progress/10*10+10;
                seekBar.setProgress(Progress);
                if(!setEXTPort.isChecked()) {
                    CharSequence str = "图传带宽  HDMI " + Progress + "% : " + "AV " + (100 - Progress) + "%";
                    textView.setText(str);
                }
                else{
                    CharSequence str = "图传带宽  HDMI " + Progress + "% : " + "EXT " + (100 - Progress) + "%";
                    textView.setText(str);
                }

                float percentage=((float)Progress)/100;
                if(product!=null){
                    final LightbridgeLink lightbridgeLink=product.getAirLink().getLightbridgeLink();
                    if(setEXTPort.isChecked()){
                        lightbridgeLink.setBandwidthAllocationForLBVideoInputPort(percentage, new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
                                if(djiError!=null) showToast(djiError.getDescription());
                            }
                        });
                    }else {
                        lightbridgeLink.setBandwidthAllocationForHDMIVideoInputPort(percentage, new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
                                if (djiError != null) showToast(djiError.getDescription());
                            }
                        });
                    }
                }
                //showToast("per:"+percentage);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //float bandwidth=seekBar.getProgress();
                //float percentage=bandwidth/100;
            }
        });

        builder.show();
    }

    /**
     * 菜单栏Popup Menu  add in 2017.10.20
     * */
    public void OutputPwm(){
        PopupMenu popup = new PopupMenu(MainActivity.this,pwmOutput);
        popup.getMenuInflater().inflate(R.menu.setmenu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.pwm1:
                        handler.sendEmptyMessage(1);
                        break;
                    case R.id.pwm2:
                        handler.sendEmptyMessage(3);
                        break;
                    case R.id.pwm3:
                        handler.sendEmptyMessage(4);
                        break;
                    case R.id.pwm4:
                        handler.sendEmptyMessage(6);
                        break;
                    case R.id.pwm5:
                        handler.sendEmptyMessage(8);
                        break;
                    case R.id.pwm6:
                        handler.sendEmptyMessage(9);
                        break;
                    case R.id.pwm7:
                        handler.sendEmptyMessage(11);
                        break;
                    case R.id.record:
                        RecordVideo();
                        break;
                    case R.id.stopRecord:
                        stopRecordVideo();
                        break;
                    case R.id.displayAccount:
                        displayAccountState();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
        popup.show();
    }


    /**
     * 初始化飞行控制器
     *
     * */

    private void initFlightController(){
        product=DemoApplication.getProductInstance();
        if(product!=null && product.isConnected()){
            final FlightController flightController=((Aircraft)product).getFlightController();
            final RemoteController remoteController=((Aircraft) product).getRemoteController();
            final Gimbal gimbal=product.getGimbal();
            if (null != flightController) {
                flightController.setStateCallback(new FlightControllerState.Callback() {
                    @Override
                    public void onUpdate(@NonNull FlightControllerState flightControllerState) {
                        longitude=flightControllerState.getAircraftLocation().getLongitude();
                        latitude=flightControllerState.getAircraftLocation().getLatitude();
                        altitude=flightControllerState.getAircraftLocation().getAltitude();
                        flightpitch=flightControllerState.getAttitude().pitch;
                        flightroll=flightControllerState.getAttitude().roll;
                        flightyaw=flightControllerState.getAttitude().yaw;
                    }
                });

                flightController.setOnboardSDKDeviceDataCallback(new FlightController.OnboardSDKDeviceDataCallback() {
                    @Override
                    public void onReceive(byte[] bytes) {
                        //showToast(new String(bytes));
                        if(bytes[0]=='3'&&bytes[1]=='F'){
                            handler.sendEmptyMessage(6);
                            warning();
                            showToast("警报！最高温度超过50摄氏度"+"\n"+"经度:"+String.valueOf(longitude)+",纬度:"+String.valueOf(latitude)+"高度:"+String.valueOf(altitude));
                        }
//                        if(bytes[0]=='4'&&bytes[1]=='F'){
//                            showToast("Raspberry Pi output PWM wave successfully");
//                        }
                        if(bytes[0]=='5'&&bytes[1]=='F'){
                            handler.sendEmptyMessage(6);
                            writetext("温度降低到50℃以下");
                            showToast("最高温度降低到50摄氏度以下"+"\n"+"经度:"+String.valueOf(longitude)+",纬度:"+String.valueOf(latitude)+"高度:"+String.valueOf(altitude));
                        }
                    }
                });
            }

            if(remoteController!=null){
                remoteController.setHardwareStateCallback(new HardwareState.HardwareStateCallback() {
                    @Override
                    public void onUpdate(@NonNull HardwareState hardwareState) {
                        if (hardwareState.getC1Button().isClicked()){
                            handler.sendEmptyMessage(6);
                        }
                        if(hardwareState.getC2Button().isClicked()){
                            handler.sendEmptyMessage(8);
                        }
                    }
                });
            }

            if(gimbal!=null){
                gimbal.setStateCallback(new GimbalState.Callback() {
                    @Override
                    public void onUpdate(@NonNull GimbalState gimbalState) {
                        gimbalpitch=gimbalState.getAttitudeInDegrees().getPitch();
                        gimbalroll=gimbalState.getAttitudeInDegrees().getRoll();
                        gimbalyaw=gimbalState.getAttitudeInDegrees().getYaw();

                    }
                });
            }

            if(product.getCamera()!=null){
                product.getCamera().setSystemStateCallback(new SystemState.Callback() {
                    @Override
                    public void onUpdate(@NonNull SystemState systemState) {
                        int recordTime = systemState.getCurrentVideoRecordingTimeInSeconds();
                        int minutes = (recordTime % 3600) / 60;
                        int seconds = recordTime % 60;
                        timeStamp = String.format("%02d:%02d", minutes, seconds);
                        isVideoDataRecord = systemState.isRecording();
                    }
                });
            }

            if(product.getBattery()!=null){
                product.getBattery().setStateCallback(new BatteryState.Callback() {
                    @Override
                    public void onUpdate(BatteryState batteryState) {
                        if(batteryState!=null) {
                            int voltage = batteryState.getVoltage();
                            final int vol1 = voltage / 1000;
                            final int vol2 = (voltage-voltage/1000*1000)/100;
                            final int vol3=(voltage-voltage/100*100)/10;
                            final int vol4=(voltage-voltage/10*10);

                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    batteryVol.setText(vol1 + "." +vol2 +vol3+vol4+ "V");
                                }
                            });
                        }
                    }
                });
            }
        }
    }

    /**
     * 状态栏警告
     */
    private void warning(){
        PendingIntent pi = PendingIntent.getActivity(
                MainActivity.this,
                100,
                new Intent(MainActivity.this, MainActivity.class),
                PendingIntent.FLAG_CANCEL_CURRENT
        );
        Bitmap largeBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.appicon);
        NotificationCompat.Builder mBuilder=new NotificationCompat.Builder(MainActivity.this);
        mBuilder.setContentTitle("警告：")
                .setContentText("最高温度超过50℃")
                .setTicker("温度警告！")
                .setSmallIcon(R.drawable.appicon)
                .setLargeIcon(largeBitmap)
                .setWhen(System.currentTimeMillis())
                .setPriority(Notification.PRIORITY_HIGH)
                .setOngoing(false)
                .setAutoCancel(true)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setContentIntent(pi);
        Notification mNotification = mBuilder.build();
        int notifyId = 123;
        mNotificationManager.notify(notifyId, mNotification);
        writetext("温度超过50℃!");
    }


    /**
     * 温度超过50℃时写入DJI_Download的Warning Log文件中
     */
    private void writetext(String s){
        File destDir =
                new File(Environment.getExternalStorageDirectory().getPath() + "/DJI_Download/");
        if(!destDir.exists()){destDir.mkdir();}
        SimpleDateFormat formatter   =   new   SimpleDateFormat   ("yyyy年MM月dd日  HH:mm:ss");
        Date curDate =  new Date(System.currentTimeMillis());
        String str = formatter.format(curDate);
        String str2="-----"+"\n"+s+"  "+str+"\n"+"经度:"+String.valueOf(longitude)+"\n"+"纬度:"+String.valueOf(latitude)+"\n"+"高度:"+String.valueOf(altitude)+"\n";
        str=Environment.getExternalStorageDirectory().getPath() + "/DJI_Download/Warning Log.txt";
        File txtfile =
                new File(str);
        try {
            if (!txtfile.exists()){txtfile.createNewFile();}
            RandomAccessFile raf = new RandomAccessFile(txtfile, "rwd");
            raf.seek(txtfile.length());
            raf.write(str2.getBytes());  //将String字符串以字节流的形式写入到输出流中
            raf.close();         //关闭输出流
        }catch (Exception e){
            Toast.makeText(this, "Error!", Toast.LENGTH_SHORT).show();
        }

        //Store in SQLite Database, add in 2017.7.25
        String str1=Environment.getExternalStorageDirectory().getPath() + "/DJI_Download/Warning Log.db";
        SQLiteDatabase db= SQLiteDatabase.openOrCreateDatabase(str1,null);
        String TemperatureLog="create table if not exists TemperatureLog(_id integer primary key autoincrement,time text,longitude double,latitude double,height double,temperature text)";
        db.execSQL(TemperatureLog);
        String insertStr = "insert into TemperatureLog(time,longitude,latitude,height,temperature) values(?,?,?,?,?)";
        Object[] value=new Object[]{formatter.format(curDate),longitude,latitude,altitude,s};
        db.execSQL(insertStr,value);
        db.close();
    }


    public void addTimeStamp(String timeString){
        File destDir =
                new File(Environment.getExternalStorageDirectory().getPath() + "/DJI_Download/");
        if(!destDir.exists()) destDir.mkdir();

        String str1=Environment.getExternalStorageDirectory().getPath() + "/DJI_Download/VideoInfo.db";
        SQLiteDatabase db= SQLiteDatabase.openOrCreateDatabase(str1,null);
        String DataLog="create table if not exists DataLog(_id integer primary key autoincrement,time text,longitude double,latitude double,height double,flightPitch double,flightRoll double,flightYaw double,gimbalPitch double,gimbalRoll double,gimbalYaw double)";
        db.execSQL(DataLog);
        String insertStr = "insert into DataLog(time,longitude,latitude,height,flightPitch,flightRoll,flightYaw,gimbalPitch,gimbalRoll,gimbalYaw) values(?,?,?,?,?,?,?,?,?,?)";

        Object[] value=new Object[]{timeString,longitude,latitude,altitude,flightpitch,flightroll,flightyaw,gimbalpitch,gimbalroll,gimbalyaw};
        db.execSQL(insertStr,value);
        db.close();

    }


    private void RecordVideo(){
        final Camera camera = DemoApplication.getProductInstance().getCamera();
        if (camera != null) {
            ResolutionAndFrameRate resolutionAndFrameRate=new ResolutionAndFrameRate(SettingsDefinitions.VideoResolution.RESOLUTION_1920x1080, SettingsDefinitions.VideoFrameRate.FRAME_RATE_50_FPS);
            camera.setVideoResolutionAndFrameRate(resolutionAndFrameRate, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if(djiError!=null) showToast(djiError.getDescription());
                }
            });
            //SettingsDefinitions.CameraMode photoMode = SettingsDefinitions.CameraMode.RECORD_VIDEO;
            //camera.setMode(photoMode,null);
            camera.startRecordVideo(new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError == null) {
                        //showToast("开始录像！");
                        //new Thread(r).start();
                        //new RecordData().execute();
                        if(timer==null) timer=new Timer();
                        if(timerTask==null) timerTask=new TimerTask() {
                            @Override
                            public void run() {
                                addTimeStamp(timeStamp);
                            }
                        };
                        timer.schedule(timerTask,0,18);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                SqlState.setText("正在记录视频数据…");
                            }
                        });
                    } else {
                        showToast(djiError.getDescription());
                    }
                }
            });
        }
    }

    private void stopRecordVideo(){
        Camera camera=DemoApplication.getProductInstance().getCamera();
        if (camera != null) {
            camera.stopRecordVideo(new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError == null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                SqlState.setText("未记录视频数据");
                            }
                        });
                        stopTimer();

                    } else {
                        showToast(djiError.getDescription());
                    }
                }
            });

        }
    }

    private void displayAppState(){
        AppActivationState appState=djisdkManager.getAppActivationManager().getAppActivationState();
        AircraftBindingState aircraftBindState=djisdkManager.getAppActivationManager().getAircraftBindingState();
        showToast("APP激活状态："+appState.toString()+" 飞行器绑定状态："+aircraftBindState.toString());
    }

    private void displayAccountState(){
        final UserAccountManager accountManager=UserAccountManager.getInstance();
        accountManager.getLoggedInDJIUserAccountName(new CommonCallbacks.CompletionCallbackWith<String>() {
            @Override
            public void onSuccess(String s) {
                showToast("User:"+s+"  State:"+accountManager.getUserAccountState().toString());
            }

            @Override
            public void onFailure(DJIError djiError) {
               showToast(djiError.getDescription());
            }
        });
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

}

