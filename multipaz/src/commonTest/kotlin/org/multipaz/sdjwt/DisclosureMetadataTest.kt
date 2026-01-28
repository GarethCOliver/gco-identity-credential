package org.multipaz.sdjwt

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.multipaz.sdjwt.DisclosureMetadata.Companion.isClaimSelectivelyDisclosable
import org.multipaz.sdjwt.DisclosureMetadata.Companion.isIndexSelectivelyDisclosable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DisclosureMetadataTest {

    @Test
    fun disclosureMetadata_serializesAndDeserializes() = runTest {
        val expected = DisclosureMetadata(
            claimNames = listOf("a", "b"),
            arrayDisclosures = listOf(
                DisclosureMetadata.ArrayDisclosure("b"),
                DisclosureMetadata.ArrayDisclosure("b", listOf(3, 4))
            )
        )

        val actual = Json.decodeFromString<DisclosureMetadata>(Json.encodeToString(expected))

        assertEquals(expected, actual)
    }

    @Test
    fun listOfArrayDisclosures_createsList() = runTest {
        val actual = DisclosureMetadata.listOfArrayDisclosures("field1", "field2")

        assertEquals(
            listOf(
                DisclosureMetadata.ArrayDisclosure("field1"),
                DisclosureMetadata.ArrayDisclosure("field2")
            ), actual)
    }

    @Test
    fun isClaimSelectivelyDisclosable_allIsTrue_returnsTrue() = runTest {
        val metadata = DisclosureMetadata.All

        assertTrue(metadata.isClaimSelectivelyDisclosable("any_claim"))
    }

    @Test
    fun isClaimSelectivelyDisclosable_hasClaimName_returnsTrue() = runTest {
        val metadata = DisclosureMetadata(claimNames = listOf("claim1"))

        assertTrue(metadata.isClaimSelectivelyDisclosable("claim1"))
    }

    @Test
    fun isClaimSelectivelyDisclosable_doesNotHaveClaimName_returnsFalse() = runTest {
        val metadata = DisclosureMetadata(claimNames = listOf("claim1"))

        assertFalse(metadata.isClaimSelectivelyDisclosable("notPresentClaim"))
    }

    @Test
    fun isClaimSelectivelyDisclosable_whenNull_returnsFalse() = runTest {
        val metadata: DisclosureMetadata? = null

        assertFalse(metadata.isClaimSelectivelyDisclosable("any_claim"))
    }

    @Test
    fun isIndexSelectivelyDisclosable_allIsTrue_returnsTrue() = runTest {
        val metadata = DisclosureMetadata.All

        assertTrue(metadata.isIndexSelectivelyDisclosable("any_claim", 0))
    }

    @Test
    fun isIndexSelectivelyDisclosable_absentField_returnsFalse() = runTest {
        val metadata = DisclosureMetadata(
            claimNames = listOf("claim1"),
            arrayDisclosures = listOf(
                DisclosureMetadata.ArrayDisclosure("claim2")
            )
        )

        assertFalse(metadata.isIndexSelectivelyDisclosable("claim1", 0))
    }

    @Test
    fun isIndexSelectivelyDisclosable_presentWithNullIndices_returnsTrue() = runTest {
        val metadata = DisclosureMetadata(
            arrayDisclosures = listOf(
                DisclosureMetadata.ArrayDisclosure("claim1")
            )
        )

        assertTrue(metadata.isIndexSelectivelyDisclosable("claim1", 1))
    }

    @Test
    fun isIndexSelectivelyDisclosable_presentWithIndexInList_returnsTrue() = runTest {
        val metadata = DisclosureMetadata(
            arrayDisclosures = listOf(
                DisclosureMetadata.ArrayDisclosure("claim1", indices = listOf(1, 3))
            )
        )
        assertTrue(metadata.isIndexSelectivelyDisclosable("claim1", 1))
    }

    @Test
    fun isIndexSelectivelyDisclosable_presentWithIndexNotInList_returnsFalse() = runTest {
        val metadata = DisclosureMetadata(
            arrayDisclosures = listOf(
                DisclosureMetadata.ArrayDisclosure("claim1", indices = listOf(1, 3))
            )
        )

        assertFalse(metadata.isIndexSelectivelyDisclosable("claim1", 0))
    }

    @Test
    fun isIndexSelectivelyDisclosable_null_returnsFalse() = runTest {
        val metadata: DisclosureMetadata? = null

        assertFalse(metadata.isIndexSelectivelyDisclosable("any_claim", 0))
    }
}
