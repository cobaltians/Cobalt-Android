/**
 *
 * PubSubReceiver
 * PubSub
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Kristal
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

package org.cobaltians.cobalt.pubsub;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.cobaltians.cobalt.Cobalt;
import org.cobaltians.cobalt.fragments.CobaltFragment;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

/**
 * An object allowing a WebView contained in a CobaltFragment
 * to subscribe/unsubscribe for messages sent via a channel and receive them.
 *
 * @author Sébastien Vitard
 */
final class PubSubWebReceiver extends PubSubReceiver
{
    private static final String TAG = PubSubWebReceiver.class.getSimpleName();

    /***********************************************************************************************
     * MEMBERS
     **********************************************************************************************/
    
    /**
     * The CobaltFragment containing the WebView to which send messages
     */
    private WeakReference<CobaltFragment> mFragmentReference;

    /***********************************************************************************************
     * METHODS
     **********************************************************************************************/

    /***********************************************************************************************
     * Constructors
     **********************************************************************************************/
    
    /**
     * Creates and return a PubSubWebReceiver for the CobaltFragment
     * holding the Web to subscribes to channel
     * @param fragment          the CobaltFragment containing the WebView to which send messages.
     * @param channel           the channel from which the messages will come from.
     * @param internalListener  the internalListener to notify when reference is null.
     */
    PubSubWebReceiver(@NonNull CobaltFragment fragment,
            @NonNull String channel,
            @NonNull PubSubInternalInterface internalListener)
    {
        super(channel, internalListener);
        
        mFragmentReference = new WeakReference<>(fragment);
    }

    /***********************************************************************************************
     * Getters / Setters
     **********************************************************************************************/

    /**
     * Gets the CobaltFragment containing the WebView to which send messages
     * @return the CobaltFragment containing the WebView to which send messages
     */
    @Nullable
    CobaltFragment getFragment() {
        return mFragmentReference.get();
    }

    /***********************************************************************************************
     * Helpers
     **********************************************************************************************/

    /**
     * If the PubSubWebReceiver has subscribed to the specified channel,
     * sends the specified message from this channel to the WebView contained in the fragment
     * @param message   the message received from the channel.
     * @param channel   the channel from which the messages come from.
     * @implNote if fragment is null at this time, due to deallocation or wrong initialization,
     * it will call the receiverReadyForRemove method of its PubSubInternalInterface.
     */
    @Override
    void receiveMessage(@Nullable JSONObject message, @NonNull String channel)
    {
        CobaltFragment fragment = mFragmentReference.get();
        if (fragment == null)
        {
            Log.w(TAG, "receiveMessage - CobaltFragment is null.\n"
                        + "It may be caused by its deallocation or the PubSubWebReceiver was not correctly initialized...\n"
                        + "Please check if the PubSubWebReceiver has been initialized with PubSubWebReceiver(CobaltFragment, String, PubSubInternalInterface) method.");
            
            mInternalListener.receiverReadyForRemove(this);
            
            return;
        }
        
        try
        {
            JSONObject cobaltMessage = new JSONObject();
            cobaltMessage.put(Cobalt.kJSType, Cobalt.JSTypePubsub);
            cobaltMessage.put(Cobalt.kJSChannel, channel);
            cobaltMessage.put(Cobalt.kJSMessage, message);
    
            fragment.sendMessage(cobaltMessage);
        }
        catch (JSONException exception)
        {
            exception.printStackTrace();
        }
    }
}
