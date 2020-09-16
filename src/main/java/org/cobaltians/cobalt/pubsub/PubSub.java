/**
 *
 * PubSub
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.cobaltians.cobalt.fragments.CobaltFragment;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Allows Cobalt WebViews contained in CobaltFragments or native components
 * to broadcast messages between them into channels.
 * Handles subscribe/unsubscribe to channels and publish messages.
 * Broadcasts messages to Cobalt WebViews or native components
 * which have subscribed to the channel where they are from.
 * @author Sébastien Vitard
 */
public final class PubSub implements PubSubInternalInterface
{
	private static final String TAG = PubSub.class.getSimpleName();

	/**********************************************************************************************
	 *
	 * MEMBERS
	 *
	 **********************************************************************************************/

	private static PubSub sInstance;

	/**
	 * The array which keeps track of PubSubReceivers
	 */
	private final ArrayList<PubSubReceiver> mReceivers = new ArrayList<>();

	/**********************************************************************************************
	 *
	 * CONSTRUCTORS
	 *
	 **********************************************************************************************/

	public static PubSub getInstance()
	{
		if (sInstance == null) {
			sInstance = new PubSub();
		}
		return sInstance;
	}
	
    /**********************************************************************************************
	 *
     * METHODS
	 *
     **********************************************************************************************/

	/**
	 * Broadcasts the specified message to PubSubReceivers which have subscribed to the specified channel.
	 * @param message the message to broadcast to PubSubReceivers via the channel.
	 * @param channel the channel to which broadcast the message.
	 */
	public final void publishMessage(@Nullable JSONObject message, @NonNull String channel)
	{
		for (PubSubReceiver receiver : new ArrayList<>(mReceivers))
		{
			if (receiver.hasSubscribedToChannel(channel))
			{
				receiver.receiveMessage(message, channel);
			}
		}
	}

	/**
	 * Subscribes the specified CobaltFragment holding the WebView to messages sent via the specified channel.
	 * @implNote if no PubSubWebReceiver was created for the specified CobaltFragment, creates it.
	 * @param fragment the CobaltFragment the PubSubReceiver will have to use to send messages.
	 * @param channel the channel the PubSubReceiver subscribes.
	 */
	public final void subscribeWebToChannel(@NonNull CobaltFragment fragment,
			@NonNull String channel)
	{
		PubSubWebReceiver subscribingReceiver = null;

		for (PubSubReceiver receiver : new ArrayList<>(mReceivers))
		{
			if (receiver instanceof PubSubWebReceiver
				&& fragment.equals(((PubSubWebReceiver) receiver).getFragment()))
			{
				subscribingReceiver = (PubSubWebReceiver) receiver;
				break;
			}
		}

		if (subscribingReceiver != null)
		{
			subscribingReceiver.subscribeToChannel(channel);
		}
		else
		{
			subscribingReceiver = new PubSubWebReceiver(fragment, channel, this);
			mReceivers.add(subscribingReceiver);
		}
	}

	/**
	 * Unsubscribes the specified CobaltFragment holding the WebView from messages sent via the specified channel.
	 * @param fragment the CobaltFragment to unsubscribes from the channel.
	 * @param channel the channel from which the messages come from.
	 */
	public final void unsubscribeWebFromChannel(@NonNull CobaltFragment fragment,
			@NonNull String channel)
	{
		PubSubWebReceiver unsubscribingReceiver = null;

		for (PubSubReceiver receiver : new ArrayList<>(mReceivers))
		{
			if (receiver instanceof PubSubWebReceiver
				&& fragment.equals(((PubSubWebReceiver) receiver).getFragment()))
			{
				unsubscribingReceiver = (PubSubWebReceiver) receiver;
				break;
			}
		}

		if (unsubscribingReceiver != null)
		{
			unsubscribingReceiver.unsubscribeFromChannel(channel);
		}
	}
	
	/**
	 * Subscribes the specified PubSubInterface to messages sent via the specified channel.
	 * @implNote if no PubSubReceiver was created for the specified PubSubInterface, creates it.
	 * @param channel the channel the PubSubReceiver subscribes.
	 * @param listener the PubSubInterface the PubSubReceiver will have to use to send messages.
	 */
	public final void subscribeToChannel(@NonNull String channel,
										 @NonNull PubSubInterface listener)
	{
		PubSubReceiver subscribingReceiver = null;
		
		for (PubSubReceiver receiver : new ArrayList<>(mReceivers))
		{
			if (! (receiver instanceof PubSubWebReceiver)
				&& listener.equals(receiver.getListener()))
			{
				subscribingReceiver = receiver;
				break;
			}
		}
		
		if (subscribingReceiver != null)
		{
			subscribingReceiver.subscribeToChannel(channel);
		}
		else
		{
			subscribingReceiver = new PubSubReceiver(listener, channel, this);
			mReceivers.add(subscribingReceiver);
		}
	}
	
	/**
	 * Unsubscribes the specified PubSubInterface from messages sent via the specified channel.
	 * @param channel the channel from which the messages come from.
	 * @param listener the PubSubInterface to unsubscribes from the channel.
	 */
	public final void unsubscribeFromChannel(@NonNull String channel,
											 @NonNull PubSubInterface listener)
	{
		PubSubReceiver unsubscribingReceiver = null;
		
		for (PubSubReceiver receiver : new ArrayList<>(mReceivers))
		{
			if (! (receiver instanceof PubSubWebReceiver)
				&& listener.equals(receiver.getListener()))
			{
				unsubscribingReceiver = receiver;
				break;
			}
		}
		
		if (unsubscribingReceiver != null)
		{
			unsubscribingReceiver.unsubscribeFromChannel(channel);
		}
	}
	
	/**********************************************************************************************
	 *
	 * INTERNAL LISTENER
	 *
	 **********************************************************************************************/
	
	@Override
	public void receiverReadyForRemove(@NonNull PubSubReceiver receiver)
	{
		mReceivers.remove(receiver);
	}
}
