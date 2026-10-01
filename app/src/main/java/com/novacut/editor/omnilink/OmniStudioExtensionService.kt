package com.novacut.editor.omnilink

import android.util.Log
import com.omnilink.sdk.AccessController
import com.omnilink.sdk.AccessDecision
import com.omnilink.sdk.ActionError
import com.omnilink.sdk.ActionOutcome
import com.omnilink.sdk.ActionRequest
import com.omnilink.sdk.AuditLogger
import com.omnilink.sdk.CallerContext
import com.omnilink.sdk.CapabilityDescriptor
import com.omnilink.sdk.CapabilityExecutionMode
import com.omnilink.sdk.CapabilityRisk
import com.omnilink.sdk.CommunicationDirection
import com.omnilink.sdk.DataScope
import com.omnilink.sdk.ExtensionService
import com.omnilink.sdk.IdempotencySemantics
import com.omnilink.sdk.OmniLinkConstants
import com.omnilink.sdk.TrustTier
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * First-party OmniLink entry point for Omni Studio.
 *
 * Phase 0 intentionally exposes only a health/introspection capability. Timeline reads and writes
 * will be added behind explicit semantic contracts once their adapters are covered by tests.
 */
class OmniStudioExtensionService : ExtensionService() {
    override val minSupportedVersion: Int = OmniLinkConstants.CURRENT_PROTOCOL_VERSION
    override val maxSupportedVersion: Int = OmniLinkConstants.CURRENT_PROTOCOL_VERSION

    override val accessController: AccessController = object : AccessController {
        override fun decide(caller: CallerContext, request: ActionRequest): AccessDecision =
            when (request.name) {
                CAPABILITY_HEALTH -> AccessDecision.ALLOW
                else -> AccessDecision.DENY
            }
    }

    override val auditLogger: AuditLogger = object : AuditLogger {
        override fun log(caller: CallerContext, request: ActionRequest, result: ActionOutcome) {
            Log.i(
                TAG,
                "caller=${caller.callingPackage} capability=${request.name} outcome=${result::class.simpleName}"
            )
        }
    }

    override val capabilities: List<CapabilityDescriptor> = listOf(
        CapabilityDescriptor(
            name = CAPABILITY_HEALTH,
            description = "Read Omni Studio integration and protocol status",
            executionMode = CapabilityExecutionMode.IMMEDIATE,
            requiredTrustTier = TrustTier.FIRST_PARTY,
            communicationDirection = CommunicationDirection.OMNI_TO_APP_ONLY,
            risk = CapabilityRisk.LOW,
            idempotency = IdempotencySemantics.IDEMPOTENT,
            dataScopes = setOf(DataScope.OMNI_ECOSYSTEM)
        )
    )

    override suspend fun onAction(
        caller: CallerContext,
        request: ActionRequest
    ): ActionOutcome = when (request.name) {
        CAPABILITY_HEALTH -> ActionOutcome.Success(
            JsonObject(
                mapOf(
                    "product" to JsonPrimitive("Omni Studio"),
                    "sdkVersion" to JsonPrimitive(OmniLinkConstants.SDK_VERSION),
                    "protocolVersion" to JsonPrimitive(OmniLinkConstants.CURRENT_PROTOCOL_VERSION),
                    "phase" to JsonPrimitive("foundation"),
                    "agentNativeEditing" to JsonPrimitive(false)
                )
            )
        )
        else -> ActionOutcome.Failure(
            ActionError("unsupported", "Unknown Omni Studio capability: ${request.name}")
        )
    }

    private companion object {
        const val TAG = "OmniStudioLink"
        const val CAPABILITY_HEALTH = "studio.health"
    }
}
