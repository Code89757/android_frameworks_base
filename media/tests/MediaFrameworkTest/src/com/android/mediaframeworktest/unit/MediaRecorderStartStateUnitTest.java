/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.mediaframeworktest.unit;

import android.media.MediaRecorder;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.LargeTest;
import android.test.suitebuilder.annotation.Suppress;

/**
 * Unit test class to test the set of valid and invalid states that
 * MediaRecorder.start() method can be called.
 */
public class MediaRecorderStartStateUnitTest extends AndroidTestCase implements MediaRecorderMethodUnderTest {
    private MediaRecorderStateUnitTestTemplate mTestTemplate = new MediaRecorderStateUnitTestTemplate();

    /**
     * 1. It is valid to call start() in the following states:
     *    {Prepared}.
     * 2. It is invalid to call start() in the following states:
     *    {Initial, Initialized, DataSourceConfigured, Recording, Error}
     *    
     * @param stateErrors the MediaRecorderStateErrors to check against.
     */
    public void checkStateErrors(MediaRecorderStateErrors stateErrors) {
        // Valid states.
        assertTrue(!stateErrors.errorInPreparedState);
        
        // Invalid states.
        assertTrue(stateErrors.errorInRecordingState);
        assertTrue(stateErrors.errorInInitialState);
        assertTrue(stateErrors.errorInInitialStateAfterReset);
        assertTrue(stateErrors.errorInInitialStateAfterStop);
        assertTrue(stateErrors.errorInInitializedState);
        assertTrue(stateErrors.errorInErrorState);
        assertTrue(stateErrors.errorInDataSourceConfiguredState);
    }

    public void invokeMethodUnderTest(MediaRecorder recorder) {
        recorder.start();
    }

    @LargeTest
    public void testStart() {
        mTestTemplate.runTestOnMethod(this);
    }
    
    @Override
    public String toString() {
        return "start()";
    }
}