/**
 *
 * PubSubInterface
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

import org.json.JSONObject;

/**
 * An Interface to implement to be notified when a message has been received on a channel
 * @author Sébastien Vitard
 */
public interface PubSubInterface
{
    /**
    * Notifies when a message has been received on a channel
    * @param message the message received
    * @param channel the channel on which the message has been received
    */
    void onMessageReceived(@Nullable JSONObject message, @NonNull String channel);
}