/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.camera.one.v2.sharedimagereader;

import android.media.ImageReader;

import com.android.camera.async.Lifetime;
import com.android.camera.async.Updatable;
import com.android.camera.one.v2.core.RequestBuilder;
import com.android.camera.one.v2.sharedimagereader.imagedistributor.ImageDistributor;
import com.android.camera.one.v2.sharedimagereader.imagedistributor.ImageDistributorFactory;
import com.android.camera.one.v2.sharedimagereader.ticketpool.FiniteTicketPool;
import com.android.camera.one.v2.sharedimagereader.ticketpool.TicketPool;

/**
 * Usage:
 * <p>
 * Ensure that *all* capture requests send to the camera device update the
 * global timestamp queue with the timestamp associated with the image as soon
 * as it arrives.
 * <p>
 * Add the OnImageAvailableListener to the image reader in a separate thread.
 * <p>
 * Use the ImageQueueCaptureStreamFactory to create image streams to add to
 * {@link RequestBuilder}s to interact with the camera and ImageReader.
 */
public class SharedImageReaderFactory {
    private final Updatable<Long> mGlobalTimestampQueue;
    private final ImageStreamFactory mSharedImageReader;

    /**
     * @param lifetime The lifetime of the SharedImageReader, and other
     *            components, to produce. Note that this may be shorter than the
     *            lifetime of the provided ImageReader.
     * @param imageReader The ImageReader to wrap. Note that this can outlive
     *            the resulting SharedImageReader instance.
     */
    public SharedImageReaderFactory(Lifetime lifetime, ImageReader imageReader) {
        ImageDistributorFactory imageDistributorFactory = new ImageDistributorFactory(new
                Lifetime(lifetime), imageReader);
        ImageDistributor imageDistributor = imageDistributorFactory.provideImageDistributor();
        mGlobalTimestampQueue = imageDistributorFactory.provideGlobalTimestampCallback();

        TicketPool ticketPool = new FiniteTicketPool(imageReader.getMaxImages() - 2);
        mSharedImageReader = new ImageStreamFactory(
                new Lifetime(lifetime), ticketPool, imageReader.getSurface(), imageDistributor);
    }

    public Updatable<Long> provideGlobalTimestampQueue() {
        return mGlobalTimestampQueue;
    }

    public ImageStreamFactory provideSharedImageReader() {
        return mSharedImageReader;
    }
}