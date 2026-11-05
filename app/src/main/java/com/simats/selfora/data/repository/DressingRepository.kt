package com.simats.selfora.data.repository

import com.simats.selfora.data.model.dressing.*

class DressingRepository {

    fun getBoyTShirtActivity(): DressingActivity {
        val steps = listOf(
            DressingStep(
                stepId = "boy_tshirt_01",
                activityId = "boy_tshirt_activity",
                stepNumber = 1,
                title = "Stand or sit in front of the T-shirt",
                childGuidance = "Stand or sit in front of your T-shirt!",
                visualDescription = "Show the boy standing or sitting calmly in front of the T-shirt.",
                visualHighlight = "Boy in front of T-shirt",
                assetPath = "file:///android_asset/dressing/boy/tshirt/step_01.gif"
            ),
            DressingStep(
                stepId = "boy_tshirt_02",
                activityId = "boy_tshirt_activity",
                stepNumber = 2,
                title = "Look at the T-shirt",
                childGuidance = "Look at the T-shirt carefully!",
                visualDescription = "Show the boy focusing attention on the T-shirt.",
                visualHighlight = "T-Shirt on surface",
                assetPath = "file:///android_asset/dressing/boy/tshirt/step_02.gif"
            ),
            DressingStep(
                stepId = "boy_tshirt_03",
                activityId = "boy_tshirt_activity",
                stepNumber = 3,
                title = "Pick up the T-shirt with both hands",
                childGuidance = "Pick up your T-shirt with both hands!",
                visualDescription = "Show the boy picking up the T-shirt using both hands.",
                visualHighlight = "Both hands picking up T-shirt",
                assetPath = "file:///android_asset/dressing/boy/tshirt/step_03.gif"
            ),
            DressingStep(
                stepId = "boy_tshirt_04",
                activityId = "boy_tshirt_activity",
                stepNumber = 4,
                title = "Identify the front and back of the T-shirt",
                childGuidance = "Find the tag on the back and picture on the front!",
                visualDescription = "Show the boy identifying the front picture and back collar tag.",
                visualHighlight = "Front graphic and back collar tag",
                assetPath = "file:///android_asset/dressing/boy/tshirt/step_04.gif"
            ),
            DressingStep(
                stepId = "boy_tshirt_05",
                activityId = "boy_tshirt_activity",
                stepNumber = 5,
                title = "Find the bottom edge of the T-shirt",
                childGuidance = "Find the bottom opening of your shirt!",
                visualDescription = "Show the boy locating the lower hem opening of the T-shirt.",
                visualHighlight = "Bottom edge opening",
                assetPath = "file:///android_asset/dressing/boy/tshirt/step_05.gif"
            ),
            DressingStep(
                stepId = "boy_tshirt_06",
                activityId = "boy_tshirt_activity",
                stepNumber = 6,
                title = "Find the neck opening",
                childGuidance = "Look for the neck hole at the top!",
                visualDescription = "Show the boy holding the T-shirt and identifying the neck opening.",
                visualHighlight = "Neck opening",
                assetPath = "file:///android_asset/dressing/boy/tshirt/step_06.gif"
            ),
            DressingStep(
                stepId = "boy_tshirt_07",
                activityId = "boy_tshirt_activity",
                stepNumber = 7,
                title = "Hold the T-shirt at the shoulder areas",
                childGuidance = "Hold your shirt near both shoulders!",
                visualDescription = "Show the boy gripping both shoulder seams firmly.",
                visualHighlight = "Both shoulder seams",
                assetPath = "file:///android_asset/dressing/boy/tshirt/step_07.gif"
            ),
            DressingStep(
                stepId = "boy_tshirt_08",
                activityId = "boy_tshirt_activity",
                stepNumber = 8,
                title = "Lift the T-shirt to chest level",
                childGuidance = "Lift the shirt up to your chest!",
                visualDescription = "Show the boy lifting the T-shirt upward toward chest level.",
                visualHighlight = "T-shirt at chest height",
                assetPath = "file:///android_asset/dressing/boy/tshirt/step_08.gif"
            ),
            DressingStep(
                stepId = "boy_tshirt_09",
                activityId = "boy_tshirt_activity",
                stepNumber = 9,
                title = "Put the head through the neck opening",
                childGuidance = "Pop your head through the neck hole! Peekaboo!",
                visualDescription = "Show the boy guiding his head through the collar opening.",
                visualHighlight = "Head popping through collar",
                assetPath = "file:///android_asset/dressing/boy/tshirt/step_09.gif"
            ),
            DressingStep(
                stepId = "boy_tshirt_10",
                activityId = "boy_tshirt_activity",
                stepNumber = 10,
                title = "Pull the T-shirt down over the head",
                childGuidance = "Pull the shirt down around your neck!",
                visualDescription = "Show the boy pulling the collar down past his head onto the neck.",
                visualHighlight = "Collar resting around neck",
                assetPath = "file:///android_asset/dressing/boy/tshirt/step_10.gif"
            ),
            DressingStep(
                stepId = "boy_tshirt_11",
                activityId = "boy_tshirt_activity",
                stepNumber = 11,
                title = "Find the right sleeve opening",
                childGuidance = "Find the right sleeve hole!",
                visualDescription = "Show the boy locating the right sleeve opening.",
                visualHighlight = "Right sleeve opening",
                assetPath = "file:///android_asset/dressing/boy/tshirt/step_11.gif"
            ),
            DressingStep(
                stepId = "boy_tshirt_12",
                activityId = "boy_tshirt_activity",
                stepNumber = 12,
                title = "Put the right arm through the right sleeve",
                childGuidance = "Push your right arm through the sleeve!",
                visualDescription = "Show the boy pushing his right hand and arm through the right sleeve.",
                visualHighlight = "Right arm extending through sleeve",
                assetPath = "file:///android_asset/dressing/boy/tshirt/step_12.gif"
            ),
            DressingStep(
                stepId = "boy_tshirt_13",
                activityId = "boy_tshirt_activity",
                stepNumber = 13,
                title = "Pull the right sleeve up toward the shoulder",
                childGuidance = "Pull the sleeve up on your right shoulder!",
                visualDescription = "Show the boy pulling the right sleeve securely up onto the shoulder.",
                visualHighlight = "Right sleeve on shoulder",
                assetPath = "file:///android_asset/dressing/boy/tshirt/step_13.gif"
            ),
            DressingStep(
                stepId = "boy_tshirt_14",
                activityId = "boy_tshirt_activity",
                stepNumber = 14,
                title = "Find the left sleeve opening",
                childGuidance = "Find the left sleeve hole!",
                visualDescription = "Show the boy locating the left sleeve opening.",
                visualHighlight = "Left sleeve opening",
                assetPath = "file:///android_asset/dressing/boy/tshirt/step_14.gif"
            ),
            DressingStep(
                stepId = "boy_tshirt_15",
                activityId = "boy_tshirt_activity",
                stepNumber = 15,
                title = "Put the left arm through the left sleeve",
                childGuidance = "Push your left arm through the sleeve!",
                visualDescription = "Show the boy pushing his left hand and arm through the left sleeve.",
                visualHighlight = "Left arm extending through sleeve",
                assetPath = "file:///android_asset/dressing/boy/tshirt/step_15.gif"
            ),
            DressingStep(
                stepId = "boy_tshirt_16",
                activityId = "boy_tshirt_activity",
                stepNumber = 16,
                title = "Pull the left sleeve up toward the shoulder",
                childGuidance = "Pull the sleeve up on your left shoulder!",
                visualDescription = "Show the boy pulling the left sleeve securely up onto the shoulder.",
                visualHighlight = "Left sleeve on shoulder",
                assetPath = "file:///android_asset/dressing/boy/tshirt/step_16.gif"
            ),
            DressingStep(
                stepId = "boy_tshirt_17",
                activityId = "boy_tshirt_activity",
                stepNumber = 17,
                title = "Pull the front of the T-shirt down",
                childGuidance = "Pull the front of your shirt down!",
                visualDescription = "Show the boy pulling the front hem of the T-shirt down over his torso.",
                visualHighlight = "Front torso covered",
                assetPath = "file:///android_asset/dressing/boy/tshirt/step_17.gif"
            ),
            DressingStep(
                stepId = "boy_tshirt_18",
                activityId = "boy_tshirt_activity",
                stepNumber = 18,
                title = "Pull the back of the T-shirt down",
                childGuidance = "Pull the back of your shirt down smooth!",
                visualDescription = "Show the boy pulling the back hem of the T-shirt down over his waist.",
                visualHighlight = "Back waist covered",
                assetPath = "file:///android_asset/dressing/boy/tshirt/step_18.gif"
            ),
            DressingStep(
                stepId = "boy_tshirt_19",
                activityId = "boy_tshirt_activity",
                stepNumber = 19,
                title = "Straighten the T-shirt around the shoulders and waist",
                childGuidance = "Straighten your shirt nicely!",
                visualDescription = "Show the boy adjusting and smoothing the fabric across shoulders and waist.",
                visualHighlight = "Smooth neat shirt",
                assetPath = "file:///android_asset/dressing/boy/tshirt/step_19.gif"
            ),
            DressingStep(
                stepId = "boy_tshirt_20",
                activityId = "boy_tshirt_activity",
                stepNumber = 20,
                title = "Check that the T-shirt is worn comfortably and correctly",
                childGuidance = "Look in the mirror! You look great in your T-shirt!",
                visualDescription = "Show the boy standing proudly with the T-shirt worn comfortably and correctly.",
                visualHighlight = "Fully dressed happy boy",
                assetPath = "file:///android_asset/dressing/boy/tshirt/step_20.gif"
            )
        )

        return DressingActivity(
            activityId = "boy_tshirt_activity",
            title = "Boy T-Shirt Dressing",
            category = "Dressing",
            gender = GenderCategory.BOY,
            garmentType = "T-Shirt",
            totalSteps = 20,
            iconName = "ic_boy_tshirt",
            description = "20 Clinical task-analysis steps for independent T-shirt dressing.",
            steps = steps
        )
    }

