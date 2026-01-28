package org.multipaz.sdjwt

import kotlinx.serialization.Serializable

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
@Serializable
data class DisclosureMetadata(
    val claimNames: List<String> = emptyList(),
    val arrayDisclosures: List<ArrayDisclosure> = emptyList(),
    val discloseAll: Boolean = false,
) {

    /**
     * Object containing which individual elements in an array are selectively disclosable.
     *
     * @param claimName the claim name of the
     * @param indices the indices that are Selectively Disclosable. If null then all elements will
     * be Selectively Disclosable.
     */
    @Serializable
    data class ArrayDisclosure(
        val claimName: String,
        val indices: List<Int>? = null,
    ) {
        fun isIndexSelectivelyDisclosable(index: Int) = indices == null || indices.contains(index)
    }

    companion object {
        val All = DisclosureMetadata(discloseAll = true)

        /**
         * Creates a list of [DisclosureMetadata.ArrayDisclosure] that allow each element in each
         * array to be Selectively Disclosed.
         */
        fun listOfArrayDisclosures(vararg fieldName: String): List<ArrayDisclosure>
            = (fieldName).map { ArrayDisclosure(it) }

        /** Extension for determining if a Claim should be made Selectively Disclosable. */
        fun DisclosureMetadata?.isClaimSelectivelyDisclosable(claimName: String) =
            this != null && (discloseAll || claimNames.contains(claimName))

        /**
         * Extension for determining if an Index in an JSON Array should be made Selectively
         * Disclosable.
         */
        fun DisclosureMetadata?.isIndexSelectivelyDisclosable(claimName: String, index: Int) =
            this != null && (discloseAll || arrayDisclosures.find { it.claimName == claimName }
                ?.isIndexSelectivelyDisclosable(index) == true)
    }
}

