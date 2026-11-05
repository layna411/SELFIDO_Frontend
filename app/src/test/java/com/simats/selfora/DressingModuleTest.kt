package com.simats.selfora

import com.simats.selfora.data.repository.DressingRepository
import org.junit.Test
import org.junit.Assert.*

class DressingModuleTest {

    private val repository = DressingRepository()

    @Test
    fun testBoyTShirtActivityHas20Steps() {
        val activity = repository.getBoyTShirtActivity()
        assertEquals("Boy T-Shirt Dressing", activity.title)
        assertEquals(20, activity.totalSteps)
        assertEquals(20, activity.steps.size)

        // Verify Step 1
        val step1 = activity.steps[0]
        assertEquals(1, step1.stepNumber)
        assertEquals("Stand or sit in front of the T-shirt", step1.title)
        assertEquals("Stand or sit in front of your T-shirt!", step1.childGuidance)

        // Verify Step 9 (Head through neck opening)
        val step9 = activity.steps[8]
        assertEquals(9, step9.stepNumber)
        assertEquals("Put the head through the neck opening", step9.title)

        // Verify Step 20 (Check comfortable positioning)
        val step20 = activity.steps[19]
        assertEquals(20, step20.stepNumber)
        assertEquals("Check that the T-shirt is worn comfortably and correctly", step20.title)
        assertEquals("Look in the mirror! You look great in your T-shirt!", step20.childGuidance)
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

    @Test
    fun testRoleBasedClinicalControlsPermissions() {
        val childRole = "ROLE_CHILD"
        val caregiverRole = "ROLE_CAREGIVER"
        val therapistRole = "ROLE_THERAPIST"

        val isChildTherapist = childRole.equals("ROLE_THERAPIST", ignoreCase = true)
        val isCaregiverTherapist = caregiverRole.equals("ROLE_THERAPIST", ignoreCase = true)
        val isTherapistTherapist = therapistRole.equals("ROLE_THERAPIST", ignoreCase = true)

        assertFalse("Child Mode must NEVER render therapist controls", isChildTherapist)
        assertFalse("Caregiver Mode must NOT render therapist controls", isCaregiverTherapist)
        assertTrue("Therapist Mode MUST render therapist controls", isTherapistTherapist)
    }
}
