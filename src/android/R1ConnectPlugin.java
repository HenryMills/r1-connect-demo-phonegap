package com.radiumone.cordova.plugin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import com.radiumone.emitter.R1Emitter;
import com.radiumone.emitter.R1EmitterLineItem;
import com.radiumone.emitter.R1SocialPermission;
import com.radiumone.emitter.location.LocationHelper;
import com.radiumone.emitter.location.LocationPreferences;
import com.radiumone.emitter.push.R1Push;
import com.radiumone.emitter.push.R1PushConfig;
import com.radiumone.emitter.push.R1PushPreferences;
import com.radiumone.geofence_sdk.R1GeofenceSDKManager;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class R1ConnectPlugin extends CordovaPlugin implements
        R1Push.OnOpenListener {

    private static final String R1_PREF = "com.radiumone.r1connect";

    static final String APPLICATION_KEY = "com.radiumone.r1connect.applicationid";
    static final String CLIENT_KEY = "com.radiumone.r1connect.clientkey";
    static final String GCM_SENDER_ID = "com.radiumone.r1connect.senderid";
    static final String DISABLE_ALL_ADV_IDS_SETTINGS_KEY = "com.radiumone.r1connect.disableallavertisingids";
    static final String COOKIE_MAPPING = "com.radiumone.r1connect.cookiemapping";
    static final String DEFERRED_DEEP_LINK_SCHEME = "com.radiumone.r1connect.deferreddeeplinkscheme";

    private static final String WRONG_PARAMETERS_COUNT = "Wrong parameters count";
    private static final String WRONG_PARAMETERS = "One of parameter is wrong";

    private Context applicationContext;

    private String applicationKey;

    private CallbackContext callbackContext;

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        applicationContext = cordova.getActivity().getApplicationContext();
        Map<String, String> params = parseConfig(applicationContext);

        applicationKey = params.get(APPLICATION_KEY);
        String clientKey = params.get(CLIENT_KEY);
        String senderId = params.get(GCM_SENDER_ID);

        String disableAllAdvertisingIds = params
                .get(DISABLE_ALL_ADV_IDS_SETTINGS_KEY);

        String cookieMapping = params
                .get(COOKIE_MAPPING);

        if (cookieMapping != null && (!TextUtils.isEmpty(cookieMapping) && "true".equals(cookieMapping))){
            R1PushPreferences.getInstance(applicationContext).cookieMappingEnable();
        } else {
            R1PushPreferences.getInstance(applicationContext).cookieMappingDisable();
        }

        String defferedDeepLinkScheme = params.get(DEFERRED_DEEP_LINK_SCHEME);
        if (!TextUtils.isEmpty(defferedDeepLinkScheme)){
            R1Emitter.getInstance().setEnableDefferedDeepLinkScheme(defferedDeepLinkScheme);
        }

        if (!TextUtils.isEmpty(disableAllAdvertisingIds)) {
            R1Emitter.getInstance().disableAllAdvertisingIds(
                    "true".equalsIgnoreCase(disableAllAdvertisingIds));
        }

        if (!TextUtils.isEmpty(senderId)) {
            R1PushConfig.getInstance(applicationContext).setSenderId(senderId,
                    true);
        }
        R1PushConfig.getInstance(applicationContext).setCredentials(
                applicationContext, applicationKey, clientKey, true);
        R1Emitter.getInstance().connect(applicationContext);
        R1Push.getInstance(applicationContext).setIntentReceiver(CordovaPushReceiver.class);
        R1Push.getInstance(this.cordova.getActivity()).setOnOpenListener(this);
        Executors.newSingleThreadScheduledExecutor().schedule(new Runnable() {
            
            @Override
            public void run() {
                if (CordovaPushReceiver.openNotification) {
                    CordovaPushReceiver.openNotification = false;
                    opened();
                }
            }
        }, 1, TimeUnit.SECONDS);
    }

    @Override
    public boolean execute(String action, JSONArray args,
            CallbackContext callbackContext) throws JSONException {
        try {
            if (callbackContext != null) {
                if (this.callbackContext == null) {
                    this.callbackContext = callbackContext;
                }
                if ("isStarted".equals(action)) {
                    PluginResult pluginResult = new PluginResult(
                            PluginResult.Status.OK, true);
                    pluginResult.setKeepCallback(true);
                    callbackContext.sendPluginResult(pluginResult);
                    return true;

                } else if ("emitter_isStarted".equals(action)) {
                    final String applicationId = R1PushConfig.getInstance(
                            applicationContext).getAppId();
                    boolean emitterStarted = !TextUtils.isEmpty(applicationId);
                    PluginResult pluginResult = new PluginResult(
                            PluginResult.Status.OK, emitterStarted);
                    pluginResult.setKeepCallback(true);
                    callbackContext.sendPluginResult(pluginResult);
                    return true;

                } else if ("push_isStarted".equals(action)) {
                    final String applicationId = R1PushConfig.getInstance(
                            applicationContext).getAppId();
                    final String clientKey = R1PushConfig.getInstance(
                            applicationContext).getClientKey();
                    final String senderId = R1PushConfig.getInstance(
                            applicationContext).getSenderId();
                    boolean pushStarted = !TextUtils.isEmpty(senderId)
                            && !TextUtils.isEmpty(applicationId)
                            && !TextUtils.isEmpty(clientKey);
                    PluginResult pluginResult = new PluginResult(
                            PluginResult.Status.OK, pushStarted);
                    pluginResult.setKeepCallback(true);
                    callbackContext.sendPluginResult(pluginResult);
                    return true;

                } else if ("push_getDeviceToken".equals(action)) {
                    final String deviceToken = R1PushConfig.getInstance(
                            applicationContext).getGCMRegistationId();
                    PluginResult pluginResult = new PluginResult(
                            PluginResult.Status.OK, deviceToken);
                    pluginResult.setKeepCallback(true);
                    callbackContext.sendPluginResult(pluginResult);
                    return true;

                } else if ("location_isEnabled".equals(action)) {

                    PluginResult pluginResult = new PluginResult(
                            PluginResult.Status.OK, LocationPreferences
                                    .getLocationPreferences(applicationContext)
                                    .isSDKLocationOn());
                    pluginResult.setKeepCallback(true);
                    callbackContext.sendPluginResult(pluginResult);
                    return true;

                } else if ("location_setEnabled".equals(action)) {

                    R1PushPreferences.getInstance(applicationContext)
                            .setLocationEnable(true);
                    callbackContext.success();
                    return true;

                } else if ("location_setDisable".equals(action)) {

                    R1PushPreferences.getInstance(applicationContext)
                            .setLocationEnable(false);
                    callbackContext.success();
                    return true;

                } else if ("location_getCoordinate".equals(action)) {
                    LocationHelper.getInstance(applicationContext)
                            .getLastKnownLocation();
                    callbackContext.success();
                    return true;
                } else if ("location_getAutoupdateTimeout".equals(action)) {
                    PluginResult pluginResult = new PluginResult(
                            PluginResult.Status.OK, LocationPreferences
                                    .getLocationPreferences(applicationContext)
                                    .getUpdateLocationTimeout());
                    pluginResult.setKeepCallback(true);
                    callbackContext.sendPluginResult(pluginResult);
                    return true;
                } else if ("location_setAutoupdateTimeout".equals(action)) {
                    if (args != null && args.length() > 0) {
                        int autoupdateTimeout = getIntFromParameter(args, 0);
                        LocationPreferences.getLocationPreferences(
                                applicationContext).setUpdateLocationTimeout(
                                autoupdateTimeout);
                        callbackContext.success();
                    } else {
                        callbackContext.error(WRONG_PARAMETERS_COUNT);
                    }
                    return true;
                } else if ("location_getState".equals(action)) {
                    callbackContext.success();
                    return true;
                } else if ("isGeofencingEnabled".equals(action)) {

                    PluginResult pluginResult = new PluginResult(
                            PluginResult.Status.OK, R1GeofenceSDKManager
                                    .getInstance().isGeofencingEnabled());
                    pluginResult.setKeepCallback(true);
                    callbackContext.sendPluginResult(pluginResult);
                    return true;

                } else if ("setGeofencingEnabled".equals(action)) {
                    if (args != null && args.length() != 1) {
                        Boolean advertisingEnabled = getBooleanFromParameter(
                                args, 0);
                        if (advertisingEnabled != null && advertisingEnabled) {
                            R1GeofenceSDKManager.getInstance().initGeofenceSDK(
                                    applicationContext, applicationKey);
                        } else {
                            R1GeofenceSDKManager.getInstance()
                                    .disableGeofencing();
                        }

                        callbackContext.success();
                    } else {
                        callbackContext.error(WRONG_PARAMETERS_COUNT);
                    }
                    return true;

                } else if ("isEngageEnabled".equals(action)) {

                    PluginResult pluginResult = new PluginResult(
                            PluginResult.Status.OK, R1Emitter.getInstance()
                                    .getEngageEnabled());
                    pluginResult.setKeepCallback(true);
                    callbackContext.sendPluginResult(pluginResult);
                    return true;

                } else if ("setEngageEnabled".equals(action)) {
                    if (args != null && args.length() != 1) {
                        Boolean engageEnabled = getBooleanFromParameter(args, 0);
                        if (engageEnabled != null) {
                            R1Emitter.getInstance().setEngageEnabled(
                                    engageEnabled);
                        } else {
                            R1Emitter.getInstance().setEngageEnabled(false);
                        }

                        callbackContext.success();
                    } else {
                        callbackContext.error(WRONG_PARAMETERS_COUNT);
                    }
                    return true;

                } else if ("setApplicationUserId".equals(action)) {

                    if (args != null && args.length() > 0) {
                        String userId = getStringFromParameter(args, 0);
                        if (!TextUtils.isEmpty(userId)) {
                            R1Emitter.getInstance()
                                    .setApplicationUserId(userId);
                        }
                        callbackContext.success();
                    } else {
                        callbackContext.error(WRONG_PARAMETERS_COUNT);
                    }
                    return true;

                } else if ("getApplicationUserId".equals(action)) {

                    PluginResult pluginResult = new PluginResult(
                            PluginResult.Status.OK, R1Emitter.getInstance()
                                    .getApplicationUserId());
                    pluginResult.setKeepCallback(true);
                    callbackContext.sendPluginResult(pluginResult);
                    return true;

                } else if ("emitter_setSessionTimeout".equals(action)) {

                    int sessionTimeout = R1Emitter.DEFAULT_SESSION_TIMEOUT;
                    if (args != null && args.length() > 0) {
                        sessionTimeout = getIntFromParameter(args, 0);
                    }
                    R1Emitter.getInstance().setSessionTimeout(sessionTimeout);
                    callbackContext.success();
                    return true;

                } else if ("emitter_getSessionTimeout".equals(action)) {

                    PluginResult pluginResult = new PluginResult(
                            PluginResult.Status.OK, R1Emitter.getInstance()
                                    .getSessionTimeout());
                    pluginResult.setKeepCallback(true);
                    callbackContext.sendPluginResult(pluginResult);
                    return true;

                } else if ("emitter_setAppName".equals(action)) {

                    if (args != null && args.length() > 0) {
                        String name = getStringFromParameter(args, 0);
                        if (!TextUtils.isEmpty(name)) {
                            R1Emitter.getInstance().setApplicationName(name);
                        }
                        callbackContext.success();
                    } else {
                        callbackContext.error(WRONG_PARAMETERS_COUNT);
                    }
                    return true;

                } else if ("emitter_getAppName".equals(action)) {

                    PluginResult pluginResult = new PluginResult(
                            PluginResult.Status.OK, R1Emitter.getInstance()
                                    .getAppName());
                    pluginResult.setKeepCallback(true);
                    callbackContext.sendPluginResult(pluginResult);
                    return true;

                } else if ("emitter_setAppId".equals(action)) {

                    if (args != null && args.length() > 0) {
                        String name = getStringFromParameter(args, 0);
                        if (!TextUtils.isEmpty(name)) {
                            R1Emitter.getInstance().setAppId(name);
                        }
                        callbackContext.success();
                    } else {
                        callbackContext.error(WRONG_PARAMETERS_COUNT);
                    }
                    return true;

                } else if ("emitter_getAppId".equals(action)) {

                    PluginResult pluginResult = new PluginResult(
                            PluginResult.Status.OK, R1Emitter.getInstance()
                                    .getAppId());
                    pluginResult.setKeepCallback(true);
                    callbackContext.sendPluginResult(pluginResult);
                    return true;

                } else if ("emitter_setAppVersion".equals(action)) {

                    if (args != null && args.length() > 0) {
                        String version = getStringFromParameter(args, 0);
                        if (!TextUtils.isEmpty(version)) {
                            R1Emitter.getInstance().setAppVersion(version);
                        }
                        callbackContext.success();
                    } else {
                        callbackContext.error(WRONG_PARAMETERS_COUNT);
                    }
                    return true;

                } else if ("emitter_getAppVersion".equals(action)) {

                    PluginResult pluginResult = new PluginResult(
                            PluginResult.Status.OK, R1Emitter.getInstance()
                                    .getAppVersion());
                    pluginResult.setKeepCallback(true);
                    callbackContext.sendPluginResult(pluginResult);
                    return true;

                } else if ("emitter_emitEvent".equals(action)) {

                    if (args == null || args.length() == 0) {
                        callbackContext.error(WRONG_PARAMETERS_COUNT);
                    } else {
                        String name = getStringFromParameter(args, 0);
                        if (!TextUtils.isEmpty(name)) {
                            HashMap<String, Object> params = new HashMap<String, Object>();
                            if (args.length() > 1) {
                                params = getMapFromParameter(args, 1);
                            }
                            R1Emitter.getInstance().emitEvent(name, params);
                            callbackContext.success();
                        } else {
                            callbackContext.error(WRONG_PARAMETERS);
                        }
                    }
                    return true;

                } else if ("emitter_emitAction".equals(action)) {
                    if (args == null || args.length() == 0) {
                        callbackContext.error(WRONG_PARAMETERS_COUNT);
                    } else {
                        final String actionString = getStringFromParameter(
                                args, 0);
                        final String label = getStringFromParameter(args, 1);
                        final long value = getLongFromParameter(args, 2);

                        HashMap<String, Object> params = null;

                        if (args.length() > 3) {
                            params = getMapFromParameter(args, 3);
                        }
                        R1Emitter.getInstance().emitAction(actionString, label,
                                value, params);

                        callbackContext.success();
                    }
                    return true;
                } else if ("emitter_emitLogin".equals(action)) {
                    if (args == null || args.length() == 0) {
                        callbackContext.error(WRONG_PARAMETERS_COUNT);
                    } else {
                        final String userId = getStringFromParameter(args, 0);
                        final String userName = getStringFromParameter(args, 1);
                        HashMap<String, Object> params = null;
                        if (args.length() > 2) {
                            params = getMapFromParameter(args, 2);
                        }
                        R1Emitter.getInstance().emitLogin(userId, userName,
                                params);
                        callbackContext.success();
                    }
                    return true;
                } else if ("emitter_emitRegistration".equals(action)) {

                    if (args == null || args.length() == 0) {
                        callbackContext.error(WRONG_PARAMETERS_COUNT);
                    } else {
                        String userId = getStringFromParameter(args, 0);
                        String userName = getStringFromParameter(args, 1);
                        String country = getStringFromParameter(args, 2);
                        String state = getStringFromParameter(args, 3);
                        String city = getStringFromParameter(args, 4);
                        HashMap<String, Object> params = null;
                        if (args.length() > 5) {
                            params = getMapFromParameter(args, 5);
                        }
                        R1Emitter.getInstance().emitRegistration(userId,
                                userName, country, state, city, params);
                        callbackContext.success();
                    }
                    return true;

                } else if ("emitter_emitUserInfo".equals(action)) {

                    if (args == null || args.length() == 0) {
                        callbackContext.error(WRONG_PARAMETERS_COUNT);
                    } else {
                        HashMap<String, Object> userData = getMapFromParameter(
                                args, 0);

                        R1Emitter.UserItem userItem = new R1Emitter.UserItem();

                        userItem.userId = (String) userData.get("userID");
                        userItem.userName = (String) userData.get("userName");
                        userItem.email = (String) userData.get("email");
                        userItem.firstName = (String) userData.get("firstName");
                        userItem.lastName = (String) userData.get("lastName");
                        userItem.streetAddress = (String) userData
                                .get("streetAddress");
                        userItem.phone = (String) userData.get("phone");
                        userItem.city = (String) userData.get("city");
                        userItem.state = (String) userData.get("state");
                        userItem.zip = (String) userData.get("zip");

                        HashMap<String, Object> params = null;
                        if (args.length() > 1) {
                            params = getMapFromParameter(args, 1);
                        }
                        R1Emitter.getInstance().emitUserInfo(userItem, params);
                        callbackContext.success();
                    }
                    return true;

                } else if ("emitter_emitFBConnect".equals(action)) {

                    if (args == null || args.length() == 0) {
                        callbackContext.error(WRONG_PARAMETERS_COUNT);
                    } else {
                        List<R1SocialPermission> permissionList = getSocialPermissionListFromParameter(
                                args, 0);

                        HashMap<String, Object> params = null;
                        if (args.length() > 1) {
                            params = getMapFromParameter(args, 1);
                        }
                        R1Emitter.getInstance().emitFBConnect(permissionList,
                                params);
                        callbackContext.success();
                    }
                    return true;

                } else if ("emitter_emitTConnect".equals(action)) {

                    if (args == null || args.length() == 0) {
                        callbackContext.error(WRONG_PARAMETERS_COUNT);
                    } else {
                        final String userId = getStringFromParameter(args, 0);
                        final String userName = getStringFromParameter(args, 1);

                        List<R1SocialPermission> permissionList = getSocialPermissionListFromParameter(
                                args, 2);

                        HashMap<String, Object> params = null;
                        if (args.length() > 3) {
                            params = getMapFromParameter(args, 3);
                        }
                        R1Emitter.getInstance().emitTConnect(userId, userName,
                                permissionList, params);
                        callbackContext.success();
                    }
                    return true;

                } else if ("emitter_emitTransaction".equals(action)) {

                    if (args == null || args.length() == 0) {
                        callbackContext.error(WRONG_PARAMETERS_COUNT);
                    } else {
                        final R1Emitter.EmitItem emitItem = new R1Emitter.EmitItem();
                        emitItem.transactionId = getStringFromParameter(args, 0);
                        emitItem.storeId = getStringFromParameter(args, 1);
                        emitItem.storeName = getStringFromParameter(args, 2);
                        emitItem.cartId = getStringFromParameter(args, 3);
                        emitItem.orderId = getStringFromParameter(args, 4);
                        emitItem.totalSale = getFloatFromParameter(args, 5);
                        emitItem.currency = getStringFromParameter(args, 6);
                        emitItem.shippingCosts = getFloatFromParameter(args, 7);
                        emitItem.transactionTax = getFloatFromParameter(args, 8);

                        HashMap<String, Object> params = null;
                        if (args.length() > 9) {
                            params = getMapFromParameter(args, 9);
                        }
                        R1Emitter.getInstance().emitTransaction(emitItem,
                                params);
                        callbackContext.success();
                    }
                    return true;

                } else if ("emitter_emitTransactionItem".equals(action)) {

                    if (args == null || args.length() == 0) {
                        callbackContext.error(WRONG_PARAMETERS_COUNT);
                    } else {

                        final String transactionId = getStringFromParameter(
                                args, 0);
                        HashMap<String, Object> lineItemMap = getMapFromParameter(
                                args, 1);
                        R1EmitterLineItem lineItem = null;
                        if (lineItemMap != null) {
                            lineItem = new R1EmitterLineItem();
                            lineItem.productId = getStringFromMap(lineItemMap,
                                    "productID");
                            lineItem.productName = getStringFromMap(
                                    lineItemMap, "productName");
                            lineItem.quantity = getIntFromMap(lineItemMap,
                                    "quantity");
                            lineItem.unitOfMeasure = getStringFromMap(
                                    lineItemMap, "unitOfMeasure");
                            lineItem.msrPrice = getFloatFromMap(lineItemMap,
                                    "msrPrice");
                            lineItem.pricePaid = getFloatFromMap(lineItemMap,
                                    "pricePaid");
                            lineItem.currency = getStringFromMap(lineItemMap,
                                    "currency");
                            lineItem.itemCategory = getStringFromMap(
                                    lineItemMap, "itemCategory");
                        }
                        HashMap<String, Object> params = null;
                        if (args.length() > 2) {
                            params = getMapFromParameter(args, 2);
                        }
                        R1Emitter.getInstance().emitTransactionItem(
                                transactionId, lineItem, params);
                        callbackContext.success();
                    }
                    return true;

                } else if ("emitter_emitCartCreate".equals(action)) {

                    if (args == null || args.length() == 0) {
                        callbackContext.error(WRONG_PARAMETERS_COUNT);
                    } else {
                        String cartId = getStringFromParameter(args, 0);
                        HashMap<String, Object> params = null;
                        if (args.length() > 1) {
                            params = getMapFromParameter(args, 1);
                        }
                        R1Emitter.getInstance().emitCartCreate(cartId, params);
                        callbackContext.success();
                    }
                    return true;

                } else if ("emitter_emitCartDelete".equals(action)) {

                    if (args == null || args.length() == 0) {
                        callbackContext.error(WRONG_PARAMETERS_COUNT);
                    } else {
                        String cartId = getStringFromParameter(args, 0);
                        HashMap<String, Object> params = null;
                        if (args.length() > 1) {
                            params = getMapFromParameter(args, 1);
                        }
                        R1Emitter.getInstance().emitCartDelete(cartId, params);
                        callbackContext.success();
                    }
                    return true;

                } else if ("emitter_emitAddToCart".equals(action)) {

                    if (args == null || args.length() == 0) {
                        callbackContext.error(WRONG_PARAMETERS_COUNT);
                    } else {
                        final String cartId = getStringFromParameter(args, 0);
                        HashMap<String, Object> lineItemMap = getMapFromParameter(
                                args, 1);
                        R1EmitterLineItem lineItem = null;
                        if (lineItemMap != null) {
                            lineItem = new R1EmitterLineItem();
                            lineItem.productId = getStringFromMap(lineItemMap,
                                    "productID");
                            lineItem.productName = getStringFromMap(
                                    lineItemMap, "productName");
                            lineItem.quantity = getIntFromMap(lineItemMap,
                                    "quantity");
                            lineItem.unitOfMeasure = getStringFromMap(
                                    lineItemMap, "unitOfMeasure");
                            lineItem.msrPrice = getFloatFromMap(lineItemMap,
                                    "msrPrice");
                            lineItem.pricePaid = getFloatFromMap(lineItemMap,
                                    "pricePaid");
                            lineItem.currency = getStringFromMap(lineItemMap,
                                    "currency");
                            lineItem.itemCategory = getStringFromMap(
                                    lineItemMap, "itemCategory");
                        }
                        HashMap<String, Object> params = null;
                        if (args.length() > 2) {
                            params = getMapFromParameter(args, 2);
                        }
                        R1Emitter.getInstance().emitAddToCart(cartId, lineItem,
                                params);
                        callbackContext.success();
                    }
                    return true;

                } else if ("emitter_emitDeleteFromCart".equals(action)) {

                    if (args == null || args.length() == 0) {
                        callbackContext.error(WRONG_PARAMETERS_COUNT);
                    } else {
                        final String cartId = getStringFromParameter(args, 0);
                        HashMap<String, Object> lineItemMap = getMapFromParameter(
                                args, 1);
                        R1EmitterLineItem lineItem = null;
                        if (lineItemMap != null) {
                            lineItem = new R1EmitterLineItem();
                            lineItem.productId = getStringFromMap(lineItemMap,
                                    "productID");
                            lineItem.productName = getStringFromMap(
                                    lineItemMap, "productName");
                            lineItem.quantity = getIntFromMap(lineItemMap,
                                    "quantity");
                            lineItem.unitOfMeasure = getStringFromMap(
                                    lineItemMap, "unitOfMeasure");
                            lineItem.msrPrice = getFloatFromMap(lineItemMap,
                                    "msrPrice");
                            lineItem.pricePaid = getFloatFromMap(lineItemMap,
                                    "pricePaid");
                            lineItem.currency = getStringFromMap(lineItemMap,
                                    "currency");
                            lineItem.itemCategory = getStringFromMap(
                                    lineItemMap, "itemCategory");
                        }
                        HashMap<String, Object> params = null;
                        if (args.length() > 2) {
                            params = getMapFromParameter(args, 2);
                        }
                        R1Emitter.getInstance().emitRemoveFromCart(cartId,
                                lineItem, params);
                        callbackContext.success();
                    }
                    return true;
                } else if ("emitter_emitUpgrade".equals(action)) {

                    HashMap<String, Object> params = null;
                    if (args != null && args.length() > 0) {
                        params = getMapFromParameter(args, 0);
                    }
                    R1Emitter.getInstance().emitUpgrade(params);
                    callbackContext.success();
                    return true;

                } else if ("emitter_emitTrialUpgrade".equals(action)) {

                    HashMap<String, Object> params = null;
                    if (args != null && args.length() > 0) {
                        params = getMapFromParameter(args, 0);
                    }
                    R1Emitter.getInstance().emitTrialUpgrade(params);
                    callbackContext.success();
                    return true;

                } else if ("emitter_emitScreenView".equals(action)) {

                    if (args == null || args.length() == 0) {
                        callbackContext.error(WRONG_PARAMETERS_COUNT);
                    } else {
                        final String title = getStringFromParameter(args, 0);
                        final String descript = getStringFromParameter(args, 1);
                        final String locationUrl = getStringFromParameter(args,
                                2);
                        final String hostName = getStringFromParameter(args, 3);
                        final String path = getStringFromParameter(args, 4);

                        HashMap<String, Object> params = null;
                        if (args.length() > 5) {
                            params = getMapFromParameter(args, 5);
                        }
                        R1Emitter.getInstance().emitAppScreen(title, descript,
                                locationUrl, hostName, path, params);
                        callbackContext.success();
                    }
                    return true;

                } else if ("push_setPushEnabled".equals(action)) {

                    boolean pushEnabled = true;

                    if (args.length() > 0) {
                        pushEnabled = getBooleanFromParameter(args, 0);
                    }
                    if (pushEnabled) {
                        R1PushPreferences.getInstance(applicationContext)
                                .pushEnable();
                    } else {
                        R1PushPreferences.getInstance(applicationContext)
                                .pushDisable();
                    }
                    callbackContext.success();
                    return true;

                } else if ("push_isPushEnabled".equals(action)) {

                    PluginResult pluginResult = new PluginResult(
                            PluginResult.Status.OK, R1PushPreferences
                                    .getInstance(applicationContext)
                                    .isPushEnabled());
                    pluginResult.setKeepCallback(true);
                    callbackContext.sendPluginResult(pluginResult);
                    return true;

                } else if ("push_setTags".equals(action)) {
                    if (args == null || args.length() == 0) {
                        callbackContext.error(WRONG_PARAMETERS_COUNT);
                    } else {

                        JSONArray newTags = getJsonArrayFromParameter(args, 0);
                        ArrayList<String> tags = new ArrayList<String>();
                        if (newTags != null && newTags.length() > 0) {
                            try {
                                int arrayLength = newTags.length();
                                if (arrayLength > 0) {
                                    for (int i = 0; i < arrayLength; i++) {
                                        String tag = newTags.getString(i);
                                        tags.add(tag);
                                    }
                                }
                            } catch (JSONException ex) {
                                ex.printStackTrace();
                                callbackContext.error("wrong parameters");
                                return true;
                            }
                        }
                        R1Push.getInstance(applicationContext).setTags(tags);
                        callbackContext.success();
                    }
                    return true;

                } else if ("push_addTag".equals(action)) {

                    if (args == null || args.length() == 0) {
                        callbackContext.error(WRONG_PARAMETERS_COUNT);
                    } else {
                        String tag = getStringFromParameter(args, 0);
                        if (!TextUtils.isEmpty(tag)) {
                            R1Push.getInstance(applicationContext).addTag(tag);
                        }
                        callbackContext.success();
                    }
                    return true;

                } else if ("push_removeTag".equals(action)) {

                    if (args == null || args.length() == 0) {
                        callbackContext.error(WRONG_PARAMETERS_COUNT);
                    } else {
                        String tag = getStringFromParameter(args, 0);
                        if (!TextUtils.isEmpty(tag)) {
                            R1Push.getInstance(applicationContext).removeTag(
                                    tag);
                        }
                        callbackContext.success();
                    }
                    return true;

                } else if ("push_getTags".equals(action)) {

                    String[] tags = R1Push.getInstance(applicationContext)
                            .getTags(applicationContext);
                    JSONArray array = new JSONArray();
                    if (tags != null && tags.length > 0) {
                        for (String tag : tags) {
                            array.put(tag);
                        }
                    }

                    PluginResult pluginResult = new PluginResult(
                            PluginResult.Status.OK, array);
                    pluginResult.setKeepCallback(true);
                    callbackContext.sendPluginResult(pluginResult);
                    return true;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
        R1Emitter.getInstance().onStart(applicationContext);
        R1Push.getInstance(this.cordova.getActivity()).setOnOpenListener(this);
        CordovaPushReceiver.onBackground = false;
        if (CordovaPushReceiver.openNotification) {
            CordovaPushReceiver.openNotification = false;
            opened();
        }
    }

    @Override
    public void onPause(boolean multitasking) {
        super.onPause(multitasking);
        R1Emitter.getInstance().onStop(applicationContext);
        R1Push.getInstance(this.cordova.getActivity()).setOnOpenListener(null);
        CordovaPushReceiver.onBackground = true;
    }

    private Map<String, String> parseConfig(Context application) {

        HashMap<String, String> configMap = new HashMap<String, String>();

        int id = application.getResources().getIdentifier("config", "xml",
                application.getPackageName());
        if (id == 0) {
            return configMap;
        }

        XmlResourceParser xml = application.getResources().getXml(id);
        if (xml != null) {
            int eventType = -1;
            while (eventType != XmlResourceParser.END_DOCUMENT) {

                if (eventType == XmlResourceParser.START_TAG) {
                    if (("preference").equals(xml.getName())) {
                        String name = xml.getAttributeValue(null, "name");
                        if (name != null) {
                            name = name.toLowerCase();
                        }
                        String value = xml.getAttributeValue(null, "value");

                        if (value != null && name != null
                                && name.startsWith(R1_PREF)) {
                            configMap.put(name, value);
                        }
                    }
                }

                try {
                    eventType = xml.next();
                } catch (Exception e) {

                }
            }
        }
        return configMap;
    }

    private Object getObjectFromParameter(JSONArray params, int index) {
        if (params == null || params.length() >= index) {
            return new HashMap<String, Object>();
        }
        Object obj = null;

        try {
            obj = params.get(index);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return obj;
    }

    private HashMap<String, Object> getMapFromParameter(JSONArray params,
            int index) {
        HashMap<String, Object> map = null;
        if (params == null || params.length() <= index) {
            return null;
        }

        try {
            Object object = params.get(index);
            if (object instanceof JSONObject) {
                map = new HashMap<String, Object>();
                JSONObject jsonObject = (JSONObject) object;
                fillMapFromJson(map, jsonObject);
                return map;
            } else if (object instanceof String && !"null".equals(object)) {
                map = new HashMap<String, Object>();
                String jsonObjectString = (String) object;
                JSONObject jsonObject = new JSONObject(jsonObjectString);
                fillMapFromJson(map, jsonObject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return map;
    }

    private void fillMapFromJson(HashMap<String, Object> map,
            JSONObject jsonObject) throws JSONException {
        Iterator keys = jsonObject.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            Object value = jsonObject.get(key);
            if (!(value instanceof JSONObject)) {
                map.put(key, value);
            }
        }
    }

    private float getFloatFromMap(HashMap<String, Object> lineItem, String key) {
        Object floatParamObject = lineItem.get(key);
        if (floatParamObject instanceof Double) {
            return ((Double) floatParamObject).floatValue();
        } else if (floatParamObject instanceof Float) {
            return (Float) floatParamObject;
        } else if (floatParamObject instanceof Integer) {
            return ((Integer) floatParamObject).floatValue();
        } else if (floatParamObject instanceof Long) {
            return ((Long) floatParamObject).floatValue();
        } else if (floatParamObject instanceof String) {
            try {
                return Float.parseFloat((String) floatParamObject);
            } catch (NumberFormatException ex) {
            }
        }
        return 0f;
    }

    private int getIntFromMap(HashMap<String, Object> lineItem, String key) {
        Object intObject = lineItem.get(key);
        if (intObject instanceof Integer) {
            return (Integer) intObject;
        } else if (intObject instanceof Float) {
            return ((Float) intObject).intValue();
        } else if (intObject instanceof Long) {
            return ((Long) intObject).intValue();
        } else if (intObject instanceof Double) {
            return ((Double) intObject).intValue();
        } else if (intObject instanceof String) {
            try {
                return Integer.parseInt((String) intObject);
            } catch (NumberFormatException ex) {
            }
        }
        return 0;
    }

    private String getStringFromMap(Map<String, Object> lineItem, String key) {
        Object stringObject = lineItem.get(key);
        if (stringObject instanceof String) {
            return (String) stringObject;
        } else {
            String s = String.valueOf(stringObject);
            if ("null".equals(s)) {
                return null;
            }
            return s;
        }
    }

    private Long getLongFromParameter(JSONArray params, int index) {
        if (params == null || params.length() <= index) {
            return 0l;
        }
        try {
            Object longParamObject = params.get(index);
            if (longParamObject instanceof Long) {
                return (Long) longParamObject;
            } else if (longParamObject instanceof Float) {
                return ((Float) longParamObject).longValue();
            } else if (longParamObject instanceof Integer) {
                return ((Integer) longParamObject).longValue();
            } else if (longParamObject instanceof Double) {
                return ((Double) longParamObject).longValue();
            } else if (longParamObject instanceof String) {
                try {
                    return Long.parseLong((String) longParamObject);
                } catch (NumberFormatException ex) {
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return 0l;
    }

    private Integer getIntFromParameter(JSONArray params, int index) {
        if (params == null || params.length() <= index) {
            return 0;
        }
        try {
            Object intParamObject = params.get(index);
            if (intParamObject instanceof Integer) {
                return (Integer) intParamObject;
            } else if (intParamObject instanceof Float) {
                return ((Float) intParamObject).intValue();
            } else if (intParamObject instanceof Long) {
                return ((Long) intParamObject).intValue();
            } else if (intParamObject instanceof Double) {
                return ((Double) intParamObject).intValue();
            } else if (intParamObject instanceof String) {
                try {
                    return Integer.parseInt((String) intParamObject);
                } catch (NumberFormatException ex) {
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return 0;
    }

    private Double getDoubleFromParameter(JSONArray params, int index) {
        if (params == null || params.length() <= index) {
            return 0d;
        }
        try {
            Object doubleParamObject = params.get(index);
            if (doubleParamObject instanceof Double) {
                return (Double) doubleParamObject;
            } else if (doubleParamObject instanceof Float) {
                return ((Float) doubleParamObject).doubleValue();
            } else if (doubleParamObject instanceof Integer) {
                return ((Integer) doubleParamObject).doubleValue();
            } else if (doubleParamObject instanceof Long) {
                return ((Long) doubleParamObject).doubleValue();
            } else if (doubleParamObject instanceof String) {
                try {
                    return Double.parseDouble((String) doubleParamObject);
                } catch (NumberFormatException ex) {
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return 0d;
    }

    private Float getFloatFromParameter(JSONArray params, int index) {
        if (params == null || params.length() <= index) {
            return 0f;
        }
        try {
            Object floatParamObject = params.get(index);
            if (floatParamObject instanceof Double) {
                return ((Double) floatParamObject).floatValue();
            } else if (floatParamObject instanceof Float) {
                return (Float) floatParamObject;
            } else if (floatParamObject instanceof Integer) {
                return ((Integer) floatParamObject).floatValue();
            } else if (floatParamObject instanceof Long) {
                return ((Long) floatParamObject).floatValue();
            } else if (floatParamObject instanceof String) {
                try {
                    return Float.parseFloat((String) floatParamObject);
                } catch (NumberFormatException ex) {
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return 0f;
    }

    private boolean getBooleanFromParameter(JSONArray params, int index) {
        if (params == null || params.length() <= index) {
            return false;
        }

        try {
            Object booleanParamObject = params.get(index);
            if (booleanParamObject instanceof Boolean) {
                return (Boolean) booleanParamObject;
            } else if (booleanParamObject instanceof String) {
                if ("true".equalsIgnoreCase((String) booleanParamObject)) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;
    }

    private List<R1SocialPermission> getSocialPermissionListFromParameter(
            JSONArray params, int index) {
        List<R1SocialPermission> list = null;
        if (params == null || params.length() <= index) {
            return null;
        }

        Object permissionsObject = null;
        try {
            permissionsObject = params.get(index);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        if (permissionsObject instanceof JSONArray) {
            try {
                JSONArray array = (JSONArray) permissionsObject;
                int size = array.length();
                if (size > 0) {
                    list = new ArrayList<R1SocialPermission>();
                    for (int i = 0; i < size; i++) {
                        JSONObject object = array.getJSONObject(i);
                        if (object != null) {
                            final R1SocialPermission permission = new R1SocialPermission();
                            permission.name = object.getString("name");
                            permission.granted = object.getBoolean("granted");
                            list.add(permission);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            String paramString = getStringFromParameter(params, index);
            if (!TextUtils.isEmpty(paramString)) {
                try {
                    JSONArray array = new JSONArray(paramString);
                    int size = array.length();
                    if (size > 0) {
                        list = new ArrayList<R1SocialPermission>();
                        for (int i = 0; i < size; i++) {
                            JSONObject object = array.getJSONObject(i);
                            if (object != null) {
                                final R1SocialPermission permission = new R1SocialPermission();
                                permission.name = object.getString("name");
                                permission.granted = object
                                        .getBoolean("granted");
                                list.add(permission);
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }

        return list;
    }

    private String getStringFromParameter(JSONArray params, int index) {
        if (params == null || params.length() <= index) {
            return null;
        }
        Object stringParamObject = null;
        try {
            stringParamObject = params.get(index);
            if (stringParamObject instanceof String) {
                return (String) stringParamObject;
            } 
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private JSONArray getJsonArrayFromParameter(JSONArray params, int index) {
        if (params == null || params.length() <= index) {
            return new JSONArray();
        }

        try {
            return params.getJSONArray(index);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONArray();
    }
    
    private JSONObject getJsonFromParameter(JSONArray params, int index) {
        if (params == null || params.length() <= index) {
            return new JSONObject();
        }

        try {
            return params.getJSONObject(index);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }

    @Override
    public void opened() {
        if (this.cordova != null) {
            Activity activity = this.cordova.getActivity();
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        final Intent lastPushIntent = R1Push.getInstance(
                                applicationContext).getLastNotificationIntent();

                        if (lastPushIntent != null) {
                            Bundle extras = lastPushIntent.getExtras();
                            if (extras != null) {
                                final Set<String> keys = extras.keySet();
                                if (keys != null) {
                                    JSONObject keyValues = new JSONObject();
                                    for (String key : keys) {
                                        Object value = extras.get(key);
                                        if (value instanceof String) {
                                            try {
                                                keyValues.put(key, value);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                    String json = "null";
                                    if (keyValues != null) {
                                        json = keyValues.toString();
                                    }
                                    String js = String
                                            .format("cordova.fireDocumentEvent('%s.%s', %s);",
                                                    "R1Push",
                                                    "backgroundNotification",
                                                    json);
                                    evalJs(js);
                                }
                            }
                        }
                    }

                    private void evalJs(String js) {
                        if (webView != null) {
                            if (Build.VERSION.SDK_INT > 18) {
                                webView.evaluateJavascript(js, null);
                            } else {
                                webView.loadUrl("javascript:" + js);
                            }
                        }
                    }
                });
            }
        }
    }

}
