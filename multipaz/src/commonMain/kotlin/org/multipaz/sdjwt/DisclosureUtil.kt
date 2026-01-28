package org.multipaz.sdjwt

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import org.multipaz.crypto.Algorithm
import org.multipaz.crypto.Crypto
import org.multipaz.util.toBase64Url

/** Helper extensions related to disclosures. */
object DisclosureUtil {

    /** Creates a digest from a disclosure, using the provided digest algorithm. */
    suspend fun JsonArray.toDigest(digestAlgorithm: Algorithm) = Crypto.digest(
        digestAlgorithm,
        toString()
            .encodeToByteArray()
            .toBase64Url()
            .encodeToByteArray()
    ).toBase64Url()

    /** Creates the appropriate JSON Object for a Digest being inserted into a [JsonArray]. */
    suspend fun JsonArray.toArrayDigestElement(digestAlgorithm: Algorithm) = buildJsonObject {
        put("...", JsonPrimitive(toDigest(digestAlgorithm)))
    }

    /** Creates the appropriate JSON Primitive for a Digest being inserted into a JSON Object. */
    suspend fun JsonArray.toClaimDigestElement(digestAlgorithm: Algorithm) = JsonPrimitive(
        toDigest(digestAlgorithm)
    )

    /**
     * Creates and inserts the digests for selectively disclosable object properties into a
     * JSON Object as defined SD-JWT 4.2.4.1.
     */
    suspend fun JsonObjectBuilder.putClaimDisclosureDigests(
        disclosures: List<JsonArray>,
        digestAlgorithm: Algorithm,
    ): JsonObjectBuilder {
        put(
            "_sd",
            JsonArray(disclosures.map {
                it.toClaimDigestElement(digestAlgorithm)
            })
        )
        return this
    }

    /** Creates a disclosure for a [JsonElement] in a [JsonArray] as defined by SD-JWT 4.2.2. */
    fun JsonElement.toArrayDisclosure(salt: String) = buildJsonArray {
        add(JsonPrimitive(salt))
        add(this@toArrayDisclosure)
    }

    /** Creates a disclosure for a [JsonElement] Object Property as defined by SD-JWT 4.2.1. */
    fun JsonElement.toClaimDisclosure(
        fieldName: String,
        salt: String
    ) = buildJsonArray {
        add(JsonPrimitive(salt))
        add(JsonPrimitive(fieldName))
        add(this@toClaimDisclosure)
    }
}
