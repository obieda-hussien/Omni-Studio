package com.novacut.editor.engine

import java.util.Locale
import org.junit.Assert.*
import org.junit.Test

class MotionInterpolationPolicyTest {
    @Test fun durationUsesDecimalPointInEveryLocaleAndPaddingPrecedesInterpolation() {
        val previous = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("ar-EG"))
            val filter = motionInterpolationFilter(60, 3500)
            assertTrue(filter.contains("trim=duration=3.500"))
            assertTrue(filter.indexOf("tpad=") < filter.indexOf("minterpolate="))
            assertTrue(filter.contains("mi_mode=mci"))
            assertFalse(filter.contains("scale="))
            assertFalse(filter.contains("setpts=2*"))
        } finally { Locale.setDefault(previous) }
    }
    @Test(expected = IllegalArgumentException::class) fun invalidFrameRateIsRejected() { motionInterpolationFilter(0, 3500) }
    @Test(expected = IllegalArgumentException::class) fun invalidDurationIsRejected() { motionInterpolationFilter(60, 0) }
}
