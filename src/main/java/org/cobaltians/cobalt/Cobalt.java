/**
 *
 * Cobalt
 * Cobalt
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Cobaltians
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

package org.cobaltians.cobalt;

import org.cobaltians.cobalt.activities.CobaltActivity;
import org.cobaltians.cobalt.customviews.BottomBar;
import org.cobaltians.cobalt.fragments.CobaltFragment;
import org.cobaltians.cobalt.plugin.CobaltAbstractPlugin;
import org.cobaltians.cobalt.pubsub.PubSub;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.util.TypedValue;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;

import junit.framework.Assert;

import org.cobaltians.cobalt.pubsub.PubSubInterface;
import org.json.JSONException;
import org.json.JSONObject;

public class Cobalt {

    // TAG
	public static final String TAG = Cobalt.class.getSimpleName();

    // DEBUG
    public static boolean DEBUG = true;

    // RESOURCES
    private static final String ASSETS_PATH = "file:///android_asset/";

    // INFINITE SCROLL
    public static final int INFINITE_SCROLL_OFFSET_DEFAULT_VALUE = 0;

    // DEFAULT COLOR VALUES
    // Background color view
    public static final String BACKGROUND_COLOR_DEFAULT = "#FFFFFF";
    public static final int BAR_BACKGROUND_COLOR_DEFAULT_VALUE = 0xFF212121;
    public static final int BAR_ICON_COLOR_DEFAULT_VALUE = Color.WHITE;
    public static final int BAR_TEXT_COLOR_DEFAULT_VALUE = Color.WHITE;

    /**********************************************************************************************
     * CONFIGURATION FILE
     **********************************************************************************************/

    public final static String CONF_FILE = "cobalt.json";
    private final static String kControllers = "controllers";
    private final static String kPlugins = "plugins";
    private final static String kAndroid = "android";
    private final static String kDefaultController = "default";

    public final static String kBars = "bars";
    public final static String kBarsVisible = "visible";
    public final static String kVisibleTop = "top";
    public final static String kVisibleBottom = "bottom";
    public final static String kBackgroundColor = "backgroundColor";
    public final static String kBarsColor = "color";
    public final static String kBarsTitle = "title";
    public final static String kBarsIcon = "androidIcon";
    public final static String kBarsNavigationIcon = "androidNavigationIcon";
    public final static String kNavigationIconEnabled = "enabled";
    public final static String kNavigationIconIcon = "icon";
    public final static String kBarsActions = "actions";
    public final static String kActionActions = "actions";
    public final static String kActionName = "name";
    public final static String kActionTitle = "title";
    public final static String kActionPosition = "androidPosition";
    public final static String kPositionTop = "top";
    public final static String kPositionOverflow = "overflow";
    public final static String kPositionBottom = "bottom";
    public final static String kActionIcon = "icon";
    public final static String kActionAndroidIcon = "androidIcon";
    public final static String kActionColor = "color";
    public final static String kActionVisible = "visible";
    public final static String kActionEnabled = "enabled";
    public final static String kActionBadge = "badge";

    public final static String kExtras = "cobalt";
    public final static String kController = "controller";
    public final static String kPage = "page";
    public final static String kActivity = "activity";
    public final static String kPopAsModal = "popAsModal";
    public final static String kPushAsModal = "pushAsModal";
    public final static String kPullToRefresh = "pullToRefresh";
    public final static String kInfiniteScroll = "infiniteScroll";
    public final static String kInfiniteScrollOffset = "infiniteScrollOffset";
    public final static String kSwipe = "swipe";

    /**********************************************************************************************
     * JS KEYWORDS
     **********************************************************************************************/

    // GENERAL
    public final static String kJSAction = "action";
    public final static String kJSCallback = "callback";
    public final static String kJSCallbackChannel = "callbackChannel";
    public final static String kJSData = "data";
    public final static String kJSMessage = "message";
    public final static String kJSPage = "page";
    public final static String kJSType = "type";
    public final static String kJSValue = "value";
    public final static String kJSVersion = "version";

    // CALLBACKS
    public final static String JSTypeCallBack = "callback";

    // COBALT IS READY
    public final static String JSTypeCobaltIsReady = "cobaltIsReady";

    // EVENTS
    public final static String JSTypeEvent = "event";
    public final static String kJSEvent = "event";

    // APP EVENTS
    public final static String JSEventOnAppStarted = "cobalt:onAppStarted";
    public final static String JSEventOnAppBackground = "cobalt:onAppBackground";
    public final static String JSEventOnAppForeground = "cobalt:onAppForeground";
    public final static String JSEventOnPageShown = "cobalt:onPageShown";

    // INTENT
    public final static String JSTypeIntent = "intent";
    public final static String JSActionIntentOpenExternalUrl = "openExternalUrl";
    public final static String kJSUrl = "url";

    // LOG
    public final static String JSTypeLog = "log";

    // NAVIGATION
    public final static String JSTypeNavigation = "navigation";
    public final static String JSActionNavigationPush = "push";
    public final static String JSActionNavigationPop ="pop";
    public final static String JSActionNavigationModal = "modal";
    public final static String JSActionNavigationDismiss = "dismiss";
    public final static String JSActionNavigationReplace = "replace";
    public final static String kJSController = "controller";
    public final static String kJSBars = "bars";
    public final static String kJSAnimated = "animated";
    public final static String kJSClearHistory = "clearHistory";

    // BACK BUTTON
    public final static String JSEventOnBackButtonPressed = "cobalt:onBackButtonPressed";

    // UI
    public final static String JSTypeUI = "ui";
    public final static String kJSUIControl = "control";
    public final static String JSActionDismiss = "dismiss";
    
    // ALERT
    public final static String JSControlAlert = "alert";
    public final static String kJSAlertId = "alertId";
    public final static String kJSAlertTitle = "title";
    public final static String kJSAlertButtons = "buttons";
    public final static String kJSAlertCancelable = "cancelable";
    public final static String kJSAlertButtonIndex  = "index";

    // BARS
    public final static String JSControlBars = "bars";
    public final static String JSActionActionPressed = "actionPressed";
    public final static String kJSActionName = "name";
    public final static String JSActionSetBars = "setBars";
    public final static String JSActionSetActionBadge = "setActionBadge";
    public final static String JSActionSetActionContent = "setActionContent";
    public final static String JSActionSetBarsVisible = "setBarsVisible";
    public final static String JSActionSetBarContent = "setBarContent";
    public final static String JSActionSetActionVisible = "setActionVisible";
    public final static String JSActionSetActionEnabled = "setActionEnabled";
    public final static String kContent = "content";
    public final static String kVisible = "visible";
    public final static String kEnabled = "enabled";

    // TOAST
    public final static String JSControlToast = "toast";

    // WEB LAYER
    public final static String JSTypeWebLayer = "webLayer";
    public final static String JSActionWebLayerShow = "show";
    public final static String JSActionWebLayerDismiss = "dismiss";
    public final static String JSActionWebLayerBringToFront = "bringToFront";
    public final static String JSActionWebLayerSendToBack = "sendToBack";
    public final static String kJSWebLayerFadeDuration = "fadeDuration";
	public final static String JSEventonWebLayerLoading = "cobalt:onWebLayerLoading";
    public final static String JSEventonWebLayerLoaded = "cobalt:onWebLayerLoaded";
    public final static String JSEventonWebLayerDismissed = "cobalt:onWebLayerDismissed";

    // PULL TO REFRESH
    public final static String JSControlPullToRefresh = "pullToRefresh";
    public final static String JSEventPullToRefresh = "cobalt:onPullToRefresh";
    public final static String JSCallbackPullToRefreshDidRefresh = "pullToRefreshDidRefresh";

    // INFINITE SCROLL
    public final static String JSControlInfiniteScroll = "infiniteScroll";
    public final static String JSEventInfiniteScroll= "cobalt:onInfiniteScroll";
    public final static String JSCallbackInfiniteScrollDidRefresh = "infiniteScrollDidRefresh";

    //PLUGIN
    public final static String JSTypePlugin = "plugin";
    public final static String kJSPluginName = "name";
    public final static String kJSPluginClasses = "classes";
    public final static String kJSPluginAndroid = "android";

    //PUBSUB
    public final static String JSTypePubsub = "pubsub";
    public final static String JSActionSubscribe = "subscribe";
    public final static String JSActionUnsubscribe = "unsubscribe";
    public final static String JSActionPublish = "publish";
    public final static String kJSChannel = "channel";
    
    /**********************************************************************************************
     * MEMBERS
     **********************************************************************************************/

    private static Cobalt sInstance;
    private static Context sContext;
    private static JSONObject sCobaltConfiguration;

    private String mResourcePath = "www/";

    private int mRunningActivities = 0;
    private boolean mFirstActivityStart = true;

    /**********************************************************************************************
     * CONSTRUCTORS
     **********************************************************************************************/

    private Cobalt(Context context) {
        sContext = context.getApplicationContext();
    }

    public static Cobalt getInstance(Context context) {
        if (sInstance == null) {
            Assert.assertNotNull(TAG + " - getInstance: context could not be null", context);
            sInstance = new Cobalt(context);
        }

        return sInstance;
    }

    /**********************************************************************************************
     * GETTERS / SETTERS
     **********************************************************************************************/
	
	public String getResourcePath() {
		return ASSETS_PATH + mResourcePath;
	}
	
	public void setResourcePath(String resourcePath) {
        if (resourcePath != null) mResourcePath = resourcePath;
        else mResourcePath = "";
	}

    public String getResourcePathFromAsset() {return mResourcePath;}

    public static Context getAppContext() {
        return sContext;
    }

    /**********************************************************************************************
     * APP LIFECYCLE
     **********************************************************************************************/

    public void onActivityStarted(CobaltActivity activity) {
        if (++mRunningActivities == 1) {
            if (mFirstActivityStart) {
                mFirstActivityStart = false;
                
                activity.onAppStarted();
            }
            else activity.onAppForeground();
        }
    }

    public void onActivityStopped(CobaltActivity activity) {
        if (--mRunningActivities == 0) activity.onAppBackground();
    }

    /**********************************************************************************************
     * CONFIGURATION FILE
     **********************************************************************************************/

    public CobaltFragment getFragmentForController(Class<?> CobaltFragmentClass, String controller, String page) {
        CobaltFragment fragment = null;

        try {
            if (CobaltFragment.class.isAssignableFrom(CobaltFragmentClass)) {
                fragment = (CobaltFragment) CobaltFragmentClass.newInstance();
                Bundle configuration = getConfigurationForController(controller);
                configuration.putString(kPage, page);
                fragment.setArguments(configuration);
            }
            else if (Cobalt.DEBUG) Log.e(Cobalt.TAG, TAG + " - getFragmentForController: " + CobaltFragmentClass.getSimpleName() + " does not inherit from CobaltFragment!");
        }
        catch (java.lang.InstantiationException exception) {
            if (Cobalt.DEBUG) Log.e(Cobalt.TAG, TAG + " - getFragmentForController: InstantiationException");
            exception.printStackTrace();
        }
        catch (IllegalAccessException exception) {
            if (Cobalt.DEBUG) Log.e(Cobalt.TAG, TAG + " - getFragmentForController: IllegalAccessException");
            exception.printStackTrace();
        }

        return fragment;
    }

    public Intent getIntentForController(String controller, String page) {
        Intent intent = null;

        Bundle configuration = getConfigurationForController(controller);

        if (! configuration.isEmpty()) {
            String activity = configuration.getString(kActivity);

            // Creates intent
            Class<?> pClass;
            try {
                pClass = Class.forName(activity);
                // Instantiates intent only if class inherits from Activity
                if (Activity.class.isAssignableFrom(pClass)) {
                    configuration.putString(kPage, page);

                    intent = new Intent(sContext, pClass);
                    intent.putExtra(kExtras, configuration);
                }
                else if (Cobalt.DEBUG) Log.e(Cobalt.TAG, TAG + " - getIntentForController: " + activity + " does not inherit from Activity!");
            }
            catch (ClassNotFoundException exception) {
                if (Cobalt.DEBUG) Log.e(Cobalt.TAG, TAG + " - getIntentForController: " + activity + " class not found for id " + controller + "!");
                exception.printStackTrace();
            }
        }

        return intent;
    }

    public Bundle getConfigurationForController(String controller) {
        Bundle bundle = new Bundle();

        JSONObject configuration = getConfiguration();

        // Gets configuration
        try {
            JSONObject controllers = configuration.getJSONObject(kControllers);

            String activity;
            JSONObject bars;
            boolean enablePullToRefresh;
            boolean enableInfiniteScroll;
            int infiniteScrollOffset;
            String backgroundColor;
            // TODO: add enableGesture

            if (controller != null
                && controllers.has(controller)) {
                activity = controllers.getJSONObject(controller).optString(kAndroid, null);
                bars = controllers.getJSONObject(controller).optJSONObject(kBars);
                enablePullToRefresh = controllers.getJSONObject(controller).optBoolean(kPullToRefresh);
                enableInfiniteScroll = controllers.getJSONObject(controller).optBoolean(kInfiniteScroll);
                infiniteScrollOffset = controllers.getJSONObject(controller).optInt(kInfiniteScrollOffset, INFINITE_SCROLL_OFFSET_DEFAULT_VALUE);
                backgroundColor = controllers.getJSONObject(controller).optString(kBackgroundColor, BACKGROUND_COLOR_DEFAULT);
            }
            else {
                activity = controllers.getJSONObject(kDefaultController).optString(kAndroid, null);
                bars = controllers.getJSONObject(kDefaultController).optJSONObject(kBars);
                enablePullToRefresh = controllers.getJSONObject(kDefaultController).optBoolean(kPullToRefresh);
                enableInfiniteScroll = controllers.getJSONObject(kDefaultController).optBoolean(kInfiniteScroll);
                infiniteScrollOffset = controllers.getJSONObject(kDefaultController).optInt(kInfiniteScrollOffset, INFINITE_SCROLL_OFFSET_DEFAULT_VALUE);
                backgroundColor = controllers.getJSONObject(kDefaultController).optString(kBackgroundColor, BACKGROUND_COLOR_DEFAULT);
            }

            if (activity == null)
            {
                activity = "org.cobaltians.cobalt.activities.CobaltActivity";
            }
            else if (activity.startsWith("."))
            {
                activity = sContext.getPackageName() + activity;
            }
    
            bundle.putString(kController, controller);
            bundle.putString(kActivity, activity);
            if (bars != null) bundle.putString(kBars, bars.toString());
            bundle.putBoolean(kPullToRefresh, enablePullToRefresh);
            bundle.putBoolean(kInfiniteScroll, enableInfiniteScroll);
            bundle.putInt(kInfiniteScrollOffset, infiniteScrollOffset);
            bundle.putString(kBackgroundColor, backgroundColor);

            return bundle;
        }
        catch (JSONException exception) {
            if (Cobalt.DEBUG) Log.e(Cobalt.TAG,     TAG + " - getConfigurationForController: check cobalt.json. Known issues: \n "
                                                    + "\t - controllers field not found or not a JSONObject \n "
                                                    + "\t - " + controller + " controller not found and no " + kDefaultController + " controller defined \n ");
            exception.printStackTrace();
        }

        return bundle;
    }

    /**********************************************************************************************
     * PLUGINS FILE
     **********************************************************************************************/

    public HashMap<String, Class<? extends CobaltAbstractPlugin>> getPlugins() {
        HashMap<String, Class<? extends CobaltAbstractPlugin>> pluginsMap = new HashMap<>();

        try {
            JSONObject configuration = getConfiguration();
            JSONObject plugins = configuration.getJSONObject(kPlugins);
            Iterator<String> pluginsIterator = plugins.keys();

            while (pluginsIterator.hasNext()) {
                String pluginName = pluginsIterator.next();
                try {
                    JSONObject plugin = plugins.getJSONObject(pluginName);
                    String pluginClassName = plugin.getString(kAndroid);

                    try {
                        Class<?> pluginClass = Class.forName(pluginClassName);
                        if (CobaltAbstractPlugin.class.isAssignableFrom(pluginClass)) {
                            pluginsMap.put(pluginName, (Class<? extends CobaltAbstractPlugin>) pluginClass);
                        }
                        else if (Cobalt.DEBUG) Log.e(Cobalt.TAG, TAG + " - getPlugins: " + pluginClass + " does not inherit from CobaltAbstractActivity!\n" + pluginName + " plugin message will not be processed.");
                    }
                    catch (ClassNotFoundException exception) {
                        if (Cobalt.DEBUG) {
                            Log.e(Cobalt.TAG, TAG + " - getPlugins: " + pluginClassName + " class not found!\n" + pluginName + " plugin message will not be processed.");
                            exception.printStackTrace();
                        }
                    }
                }
                catch (JSONException exception) {
                    if (Cobalt.DEBUG) {
                        Log.e(Cobalt.TAG, TAG + " - getPlugins: " + pluginName + " field is not a JSONObject or does not contain an android field or is not a String.\n" + pluginName + " plugin message will not be processed.");
                        exception.printStackTrace();
                    }
                }
            }
        }
        catch (JSONException exception) {
            if (Cobalt.DEBUG) {
                Log.w(Cobalt.TAG, TAG + " - getPlugins: plugins field of cobalt.json not found or not a JSONObject.");
                exception.printStackTrace();
            }
        }

        return pluginsMap;
    }
    
    /**********************************************************************************************
     * HELPER METHODS
     **********************************************************************************************/

    /**
     * Broadcasts the specified message to PubSubReceivers which have subscribed to the specified channel.
     * @param message the message to broadcast to PubSubReceivers via the channel.
     * @param channel the channel to which broadcast the message.
     */
    public static void publishMessage(@Nullable JSONObject message, @NonNull String channel) {
        PubSub.getInstance().publishMessage(message, channel);
    }

    /**
     * Subscribes the specified PubSubInterface to messages sent via the specified channel.
     * @param listener the PubSubInterface the PubSubReceiver will have to use to send messages.
     * @param channel the channel the PubSubReceiver subscribes.
     */
    public static void subscribeToChannel(@NonNull PubSubInterface listener,
                                         @NonNull String channel)
    {
        PubSub.getInstance().subscribeToChannel(listener, channel);
    }

    /**
     * Unsubscribes the specified PubSubInterface from messages sent via the specified channel.
     * @param listener the PubSubInterface to unsubscribes from the channel.
     * @param channel the channel from which the messages come from.
     */
    public static void unsubscribeFromChannel(@NonNull PubSubInterface listener,
                                             @NonNull String channel)
    {
        PubSub.getInstance().unsubscribeFromChannel(listener, channel);
    }

    private JSONObject getConfiguration() {
        if (sCobaltConfiguration == null) {
            String configuration = readFileFromAssets(mResourcePath + CONF_FILE);
            try {
                sCobaltConfiguration = new JSONObject(configuration);
            }
            catch (JSONException exception) {
                if (Cobalt.DEBUG) Log.e(Cobalt.TAG, TAG + " - getConfiguration: check cobalt.json. File is missing or not at " + ASSETS_PATH + mResourcePath + CONF_FILE);
                exception.printStackTrace();
                return new JSONObject();
            }
        }
        return sCobaltConfiguration;
    }

    private String readFileFromAssets(String file) {
        try {
            AssetManager assetManager = sContext.getAssets();
            InputStream inputStream = assetManager.open(file);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder fileContent = new StringBuilder();
            int character;

            while ((character = bufferedReader.read()) != -1) {
                fileContent.append((char) character);
            }

            return fileContent.toString();
        }
        catch (FileNotFoundException exception) {
            if (Cobalt.DEBUG) Log.e(Cobalt.TAG, TAG + " - readFileFromAssets: " + file + "not found.");
        }
        catch (IOException exception) {
            if (Cobalt.DEBUG) Log.e(Cobalt.TAG, TAG + " - readFileFromAssets: IOException");
            exception.printStackTrace();
        }

        return "";
    }

    /**
     * Retrieve the value of the color attribute in the theme applied to bars corresponding to the
     * background color of bars.
     * @param activity The activity holding the bars.
     * @return int Returns the color int if the attribute was found and its value parse
     *             succeeded, else the color int corresponding to material grey 900 (0xFF212121).
     */
    public int getThemedBarBackgroundColor(@NonNull CobaltActivity activity) {
        return getThemedColorValue(activity.getTheme(), android.support.v7.appcompat.R.attr.colorPrimary, BAR_BACKGROUND_COLOR_DEFAULT_VALUE);
    }

    // TODO: differentiate in overflow or not?
    /**
     * Retrieve the value of the color attribute in the theme applied to bars corresponding to the
     * text color in bars.
     * @param activity The activity holding the bars.
     * @return int Returns the color int if the attribute was found and its value parse
     *             succeeded, else the color int corresponding to white (0xFFFFFFFF).
     */
    public int getThemedBarTextColor(@NonNull CobaltActivity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            ActionBar actionBar = activity.getSupportActionBar();
            if (actionBar != null) {
                return getThemedColorValue(actionBar.getThemedContext().getTheme(), android.R.attr.textColorPrimary, BAR_TEXT_COLOR_DEFAULT_VALUE);
            }
        }
        else {
            return getThemedColorValue(activity.getTheme(), android.R.attr.textColorPrimary, BAR_TEXT_COLOR_DEFAULT_VALUE);
        }

        return BAR_TEXT_COLOR_DEFAULT_VALUE;
    }

    // TODO: differentiate in overflow or not?
    /**
     * Retrieve the value of the color attribute in the theme applied to bars corresponding to the
     * icons color in bars.
     * @param activity The activity holding the bars.
     * @return int Returns the color int if the attribute was found and its value parse
     *             succeeded, else the color int corresponding to white (0xFFFFFFFF).
     */
    public int getThemedBarIconColor(@NonNull CobaltActivity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            ActionBar actionBar = activity.getSupportActionBar();
            if (actionBar != null) {
                return getThemedColorValue(actionBar.getThemedContext().getTheme(), android.R.attr.textColorPrimary, BAR_ICON_COLOR_DEFAULT_VALUE);
            }
        }
        else {
            return getThemedColorValue(activity.getTheme(), android.support.v7.appcompat.R.attr.colorControlNormal, BAR_ICON_COLOR_DEFAULT_VALUE);
        }

        return BAR_ICON_COLOR_DEFAULT_VALUE;
    }

    /**
     * Retrieve the value of a color attribute in the theme.
     * @param theme The theme.
     * @param attr The resource identifier of the desired attribute.
     * @param defaultValue The default value to return if the attribute is not found
     *                     or its value has failed to parse.
     * @return int Returns the color int if the attribute was found
     *         and its value parse succeeded, else defaultValue.
     */
    private int getThemedColorValue(@NonNull Resources.Theme theme, @AttrRes int attr, int defaultValue) {
        TypedValue attrValue = new TypedValue();

        if (! theme.resolveAttribute(attr, attrValue, true)) {
            return 0;
        }

        switch (attrValue.type) {
            case TypedValue.TYPE_INT_COLOR_ARGB4:
            case TypedValue.TYPE_INT_COLOR_ARGB8:
            case TypedValue.TYPE_INT_COLOR_RGB4:
            case TypedValue.TYPE_INT_COLOR_RGB8:
                return attrValue.data;
            case TypedValue.TYPE_STRING:
                if (attrValue.resourceId == 0) {
                    return 0;
                }

                try {
                    ColorStateList textColorPrimaryList;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        textColorPrimaryList = sContext.getResources().getColorStateList(attrValue.resourceId, theme);
                    }
                    else {
                        textColorPrimaryList = sContext.getResources().getColorStateList(attrValue.resourceId);
                    }

                    if (textColorPrimaryList == null) {
                        return 0;
                    }
                    else {
                        return textColorPrimaryList.getDefaultColor();
                    }
                }
                catch(Resources.NotFoundException exception) {
                    exception.printStackTrace();
                    return 0;
                }
            default:
                return 0;
        }
    }

    /**
     * Parses the color string and return the corresponding color-int.
     * @param color the color string to parse. Supported formats are (#)RGB & (#)RRGGBB(AA).
     * @return the corresponding color-int.
     * @throws IllegalArgumentException if the string cannot be parsed.
     */
    public static int parseColor(String color) throws IllegalArgumentException {
        if (color == null) {
            throw new IllegalArgumentException();
        }

        if (! color.startsWith("#")) {
            color = "#" + color;
        }

        switch(color.length()) {
            case 4:
                // #RGB -> #RRGGBB
                String red = color.substring(1, 2);
                String green = color.substring(2, 3);
                String blue = color.substring(3, 4);
                color = "#" + red + red + green + green + blue + blue;
                break;
            case 7:
                // #RRGGBB
                break;
            case 9:
                // #RRGGBBAA -> #AARRGGBB
                color = "#" + color.substring(7, 9) + color.substring(1, 7);
                break;
            default:
                throw new IllegalArgumentException();
        }

        return Color.parseColor(color);
    }
}