    fun getGirlFrockActivity(): DressingActivity {
        val steps = listOf(
            DressingStep(
                stepId = "girl_frock_01",
                activityId = "girl_frock_activity",
                stepNumber = 1,
                title = "Look at the Frock",
                childGuidance = "Look at your beautiful Frock in the cupboard!",
                visualDescription = "Show the girl looking at a Frock inside an open cupboard.",
                visualHighlight = "Frock in Cupboard",
                assetPath = "file:///android_asset/dressing/girl/frock/step_01_look.gif"
            ),
            DressingStep(
                stepId = "girl_frock_02",
                activityId = "girl_frock_activity",
                stepNumber = 2,
                title = "Pick up the Frock",
                childGuidance = "Pick up your Frock with both hands!",
                visualDescription = "Show the girl reaching toward the Frock and picking it up.",
                visualHighlight = "Both Hands & Frock",
                assetPath = "file:///android_asset/dressing/girl/frock/step_02_pickup.gif"
            ),
            DressingStep(
                stepId = "girl_frock_03",
                activityId = "girl_frock_activity",
                stepNumber = 3,
                title = "Identify front and back tag",
                childGuidance = "Find the tag on the back!",
                visualDescription = "Show the girl examining the Frock tag.",
                visualHighlight = "Tag on Back",
                assetPath = "file:///android_asset/dressing/girl/frock/step_03_front_back.gif"
            ),
            DressingStep(
                stepId = "girl_frock_04",
                activityId = "girl_frock_activity",
                stepNumber = 4,
                title = "Find neck opening",
                childGuidance = "Look for the top neck hole!",
                visualDescription = "Show the girl locating top neck opening.",
                visualHighlight = "Top Neck Hole",
                assetPath = "file:///android_asset/dressing/girl/frock/step_04_neck.gif"
            ),
            DressingStep(
                stepId = "girl_frock_05",
                activityId = "girl_frock_activity",
                stepNumber = 5,
                title = "Hold Frock at shoulder straps",
                childGuidance = "Hold your frock at the shoulder straps!",
                visualDescription = "Show girl holding shoulder straps.",
                visualHighlight = "Shoulder Straps",
                assetPath = "file:///android_asset/dressing/girl/frock/step_05_shoulders.gif"
            ),
            DressingStep(
                stepId = "girl_frock_06",
                activityId = "girl_frock_activity",
                stepNumber = 6,
                title = "Lift Frock to chest level",
                childGuidance = "Lift the frock up high to your chest!",
                visualDescription = "Show girl lifting frock to chest level.",
                visualHighlight = "Frock at Chest",
                assetPath = "file:///android_asset/dressing/girl/frock/step_06_chest.gif"
            ),
            DressingStep(
                stepId = "girl_frock_07",
                activityId = "girl_frock_activity",
                stepNumber = 7,
                title = "Put head through neck opening",
                childGuidance = "Pop your head through the neck hole! Peekaboo!",
                visualDescription = "Show girl putting head through neck hole.",
                visualHighlight = "Head Through Neck Opening",
                assetPath = "file:///android_asset/dressing/girl/frock/step_07_head.gif"
            ),
            DressingStep(
                stepId = "girl_frock_08",
                activityId = "girl_frock_activity",
                stepNumber = 8,
                title = "Pull Frock down past head",
                childGuidance = "Pull the frock down past your head!",
                visualDescription = "Show girl pulling frock past head.",
                visualHighlight = "Frock Pulled Down",
                assetPath = "file:///android_asset/dressing/girl/frock/step_08_pull_down.gif"
            ),
            DressingStep(
                stepId = "girl_frock_09",
                activityId = "girl_frock_activity",
                stepNumber = 9,
                title = "Find right armhole",
                childGuidance = "Find the right arm hole!",
                visualDescription = "Show girl locating right armhole.",
                visualHighlight = "Right Armhole",
                assetPath = "file:///android_asset/dressing/girl/frock/step_09_right_armhole.gif"
            ),
            DressingStep(
                stepId = "girl_frock_10",
                activityId = "girl_frock_activity",
                stepNumber = 10,
                title = "Put right arm through armhole",
                childGuidance = "Push your right arm through!",
                visualDescription = "Show girl putting right arm through armhole.",
                visualHighlight = "Right Arm in Armhole",
                assetPath = "file:///android_asset/dressing/girl/frock/step_10_right_arm.gif"
            ),
            DressingStep(
                stepId = "girl_frock_11",
                activityId = "girl_frock_activity",
                stepNumber = 11,
                title = "Pull right sleeve to shoulder",
                childGuidance = "Pull the right sleeve up onto your shoulder!",
                visualDescription = "Show girl adjusting right strap/sleeve.",
                visualHighlight = "Right Sleeve on Shoulder",
                assetPath = "file:///android_asset/dressing/girl/frock/step_11_right_shoulder.gif"
            ),
            DressingStep(
                stepId = "girl_frock_12",
                activityId = "girl_frock_activity",
                stepNumber = 12,
                title = "Find left armhole",
                childGuidance = "Find the left arm hole!",
                visualDescription = "Show girl locating left armhole.",
                visualHighlight = "Left Armhole",
                assetPath = "file:///android_asset/dressing/girl/frock/step_12_left_armhole.gif"
            ),
            DressingStep(
                stepId = "girl_frock_13",
                activityId = "girl_frock_activity",
                stepNumber = 13,
                title = "Put left arm through armhole",
                childGuidance = "Push your left arm through!",
                visualDescription = "Show girl putting left arm through armhole.",
                visualHighlight = "Left Arm in Armhole",
                assetPath = "file:///android_asset/dressing/girl/frock/step_13_left_arm.gif"
            ),
            DressingStep(
                stepId = "girl_frock_14",
                activityId = "girl_frock_activity",
                stepNumber = 14,
                title = "Pull left sleeve to shoulder",
                childGuidance = "Pull the left sleeve up onto your shoulder!",
                visualDescription = "Show girl adjusting left strap/sleeve.",
                visualHighlight = "Left Sleeve on Shoulder",
                assetPath = "file:///android_asset/dressing/girl/frock/step_14_left_shoulder.gif"
            ),
            DressingStep(
                stepId = "girl_frock_15",
                activityId = "girl_frock_activity",
                stepNumber = 15,
                title = "Pull dress front down",
                childGuidance = "Pull the dress front down to your waist!",
                visualDescription = "Show girl pulling dress front down to waist.",
                visualHighlight = "Dress Front",
                assetPath = "file:///android_asset/dressing/girl/frock/step_15_front.gif"
            ),
            DressingStep(
                stepId = "girl_frock_16",
                activityId = "girl_frock_activity",
                stepNumber = 16,
                title = "Pull skirt back down",
                childGuidance = "Pull the back skirt down smooth!",
                visualDescription = "Show girl pulling back skirt down.",
                visualHighlight = "Back Skirt",
                assetPath = "file:///android_asset/dressing/girl/frock/step_16_back.gif"
            ),
            DressingStep(
                stepId = "girl_frock_17",
                activityId = "girl_frock_activity",
                stepNumber = 17,
                title = "Straighten & adjust frock",
                childGuidance = "Smooth out your pretty frock!",
                visualDescription = "Show girl smoothing out frock.",
                visualHighlight = "Frock Surface",
                assetPath = "file:///android_asset/dressing/girl/frock/step_17_straighten.gif"
            ),
            DressingStep(
                stepId = "girl_frock_18",
                activityId = "girl_frock_activity",
                stepNumber = 18,
                title = "Check comfortable fit",
                childGuidance = "You look like a princess! All ready!",
                visualDescription = "Show girl standing comfortably in frock with victory animation.",
                visualHighlight = "Princess Victory Completion State",
                assetPath = "file:///android_asset/dressing/girl/frock/step_18_complete.gif"
            )
        )

        return DressingActivity(
            activityId = "girl_frock_activity",
            title = "Girl Frock Dressing",
            category = "Dressing",
            gender = GenderCategory.GIRL,
            garmentType = "Frock",
            totalSteps = 18,
            iconName = "ic_girl_frock",
            description = "18 Clinical task-analysis steps for independent Frock dressing.",
            steps = steps
        )
    }

