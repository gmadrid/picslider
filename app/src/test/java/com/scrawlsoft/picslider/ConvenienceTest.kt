@file:Suppress("IllegalIdentifier")

package com.scrawlsoft.picslider

import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.junit.Test

class IsNullTests {
    @Test
    fun `null obj is null`() {
        val str: String? = null
        assertTrue(str.isNull())
    }

    @Test
    fun `non-null obj is not null`() {
        val str: String? = "Joe"
        assertFalse(str.isNull())
    }

    @Test
    fun `non-nullable obj is not null`() {
        val str: String = "Ben"
        assertFalse(str.isNull())
    }
}

class IsNotNullTests {
    @Test
    fun `null obj is null`() {
        val str: String? = null
        assertFalse(str.isNotNull())
    }

    @Test
    fun `non-null obj is not null`() {
        val str: String? = "Joe"
        assertTrue(str.isNotNull())
    }

    @Test
    fun `non-nullable obj is not null`() {
        val str: String = "Ben"
        assertTrue(str.isNotNull())
    }
}

