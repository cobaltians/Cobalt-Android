/**
 *
 * CobaltPluginManager
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

package org.cobaltians.cobalt.plugin;

import org.cobaltians.cobalt.Cobalt;
import org.cobaltians.cobalt.fragments.CobaltFragment;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class CobaltPluginManager
{
	/***********************************************************************************************
	 *
     * MEMBERS
	 *
     **********************************************************************************************/
	
	private static final String TAG = CobaltPluginManager.class.getSimpleName();
	
	/***********************************************************************************************
	 *
     * COBALT METHODS
	 *
     **********************************************************************************************/
	
	public static boolean onMessage(@NonNull Context context, @NonNull CobaltFragment fragment,
			@NonNull final JSONObject message)
	{
		String pluginClassName;
		final String action;
		final JSONObject data;
		final String callbackChannel;
		try
		{
			JSONObject pluginClasses = message.getJSONObject(Cobalt.kJSPluginClasses);
			pluginClassName = pluginClasses.getString(Cobalt.kJSPluginAndroid);
			action = message.getString(Cobalt.kJSAction);
			data = message.optJSONObject(Cobalt.kJSData);
			callbackChannel = message.optString(Cobalt.kJSCallbackChannel);
		}
		catch(JSONException exception)
		{
			if (Cobalt.DEBUG)
			{
				Log.e(TAG, "onMessage: classes.android and/or action not found or not a string.\n"
						   + message.toString());
				exception.printStackTrace();
			}
			return false;
		}
		
		Class<? extends CobaltAbstractPlugin> pluginClass;
		try
		{
			Class<?> classFromName = Class.forName(pluginClassName);
			if (CobaltAbstractPlugin.class.isAssignableFrom(classFromName))
			{
				// TODO fix Unchecked cast: 'java.lang.Class<capture<?>>' to 'java.lang.Class<? extends org.cobaltians.cobalt.plugin.CobaltAbstractPlugin
				pluginClass = (Class<? extends CobaltAbstractPlugin>) classFromName;
			}
			else
			{
				if (Cobalt.DEBUG)
				{
					Log.e(Cobalt.TAG, TAG + "onMessage: " + pluginClassName
									  + " does not inherit from CobaltAbstractActivity.");
				}
				return false;
			}
		}
		catch (ClassNotFoundException exception)
		{
			if (Cobalt.DEBUG)
			{
				Log.e(Cobalt.TAG, TAG + "onMessage : " + pluginClassName
								  + " class not found.");
				exception.printStackTrace();
			}
			return false;
		}
		
		try
		{
			Method pluginGetInstanceMethod = pluginClass.getDeclaredMethod(CobaltAbstractPlugin.GETINSTANCE_METHOD_NAME);
			try
			{
				final CobaltPluginWebContainer webContainer = new CobaltPluginWebContainer((Activity) context,
																						   fragment);
				final CobaltAbstractPlugin plugin = (CobaltAbstractPlugin) pluginGetInstanceMethod.invoke(null);

				((Activity) context).runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						plugin.onMessage(webContainer, action, data, callbackChannel);
					}
				});

				return true;
			}
			catch (NullPointerException exception)
			{
				if (Cobalt.DEBUG)
				{
					Log.e(TAG, "onMessage: " + pluginClass.getSimpleName()
							   + ".getInstance() method must be static.");
					exception.printStackTrace();
				}
			}
			catch (IllegalAccessException exception)
			{
				if (Cobalt.DEBUG)
				{
					Log.e(TAG, "onMessage: " + pluginClass.getSimpleName()
							   + ".getInstance() method must be public and have no parameters.");
					exception.printStackTrace();
				}
			}
			catch (InvocationTargetException exception)
			{
				if (Cobalt.DEBUG)
				{
					Log.e(TAG, "onMessage: exception thrown by "
							   + pluginClass.getSimpleName() + ".getInstance() method.");
					exception.printStackTrace();
				}
			}
		}
		catch (NoSuchMethodException exception)
		{
			if (Cobalt.DEBUG)
			{
				Log.e(TAG, "onMessage: no method found matching "
						   + pluginClass.getSimpleName() + ".getInstance() or not public.");
				exception.printStackTrace();
			}
		}

		return false;
	}
}