    fun getEatingActivity(): DressingActivity {
        val titles = listOf(
            "Look at food & spoon", "Hold spoon with proper grip", "Dip spoon into bowl",
            "Scoop food into spoon", "Lift spoon to mouth", "Open mouth wide",
            "Place food into mouth", "Chew food thoroughly", "Swallow food comfortably", "Wipe mouth with napkin"
        )
        val childTexts = listOf(
            "Look at your tasty meal and spoon! 🥣🥄", "Hold your spoon firmly! 🥄", "Dip your spoon into the food! 🍲",
            "Scoop up a yummy mouthful! 😋", "Lift the spoon carefully to your mouth! 🌟", "Open your mouth wide! Ahhh! 😮",
            "Put the spoon inside your mouth! Yum! 😋", "Chew your food nicely! 🍎", "Swallow your bite! Good job! 👍",
            "Wipe your mouth with a napkin! Clean & happy! ✨"
        )
        val highlights = listOf(
            "Food Bowl & Spoon", "Hand & Spoon Handle", "Bowl & Spoon Tip",
            "Food on Spoon", "Spoon Lift Action", "Open Mouth",
            "Mouth & Spoon", "Chewing Motion", "Swallowing Action", "Napkin & Clean Mouth"
        )

        val steps = titles.mapIndexed { idx, title ->
            DressingStep(
                stepId = "eating_${idx + 1}",
                activityId = "eating_activity",
                stepNumber = idx + 1,
                title = title,
                childGuidance = childTexts[idx],
                visualDescription = "Demonstrate step ${idx + 1} of self-feeding with spoon: $title",
                visualHighlight = highlights[idx],
                assetPath = "file:///android_asset/eating/step_${String.format("%02d", idx + 1)}.gif"
            )
        }

        return DressingActivity(
            activityId = "eating_activity",
            title = "Eating & Self-Feeding",
            category = "Eating",
            gender = GenderCategory.UNISEX,
            garmentType = "Utensil",
            totalSteps = 10,
            iconName = "ic_spoon",
            description = "10 Clinical task-analysis steps for independent spoon eating.",
            steps = steps
        )
    }

