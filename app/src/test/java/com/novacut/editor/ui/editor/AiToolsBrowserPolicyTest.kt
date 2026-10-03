package com.novacut.editor.ui.editor

import org.junit.Assert.*
import org.junit.Test
import java.util.Locale

class AiToolsBrowserPolicyTest {
    @Test fun blankQueriesShowToolsAndEverySearchWordMustMatch() {
        assertTrue(matchesAiToolQuery("  ", "Auto Captions", "Generate subtitles from speech"))
        assertTrue(matchesAiToolQuery("  CAPTIONS   speech ", "Auto Captions", "Generate subtitles from speech"))
        assertFalse(matchesAiToolQuery("captions background", "Auto Captions", "Generate subtitles from speech"))
    }

    @Test fun searchIncludesCategoryAndWorksWithArabicCopy() {
        assertTrue(matchesAiToolQuery("motion shake", "Stabilize", "Reduce camera shake", "Motion & framing"))
        assertTrue(matchesAiToolQuery("إزالة خلفية", "إزالة الخلفية", "إزالة خلفية الفيديو"))
    }

    @Test fun englishSearchDoesNotChangeWithDeviceLocale() {
        val original = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"))
            assertTrue(matchesAiToolQuery("DETAIL", "Color & detail"))
        } finally {
            Locale.setDefault(original)
        }
    }

    @Test fun toolFamiliesHavePredictableLocations() {
        assertEquals(AiToolCategory.EDIT, aiToolCategory("cut_assistant"))
        assertEquals(AiToolCategory.AUDIO, aiToolCategory("auto_captions"))
        assertEquals(AiToolCategory.AUDIO, aiToolCategory("denoise"))
        assertEquals(AiToolCategory.BACKGROUND, aiToolCategory("bg_replace"))
        assertEquals(AiToolCategory.MOTION, aiToolCategory("ai_stabilize"))
        assertEquals(AiToolCategory.ENHANCE, aiToolCategory("video_upscale"))
    }
}
