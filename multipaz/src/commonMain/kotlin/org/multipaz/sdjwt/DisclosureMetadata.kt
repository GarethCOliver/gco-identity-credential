package org.multipaz.sdjwt

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Used to describe which claims in a JSON Object are Selectively Disclosable.
 *
 * This is used with [SdJwt.create] to select which claims are able to be Selectively Disclosed. It
 * is set as the "_sd" claim in each JSON Object.
 *
 * @param claimNames is the list of claimNames that should be Selectively Disclosable.
 * @param arrayDisclosures is used to allow the individual elements of an array to be Selectively
 * Disclosable. This is in addition to allowing the entire claim to be .
 * @param discloseAll is a convenience to make all claimNames and all array elements individually
 * disclosable.
 */
data class DisclosureMetadata(
    val claimNames: List<String> = emptyList(),
    val arrayDisclosures: List<ArrayDisclosure> = emptyList(),
    val discloseAll: Boolean = false,
) {

    /**
     * Create a [JsonObject] from this [DisclosureMetadata].
     *
     * @return [JsonObject] representing the [DisclosureMetadata].
     */
    fun toJsonObject() = buildJsonObject {
        if (claimNames.isNotEmpty()) {
            put("claimNames", JsonArray(claimNames.map { JsonPrimitive(it) }))
        }
        if (arrayDisclosures.isNotEmpty()) {
            put("arrayDisclosures", JsonArray(arrayDisclosures.map { it.toJsonObject() }))
        }
        if (discloseAll) {
            put("discloseAll", JsonPrimitive(true))
        }
    }

    /**
     * Object containing which individual elements in an array are selectively disclosable.
     *
     * @param claimName the claim name of the array.
     * @param indices the indices that are Selectively Disclosable. If empty then all elements will
     * be Selectively Disclosable.
     */
    data class ArrayDisclosure(
        val claimName: String,
        val indices: List<Int> = emptyList(),
    ) {

        /**
         * Method for determining is a particular index is selectively disclosable.
         *
         * @param index the of the element.
         * @return true if the index should be selectively disclosable, otherwise false.
         */
        fun isIndexSelectivelyDisclosable(index: Int) = indices.isEmpty() || indices.contains(index)

        /**
         * @return a [JsonObject] representing this [ArrayDisclosure].
         */
        fun toJsonObject() = buildJsonObject {
            put("claimName", JsonPrimitive(claimName))
            put("indices", JsonArray(indices.map { JsonPrimitive(it) }))
        }
    }

    companion object {
        /**
         *  A [DisclosureMetadata] that indicates all claims and arrays in the object should be
         *  selectively disclosable.
         */
        val All = DisclosureMetadata(discloseAll = true)

        /**
         * Creates a list of [ArrayDisclosure] that allow each element in each array to be
         * Selectively Disclosable.
         *
         * @param claimName the repeated names of each array whose elements should be selectively
         *        disclosable.
         * @return the list of [ArrayDisclosure].
         */
        fun listOfArrayDisclosures(vararg claimName: String): List<ArrayDisclosure>
            = (claimName).map { ArrayDisclosure(it) }

        /**
         * Extension for determining if a Claim should be made Selectively Disclosable.
         *
         * @receiver the [DisclosureMetadata] to check.
         * @return true if the claim should be selectively disclosable, otherwise false. Returns
         *         false when the receiver is null.
         */
        fun DisclosureMetadata?.isClaimSelectivelyDisclosable(claimName: String) =
            this != null && (discloseAll || claimNames.contains(claimName))

        /**
         * Extension for determining if an Index in an JSON Array should be made Selectively
         * Disclosable.
         *
         * @receiver the [DisclosureMetadata] to check.
         * @return true if the index should be selectively disclosable, otherwise false. Returns
         *         false when the receiver is null.
         */
        fun DisclosureMetadata?.isIndexSelectivelyDisclosable(claimName: String, index: Int) =
            this != null && (discloseAll || arrayDisclosures.find { it.claimName == claimName }
                ?.isIndexSelectivelyDisclosable(index) == true)

        /**
         *  @receiver the [JsonObject] representing a [DisclosureMetadata].
         *  @return a [DisclosureMetadata] from this [JsonObject].
         */
        fun JsonObject.toDisclosureMetadata(): DisclosureMetadata {
            val claimNames = get("claimNames")?.jsonArray?.map { it.jsonPrimitive.content }
            val arrayDisclosures =
                get("arrayDisclosures")?.jsonArray?.map { it.jsonObject.toArrayDisclosure() }
            val discloseAll = get("discloseAll")?.jsonPrimitive?.content == "true"
            return DisclosureMetadata(
                claimNames?.toList() ?: emptyList(),
                arrayDisclosures ?: emptyList(),
                discloseAll,
            )
        }

        /**
         * @receiver the [JsonObject] representing a [ArrayDisclosure].
         * @return a [ArrayDisclosure] from this [JsonObject].
         */
        fun JsonObject.toArrayDisclosure(): ArrayDisclosure {
            val claimName = get("claimName")?.jsonPrimitive?.content ?: ""
            val indices = get("indices")?.jsonArray?.map { it.jsonPrimitive.int }
            return ArrayDisclosure(claimName, indices?.toList() ?: emptyList())
        }
    }
}