    fun getShoesActivity(): DressingActivity {
        val titles = listOf(
            "Look at shoes & socks", "Pick up right sock", "Pull sock open with hands",
            "Push toes into sock", "Pull sock over heel", "Unfasten velcro straps",
            "Push foot into shoe", "Pull heel tab up", "Press velcro strap tight", "Stand up & check fit"
        )
        val childTexts = listOf(
            "Look at your cool shoes and socks! 👟", "Pick up your right sock! 🧦", "Open up the sock hole with both hands! ⭕",
            "Slide your toes into the sock! 🦶", "Pull the sock up over your heel! 🧦", "Open up the shoe velcro straps! 👟",
            "Slide your foot all the way into the shoe! 🦶", "Pull up the shoe back tab! 👟", "Press the velcro strap tight and secure! 🔒",
            "You put on your shoes! Stand up & high five! 🙌⭐"
        )
        val highlights = listOf(
            "Shoes & Socks", "Right Sock", "Sock Opening",
            "Toes in Sock", "Heel & Sock", "Velcro Straps",
            "Foot & Shoe Tongue", "Heel Tab", "Velcro Lock", "Standing Comfortably Fit"
        )

        val steps = titles.mapIndexed { idx, title ->
            DressingStep(
                stepId = "shoes_${idx + 1}",
                activityId = "shoes_activity",
                stepNumber = idx + 1,
                title = title,
                childGuidance = childTexts[idx],
                visualDescription = "Demonstrate step ${idx + 1} of wearing shoes and socks: $title",
                visualHighlight = highlights[idx],
                assetPath = "file:///android_asset/shoes/step_${String.format("%02d", idx + 1)}.gif"
            )
        }

        return DressingActivity(
            activityId = "shoes_activity",
            title = "Shoes & Socks Training",
            category = "Dressing",
            gender = GenderCategory.UNISEX,
            garmentType = "Shoes",
            totalSteps = 10,
            iconName = "ic_shoes",
            description = "10 Clinical task-analysis steps for putting on socks and shoes.",
            steps = steps
        )
    }

    fun getActivityById(activityId: String): DressingActivity {
        return when {
            activityId.contains("girl", ignoreCase = true) || activityId.contains("frock", ignoreCase = true) -> getGirlFrockActivity()
            activityId.contains("eating", ignoreCase = true) || activityId.contains("food", ignoreCase = true) -> getEatingActivity()
            activityId.contains("shoes", ignoreCase = true) || activityId.contains("socks", ignoreCase = true) -> getShoesActivity()
            else -> getBoyTShirtActivity()
        }
    }
}
