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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import org.json.JSONObject;

/**
 * An object allowing a WebView contained in a CobaltFragment
 * to subscribe/unsubscribe for messages sent via a channel and receive them.
 *
 * @author Sébastien Vitard
 */
class PubSubReceiver
{
    private static final String TAG = PubSubReceiver.class.getSimpleName();

    /***********************************************************************************************
     * MEMBERS
     **********************************************************************************************/
    
    /**
     * The PubSubInterface to which send messages
     */
    private WeakReference<PubSubInterface> mListenerReference;
    /**
     * The array which keeps track of subscribed channels
     */
    private ArrayList<String> mChannels;
    /**
     * The listener to notify when the fragment is null (deallocated or not correctly initialized)
     * or the PubSubReceiver is not subscribed to any channel any more
     */
    protected PubSubInternalInterface mInternalListener;

    /***********************************************************************************************
     * METHODS
     **********************************************************************************************/

    /***********************************************************************************************
     * Constructors
     **********************************************************************************************/
    
    /**
     * Creates and return a PubSubReceiver for the specified channel and internalListener
     * @param channel           the channel from which the messages will come from.
     * @param internalListener  the internalListener to notify when reference is null..
     */
    protected PubSubReceiver(@NonNull String channel, @NonNull PubSubInternalInterface internalListener) {
        mChannels = new ArrayList<>(1);
        mChannels.add(channel);
        mInternalListener = internalListener;
    }
    
    /**
     * Creates and return a PubSubReceiver for the specified channel and internalListener
     * @param listener          the PubSubInterface to which send messages.
     * @param channel           the channel from which the messages will come from.
     * @param internalListener  the internalListener to notify when reference is null..
     */
    PubSubReceiver(@NonNull PubSubInterface listener, @NonNull String channel,
            @NonNull PubSubInternalInterface internalListener) {
        this(channel, internalListener);
        mListenerReference = new WeakReference<>(listener);
    }
    
    /***********************************************************************************************
     * Getters / Setters
     **********************************************************************************************/

    /**
     * Gets the PubSubInterface to which send messages
     * @return the PubSubInterface to which send messages
     */
    @Nullable
    final PubSubInterface getListener() {
        return mListenerReference.get();
    }

    /***********************************************************************************************
     * Helpers
     **********************************************************************************************/
    
    /**
     * Checks if the PubSubReceiver is currently subscribed to the specified channel.
     * @param channel   the channel to check if the PubSubReceiver is currently subscribed to.
     * @returns true    if the PubSubReceiver is currently subscribed to the specified channel,
     *          false   otherwise.
     */
    final boolean hasSubscribedToChannel(@NonNull String channel)
    {
        return mChannels.contains(channel);
    }
    
    /**
     * Subscribes to messages sent from the specified channel.
     * @param channel   the channel from which the messages will come from.
     * @implNote does nothing if the PubSubReceiver has already subscribed to the specified channel
     */
    final void subscribeToChannel(@NonNull String channel)
    {
        if (! mChannels.contains(channel))
        {
            mChannels.add(channel);
        }
    }

    /**
     * Unsubscribes from messages sent from the specified channel.
     * @param channel the channel from which the messages come from.
     * @implNote if after the unsubscription, the PubSubReceiver is not subscribed to any channel,
     * it will call the receiverReadyForRemove method of its PubSubInternalInterface.
     */
    final void unsubscribeFromChannel(@NonNull String channel)
    {
        mChannels.remove(channel);
        
        if (mChannels.isEmpty())
        {
            mInternalListener.receiverReadyForRemove(this);
        }
    }
    
    /**
    * If the PubSubReceiver has subscribed to the specified channel,
    * sends the specified message from this channel to the PubSubInterface
    * @param message   the message received from the channel.
    * @param channel   the channel from which the messages come from.
    * @implNote if PubSubInterface is null at this time, due to deallocation or wrong initialization,
    * it will call the receiverReadyForRemove method of its PubSubInternalInterface.
    */
    void receiveMessage(@Nullable JSONObject message, @NonNull String channel)
    {
        PubSubInterface listener = mListenerReference.get();
        if (listener == null)
        {
            Log.w(TAG, "receiveMessage - PubSubInterface is null.\n"
                       + "It may be caused by its deallocation or the PubSubReceiver was not correctly initialized...\n"
                       + "Please check if the PubSubReceiver has been initialized with PubSubReceiver(PubSubInterface, String, PubSubInternalInterface) method.");
    
            mInternalListener.receiverReadyForRemove(this);
            
            return;
        }
        
        listener.onMessageReceived(message, channel);
    }
}
