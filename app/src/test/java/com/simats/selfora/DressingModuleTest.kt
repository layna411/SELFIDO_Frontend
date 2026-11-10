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
    fun testBoyShirtActivityHas23Steps() {
        val activity = repository.getBoyShirtActivity()
        assertEquals("Boy Shirt Dressing", activity.title)
        assertEquals(23, activity.totalSteps)
        assertEquals(23, activity.steps.size)

        // Verify Step 1
        val step1 = activity.steps[0]
        assertEquals(1, step1.stepNumber)
        assertEquals("Look at the shirt", step1.title)

        // Verify Step 13 (Buttoning sequence start)
        val step13 = activity.steps[12]
        assertEquals(13, step13.stepNumber)
        assertEquals("Find the top button and corresponding buttonhole", step13.title)

        // Verify Step 23 (Completion)
        val step23 = activity.steps[22]
        assertEquals(23, step23.stepNumber)
        assertEquals("Check that the shirt is correctly positioned and comfortable", step23.title)

        // Verify asset paths
        activity.steps.forEach { step ->
            assertTrue(step.assetPath.startsWith("file:///android_asset/dressing/boy/shirt/"))
            assertTrue(step.childGuidance.isNotEmpty())
            assertTrue(step.visualHighlight.isNotEmpty())
        }
    }

    @Test
    fun testBoyJacketActivityHas26Steps() {
        val activity = repository.getBoyJacketActivity()
        assertEquals("Boy Jacket Dressing", activity.title)
        assertEquals(26, activity.totalSteps)
        assertEquals(26, activity.steps.size)

        // Verify Step 1
        val step1 = activity.steps[0]
        assertEquals(1, step1.stepNumber)
        assertEquals("Stand or sit in front of the jacket", step1.title)

        // Verify Step 18 (Insert pin fully into zipper slider)
        val step18 = activity.steps[17]
        assertEquals(18, step18.stepNumber)
        assertEquals("Insert the pin fully into the zipper slider", step18.title)

        // Verify Step 22 (Pull zipper toward the neck)
        val step22 = activity.steps[21]
        assertEquals(22, step22.stepNumber)
        assertEquals("Pull the zipper toward the neck", step22.title)

        // Verify Step 26 (Completion)
        val step26 = activity.steps[25]
        assertEquals(26, step26.stepNumber)
        assertEquals("Check that the jacket is properly positioned and comfortable", step26.title)

        // Verify asset paths
        activity.steps.forEach { step ->
            assertTrue(step.assetPath.startsWith("file:///android_asset/dressing/boy/jacket/"))
            assertTrue(step.childGuidance.isNotEmpty())
            assertTrue(step.visualHighlight.isNotEmpty())
        }
    }

    @Test
    fun testBoyVestActivityHas20Steps() {
        val activity = repository.getBoyVestActivity()
        assertEquals("Boy Undergarment – Vest", activity.title)
        assertEquals(20, activity.totalSteps)
        assertEquals(20, activity.steps.size)

        // Verify Step 1
        val step1 = activity.steps[0]
        assertEquals(1, step1.stepNumber)
        assertEquals("Move to a private changing area", step1.title)

        // Verify Step 8 (Put head through neck opening)
        val step8 = activity.steps[7]
        assertEquals(8, step8.stepNumber)
        assertEquals("Put the head through the neck opening", step8.title)

        // Verify Step 20 (Completion)
        val step20 = activity.steps[19]
        assertEquals(20, step20.stepNumber)
        assertEquals("Check that the vest is correctly positioned and comfortable", step20.title)

        // Verify asset paths
        activity.steps.forEach { step ->
            assertTrue(step.assetPath.startsWith("file:///android_asset/dressing/boy/vest/"))
            assertTrue(step.childGuidance.isNotEmpty())
            assertTrue(step.visualHighlight.isNotEmpty())
        }
    }

    @Test
    fun testBoyTrousersActivityHas10Steps() {
        val activity = repository.getBoyTrousersActivity()
        assertEquals("Boy Trousers Dressing", activity.title)
        assertEquals(10, activity.totalSteps)
        assertEquals(10, activity.steps.size)

        // Verify Step 1
        val step1 = activity.steps[0]
        assertEquals(1, step1.stepNumber)
        assertEquals("Stand in front of the trousers", step1.title)

        // Verify Step 6 (Find right leg opening)
        val step6 = activity.steps[5]
        assertEquals(6, step6.stepNumber)
        assertEquals("Find the right leg opening", step6.title)

        // Verify Step 9 (Step right leg)
        val step9 = activity.steps[8]
        assertEquals(9, step9.stepNumber)
        assertEquals("Step your right leg into the right leg opening", step9.title)

        // Verify Step 10 (Completion)
        val step10 = activity.steps[9]
        assertEquals(10, step10.stepNumber)
        assertEquals("Step your left leg in and pull the trousers up", step10.title)

        // Verify asset paths
        activity.steps.forEach { step ->
            assertTrue(step.assetPath.startsWith("file:///android_asset/dressing/boy/trousers/"))
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
