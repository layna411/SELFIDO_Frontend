package com.simats.selfora

import com.simats.selfora.data.repository.DressingRepository
import org.junit.Test
import org.junit.Assert.*

class DressingModuleTest {

    private val repository = DressingRepository()

    @Test
    fun testBoyTShirtActivityHas18Steps() {
        val activity = repository.getBoyTShirtActivity()
        assertEquals("Boy T-Shirt Dressing", activity.title)
        assertEquals(18, activity.totalSteps)
        assertEquals(18, activity.steps.size)

        // Verify Step 1
        val step1 = activity.steps[0]
        assertEquals(1, step1.stepNumber)
        assertEquals("Look at the T-shirt", step1.title)
        assertEquals("Look at your cool T-Shirt in the cupboard!", step1.childGuidance)

        // Verify Step 7 (Head through neck opening)
        val step7 = activity.steps[6]
        assertEquals(7, step7.stepNumber)
        assertEquals("Put head through neck opening", step7.title)

        // Verify Step 18 (Check comfortable positioning)
        val step18 = activity.steps[17]
        assertEquals(18, step18.stepNumber)
        assertEquals("Check comfortable positioning", step18.title)
        assertEquals("You look awesome! All set!", step18.childGuidance)
    }

    @Test
    fun testGirlFrockActivityHas18Steps() {
        val activity = repository.getGirlFrockActivity()
        assertEquals("Girl Frock Dressing", activity.title)
        assertEquals(18, activity.totalSteps)
        assertEquals(18, activity.steps.size)

        // Verify Step 1
        val step1 = activity.steps[0]
        assertEquals(1, step1.stepNumber)
        assertEquals("Look at the Frock", step1.title)

        // Verify Step 18
        val step18 = activity.steps[17]
        assertEquals(18, step18.stepNumber)
        assertEquals("Check comfortable fit", step18.title)
        assertEquals("You look like a princess! All ready!", step18.childGuidance)
    }

    @Test
    fun testStepAssetPathsAreValid() {
        val boyActivity = repository.getBoyTShirtActivity()
        boyActivity.steps.forEach { step ->
            assertTrue(step.assetPath.startsWith("file:///android_asset/dressing/boy/tshirt/"))
            assertTrue(step.childGuidance.isNotEmpty())
            assertTrue(step.visualHighlight.isNotEmpty())
        }
    }
}
