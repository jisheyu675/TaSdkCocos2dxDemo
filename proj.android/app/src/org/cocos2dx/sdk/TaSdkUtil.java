package org.cocos2dx.sdk;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.aly.account.ALYLogin;
import com.aly.analysis.basicdata.conversion.AFConversionDataResultListener;
import com.aly.analysis.basicdata.payuserlayer.PayUserLayerDataListener;
import com.aly.sdk.ALYAnalysis;
import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.cocos2dx.cpp.AppActivity;
import org.cocos2dx.cpp.JavaHelper;

import java.util.HashMap;
import java.util.Map;

public class TaSdkUtil {
    private final static String TAG = "====>taSdk cocos demo";
    private static Context context = AppActivity.getContext();
    private static Boolean isInit = false;


    public static void initTaSdk(String productId,String channelId){

        ALYAnalysis.enalbeDebugMode(true);
        ALYAnalysis.init(context.getApplicationContext(), productId, channelId, new ALYAnalysis.TasdkinitializdListener() {
            @Override
            public void onSuccess(String userid) {
                Log.i(TAG, "init success userId is   " + userid);
                initAf();
                isInit = true;
                JavaHelper.TaSdkInitSuccess(userid);
            }

            @Override
            public void onFail(String errorMsg) {
                Log.i(TAG, "init error  " + errorMsg);
                JavaHelper.TaSdkInitFail(errorMsg);
            }
        });

        //在线时长上报
        ALYAnalysis.openAlyAutoDuration(context);

    }


    public static void eventLog(){
        if (!isInit) {
            Toast.makeText(context, "taSdk没有初始化完成", Toast.LENGTH_SHORT).show();
            return;
        }
        ALYAnalysis.log("game_start");
        ALYAnalysis.log("game_resume", "duration:30000");

        HashMap<String, String> map = new HashMap<>();
        map.put("name", "Jack.Lin");
        map.put("level", "35");
        map.put("star", "4");
        ALYAnalysis.log("game_level_pass", map);

        ALYAnalysis.count("game_level_pass");

    }

    public static void login(){
        if (!isInit) {
            Toast.makeText(context, "taSdk没有初始化完成", Toast.LENGTH_SHORT).show();
            return;
        }
        ALYLogin.guestLogin("playerId");
    }

    public static void payLog(){

        if (!isInit) {
            Toast.makeText(context, "taSdk没有初始化完成", Toast.LENGTH_SHORT).show();
            return;
        }
        ALYAnalysis.getPayUserLayerData(new PayUserLayerDataListener() {
            @Override
            public void onSuccess(String s) {
                Log.i(TAG, "onSuccess: PayUserLayer :"+s);
                JavaHelper.PayUserLayerSuccess(s);
            }

            @Override
            public void onFail(String s) {
                Log.i(TAG, "onFail: PayUserLayer :"+s);
                JavaHelper.PayUserLayerFail(s);

            }
        });

    }
    public static void firebaseTransfer(){

        if (!isInit) {
            Toast.makeText(context, "taSdk没有初始化完成", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<FirebaseAnalytics.ConsentType, FirebaseAnalytics.ConsentStatus> consentMap = new HashMap<FirebaseAnalytics.ConsentType, FirebaseAnalytics.ConsentStatus>();
        consentMap.put(FirebaseAnalytics.ConsentType.ANALYTICS_STORAGE, FirebaseAnalytics.ConsentStatus.GRANTED);
        FirebaseAnalytics.getInstance(context).setConsent(consentMap);
        FirebaseAnalytics.getInstance(context).getAppInstanceId().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<String> task) {
                if (task.isSuccessful()) {
                    String firebaseId = task.getResult();
                    ALYAnalysis.setFirebaseId(firebaseId);
                }
            }
        });

    }

    private static void initAf(){
        AppsFlyerLib appsflyer = AppsFlyerLib.getInstance();
        // Make sure you remove the following line when building to production
        appsflyer.setDebugLog(true);
        AppsFlyerConversionListener conversionListener = new AppsFlyerConversionListener() {
            @Override
            public void onConversionDataSuccess(Map<String, Object> conversionData) {
                //获取 afid
                String afId=AppsFlyerLib.getInstance().getAppsFlyerUID(context);
                // 将afid赋值给统计包
                ALYAnalysis.setAFId(afId);

                ALYAnalysis.getConversionData(conversionData, new AFConversionDataResultListener() {
                    @Override
                    public void onSuccess(String data) {
                        Log.i("tag", "onSuccess: "+data); // 其中data就是用户分级的返回值
                    }

                    @Override
                    public void onFail(String s) {
                        Log.i("tag", "onFail: "+s);
                    }
                });
            }

            @Override
            public void onConversionDataFail(String errorMessage) {
                // no need
            }

            @Override
            public void onAppOpenAttribution(Map<String, String> conversionData) {
                // no need
            }

            @Override
            public void onAttributionFailure(String errorMessage) {
                // no need
            }
        };

        // 获取openId
        String openId=ALYAnalysis.getOpenId();

        // 将userId赋值给AppsFlyer
        AppsFlyerLib.getInstance().setCustomerUserId(openId);

        appsflyer.init("gpXCNEALoPocvpxkn5r7iT", null, context);
        appsflyer.startTracking((Application) context.getApplicationContext());

    }

}
