/**
 *
 * CobaltAbstractPlugin
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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONObject;

/**
 * 
 * @author Sébastien Famel
 */
public abstract class CobaltAbstractPlugin
{

    /***********************************************************************************************
	 *
     * MEMBERS
	 *
     **********************************************************************************************/

    static final String GETINSTANCE_METHOD_NAME = "getInstance";
	
	// TODO: need to declare sInstance in each plugin
	//protected static CobaltAbstractPlugin sInstance;
	
	/***********************************************************************************************
	 *
	 * CONSTRUCTORS
	 *
	 **********************************************************************************************/
	
	// TODO: need to implement method getInstance in each plugin
	/*
	public static CobaltAbstractPlugin getInstance()
	{
		if (sInstance == null)
		{
			sInstance = new CobaltAbstractPlugin();
		}
		
		return sInstance;
	}
	*/
	
    /***********************************************************************************************
	 *
     * ABSTRACT METHODS
	 *
     **********************************************************************************************/
    
    /**
     * Called when a {@link CobaltPluginWebContainer} has sent a message
	 * to this {@link CobaltAbstractPlugin} inherited singleton.
     * @param webContainer the {@link CobaltPluginWebContainer} which sent the message.
     * @param action the action to perform by the {@link CobaltAbstractPlugin}.
	 * @param data the data used to perform the action, may be null.
	 * @param callbackChannel the channel used to publish a response if needed, may be null.
     */
    public abstract void onMessage(@NonNull CobaltPluginWebContainer webContainer,
			@NonNull String action, @Nullable JSONObject data, @Nullable String callbackChannel);
}
