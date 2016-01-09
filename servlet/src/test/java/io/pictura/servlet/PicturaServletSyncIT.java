/**
 * Copyright 2015 Steffen Kremp
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.pictura.servlet;

import org.junit.Test;

/**
 * @author Steffen Kremp
 */
public class PicturaServletSyncIT extends PicturaServletIT {

    @Override
    protected boolean getAsyncSupported() {
	return false;
    }

    @Test
    public void testFormat_Sync() throws Exception {
	System.out.println("testFormat_Sync");

	PicturaServletFormatIT formatTest = new PicturaServletFormatIT();
	formatTest.testFormat_GIF();
	formatTest.testFormat_ICO();
	formatTest.testFormat_JPEG();
	formatTest.testFormat_JPG();
	formatTest.testFormat_JPG_B();
	formatTest.testFormat_JPG_Base64();
	formatTest.testFormat_JPG_P();
	formatTest.testFormat_PNG();
    }

    @Test
    public void testRotate_Sync() throws Exception {
	System.out.println("testRotate_Sync");

	PicturaServletRotateIT rotateTest = new PicturaServletRotateIT();
	rotateTest.testRotate_H();
	rotateTest.testRotate_L();
	rotateTest.testRotate_LR();
	rotateTest.testRotate_R();
	rotateTest.testRotate_V();
    }

    @Test
    public void testScale_Sync() throws Exception {
	System.out.println("testScale_Sync");

	PicturaServletScaleIT scaleTest = new PicturaServletScaleIT();
	scaleTest.testScale_DPR();
	scaleTest.testScale_DPRDot();
	scaleTest.testScale_DPRDotD();
	scaleTest.testScale_EmptyParam();
	scaleTest.testScale_H();
	scaleTest.testScale_H0();
	scaleTest.testScale_M0();
	scaleTest.testScale_M1();
	scaleTest.testScale_M2();
	scaleTest.testScale_M3();
	scaleTest.testScale_M4();
	scaleTest.testScale_Q0();
	scaleTest.testScale_Q1();
	scaleTest.testScale_Q2();
	scaleTest.testScale_Q3();
	scaleTest.testScale_Q4();
	scaleTest.testScale_U();
	scaleTest.testScale_W();
	scaleTest.testScale_W0();
    }

    @Test
    public void testScript_Sync() throws Exception {
	System.out.println("testScript_Sync");

	PicturaServletScriptIT scriptTest = new PicturaServletScriptIT();
	scriptTest.testDoGet_JS();
	scriptTest.testDoGet_JSMin();
	scriptTest.testDoGet_JS_IfModifiedSince();
	scriptTest.testDoGet_JS_IfNoneMatch();
	scriptTest.testDoGet_JS_NotFound();
    }

}
