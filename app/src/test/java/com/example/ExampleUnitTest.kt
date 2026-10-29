package com.example

import com.example.security.CryptoVaultEngine
import com.example.security.DtnBloomFilter
import com.example.security.SignalProtocolEngine
import com.example.service.AdvancedMeshCryptoEngine
import com.example.service.MeshPeerNode
import com.example.service.SdrModulationMode
import com.example.service.TacticalHardwareAndSecuritySuite
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun signalPqxdhDoubleRatchet_enforcesSenderRecipientExclusiveDecryption() {
        CryptoVaultEngine.initializeSessionKey()
        SignalProtocolEngine.reinitializeSignalIdentity()

        val peerAlice = "peer_valkyrie_01"
        val peerBob = "peer_kestrel_02"
        val secretText = "Post-Quantum Kyber-1024 + Double Ratchet tactical coordinates"

        val envelope = SignalProtocolEngine.encryptE2eeMessage(peerAlice, secretText, isOutgoing = true)
        assertTrue(envelope.wirePayload.startsWith("E2EE_SIGv3|"))

        // Authorized recipient session decrypts accurately
        val decryptedByAuthorized = SignalProtocolEngine.decryptE2eeMessage(envelope.wirePayload, expectedPeerId = peerAlice)
        assertEquals(secretText, decryptedByAuthorized)

        // Unauthorized third-party / wrong recipient session is strictly blocked by AEAD AAD & session check
        val decryptedByWrongPeer = SignalProtocolEngine.decryptE2eeMessage(envelope.wirePayload, expectedPeerId = peerBob)
        assertTrue(decryptedByWrongPeer.contains("E2EE AUTH FAILED"))

        // Self-destruct message key purge prevents any subsequent decryption
        SignalProtocolEngine.purgeEphemeralMessageKey(
            peerId = peerAlice,
            ratchetGeneration = envelope.ratchetGeneration,
            messageNumber = envelope.messageNumber,
            isOutgoing = true
        )
        val afterSelfDestruct = SignalProtocolEngine.decryptE2eeMessage(envelope.wirePayload, expectedPeerId = peerAlice)
        assertTrue(afterSelfDestruct.contains("Ratcheted/Wiped"))
    }

    @Test
    fun aes256GcmEncryption_burnerId_zkpAttestation_andSharding_workCorrectly() {
        CryptoVaultEngine.initializeSessionKey()
        val plaintext = "Tactical rally at Sector 7 Bridge"
        val ciphertext = CryptoVaultEngine.encryptAes256Gcm(plaintext)
        assertTrue(ciphertext.startsWith("ENC256:"))
        assertNotEquals(plaintext, ciphertext)
        assertEquals(plaintext, CryptoVaultEngine.decryptAes256Gcm(ciphertext))

        // Anonymous Burner Contact ID generation
        val (burnerId, tokenUri) = CryptoVaultEngine.generateAnonymousBurnerId("BRN")
        assertTrue(burnerId.startsWith("BRN-"))
        assertTrue(tokenUri.startsWith("gcmesh://burner/"))

        // Zero-Knowledge Proof (ZKP) Anonymous Group Attestation verification
        val zkp = AdvancedMeshCryptoEngine.generateAndVerifyZkpGroupAttestation("RING-ALPHA-01")
        assertTrue(zkp.isVerifiedValid)
        assertEquals("ZERO_KNOWLEDGE (ANONYMOUS_VERIFIED)", zkp.identityRevealed)

        // Decentralized 3-of-5 Local Sharding
        val samplePeers = listOf(
            MeshPeerNode("EP-1", "Valkyrie-7", "QS", "5GHz", 1, 14, -52, true, true),
            MeshPeerNode("EP-2", "Kestrel", "QS", "BLE", 2, 13, -65, true, false)
        )
        val manifest = AdvancedMeshCryptoEngine.shardFileAcrossTrustedMeshPeers(
            fileName = "secret_map.pmtiles",
            totalBytes = 3_000_000L,
            peers = samplePeers
        )
        assertEquals(5, manifest.shards.size)
        assertEquals(3, manifest.thresholdK)

        // SDR KISS AX.25 Framing
        val sdrFrame = TacticalHardwareAndSecuritySuite.encodeKissAx25SdrFrame(
            sourceCallsign = "GC0OPS",
            destCallsign = "VALK07",
            mode = SdrModulationMode.VHF_2M_AFSK,
            encryptedPayload = ciphertext
        )
        assertTrue(sdrFrame.kissAx25HexHeader.startsWith("C0 00"))

        // Duress Panic Wipe zeroes keys
        CryptoVaultEngine.triggerDuressPanicWipe()
        assertTrue(CryptoVaultEngine.isDuressDecoyMode)
        assertTrue(CryptoVaultEngine. restoreVaultFromDuress("*#*#1234#*#*"))
    }

    @Test
    fun blake3SwarmChunkingAndDtnBloomFilter_deduplicatesCorrectly() {
        val chunks = CryptoVaultEngine.createSwarmChunks("offline_map.pmtiles", 1024 * 1024L)
        assertEquals(4, chunks.size)
        assertTrue(chunks.first().blake3Hash.startsWith("b3_"))

        val bloom = DtnBloomFilter(2048)
        assertFalse(bloom.mightContainOrInsert("PKT-ALPHA-01"))
        assertTrue(bloom.mightContainOrInsert("PKT-ALPHA-01"))
        assertEquals(1, bloom.deduplicatedCount)
    }

    @Test
    fun dynamicTypographyAndAdaptiveContrastPalettes_cycleAndResolveCorrectly() {
        val standardFont = com.example.service.FontScalePreset.STANDARD
        val largeFont = standardFont.next()
        assertEquals(com.example.service.FontScalePreset.LARGE, largeFont)
        assertTrue(largeFont.scaleFactor > standardFont.scaleFactor)

        val amoledMode = com.example.service.AdaptiveContrastMode.AMOLED_STEALTH
        val hiConMode = amoledMode.next()
        val sunlightMode = hiConMode.next()
        assertEquals(com.example.service.AdaptiveContrastMode.HIGH_CONTRAST_PROTEST_NIGHT, hiConMode)
        assertEquals(com.example.service.AdaptiveContrastMode.SUNLIGHT_OUTDOOR_MAX, sunlightMode)

        val amoledPalette = com.example.service.TacticalHapticEngine.resolvePalette(amoledMode)
        val sunlightPalette = com.example.service.TacticalHapticEngine.resolvePalette(sunlightMode)
        assertNotEquals(amoledPalette.backgroundColor, sunlightPalette.backgroundColor)
    }
}
