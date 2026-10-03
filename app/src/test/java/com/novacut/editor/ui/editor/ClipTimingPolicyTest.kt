package com.novacut.editor.ui.editor

import org.junit.Assert.*
import org.junit.Test

class ClipTimingPolicyTest {
    @Test fun exactSecondsAndArabicNumbersAreSupported() {
        assertEquals(5000L, parseTrimTime("5"))
        assertEquals(8000L, parseTrimTime("٨٫٠٠٠"))
        assertEquals(1250L, parseTrimTime("1,250"))
        assertEquals(60250L, parseTrimTime("1:00.250"))
    }
    @Test fun millisecondsSurviveOpeningAndApplyingTheDialog() {
        listOf(33L, 1234L, 61033L).forEach { assertEquals(it, parseTrimTime(formatClipSeconds(it))) }
    }
    @Test fun invalidAndOverflowingTimesAreRejected() {
        listOf("-5", "NaN", "Infinity", "99999999999999999999", "-1:30", "1:60").forEach { assertNull(parseTrimTime(it)) }
    }
}
