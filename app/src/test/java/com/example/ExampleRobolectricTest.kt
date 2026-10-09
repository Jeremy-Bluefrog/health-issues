package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.InitialSymptomInput
import com.example.data.model.TriageLevel
import com.example.data.remote.ClinicalKnowledgeEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("症狀AI隨身問", appName)
  }

  @Test
  fun `test symptom inquiry triage for abdominal pain`() {
    val input = InitialSymptomInput(
      description = "右下腹悶痛，走路震動時更痛，食慾差且微燒",
      duration = "1天",
      painLevel = 7
    )
    val result = ClinicalKnowledgeEngine.analyze(input, emptyList(), isFinal = false, round = 1)
    assertNotNull(result)
    assertTrue("Should contain follow-up questions", result.followUpQuestions.isNotEmpty())
    assertTrue("Should suggest possible causes", result.possibleCauses.isNotEmpty())
    assertTrue("Should recommend department", result.recommendedDepartments.isNotEmpty())
  }
}

