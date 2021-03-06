/**
 * Copyright (c) 2018 MicroNova AG
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * <p>
 * 2. Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 * <p>
 * 3. Neither the name of MicroNova AG nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package jenkins.internal;

import hudson.FilePath;
import hudson.model.Node;
import hudson.util.FormValidation;
import jenkins.task._exam.Messages;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.*;

public class UtilTest {

    private char[] chars = "1234567890abcdef".toCharArray();

    private String generateValidUuid(boolean withMinus) {
        String uuid = "";
        Random rand = new Random();
        for (int i = 0; i < 32; i++) {
            int num = rand.nextInt() % chars.length;
            if (num < 0) {
                num = num * -1;
            }
            uuid += chars[num];
            if (withMinus) {
                if (i == 4 || i == 12 || i == 25) {
                    uuid += '-';
                }
            }
        }
        return uuid;
    }

    private String generateValidId() {
        Random rnd = new Random();

        int rndId = rnd.nextInt(999999999) + 1;
        return "I" + rndId;
    }

    @Test
    public void isUuidValid() throws Exception {
        String uuid = generateValidUuid(false);
        assertTrue("TestUuid: " + uuid, Util.isUuidValid(uuid));
    }

    @Test
    public void isUuidValidMinus() throws Exception {
        String uuid = generateValidUuid(true);
        assertTrue("TestUuid: " + uuid, Util.isUuidValid(uuid));
    }

    @Test
    public void isUuidValidFalse() throws Exception {
        String uuid = generateValidUuid(false) + "a";
        assertFalse("TestUuid: " + uuid, Util.isUuidValid(uuid));
        uuid = generateValidUuid(false).substring(1);
        assertFalse("TestUuid: " + uuid, Util.isUuidValid(uuid));
        uuid = generateValidUuid(false).substring(1) + "g";
        assertFalse("TestUuid: " + uuid, Util.isUuidValid(uuid));
    }

    @Test
    public void validateUuid() throws Exception {
        FormValidation ret = Util.validateUuid(generateValidUuid(false));
        assertEquals(FormValidation.Kind.OK, ret.kind);
    }

    @Test
    public void validateUuidFalse() throws Exception {
        FormValidation ret = Util.validateUuid(generateValidUuid(false) + "g");
        assertEquals(FormValidation.Kind.ERROR, ret.kind);
    }

    @Test
    public void isIdValid() throws Exception {
        String id1 = this.generateValidId();
        String id2 = this.generateValidId();
        String id3 = "blablablallslsjkdf";
        String id4 = "3" + this.generateValidId();

        Boolean result1 = Whitebox.invokeMethod(Util.class, "isIdValid", id1);
        Boolean result2 = Whitebox.invokeMethod(Util.class, "isIdValid", id2);
        Boolean result3 = Whitebox.invokeMethod(Util.class, "isIdValid", id3);
        Boolean result4 = Whitebox.invokeMethod(Util.class, "isIdValid", id4);

        assertTrue(result1);
        assertTrue(result2);
        assertFalse(result3);
        assertFalse(result4);
    }

    @Test
    public void isPythonConformName() {
        Map<String, Boolean> testsAndExpectedResults = new HashMap<String, Boolean>() {{
            put("_IAmAPythonConformName", true);
            put("_alskfdkjlsajf_I@##___IAmAlsoAPythonConformName@@", true);
            put("12IAmNotAPythonConformName", false);
            put("#IAmAlsoNoPythonConformName", false);
            put("IAmAlsoNoPythonConformName", false);
            put("break", false);
            put("IAmAlsoNo.P.ythonConformName", true);
            put("AmAlsoNo.break.hallo", false);
            put("AmAlsoNo.34huhu", false);
            put(null, false);
        }};

        testsAndExpectedResults.forEach((name, expectedValue) -> {
            try {
                Boolean result = Whitebox.invokeMethod(Util.class, "isPythonConformName", name);
                if (expectedValue) {
                    assertTrue(result);
                } else {
                    assertFalse(result);
                }
            } catch (Exception e) {
                fail("isPythonConformName threw an Exception");
            }
        });
    }

    @Test
    public void validateId() throws Exception {
        String validId = this.generateValidId();
        String invalidId = "3" + this.generateValidId();

        FormValidation fv_ok = Whitebox.invokeMethod(Util.class, "validateId", validId);
        FormValidation fv_error = Whitebox.invokeMethod(Util.class, "validateId", invalidId);

        assertEquals(FormValidation.ok(), fv_ok);
        assertEquals(FormValidation.error(Messages.EXAM_RegExId()).getMessage(), fv_error.getMessage());
    }

    @Test
    public void validatePythonConformName() throws Exception {
        String name1 = "_IAmAPythonConformName";
        String name2 = "#IAmAlsoNoPythonConformName";

        FormValidation fv_ok = Whitebox.invokeMethod(Util.class, "validatePythonConformName", name1);
        FormValidation fvD_error = Whitebox.invokeMethod(Util.class, "validatePythonConformName", name2);

        assertEquals(FormValidation.ok(), fv_ok);
        assertEquals(FormValidation.error(Messages.EXAM_RegExFsn()).getMessage(), fvD_error.getMessage());
    }

    @Test
    public void validateElementForSearch() throws Exception {
        String newLine = "\r\n";
        String expectedErrorMsg = Messages.EXAM_RegExUuid() + newLine + Messages.EXAM_RegExId() + newLine + Messages.EXAM_RegExFsn() + newLine;

        String invalidString = "#IAmAlsoNoPythonConformName";
        String validString = this.generateValidId();

        FormValidation fv_invalidResult = Whitebox.invokeMethod(Util.class, "validateElementForSearch", invalidString);
        FormValidation fv_validResult = Whitebox.invokeMethod(Util.class, "validateElementForSearch", validString);

        assertEquals(FormValidation.error(expectedErrorMsg).getMessage(), fv_invalidResult.getMessage());
        assertEquals(FormValidation.ok(), fv_validResult);
    }

    @Test
    public void workspaceToNode(){
        //TODO: implementieren
    }
}
