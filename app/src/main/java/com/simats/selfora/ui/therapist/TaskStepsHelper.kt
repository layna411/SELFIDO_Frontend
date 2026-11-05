package com.simats.selfora.ui.therapist





import androidx.compose.foundation.background


import androidx.compose.foundation.clickable


import androidx.compose.foundation.layout.*


import androidx.compose.foundation.shape.RoundedCornerShape


import androidx.compose.material3.Text


import androidx.compose.runtime.Composable


import androidx.compose.ui.Alignment


import androidx.compose.ui.Modifier


import androidx.compose.ui.draw.clip


import androidx.compose.ui.graphics.Color


import androidx.compose.ui.text.font.FontWeight


import androidx.compose.ui.unit.dp


import androidx.compose.ui.unit.sp


import com.simats.selfora.data.model.TaskStepResponse


import com.simats.selfora.ui.theme.SelforaTextPrimary





fun getSampleTShirtSteps(): List<TaskStepResponse> {
    return SelfidoCatalogue.getStepsForActivity(1L)
}

fun getSampleShirtSteps(): List<TaskStepResponse> {
    return SelfidoCatalogue.getStepsForActivity(2L)
}





fun getSampleGirlFrockSteps(): List<TaskStepResponse> {


    return getSampleTShirtSteps().map {


        it.copy(title = it.title.replace("T-shirt", "Frock").replace("shirt", "frock"))


    }


}





fun getSampleEatingSteps(): List<TaskStepResponse> {


    val eatingSteps = listOf(


        "Grasp spoon handle",


        "Dip spoon into bowl",


        "Scoop food onto spoon",


        "Lift spoon carefully",


        "Bring spoon to mouth",


        "Open mouth",


        "Place spoon inside mouth",


        "Close lips around spoon",


        "Remove empty spoon",


        "Chew and swallow"


    )


    return eatingSteps.mapIndexed { index, title ->


        TaskStepResponse(


            id = (100 + index + 1).toLong(),


            activityId = 2L,


            stepNumber = index + 1,


            title = title,


            instructionText = title,


            childInstruction = title,


            audioPromptUrl = null,


            media = emptyList()


        )


    }


}





fun getSampleShoesSteps(): List<TaskStepResponse> {


    val shoesSteps = listOf(


        "Unfasten shoe straps",


        "Open shoe wide",


        "Align sock with foot",


        "Pull sock over toes",


        "Pull sock over heel",


        "Insert toes into shoe",


        "Push heel down into shoe",


        "Pull tongue of shoe straight",


        "Fasten shoe strap",


        "Check shoe fit"


    )


    return shoesSteps.mapIndexed { index, title ->


        TaskStepResponse(


            id = (200 + index + 1).toLong(),


            activityId = 3L,


            stepNumber = index + 1,


            title = title,


            instructionText = title,


            childInstruction = title,


            audioPromptUrl = null,


            media = emptyList()


        )


    }


}





fun getSampleToothBrushingSteps(): List<TaskStepResponse> {


    val toothBrushingSteps = listOf(


        Pair("Pick up toothbrush", "Pick up your toothbrush by the handle."),


        Pair("Apply toothpaste", "Squeeze a pea-sized amount of toothpaste onto the bristles."),


        Pair("Brush your teeth", "Move the toothbrush all around your teeth gently."),


        Pair("Brush tongue", "Gently brush the surface of your tongue."),


        Pair("Rinse mouth", "Take a sip of water, swish, and spit it into the sink."),


        Pair("Rinse toothbrush", "Rinse the toothbrush bristles under clean running water."),


        Pair("Wipe mouth", "Wipe your mouth with a clean towel.")


    )


    return toothBrushingSteps.mapIndexed { index, (title, instruction) ->


        TaskStepResponse(


            id = (300 + index + 1).toLong(),


            activityId = 4L,


            stepNumber = index + 1,


            title = title,


            instructionText = instruction,


            childInstruction = instruction,


            audioPromptUrl = null,


            media = emptyList()


        )


    }


}





@Composable


fun PromptHierarchyBar(


    selectedLevel: Int,


    isUnable: Boolean,


    onLevelSelected: (Int) -> Unit,


    onUnableSelected: () -> Unit


) {


    val prompts = listOf(


        0 to "L0",


        1 to "L1",


        2 to "L2",


        3 to "L3",


        4 to "L4",


        5 to "L5",


        6 to "L6"


    )


    Row(


        modifier = Modifier


            .fillMaxWidth()


            .clip(RoundedCornerShape(12.dp))


            .background(Color.White)


            .padding(4.dp),


        horizontalArrangement = Arrangement.SpaceBetween


    ) {


        prompts.forEach { (levelNum, label) ->


            val isSelected = !isUnable && selectedLevel == levelNum


            Box(


                modifier = Modifier


                    .clip(RoundedCornerShape(8.dp))


                    .background(if (isSelected) com.simats.selfora.ui.theme.SelforaPrimary else Color.Transparent)


                    .clickable { onLevelSelected(levelNum) }


                    .padding(horizontal = 6.dp, vertical = 6.dp),


                contentAlignment = Alignment.Center


            ) {


                Text(


                    text = label,


                    fontSize = 11.sp,


                    fontWeight = FontWeight.Bold,


                    color = if (isSelected) Color.White else SelforaTextPrimary


                )


            }


        }


        Box(


            modifier = Modifier


                .clip(RoundedCornerShape(8.dp))


                .background(if (isUnable) com.simats.selfora.ui.theme.SelforaError else Color.Transparent)


                .clickable { onUnableSelected() }


                .padding(horizontal = 6.dp, vertical = 6.dp),


            contentAlignment = Alignment.Center


        ) {


            Text(


                text = "Unable",


                fontSize = 11.sp,


                fontWeight = FontWeight.Bold,


                color = if (isUnable) Color.White else SelforaTextPrimary


            )


        }


    }


}


