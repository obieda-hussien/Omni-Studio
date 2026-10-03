package com.novacut.editor

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OmniStudioFoundationContractTest {

    @Test
    fun officialOmniLinkV3AndFirstPartyPrivilegesStayWired() {
        val build = locate("app/build.gradle.kts").readText()
        val manifest = locate("app/src/main/AndroidManifest.xml").readText()

        assertTrue(
            build.contains(
                "com.github.obieda-hussien.OmniLinkSDK:omni-link-sdk:v3.0.0"
            )
        )
        assertFalse(
            "Omni Studio must consume the official v3.0.0 coordinate, not the old commit coordinate",
            build.contains("omni-link-sdk:0a0607a0df535bd9f1db79ab36f0aba3c21423b7")
        )

        assertTrue(
            manifest.contains(
                "<uses-permission android:name=\"com.omnilink.sdk.permission.BIND_EXTENSION\" />"
            )
        )
        assertTrue(
            manifest.contains(
                "<uses-permission android:name=\"com.omnilink.sdk.permission.BIND_AGENT\" />"
            )
        )
        assertTrue(manifest.contains("com.omnilink.sdk.action.EXTENSION_BIND"))
        assertTrue(manifest.contains("com.omnilink.sdk.action.AGENT_GATEWAY_BIND"))
        assertTrue(
            manifest.contains(
                "android:permission=\"com.omnilink.sdk.permission.BIND_EXTENSION\""
            )
        )
    }

    @Test
    fun launcherIdentityIsVectorFirstAndContainsNoLegacyRasterLauncherAssets() {
        val adaptive = locate("app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml").readText()
        val fallback = locate("app/src/main/res/mipmap-anydpi/ic_launcher.xml").readText()

        assertTrue(adaptive.contains("@drawable/ic_omni_studio_background"))
        assertTrue(adaptive.contains("@drawable/ic_omni_studio_foreground"))
        assertTrue(adaptive.contains("@drawable/ic_omni_studio_monochrome"))
        assertTrue(fallback.contains("@drawable/ic_omni_studio_background"))
        assertTrue(fallback.contains("@drawable/ic_omni_studio_foreground"))

        val res = locate("app/src/main/res")
        val legacyRasterLaunchers = res.walkTopDown()
            .filter { it.isFile }
            .filter { it.extension.equals("png", ignoreCase = true) }
            .filter { it.name.startsWith("ic_launcher") }
            .toList()
        assertTrue(
            "Launcher identity must come from the Omni Studio XML/vector system: $legacyRasterLaunchers",
            legacyRasterLaunchers.isEmpty()
        )
    }

    @Test
    fun ciProducesOptimizedReleaseArtifactsWithoutDebugSigningFallback() {
        val workflow = locate(".github/workflows/ci.yml").readText()
        val build = locate("app/build.gradle.kts").readText()

        assertTrue(workflow.contains(":app:lintRelease"))
        assertTrue(workflow.contains(":app:assembleRelease"))
        assertTrue(workflow.contains(":app:bundleRelease"))
        assertFalse(workflow.contains("name: omni-studio-release-unsigned"))
        assertFalse(workflow.contains(":app:assembleDebug"))
        assertFalse(workflow.contains("omni-studio-debug-apks"))

        assertTrue(build.contains("omni.ciUnsignedRelease"))
        assertFalse(
            "Release builds must never silently use the Android debug signing key",
            build.substringAfter("release {").substringBefore("create(\"streaming\")")
                .contains("signingConfigs.getByName(\"debug\")")
        )
    }

    @Test
    fun ciUploadsEachApkAndBundleAsSeparateDownloads() {
        val workflow = locate(".github/workflows/ci.yml").readText()
        val uploads = workflow.split(Regex("(?m)^      - name: "))
            .filter { it.contains("uses: actions/upload-artifact@") }
        val expectedPaths = listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64", "universal")
            .associate { abi ->
                "omni-studio-apk-$abi-release-unsigned" to
                    "app/build/outputs/apk/release/*-$abi-*.apk"
            } + mapOf(
                "omni-studio-aab-release-unsigned" to "app/build/outputs/bundle/release/*.aab"
            )

        expectedPaths.forEach { (artifactName, expectedPath) ->
            val matches = uploads.filter {
                Regex("(?m)^          name: ${Regex.escape(artifactName)}$").containsMatchIn(it)
            }
            assertEquals("Exactly one upload must own $artifactName", 1, matches.size)
            val upload = matches.single()
            val paths = Regex("(?m)^          path: (.+)$").findAll(upload)
                .map { it.groupValues[1].trim() }.toList()
            assertEquals("$artifactName must contain only its build output", listOf(expectedPath), paths)
            assertTrue("Missing release output must fail CI", upload.contains("if-no-files-found: error"))
        }
        assertTrue(uploads.any { it.contains("name: omni-studio-release-signing-notice") })
    }

    private fun locate(relativePath: String): File =
        listOf(File(relativePath), File("../$relativePath"))
            .firstOrNull(File::exists)
            ?: error("$relativePath not found")
}
