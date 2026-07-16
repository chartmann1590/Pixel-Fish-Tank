package com.charles.virtualpet.fishtank.playgames

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CloudSavePolicyTest {
    @Test
    fun higherLevelCloudSaveWins() {
        assertTrue(CloudSavePolicy.shouldRestoreCloud(5, 0, 4, 999_999))
    }

    @Test
    fun higherXpWinsWithinSameLevel() {
        assertTrue(CloudSavePolicy.shouldRestoreCloud(3, 250, 3, 200))
    }

    @Test
    fun localSaveWinsTiesAndHigherProgress() {
        assertFalse(CloudSavePolicy.shouldRestoreCloud(3, 200, 3, 200))
        assertFalse(CloudSavePolicy.shouldRestoreCloud(2, 900, 3, 0))
    }
}
